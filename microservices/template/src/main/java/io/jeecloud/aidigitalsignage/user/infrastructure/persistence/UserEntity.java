package io.jeecloud.aidigitalsignage.user.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * JPA Entity for User persistence.
 * This is part of the infrastructure layer and maps to database table.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_new_nric", columnList = "new_nric", unique = true),
    @Index(name = "idx_email", columnList = "email")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    
    @Id
    @Column(name = "user_id", nullable = false, updatable = false, length = 50)
    private String userId;
    
    @Column(name = "new_nric", nullable = false, unique = true, length = 20)
    private String newNric;
    
    @Column(name = "email", nullable = false, length = 100)
    private String email;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "status", nullable = false)
    private boolean status;
    
    @Column(name = "create_dt", nullable = false, updatable = false)
    private Instant createDt;
    
    @Column(name = "update_dt", nullable = false)
    private Instant updateDt;
    
    @PrePersist
    protected void onCreate() {
        if (createDt == null) {
            createDt = Instant.now();
        }
        if (updateDt == null) {
            updateDt = Instant.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateDt = Instant.now();
    }
}
