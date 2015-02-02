package org.zenbeni.jedis.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

public final class JedisExecutor {

	private static final Logger LOGGER = LoggerFactory.getLogger(JedisExecutor.class);

	private JedisJobConfiguration configuration;
	private Jedis jedis;

	JedisExecutor() {
		configuration = new JedisJobConfiguration();
	}

	void setConfiguration(final JedisJobConfiguration configuration) {
		if (!this.configuration.equals(configuration)) {
			// Flush cached jedis instance if configuration has changed
			flushJedis();
		}
		this.configuration = configuration;
	}

	/**
	 * Build an untested jedis instance or return a cached one.
	 *
	 * @return unsafe jedis instance
	 */
	Jedis spawnJedis() {
		if (jedis != null) {
			return jedis;
		} else {
			jedis = new Jedis(configuration.getHost(), configuration.getPort(), configuration.getTimeout());
			jedis.select(configuration.getDatabase());
			return jedis;
		}
	}

	void flushJedis() {
		if (jedis != null) {
			try {
				jedis.close();
			} catch (JedisException e) {
				LOGGER.error("Cannot close jedis instance:{} Error:{}", jedis, e);
			}
		}
		jedis = null;
	}

	public static <T> T submitJedisJob(final JedisJob<T> jedisJob) {
		jedisJob.initJedis();
		jedisJob.run();
		return jedisJob.getResult();
	}

}
