package org.zenbeni.jedis.exception;

public class JedisJobException extends RuntimeException {

	public JedisJobException(final String message) {
		super(message);
	}

	public JedisJobException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
