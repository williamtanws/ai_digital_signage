package io.jeecloud.aidigitalsignage.common.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for DomainException
 */
@DisplayName("DomainException Tests")
class DomainExceptionTest {

    @Test
    @DisplayName("Should create exception with message")
    void shouldCreateExceptionWithMessage() {
        String message = "Test domain exception";
        DomainException exception = new DomainException(message);
        
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Should create exception with message and cause")
    void shouldCreateExceptionWithMessageAndCause() {
        String message = "Test domain exception";
        Throwable cause = new IllegalArgumentException("Root cause");
        DomainException exception = new DomainException(message, cause);
        
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("Should be a RuntimeException")
    void shouldBeRuntimeException() {
        DomainException exception = new DomainException("test");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should be throwable")
    void shouldBeThrowable() {
        assertThatThrownBy(() -> {
            throw new DomainException("test exception");
        })
        .isInstanceOf(DomainException.class)
        .hasMessage("test exception");
    }

    @Test
    @DisplayName("Should preserve stack trace")
    void shouldPreserveStackTrace() {
        DomainException exception = new DomainException("test");
        assertThat(exception.getStackTrace()).isNotEmpty();
    }
}
