package org.zenbeni.jedis.job;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenbeni.jedis.exception.JedisJobException;
import org.zenbeni.jedis.lua.LuaScript;
import org.zenbeni.jedis.lua.LuaScriptThreadLocalCache;

/**
 * Represents a JedisJob which is lua script call in redis.
 * It needs an actual lua script instance to run.
 *
 * @param <T> the result type of the lua call.
 */
public abstract class JedisLuaJob<T> extends JedisJob<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(JedisLuaJob.class);

	protected LuaScript<T> luaScript;
	protected final List<String> keys = new ArrayList<>();
	protected final List<String> argv = new ArrayList<>();

	public JedisLuaJob() {
		this(new JedisJobConfiguration());
	}

	public JedisLuaJob(final JedisJobConfiguration jobConfiguration) {
		this(jobConfiguration, null);
	}

	public JedisLuaJob(final JedisJobConfiguration jobConfiguration, final LuaScript<T> script) {
		super(jobConfiguration);
		luaScript = script;
	}

	@Override
	protected void init() {
		super.init();
		initLuaScript();
		initKeysAndArgv();
	}

	@Override
	protected void checkConfiguration() {
		super.checkConfiguration();
		if (luaScript == null) {
			throw new JedisJobException("No lua script was defined for the job!");
		}
	}

	protected void initLuaScript() {
		LOGGER.debug("Init keys and argv before calling lua script:{}", luaScript);
	}

	protected void initKeysAndArgv() {
		LOGGER.debug("Init keys and argv before calling lua script:{}", luaScript);
	}

	@Override
	public T runJedisJob() {
		LOGGER.info("Calling lua script:{} jedis:{} with KEYS:{} and ARGV:{}",
		            luaScript,
		            jedis,
		            StringUtils.abbreviate(keys.toString(), 50),
		            StringUtils.abbreviate(argv.toString(), 50));
		// Get an instance from current thread or put it in cache (LuaScript instances should not be shared between threads)
		final LuaScript<T> script = LuaScriptThreadLocalCache.getLuaScript(luaScript);
		final T result = script.eval(jedis, keys, argv);
		LOGGER.info("Result for lua script:{} jedis:{} result:{}", luaScript, jedis, result);
		return result;
	}

}
