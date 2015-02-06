package org.zenbeni.jedis.job;

public abstract class JedisSimpleJob extends JedisJob<Void> {

	public JedisSimpleJob() {
		this(new JedisJobConfiguration());
	}

	public JedisSimpleJob(final JedisJobConfiguration configuration) {
		super(configuration);
	}

	@Override
	public final Void runJedisJob() {
		runSimpleJob();
		return null;
	}

	public abstract void runSimpleJob();
}
