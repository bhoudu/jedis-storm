package org.zenbeni.jedis.lua;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

/**
 * Wrapper of lua script which always try to eval with sha1 id on redis.
 * If the script does not exist, it catches the exception from redis and sends the script again.
 */
public class LuaScript<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(LuaScript.class);

	private final String path;
	private final String luaScript;
	private final String sha1;

	public static LuaScript buildFromPath(final String path) {
		final InputStream inputStream = LuaScript.class.getResourceAsStream(path);
		final String script = LuaHelper.convertStreamToString(inputStream);
		return buildFromScript(script, path);
	}

	public static LuaScript buildFromScript(final String luaScript) {
		return new LuaScript(luaScript, "undefined");
	}

	static LuaScript buildFromScript(final String luaScript, final String path) {
		return new LuaScript(luaScript, path);
	}

	private LuaScript(final String luaScript, final String path) {
		this(luaScript, LuaHelper.generateSHA1(luaScript), path);
	}

	private LuaScript(final String luaScript, final String sha1, final String path) {
		this.luaScript = luaScript;
		this.sha1 = sha1;
		this.path = path;
	}

	public T eval(final Jedis jedis) {
		return eval(jedis, Collections.<String>emptyList(), Collections.<String>emptyList());
	}

	public T eval(final Jedis jedis, final List<String> keys) {
		return eval(jedis, keys, Collections.<String>emptyList());
	}

	public T eval(final Jedis jedis, final List<String> keys, final List<String> args) {
		T result;
		try {
			result = (T) jedis.evalsha(sha1, keys, args);
		} catch (JedisException e) {
			if (e.getMessage().contains(LuaHelper.REDIS_MESSAGE_NOSCRIPT)) {
				result = (T) jedis.eval(luaScript, keys, args);
			} else {
				LOGGER.warn("ERROR LUA PATH:{} SHA1:{} Keys:{} args:{} Exception:{}", path, sha1, keys, args, e);
				throw e;
			}
		}
		return result;
	}

	public String getSha1() {
		return sha1;
	}

}
