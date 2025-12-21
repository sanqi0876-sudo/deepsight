package com.yidiansishiyi.deepsight.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.yidiansishiyi.deepsight.common.BaseResponse;
import com.yidiansishiyi.deepsight.common.ResultUtils;
import com.yidiansishiyi.deepsight.exception.BusinessException;
import com.yidiansishiyi.deepsight.exception.ErrorCode;
import com.yidiansishiyi.deepsight.model.dto.file.UploadFileRequest;
import com.yidiansishiyi.deepsight.model.entity.User;
import com.yidiansishiyi.deepsight.model.enums.FileUploadBizEnum;
import com.yidiansishiyi.deepsight.model.vo.FileInfoVO;
import com.yidiansishiyi.deepsight.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private UserService userService;

    // 注入配置文件中的路径
    @Value("${file.upload-path}")
    private String uploadPath;

    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
                                           String biz,
                                           UploadFileRequest uploadFileRequest, HttpServletRequest request) {
        uploadFileRequest.setBiz(biz);
        biz = uploadFileRequest.getBiz();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        validFile(multipartFile, fileUploadBizEnum);
        User loginUser = userService.getLoginUser(request);

        // 1. 生成文件名：uuid-原始文件名
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String filename = uuid + "-" + multipartFile.getOriginalFilename();

        // 2. 构建跨平台相对路径：biz/userId/filename
        // 使用 Paths.get().toString() 会自动根据系统选择 / 或 \
        String relativePath = Paths.get(fileUploadBizEnum.getValue(),
                String.valueOf(loginUser.getId()),
                filename).toString();

        // 3. 构建绝对物理路径
        File destFile = new File(uploadPath + File.separator + relativePath);

        try {
            // 4. 自动创建父级目录（适配 Win/Linux）
            if (!destFile.getParentFile().exists()) {
                destFile.getParentFile().mkdirs();
            }

            // 5. 将上传的文件写入物理磁盘
            multipartFile.transferTo(destFile);

            log.info("文件上传成功，绝对路径：{}", destFile.getAbsolutePath());

            // 6. 返回相对路径或访问 URL（前端可以通过映射读取）
            return ResultUtils.success(relativePath);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + relativePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }
    }

    /**
     * 获取用户上传的文件列表
     * @param biz 业务类型（可选，不传查全部）
     */
    @GetMapping("/list")
    public BaseResponse<List<FileInfoVO>> listFiles(String biz, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();

        // 1. 构建要扫描的根目录：uploadPath / {biz} / {userId}
        // 如果 biz 为空，则扫描 uploadPath 整个目录下该用户的所有文件夹
        String searchPath = uploadPath;
        if (StringUtils.isNotBlank(biz)) {
            searchPath = Paths.get(uploadPath, biz, String.valueOf(userId)).toString();
        } else {
            // 如果没传 biz，扫描全部业务下该用户目录
            searchPath = uploadPath;
        }

        File rootFile = new File(searchPath);
        if (!rootFile.exists()) {
            return ResultUtils.success(new ArrayList<>());
        }

        // 2. 递归获取该用户目录下所有文件
        List<File> files = FileUtil.loopFiles(rootFile, new FileFilter() {
            @Override
            public boolean accept(File file) {
                // 排除隐藏文件，且只找该用户 ID 目录下的文件
                return !file.isHidden() && file.getAbsolutePath().contains(File.separator + userId + File.separator);
            }
        });

        // 3. 转换为 VO 列表
        List<FileInfoVO> fileInfoVOList = files.stream().map(file -> {
                    FileInfoVO vo = new FileInfoVO();
                    vo.setFileName(file.getName());
                    vo.setFileSize(file.length());
                    vo.setFileType(FileUtil.getSuffix(file));
                    // 使用 Hutool 格式化时间
                    vo.setCreateTime(DateUtil.formatDateTime(new Date(file.lastModified())));

                    // 核心：构建前端下载/预览 URL
                    // 逻辑：将绝对路径中 uploadPath 之后的部分截取出来，并转换反斜杠
                    String absolutePath = file.getAbsolutePath();
                    String relativePath = absolutePath.substring(new File(uploadPath).getAbsolutePath().length());
                    String webPath = relativePath.replace("\\", "/");
                    if (!webPath.startsWith("/")) {
                        webPath = "/" + webPath;
                    }

                    // 拼接你在 WebMvcConfig 中配置的映射路径 /file/download/
                    vo.setDownloadUrl("/file/download" + webPath);

                    return vo;
                }).sorted(Comparator.comparing(FileInfoVO::getCreateTime).reversed()) // 按时间倒序
                .collect(Collectors.toList());

        return ResultUtils.success(fileInfoVOList);
    }

    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        long fileSize = multipartFile.getSize();
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 1024 * 1024L;
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > 5 * ONE_M) { // 头像放宽到 5M
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 5M");
            }
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix.toLowerCase())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }
}