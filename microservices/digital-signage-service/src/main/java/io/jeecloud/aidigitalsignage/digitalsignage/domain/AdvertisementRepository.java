package io.jeecloud.aidigitalsignage.digitalsignage.domain;

import java.util.List;

/**
 * Advertisement Repository Port (Domain Interface)
 * 
 * Defines contract for accessing advertisements.
 * Implementation in infrastructure layer.
 */
public interface AdvertisementRepository {

    /**
     * Find all advertisements ordered by total viewers descending
     */
    List<Advertisement> findAllOrderedByViewers();
    
    /**
     * Save all advertisements
     */
    List<Advertisement> saveAll(List<Advertisement> advertisements);
    
    /**
     * Delete all advertisements
     */
    void deleteAll();
}
