package org.zenbeni.jedis.job;

public final class JedisJobConfigurationBuilder {

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

	public JedisJobConfiguration build() {
		return configuration;
	}

}
