package com.acra.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final String apiPasscode;
    public SecurityConfig(){
        Dotenv dotenv = Dotenv.load();
        this.apiPasscode = dotenv.get("ACRA_API_PASSCODE");
    }

    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/graphql").authenticated()
                        .anyRequest().permitAll()
                )
                .httpBasic(httpBasic -> {});

        return http.build();

    }
   @Bean
    public UserDetailsService userDetailsService(){
       UserDetails user = User.withDefaultPasswordEncoder()
               .username("api")
               .password(apiPasscode)
               .roles("API")
               .build();
       return new InMemoryUserDetailsManager(user);
    }
}
