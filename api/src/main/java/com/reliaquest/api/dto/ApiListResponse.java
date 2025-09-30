package com.reliaquest.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** Wrapper for list responses: { "data": [ ... ], "status": "..." } */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiListResponse<T>(List<T> data, String status) {}
