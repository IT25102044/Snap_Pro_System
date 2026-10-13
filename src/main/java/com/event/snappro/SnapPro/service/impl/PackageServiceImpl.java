package com.event.snappro.SnapPro.service.impl;

import com.event.snappro.SnapPro.entity.PackageStatusEntity;
import com.event.snappro.SnapPro.entity.UserEntity;
import com.event.snappro.SnapPro.repository.PackageStatusRepository;
import com.event.snappro.SnapPro.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.event.snappro.SnapPro.dto.PackageDTO;
import com.event.snappro.SnapPro.dto.ResponseDTO;
import com.event.snappro.SnapPro.entity.PackageEntity;
import com.event.snappro.SnapPro.repository.PackageRepository;
import com.event.snappro.SnapPro.service.PackageService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PackageServiceImpl implements PackageService {

    private final PackageRepository packageRepository;
    private final PackageStatusRepository packageStatusRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public ResponseDTO getAllActivePackages() {
        List<PackageEntity> packages = packageRepository.findByPackageStatus_StatusIgnoreCase("ACTIVE");

        // If no specifically marked "ACTIVE" status exists, fetch all packages
        if (packages.isEmpty()) {
            packages = packageRepository.findAll();
        }

        List<PackageDTO> dtoList = packages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new ResponseDTO(true, "Active packages retrieved successfully.", dtoList);
    }

    @Override
    public ResponseDTO getAllPackages() {

        PackageHelperSingleton helper =
                PackageHelperSingleton.getInstance();

        System.out.println(helper.getMessage());

        List<PackageEntity> packages = packageRepository.findAll();
        List<PackageDTO> dtoList = packages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new ResponseDTO(true, "All packages retrieved successfully.", dtoList);
    }

    @Override
    public ResponseDTO getPackageById(Integer id) {
        Optional<PackageEntity> packageOptional = packageRepository.findById(id);
        if (packageOptional.isEmpty()) {
            return new ResponseDTO(false, "Package not found with ID: " + id, null);
        }

        PackageDTO dto = convertToDTO(packageOptional.get());
        return new ResponseDTO(true, "Package found.", dto);
    }
    @Override
    public ResponseDTO createPackage(PackageDTO dto) {
        if (dto == null || dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            return new ResponseDTO(false, "Package title is required.", null);
        }

        PackageStatusEntity status = resolvePackageStatus(dto.getStatus());
        UserEntity creator = resolveCreator(dto.getCreatedById());

        PackageEntity entity = new PackageEntity();
        entity.setTitle(dto.getTitle().trim());
        entity.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : "");
        entity.setCoverageHours(dto.getCoverageHours() != null ? dto.getCoverageHours() : BigDecimal.valueOf(4.0));
        entity.setInclusions(formatInclusionsForStorage(dto.getInclusions()));
        entity.setPrice(dto.getPrice() != null ? dto.getPrice() : 0.0);
        entity.setDiscountRate(dto.getDiscountRate() != null ? dto.getDiscountRate() : BigDecimal.ZERO);
        entity.setPackageStatus(status);
        entity.setCreatedBy(creator);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        PackageEntity saved = packageRepository.save(entity);
        return new ResponseDTO(true, "Package created successfully!", convertToDTO(saved));
    }

    @Override
    public ResponseDTO updatePackage(Integer id, PackageDTO dto) {
        Optional<PackageEntity> packageOptional = packageRepository.findById(id);
        if (packageOptional.isEmpty()) {
            return new ResponseDTO(false, "Package not found with ID: " + id, null);
        }

        PackageEntity entity = packageOptional.get();

        if (dto.getTitle() != null && !dto.getTitle().trim().isEmpty()) {
            entity.setTitle(dto.getTitle().trim());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription().trim());
        }
        if (dto.getCoverageHours() != null) {
            entity.setCoverageHours(dto.getCoverageHours());
        }
        if (dto.getInclusions() != null) {
            entity.setInclusions(formatInclusionsForStorage(dto.getInclusions()));
        }
        if (dto.getPrice() != null) {
            entity.setPrice(dto.getPrice());
        }
        if (dto.getDiscountRate() != null) {
            entity.setDiscountRate(dto.getDiscountRate());
        }
        if (dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) {
            PackageStatusEntity status = resolvePackageStatus(dto.getStatus());
            entity.setPackageStatus(status);
        }
        entity.setUpdatedAt(LocalDateTime.now());

        PackageEntity saved = packageRepository.save(entity);
        return new ResponseDTO(true, "Package updated successfully!", convertToDTO(saved));
    }

    @Override
    public ResponseDTO deletePackage(Integer id) {
        Optional<PackageEntity> packageOptional = packageRepository.findById(id);
        if (packageOptional.isEmpty()) {
            return new ResponseDTO(false, "Package not found with ID: " + id, null);
        }

        try {
            packageRepository.deleteById(id);
            return new ResponseDTO(true, "Package deleted successfully.", null);
        } catch (Exception ex) {
            // Soft-archive if referenced in bookings
            PackageStatusEntity archivedStatus = packageStatusRepository.findByStatusIgnoreCase("ARCHIVED")
                    .orElseGet(() -> packageStatusRepository.findByStatusIgnoreCase("DISCONTINUED")
                            .orElseGet(() -> {
                                PackageStatusEntity s = new PackageStatusEntity();
                                s.setStatus("ARCHIVED");
                                return packageStatusRepository.save(s);
                            }));
            PackageEntity entity = packageOptional.get();
            entity.setPackageStatus(archivedStatus);
            entity.setUpdatedAt(LocalDateTime.now());
            packageRepository.save(entity);
            return new ResponseDTO(true, "Package has historical bookings and was archived instead of deleted.", null);
        }
    }

    @Override
    public ResponseDTO createPackageWithImage(PackageDTO dto, org.springframework.web.multipart.MultipartFile image) {
        java.util.List<org.springframework.web.multipart.MultipartFile> images = image != null ? java.util.Collections.singletonList(image) : java.util.Collections.emptyList();
        return createPackageWithImages(dto, images);
    }

    @Override
    public ResponseDTO createPackageWithImages(PackageDTO dto, java.util.List<org.springframework.web.multipart.MultipartFile> images) {
        ResponseDTO createResponse = createPackage(dto);
        if (!createResponse.isStatus() || createResponse.getData() == null) {
            return createResponse;
        }

        PackageDTO createdDto = (PackageDTO) createResponse.getData();
        if (images != null && !images.isEmpty()) {
            java.util.List<String> imageUrls = savePackageImagesOnDisk(createdDto.getId(), images);
            createdDto.setImageUrls(imageUrls);
            if (!imageUrls.isEmpty()) {
                createdDto.setImageUrl(imageUrls.get(0));
            }
        }

        return new ResponseDTO(true, "Package and image saved successfully!", createdDto);
    }

    @Override
    public ResponseDTO updatePackageWithImage(Integer id, PackageDTO dto, org.springframework.web.multipart.MultipartFile image) {
        java.util.List<org.springframework.web.multipart.MultipartFile> images = image != null ? java.util.Collections.singletonList(image) : java.util.Collections.emptyList();
        return updatePackageWithImages(id, dto, images);
    }

    @Override
    public ResponseDTO updatePackageWithImages(Integer id, PackageDTO dto, java.util.List<org.springframework.web.multipart.MultipartFile> images) {
        ResponseDTO updateResponse = updatePackage(id, dto);
        if (!updateResponse.isStatus() || updateResponse.getData() == null) {
            return updateResponse;
        }

        PackageDTO updatedDto = (PackageDTO) updateResponse.getData();
        if (images != null && !images.isEmpty() && images.stream().anyMatch(img -> img != null && !img.isEmpty())) {
            java.util.List<String> imageUrls = savePackageImagesOnDisk(id, images);
            updatedDto.setImageUrls(imageUrls);
            if (!imageUrls.isEmpty()) {
                updatedDto.setImageUrl(imageUrls.get(0));
            }
        } else {
            java.util.List<String> existingUrls = findPackageImageUrls(id);
            updatedDto.setImageUrls(existingUrls);
            if (!existingUrls.isEmpty()) {
                updatedDto.setImageUrl(existingUrls.get(0));
            }
        }

        return new ResponseDTO(true, "Package updated successfully!", updatedDto);
    }

    @Override
    public ResponseDTO uploadPackageImage(Integer id, org.springframework.web.multipart.MultipartFile image) {
        java.util.List<org.springframework.web.multipart.MultipartFile> images = image != null ? java.util.Collections.singletonList(image) : java.util.Collections.emptyList();
        return uploadPackageImages(id, images);
    }

    @Override
    public ResponseDTO uploadPackageImages(Integer id, java.util.List<org.springframework.web.multipart.MultipartFile> images) {
        Optional<PackageEntity> packageOptional = packageRepository.findById(id);
        if (packageOptional.isEmpty()) {
            return new ResponseDTO(false, "Package not found with ID: " + id, null);
        }

        if (images == null || images.isEmpty() || images.stream().allMatch(img -> img == null || img.isEmpty())) {
            return new ResponseDTO(false, "Please select an image file to upload.", null);
        }

        java.util.List<String> imageUrls = savePackageImagesOnDisk(id, images);
        PackageDTO dto = convertToDTO(packageOptional.get());
        dto.setImageUrls(imageUrls);
        if (!imageUrls.isEmpty()) {
            dto.setImageUrl(imageUrls.get(0));
        }

        return new ResponseDTO(true, "Package image uploaded successfully!", dto);
    }

    @Override
    public ResponseDTO deletePackageImage(Integer packageId, String imageNameOrUrl) {
        if (packageId == null || imageNameOrUrl == null || imageNameOrUrl.trim().isEmpty()) {
            return new ResponseDTO(false, "Package ID and image path are required.", null);
        }

        String filename = imageNameOrUrl.trim();
        if (filename.contains("/")) {
            filename = filename.substring(filename.lastIndexOf("/") + 1);
        }
        if (filename.contains("\\")) {
            filename = filename.substring(filename.lastIndexOf("\\") + 1);
        }

        // Security / safety check: Ensure filename belongs to this packageId
        if (!filename.startsWith("package_" + packageId + "_") && !filename.startsWith("package_" + packageId + ".")) {
            return new ResponseDTO(false, "Image does not belong to package #" + packageId, null);
        }

        java.nio.file.Path srcFile = java.nio.file.Paths.get("src", "main", "resources", "static", "images", filename);
        java.nio.file.Path targetFile = java.nio.file.Paths.get("target", "classes", "static", "images", filename);

        try {
            java.nio.file.Files.deleteIfExists(srcFile);
            java.nio.file.Files.deleteIfExists(targetFile);
        } catch (Exception e) {
            return new ResponseDTO(false, "Failed to delete image: " + e.getMessage(), null);
        }

        return getPackageById(packageId);
    }

    @Override
    public ResponseDTO deleteAllPackageImages(Integer packageId) {
        if (packageId == null) {
            return new ResponseDTO(false, "Package ID is required.", null);
        }

        java.nio.file.Path srcDir = java.nio.file.Paths.get("src", "main", "resources", "static", "images");
        java.nio.file.Path targetDir = java.nio.file.Paths.get("target", "classes", "static", "images");

        cleanOldPackageImages(srcDir, packageId);
        cleanOldPackageImages(targetDir, packageId);

        return getPackageById(packageId);
    }

    private java.util.List<String> savePackageImagesOnDisk(Integer packageId, java.util.List<org.springframework.web.multipart.MultipartFile> files) {
        return savePackageImagesOnDisk(packageId, files, false);
    }

    private java.util.List<String> savePackageImagesOnDisk(Integer packageId, java.util.List<org.springframework.web.multipart.MultipartFile> files, boolean append) {
        java.util.List<String> savedUrls = new java.util.ArrayList<>();
        if (packageId == null || files == null) return savedUrls;

        java.util.List<org.springframework.web.multipart.MultipartFile> validFiles = files.stream()
                .filter(f -> f != null && !f.isEmpty())
                .collect(Collectors.toList());

        if (validFiles.isEmpty()) return savedUrls;

        // Ensure only one image per package: retain only the first valid file
        if (validFiles.size() > 1) {
            validFiles = validFiles.subList(0, 1);
        }

        try {
            java.nio.file.Path srcDir = java.nio.file.Paths.get("src", "main", "resources", "static", "images");
            if (!java.nio.file.Files.exists(srcDir)) {
                java.nio.file.Files.createDirectories(srcDir);
            }

            java.nio.file.Path targetDir = java.nio.file.Paths.get("target", "classes", "static", "images");
            try {
                if (!java.nio.file.Files.exists(targetDir)) {
                    java.nio.file.Files.createDirectories(targetDir);
                }
            } catch (Exception ignored) {}

            // Always clean any existing package images so only a single image exists for the package
            cleanOldPackageImages(srcDir, packageId);
            cleanOldPackageImages(targetDir, packageId);

            int index = 1;
            for (org.springframework.web.multipart.MultipartFile file : validFiles) {
                String originalFilename = file.getOriginalFilename();
                String extension = "jpg";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
                }

                String targetFilename = "package_" + packageId + "_" + index + "." + extension;
                java.nio.file.Path srcFile = srcDir.resolve(targetFilename);
                java.nio.file.Files.copy(file.getInputStream(), srcFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                try {
                    java.nio.file.Path targetFile = targetDir.resolve(targetFilename);
                    java.nio.file.Files.copy(srcFile, targetFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception ignored) {}

                savedUrls.add("/images/" + targetFilename);
                index++;
            }

            return findPackageImageUrls(packageId);

        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to save package image to disk: " + e.getMessage(), e);
        }
    }

    private int getNextImageIndex(java.nio.file.Path dir, Integer packageId) {
        int maxIndex = 0;
        if (dir == null || !java.nio.file.Files.exists(dir)) return 1;

        try (java.nio.file.DirectoryStream<java.nio.file.Path> stream = java.nio.file.Files.newDirectoryStream(dir)) {
            String prefix = "package_" + packageId + "_";
            for (java.nio.file.Path entry : stream) {
                String name = entry.getFileName().toString();
                if (name.startsWith(prefix)) {
                    try {
                        String idxPart = name.substring(prefix.length());
                        if (idxPart.contains(".")) {
                            idxPart = idxPart.substring(0, idxPart.indexOf("."));
                        }
                        int val = Integer.parseInt(idxPart);
                        if (val > maxIndex) maxIndex = val;
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception ignored) {}

        return maxIndex + 1;
    }

    private void cleanOldPackageImages(java.nio.file.Path dir, Integer packageId) {
        if (dir == null || !java.nio.file.Files.exists(dir)) return;
        try (java.nio.file.DirectoryStream<java.nio.file.Path> stream = java.nio.file.Files.newDirectoryStream(dir)) {
            String prefix1 = "package_" + packageId + "_";
            String prefix2 = "package_" + packageId + ".";
            for (java.nio.file.Path entry : stream) {
                String name = entry.getFileName().toString();
                if (name.startsWith(prefix1) || name.startsWith(prefix2)) {
                    try {
                        java.nio.file.Files.deleteIfExists(entry);
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception ignored) {}
    }

    private java.util.List<String> findPackageImageUrls(Integer packageId) {
        java.util.List<String> result = new java.util.ArrayList<>();
        if (packageId == null) return result;

        java.nio.file.Path dir = java.nio.file.Paths.get("src", "main", "resources", "static", "images");
        if (!java.nio.file.Files.exists(dir)) {
            dir = java.nio.file.Paths.get("target", "classes", "static", "images");
        }
        if (!java.nio.file.Files.exists(dir)) {
            return result;
        }

        try (java.nio.file.DirectoryStream<java.nio.file.Path> stream = java.nio.file.Files.newDirectoryStream(dir)) {
            String prefix1 = "package_" + packageId + "_";
            String prefix2 = "package_" + packageId + ".";
            java.util.List<String> filenames = new java.util.ArrayList<>();
            for (java.nio.file.Path entry : stream) {
                String name = entry.getFileName().toString();
                if (name.startsWith(prefix1) || name.startsWith(prefix2)) {
                    filenames.add(name);
                }
            }
            java.util.Collections.sort(filenames);
            for (String fn : filenames) {
                result.add("/images/" + fn);
            }
        } catch (Exception ignored) {}

        return result;
    }

    private PackageStatusEntity resolvePackageStatus(String statusName) {
        String query = (statusName != null && !statusName.trim().isEmpty()) ? statusName.trim() : "ACTIVE";

        // Handle numeric or text synonyms (e.g. 1 -> ACTIVE, 2 -> DISCONTINUED)
        if ("1".equals(query) || "AVAILABLE".equalsIgnoreCase(query)) {
            query = "ACTIVE";
        } else if ("2".equals(query) || "ARCHIVED".equalsIgnoreCase(query)) {
            query = "DISCONTINUED";
        }

        final String finalQuery = query;
        return packageStatusRepository.findByStatusIgnoreCase(finalQuery)
                .orElseGet(() -> {
                    PackageStatusEntity s = new PackageStatusEntity();
                    s.setStatus(finalQuery.toUpperCase());
                    return packageStatusRepository.save(s);
                });
    }

    private UserEntity resolveCreator(Integer userId) {
        if (userId != null) {
            Optional<UserEntity> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) return userOptional.get();
        }

        // Fallback to first available user
        return userRepository.findAll().stream().findFirst().orElse(null);
    }


    private PackageDTO convertToDTO(PackageEntity pkg) {
        PackageDTO dto = modelMapper.map(pkg, PackageDTO.class);
        if (pkg.getPackageStatus() != null) {
            dto.setStatus(pkg.getPackageStatus().getStatus());
        } else {
            dto.setStatus("ACTIVE");
        }
        if (pkg.getCreatedBy() != null) {
            dto.setCreatedById(pkg.getCreatedBy().getId());
            dto.setCreatedByName(pkg.getCreatedBy().getFirstName() + " " + pkg.getCreatedBy().getLastName());
        }
        java.util.List<String> urls = findPackageImageUrls(pkg.getId());
        dto.setImageUrls(urls);
        if (!urls.isEmpty()) {
            dto.setImageUrl(urls.get(0));
        }
        return dto;
    }

    private String formatInclusionsForStorage(String rawInclusions) {
        if (rawInclusions == null || rawInclusions.trim().isEmpty()) {
            return "Standard package inclusions";
        }
        // Split by commas, semicolons, or newlines
        String[] parts = rawInclusions.split("[,;\\n\\r]+");
        java.util.List<String> items = new java.util.ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                items.add(trimmed);
            }
        }
        if (items.isEmpty()) {
            return "Standard package inclusions";
        }
        return String.join("; ", items);
    }


    public static final class PackageHelperSingleton {

        private static final PackageHelperSingleton INSTANCE =
                new PackageHelperSingleton();

        private PackageHelperSingleton() {
        }

        public static PackageHelperSingleton getInstance() {
            return INSTANCE;
        }

        public String getMessage() {
            return "Fetching all packages";
        }
    }
}
