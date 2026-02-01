package uk.co.emcreations.energycoop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Security configuration for the application.
 * This configuration uses Okta for OAuth2 login and handles logout.
 * It allows public access to certain endpoints and secures all other requests.
 */
@Configuration
@Profile("!dev && !test")
@EnableMethodSecurity()
public class SecurityConfig {
    @Value("${okta.oauth2.issuer}")
    private String issuer;
    @Value("${okta.oauth2.client-id}")
    private String clientId;

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        return http.oauth2ResourceServer(oauth2ResourceServer ->
                oauth2ResourceServer.jwt(withDefaults())).build();
    }
}