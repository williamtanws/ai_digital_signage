package io.jeecloud.aidigitalsignage.common.infrastructure.rest.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ApiResponse
 */
@DisplayName("ApiResponse Tests")
class ApiResponseTest {

    @Test
    @DisplayName("Should create success response with data")
    void shouldCreateSuccessResponseWithData() {
        String data = "test data";
        ApiResponse<String> response = ApiResponse.success(data);
        
        assertThat(response.success()).isTrue();
        assertThat(response.data()).isEqualTo(data);
        assertThat(response.message()).isNull();
        assertThat(response.timestamp()).isNotNull();
        assertThat(response.timestamp()).isBeforeOrEqualTo(Instant.now());
        assertThat(response.metadata()).isNull();
    }

    @Test
    @DisplayName("Should create success response with data and message")
    void shouldCreateSuccessResponseWithDataAndMessage() {
        String data = "test data";
        String message = "Operation successful";
        ApiResponse<String> response = ApiResponse.success(data, message);
        
        assertThat(response.success()).isTrue();
        assertThat(response.data()).isEqualTo(data);
        assertThat(response.message()).isEqualTo(message);
        assertThat(response.timestamp()).isNotNull();
        assertThat(response.metadata()).isNull();
    }

    @Test
    @DisplayName("Should create error response with message")
    void shouldCreateErrorResponseWithMessage() {
        String errorMessage = "Error occurred";
        ApiResponse<String> response = ApiResponse.error(errorMessage);
        
        assertThat(response.success()).isFalse();
        assertThat(response.data()).isNull();
        assertThat(response.message()).isEqualTo(errorMessage);
        assertThat(response.timestamp()).isNotNull();
        assertThat(response.metadata()).isNull();
    }

    @Test
    @DisplayName("Should create error response with message and metadata")
    void shouldCreateErrorResponseWithMessageAndMetadata() {
        String errorMessage = "Validation failed";
        Map<String, Object> metadata = Map.of("field", "email", "error", "invalid format");
        ApiResponse<String> response = ApiResponse.error(errorMessage, metadata);
        
        assertThat(response.success()).isFalse();
        assertThat(response.data()).isNull();
        assertThat(response.message()).isEqualTo(errorMessage);
        assertThat(response.timestamp()).isNotNull();
        assertThat(response.metadata()).isEqualTo(metadata);
    }

    @Test
    @DisplayName("Should handle null data in success response")
    void shouldHandleNullDataInSuccessResponse() {
        ApiResponse<String> response = ApiResponse.success(null);
        
        assertThat(response.success()).isTrue();
        assertThat(response.data()).isNull();
    }

    @Test
    @DisplayName("Should support different data types")
    void shouldSupportDifferentDataTypes() {
        // Integer
        ApiResponse<Integer> intResponse = ApiResponse.success(42);
        assertThat(intResponse.data()).isEqualTo(42);
        
        // List
        ApiResponse<java.util.List<String>> listResponse = ApiResponse.success(java.util.List.of("a", "b"));
        assertThat(listResponse.data()).containsExactly("a", "b");
        
        // Map
        ApiResponse<Map<String, String>> mapResponse = ApiResponse.success(Map.of("key", "value"));
        assertThat(mapResponse.data()).containsEntry("key", "value");
    }

    @Test
    @DisplayName("Should be immutable record")
    void shouldBeImmutableRecord() {
        ApiResponse<String> response = ApiResponse.success("data");
        
        // Records are immutable - verify all fields are accessible
        assertThat(response.success()).isNotNull();
        assertThat(response.data()).isNotNull();
        assertThat(response.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        Instant now = Instant.now();
        ApiResponse<String> response1 = new ApiResponse<>(true, "data", "msg", now, null);
        ApiResponse<String> response2 = new ApiResponse<>(true, "data", "msg", now, null);
        ApiResponse<String> response3 = new ApiResponse<>(false, "data", "msg", now, null);
        
        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        assertThat(response1).isNotEqualTo(response3);
    }

    @Test
    @DisplayName("Should have toString implementation")
    void shouldHaveToStringImplementation() {
        ApiResponse<String> response = ApiResponse.success("data", "message");
        String toString = response.toString();
        
        assertThat(toString).contains("success=true");
        assertThat(toString).contains("data=data");
        assertThat(toString).contains("message=message");
    }
}
