package com.GadgetZone.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path staticImagesLocation;

    public FileStorageService() throws IOException {
        this.staticImagesLocation = Paths.get("src/main/resources/static/images")
                .toAbsolutePath()
                .normalize();
        Files.createDirectories(this.staticImagesLocation);
        System.out.println("Image storage path: " + this.staticImagesLocation);
    }

    public String storeFile(MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String newFileName = UUID.randomUUID() + "_" + fileName;
        Path targetLocation = this.staticImagesLocation.resolve(newFileName);
        System.out.println("Saving file to: " + targetLocation);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("File saved successfully: " + newFileName);
        return "/images/" + newFileName;
    }
}

