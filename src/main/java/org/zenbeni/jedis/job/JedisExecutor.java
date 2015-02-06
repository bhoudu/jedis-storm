package org.zenbeni.jedis.job;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenbeni.jedis.exception.JedisJobException;
import org.zenbeni.jedis.lua.LuaScript;

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
			if (!StringUtils.isEmpty(configuration.getPassword())) {
				final String auth = jedis.auth(configuration.getPassword());
				if ("OK".equals(auth)) {
					LOGGER.debug("AUTH SUCCEDEED:{}", jedis);
				} else {
					final String message = String.format("NOT AUTHORIZED TO LOG IN REDIS:%s", configuration);
					throw new JedisJobException(message);
				}
			}
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

	/**
	 * Store one jedis executor per thread (storm executor)
	 */
	static final ThreadLocal<JedisExecutor> JEDIS_EXECUTOR_THREAD_LOCAL = new ThreadLocal<JedisExecutor>() {
		@Override
		protected JedisExecutor initialValue() {
			return new JedisExecutor();
		}
	};

	/**
	 * Execute a JedisJob against Redis.
	 * This method is thread-safe and can be safely called in any Bolt / Spout.
	 * It supports an exponential backoff policy to automatically retry failed jobs.
	 *
	 * @param jedisJob implementation of a job against redis.
	 * @param <T>      the result type of the JedisJob
	 * @return the result of the JedisJob
	 */
	public static <T> T submitJedisJob(final JedisJob<T> jedisJob) {
		jedisJob.init();
		jedisJob.initExecutor(JEDIS_EXECUTOR_THREAD_LOCAL.get());
		jedisJob.checkConfiguration();
		jedisJob.run();
		return jedisJob.getResult();
	}

	public static <T> T submitLuaJob(final LuaScript<T> script) {
		return submitLuaJob(new JedisJobConfiguration(), script);
	}

	public static <T> T submitLuaJob(final JedisJobConfiguration jobConfiguration, final LuaScript<T> script) {
		return submitLuaJob(jobConfiguration, script, Collections.<String>emptyList());
	}

	public static <T> T submitLuaJob(final JedisJobConfiguration jobConfiguration, final LuaScript<T> script, final List<String> keysJob) {
		return submitLuaJob(jobConfiguration, script, keysJob, Collections.<String>emptyList());
	}

	public static <T> T submitLuaJob(final JedisJobConfiguration jobConfiguration, final LuaScript<T> script, final List<String> keysJob, final List<String> argvJob) {
		final JedisLuaJob<T> job = new JedisLuaJob<T>(jobConfiguration, script) {
			@Override
			protected void initKeysAndArgv() {
				keys.addAll(keysJob);
				argv.addAll(argvJob);
			}
		};
		return submitJedisJob(job);
	}

}
