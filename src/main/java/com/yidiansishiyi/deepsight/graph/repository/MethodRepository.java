package com.yidiansishiyi.deepsight.graph.repository;

import com.yidiansishiyi.deepsight.graph.entity.common.MethodEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data Neo4j 接口，用于MethodEntity的CRUD操作
 */
@Repository
public interface MethodRepository extends Neo4jRepository<MethodEntity, Long> {
}