package com.mycompany.auth.security.matcher;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

public class SkipPathRequestMatcher implements RequestMatcher {
  private final OrRequestMatcher matchers;

  public SkipPathRequestMatcher(final List<String> pathsToSkip) {
    Assert.notNull(pathsToSkip, "List of paths to skip is required.");
    List<RequestMatcher> m =
        pathsToSkip.stream().map(AntPathRequestMatcher::new).collect(Collectors.toList());
    matchers = new OrRequestMatcher(m);
  }

  @Override
  public boolean matches(final HttpServletRequest request) {
    return !matchers.matches(request);
  }
}
