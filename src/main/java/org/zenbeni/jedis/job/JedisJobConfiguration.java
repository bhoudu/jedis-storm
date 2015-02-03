package org.zenbeni.jedis.job;

import redis.clients.jedis.Protocol;

public class JedisJobConfiguration {

	private String host = "localhost";
	private int port = Protocol.DEFAULT_PORT;
	private int timeout = Protocol.DEFAULT_TIMEOUT;
	private int database;
	private String password;

	JedisJobConfiguration() {
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final JedisJobConfiguration that = (JedisJobConfiguration) o;
		if (database != that.database) {
			return false;
		}
		if (port != that.port) {
			return false;
		}
		if (timeout != that.timeout) {
			return false;
		}
		if (!host.equals(that.host)) {
			return false;
		}
		if (password != null ? !password.equals(that.password) : that.password != null) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int result = host.hashCode();
		result = 31 * result + port;
		result = 31 * result + timeout;
		result = 31 * result + database;
		result = 31 * result + (password != null ? password.hashCode() : 0);
		return result;
	}

	public String getHost() {
		return host;
	}

	public void setHost(final String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(final int port) {
		this.port = port;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(final int timeout) {
		this.timeout = timeout;
	}

	public int getDatabase() {
		return database;
	}

	public void setDatabase(final int database) {
		this.database = database;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

}
