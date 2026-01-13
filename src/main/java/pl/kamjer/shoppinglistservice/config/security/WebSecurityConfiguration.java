package pl.kamjer.shoppinglistservice.config.security;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pl.kamjer.shoppinglistservice.config.components.SkipAuthorizationFilter;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfiguration {

    private static final String USER_URL = "/user";
    private static final String USER_LOG_URL = "/user/log";
    private static final String UTIL_MASSAGE_URL = "/util/message";

    private static final List<String> urlPertmit = List.of(USER_URL, USER_LOG_URL, UTIL_MASSAGE_URL);

    private final UserDetailService userDetailsService;
    private final SkipAuthorizationFilter skipAuthorizationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(skipAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests((authz) -> {
                    urlPertmit.forEach(s -> authz.requestMatchers(s).permitAll());
                    authz.anyRequest().authenticated();
                })
                .httpBasic(withDefaults());

        return httpSecurity.build();
    }

    @Bean
    AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}