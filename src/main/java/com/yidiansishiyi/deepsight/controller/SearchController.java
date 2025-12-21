package com.yidiansishiyi.deepsight.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.yidiansishiyi.deepsight.aop.TagDiscoveryService;
import com.yidiansishiyi.deepsight.common.BaseResponse;
import com.yidiansishiyi.deepsight.common.ResultUtils;
import com.yidiansishiyi.deepsight.common.SearchRequest;
import com.yidiansishiyi.deepsight.exception.ErrorCode;
import com.yidiansishiyi.deepsight.exception.ThrowUtils;
import com.yidiansishiyi.deepsight.model.vo.SearchThemeVO;
import com.yidiansishiyi.deepsight.model.vo.SearchVO;
import com.yidiansishiyi.deepsight.service.CiisDocxNeo4J;
import com.yidiansishiyi.deepsight.utils.MarkdownDocumentBuilder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/search")
@Slf4j
public class SearchController {

    @Resource
    private TagDiscoveryService tagDiscoveryService;

    @Resource
    private CiisDocxNeo4J ciisDocxNeo4J;

    @PostMapping("/queryTag")
    public BaseResponse<SearchVO> searchTag(@RequestBody SearchRequest searchRequest) {
        ThrowUtils.throwIf(searchRequest == null, ErrorCode.PARAMS_ERROR);

        SearchVO searchVO = new SearchVO();
        List<SearchThemeVO> searchThemeVOList = TagDiscoveryService.searchThemeVOList;
        searchVO.setSearchThemeVOList(searchThemeVOList);

        return ResultUtils.success(searchVO);
    }

    @PostMapping("/query")
    public BaseResponse<SearchVO> search(@RequestBody SearchRequest searchRequest) {
        ThrowUtils.throwIf(searchRequest == null, ErrorCode.PARAMS_ERROR);
        List<Map<String, Object>> fullDocumentByMethod = null;
        // 自然语言转换抽取关键词，获取 type 类内容 ok 专门放到一个方法里面去做
//        不用自然语言的情况下专门关键词搜索
        String type = searchRequest.getType();
        if (StrUtil.isNotBlank(type)) {
            String[] split = type.split(":");
            if (split.length > 2) {
                for (String tag : split) {
//                    tag.
                }
            }
        }else {
            fullDocumentByMethod = ciisDocxNeo4J.getFullDocumentByMethod(searchRequest.getSearchText());
        }

        String mdDoc = new MarkdownDocumentBuilder().buildMarkdown(fullDocumentByMethod);
        SearchVO searchVO = new SearchVO();

        Map<String, Object> res = new HashMap<>();
        res.put("variable","markDown");
        res.put("date",mdDoc);
        searchVO.setSendBody(res);

        return ResultUtils.success(searchVO);
    }


}
