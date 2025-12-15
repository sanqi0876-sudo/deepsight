package com.yidiansishiyi.deepsight.ingestion.doc.model.dto;

import com.yidiansishiyi.deepsight.ingestion.doc.service.impl.CiisDocxParserImpl;
import lombok.Data;
import java.util.List;
import java.util.ArrayList;

/**
 * 顶级原始数据模型：承载从一个API接口文档章节中解析出的所有信息。
 * 作用：将 Apache POI 解析细节与 GraphMapper 建模逻辑解耦。
 */
@Data
public class RawApiData implements DocxDto{

    /**
     * 聚合所有可检索文本 (TopicPath, 接口名, 字段名/备注等)。
     * 用于在 Neo4j 中创建全文索引，以实现模糊搜索。
     */
    private String searchContent;

    // =========================================================
    // 方法/接口基础信息
    // =========================================================
    private String commonInterface;    // 接口标题 (例如 ICiisCalculateServcie)
    private String fullPath;           // 功能方法全路径，唯一标识
    private String headingPath;        // 对应标题章节 (用于回溯原始文档位置)
    private String methodName;         // 方法名，如 getApprovalStatusAndInspection
    private String description;        // 接口描述

    // 入参信息（对应文档中的“请求字段”表格）
    private List<RawField> inputParams = new ArrayList<>();

    // 返回信息（对应文档中的“返回字段”表格，通常只有一个顶级字段）
    private List<RawField> returnField;

    // 独立定义的数据结构列表（对应文档中的 表114 BpmTaskProcessVO详细字段 等）
    private List<RawStructure> nestedStructures = new ArrayList<>();

    // ------------------------------------------------------------------------
    // 嵌套类 1：RawField (用于承载表格中的一行数据，无论是入参还是出参)
    // ------------------------------------------------------------------------

    /**
     * 代表表格中的一行数据（一个字段或属性）。
     */
    @Data
    public static class RawField {
        private String name;
        private String cnName;
        private String javaType;
        private String dbType;
        private String required;
        private String comment;

        private String complexTypeName; // 原始 JavaType 中包含的复杂类型名称
    }

    // ------------------------------------------------------------------------
    // 嵌套类 2：RawStructure (用于承载独立定义的一个 VO/DTO/Class)
    // ------------------------------------------------------------------------

    /**
     * 代表文档中独立定义的复杂结构（如 表114 BpmTaskProcessVO详细字段）。
     */
    @Data
    public static class RawStructure {
        private String structureName;
        private String description;
        private List<RawField> fields = new ArrayList<>();
    }
}