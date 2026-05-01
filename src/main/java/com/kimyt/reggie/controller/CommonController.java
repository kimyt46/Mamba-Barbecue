package com.kimyt.reggie.controller;

import com.kimyt.reggie.common.R;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<String> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return R.error("上传文件不能为空");
        }

        log.info("upload file: {}", file.getOriginalFilename());

        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (StringUtils.hasText(originalFilename) && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID() + suffix;

        try {
            Path dirPath = Paths.get(basePath);
            Files.createDirectories(dirPath);
            file.transferTo(dirPath.resolve(fileName));
            return R.success(fileName);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return R.error("文件上传失败");
        }
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        Path filePath = Paths.get(basePath, name);
        try (FileInputStream fileInputStream = new FileInputStream(filePath.toFile());
             ServletOutputStream outputStream = response.getOutputStream()) {
            response.setContentType("image/jpeg");
            byte[] bytes = new byte[1024];
            int len;
            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
            }
            outputStream.flush();
        } catch (Exception e) {
            log.error("文件下载失败", e);
        }
    }
}
