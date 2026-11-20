package pl.kamjer.shoppinglistservice.config.websocket;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.socket.WebSocketHandler;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class WebSocketTokenHandshakeInterceptorTest {

    private final WebSocketTokenHandshakeInterceptor interceptor = new WebSocketTokenHandshakeInterceptor();

    private Map<String, Object> beforeHandshake(MockHttpServletRequest servletRequest,
                                                MockHttpServletResponse servletResponse) {
        Map<String, Object> attributes = new HashMap<>();
        boolean proceed = interceptor.beforeHandshake(
                new ServletServerHttpRequest(servletRequest),
                new ServletServerHttpResponse(servletResponse),
                mock(WebSocketHandler.class),
                attributes);
        assertThat(proceed).isTrue();
        return attributes;
    }

    @Test
    void capturesTokenFromAuthorizationHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ws");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer header-token");

        Map<String, Object> attributes = beforeHandshake(request, new MockHttpServletResponse());

        assertThat(attributes).containsEntry(
                WebSocketTokenHandshakeInterceptor.HANDSHAKE_TOKEN_ATTRIBUTE, "header-token");
    }

    @Test
    void capturesTokenFromQueryParameter() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ws");
        request.setQueryString("token=query-token");

        Map<String, Object> attributes = beforeHandshake(request, new MockHttpServletResponse());

        assertThat(attributes).containsEntry(
                WebSocketTokenHandshakeInterceptor.HANDSHAKE_TOKEN_ATTRIBUTE, "query-token");
    }

    @Test
    void rejectsConflictingTokens() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ws");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer header-token");
        request.setQueryString("token=query-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Map<String, Object> attributes = new HashMap<>();

        boolean proceed = interceptor.beforeHandshake(
                new ServletServerHttpRequest(request),
                new ServletServerHttpResponse(response),
                mock(WebSocketHandler.class),
                attributes);

        assertThat(proceed).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(attributes).isEmpty();
    }

    @Test
    void proceedsWithoutToken() {
        Map<String, Object> attributes = beforeHandshake(
                new MockHttpServletRequest("GET", "/ws"), new MockHttpServletResponse());

        assertThat(attributes).isEmpty();
    }
}
