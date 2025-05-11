package com.mycompany.auth.config;

import com.mycompany.auth.exception.ErrorResponseHandler;
import com.mycompany.auth.security.jwt.JwtTokenProvider;
import com.mycompany.auth.security.jwt.RefreshTokenAuthenticationFilter;
import com.mycompany.auth.security.jwt.TokenAuthenticationFilter;
import com.mycompany.auth.security.login.LoginAuthenticationFilter;
import com.mycompany.auth.security.matcher.SkipPathRequestMatcher;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@Order(SecurityProperties.BASIC_AUTH_ORDER)
public class SecurityConfiguration {

  public static final String SIGNIN_ENDPOINT = "/auth/signin";
  public static final String SIGNUP_ENDPOINT = "/auth/signup";
  public static final String TOKEN_REFRESH_ENDPOINT = "/auth/refreshToken";
  public static final String API_ENDPOINT = "/admin/**";

  private final JwtTokenProvider jwtTokenProvider;
  private final AuthenticationManager authenticationManager;
  private final AuthenticationSuccessHandler loginSuccessHandler;
  private final AuthenticationFailureHandler failureHandler;
  private final ErrorResponseHandler errorResponseHandler;

  public SecurityConfiguration(
      JwtTokenProvider jwtTokenProvider,
      AuthenticationManager authenticationManager,
      @Qualifier("loginAuthenticationSuccessHandler")
          AuthenticationSuccessHandler loginSuccessHandler,
      AuthenticationFailureHandler failureHandler,
      ErrorResponseHandler errorResponseHandler) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.authenticationManager = authenticationManager;
    this.loginSuccessHandler = loginSuccessHandler;
    this.failureHandler = failureHandler;
    this.errorResponseHandler = errorResponseHandler;
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    var config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("*"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(errorResponseHandler)
                    .accessDeniedHandler(errorResponseHandler))
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(SIGNIN_ENDPOINT, SIGNUP_ENDPOINT, TOKEN_REFRESH_ENDPOINT)
                    .permitAll()
                    .requestMatchers(API_ENDPOINT)
                    .hasRole("ADMIN")
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(tokenFilter(), UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(loginFilter(), TokenAuthenticationFilter.class)
        .addFilterBefore(refreshFilter(), LoginAuthenticationFilter.class);
    return http.build();
  }

  private LoginAuthenticationFilter loginFilter() {
    var filter =
        new LoginAuthenticationFilter(SIGNIN_ENDPOINT, loginSuccessHandler, failureHandler);
    filter.setAuthenticationManager(authenticationManager);
    return filter;
  }

  private TokenAuthenticationFilter tokenFilter() {
    var pathsToSkip =
        List.of(SIGNIN_ENDPOINT, SIGNUP_ENDPOINT, TOKEN_REFRESH_ENDPOINT, API_ENDPOINT);
    var matcher = new SkipPathRequestMatcher(pathsToSkip);
    var filter = new TokenAuthenticationFilter(jwtTokenProvider, matcher, failureHandler);
    filter.setAuthenticationManager(authenticationManager);
    return filter;
  }

  private RefreshTokenAuthenticationFilter refreshFilter() {
    var filter =
        new RefreshTokenAuthenticationFilter(
            TOKEN_REFRESH_ENDPOINT, loginSuccessHandler, failureHandler);
    filter.setAuthenticationManager(authenticationManager);
    return filter;
  }
}
