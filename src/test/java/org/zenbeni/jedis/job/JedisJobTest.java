package org.zenbeni.jedis.job;

import org.testng.annotations.Test;

import com.fiftyonred.mock_jedis.MockJedis;

public class JedisJobTest {

	@Test
	public void basicTest() {
		JedisExecutor.submitJedisJob(new JedisJob() {

			@Override
			public void initJedis() {
				jedis = new MockJedis("test");
			}

			@Override
			public void runJedisJob() {
				jedis.set("test", "valeur");
			}
		});
	}

}
