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

    /** GET /employee/{id} */
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

    /** POST /employee (body: {name, salary, age, title}) */
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

    /** Mock API deletes by NAME: DELETE /employee/{name} → {"data": true} */
    // in EmployeeClient
    public boolean deleteByName(String name) {
        log.info("DELETE /employee/{}", name);
        // for other statuses, surface as error
        return Boolean.TRUE.equals(webClient.delete()
                .uri("/employee/{name}", name)
                .exchangeToMono(resp -> {
                    if (resp.statusCode().is2xxSuccessful()) {
                        return resp.bodyToMono(BooleanResponse.class)
                                .map(r -> r != null && Boolean.TRUE.equals(r.data()));
                    }
                    if (resp.statusCode().value() == 404) {
                        log.info("DELETE /employee/{} -> 404 (not found)", name);
                        return reactor.core.publisher.Mono.just(false);
                    }
                    if (resp.statusCode().is5xxServerError()) {
                        return resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .doOnNext(body -> log.warn("DELETE /employee/{} -> {} body={}",
                                        name, resp.statusCode(), body))
                                .thenReturn(false);
                    }
                    // for other statuses, surface as error
                    return resp.createException().flatMap(reactor.core.publisher.Mono::error);
                })
                .block());
    }

}
