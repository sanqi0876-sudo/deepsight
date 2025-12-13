package com.yidiansishiyi.deepsight.graph.entity.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.*;

import java.util.List;

/**
 * 对应图谱中的 (:DataStructure) 节点。
 * 代表 VO, DTO, Java Class 或未来的数据库 Table。
 */
@Node("DataStructure")
@Data
@NoArgsConstructor
public class DataStructureEntity {

    @Id @GeneratedValue
    private Long id;
    
    // 结构名称，例如：BpmTaskProcessVO
    @Property("name") 
    private String name; 

    // 结构描述或所属文档的引用
    @Property("description")
    private String description;
    
    // 关系：[:HAS_ATTRIBUTE] 指向简单原子属性
    @Relationship(type = "HAS_ATTRIBUTE", direction = Relationship.Direction.OUTGOING)
    private List<AttributeEntity> attributes;

    // 关系：[:HAS_NESTED_STRUCTURE] 指向另一个复杂结构
    // 例如：BpmTaskProcessVO 包含 TradeInspectionAndApprovalResultSpecialVO
    @Relationship(type = "HAS_NESTED_STRUCTURE", direction = Relationship.Direction.OUTGOING)
    private List<DataStructureEntity> nestedStructures;
    
    // 如果这个结构是作为另一个结构的一个字段存在，我们需要知道它在那里的字段名
    @Property("fieldNameInParent")
    private String fieldNameInParent;
    
    // Java List/Map 等泛型信息
    @Property("genericInfo") 
    private String genericInfo; 
}