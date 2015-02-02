package org.zenbeni.jedis.lua;

import java.util.HashMap;
import java.util.Map;

public final class LuaScriptThreadLocalCache {

	private static final ThreadLocal<Map<String, LuaScript>> MAP = new ThreadLocal<Map<String, LuaScript>>() {
		@Override
		protected Map<String, LuaScript> initialValue() {
			return new HashMap<>();
		}
	};

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

	public static void clear() {
		MAP.get().clear();
	}

	private LuaScriptThreadLocalCache() {
	}

}
