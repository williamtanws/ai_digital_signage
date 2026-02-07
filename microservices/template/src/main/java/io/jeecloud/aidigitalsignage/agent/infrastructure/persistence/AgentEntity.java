package io.jeecloud.aidigitalsignage.agent.infrastructure.persistence;

import io.jeecloud.aidigitalsignage.agent.domain.Channel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * JPA Entity for Agent persistence.
 * This is part of the infrastructure layer and maps to database table.
 */
@Entity
@Table(name = "agents", indexes = {
    @Index(name = "idx_branch_code", columnList = "branch_code"),
    @Index(name = "idx_channel", columnList = "channel")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentEntity {
    
    @Id
    @Column(name = "agent_code", nullable = false, updatable = false, length = 10)
    private String agentCode;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "branch_code", nullable = false, length = 6)
    private String branchCode;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private Channel channel;
    
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

