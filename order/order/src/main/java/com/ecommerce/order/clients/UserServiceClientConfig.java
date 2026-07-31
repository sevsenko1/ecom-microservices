package com.ecommerce.order.clients;

import org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class UserServiceClientConfig {

    @Bean
    public UserServiceClient userServiceClient(LoadBalancerInterceptor loadBalancerInterceptor) {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://user-service")
                .requestInterceptor(loadBalancerInterceptor)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (request, response) -> {
                    // 4xx durumunda exception fırlatma, sessizce geç
                })
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(UserServiceClient.class);
    }
}