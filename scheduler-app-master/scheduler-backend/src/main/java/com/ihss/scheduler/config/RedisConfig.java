package com.ihss.scheduler.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${redis.channels.job-started}")
    private String jobStartedChannel;

    @Value("${redis.channels.job-progress}")
    private String jobProgressChannel;

    @Value("${redis.channels.job-completed}")
    private String jobCompletedChannel;

    @Value("${redis.channels.job-failed}")
    private String jobFailedChannel;

    /**
     * Configure RedisTemplate with JSON serialization.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Use JSON serializer for values
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer jsonSerializer =
            new GenericJackson2JsonRedisSerializer(objectMapper);

        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Container for Redis message listeners.
     * Listens to job event channels from CMIPS backend.
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter jobEventListenerAdapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // Subscribe to all job event channels
        container.addMessageListener(jobEventListenerAdapter, jobStartedTopic());
        container.addMessageListener(jobEventListenerAdapter, jobProgressTopic());
        container.addMessageListener(jobEventListenerAdapter, jobCompletedTopic());
        container.addMessageListener(jobEventListenerAdapter, jobFailedTopic());

        return container;
    }

    /**
     * Message listener adapter that delegates to JobEventListener service.
     */
    @Bean
    public MessageListenerAdapter jobEventListenerAdapter(JobEventListenerDelegate delegate) {
        return new MessageListenerAdapter(delegate, "handleMessage");
    }

    // Channel topics
    @Bean
    public ChannelTopic jobStartedTopic() {
        return new ChannelTopic(jobStartedChannel);
    }

    @Bean
    public ChannelTopic jobProgressTopic() {
        return new ChannelTopic(jobProgressChannel);
    }

    @Bean
    public ChannelTopic jobCompletedTopic() {
        return new ChannelTopic(jobCompletedChannel);
    }

    @Bean
    public ChannelTopic jobFailedTopic() {
        return new ChannelTopic(jobFailedChannel);
    }

    /**
     * Interface for the job event listener delegate.
     * Implementation will be in the service layer.
     */
    public interface JobEventListenerDelegate {
        void handleMessage(String message, String channel);
    }
}
