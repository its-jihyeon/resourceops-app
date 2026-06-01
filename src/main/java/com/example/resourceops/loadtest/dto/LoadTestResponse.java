package com.example.resourceops.loadtest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoadTestResponse {
    private String type;
    private Object value;
    private String message;
}