package org.zenbeni.jedis.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.zenbeni.jedis.lua.LuaScript;

public class JedisJobTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(JedisJobTest.class);

	private JedisJobConfiguration jobConfiguration;

	@BeforeClass
	public void before() {
		jobConfiguration = JedisJobConfigurationBuilder.create().withHost("localhost").withDatabase(15).build();
	}

	@Test
	public void basicTest() {
		final String key = "my.key.test";
		final String value = "value.of.test";
		JedisExecutor.submitJedisJob(new JedisSimpleJob(jobConfiguration) {

			@Override
			public void runSimpleJob() {
				jedis.set(key, value);
			}
		});
		final String result = JedisExecutor.submitJedisJob(new JedisJob<String>(jobConfiguration) {
			@Override
			public String runJedisJob() {
				return jedis.get(key);
			}
		});
		Assert.assertEquals(result, value);
	}

	@Test
	public void luaTest() {
		final LuaScript<String> script = LuaScript.buildFromPath("/lua/helloworld.lua");
		final String key = "storm.is.cool";
		final String arg = "redis.is.cool";
		final String result = JedisExecutor.submitJedisJob(new JedisLuaJob<String>(jobConfiguration, script) {
			@Override
			protected void initKeysAndArgv() {
				keys.add(key);
				argv.add(arg);
			}
		});
		LOGGER.info("Result:{}", result);
		Assert.assertEquals(result, "hello world:" + key + " " + arg);
	}

}
