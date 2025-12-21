package com.yidiansishiyi.deepsight.model.vo;

import lombok.Data;

@Data
public class FileInfoVO {
    private String fileName;
    private Long fileSize;
    private String fileType;
    private String createTime;
    private String downloadUrl; // 拼接好的前端访问地址
}