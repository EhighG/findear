package com.findear.main.security;

import com.findear.main.member.common.domain.Role;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final String ACCESS_TOKEN = "access-token";
    private final String REFRESH_TOKEN = "refresh-token";
    private final JwtAuthenticationProvider authenticationProvider;

    private final Authentication authenticationForGuests;
    private Set<String> exclusiveUris;

    public JwtFilter(JwtAuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
        authenticationForGuests = JwtAuthenticationToken.authenticated("guest", "guestCredntial",
                null);
        exclusiveUris = new HashSet<>();
        exclusiveUris.addAll(Arrays.asList("/members/register", "/members/login"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        JwtAuthenticationToken authentication = new JwtAuthenticationToken("tmpPrincipal", "tmpCredentials", Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
//
//        authentication.setDetails(new JwtUserDetails("tmpAuthorities", "1", "sampletoken"));
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        filterChain.doFilter(request, response);
        String requestUri = request.getRequestURI();
        Authentication authentication = exclusiveUris.contains(requestUri) ? authenticationForGuests : attemptAuthenticate(request);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private Authentication attemptAuthenticate(HttpServletRequest request) {
        String accessToken = request.getHeader(ACCESS_TOKEN);

        if (!StringUtils.isEmpty(accessToken)) {
            return authenticationProvider.authenticateAccessToken(accessToken);
        } else {
            String refreshToken = request.getHeader(REFRESH_TOKEN);
            return authenticationProvider.authenticationRefreshToken(refreshToken);
        }
    }
}
