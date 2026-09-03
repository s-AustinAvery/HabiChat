package com.habichat.security;

import com.habichat.entity.Identity;
import com.habichat.service.IdentityService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * This is not a full authentication (no login/password)
 * This just resolves the bearer token (if present) to an Identity and
 * attaches it as a request attribute for controllers.
 */
@Component
public class TokenAuthFilter extends OncePerRequestFilter {

    public static final String IDENTITY_ATTRIBUTE = "identity";

    private final IdentityService identityService;

    public TokenAuthFilter(IdentityService identityService) {
        this.identityService = identityService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring("Bearer ".length());
            Optional<Identity> identity = identityService.resolveByToken(token);
            identity.ifPresent(value -> request.setAttribute(IDENTITY_ATTRIBUTE, value));
        }
        filterChain.doFilter(request, response);
    }
}
