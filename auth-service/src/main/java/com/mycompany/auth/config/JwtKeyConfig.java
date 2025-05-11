package com.mycompany.auth.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class JwtKeyConfig {
  @Value("${auth.jwt.private-key-location}")
  private Resource privateKeyResource;

  @Value("${auth.jwt.public-key-location}")
  private Resource publicKeyResource;

  @Bean
  public RSAPrivateKey rsaPrivateKey() throws IOException, GeneralSecurityException {
    String pem =
        new String(privateKeyResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
            .replaceAll("-----BEGIN (.*)-----", "")
            .replaceAll("-----END (.*)-----", "")
            .replaceAll("\\s", "");
    byte[] der = Base64.getDecoder().decode(pem);
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
    return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);
  }

  @Bean
  public RSAPublicKey rsaPublicKey() throws IOException, GeneralSecurityException {
    String pem =
        new String(publicKeyResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
            .replaceAll("-----BEGIN (.*)-----", "")
            .replaceAll("-----END (.*)-----", "")
            .replaceAll("\\s", "");
    byte[] der = Base64.getDecoder().decode(pem);
    X509EncodedKeySpec spec = new X509EncodedKeySpec(der);
    return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
  }
}
