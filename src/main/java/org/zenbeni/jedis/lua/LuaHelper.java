package org.zenbeni.jedis.lua;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LuaHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(LuaHelper.class);

	public static final String DEFAULT_LUA_SCRIPT_ENCODING = "UTF-8";
	public static final String SHA_1_ALGORITHM = "SHA-1";
	public static final String REDIS_MESSAGE_NOSCRIPT = "NOSCRIPT No matching script";

	private LuaHelper() {
	}

	public static String convertStreamToString(final InputStream inputStream) {
		final Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
		return scanner.hasNext() ? scanner.next() : "";
	}

	public static String generateSHA1(final String luaScript) {
		if (luaScript == null || luaScript.isEmpty()) {
			LOGGER.warn("Value is null or empty! Can't generate SHA1! {}", StringUtils.abbreviate(luaScript, 50));
			return null;
		}
		String sha1 = null;
		try {
			final MessageDigest crypt = MessageDigest.getInstance(SHA_1_ALGORITHM);
			crypt.reset();
			final byte[] bytes = luaScript.getBytes(DEFAULT_LUA_SCRIPT_ENCODING);
			crypt.update(bytes);
			sha1 = byteToHex(crypt.digest());
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			LOGGER.warn("Bad SHA1 generated:{}", e);
		}
		return sha1;
	}

	static String byteToHex(final byte[] hash) {
		final Formatter formatter = new Formatter();
		for (final byte b : hash) {
			formatter.format("%02x", b);
		}
		final String result = formatter.toString();
		formatter.close();
		return result;
	}

}
