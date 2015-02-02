package org.zenbeni.jedis.job;

import org.testng.annotations.Test;

import com.fiftyonred.mock_jedis.MockJedis;

public class JedisJobTest {

	@Test
	public void basicTest() {
		JedisExecutor.submitJedisJob(new JedisSimpleJob() {

			@Override
			public void initJedis() {
				jedis = new MockJedis("test");
			}

			@Override
			public void runSimpleJob() {
				jedis.set("test", "valeur");
			}
		});
	}

}
