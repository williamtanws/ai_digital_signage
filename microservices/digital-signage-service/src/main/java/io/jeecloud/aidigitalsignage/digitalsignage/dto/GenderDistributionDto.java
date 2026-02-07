package io.jeecloud.aidigitalsignage.digitalsignage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Gender Distribution DTO
 * 
 * Represents the breakdown of audience by gender.
 * Binary classification for simplicity in this prototype.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenderDistributionDto {

    /**
     * Number of male audience members
     */
    private Integer male;
    
    /**
     * Number of female audience members
     */
    private Integer female;
}
