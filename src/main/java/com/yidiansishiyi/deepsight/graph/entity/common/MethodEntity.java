package com.yidiansishiyi.deepsight.graph.entity.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.*;

import java.util.List;

/**
 * 对应图谱中的 (:Method) 节点。
 * 代表一个 API 接口或功能方法。
 */
@Node("Method") 
@Data
@NoArgsConstructor
public class MethodEntity {

    @Id @GeneratedValue 
    private Long id;
    
    @Property("methodName") 
    private String methodName; 

    @Property("fullPath")
    private String fullPath; 
    
    @Property("description")
    private String description;

    @Property("commonInterface")
    private String commonInterface;

    @Property("headingPath")
    private String headingPath; // 对应标题章节 (用于回溯原始文档位置)

    // 关系：[:REQUIRES] 入参，指向 ParameterEntity
    @Relationship(type = "REQUIRES", direction = Relationship.Direction.OUTGOING)
    private List<ParameterEntity> parameters;

    @Relationship(type = "RETURNS", direction = Relationship.Direction.OUTGOING)
    private List<ParameterEntity> returnStructure;

    // 关系：[:RETURNS] 出参，指向 DataStructureEntity (顶级返回结构)
    // 例如：返回 Map<String, List<BpmTaskProcessVO>> 对应的 DataStructureEntity
//    @Relationship(type = "RETURNS", direction = Relationship.Direction.OUTGOING)
//    private DataStructureEntity returnStructure;
    
    // 关系：[:USES] 入参中使用的复杂结构（例如：getApprovalStatusCommon 使用了 BpmRequestCiisVO）
    @Relationship(type = "USES", direction = Relationship.Direction.OUTGOING)
    private List<DataStructureEntity> usedStructures;
    
    // 全文搜索内容
    @Property("searchContent")
    private String searchContent;
}