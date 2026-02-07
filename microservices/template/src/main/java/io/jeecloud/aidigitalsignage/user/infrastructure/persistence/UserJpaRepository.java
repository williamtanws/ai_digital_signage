package io.jeecloud.aidigitalsignage.user.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for UserEntity.
 * This provides the actual database operations.
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, String> {
    
    Optional<UserEntity> findByNewNric(String newNric);
    
    Optional<UserEntity> findByEmail(String email);
    
    @Query("SELECT u FROM UserEntity u WHERE u.status = true")
    List<UserEntity> findAllActive();
    
    boolean existsByNewNric(String newNric);
}
