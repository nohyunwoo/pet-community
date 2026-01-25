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
import java.util.UUID;

import static org.springframework.web.util.WebUtils.getRealPath;

@Slf4j
@Service
public class FileService {
    // 도커 컨테이너 내부의 경로입니다.
    @Value("${file.upload-path}")
    private String uploadPath;

    public String storeFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new CustomException(ErrorCode.IMAGES_PROCESS_ERROR);
        }

        String originalFilename = file.getOriginalFilename();
        String storedFileName = UUID.randomUUID().toString() + "_" + originalFilename;
        String realPath = getRealPath();

        try {
            Thumbnails.of(file.getInputStream())
                    .size(800, 800)
                    .outputQuality(0.75)
                    .outputFormat("jpg")
                    .toFile(new File(realPath + storedFileName));
        } catch (IOException e) {
            throw new CustomException(ErrorCode.IMAGE_SIZE_EXCEEDED);
        }

        return storedFileName;
    }

    public void deleteFile(String storedFileName){
        if(storedFileName == null || storedFileName.isEmpty()){
            return;
        }

        String realPath = getRealPath();
        File file = new File(realPath + storedFileName);

        if(file.exists()){
            if(file.delete()){
                log.info("파일 삭제 성공: {}", storedFileName);
            }
            else{
                log.warn("파일 삭제 실패(파일은 존재하나 삭제하지 못함): {}", storedFileName);
            }
        }else{
            log.warn("삭제하려는 파일이 존재하지 않음: {}", storedFileName);
        }

    }

    private String getRealPath() {
        return uploadPath.replace("file:", "");
    }
}
