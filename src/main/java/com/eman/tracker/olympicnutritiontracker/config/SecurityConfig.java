package com.eman.tracker.olympicnutritiontracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/athletes/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/consultations/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/nutrition-entries/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/**").hasAnyRole("ADMIN","COACH")
                        .requestMatchers(HttpMethod.PUT,  "/api/**").hasAnyRole("ADMIN","COACH")
                        .requestMatchers(HttpMethod.DELETE,"/api/**").hasAnyRole("ADMIN","COACH")

                        // الباقي لازم يكون مصرح له
                        .anyRequest().authenticated()
                )

                // مصادقة بسيطة Basic لتجربة الـ API
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public UserDetailsService users() {
        return new InMemoryUserDetailsManager(
                User.withUsername("admin").password("{noop}admin123").roles("ADMIN").build(),
                User.withUsername("coach").password("{noop}coach123").roles("COACH").build()
        );
    }
}
