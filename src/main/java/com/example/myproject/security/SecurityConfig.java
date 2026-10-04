package com.example.myproject.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.myproject.service.CustomOAuth2UserService;
import com.example.myproject.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Autowired
        private CustomUserDetailsService userDetailsService;

        @Autowired
        private CustomOAuth2UserService oauth2UserService;

        @Autowired
        private CustomAuthenticationFailureHandler authenticationFailureHandler;

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(authz -> authz
                                                .requestMatchers("/login", "/register", "/forgot-password", "/css/**",
                                                                "/js/**", "/images/**",
                                                                "/favicon.ico", "/", "/products/**",
                                                                "/reset-password")
                                                .permitAll()
                                                .requestMatchers("/manager/**", "/admin/**", "/api/manager/**")
                                                .hasRole("MANAGER")
                                                .requestMatchers("/profile", "/cart/**", "/orders/**").authenticated()
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .defaultSuccessUrl("/", true)
                                                .failureHandler(authenticationFailureHandler)
                                                .usernameParameter("username")
                                                .passwordParameter("password")
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout=true")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll())
                                .oauth2Login(oauth -> oauth
                                                .loginPage("/login")
                                                .defaultSuccessUrl("/", true)
                                                .userInfoEndpoint(userInfo -> userInfo.userService(oauth2UserService))
                                                .successHandler((request, response, authentication) -> {
                                                        System.out.println("OAuth2 login successful for: "
                                                                        + authentication.getName());
                                                        response.sendRedirect("/");
                                                }))
                                .rememberMe(remember -> remember
                                                .key("uniqueAndSecret")
                                                .tokenValiditySeconds(86400) // 1 day
                                                .userDetailsService(userDetailsService));

                return http.build();
        }

        public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
                auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
        }
}