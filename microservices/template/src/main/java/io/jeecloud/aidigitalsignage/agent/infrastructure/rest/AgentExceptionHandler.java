package io.jeecloud.aidigitalsignage.agent.infrastructure.rest;

import io.jeecloud.aidigitalsignage.agent.domain.exception.AgentNotFoundException;
import io.jeecloud.aidigitalsignage.common.domain.exception.DomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST controllers.
 * Maps domain exceptions to appropriate HTTP responses.
 */
@RestControllerAdvice
public class AgentExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(AgentExceptionHandler.class);

    @ExceptionHandler(AgentNotFoundException.class)
    public ProblemDetail handleAgentNotFoundException(AgentNotFoundException ex) {
        logger.error("Agent not found: {}", ex.getMessage());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage()
        );
        problemDetail.setTitle("Agent Not Found");
        problemDetail.setType(URI.create("https://api.allianz.com/problems/agent-not-found"));
        problemDetail.setProperty("timestamp", Instant.now());
        
        return problemDetail;
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomainException(DomainException ex) {
        logger.error("Domain exception: {}", ex.getMessage());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problemDetail.setTitle("Business Rule Violation");
        problemDetail.setType(URI.create("https://api.allianz.com/problems/domain-error"));
        problemDetail.setProperty("timestamp", Instant.now());
        
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        logger.error("Validation error: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Validation failed for one or more fields"
        );
        problemDetail.setTitle("Validation Error");
        problemDetail.setType(URI.create("https://api.allianz.com/problems/validation-error"));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("errors", errors);
        
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        logger.error("Unexpected error", ex);
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred"
        );
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create("https://api.allianz.com/problems/internal-error"));
        problemDetail.setProperty("timestamp", Instant.now());
        
        return problemDetail;
    }
}

