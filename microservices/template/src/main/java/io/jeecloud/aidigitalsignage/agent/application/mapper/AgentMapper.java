package io.jeecloud.aidigitalsignage.agent.application.mapper;

import io.jeecloud.aidigitalsignage.agent.application.dto.AgentRequest;
import io.jeecloud.aidigitalsignage.agent.application.dto.AgentResponse;
import io.jeecloud.aidigitalsignage.agent.domain.Agent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * MapStruct mapper for Agent DTO conversions.
 * Provides type-safe, compile-time validated mappings between layers.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AgentMapper {
    
    /**
     * Convert domain Agent to AgentResponse DTO.
     */
    @Mapping(source = "agentCode.value", target = "agentCode")
    AgentResponse toResponse(Agent agent);
    
    /**
     * Note: For request to domain, we use factory methods in the domain
     * to ensure proper validation and business rules are applied.
     */
}

