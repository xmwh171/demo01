package com.example.demofirst.utils;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

/**
 * 文件上传工具类（NIO优化+MD5校验+类型限制）
 */
@Slf4j
@Component
public class FileUploadUtils {

    // 注入配置文件中的存储路径
    @Value("${file.upload.path}")
    private String uploadPath;

    // 注入允许上传的文件类型
    @Value("${file.upload.allowed-types}")
    private String allowedTypes;

    /**
     * NIO方式上传文件（零拷贝优化）
     * @param file 上传的文件
     * @param bizType 业务类型
     * @return 文件存储路径+MD5值
     */
    public UploadResult uploadFile(MultipartFile file, String bizType) throws IOException {
        // 1. 基础校验
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // 2. 文件类型校验
        String originalFilename = file.getOriginalFilename();
        String fileExt = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!Arrays.asList(allowedTypes.split(",")).contains(fileExt)) {
            throw new IllegalArgumentException("不支持的文件类型：" + fileExt + "，允许类型：" + allowedTypes);
        }

        // 3. 创建业务目录（如/opt/demo01/upload/user_avatar/）
        String bizPath = uploadPath + bizType + "/";
        File bizDir = new File(bizPath);
        if (!bizDir.exists()) {
            boolean mkdirsSuccess = bizDir.mkdirs();
            if (!mkdirsSuccess) {
                throw new IOException("创建文件目录失败：" + bizPath);
            }
        }

        // 4. 生成唯一文件名（避免重复）
        String uniqueFileName = UUID.randomUUID().toString() + "." + fileExt;
        String filePath = bizPath + uniqueFileName;

        // 5. NIO零拷贝上传（核心优化：替代BIO的FileUtils.copyInputStreamToFile）
        File tempFile = File.createTempFile("upload_", "_tmp");
        file.transferTo(tempFile); // 先把上传文件写入临时文件
        try (FileChannel inChannel = new FileInputStream(tempFile).getChannel();
             FileChannel outChannel = new FileOutputStream(filePath).getChannel()) {
            // 零拷贝：直接将输入通道的数据传输到输出通道
            long transferred = 0;
            long size = inChannel.size();
            while (transferred < size) {
                transferred += inChannel.transferTo(transferred, size - transferred, outChannel);
            }
            log.info("文件上传成功（NIO零拷贝），原文件名：{}，存储路径：{}，文件大小：{}MB",
                    originalFilename, filePath, size / 1024 / 1024);
        }

        // 6. 计算文件MD5（防篡改、防重复上传）
        String fileMd5 = calculateFileMd5(filePath);

        // 7. 返回上传结果
        return new UploadResult(filePath, fileMd5, originalFilename, fileExt, file.getSize());

    }

    /**
     * 计算文件MD5值
     */
    public String calculateFileMd5(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            return DigestUtils.md5DigestAsHex(fis);
        }
    }

    /**
     * NIO方式下载文件
     */
    public void downloadFile(String filePath, OutputStream outputStream) throws IOException {
        if (!Files.exists(Paths.get(filePath))) {
            throw new IOException("文件不存在：" + filePath);
        }

        // 方式1：如果是 FileOutputStream，继续用零拷贝
        if (outputStream instanceof FileOutputStream fos) {
            try (FileChannel inChannel = new FileInputStream(filePath).getChannel();
                 FileChannel outChannel = fos.getChannel()) {
                long transferred = 0;
                long size = inChannel.size();
                while (transferred < size) {
                    transferred += inChannel.transferTo(transferred, size - transferred, outChannel);
                }
                log.info("文件下载成功（NIO零拷贝），路径：{}，大小：{}MB", filePath, size / 1024 / 1024);
            }
        }
        // 方式2：如果是 ServletOutputStream（HTTP响应流），用普通流拷贝
        else {
            try (InputStream in = new FileInputStream(filePath)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
                log.info("文件下载成功（流拷贝），路径：{}", filePath);
            }
        }
    }

    /**
     * 文件上传结果封装类
     */
    public static class UploadResult {
        private String filePath;    // 存储路径
        private String md5;         // MD5值
        private String originalName;// 原文件名
        private String ext;         // 文件后缀
        private long size;          // 文件大小（字节）

        public UploadResult(String filePath, String md5, String originalName, String ext, long size) {
            this.filePath = filePath;
            this.md5 = md5;
            this.originalName = originalName;
            this.ext = ext;
            this.size = size;
        }

        // getter/setter
        public String getFilePath() { return filePath; }
        public String getMd5() { return md5; }
        public String getOriginalName() { return originalName; }
        public String getExt() { return ext; }
        public long getSize() { return size; }
    }

}
