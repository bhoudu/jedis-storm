package org.zenbeni.jedis.job;

import org.testng.annotations.Test;

import com.fiftyonred.mock_jedis.MockJedis;

public class JedisJobTest {

	@Test
	public void basicTest() {
		JedisExecutor.submitJedisJob(new JedisSimpleJob() {

			@Override
			public void runSimpleJob() {
				jedis = new MockJedis("test");
				jedis.set("test", "valeur");
			}
		});
	}

}
