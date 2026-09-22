package com.challenge.customers.config;

import org.springframework.context.annotation.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder encoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    UserDetailsService users(PasswordEncoder e) {
        return new InMemoryUserDetailsManager(User.withUsername("user").password(e.encode("user123")).roles("USER").build(), User.withUsername("admin").password(e.encode("admin123")).roles("ADMIN", "USER").build());
    }

    @Bean
    SecurityFilterChain chain(HttpSecurity h) throws Exception {
        return h.csrf(c -> c.disable()).authorizeHttpRequests(a -> a.requestMatchers(org.springframework.http.HttpMethod.POST, "/customers").hasRole("ADMIN").requestMatchers(org.springframework.http.HttpMethod.PUT, "/customers/**").hasRole("ADMIN").requestMatchers(org.springframework.http.HttpMethod.DELETE, "/customers/**").hasRole("ADMIN").anyRequest().hasAnyRole("USER", "ADMIN")).httpBasic(Customizer.withDefaults()).build();
    }
}