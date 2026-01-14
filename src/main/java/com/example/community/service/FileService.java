package com.example.community.service;

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

        String originalFilename = file.getOriginalFilename();
        String storedFileName = UUID.randomUUID().toString() + "_" + originalFilename;

        // 중요: "file:" 접두사를 제거해서 실제 경로인 "/app/upload/"로 만듭니다.
        String realPath = uploadPath.replace("file:", "");

        file.transferTo(new File(realPath + storedFileName));

        return storedFileName;
    }
}
