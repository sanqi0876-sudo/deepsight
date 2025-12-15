package com.yidiansishiyi.deepsight.graph.entity.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.*;

/**
 * 对应图谱中的 (:Attribute) 节点。
 * 代表一个数据结构或方法中的最小数据项（字段、列、属性）。
 */
@Node("Attribute")
@Data
@NoArgsConstructor
public class AttributeEntity {

    @Id @GeneratedValue // 节点ID，由Neo4j自动生成
    private Long id;
    
    @Property("name") // 字段代码，如 serialNumber
    private String name; 
    
    @Property("cnName") // 中文名称，如 审批单号
    private String cnName;
    
    @Property("javaType") // Java 类型，如 String
    private String javaType;

    @Property("dbType") // 数据库类型，如 VARCHAR2(64)
    private String dbType;

    @Property("comment") // 备注信息，用于全文搜索
    private String comment;

    @Property("required") // 是否必填 (仅入参使用)
    private String required;
    
    // 字段所在的复杂结构名称（用于反查，辅助Mapper）
    @Property("ownerStructureName")
    private String ownerStructureName; 
}