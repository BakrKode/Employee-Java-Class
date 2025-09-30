package com.reliaquest.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Wrapper for single-item responses: { "data": {...}, "status": "..." } */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiSingleResponse<T>(T data, String status) {}
