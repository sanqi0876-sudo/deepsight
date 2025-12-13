package com.yidiansishiyi.deepsight.graph.entity.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.*;

/**
 * 对应图谱中的 (:Parameter) 节点。
 * 专用于表示方法调用的入参。
 */
@Node("Parameter")
@Data
@NoArgsConstructor
public class ParameterEntity {

    @Id @GeneratedValue
    private Long id;
    
    @Property("name") // 字段名
    private String name; 
    
    @Property("cnName") // 中文名称
    private String cnName;
    
    @Property("javaType") // Java 类型
    private String javaType;
    
    @Property("required") // 是否必填 (是/否)
    private String required;
    
    @Property("comment") // 备注
    private String comment; 
}