package com.mycompany.auth.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

class ErrorResponseHandlerTest {

  @Mock private HttpServletRequest req;
  @Mock private HttpServletResponse res;

  private ErrorResponseHandler handler;
  private StringWriter body;

  @BeforeEach
  void init() throws Exception {
    MockitoAnnotations.openMocks(this);
    handler = new ErrorResponseHandler();
    body = new StringWriter();
    when(res.getWriter()).thenReturn(new PrintWriter(body));
  }

  @Test
  void commence_returns401_onAuthenticationException() throws Exception {
    handler.commence(req, res, new AuthenticationException("bad") {});
    verify(res).setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    JsonNode node = new ObjectMapper().readTree(body.toString());
    assertEquals("exception.authenticationFailed", node.get("message").asText());
    assertEquals(2, node.get("code").asInt());
  }

  @Test
  void accessDenied_returns403_onAccessDeniedException() throws Exception {
    handler.handle(req, res, new AccessDeniedException("denied"));
    verify(res).setStatus(HttpServletResponse.SC_FORBIDDEN);

    JsonNode node = new ObjectMapper().readTree(body.toString());
    assertEquals("exception.accessDenied", node.get("message").asText());
    assertEquals(20, node.get("code").asInt());
  }
}
