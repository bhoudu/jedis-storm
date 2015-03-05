jedis-storm
===========

[![Build Status](https://travis-ci.org/bhoudu/jedis-storm.svg?branch=master)](https://travis-ci.org/bhoudu/jedis-storm)

A library to make the use of jedis inside a storm cluster easier and reliable.

JedisJob API with exponential backoff policy support
----------------------------------------------------

As the network can become unreliable (if saturation is quite high for instance), jedis-storm defines jobs to be executed and retried if they fail with an exponential backoff policy which can be overwritten if needed.
Here is a classic example of how the JedisJob API can be used, with a configuration parsed from Storm configuration.

	final JedisJobConfiguration jobConfiguration = JedisJobConfigurationBuilder.create()
	  .withConfiguration(stormConfiguration)
	  .build();

	JedisExecutor.submitJedisJob(new JedisSimpleJob(jobConfiguration) {

		@Override
		public void runSimpleJob() {
			final String value = jedis.get("some.key");
			jedis.set(value, "another.value");
		}
	}

Making lua calls easier
-----------------------

Lua scripts are really important when using Redis. They are atomic, and as they run in Redis, they allow less roundtrips on the network. Jedis-storm provides a class LuaScript that can read lua scripts provided in the classpath and execute them with JedisLuaJob thanks to jedis-storm job API.

	final JedisJobConfiguration jobConfiguration = JedisJobConfigurationBuilder.create()
	    .withConfiguration(stormConfiguration)
	    .build();

	// Build a LuaScript wrapper from lua script present in classpath, it returns a String
	final LuaScript<String> luaScript = LuaScript.buildFromPath("/lua/script.lua");
	
	// Get the result from Lua converted to Java thanks to Jedis
	final String result = JedisExecutor.submitLuaJob(jobConfiguration, luaScript);

Still, beware of threshold issues, as a timeout can make a lua script fail (taking too much time). For instance, if it is a cleaning task that fails with a lua timeout, its chances to succeed become fewer as time goes: redis become bigger and bigger and the cleaning script needs even more time than before to run.

Jedis-storm, storm executors and tasks
--------------------------------------

Threads are called executors in Storm. Instead of managing a pool (per worker, or per Bolt/Spout), jedis-storm uses a single jedis connection per executor thanks to Java ThreadLocal API.

As your topology and cluster evolve, you won't have to redefine a pool of connection, the number of effective jedis connections needed is the sum of all executors defined that run jedis jobs.

If you have a design where you have more tasks than executors, each executor will use only one jedis connection for all tasks that it runs in the cluster.

If a jedis connection fails, a new one will be provided automatically.

Why using jedis-storm? The problematic of the use of JedisPool within Storm
---------------------------------------------------------------------------

It is usually difficult to manage redis connections efficiently inside a storm cluster. The reason is that the only proper way to have a permanent connection is to use a JedisPool per Bolt / Spout with Jedis.

It is a problem as tuning so many pools with different configurations (depending on your cluster usage and configuration) is expensive and fragile. A workaround is to use a single JedisPool for a worker with lazy initialization of it with double-check locking, as JedisPool is thread-safe not Jedis instances.

Again it is unsatisfaying as you will have to monitor it, size it correctly when the topology changes, the cluster changes or the throughput changes.

If you have a lot of data to manipulate at once and not enough resources in your pool, storm will slow down or fail and in the worst case, kill the worker.

We don't want to use this anymore:

	@Deprecated
	public final class RedisFactory {

		public static JedisPool getJedisPool(final Map stormConfiguration) {
			if (null == jedisPool) {
				synchronized (RedisFactory.class) {
					if (null == jedisPool) {
						jedisPool = createJedisPool(stormConfiguration);
					}
				}
			}
			return jedisPool;
		}

		// ... more code
	}
