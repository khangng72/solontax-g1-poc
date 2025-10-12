package solontax.g1.management.kafka.config.consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.support.mapping.DefaultJackson2JavaTypeMapper;
import org.springframework.kafka.support.mapping.Jackson2JavaTypeMapper;
import solontax.g1.management.core.common.dto.TaxCalculationDto;
import solontax.g1.management.core.domain.model.Person;

import java.util.HashMap;
import java.util.Map;

@Configuration
@PropertySource("classpath:application-kafka.properties")
public class GenericConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    public String kafkaServer;

    @Value("${kafka.retry.topic.attempts}")
    public int maxRetryAttempt;

    @Value("${kafka.retry.topic.multiplier}")
    public double retryMultiplier;

    @Value("${kafka.retry.topic.initial.interval}")
    public Long retryInitialInterval;

    public DefaultJackson2JavaTypeMapper typeMapper() {
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.TYPE_ID);
        typeMapper.addTrustedPackages("*");
        
        Map<String, Class<?>> mappings = new HashMap<>();
        mappings.put("person", Person.class);
        mappings.put("taxCalculation", TaxCalculationDto.class);

        typeMapper.setIdClassMapping(mappings);

        return typeMapper;
    }
}
