package org.zenbeni.jedis.job;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenbeni.jedis.lua.LuaScript;

public abstract class JedisLuaJob<T> extends JedisJob<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(JedisLuaJob.class);

	protected LuaScript<T> luaScript;
	protected List<String> keys = new ArrayList<>();
	protected List<String> argv = new ArrayList<>();

	public abstract void initLuaScript();

	@Override
	public void initExecutor() {
		super.initExecutor();
		initLuaScript();
	}

	public void initKeysAndArgv() {
		LOGGER.debug("Init keys and argv before calling lua script:{}", luaScript);
	}

	@Override
	public T runJedisJob() {
		initKeysAndArgv();
		LOGGER.debug("Calling lua script:{} with KEYS:{} and ARGV:{}", luaScript, keys, argv);
		return luaScript.eval(jedis, keys, argv);
	}

}
