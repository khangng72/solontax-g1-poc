package solontax.g1.management.kafka.config.utils;

import org.springframework.kafka.support.mapping.DefaultJackson2JavaTypeMapper;
import org.springframework.kafka.support.mapping.Jackson2JavaTypeMapper;
import solontax.g1.management.core.common.dto.TaxCalculationDto;
import solontax.g1.management.core.domain.model.Person;

import java.util.HashMap;
import java.util.Map;

public class Utils {
    public static DefaultJackson2JavaTypeMapper typeMapper() {
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.TYPE_ID);

        Map<String, Class<?>> mappings = new HashMap<>();
        mappings.put("person", Person.class);
        mappings.put("taxCalculation", TaxCalculationDto.class);

        typeMapper.setIdClassMapping(mappings);

        return typeMapper;
    }
}
