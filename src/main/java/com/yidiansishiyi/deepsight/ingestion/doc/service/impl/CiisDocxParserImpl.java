package com.yidiansishiyi.deepsight.ingestion.doc.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
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
import com.yidiansishiyi.deepsight.utils.DocxOutlineExtractor;
import com.yidiansishiyi.deepsight.utils.HeadingItem;
import com.yidiansishiyi.deepsight.utils.WordDirectoryExtractor;
import jakarta.annotation.PostConstruct;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * [具体策略] 专门用于解析 CIIS 接口文档的实现类。
 * 核心逻辑：基于文档特征（如 "功能方法,"）和上下文识别数据。
 */
@Service
public class CiisDocxParserImpl implements DocxParser {

    // 优化后的正则：匹配独立结构标题。例如 "表104 查询地区返回contents字段" 或 "表\d+ BpmTaskProcessVO详细字段"
    private static final Pattern STRUCTURE_TABLE_TITLE_PATTERN =
            Pattern.compile("表\\d+\\s+(查询)?(.+?)(返回contents字段|详细字段)");

    // 独立结构（如 地区，行业，评级）的名称列表，用于辅助识别段落标题
    private static final List<String> GLOBAL_STRUCTURE_NAMES = List.of("地区", "行业", "评级", "自定义字段设置");

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    void setUp() {
        // 用于测试时加载文件
        String testFileName = "ciis/doc/1.第一章-基础数据.docx";
        InputStream testFileStream = ResourceUtil.getStream(testFileName);
//        List<RawDocxContentDto> rawDocxContentDtos = WordDirectoryExtractor.extractDirectoryStructure(testFileName);
        List<DocxDto> parse = parse(testFileStream);
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        String jsonStr = JSONUtil.toJsonStr(parse);
        System.out.println(jsonStr);
    }

    @Override
    public boolean supports(String fileName) {
        // 示例：判断文件名前缀是否符合 CIIS 命名规范
        return fileName.contains("第四章") || fileName.contains("ciis");
    }

    @Override
    public List<DocxDto> parse(InputStream inputStream) {
        List<RawDocxContentDto> rawDocxContentDtos = WordDirectoryExtractor.extractDirectoryStructure(inputStream);
        ThrowUtils.throwIf(CollUtil.isEmpty(rawDocxContentDtos), ErrorCode.DATA_PARSING_ERROR, "文档目录结构为空");

        List<DocxDto> docxDtos = new ArrayList<>();
        RawApiData rawApiData = new RawApiData();
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
                        if (parsedContent.size() == 3) {
                            JsonNode jsonNode0 = parsedContent.get(0);
                            rawApiData.setCommonInterface(jsonNode0.get("内容").asText());

                            JsonNode jsonNode1 = parsedContent.get(1);
                            rawApiData.setMethodName(jsonNode1.get("内容").asText());

                            JsonNode jsonNode2 = parsedContent.get(2);
                            rawApiData.setMethodName(jsonNode2.get("内容").asText());

                        }
                        List<RawField> rawFields = mapINTERJsonToRawFields(parsedContent);
                        rawApiData.setInputParams(rawFields);
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

                    // 两种，一种调用接口信息的，另一种是入参出参的，可以接多个表单信息嵌套。
                    System.out.println(parsedContent);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

                System.out.println();
            } else {
                rawApiData = new RawApiData();
                rawApiData.setHeadingPath(rawDocxContentDto.getStyle() + "#" + rawDocxContentDto.getContent());
                docxDtos.add(rawApiData);
            }
        }


        return docxDtos;
    }

    private List<RawField> mapINTERJsonToRawFields(List<JsonNode> parsedContent) {
        return null;
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

    /**
     * 从 Java 类型字符串中提取出复杂类型名称（如 List<String> 中的 String）
     *
     * @param javaType 原始 Java 类型字符串
     * @return 复杂类型名称，如果不是复杂类型则返回 null
     */
    private String extractComplexTypeName(String javaType) {
        if (javaType == null || !javaType.contains("<") || !javaType.contains(">")) {
            return null;
        }

        // 查找最后一个 '<' 的位置
        int start = javaType.lastIndexOf('<');
        // 查找第一个 '>' 的位置
        int end = javaType.indexOf('>');

        if (start != -1 && end != -1 && start < end) {
            // 提取 <> 之间的内容
            String innerType = javaType.substring(start + 1, end).trim();

            // 如果内部类型仍然是复杂类型 (如 Map<K,V> 或 List<List<T>>)
            // 我们可以只取第一个逗号之前或直接返回
            if (innerType.contains(",")) {
                // 如果是 Map<K, V>，通常我们只关心值类型 V，这里简化为只取最后一部分
                return innerType.substring(innerType.lastIndexOf(',') + 1).trim();
            }

            return innerType;
        }
        return null;
    }

}