package org.zenbeni.jedis.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenbeni.jedis.exception.JedisJobException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

/**
 * Represents a job to be executed against redis.
 * It is used with JedisExecutor to ensure thread-safety which is critical in Storm.
 *
 * @param <T> the result type of the JedisJob.
 */
public abstract class JedisJob<T> implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(JedisJob.class);

	static final int DEFAULT_MAX_RETRIES = 4;
	static final int DEFAULT_BACKOFF = 100;

	protected JedisJobConfiguration configuration;
	protected JedisExecutor executor;
	protected Jedis jedis;
	protected T result;

	protected int maxRetries;
	protected int backoff;

	public JedisJob() {
		this(new JedisJobConfiguration());
	}

	public JedisJob(final JedisJobConfiguration configuration) {
		this.configuration = configuration;
	}

	protected void init() {
		initConfiguration();
		initBackOffPolicy();
	}

	protected void initConfiguration() {
		LOGGER.debug("Job configuration:{}", configuration);
	}

	protected void initBackOffPolicy() {
		maxRetries = DEFAULT_MAX_RETRIES;
		backoff = DEFAULT_BACKOFF;
		LOGGER.debug("BackOff policy: maxRetries={} backoffFactor={}", maxRetries, backoff);
	}

	protected void initExecutor(final JedisExecutor executor) {
		this.executor = executor;
		this.executor.setConfiguration(configuration);
	}

	protected void checkConfiguration() {
		if (configuration == null) {
			throw new JedisJobException("No jedis configuration was provided!");
		}
		if (executor == null) {
			throw new JedisJobException("No jedis executor was defined!");
		}
	}

	@Override
	public final void run() {
		jedis = executor.spawnJedis();
		result = executeJedisJob(0, 0);
	}

	protected T executeJedisJob(final int retry, final long time) {
		try {
			return runJedisJob();

		} catch (JedisException e) {

			// Check if jedis instance is safe
			if (checkJedis(jedis)) {
				throw new JedisJobException("Error on JedisJob, bad jedis instance", e);
			}

			// Check if exponential backoff is still active
			if (retry >= maxRetries) {
				final String message = String.format("Could not build a safe jedis instance. Is redis up? Waited:%sms tried:%s", time, maxRetries);
				throw new JedisJobException(message, e);
			}

			// Sleep when exponential backoff is active
			final long delay = delay(backoff, retry);
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e1) {
				LOGGER.warn("Interrupted sleep? {} Pool:{}", e1, executor);
			}

			// Retry execution of JedisJob
			return executeJedisJob(retry + 1, time + delay);
		}
	}

	/**
	 * Implementation of the job.
	 *
	 * @return whatever you want depending on your implementation.
	 */
	public abstract T runJedisJob();

	public T getResult() {
		return result;
	}

	public int getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(final int maxRetries) {
		this.maxRetries = maxRetries;
	}

	public int getBackoff() {
		return backoff;
	}

	public void setBackoff(final int backoff) {
		this.backoff = backoff;
	}

	/**
	 * Test if jedis can ping redis
	 *
	 * @param jedis a not thread safe instance of jedis, do not share!
	 * @return true if it is a safe jedis instance
	 */
	static boolean checkJedis(final Jedis jedis) {
		try {
			jedis.ping();
		} catch (JedisException e) {
			LOGGER.warn("Error on jedis! {}", e);
			try {
				// Try to kindly close the connection.
				jedis.close();
			} catch (JedisException f) {
				LOGGER.warn("Error on jedis! {}", f);
			}
			return false;
		}
		return true;
	}

	static long delay(final int backoff, final int retry) {
		return backoff + (long) (backoff * Math.random() * retry);
	}

}
