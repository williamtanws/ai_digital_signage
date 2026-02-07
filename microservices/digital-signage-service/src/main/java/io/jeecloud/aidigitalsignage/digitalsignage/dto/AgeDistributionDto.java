package io.jeecloud.aidigitalsignage.digitalsignage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Age Distribution DTO
 * 
 * Represents the breakdown of audience by age groups.
 * Age categories follow standard demographic segmentation:
 * - Children: 0-12 years
 * - Teenagers: 13-19 years  * - Young Adults: 20-35 years
 * - Mid-Aged: 36-55 years
 * - Seniors: 56+ years
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgeDistributionDto {

    /**
     * Number of children (0-12 years)
     */
    private Integer children;
    
    /**
     * Number of teenagers (13-19 years)
     */
    private Integer teenagers;
    
    /**
     * Number of young adults (20-35 years)
     */
    private Integer youngAdults;
    
    /**
     * Number of mid-aged adults (36-55 years)
     */
    private Integer midAged;
    
    /**
     * Number of seniors (56+ years)
     */
    private Integer seniors;
}
