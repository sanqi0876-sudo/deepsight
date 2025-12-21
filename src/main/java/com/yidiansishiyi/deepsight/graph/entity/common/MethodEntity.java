package com.yidiansishiyi.deepsight.graph.entity.common;

import com.yidiansishiyi.deepsight.annotation.SearchTag;
import com.yidiansishiyi.deepsight.annotation.SearchTheme;
import com.yidiansishiyi.deepsight.graph.repository.Method;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.*;

import java.util.List;

/**
 * 对应图谱中的 (:Method) 节点。
 * 代表一个 API 接口或功能方法。
 */
@SearchTheme(name = "ciis", code = "ciis_doc")
@Node("Method")
@Data
@NoArgsConstructor
public class MethodEntity {

    @Id
    @GeneratedValue
    private Long id;

    @SearchTag("方法名")
    @Property("methodName")
    private String methodName;

    @SearchTag("章节路径")
    @Property("fullPath")
    private String fullPath;

    @Property("description")
    private String description;

    @SearchTag("接口名称")
    @Property("commonInterface")
    private String commonInterface;

    @Property("headingPath")
    private String headingPath; // 对应标题章节 (用于回溯原始文档位置)

    @SearchTag("入参")
    // 关系：[:REQUIRES] 入参，指向 ParameterEntity
    @Relationship(type = "REQUIRES", direction = Relationship.Direction.OUTGOING)
    private List<ParameterEntity> parameters;

    @SearchTag("返回参数")
    @Relationship(type = "RETURNS", direction = Relationship.Direction.OUTGOING)
    private List<ParameterEntity> returnStructure;

    // 关系：[:USES] 入参中使用的复杂结构（例如：getApprovalStatusCommon 使用了 BpmRequestCiisVO）
    @Relationship(type = "USES", direction = Relationship.Direction.OUTGOING)
    private List<DataStructureEntity> usedStructures;

    // 全文搜索内容
    @Property("searchContent")
    private String searchContent;

}