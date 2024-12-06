package com.acra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, 
                                                 ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        // Create authorization request resolver
        OAuth2AuthorizationRequestResolver resolver = new DefaultOAuth2AuthorizationRequestResolver(
            clientRegistrationRepository, "/oauth2/authorization");

        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/graphql"))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/graphql").authenticated()
                .requestMatchers("/graphiql").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Client(oauth2 -> oauth2
                .authorizationCodeGrant(grant -> grant
                    .authorizationRequestResolver(resolver)
                )
            );
        
        return http.build();
    }
}