package org.zenbeni.jedis.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenbeni.jedis.exception.JedisJobException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

public abstract class JedisJob<T> implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(JedisJob.class);

	static final int DEFAULT_MAX_RETRIES = 4;
	static final int DEFAULT_BACKOFF = 100;

	protected JedisJobConfiguration configuration;
	protected JedisExecutor executor;
	protected Jedis jedis;
	protected T result;

	protected int maxRetries = DEFAULT_MAX_RETRIES;
	protected int backoff = DEFAULT_BACKOFF;

	/**
	 * Store one jedis executor per thread (storm executor)
	 */
	static final ThreadLocal<JedisExecutor> JEDIS_EXECUTOR_THREAD_LOCAL = new ThreadLocal<JedisExecutor>() {
		@Override
		protected JedisExecutor initialValue() {
			return new JedisExecutor();
		}
	};

	public JedisJob() {
		initConfiguration();
		initExecutor();
	}

	public JedisJob(final JedisJobConfiguration configuration) {
		this.configuration = configuration;
		initExecutor();
	}

	protected void initConfiguration() {
		configuration = new JedisJobConfiguration();
	}

	protected void initExecutor() {
		executor = JEDIS_EXECUTOR_THREAD_LOCAL.get();
		executor.setConfiguration(configuration);
	}

	protected void initJedis() {
		jedis = executor.spawnJedis();
	}

	@Override
	public final void run() {
		result = executeJedisJob(0, 0);
	}

	protected T executeJedisJob(final int retry, final long time) {
		try {
			return runJedisJob();
		} catch (JedisException e) {
			LOGGER.warn("{}", e);
			if (checkJedis(jedis)) {
				throw new JedisJobException("Error on JedisJob:{}", e);
			}
			if (retry >= maxRetries) {
				final String message = String.format("Could not build a safe jedis instance. Is redis up? Waited:%sms tried:%s", time, maxRetries);
				throw new JedisJobException(message);
			}
			final long delay = delay(backoff, retry);
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e1) {
				LOGGER.warn("Interrupted sleep? {} Pool:{}", e1, executor);
			}
			return executeJedisJob(retry + 1, time + delay);
		}
	}

	public abstract T runJedisJob();

	public T getResult() {
		return result;
	}

	/**
	 * Test if jedis can ping redis
	 *
	 * @param jedis
	 * @return true if it is a safe jedis instance
	 */
	static boolean checkJedis(final Jedis jedis) {
		try {
			jedis.ping();
		} catch (JedisException e) {
			LOGGER.warn("Error on jedis! {}", e);
			try {
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
