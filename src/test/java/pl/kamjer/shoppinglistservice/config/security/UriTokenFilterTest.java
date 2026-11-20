package pl.kamjer.shoppinglistservice.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UriTokenFilterTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    @Mock
    private Authentication authentication;

    private UriTokenFilter filter;

    @BeforeEach
    void setUp() {
        filter = new UriTokenFilter(authenticationManager);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authorizationHeader_authenticates() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer header-token");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        filter.doFilterInternal(request, response, filterChain);

        ArgumentCaptor<JwtAuthToken> captor = ArgumentCaptor.forClass(JwtAuthToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getCredentials()).isEqualTo("header-token");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(authentication);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void queryParameter_authenticates() throws Exception {
        when(request.getParameter("token")).thenReturn("query-token");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        filter.doFilterInternal(request, response, filterChain);

        ArgumentCaptor<JwtAuthToken> captor = ArgumentCaptor.forClass(JwtAuthToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getCredentials()).isEqualTo("query-token");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void headerPreferredWhenBothIdentical() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer same-token");
        when(request.getParameter("token")).thenReturn("same-token");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        filter.doFilterInternal(request, response, filterChain);

        ArgumentCaptor<JwtAuthToken> captor = ArgumentCaptor.forClass(JwtAuthToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getCredentials()).isEqualTo("same-token");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void conflictingTokens_rejectsWith401() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer header-token");
        when(request.getParameter("token")).thenReturn("query-token");

        filter.doFilterInternal(request, response, filterChain);

        verify(authenticationManager, never()).authenticate(any());
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Conflicting authentication tokens");
        verify(filterChain, never()).doFilter(any(), any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void invalidToken_rejectsWith401() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer bad-token");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid token"));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void noToken_passesThrough() throws Exception {
        filter.doFilterInternal(request, response, filterChain);

        verify(authenticationManager, never()).authenticate(any());
        verify(filterChain).doFilter(request, response);
    }
}
