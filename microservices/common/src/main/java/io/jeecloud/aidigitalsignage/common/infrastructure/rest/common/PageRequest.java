package io.jeecloud.aidigitalsignage.common.infrastructure.rest.common;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Standard pagination parameters for list endpoints.
 */
public record PageRequest(
    @Min(0) Integer page,
    @Min(1) @Max(100) Integer size,
    String sort
) {
    public PageRequest {
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 20;
        }
    }
    
    public static PageRequest of(Integer page, Integer size, String sort) {
        return new PageRequest(page, size, sort);
    }
    
    public org.springframework.data.domain.PageRequest toSpringPageRequest() {
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            String property = parts[0];
            org.springframework.data.domain.Sort.Direction direction = 
                parts.length > 1 && "desc".equalsIgnoreCase(parts[1]) 
                    ? org.springframework.data.domain.Sort.Direction.DESC 
                    : org.springframework.data.domain.Sort.Direction.ASC;
            return org.springframework.data.domain.PageRequest.of(
                page, 
                size, 
                org.springframework.data.domain.Sort.by(direction, property)
            );
        }
        return org.springframework.data.domain.PageRequest.of(page, size);
    }
}
