package org.example.eventpal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final FilterChainExceptionHandler filterChainExceptionHandler;

    private final AuthenticationProvider authenticationProvider;

    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    private final LogoutHandler logoutHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                                 FilterChainExceptionHandler filterChainExceptionHandler,
                                 AuthenticationProvider authenticationProvider,
                                 LogoutHandler logoutHandler,
                                 CustomAccessDeniedHandler customAccessDeniedHandler,
                                 CustomAuthenticationEntryPoint customAuthenticationEntryPoint) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.filterChainExceptionHandler = filterChainExceptionHandler;
        this.authenticationProvider = authenticationProvider;
        this.logoutHandler = logoutHandler;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(permittedEndpoints).permitAll()
                        .anyRequest().authenticated()
                )
                .cors(cors ->{})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(filterChainExceptionHandler, LogoutFilter.class)
                .logout(logout ->
                        logout.logoutUrl("/api/v1/auth/logout").
                                addLogoutHandler(logoutHandler).
                                logoutSuccessHandler((request, response, authentication) ->
                                        SecurityContextHolder.clearContext()
                                ));

        return http.build();
    }

    private final String[] permittedEndpoints = {
            "/api/v1/auth/**",
            "/api/v1/users/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api/v1/events/**",
            "/api/v1/venues/**",
            "/api/v1/checkout/**",
            "/api/v1/ticket-types/**",
            "/api/v1/tickets/**",
    };
}
