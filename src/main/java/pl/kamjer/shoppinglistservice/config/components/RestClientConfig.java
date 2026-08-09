package pl.kamjer.shoppinglistservice.config.components;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RestClientConfig {

    @Bean (name = "userRestClient")
    public RestClient userRestClient(@Value("${user.service.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

}
