package com.xxj.qqbot.util.common.redis.config;


import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 配置文件读取
 */
@Data
@Configuration
@PropertySource("classpath:/config/application.yml")
public class StringTemplateConfig {

    @Bean("jedisPoolConfig")
    @ConfigurationProperties(prefix = "spring.redis.jedis.pool")
    public JedisPoolConfig initRedisConfig() {
        return new JedisPoolConfig();
    }

    @Bean("jedisConnectionFactory")
    @ConfigurationProperties(prefix = "spring.redis")
    public JedisConnectionFactory initConnectionFactory(@Qualifier("jedisPoolConfig") final JedisPoolConfig jedisPoolConfig) {
        return new JedisConnectionFactory(jedisPoolConfig);
    }

    @Bean
    public StringRedisTemplate getRedisTemplate(@Qualifier("jedisConnectionFactory") final JedisConnectionFactory jedisConnectionFactory) {
        return new StringRedisTemplate(jedisConnectionFactory);
    }
}
