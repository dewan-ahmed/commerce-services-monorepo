package com.acme.platform.web;

import com.acme.platform.web.security.JwtProperties;
import com.acme.platform.web.security.EntraProperties;
import com.acme.platform.web.security.PlatformSecurityConfiguration;
import com.acme.platform.web.observability.RequestCorrelationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(EntraProperties.class)
@Import(PlatformSecurityConfiguration.class)
public class PlatformWebConfiguration {
    @Bean
    JwtProperties jwtProperties(EntraProperties properties) {
        return properties.jwtProperties();
    }

    @Bean
    FilterRegistrationBean<RequestCorrelationFilter> requestCorrelationFilter() {
        FilterRegistrationBean<RequestCorrelationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestCorrelationFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
