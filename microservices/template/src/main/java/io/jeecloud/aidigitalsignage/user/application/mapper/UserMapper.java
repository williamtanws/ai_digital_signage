package io.jeecloud.aidigitalsignage.user.application.mapper;

import io.jeecloud.aidigitalsignage.user.application.dto.UserRequest;
import io.jeecloud.aidigitalsignage.user.application.dto.UserResponse;
import io.jeecloud.aidigitalsignage.user.domain.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between User domain entities and DTOs.
 * Part of the Application layer.
 */
@Component
public class UserMapper {
    
    /**
     * Convert User domain entity to UserResponse DTO.
     */
    public UserResponse toResponse(User user) {
        return UserResponse.from(user);
    }
    
    /**
     * Convert a list of User domain entities to UserResponse DTOs.
     */
    public List<UserResponse> toResponseList(List<User> users) {
        return users.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
}
