package org.zenbeni.jedis.lua;

import java.util.HashMap;
import java.util.Map;

/**
 * Thread local cache of lua scripts instances.
 * Each thread uses its own lua scripts and never shares them with another thread.
 */
public final class LuaScriptThreadLocalCache {

	// TODO Use of classic HashMap, maybe should I use a WeakHashMap or implement a custom eviction strategy?
	// LuaScript instances can be heavy as their size depend on the script size (String)
	private static final ThreadLocal<Map<String, LuaScript>> MAP = new ThreadLocal<Map<String, LuaScript>>() {
		@Override
		protected Map<String, LuaScript> initialValue() {
			return new HashMap<>();
		}
	};

	/**
	 * Get from the thread local cache the lua script.
	 * If it does not exists, it builds a new one by using clone() on the LuaScript given in parameter.
	 *
	 * @param script used to find the luaScript in cache. Or to build a new instance in thread local cache.
	 * @param <T>    the result type of lua script call.
	 * @return a LuaScript instance in thread local cache. It cannot be shared with other threads.
	 */
	public static <T> LuaScript<T> getLuaScript(final LuaScript<T> script) {
		final Map<String, LuaScript> map = MAP.get();
		final LuaScript luaScript = map.get(script.getSha1());
		if (luaScript != null) {
			return luaScript;
		}
		final LuaScript<T> clone = (LuaScript<T>) script.clone();
		map.put(clone.getSha1(), clone);
		return clone;
	}

	/**
	 * Clear thread local cache of lua scripts.
	 */
	public static void clear() {
		MAP.get().clear();
	}

	private LuaScriptThreadLocalCache() {
	}

}
