// File: MethodFullDetailsProjection.java

package com.yidiansishiyi.deepsight.graph.repository;

import com.yidiansishiyi.deepsight.graph.entity.common.DataStructureEntity;
import com.yidiansishiyi.deepsight.graph.entity.common.ParameterEntity;

import java.util.List;
import java.util.Map;

// 投影接口：用于接收复杂 Cypher 查询的返回结果
public interface Method {

    Long getId();

    String getMethodName();

    String getFullPath();

    String getDescription();

    String getCommonInterface();

    String getHeadingPath();

    List<ParameterEntity> getParameters();

    List<ParameterEntity> getReturnStructure();

    List<DataStructureEntity> getUsedStructures();

    String getSearchContent();

}