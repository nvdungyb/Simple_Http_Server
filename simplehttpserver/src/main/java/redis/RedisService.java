package redis;

import redis.clients.jedis.Jedis;

import java.io.*;
import java.util.List;

public class RedisService {
    private Jedis redisConnection;

    public RedisService() {
        this.redisConnection = RedisConnection.getInstance();
    }

    public void setStrValue(String key, String value) {
        redisConnection.set(key, value);
        redisConnection.expire(key, 60);
    }

    public void increaseValue(String key) {
        redisConnection.incr(key);
        redisConnection.expire(key, 120);
    }

    public void setBytesValue(String key, byte[] value) {
        redisConnection.set(key.getBytes(), value);
        redisConnection.expire(key, 120);
    }

    public byte[] getBytesValue(String key) {
        return redisConnection.get(key.getBytes());
    }

    public int getNumberRequest(String target) {
        try {
            return Integer.parseInt(redisConnection.get(target));
        } catch (Exception e) {
            return 0;
        }
    }

    public static void main(String[] args) {
        Jedis jedis = RedisConnection.getInstance();

        RedisService redisService = new RedisService();
        redisService.setStrValue("key", "value");

        List<Integer> list = List.of(1, 2, 3, 4);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(list);
            objectOutputStream.flush();

            byte[] bytes = outputStream.toByteArray();
            redisService.setBytesValue("object", bytes);

            // read bytes
            byte[] readBytes = redisService.getBytesValue("object");
            ByteArrayInputStream inputStream = new ByteArrayInputStream(readBytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            List<Integer> readValue = (List<Integer>) objectInputStream.readObject();
            readValue.forEach(System.out::println);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
