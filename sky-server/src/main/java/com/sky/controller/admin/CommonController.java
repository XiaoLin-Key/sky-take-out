package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@Api(tags = "通用接口")
@RequestMapping("/admin/common")
@Slf4j
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;
    @ApiOperation("文件上传")
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) {
        try {
            // 原始文件名
            String originalFilename = file.getOriginalFilename();
            // 截取原始文件名的后缀（例如：.jpg）
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 构造新文件名（例如：123.jpg）
            String objectName = UUID.randomUUID().toString() + extension;

            // 调用阿里云OSS工具类上传文件
            String uploadUrl = aliOssUtil.upload(file.getBytes(), objectName);
            return Result.success(uploadUrl);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败");
        }
    }
}
