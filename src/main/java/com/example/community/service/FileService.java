package com.example.community.service;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileService {
    // 도커 컨테이너 내부의 경로입니다.
    @Value("${file.upload-path}")
    private String uploadPath;

    public String storeFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }

        String originalFilename = file.getOriginalFilename();
        String storedFileName = UUID.randomUUID().toString() + "_" + originalFilename;
        String realPath = uploadPath.replace("file:", "");

        file.transferTo(new File(realPath + storedFileName));

        Thumbnails.of(file.getInputStream())
                .size(800, 800)
                .outputQuality(0.75)
                .toFile(new File(realPath + storedFileName));

        return storedFileName;
    }
}
