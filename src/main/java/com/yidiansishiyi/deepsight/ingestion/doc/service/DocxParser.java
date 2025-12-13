package com.yidiansishiyi.deepsight.ingestion.doc.service;

import com.yidiansishiyi.deepsight.ingestion.doc.model.dto.DocxDto;

import java.io.InputStream;
import java.util.List;

/**
 * [策略接口] DOCX 文档解析策略。
 * 定义了所有文档解析器（如 CIIS 文档、XMS 文档等）必须实现的通用方法。
 * 目标：将文件读取和业务数据提取解耦。
 */
public interface DocxParser {

    /**
     * 解析给定的 DOCX 文件输入流，将其内容转换为标准化的 RawApiData 列表。
     * * @param inputStream DOCX 文件的输入流
     * @return 包含所有解析出的 API 方法信息的列表
     */
    List<DocxDto> parse(InputStream inputStream);

    /**
     * 判断当前解析器是否支持处理该文件。
     * 用于未来的策略选择器（例如：根据文档内容的第一行判断是 CIIS 文档还是 XMS 文档）。
     * * @param fileName 文件名或文档标识
     * @return 如果支持解析则返回 true
     */
    boolean supports(String fileName);
}