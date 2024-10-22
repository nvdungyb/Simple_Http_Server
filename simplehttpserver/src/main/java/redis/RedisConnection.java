package redis;

import redis.clients.jedis.Jedis;

public class RedisConnection {
    private static Jedis redisConnection = null;

    private RedisConnection() {
    };

    public static Jedis getInstance() {
        if (redisConnection == null) {
            String url = "localhost";
            int port = 6379;
            redisConnection = new Jedis(url, port);
        }

        return redisConnection;
    }
}
