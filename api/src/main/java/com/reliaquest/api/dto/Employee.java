package com.reliaquest.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Employee(
        String id,
        @JsonProperty("employee_name") String employeeName,
        @JsonProperty("employee_salary") int employeeSalary,
        @JsonProperty("employee_age") int employeeAge,
        @JsonProperty("employee_title") String employeeTitle,
        @JsonProperty("employee_email") String employeeEmail) {}
