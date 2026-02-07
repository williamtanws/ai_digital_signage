package io.jeecloud.aidigitalsignage.common.infrastructure.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class RedisCacheAdapterTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RedisConnectionFactory connectionFactory;

    @Mock
    private RedisConnection connection;

    private ObjectMapper objectMapper;
    private RedisCacheAdapter cacheAdapter;

    // Test data class
    private record TestData(String id, String name) {}

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        cacheAdapter = new RedisCacheAdapter(redisTemplate, objectMapper);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldPutValueWithDefaultTTL() throws Exception {
        // Given
        String key = "test:key";
        TestData value = new TestData("123", "Test");
        String jsonValue = objectMapper.writeValueAsString(value);

        // When
        cacheAdapter.put(key, value);

        // Then
        verify(valueOperations).set(eq(key), eq(jsonValue), eq(Duration.ofSeconds(3600)));
    }

    @Test
    void shouldPutValueWithCustomTTL() throws Exception {
        // Given
        String key = "test:key";
        TestData value = new TestData("123", "Test");
        int customTTL = 600;
        String jsonValue = objectMapper.writeValueAsString(value);

        // When
        cacheAdapter.put(key, value, customTTL);

        // Then
        verify(valueOperations).set(eq(key), eq(jsonValue), eq(Duration.ofSeconds(customTTL)));
    }

    @Test
    void shouldNotPutNullKey() {
        // Given
        TestData value = new TestData("123", "Test");

        // When
        cacheAdapter.put(null, value);

        // Then
        verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void shouldNotPutNullValue() {
        // Given
        String key = "test:key";

        // When
        cacheAdapter.put(key, null);

        // Then
        verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void shouldHandleSerializationErrorGracefully() throws Exception {
        // Given
        String key = "test:key";
        ObjectMapper failingMapper = mock(ObjectMapper.class);
        when(failingMapper.writeValueAsString(any())).thenThrow(new RuntimeException("Serialization failed"));
        RedisCacheAdapter adapter = new RedisCacheAdapter(redisTemplate, failingMapper);
        TestData value = new TestData("123", "Test");

        // When
        adapter.put(key, value);

        // Then
        verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void shouldGetValueFromCache() throws Exception {
        // Given
        String key = "test:key";
        TestData expectedValue = new TestData("123", "Test");
        String jsonValue = objectMapper.writeValueAsString(expectedValue);
        
        when(valueOperations.get(key)).thenReturn(jsonValue);

        // When
        Optional<TestData> result = cacheAdapter.get(key, TestData.class);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo("123");
        assertThat(result.get().name()).isEqualTo("Test");
        verify(valueOperations).get(key);
    }

    @Test
    void shouldReturnEmptyWhenCacheMiss() {
        // Given
        String key = "test:key";
        when(valueOperations.get(key)).thenReturn(null);

        // When
        Optional<TestData> result = cacheAdapter.get(key, TestData.class);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenKeyIsNull() {
        // When
        Optional<TestData> result = cacheAdapter.get(null, TestData.class);

        // Then
        assertThat(result).isEmpty();
        verify(valueOperations, never()).get(anyString());
    }

    @Test
    void shouldReturnEmptyWhenDeserializationFails() {
        // Given
        String key = "test:key";
        when(valueOperations.get(key)).thenReturn("invalid-json");

        // When
        Optional<TestData> result = cacheAdapter.get(key, TestData.class);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldEvictKey() {
        // Given
        String key = "test:key";
        when(redisTemplate.delete(key)).thenReturn(true);

        // When
        cacheAdapter.evict(key);

        // Then
        verify(redisTemplate).delete(key);
    }

    @Test
    void shouldHandleEvictWhenKeyDoesNotExist() {
        // Given
        String key = "test:key";
        when(redisTemplate.delete(key)).thenReturn(false);

        // When
        cacheAdapter.evict(key);

        // Then
        verify(redisTemplate).delete(key);
    }

    @Test
    void shouldNotEvictNullKey() {
        // When
        cacheAdapter.evict(null);

        // Then
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void shouldHandleEvictErrorGracefully() {
        // Given
        String key = "test:key";
        when(redisTemplate.delete(key)).thenThrow(new RuntimeException("Redis error"));

        // When
        cacheAdapter.evict(key);

        // Then - no exception thrown
        verify(redisTemplate).delete(key);
    }

    @Test
    void shouldClearAllCache() {
        // Given
        when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.getConnection()).thenReturn(connection);

        // When
        cacheAdapter.clear();

        // Then
        verify(connection).flushDb();
    }

    @Test
    void shouldHandleClearErrorGracefully() {
        // Given
        when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.getConnection()).thenThrow(new RuntimeException("Connection failed"));

        // When
        cacheAdapter.clear();

        // Then - no exception thrown
        verify(connectionFactory).getConnection();
    }

    @Test
    void shouldCheckIfKeyExists() {
        // Given
        String key = "test:key";
        when(redisTemplate.hasKey(key)).thenReturn(true);

        // When
        boolean exists = cacheAdapter.exists(key);

        // Then
        assertThat(exists).isTrue();
        verify(redisTemplate).hasKey(key);
    }

    @Test
    void shouldReturnFalseWhenKeyDoesNotExist() {
        // Given
        String key = "test:key";
        when(redisTemplate.hasKey(key)).thenReturn(false);

        // When
        boolean exists = cacheAdapter.exists(key);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void shouldHandleExistsErrorGracefully() {
        // Given
        String key = "test:key";
        when(redisTemplate.hasKey(key)).thenThrow(new RuntimeException("Redis error"));

        // When
        boolean exists = cacheAdapter.exists(key);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void shouldGetTTL() {
        // Given
        String key = "test:key";
        when(redisTemplate.getExpire(key)).thenReturn(300L);

        // When
        long ttl = cacheAdapter.getTTL(key);

        // Then
        assertThat(ttl).isEqualTo(300L);
        verify(redisTemplate).getExpire(key);
    }

    @Test
    void shouldReturnNegativeOneWhenKeyDoesNotExist() {
        // Given
        String key = "test:key";
        when(redisTemplate.getExpire(key)).thenReturn(null);

        // When
        long ttl = cacheAdapter.getTTL(key);

        // Then
        assertThat(ttl).isEqualTo(-1);
    }

    @Test
    void shouldHandleGetTTLErrorGracefully() {
        // Given
        String key = "test:key";
        when(redisTemplate.getExpire(key)).thenThrow(new RuntimeException("Redis error"));

        // When
        long ttl = cacheAdapter.getTTL(key);

        // Then
        assertThat(ttl).isEqualTo(-1);
    }

    @Test
    void shouldCacheComplexObject() throws Exception {
        // Given
        String key = "complex:key";
        ComplexData value = new ComplexData("123", 42, true, new String[]{"tag1", "tag2"});
        String jsonValue = objectMapper.writeValueAsString(value);
        
        when(valueOperations.get(key)).thenReturn(jsonValue);

        // First put
        cacheAdapter.put(key, value, 300);
        verify(valueOperations).set(eq(key), eq(jsonValue), eq(Duration.ofSeconds(300)));

        // Then get
        Optional<ComplexData> result = cacheAdapter.get(key, ComplexData.class);
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo("123");
        assertThat(result.get().count()).isEqualTo(42);
        assertThat(result.get().active()).isTrue();
        assertThat(result.get().tags()).containsExactly("tag1", "tag2");
    }

    // Static nested class for testing complex objects
    static record ComplexData(String id, int count, boolean active, String[] tags) {}
}
