package com.touralert.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import com.touralert.repository.UserRepository;
import com.touralert.model.User;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 1. Extract the HTTP Authorization header
        String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwtToken = null;

        // 2. Validate header structure (Must start with "Bearer ")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwtToken);
            } catch (Exception e) {
                System.out.println("JWT Parsing Error: " + e.getMessage());
            }
        }

        // 3. Inject user security context into Spring if token is authenticated
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validateToken(jwtToken, username)) {
                // Lookup user role and grant appropriate authority
                User user = userRepository.findByUsername(username).orElse(null);
                List<SimpleGrantedAuthority> authorities = Collections.emptyList();
                if (user != null && user.getRole() != null) {
                    authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
                }
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username, null, authorities
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Authorize the incoming request
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 4. Pass control down to the next filter in the pipeline chain
        filterChain.doFilter(request, response);
    }
}