package com.yidiansishiyi.deepsight.ingestion.doc.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yidiansishiyi.deepsight.exception.ErrorCode;
import com.yidiansishiyi.deepsight.exception.ThrowUtils;
import com.yidiansishiyi.deepsight.ingestion.doc.model.dto.DocxDto;
import com.yidiansishiyi.deepsight.ingestion.doc.model.dto.RawApiData;
import com.yidiansishiyi.deepsight.ingestion.doc.model.dto.RawApiData.RawField;
import com.yidiansishiyi.deepsight.ingestion.doc.model.dto.RawApiData.RawStructure;
import com.yidiansishiyi.deepsight.ingestion.doc.model.dto.RawDocxContentDto;
import com.yidiansishiyi.deepsight.ingestion.doc.service.DocxParser;
import com.yidiansishiyi.deepsight.utils.WordDirectoryExtractor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * [具体策略] 专门用于解析 CIIS 接口文档的实现类。
 * 核心逻辑：基于文档特征（如 "功能方法,"）和上下文识别数据。
 */
@Service
public class CiisDocxParserImpl implements DocxParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public boolean supports(String fileName) {
        return fileName.contains("ciis");
    }

    @Override
    public List<DocxDto> parse(InputStream inputStream) {
        List<RawDocxContentDto> rawDocxContentDtos = WordDirectoryExtractor.extractDirectoryStructure(inputStream);
        ThrowUtils.throwIf(CollUtil.isEmpty(rawDocxContentDtos), ErrorCode.DATA_PARSING_ERROR, "文档目录结构为空");

        List<DocxDto> docxDtos = new ArrayList<>();
        RawApiData rawApiData = new RawApiData();
        String[] topicArray = new String[20];
        for (RawDocxContentDto rawDocxContentDto : rawDocxContentDtos) {
            String style = rawDocxContentDto.getStyle();
            if (DocxDto.DOCX_TYPE_TXT.equals(style)) {
                StringBuilder description = new StringBuilder(rawApiData.getDescription() == null ? "" : rawApiData.getDescription());
                description.append(rawDocxContentDto.getContent()).append("\n");
                rawApiData.setDescription(description.toString());
            } else if (DocxDto.DOCX_TYPE_TABLE.equals(style)) {
                String content = rawDocxContentDto.getContent();

                try {
                    List<JsonNode> parsedContent = objectMapper.readValue(
                            content,
                            new TypeReference<List<JsonNode>>() {
                            }
                    );
                    if (CollUtil.isEmpty(parsedContent)) {
                        continue;
                    }
                    if (containsAnyKeyword(rawApiData.getHeadingPath(), DocxDto.INTER_FLAG)) {
                        setInputParams(parsedContent, rawApiData);
                    }
                    if (containsAnyKeyword(rawApiData.getHeadingPath(), DocxDto.REQUEST_FLAG)) {
                        List<RawField> rawFields = mapJsonToRawFields(parsedContent);
                        rawApiData.setInputParams(rawFields);
                    }
                    if (containsAnyKeyword(rawApiData.getHeadingPath(), DocxDto.RESPONSE_FLAG)) {
                        List<RawField> rawFields = mapJsonToRawFields(parsedContent);
                        RawStructure rawStructure = new RawStructure();
                        rawStructure.setFields(rawFields);
                        rawApiData.setReturnField(rawFields);
                    }
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            } else {
                rawApiData = new RawApiData();
                String topic = getTopic(topicArray, Integer.valueOf(rawDocxContentDto.getStyle()), rawDocxContentDto.getContent());
                rawApiData.setFullPath(topic);
                rawApiData.setHeadingPath(rawDocxContentDto.getStyle() + "#" + rawDocxContentDto.getContent());
                docxDtos.add(rawApiData);
            }
        }


        return docxDtos;
    }

    /**
     * 更新数组中指定位置的 Topic，并返回当前有效 Topic 的完整路径。
     *
     * @param topicArray 存储 Topic 的数组。
     * @param idx 当前有效的 Topic 数量 (size)，即下一个元素应该放置的索引。
     * @param topic 要放置的新 Topic 字符串。
     * @return 使用 " > " 分割符连接的当前 Topic 路径（从索引 0 到 idx-1）。
     */
    private String getTopic(String[] topicArray, int idx, String topic) {
        if (idx <= 0 || idx > topicArray.length) {
            if (idx <= 0) {
                return "";
            }
            throw new IndexOutOfBoundsException("数组索引错误或 Topic Stack 溢出。Idx: " + idx + ", 数组长度: " + topicArray.length);
        }

        topicArray[idx - 1] = topic;

        List<String> validTopics = Arrays.stream(topicArray, 0, idx - 1)
                .filter(StrUtil::isNotBlank)
                .filter(res -> !"null".equals(res))
                .collect(Collectors.toList());

        return StrUtil.join(" > ", validTopics);
    }

    private void setInputParams(List<JsonNode> parsedContent, RawApiData rawApiData) {
        if (parsedContent.size() == 3) {
            JsonNode jsonNode0 = parsedContent.get(0);
            rawApiData.setCommonInterface(jsonNode0.get("内容").asText());

            JsonNode jsonNode1 = parsedContent.get(1);
            rawApiData.setMethodName(jsonNode1.get("内容").asText());

            JsonNode jsonNode2 = parsedContent.get(2);
            String text = jsonNode2.get("内容").asText();
            List<RawField> rawFields = mapINTERJsonToRawFields(text);
            rawApiData.setInputParams(rawFields);
        }
    }

    private List<RawField> mapINTERJsonToRawFields(String parsedContent) {
        if (parsedContent == null) {
            return null;
        }
        String[] split = parsedContent.replace("(", "")
                .replace(")", "")
                .replace("（", "")
                .replace("）", "")
                .split(",");
        List<RawField> res = new ArrayList<>();
        for (String age : split) {
            if (StrUtil.isBlank(age) || "空".equals(age)) {
                continue;
            }
            String[] ageN = age.trim().split(" ");
            RawField rawField = new RawField();
            rawField.setJavaType(ageN[0]);
            try {
                rawField.setName(ageN[1]);
            } catch (Exception e) {
                System.out.println("==========================");
                System.out.println(age);
            }
            res.add(rawField);
        }
        return res;
    }

    public static boolean containsAnyKeyword(String text, List<String> keywords) {
        return text != null && keywords.stream().anyMatch(text::contains);
    }

    public List<RawField> mapJsonToRawFields(List<JsonNode> parsedContent) {
        List<RawField> rawFields = new ArrayList<>();

        for (JsonNode node : parsedContent) {
            try {
                RawField rawField = new RawField();

                JsonNode dbType = node.get("数据库类型");
                rawField.setDbType(dbType == null ? "" : dbType.asText());
                JsonNode name = node.get("字段");
                rawField.setName(name == null ? "" : name.asText());
                JsonNode javaType = node.get("Java类型");
                rawField.setJavaType(javaType == null ? "" : javaType.asText());
                JsonNode cnName = node.get("名称");
                rawField.setCnName(cnName == null ? "" : cnName.asText());
                JsonNode comment = node.get("备注");
                rawField.setComment(comment == null ? "" : comment.asText());
                JsonNode required = node.get("是否必填");
                rawField.setRequired(required == null ? "" : required.asText());

                rawFields.add(rawField);
            } catch (IllegalArgumentException e) {
                // 捕获转换失败异常 (例如：JsonNode中缺少字段或类型不匹配)
                System.err.println("映射 JsonNode 到 RawField 失败: " + node.toString());
                e.printStackTrace();
            }
        }
        return rawFields;
    }

}