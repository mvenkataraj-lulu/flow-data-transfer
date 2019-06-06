package com.lululemon.flow.data.transfer.filter;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AuthenticationFilter extends GenericFilterBean {

    private static final String API_TOKEN_PREFIX = "api.key";

    private List<String> apiTokens = new ArrayList<>();


    public AuthenticationFilter() {
        Map<String, String> env = System.getenv();
        for (String envName : env.keySet()) {
            if (envName.startsWith(API_TOKEN_PREFIX)) {
                apiTokens.add(env.get(envName));
            }
        }
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;

        final String token = httpRequest.getHeader("API-TOKEN");

        if (token == null) {
            throw new SecurityException("API-TOKEN is null, please provide token as header");
        }

        if (!apiTokens.contains(token)) {
            throw new SecurityException("API-TOKEN is invalid, please provide valid token");
        }

        final User user = new User("user", "password", new ArrayList<>());

        final UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);


        chain.doFilter(request, response);
    }
}



