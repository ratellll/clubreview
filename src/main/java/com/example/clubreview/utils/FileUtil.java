package com.example.clubreview.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

public class FileUtil {


    public static String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String uploadDir = System.getProperty("user.home") + "/uploads";
        String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        String uniqueFileName = UUID.randomUUID().toString() + extension;

        Files.createDirectories(Paths.get(uploadDir));
        file.transferTo(new File(uploadDir, uniqueFileName));
        return "/uploads/" + uniqueFileName;
    }
}
