package com.openops.server.config;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import java.lang.reflect.Method;

@Configuration
@AutoConfigureBefore(RedisAutoConfiguration.class)
public class RedisComConfig {
    @Value("${spring.redis.maxTotal}")
    private int maxTotal;

    @Value("${spring.redis.maxIdle}")
    private int maxIdle;

    @Value("${spring.redis.minIdle}")
    private int minIdle;

    @Value("${spring.redis.maxWaitMillis}")
    private long maxWaitMillis;

    @Value("${spring.redis.testOnBorrow}")
    private boolean testOnBorrow;

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.connTimeout}")
    private int connTimeout;
    @Value("${spring.redis.readTimeout}")
    private int readTimeout;

    @Value("${spring.redis.password:}")
    private String password;

    @Value("${spring.redis.database}")
    private int database;

    @Value("${spring.redis.minEvictableIdleTimeMillis}")
    private int minEvictableIdleTimeMillis;

    @Value("${spring.redis.softMinEvictableIdleTimeMillis}")
    private int softMinEvictableIdleTimeMillis;

    @Value("${spring.redis.timeBetweenEvictionRunsMillis}")
    private int timeBetweenEvictionRunsMillis;

    @Value("${spring.redis.numTestsPerEvictionRun}")
    private int numTestsPerEvictionRun;

    @Value("${spring.redis.blockWhenExhausted}")
    private boolean blockWhenExhausted;

    @Value("${spring.redis.testWhileIdle}")
    private boolean testWhileIdle;

    /**
     * jedis ?????????
     *
     * @return jedis ?????????
     */

    @Bean
    public GenericObjectPoolConfig<?> poolConfig() {
        GenericObjectPoolConfig<?> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMaxWaitMillis(maxWaitMillis);
        poolConfig.setTestOnBorrow(testOnBorrow);
        poolConfig.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        poolConfig.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        poolConfig.setSoftMinEvictableIdleTimeMillis(softMinEvictableIdleTimeMillis);
        poolConfig.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        poolConfig.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
        poolConfig.setBlockWhenExhausted(blockWhenExhausted);
        poolConfig.setTestWhileIdle(testWhileIdle);
        poolConfig.setMinIdle(minIdle);
        return poolConfig;
    }

    /**
     * Jedis ????????????.
     *
     * @return ????????????Jedis????????????
     */
    @Bean
    public RedisConnectionFactory connectionFactory(GenericObjectPoolConfig<?> poolConfig) {
        // ??????????????????
        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration();
        // ????????????????????????????????????????????????
        serverConfig.setHostName(host);
        serverConfig.setPort(port);
        //?????????????????????????????????????????????????????????RedisPassword???
        if (StringUtils.isNotEmpty(password)) {
            serverConfig.setPassword(RedisPassword.of(password));
        }
        serverConfig.setDatabase(database);
        // ?????????????????????????????????
        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder lettucePoolClientConfBuilder = LettucePoolingClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(connTimeout));
        lettucePoolClientConfBuilder.poolConfig(poolConfig);

        // ???????????????
        LettuceClientConfiguration clientConfig = lettucePoolClientConfBuilder.build();

        return new LettuceConnectionFactory(serverConfig, clientConfig);
    }


    @Bean(name = "redisTemplate")
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        /*
         * Redis ????????????.
         *
         * RedisTemplate ???????????????????????? JdkSerializationRedisSerializer,???JdkSerializationRedisSerializer???????????????,
         * ?????????????????????????????????Serializable???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
         *
         * Jackson2JsonRedisSerializer ??? GenericJackson2JsonRedisSerializer??????????????????????????? json???
         * ?????????????????? json ????????? @class ??????????????????????????????????????????????????????????????????????????? List ??????????????????????????????????????????
         * TypeReference ???????????? java.util.LinkedHashMap cannot be cast to
         */
        RedisSerializer genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
        RedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // ??????RedisTemplate????????????????????????
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();

        // key ?????????????????? StringRedisSerializer
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        // value ????????????????????? GenericJackson2JsonRedisSerializer
        redisTemplate.setValueSerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(stringRedisSerializer);
        // ??????????????????
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setEnableTransactionSupport(false);
        return redisTemplate;


    }

    @Bean(name = "stringRedisTemplate")
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        // ??????RedisTemplate????????????????????????
        StringRedisTemplate redisTemplate = new StringRedisTemplate(connectionFactory);
        RedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setDefaultSerializer(stringRedisSerializer);
        redisTemplate.setEnableTransactionSupport(false);
        return redisTemplate;
    }

    @Bean
    public CacheManager initRedisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager
                .RedisCacheManagerBuilder.fromConnectionFactory(connectionFactory);
        return builder.build();
    }

    @Bean
    public KeyGenerator keyGenerator() {
        return new KeyGenerator() {
            @Override
            public Object generate(Object o, Method method, Object... objects) {
                StringBuilder sb = new StringBuilder();
                sb.append(o.getClass().getName());
                sb.append(method.getName());
                for (Object obj : objects) {
                    sb.append(obj.toString());
                }
                return sb.toString();
            }
        };
    }

}