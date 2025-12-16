package com.yidiansishiyi.deepsight.controller;

import cn.hutool.json.JSONUtil;
import com.yidiansishiyi.deepsight.common.BaseResponse;
import com.yidiansishiyi.deepsight.common.ResultUtils;
import com.yidiansishiyi.deepsight.common.SearchRequest;
import com.yidiansishiyi.deepsight.exception.ErrorCode;
import com.yidiansishiyi.deepsight.exception.ThrowUtils;
import com.yidiansishiyi.deepsight.graph.entity.common.DataStructureEntity;
import com.yidiansishiyi.deepsight.graph.entity.common.MethodEntity;
import com.yidiansishiyi.deepsight.graph.entity.common.ParameterEntity;
import com.yidiansishiyi.deepsight.graph.repository.MethodFullDetailsProjection;
import com.yidiansishiyi.deepsight.service.CiisDocxNeo4J;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ciis/doc")
public class CiisDocController {

    @Resource
    private CiisDocxNeo4J ciisDocxNeo4J;

    @PostMapping("/query")
    public BaseResponse<List<MethodEntity>> userRegister(@RequestBody SearchRequest searchRequest) {
        ThrowUtils.throwIf(searchRequest == null, ErrorCode.PARAMS_ERROR);
        List<MethodEntity> methodEntities = ciisDocxNeo4J.getFullDocumentByMethod(searchRequest.getSearchText());
        String jsonStr = JSONUtil.toJsonStr(methodEntities);
        System.out.println(jsonStr);
        return ResultUtils.success(methodEntities);
    }
}
