package redis;

import analysis_request.StatisticTarget;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.resps.Tuple;
import writer.ResponseWriter;

import java.util.*;

public class RedisService {
    private static final JedisPool pool = new JedisPool(new JedisPoolConfig(), "redis://localhost:6379");

    public static Jedis getConnection() {
        return pool.getResource();
    }

    public static void increaseValue(String key) {
        Jedis jedis = getConnection();
        jedis.incr("number:" + key);
        setExpireKey(jedis, "number:" + key);
        jedis.close();
    }

    public static void setBytesValue(String key, byte[] value) {
        Jedis jedis = getConnection();
        jedis.set(key.getBytes(), value);
        setExpireKey(jedis, key);
        jedis.close();
    }

    public static void setExpireKey(Jedis jedis, String key) {
        jedis.expire(key, 120);
    }

    public static byte[] getBytesValue(String key) {
        Jedis jedis = getConnection();
        byte[] value = jedis.get(key.getBytes());
        jedis.close();
        return value;
    }

    public static int getNumberRequest(String target) {
        Jedis jedis = getConnection();
        try {
            int parsedValue = Integer.parseInt(jedis.get("number:" + target));
            jedis.close();
            return parsedValue;
        } catch (Exception e) {
            jedis.del("number:" + target);
            jedis.close();
            return 0;
        }
    }

    public static void cacheRequests(String host, String target) {
        Jedis jedis = getConnection();
        jedis.zincrby("analysis:" + host, 1, target);
        setExpireKey(jedis, "analysis:" + host);
        jedis.close();
    }

    public static Map<String, List<StatisticTarget>> getAllRequests() {
        Jedis jedis = getConnection();
        Set<String> allRequest = jedis.keys("analysis:*");

        Map<String, List<StatisticTarget>> map = new HashMap<>();
        for (String request : allRequest) {
            List<StatisticTarget> statisticTarget = new ArrayList<>();
            List<Tuple> tuples = jedis.zrangeWithScores(request, 0, -1);

            tuples.stream().forEach(val -> {
                statisticTarget.add(new StatisticTarget(val.getElement(), (int) val.getScore()));
            });
            map.put(request, statisticTarget);
        }

        map.keySet().stream().forEach(val -> System.out.println(map.get(val)));
        jedis.close();
        return map;
    }

    public static void cacheResource(byte[] data, int numbersRequestToTarget, String requestTarget) {
        // parallel cache resource to reduce response time.
        byte[] finalData = data;
        Jedis jedis = RedisService.getConnection();
        Thread thead = new Thread(() -> {
            if (numbersRequestToTarget == ResponseWriter.NUMBER_REQUEST_TO_CACHE) {
                RedisService.setBytesValue(requestTarget, finalData);
                System.out.println("Cached resource successfully");
            } else if (numbersRequestToTarget > ResponseWriter.NUMBER_REQUEST_TO_CACHE) {
                RedisService.setExpireKey(jedis, requestTarget);
            }
            RedisService.increaseValue(requestTarget);
            System.out.println("Saved: " + requestTarget);
        });
        thead.start();
    }
}
