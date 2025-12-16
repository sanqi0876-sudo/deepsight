// File: MethodFullDetailsProjection.java

package com.yidiansishiyi.deepsight.graph.repository;

import com.yidiansishiyi.deepsight.graph.entity.common.DataStructureEntity;
import com.yidiansishiyi.deepsight.graph.entity.common.ParameterEntity;
import java.util.List;
import java.util.Map;

// 投影接口：用于接收复杂 Cypher 查询的返回结果
public interface MethodFullDetailsProjection {
    
    // 对应 RETURN 语句中的 m2.methodName
    String getMethodName();
    
    // 对应 RETURN 语句中的 m2.fullPath
    String getFullPath();

    // 对应 RETURN 语句中的 m2 {.*} (投影为 Map<String, Object>)
    Map<String, Object> getMethodDetails();

    // 对应 RETURN 语句中的 COLLECT(DISTINCT p)
    List<ParameterEntity> getInputParameters();
    
    // 对应 RETURN 语句中的 rs AS ReturnStructure
    DataStructureEntity getReturnStructure();
    
    // 对应 RETURN 语句中的 COLLECT(DISTINCT us)
    List<DataStructureEntity> getUsedStructures();
}