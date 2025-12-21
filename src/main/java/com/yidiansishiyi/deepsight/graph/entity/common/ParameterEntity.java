package com.yidiansishiyi.deepsight.graph.entity.common;

import com.yidiansishiyi.deepsight.annotation.SearchTag;
import com.yidiansishiyi.deepsight.annotation.SearchTheme;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.*;

/**
 * 对应图谱中的 (:Parameter) 节点。
 * 专用于表示方法调用的入参。
 */
@SearchTheme(name = "ciis", code = "ciis_doc")
@Node("Parameter")
@Data
@NoArgsConstructor
public class ParameterEntity {

    @Id @GeneratedValue
    private Long id;

    @SearchTag("字段名")
    @Property("name") // 字段名
    private String name;

    @SearchTag("字段中文名称")
    @Property("cnName") // 中文名称
    private String cnName;
    
    @Property("javaType") // Java 类型
    private String javaType;

    @Property("dbType") // Java 类型
    private String dbType;
    
    @Property("required") // 是否必填 (是/否)
    private String required;
    
    @Property("comment") // 备注
    private String comment;

    @Property("complexTypeName") // 特殊类型
    private String complexTypeName;
}