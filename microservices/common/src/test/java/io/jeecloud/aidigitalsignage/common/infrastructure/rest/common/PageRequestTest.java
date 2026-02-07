package io.jeecloud.aidigitalsignage.common.infrastructure.rest.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for PageRequest
 */
@DisplayName("PageRequest Tests")
class PageRequestTest {

    @Test
    @DisplayName("Should create PageRequest with all parameters")
    void shouldCreatePageRequestWithAllParameters() {
        PageRequest request = new PageRequest(1, 50, "name,asc");
        
        assertThat(request.page()).isEqualTo(1);
        assertThat(request.size()).isEqualTo(50);
        assertThat(request.sort()).isEqualTo("name,asc");
    }

    @Test
    @DisplayName("Should use default page when null")
    void shouldUseDefaultPageWhenNull() {
        PageRequest request = new PageRequest(null, 20, null);
        
        assertThat(request.page()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should use default size when null")
    void shouldUseDefaultSizeWhenNull() {
        PageRequest request = new PageRequest(0, null, null);
        
        assertThat(request.size()).isEqualTo(20);
    }

    @Test
    @DisplayName("Should use both defaults when null")
    void shouldUseBothDefaultsWhenNull() {
        PageRequest request = new PageRequest(null, null, null);
        
        assertThat(request.page()).isEqualTo(0);
        assertThat(request.size()).isEqualTo(20);
    }

    @Test
    @DisplayName("Should create via of factory method")
    void shouldCreateViaOfFactoryMethod() {
        PageRequest request = PageRequest.of(2, 30, "createdAt,desc");
        
        assertThat(request.page()).isEqualTo(2);
        assertThat(request.size()).isEqualTo(30);
        assertThat(request.sort()).isEqualTo("createdAt,desc");
    }

    @Test
    @DisplayName("Should convert to Spring PageRequest without sort")
    void shouldConvertToSpringPageRequestWithoutSort() {
        PageRequest request = new PageRequest(1, 25, null);
        org.springframework.data.domain.PageRequest springRequest = request.toSpringPageRequest();
        
        assertThat(springRequest.getPageNumber()).isEqualTo(1);
        assertThat(springRequest.getPageSize()).isEqualTo(25);
        assertThat(springRequest.getSort()).isEqualTo(Sort.unsorted());
    }

    @Test
    @DisplayName("Should convert to Spring PageRequest with ascending sort")
    void shouldConvertToSpringPageRequestWithAscendingSort() {
        PageRequest request = new PageRequest(0, 10, "name,asc");
        org.springframework.data.domain.PageRequest springRequest = request.toSpringPageRequest();
        
        assertThat(springRequest.getPageNumber()).isEqualTo(0);
        assertThat(springRequest.getPageSize()).isEqualTo(10);
        assertThat(springRequest.getSort().getOrderFor("name")).isNotNull();
        assertThat(springRequest.getSort().getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @DisplayName("Should convert to Spring PageRequest with descending sort")
    void shouldConvertToSpringPageRequestWithDescendingSort() {
        PageRequest request = new PageRequest(0, 10, "createdAt,desc");
        org.springframework.data.domain.PageRequest springRequest = request.toSpringPageRequest();
        
        assertThat(springRequest.getSort().getOrderFor("createdAt")).isNotNull();
        assertThat(springRequest.getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("Should default to ASC when sort direction not specified")
    void shouldDefaultToAscWhenSortDirectionNotSpecified() {
        PageRequest request = new PageRequest(0, 10, "name");
        org.springframework.data.domain.PageRequest springRequest = request.toSpringPageRequest();
        
        assertThat(springRequest.getSort().getOrderFor("name")).isNotNull();
        assertThat(springRequest.getSort().getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @DisplayName("Should handle blank sort string")
    void shouldHandleBlankSortString() {
        PageRequest request = new PageRequest(0, 10, "  ");
        org.springframework.data.domain.PageRequest springRequest = request.toSpringPageRequest();
        
        assertThat(springRequest.getSort()).isEqualTo(Sort.unsorted());
    }

    @Test
    @DisplayName("Should handle empty sort string")
    void shouldHandleEmptySortString() {
        PageRequest request = new PageRequest(0, 10, "");
        org.springframework.data.domain.PageRequest springRequest = request.toSpringPageRequest();
        
        assertThat(springRequest.getSort()).isEqualTo(Sort.unsorted());
    }

    @Test
    @DisplayName("Should be immutable record")
    void shouldBeImmutableRecord() {
        PageRequest request = new PageRequest(1, 20, "name,asc");
        
        // Records are immutable
        assertThat(request.page()).isEqualTo(1);
        assertThat(request.size()).isEqualTo(20);
        assertThat(request.sort()).isEqualTo("name,asc");
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        PageRequest request1 = new PageRequest(1, 20, "name,asc");
        PageRequest request2 = new PageRequest(1, 20, "name,asc");
        PageRequest request3 = new PageRequest(2, 20, "name,asc");
        
        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        assertThat(request1).isNotEqualTo(request3);
    }
}
