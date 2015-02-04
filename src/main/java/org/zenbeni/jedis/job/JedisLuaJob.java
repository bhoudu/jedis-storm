package org.zenbeni.jedis.job;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenbeni.jedis.lua.LuaScript;
import org.zenbeni.jedis.lua.LuaScriptThreadLocalCache;

public abstract class JedisLuaJob<T> extends JedisJob<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(JedisLuaJob.class);

	protected LuaScript<T> luaScript;
	protected final List<String> keys = new ArrayList<>();
	protected final List<String> argv = new ArrayList<>();

	public JedisLuaJob() {
	}

	public JedisLuaJob(final JedisJobConfiguration configuration) {
		super(configuration);
	}

	public JedisLuaJob(final JedisJobConfiguration configuration, final LuaScript<T> luaScript) {
		super(configuration);
		this.luaScript = luaScript;
	}

	@Override
	protected void initExecutor() {
		super.initExecutor();
		initLuaScript();
	}

	protected abstract void initLuaScript();

	protected void initKeysAndArgv() {
		LOGGER.debug("Init keys and argv before calling lua script:{}", luaScript);
	}

	@Override
	public T runJedisJob() {
		initKeysAndArgv();
		LOGGER.debug("Calling lua script:{} jedis:{} with KEYS:{} and ARGV:{}",
		             luaScript,
		             jedis,
		             StringUtils.abbreviate(keys.toString(), 50),
		             StringUtils.abbreviate(argv.toString(), 50));
		// Get an instance from current thread or put it in cache (1 LuaScript per thread)
		final LuaScript<T> script = LuaScriptThreadLocalCache.getLuaScript(luaScript);
		final T result = script.eval(jedis, keys, argv);
		LOGGER.debug("Result for lua script:{} jedis:{} result:{}", luaScript, jedis, result);
		return result;
	}

}
