package com.event.snappro.SnapPro.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import com.event.snappro.SnapPro.dto.PackageDTO;
import com.event.snappro.SnapPro.dto.ResponseDTO;
import com.event.snappro.SnapPro.service.PackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
public class PackageController {

    private final PackageService packageService;

    @GetMapping
    public ResponseEntity<ResponseDTO> getActivePackages() {
        ResponseDTO response = packageService.getAllActivePackages();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseDTO> getAllPackages() {
        ResponseDTO response = packageService.getAllPackages();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO> getPackageById(@PathVariable Integer id) {
        ResponseDTO response = packageService.getPackageById(id);
        if (!response.isStatus()) {
            return ResponseEntity.status(404).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO> createPackage(@RequestBody PackageDTO packageDTO) {
        ResponseDTO response = packageService.createPackage(packageDTO);
        if (!response.isStatus()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDTO> createPackageMultipart(
            @RequestParam("title") String title,
            @RequestParam("coverageHours") java.math.BigDecimal coverageHours,
            @RequestParam("price") Double price,
            @RequestParam(value = "discountRate", required = false, defaultValue = "0") java.math.BigDecimal discountRate,
            @RequestParam(value = "inclusions", required = false) String inclusions,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "status", required = false, defaultValue = "ACTIVE") String status,
            @RequestParam(value = "images", required = false) java.util.List<org.springframework.web.multipart.MultipartFile> images,
            @RequestParam(value = "image", required = false) java.util.List<org.springframework.web.multipart.MultipartFile> singleImage) {

        PackageDTO dto = new PackageDTO();
        dto.setTitle(title);
        dto.setCoverageHours(coverageHours);
        dto.setPrice(price);
        dto.setDiscountRate(discountRate);
        dto.setInclusions(inclusions);
        dto.setDescription(description);
        dto.setStatus(status);

        org.springframework.web.multipart.MultipartFile fileToSave = null;
        if (singleImage != null && !singleImage.isEmpty()) {
            for (org.springframework.web.multipart.MultipartFile f : singleImage) {
                if (f != null && !f.isEmpty()) { fileToSave = f; break; }
            }
        }
        if (fileToSave == null && images != null && !images.isEmpty()) {
            for (org.springframework.web.multipart.MultipartFile f : images) {
                if (f != null && !f.isEmpty()) { fileToSave = f; break; }
            }
        }

        ResponseDTO response;
        if (fileToSave != null) {
            response = packageService.createPackageWithImage(dto, fileToSave);
        } else {
            response = packageService.createPackage(dto);
        }

        if (!response.isStatus()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(value = "/{id}", consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO> updatePackage(@PathVariable Integer id, @RequestBody PackageDTO packageDTO) {
        ResponseDTO response = packageService.updatePackage(id, packageDTO);
        if (!response.isStatus()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDTO> updatePackageMultipart(
            @PathVariable Integer id,
            @RequestParam("title") String title,
            @RequestParam("coverageHours") java.math.BigDecimal coverageHours,
            @RequestParam("price") Double price,
            @RequestParam(value = "discountRate", required = false, defaultValue = "0") java.math.BigDecimal discountRate,
            @RequestParam(value = "inclusions", required = false) String inclusions,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "status", required = false, defaultValue = "ACTIVE") String status,
            @RequestParam(value = "images", required = false) java.util.List<org.springframework.web.multipart.MultipartFile> images,
            @RequestParam(value = "image", required = false) java.util.List<org.springframework.web.multipart.MultipartFile> singleImage) {

        PackageDTO dto = new PackageDTO();
        dto.setTitle(title);
        dto.setCoverageHours(coverageHours);
        dto.setPrice(price);
        dto.setDiscountRate(discountRate);
        dto.setInclusions(inclusions);
        dto.setDescription(description);
        dto.setStatus(status);

        org.springframework.web.multipart.MultipartFile fileToSave = null;
        if (singleImage != null && !singleImage.isEmpty()) {
            for (org.springframework.web.multipart.MultipartFile f : singleImage) {
                if (f != null && !f.isEmpty()) { fileToSave = f; break; }
            }
        }
        if (fileToSave == null && images != null && !images.isEmpty()) {
            for (org.springframework.web.multipart.MultipartFile f : images) {
                if (f != null && !f.isEmpty()) { fileToSave = f; break; }
            }
        }

        ResponseDTO response;
        if (fileToSave != null) {
            response = packageService.updatePackageWithImage(id, dto, fileToSave);
        } else {
            response = packageService.updatePackage(id, dto);
        }

        if (!response.isStatus()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{id}/image", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDTO> uploadImage(
            @PathVariable Integer id,
            @RequestParam(value = "images", required = false) java.util.List<org.springframework.web.multipart.MultipartFile> images,
            @RequestParam(value = "image", required = false) java.util.List<org.springframework.web.multipart.MultipartFile> singleImage) {
        org.springframework.web.multipart.MultipartFile fileToSave = null;
        if (singleImage != null && !singleImage.isEmpty()) {
            for (org.springframework.web.multipart.MultipartFile f : singleImage) {
                if (f != null && !f.isEmpty()) { fileToSave = f; break; }
            }
        }
        if (fileToSave == null && images != null && !images.isEmpty()) {
            for (org.springframework.web.multipart.MultipartFile f : images) {
                if (f != null && !f.isEmpty()) { fileToSave = f; break; }
            }
        }

        if (fileToSave == null) {
            return ResponseEntity.badRequest().body(new ResponseDTO(false, "Please select an image file to upload.", null));
        }

        ResponseDTO response = packageService.uploadPackageImage(id, fileToSave);
        if (!response.isStatus()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping({"/{id}/images", "/{id}/image"})
    public ResponseEntity<ResponseDTO> deleteImage(
            @PathVariable Integer id,
            @RequestParam(value = "imageUrl", required = false) String imageUrl,
            @RequestParam(value = "imageName", required = false) String imageName) {
        String target = imageUrl != null ? imageUrl : imageName;
        if (target == null || target.trim().isEmpty()) {
            ResponseDTO response = packageService.deleteAllPackageImages(id);
            return ResponseEntity.ok(response);
        }
        ResponseDTO response = packageService.deletePackageImage(id, target);
        if (!response.isStatus()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO> deletePackage(@PathVariable Integer id) {
        ResponseDTO response = packageService.deletePackage(id);
        if (!response.isStatus()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

}
