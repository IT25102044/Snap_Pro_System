package com.event.snappro.SnapPro.controller;

import com.event.snappro.SnapPro.dto.CatalogRequestDTO;
import com.event.snappro.SnapPro.dto.CatalogResponseDTO;
import com.event.snappro.SnapPro.dto.CatalogUpdateDTO;
import com.event.snappro.SnapPro.service.EventCatalogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ContentDisposition;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
public class EventCatalogController {

    private final EventCatalogService eventCatalogService;


    public EventCatalogController(EventCatalogService eventCatalogService) {
        this.eventCatalogService = eventCatalogService;
    }


    // =====================================================
    // POST /api/catalogs - CREATE CATALOG MANUALLY
    // =====================================================

    @PostMapping("/api/catalogs")
    public ResponseEntity<?> createCatalog(
            @Valid @RequestBody CatalogRequestDTO request
    ) {
        try {
            CatalogResponseDTO response = eventCatalogService.createCatalog(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Failed to create catalog: " + e.getMessage()));
        }
    }


    // =====================================================
    // GET /api/catalogs - GET ALL CATALOGS
    // =====================================================

    @GetMapping("/api/catalogs")
    public ResponseEntity<List<CatalogResponseDTO>> getAllCatalogs() {
        return ResponseEntity.ok(eventCatalogService.getAllCatalogs());
    }


    // =====================================================
    // GET /api/catalogs/booking/{bookingId}
    // =====================================================

    @GetMapping("/api/catalogs/booking/{bookingId}")
    public ResponseEntity<List<CatalogResponseDTO>> getCatalogsByBooking(
            @PathVariable Integer bookingId
    ) {
        return ResponseEntity.ok(eventCatalogService.getCatalogsByBooking(bookingId));
    }


    // =====================================================
    // GET /api/catalogs/{id}
    // =====================================================

    @GetMapping("/api/catalogs/{id}")
    public ResponseEntity<?> getCatalogById(
            @PathVariable Integer id
    ) {
        try {
            return ResponseEntity.ok(eventCatalogService.getCatalogById(id));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }


    // =====================================================
    // PUT /api/catalogs/{id} - EDIT ALBUM NAME / DESCRIPTION
    // =====================================================

    @PutMapping("/api/catalogs/{id}")
    public ResponseEntity<?> updateCatalog(
            @PathVariable Integer id,
            @Valid @RequestBody CatalogUpdateDTO request
    ) {
        try {
            return ResponseEntity.ok(eventCatalogService.updateCatalog(id, request));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Failed to update album."));
        }
    }


    // =====================================================
    // DELETE /api/catalogs/{id} - HARD CASCADE DELETE
    // =====================================================

    @DeleteMapping("/api/catalogs/{id}")
    public ResponseEntity<?> deleteCatalog(
            @PathVariable Integer id
    ) {
        try {
            eventCatalogService.deleteCatalog(id);
            return ResponseEntity.ok(Map.of("message", "Catalog and all associated photos deleted successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    // =====================================================
    // CUSTOMER EVENT PHOTOS (READ ONLY)
    // Reuses the existing album service and checks the customer's
    // active session, booking ownership and Completed status.
    // =====================================================

    @GetMapping("/api/customer/gallery/albums")
    public List<Map<String, Object>> getCustomerGalleryAlbums(HttpServletRequest request) {
        return eventCatalogService.getCustomerGalleryAlbums(requireCustomerId(request));
    }

    @GetMapping("/api/customer/gallery/albums/{albumId}/photos")
    public List<Map<String, Object>> getCustomerGalleryPhotos(
            @PathVariable Integer albumId, HttpServletRequest request) {
        return eventCatalogService.getCustomerGalleryPhotos(requireCustomerId(request), albumId);
    }

    @GetMapping("/api/customer/gallery/photos/{photoId}/content")
    public ResponseEntity<Resource> getCustomerGalleryPhotoContent(
            @PathVariable Integer photoId, HttpServletRequest request) {
        Resource image = eventCatalogService.getCustomerGalleryPhotoContent(requireCustomerId(request), photoId);
        String name = image.getFilename() == null ? "" : image.getFilename().toLowerCase();
        MediaType mediaType = name.endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok()
                .contentType(mediaType)
                .cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .body(image);
    }

    // GET /api/customer/gallery/photos/{photoId}/download
    // Private attachment: only the customer with a Completed booking can download it.
    @GetMapping("/api/customer/gallery/photos/{photoId}/download")
    public ResponseEntity<Resource> downloadCustomerGalleryPhoto(
            @PathVariable Integer photoId, HttpServletRequest request) {
        Integer customerId = requireCustomerId(request);
        Resource photo = eventCatalogService.getCustomerGalleryPhotoContent(customerId, photoId);
        String originalName = eventCatalogService.getCustomerGalleryPhotoDownloadName(customerId, photoId);
        String filename = safeDownloadName(originalName, photo.getFilename());
        String lowerName = photo.getFilename() == null ? "" : photo.getFilename().toLowerCase();
        MediaType type = lowerName.endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok()
                .contentType(type)
                .cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString())
                .body(photo);
    }

    // GET /api/customer/gallery/albums/{albumId}/download
    // ZIP is streamed directly to the browser; no ZIP is stored on the server.
    @GetMapping("/api/customer/gallery/albums/{albumId}/download")
    public ResponseEntity<StreamingResponseBody> downloadCustomerGalleryAlbum(
            @PathVariable Integer albumId, HttpServletRequest request) {
        Integer customerId = requireCustomerId(request);
        // This method rejects albums that aren't owned by this customer or aren't Completed.
        List<Map<String, Object>> photos = eventCatalogService.getCustomerGalleryPhotos(customerId, albumId);
        String albumName = eventCatalogService.getCatalogById(albumId).getCatalogName();
        String archiveName = safeDownloadName(albumName, "Event_Album") + ".zip";

        // Validate each file and its access BEFORE starting the HTTP stream.
        List<Resource> sources = new ArrayList<>();
        List<String> entryNames = new ArrayList<>();
        Set<String> usedNames = new HashSet<>();
        for (Map<String, Object> item : photos) {
            Integer photoId = (Integer) item.get("id");
            Resource file = eventCatalogService.getCustomerGalleryPhotoContent(customerId, photoId);
            String requestedName = (String) item.get("fileName");
            String safeName = safeDownloadName(requestedName, file.getFilename());
            String uniqueName = safeName;
            int suffix = 2;
            while (!usedNames.add(uniqueName.toLowerCase(java.util.Locale.ROOT))) {
                int dot = safeName.lastIndexOf('.');
                uniqueName = dot > 0
                        ? safeName.substring(0, dot) + " (" + suffix++ + ")" + safeName.substring(dot)
                        : safeName + " (" + suffix++ + ")";
            }
            sources.add(file);
            entryNames.add(uniqueName);
        }

        StreamingResponseBody body = output -> {
            try (ZipOutputStream zip = new ZipOutputStream(output)) {
                for (int i = 0; i < sources.size(); i++) {
                    zip.putNextEntry(new ZipEntry(entryNames.get(i)));
                    try (InputStream input = sources.get(i).getInputStream()) {
                        input.transferTo(zip);
                    }
                    zip.closeEntry();
                }
                zip.finish();
            }
        };
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(archiveName, StandardCharsets.UTF_8).build().toString())
                .body(body);
    }

    // Prevent directory traversal and unsafe characters in ZIP entries / download names.
    private String safeDownloadName(String original, String fallback) {
        String name = original == null ? "" : original.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1);
        name = name.replaceAll("[^\\p{L}\\p{N} ._()-]", "_").trim();
        while (name.startsWith(".")) name = name.substring(1);
        if (name.isBlank()) {
            name = fallback == null ? "photo" : fallback.replaceAll("[^a-zA-Z0-9._-]", "_");
        }
        if (name.length() > 180) name = name.substring(0, 180);
        return name;
    }

    private Integer requireCustomerId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("userId") instanceof Integer userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in.");
        }
        Object role = session.getAttribute("userRole");
        String roleName = role == null ? "" : role.toString().trim();
        if (!"CUSTOMER".equalsIgnoreCase(roleName) && !"CLIENT".equalsIgnoreCase(roleName)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer access only.");
        }
        return userId;
    }
}
