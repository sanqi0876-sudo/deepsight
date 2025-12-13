package com.yidiansishiyi.deepsight.ingestion.doc.model.dto;

import lombok.Data;
import java.util.List;
import java.util.ArrayList;

/**
 * 顶级原始数据模型：承载从一个API接口文档章节中解析出的所有信息。
 * 作用：将 Apache POI 解析细节与 GraphMapper 建模逻辑解耦。
 */
@Data
public class RawApiData implements DocxDto{
    
    // 方法/接口基础信息
    private String fullPath;         // 功能方法全路径，唯一标识
    private String methodName;       // 方法名，如 getApprovalStatusAndInspection
    private String description;      // 接口描述
    
    // 入参信息（对应文档中的“请求字段”表格）
    private List<RawField> inputParams = new ArrayList<>(); 
    
    // 返回信息（对应文档中的“返回字段”表格，通常只有一个顶级字段）
    private RawField returnField;     
    
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
        private String name;           // 字段代码，如 serialNumber, bpmHisTaskMap
        private String cnName;         // 中文名称
        private String javaType;       // Java 类型，如 String, List<String>, Map<String, List<BpmTaskProcessVO>>
        private String dbType;         // 数据库类型
        private String required;       // 是否必填 (仅入参使用)
        private String comment;        // 备注信息
        
        // 原始 JavaType 中包含的复杂类型名称（用于 Mapper 识别嵌套结构）
        // 例如：JavaType是 Map<String, List<BpmTaskProcessVO>>，则 elementType应为 BpmTaskProcessVO
        private String complexTypeName; 
    }

    // ------------------------------------------------------------------------
    // 嵌套类 2：RawStructure (用于承载独立定义的一个 VO/DTO/Class)
    // ------------------------------------------------------------------------
    
    /**
     * 代表文档中独立定义的复杂结构（如 表114 BpmTaskProcessVO详细字段）。
     */
    @Data
    public static class RawStructure {
        private String structureName;  // 结构名称，如 BpmTaskProcessVO
        private String description;    // 结构的描述（从表标题或上方段落提取）
        private List<RawField> fields = new ArrayList<>(); // 结构包含的字段列表
    }
}