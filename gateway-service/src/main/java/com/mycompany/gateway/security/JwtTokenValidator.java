package com.mycompany.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@Slf4j
public class JwtTokenValidator implements InitializingBean {

  @Value("${security.jwt.public-key}")
  private String publicKeyPem;

  private RSAPublicKey publicKey;

  @Override
  public void afterPropertiesSet() throws Exception {
    String cleanPem = publicKeyPem.replaceAll("\\s", "");
    byte[] der = Base64.getDecoder().decode(cleanPem);
    X509EncodedKeySpec spec = new X509EncodedKeySpec(der);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    PublicKey key = kf.generatePublic(spec);
    this.publicKey = (RSAPublicKey) key;
    log.info("JWT public key successfully initialised");
  }

  public Mono<Claims> validate(String token) {
    return Mono.fromCallable(
            () ->
                Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody())
        .doOnError(e -> log.debug("JWT validation failed: {}", e.getMessage()))
        .subscribeOn(Schedulers.boundedElastic());
  }
}
