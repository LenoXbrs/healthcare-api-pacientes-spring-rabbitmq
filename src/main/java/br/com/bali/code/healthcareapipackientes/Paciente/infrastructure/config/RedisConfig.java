package br.com.bali.code.healthcareapipackientes.Paciente.infrastructure.config;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * ObjectMapper para Redis sem DefaultTyping.EVERYTHING.
     * A api-usuarios usa EVERYTHING — vetor de ataque (polimorphic deserialization).
     * Aqui usamos apenas módulos necessários.
     */
    @Bean
    public ObjectMapper redisObjectMapper() {
        return new ObjectMapper()
                .registeredModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory,
                                          ObjectMapper redisObjectMapper) {
        GenericJacksonJsonRedisSerializer serializer =
                new GenericJacksonJsonRedisSerializer(redisObjectMapper);

        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(serializer))
                .disableCachingNullValues();

        return RedisCacheManager.builder(factory)
                .cacheDefaults(base)
                .withInitialCacheConfigurations(Map.of(
                    // Dados de paciente por ID — muda raramente
                    "pacientes",        base.entryTtl(Duration.ofMinutes(10)),
                    // Lista por status — muda conforme triagens chegam
                    "pacientes-status", base.entryTtl(Duration.ofMinutes(1))
                ))
                .build();
    }
}
