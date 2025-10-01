package com.reliaquest.api.client;

import com.reliaquest.api.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class EmployeeClient {

    private static final Logger log = LoggerFactory.getLogger(EmployeeClient.class);
    private final WebClient webClient;

    public EmployeeClient(@Qualifier("mockApiClient") WebClient webClient) {
        this.webClient = webClient;
    }

    private static final ParameterizedTypeReference<ApiListResponse<Employee>> LIST_EMPLOYEES =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<ApiSingleResponse<Employee>> SINGLE_EMPLOYEE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<BooleanResponse> BOOL_RESPONSE =
            new ParameterizedTypeReference<>() {};

    /**
     * GET /employee
     */
    public ApiListResponse<Employee> getAll() {
        log.info("GET /employee");
        return webClient
                .get()
                .uri("/employee")
                .retrieve()
                .bodyToMono(LIST_EMPLOYEES)
                .doOnSuccess(e -> log.info("GET /employee succeeded"))
                .doOnError(e -> log.error("GET /employee failed: {}", e.toString()))
                .block();
    }

    /**
     * GET /employee/{id}
     */
    public ApiSingleResponse<Employee> getById(String id) {
        log.info("GET /employee/{}", id);
        return webClient
                .get()
                .uri("/employee/{id}", id)
                .retrieve()
                .bodyToMono(SINGLE_EMPLOYEE)
                .doOnSuccess(r -> log.info("GET /employee/{} succeeded", id))
                .doOnError(e -> log.error("GET /employee/{} failed: {}", id, e.toString()))
                .block();
    }

    /**
     * POST /employee (body: {name, salary, age, title})
     */
    public ApiSingleResponse<Employee> create(EmployeeCreateRequest req) {
        log.info(
                "POST /employee name={}, salary={}, age={}, title={}",
                req.name(),
                req.salary(),
                req.age(),
                req.title());
        return webClient
                .post()
                .uri("/employee")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(SINGLE_EMPLOYEE)
                .doOnSuccess(r -> log.info("POST /employee succeeded"))
                .doOnError(e -> log.error("POST /employee failed: {}", e.toString()))
                .block();
    }

    public boolean deleteByName(String name) {
        log.info("DELETE /employee/{}", name);
        // 404 → doesn't exist
        // 405/500 "method not supported" → try alternate endpoint that expects a JSON body
        return Boolean.TRUE.equals(webClient
                .delete()
                .uri("/employee/{name}", name)
                .exchangeToMono(resp -> {
                    if (resp.statusCode().is2xxSuccessful()) {
                        return resp.bodyToMono(BooleanResponse.class)
                                .map(r -> r != null && Boolean.TRUE.equals(r.data()));
                    }
                    // 404 → doesn't exist
                    if (resp.statusCode().value() == 404) {
                        log.info("DELETE /employee/{} -> 404 (not found)", name);
                        return reactor.core.publisher.Mono.just(false);
                    }
                    // 405/500 "method not supported"
                    if (resp.statusCode().value() == 405 || resp.statusCode().is5xxServerError()) {
                        log.warn("DELETE /employee/{} -> {}. Trying body DELETE fallback.", name, resp.statusCode());
                        return webClient
                                .method(org.springframework.http.HttpMethod.DELETE)
                                .uri("/employee")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(java.util.Map.of("name", name))
                                .exchangeToMono(resp2 -> {
                                    if (resp2.statusCode().is2xxSuccessful()) {
                                        return resp2.bodyToMono(BooleanResponse.class)
                                                .map(r -> r != null && Boolean.TRUE.equals(r.data()));
                                    }
                                    if (resp2.statusCode().value() == 404)
                                        return reactor.core.publisher.Mono.just(false);
                                    return resp2.createException().flatMap(reactor.core.publisher.Mono::error);
                                });
                    }
                    return resp.createException().flatMap(reactor.core.publisher.Mono::error);
                })
                .block());
    }
}
