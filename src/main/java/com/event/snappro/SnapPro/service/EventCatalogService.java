package com.event.snappro.SnapPro.service;

import com.event.snappro.SnapPro.dto.CatalogRequestDTO;
import com.event.snappro.SnapPro.dto.CatalogResponseDTO;
import com.event.snappro.SnapPro.dto.CatalogUpdateDTO;

import java.util.List;
import java.util.Map;
import org.springframework.core.io.Resource;

public interface EventCatalogService {

    CatalogResponseDTO createCatalog(CatalogRequestDTO request);

    List<CatalogResponseDTO> getAllCatalogs();

    List<CatalogResponseDTO> getCatalogsByBooking(Integer bookingId);

    CatalogResponseDTO getCatalogById(Integer id);

    CatalogResponseDTO updateCatalog(Integer id, CatalogUpdateDTO request);

    void deleteCatalog(Integer id);

    // Customer's own completed-booking albums; no new DTO is necessary.
    List<Map<String, Object>> getCustomerGalleryAlbums(Integer customerId);

    List<Map<String, Object>> getCustomerGalleryPhotos(Integer customerId, Integer albumId);

    Resource getCustomerGalleryPhotoContent(Integer customerId, Integer photoId);

    // Original photo filename (only after customer/Completed booking authorization).
    String getCustomerGalleryPhotoDownloadName(Integer customerId, Integer photoId);
}
