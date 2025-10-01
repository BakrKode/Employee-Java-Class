package com.reliaquest.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Request body for POST /employee on the mock API.
 */
public record EmployeeCreateRequest(
        @NotBlank String name, @Positive int salary, @Min(16) @Max(75) int age, @NotBlank String title) {}
