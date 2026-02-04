package com.example.community.service;

import com.example.community.exception.CustomException;
import com.example.community.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.springframework.web.util.WebUtils.getRealPath;

@Slf4j
@Service
public class FileService {
    @Value("${file.upload-path}")
    private String uploadPath;

    public String storeFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new CustomException(ErrorCode.IMAGES_PROCESS_ERROR);
        }

        String originalFilename = file.getOriginalFilename();
        String fileNameWithoutExtension = originalFilename.substring(0, originalFilename.lastIndexOf("."));

        String baseFileName = UUID.randomUUID().toString() + "_" + fileNameWithoutExtension;
        String realPath = getRealPath();

        try {
            Thumbnails.of(file.getInputStream())
                    .size(800, 800)
                    .outputQuality(0.75)
                    .outputFormat("jpg")
                    .toFile(new File(realPath + baseFileName));
        } catch (IOException e) {
            throw new CustomException(ErrorCode.IMAGE_SIZE_EXCEEDED);
        }

        return baseFileName + ".jpg";
    }

    public void deleteFile(String storedFileName){
        if(storedFileName == null || storedFileName.isEmpty()){
            return;
        }

        Path filePath = Paths.get(getRealPath()).resolve(storedFileName);

        try {
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("파일 삭제 성공: {}", filePath.toAbsolutePath());
            } else {
                log.warn("삭제 실패: 파일이 존재하지 않습니다. 경로: {}", filePath.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("파일 삭제 중 오류 발생 (권한 등): {}", e.getMessage());
        }

    }

    private String getRealPath() {
        return uploadPath.replace("file:", "");
    }
}
