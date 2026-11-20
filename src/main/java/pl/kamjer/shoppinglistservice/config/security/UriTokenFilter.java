package pl.kamjer.shoppinglistservice.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@AllArgsConstructor
@Log4j2
public class UriTokenFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String TOKEN_QUERY_PARAM = "token";

    private AuthenticationManager authenticationManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String headerToken = extractHeaderToken(request);
        String queryToken = extractQueryToken(request);

//        both channels used at once with different tokens -> reject, so a caller cannot smuggle
//        an identity that does not match the one it was explicitly authenticated with
        if (headerToken != null && queryToken != null && !headerToken.equals(queryToken)) {
            log.warn("Rejecting request: Authorization header and token query parameter carry different tokens");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Conflicting authentication tokens");
            return;
        }

        String token = headerToken != null ? headerToken : queryToken;

        if (token != null) {
            try {
                JwtAuthToken authRequest = new JwtAuthToken(token);
                Authentication authResult = authenticationManager.authenticate(authRequest);

                SecurityContextHolder.getContext().setAuthentication(authResult);
                log.info("Authenticated request via {}", headerToken != null ? AUTHORIZATION_HEADER : TOKEN_QUERY_PARAM);
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractHeaderToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }
        String token = header.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }

    private String extractQueryToken(HttpServletRequest request) {
        String token = request.getParameter(TOKEN_QUERY_PARAM);
        return (token == null || token.isBlank()) ? null : token;
    }
}
