package io.jeecloud.aidigitalsignage.user.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/**
 * JPA Entity for the many-to-many relationship between Users and Agents.
 * This is the junction/association table.
 */
@Entity
@Table(name = "user_agent", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_agent_code", columnList = "agent_code")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAgentEntity {
    
    @EmbeddedId
    private UserAgentId id;
    
    /**
     * Composite primary key for UserAgent association.
     */
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserAgentId implements Serializable {
        
        @Column(name = "user_id", nullable = false, length = 50)
        private String userId;
        
        @Column(name = "agent_code", nullable = false, length = 10)
        private String agentCode;
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserAgentId that = (UserAgentId) o;
            return Objects.equals(userId, that.userId) && 
                   Objects.equals(agentCode, that.agentCode);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(userId, agentCode);
        }
    }
}
