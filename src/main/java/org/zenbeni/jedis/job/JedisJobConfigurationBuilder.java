package org.zenbeni.jedis.job;

import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;

public final class JedisJobConfigurationBuilder {

	public static final String REDIS_HOST_PROPERTY = "redis.host";
	public static final String REDIS_PORT_PROPERTY = "redis.port";
	public static final String REDIS_PASSWORD_PROPERTY = "redis.password";
	public static final String REDIS_DATABASE_PROPERTY = "redis.database";
	public static final String REDIS_TIMEOUT_PROPERTY = "redis.timeout";

	public static JedisJobConfigurationBuilder create() {
		return new JedisJobConfigurationBuilder();
	}

	private final JedisJobConfiguration configuration;

	private JedisJobConfigurationBuilder() {
		configuration = new JedisJobConfiguration();
	}

	public JedisJobConfigurationBuilder withHost(final String host) {
		configuration.setHost(host);
		return this;
	}

	public JedisJobConfigurationBuilder withDatabase(final int database) {
		configuration.setDatabase(database);
		return this;
	}

	public JedisJobConfigurationBuilder withPort(final int port) {
		configuration.setPort(port);
		return this;
	}

	public JedisJobConfigurationBuilder withTimeout(final int timeout) {
		configuration.setTimeout(timeout);
		return this;
	}

	public JedisJobConfigurationBuilder withPassword(final String password) {
		configuration.setPassword(password);
		return this;
	}

	public JedisJobConfigurationBuilder withConfiguration(final Map conf) {
		final String host = getProperty(conf, REDIS_HOST_PROPERTY);
		withHost(host);
		final String password = getProperty(conf, REDIS_PASSWORD_PROPERTY);
		if (!StringUtils.isEmpty(password)) {
			withPassword(password);
		}
		final int port = getPropertyAsInteger(conf, REDIS_PORT_PROPERTY);
		if (port >= 0) {
			withPort(port);
		}
		final int timeout = getPropertyAsInteger(conf, REDIS_TIMEOUT_PROPERTY);
		if (timeout >= 0) {
			withTimeout(timeout);
		}
		final int database = getPropertyAsInteger(conf, REDIS_DATABASE_PROPERTY);
		if (database >= 0) {
			withDatabase(database);
		}
		return this;
	}

	public JedisJobConfigurationBuilder withConfiguration(final Configuration conf) {
		final String host = conf.getString(REDIS_HOST_PROPERTY);
		withHost(host);
		final String password = conf.getString(REDIS_PASSWORD_PROPERTY);
		if (!StringUtils.isEmpty(password)) {
			withPassword(password);
		}
		final int port = conf.getInt(REDIS_PORT_PROPERTY);
		if (port >= 0) {
			withPort(port);
		}
		final int timeout = conf.getInt(REDIS_TIMEOUT_PROPERTY);
		if (timeout >= 0) {
			withTimeout(timeout);
		}
		final int database = conf.getInt(REDIS_DATABASE_PROPERTY);
		if (database >= 0) {
			withDatabase(database);
		}
		return this;
	}

	static String getProperty(final Map conf, final String property) {
		final Object item = conf.get(property);
		if (item != null) {
			if (item instanceof String) {
				return (String) item;
			} else {
				return item.toString();
			}
		}
		return null;
	}

	static int getPropertyAsInteger(final Map conf, final String property) {
		final String item = getProperty(conf, property);
		if (item != null) {
			return Integer.parseInt(item);
		}
		return -1;
	}

	public JedisJobConfiguration build() {
		return configuration;
	}

}
