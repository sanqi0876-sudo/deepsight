// File: MethodEntityRepository.java

package com.yidiansishiyi.deepsight.graph.repository;

import com.yidiansishiyi.deepsight.graph.entity.common.MethodEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Repository
public interface MethodEntityRepository extends Neo4jRepository<MethodEntity, Long> {

    /**
     * 根据 methodName 找到目标节点，提取 fullPath，再查找所有共享该 fullPath 的兄弟节点及其全部关联信息。
     * @param targetMethodName 目标方法的唯一标识 (com.comstar...)
     * @return 共享同一 fullPath 的所有 Method 节点的详细关联信息列表。
     */
    @Query("""
        MATCH (m1:Method)
        WHERE m1.methodName =~ ('(?i).*' + $targetMethodName + '(?i).*')
        WITH DISTINCT m1.fullPath AS targetFullPathValue
        MATCH (m2:Method)
        WHERE m2.fullPath = targetFullPathValue
        OPTIONAL MATCH (m2)-[:REQUIRES]->(p:Parameter)
        OPTIONAL MATCH (m2)-[:RETURNS]->(rs:DataStructure)
        OPTIONAL MATCH (m2)-[:USES]->(us:DataStructure)
        RETURN
            m2.methodName AS methodName,          // 映射到 Projection 的 getMethodName()
            m2.fullPath AS fullPath,        // 映射到 Projection 的 getSharedFullPath()
            m2.headingPath AS headingPath,
            COLLECT(DISTINCT p) AS parameters
    """)
    List<MethodEntity> findRelatedMethodsByMethodNameAndSharedFullPath(String targetMethodName);
}