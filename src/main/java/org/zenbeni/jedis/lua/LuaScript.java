package org.zenbeni.jedis.lua;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

/**
 * Wrapper of lua script which always try to eval with sha1 id on redis.
 * If the script does not exist, it catches the exception from redis and sends the script again.
 */
public class LuaScript<T> implements Serializable, Cloneable {

	private static final Logger LOGGER = LoggerFactory.getLogger(LuaScript.class);

	private final String name;
	private final String path;
	private final String luaScript;
	private final String sha1;

	public static LuaScript buildFromPath(final String path) {
		return buildFromPath(path, path);
	}

	public static LuaScript buildFromPath(final String name, final String path) {
		final InputStream inputStream = LuaScript.class.getResourceAsStream(path);
		final String script = LuaHelper.convertStreamToString(inputStream);
		return buildFromScript(name, script, path);
	}

	public static LuaScript buildFromScript(final String luaScript) {
		return buildFromScript(StringUtils.abbreviate(luaScript, 20), luaScript);
	}

	public static LuaScript buildFromScript(final String name, final String luaScript) {
		return new LuaScript(name, luaScript, "undefined");
	}

	static LuaScript buildFromScript(final String name, final String luaScript, final String path) {
		return new LuaScript(name, luaScript, path);
	}

	protected LuaScript(final String name, final String luaScript, final String path) {
		this(name, luaScript, LuaHelper.generateSHA1(luaScript), path);
	}

	protected LuaScript(final String name, final String luaScript, final String sha1, final String path) {
		this.name = name;
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

	@Override
	public String toString() {
		return name + '@' + Integer.toHexString(hashCode());
	}

	@Override
	protected Object clone() {
		return new LuaScript<>(name, luaScript, sha1, path);
	}

}
