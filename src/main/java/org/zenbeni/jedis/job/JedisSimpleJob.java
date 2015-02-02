package org.zenbeni.jedis.job;

public abstract class JedisSimpleJob extends JedisJob<Void> {

	@Override
	public final Void runJedisJob() {
		runSimpleJob();
		return null;
	}

	public abstract void runSimpleJob();
}
