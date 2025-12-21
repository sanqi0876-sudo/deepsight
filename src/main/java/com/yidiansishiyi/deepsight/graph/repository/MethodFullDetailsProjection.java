package com.yidiansishiyi.deepsight.graph.repository;

import java.util.List;
import java.util.Map;

public interface MethodFullDetailsProjection {
    Long getId();
    String getMethodName();
    String getFullPath();
    String getHeadingPath();
    String getDescription();
    
    // 这里用 List<Map> 或具体的 DTO 接收 collect 出来的属性
    List<Map<String, Object>> getParameters();
    List<Map<String, Object>> getReturnStructures();
}