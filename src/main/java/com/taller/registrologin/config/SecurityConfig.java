package com.taller.registrologin.config;

import com.taller.registrologin.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

        @Bean
        PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService,PasswordEncoder passwordEncoder) {

                DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
                provider.setPasswordEncoder(passwordEncoder);
                return provider;

        }

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http, DaoAuthenticationProvider authenticationProvider) throws Exception {
                http
                        .authenticationProvider(authenticationProvider)
                        .authorizeHttpRequests(auth -> auth
                                .requestMatchers(
                                        "/",
                                        "/login",
                                        "/register",
                                        "/process_register",
                                        "/css/**",
                                        "/js/**")
                                .permitAll()
                                .requestMatchers("/users").authenticated()
                                .anyRequest().permitAll())
                        .formLogin(form -> form
                                .usernameParameter("email")
                                .defaultSuccessUrl("/users", true)
                                .permitAll())
                        .logout(logout -> logout
                                .logoutSuccessUrl("/")
                                .permitAll());

                return http.build();
        }
}
