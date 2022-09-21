package com.example.demo.util;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RedisUtil {
    public static boolean isCluster = false;

    public static Object get(RedisTemplate redisTemplate, String key){
        return redisTemplate.opsForValue().get(key);
    }

    public static void set(RedisTemplate redisTemplate, String key, Object value){
        redisTemplate.opsForValue().set(key, value);
    }

    public static void HSet(RedisTemplate redisTemplate, String key1, Map<String, Object> map){
        redisTemplate.opsForHash().putAll(key1, map);
    }

    public static String dealKeyPrefix(String key) {
        if (isCluster) {
            key = "{cluster:}" + key;// 集群模式下使用
        }
        return key;
    }

    public static <T> T execute(RedisTemplate redisTemplate, final RedisScript<T> script, final List<String> keys, final Object args[]) {
        return (T) redisTemplate.execute(new RedisCallback<T>(){

            @Override
            public T doInRedis(RedisConnection connection) throws DataAccessException {
                Object nativeConnection = connection.getNativeConnection();

                // redis序列化key、value、lua脚本
                RedisSerializer keySerializer = redisTemplate.getKeySerializer();
                RedisSerializer valueSerializer = redisTemplate.getValueSerializer();
                RedisSerializer<String> stringSerializer = redisTemplate.getStringSerializer();

                List<byte[]> keys_ByteArr = new ArrayList<byte[]>(keys.size());
                List<byte[]> args_ByteArr = new ArrayList<byte[]>(args.length);

                for (int i = 0; i < keys.size(); i++) {
                    keys_ByteArr.add(keySerializer.serialize(keys.get(i)));
                }

                for (int j = 0; j < args.length; j++) {
                    args_ByteArr.add(valueSerializer.serialize(args[j]));
                }

                byte[] scriptByte = stringSerializer.serialize(script.getScriptAsString());
                // 集群模式
                if (nativeConnection instanceof JedisCluster) {
                    return (T) ((JedisCluster) nativeConnection).eval(scriptByte, keys_ByteArr, args_ByteArr);
                }
                // 单机模式
                else {
                    return (T) ((Jedis) nativeConnection).eval(scriptByte, keys_ByteArr, args_ByteArr);
                }
            }
        });
    }
}
