package pl.xsware.infrastructure.security.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.xsware.infrastructure.common.SecurityConstants;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(SecurityConstants.BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        var token = header.substring(SecurityConstants.BEARER_PREFIX.length()).trim();
        try {
            Claims claims = jwtService.parse(token);
            String userId = claims.getSubject();
            String role = claims.get("role", String.class);
            String email = claims.get("email", String.class);

            var auth = new UsernamePasswordAuthenticationToken(
                    email,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );
            auth.setDetails(userId);
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception ignored) {
            // ignored
        }

        filterChain.doFilter(request, response);
    }
}