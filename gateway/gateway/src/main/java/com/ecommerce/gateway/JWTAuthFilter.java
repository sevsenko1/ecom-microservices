package com.ecommerce.gateway;

import org.apache.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

//@Component
public class JWTAuthFilter implements WebFilter {


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String autHeader= exchange.getRequest()
                .getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if(autHeader== null || !autHeader.startsWith("Bearer")){
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // TODO : Validate JWT Token
        return chain.filter(exchange);
    }
}
