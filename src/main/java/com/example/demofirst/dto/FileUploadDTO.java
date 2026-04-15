package com.example.demofirst.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传DTO
 */
@Data
public class FileUploadDTO {

    @NotNull(message = "文件不能为空")
    private MultipartFile file;      // 上传的文件

    @NotBlank(message = "文件业务类型不能为空")
    private String bizType;         // 文件业务类型（如user_avatar/order_attachment）

    private String remark;          // 文件备注
}
