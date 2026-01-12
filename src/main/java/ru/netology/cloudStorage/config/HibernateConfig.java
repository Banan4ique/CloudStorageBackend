package ru.netology.cloudStorage.config;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Map;

@Configuration
public class HibernateConfig {

    @Bean
    @Primary
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return (Map<String, Object> hibernateProperties) -> {
            // Ключевая настройка - полностью отключаем DDL
            hibernateProperties.put(AvailableSettings.HBM2DDL_AUTO, "none");
            hibernateProperties.put(AvailableSettings.JAKARTA_HBM2DDL_DATABASE_ACTION, "none");
            hibernateProperties.put(AvailableSettings.JAKARTA_HBM2DDL_SCRIPTS_ACTION, "none");
            hibernateProperties.put(AvailableSettings.JAKARTA_HBM2DDL_CREATE_SOURCE, "none");
            hibernateProperties.put(AvailableSettings.JAKARTA_HBM2DDL_DROP_SOURCE, "none");

            // Отключаем все автоматические DDL операции
            hibernateProperties.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
            hibernateProperties.put("hibernate.jdbc.lob.non_contextual_creation", "true");

            // Диалект
            hibernateProperties.put(AvailableSettings.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
        };
    }
}