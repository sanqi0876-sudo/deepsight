package com.yidiansishiyi.deepsight.controller;

import cn.hutool.json.JSONUtil;
import com.yidiansishiyi.deepsight.common.BaseResponse;
import com.yidiansishiyi.deepsight.common.ResultUtils;
import com.yidiansishiyi.deepsight.common.SearchRequest;
import com.yidiansishiyi.deepsight.exception.ErrorCode;
import com.yidiansishiyi.deepsight.exception.ThrowUtils;
import com.yidiansishiyi.deepsight.service.CiisDocxNeo4J;
import com.yidiansishiyi.deepsight.utils.MarkdownDocumentBuilder;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ciis/doc")
public class CiisDocController {

    @Resource
    private CiisDocxNeo4J ciisDocxNeo4J;

    @PostMapping("/query")
    public BaseResponse<String> userRegister(@RequestBody SearchRequest searchRequest) {
        ThrowUtils.throwIf(searchRequest == null, ErrorCode.PARAMS_ERROR);
//        List<MethodFullDetailsProjection> methodEntities =
        List<Map<String, Object>> fullDocumentByMethod = ciisDocxNeo4J.getFullDocumentByMethod(searchRequest.getSearchText());

        String mdDoc = new MarkdownDocumentBuilder().buildMarkdown(fullDocumentByMethod);

        return ResultUtils.success(mdDoc);
    }
}
