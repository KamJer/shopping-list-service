package pl.kamjer.shoppinglistservice.config.websocket;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * Captures the access token while the HTTP handshake request is still available.
 *
 * <p>The security filter authenticates the upgrade request, but its thread-local
 * {@code SecurityContext} is cleared before {@code WebSocketHandler.afterConnectionEstablished}
 * runs. Native clients send the token in the Authorization header and cannot fall back to a
 * query parameter, so without this interceptor the token is lost and later WS messages fail
 * with "User not authenticated".</p>
 */
public class WebSocketTokenHandshakeInterceptor implements HandshakeInterceptor {

    public static final String HANDSHAKE_TOKEN_ATTRIBUTE = "HANDSHAKE_TOKEN";
    public static final String TOKEN_ATTRIBUTE = "TOKEN";

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String TOKEN_QUERY_PARAM = "token";

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        String headerToken = extractHeaderToken(request);
        String queryToken = extractQueryToken(request);

        if (headerToken != null && queryToken != null && !headerToken.equals(queryToken)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        String token = headerToken != null ? headerToken : queryToken;
        if (token != null) {
            attributes.put(HANDSHAKE_TOKEN_ATTRIBUTE, token);
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // Token is already stored in the session attributes.
    }

    private String extractHeaderToken(ServerHttpRequest request) {
        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }
        String token = header.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }

    private String extractQueryToken(ServerHttpRequest request) {
        String token = UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams()
                .getFirst(TOKEN_QUERY_PARAM);
        return (token == null || token.isBlank()) ? null : token;
    }
}
