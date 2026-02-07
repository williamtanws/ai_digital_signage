package io.jeecloud.aidigitalsignage.common.infrastructure.rest.common;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseTest {

    @Test
    void shouldCreatePageResponseWithAllFields() {
        // Given
        List<String> content = List.of("item1", "item2", "item3");
        int page = 1;
        int size = 3;
        long totalElements = 10;
        int totalPages = 4;
        boolean first = false;
        boolean last = false;

        // When
        PageResponse<String> response = new PageResponse<>(
            content, page, size, totalElements, totalPages, first, last
        );

        // Then
        assertThat(response.content()).isEqualTo(content);
        assertThat(response.page()).isEqualTo(page);
        assertThat(response.size()).isEqualTo(size);
        assertThat(response.totalElements()).isEqualTo(totalElements);
        assertThat(response.totalPages()).isEqualTo(totalPages);
        assertThat(response.first()).isFalse();
        assertThat(response.last()).isFalse();
    }

    @Test
    void shouldCreateFromSpringPageForFirstPage() {
        // Given
        List<String> content = List.of("A", "B", "C");
        Page<String> springPage = new PageImpl<>(content, PageRequest.of(0, 3), 10);

        // When
        PageResponse<String> response = PageResponse.of(springPage);

        // Then
        assertThat(response.content()).isEqualTo(content);
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.size()).isEqualTo(3);
        assertThat(response.totalElements()).isEqualTo(10);
        assertThat(response.totalPages()).isEqualTo(4);
        assertThat(response.first()).isTrue();
        assertThat(response.last()).isFalse();
    }

    @Test
    void shouldCreateFromSpringPageForMiddlePage() {
        // Given
        List<String> content = List.of("D", "E", "F");
        Page<String> springPage = new PageImpl<>(content, PageRequest.of(1, 3), 10);

        // When
        PageResponse<String> response = PageResponse.of(springPage);

        // Then
        assertThat(response.content()).isEqualTo(content);
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(3);
        assertThat(response.totalElements()).isEqualTo(10);
        assertThat(response.totalPages()).isEqualTo(4);
        assertThat(response.first()).isFalse();
        assertThat(response.last()).isFalse();
    }

    @Test
    void shouldCreateFromSpringPageForLastPage() {
        // Given
        List<String> content = List.of("J");
        Page<String> springPage = new PageImpl<>(content, PageRequest.of(3, 3), 10);

        // When
        PageResponse<String> response = PageResponse.of(springPage);

        // Then
        assertThat(response.content()).isEqualTo(content);
        assertThat(response.page()).isEqualTo(3);
        assertThat(response.size()).isEqualTo(3);
        assertThat(response.totalElements()).isEqualTo(10);
        assertThat(response.totalPages()).isEqualTo(4);
        assertThat(response.first()).isFalse();
        assertThat(response.last()).isTrue();
    }

    @Test
    void shouldCreateFromSpringPageWithEmptyContent() {
        // Given
        Page<String> springPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        // When
        PageResponse<String> response = PageResponse.of(springPage);

        // Then
        assertThat(response.content()).isEmpty();
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(0);
        assertThat(response.totalPages()).isEqualTo(0);
        assertThat(response.first()).isTrue();
        assertThat(response.last()).isTrue();
    }

    @Test
    void shouldCreateFromSpringPageWithSinglePage() {
        // Given
        List<Integer> content = List.of(1, 2, 3);
        Page<Integer> springPage = new PageImpl<>(content, PageRequest.of(0, 10), 3);

        // When
        PageResponse<Integer> response = PageResponse.of(springPage);

        // Then
        assertThat(response.content()).containsExactly(1, 2, 3);
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(3);
        assertThat(response.totalPages()).isEqualTo(1);
        assertThat(response.first()).isTrue();
        assertThat(response.last()).isTrue();
    }

    @Test
    void shouldSupportComplexObjectTypes() {
        // Given
        List<User> content = List.of(new User("Alice", 30), new User("Bob", 25));
        Page<User> springPage = new PageImpl<>(content, PageRequest.of(0, 2), 2);

        // When
        PageResponse<User> response = PageResponse.of(springPage);

        // Then
        assertThat(response.content()).hasSize(2);
        assertThat(response.content().get(0).name()).isEqualTo("Alice");
        assertThat(response.content().get(1).name()).isEqualTo("Bob");
        assertThat(response.totalElements()).isEqualTo(2);
    }

    @Test
    void shouldPreservePageNumberFromSpringData() {
        // Given
        Page<String> page5 = new PageImpl<>(List.of("X"), PageRequest.of(5, 1), 10);

        // When
        PageResponse<String> response = PageResponse.of(page5);

        // Then
        assertThat(response.page()).isEqualTo(5);
    }

    // Static nested class for testing complex objects
    static record User(String name, int age) {}
}
