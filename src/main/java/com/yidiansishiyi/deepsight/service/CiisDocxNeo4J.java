package com.yidiansishiyi.deepsight.service;

import cn.hutool.core.io.resource.ResourceUtil;
import com.yidiansishiyi.deepsight.graph.entity.common.MethodEntity;
import com.yidiansishiyi.deepsight.graph.repository.*;
import com.yidiansishiyi.deepsight.ingestion.doc.model.dto.DocxDto;
import com.yidiansishiyi.deepsight.ingestion.doc.service.DocxParser;
import com.yidiansishiyi.deepsight.ingestion.mapper.GraphMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CiisDocxNeo4J {


    private final DocxParser ciisDocxParser;

    private final GraphMapper graphMapper;

    private final MethodRepository methodRepository;

//    private final MethodDocumentRepository methodDocumentRepository;
//
//    private final MethodDocumentRepository methodEntityRepository;

//    @PostConstruct
    void setUp() {
        // 用于测试时加载文件
        String testFileName = "ciis/doc/1.第一章-基础数据.docx";
        InputStream testFileStream = ResourceUtil.getStream(testFileName);
        ingestDocxData(testFileStream);
    }

    /**
     * 执行DOCX文档的完整解析、实体转换和图谱入库操作。
     *
     * @param inputStream DOCX文档的输入流
     * @return 成功入库的方法实体列表
     */
    @Transactional // 保证整个导入过程要么成功，要么全部回滚
    public List<com.yidiansishiyi.deepsight.graph.entity.common.MethodEntity> ingestDocxData(InputStream inputStream) {

        System.out.println("--- 1. 启动DOCX文档解析 ---");

        // 1. 调用解析器：获取解析后的 Method 列表
        // 假设 DocxParserImpl.parse() 直接返回 MethodEntity 列表，
        // 如果返回的是 DTO，您需要在这里用 Mapper 转换。
        List<DocxDto> methodsToIngest = ciisDocxParser.parse(inputStream);
        List<com.yidiansishiyi.deepsight.graph.entity.common.MethodEntity> methodEntities = graphMapper.mapToMethodEntities(methodsToIngest);
        if (methodsToIngest.isEmpty()) {
            System.out.println("文档解析器未提取到任何可入库的方法实体。");
            return List.of();
        }

        System.out.printf("--- 2. 解析完成，发现 %d 个接口方法 --- \n", methodsToIngest.size());
        // 3. 调用 Repository 持久化
        // Spring Data Neo4j 会自动处理 MethodEntity 及其关联的 ParameterEntity 和 FieldEntity
        List<com.yidiansishiyi.deepsight.graph.entity.common.MethodEntity> savedMethods = methodRepository.saveAll(methodEntities);

        System.out.printf("--- 3. 数据入库成功，共存储 %d 个方法及其关联数据 --- \n", savedMethods.size());

        return savedMethods;
    }

    @Autowired
    private Neo4jClient neo4jClient; // 注入这个底层客户端

    public List<Map<String, Object>> getFullDocumentByMethod(String targetMethodName) {
        String cypher = """
            MATCH (m1:Method)
            WHERE m1.methodName =~ ('(?i).*' + $name + '.*')
            WITH DISTINCT m1.fullPath AS targetPath
            MATCH (m2:Method)
            WHERE m2.fullPath = targetPath
            OPTIONAL MATCH (m2)-[:REQUIRES]->(p:Parameter)
            OPTIONAL MATCH (m2)-[:RETURNS]->(rs:Parameter)
            RETURN 
                id(m2) AS id,
                m2.methodName AS methodName,
                m2.fullPath AS fullPath,
                m2.headingPath AS headingPath,
                m2.description AS description,
                collect(DISTINCT p {.*}) AS parameters,
                rs {.*} AS returnStructure
        """;

        // 直接获取原始 Map 列表，没有任何中间商赚差价，绝对不会再有 null
        return (List<Map<String, Object>>) neo4jClient.query(cypher)
                .bind(targetMethodName).to("name")
                .fetch()
                .all();
    }



//    public List<MethodFullDetailsProjection> getFullDocumentByMethod(String fullMethodName) {
//        return methodDocumentRepository.findFullDetailsByMethodName(fullMethodName);
//    }


//    /**
//     * 搜索方法并转换为 MethodEntity 列表
//     */
//    public List<MethodEntity> searchMethodsAsEntities(String searchText) {
//        List<MethodFullDetailsProjection> projections =
//                getFullDocumentByMethod(searchText);
//
//        return projections.stream()
//                .map(this::convertToMethodEntity)
//                .collect(Collectors.toList());
//    }

    /**
     * 将投影接口转换为 MethodEntity
     */
    private com.yidiansishiyi.deepsight.graph.entity.common.MethodEntity convertToMethodEntity(Method projection) {
//        MethodEntity methodEntity = new MethodEntity();

//        // 从 methodDetails Map 中设置基本属性
//        MethodEntity methodEntity = projection.getMethodDetails();
//
//        // 设置参数列表
//        List<ParameterEntity> parameters = projection.getInputParameters();
//        if (parameters != null && !parameters.isEmpty()) {
//            methodEntity.setParameters(new ArrayList<>(parameters));
//        }
//
//        // 设置返回结构
//        DataStructureEntity returnStructure = projection.getReturnStructure();
//        if (returnStructure != null) {
//            // 注意：这里需要根据你的实际关系结构调整
//            // 由于 MethodEntity 的 returnStructure 是 DataStructureEntity 类型
//            // 而 projection 返回的是 DataStructureEntity
//            // 可以转换为 ParameterEntity 或调整实体类定义
//            ParameterEntity param = new ParameterEntity();
//            param.setName(returnStructure.getName());
//            // 其他字段转换...
//            List<ParameterEntity> returnParams = new ArrayList<>();
//            returnParams.add(param);
//            methodEntity.setReturnStructure(returnParams);
//        }
//
//        // 设置使用的数据结构
//        List<DataStructureEntity> usedStructures = projection.getUsedStructures();
//        if (usedStructures != null && !usedStructures.isEmpty()) {
//            methodEntity.setUsedStructures(new ArrayList<>(usedStructures));
//        }

        return (com.yidiansishiyi.deepsight.graph.entity.common.MethodEntity)projection;
    }


//    public void exampleUsage() {
//        String target = "com.comstar.cnp.service.interfaces.ISecuritySearchToFbsService.getSecurityInfo";
//        List<MethodFullDetailsProjection> results = getFullDocumentByMethod(target);
//
//        if (!results.isEmpty()) {
//            System.out.println("Found " + results.size() + " related document sections.");
//        }
//    }
}
