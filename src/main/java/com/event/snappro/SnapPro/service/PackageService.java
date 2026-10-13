package com.event.snappro.SnapPro.service;

import org.springframework.web.multipart.MultipartFile;
import com.event.snappro.SnapPro.dto.PackageDTO;
import com.event.snappro.SnapPro.dto.ResponseDTO;

public interface PackageService {
    ResponseDTO getAllActivePackages();
    ResponseDTO getAllPackages();
    ResponseDTO getPackageById(Integer id);
    ResponseDTO createPackage(PackageDTO packageDTO);
    ResponseDTO createPackageWithImage(PackageDTO packageDTO, MultipartFile image);
    ResponseDTO createPackageWithImages(PackageDTO packageDTO, java.util.List<MultipartFile> images);
    ResponseDTO updatePackage(Integer id, PackageDTO packageDTO);
    ResponseDTO updatePackageWithImage(Integer id, PackageDTO packageDTO, MultipartFile image);
    ResponseDTO updatePackageWithImages(Integer id, PackageDTO packageDTO, java.util.List<MultipartFile> images);
    ResponseDTO uploadPackageImage(Integer id, MultipartFile image);
    ResponseDTO uploadPackageImages(Integer id, java.util.List<MultipartFile> images);
    ResponseDTO deletePackageImage(Integer id, String imageNameOrUrl);
    ResponseDTO deleteAllPackageImages(Integer id);
    ResponseDTO deletePackage(Integer id);
}
