package com.mycompany.auth.security.jwt;

import com.mycompany.auth.security.exception.ExpiredTokenException;
import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

  public static final String HEADER = "Authorization";
  public static final String HEADER_PREFIX = "Bearer ";

  private final UserDetailsService userDetailsService;
  private final RSAPrivateKey rsaPrivateKey;
  private final RSAPublicKey rsaPublicKey;

  @Value("${auth.jwt.access-token-validity-seconds}")
  private long accessTokenValiditySeconds;

  @Value("${auth.jwt.refresh-token-validity-seconds}")
  private long refreshTokenValiditySeconds;

  public JwtPair generateTokenPair(UserDetails user) {
    String access = createToken(user.getUsername(), accessTokenValiditySeconds);
    String refresh = createToken(user.getUsername(), refreshTokenValiditySeconds);
    return new JwtPair(access, refresh);
  }

  private String createToken(String subject, long validitySec) {
    Date now = new Date();
    Date exp = new Date(now.getTime() + validitySec * 1_000);
    return Jwts.builder()
        .setSubject(subject)
        .setIssuedAt(now)
        .setExpiration(exp)
        .signWith(rsaPrivateKey, SignatureAlgorithm.RS256)
        .compact();
  }

  public UserDetails parseJwtToken(String token) {
    if (StringUtils.hasText(token) && validateToken(token)) {
      String username = getUserNameFromJwtToken(token);
      return userDetailsService.loadUserByUsername(username);
    }
    return null;
  }

  public String getUserNameFromJwtToken(String token) {
    return Jwts.parser()
        .setSigningKey(rsaPublicKey)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parser().setSigningKey(rsaPublicKey).build().parseClaimsJws(token);
      return true;
    } catch (MalformedJwtException | IllegalArgumentException ex) {
      log.debug("Invalid JWT token", ex);
      throw new JwtException("Invalid JWT token", ex);
    } catch (ExpiredJwtException ex) {
      log.debug("Expired JWT token", ex);
      throw new ExpiredTokenException(token, "JWT token expired", ex);
    } catch (UnsupportedJwtException | SignatureException ex) {
      log.debug("JWT validation failed", ex);
      throw new JwtException("JWT validation failed", ex);
    }
  }

  public String getTokenFromRequest(HttpServletRequest req) {
    String header = req.getHeader(HEADER);
    if (!StringUtils.hasText(header) || !header.startsWith(HEADER_PREFIX)) {
      throw new AuthenticationServiceException("Missing or invalid Authorization header");
    }
    return header.substring(HEADER_PREFIX.length());
  }
}
