package com.example.demofirst.controller;


import com.example.demofirst.Result;
import com.example.demofirst.constant.ErrorCodeEnum;
import com.example.demofirst.dto.FileUploadDTO;
import com.example.demofirst.utils.FileUploadUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;


/**
 * 文件上传/下载Controller（NIO优化）
 */
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private FileUploadUtils fileUploadUtils;

    /**
     * 文件上传接口（NIO零拷贝）
     */
    @PostMapping("/upload")
    public Result<FileUploadUtils.UploadResult> uploadFile(@Validated FileUploadDTO uploadDTO) {
        try {
            FileUploadUtils.UploadResult result = fileUploadUtils.uploadFile(uploadDTO.getFile(), uploadDTO.getBizType());
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(ErrorCodeEnum.BUSINESS_ERROR,"文件上传失败：" + e.getMessage());
        }
    }


    /**
     * 文件下载接口（NIO零拷贝）
     */
    @GetMapping("/download")
    public void downloadFile(@RequestParam String filePath, HttpServletResponse response) {
        try {
            // 设置响应头（解决中文乱码）
            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
            response.setContentType("application/octet-stream");

            // NIO下载文件到响应输出流
            fileUploadUtils.downloadFile(filePath, response.getOutputStream());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                response.getWriter().write("文件下载失败：" + e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    /**
     * 文件MD5校验接口（防篡改）
     */
    @GetMapping("/checkMd5")
    public Result<String> checkFileMd5(@RequestParam String filePath, @RequestParam String expectMd5) {
        try {
            String actualMd5 = fileUploadUtils.calculateFileMd5(filePath);
            if (actualMd5.equals(expectMd5)) {
                return Result.success("文件校验通过：MD5一致");
            } else {
                return Result.error(ErrorCodeEnum.BUSINESS_ERROR,"文件校验失败：MD5不一致，实际值：" + actualMd5 + "，预期值：" + expectMd5);
            }
        } catch (Exception e) {
            return Result.error(ErrorCodeEnum.BUSINESS_ERROR,"文件校验失败：" + e.getMessage());
        }
    }
}
