package org.zenbeni.jedis.job;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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

	@AfterClass
	public void after() {
		JedisExecutor.submitJedisJob(new JedisSimpleJob(jobConfiguration) {
			@Override
			public void runSimpleJob() {
				jedis.flushDB();
			}
		});
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

		final String resultOther = JedisExecutor.submitLuaJob(jobConfiguration, script, Arrays.asList(key), Arrays.asList(arg));
		LOGGER.info("Result:{}", resultOther);
		Assert.assertEquals(resultOther, "hello world:" + key + " " + arg);
	}

	@Test
	public void testByteArray() {
		final String id = "xyzd12";
		JedisExecutor.submitJedisJob(new JedisSimpleJob(jobConfiguration) {
			@Override
			public void runSimpleJob() {
				final byte[] data = {12, 23, 45, 67, 127};
				final byte[] data2 = {13, 25, 46, 68, 121, 44};
				jedis.lpush(id.getBytes(), data);
				jedis.rpush(id.getBytes(), data2);
			}
		});
		final long result = JedisExecutor.submitJedisJob(new JedisJob<Long>(jobConfiguration) {
			@Override
			public Long runJedisJob() {
				return jedis.llen(id);
			}
		});
		LOGGER.info("Result:{}", result);
		Assert.assertEquals(result, 2);
	}

}
