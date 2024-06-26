package com.sq018.monieflex.configs;

import com.sq018.monieflex.exceptions.MonieFlexException;
import com.sq018.monieflex.services.implementations.JwtImplementation;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Collections;

@Configuration
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final UserDetailsService userDetailsService;
    private final JwtImplementation jwtImplementation;

    @Override
    @SneakyThrows
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) {
        String header = request.getHeader("Authorization");

        if(header == null || !header.startsWith("Bearer")){
            filterChain.doFilter(request, response);
            return;
        }

        String jwtToken = header.substring(7);

        if(jwtImplementation.isExpired(jwtToken)) {
            throw new MonieFlexException("Your session has expired. Please login");
        }

        String email = jwtImplementation.extractEmailAddressFromToken(jwtToken);
        if(email != null && SecurityContextHolder.getContext().getAuthentication() == null){
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    this.userDetailsService.loadUserByUsername(email), null, Collections.emptyList()
            );
            token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(token);
        }
        filterChain.doFilter(request, response);
    }
}
