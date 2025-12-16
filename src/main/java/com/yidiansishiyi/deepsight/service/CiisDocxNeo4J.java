package com.yidiansishiyi.deepsight.service;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import com.yidiansishiyi.deepsight.graph.entity.common.DataStructureEntity;
import com.yidiansishiyi.deepsight.graph.entity.common.MethodEntity;
import com.yidiansishiyi.deepsight.graph.entity.common.ParameterEntity;
import com.yidiansishiyi.deepsight.graph.repository.MethodEntityRepository;
import com.yidiansishiyi.deepsight.graph.repository.MethodFullDetailsProjection;
import com.yidiansishiyi.deepsight.graph.repository.MethodRepository;
import com.yidiansishiyi.deepsight.ingestion.doc.model.dto.DocxDto;
import com.yidiansishiyi.deepsight.ingestion.doc.service.DocxParser;
import com.yidiansishiyi.deepsight.ingestion.mapper.GraphMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CiisDocxNeo4J {


    private final DocxParser ciisDocxParser;

    private final GraphMapper graphMapper;

    private final MethodRepository methodRepository;

    private final MethodEntityRepository methodEntityRepository;

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
    public List<MethodEntity> ingestDocxData(InputStream inputStream) {

        System.out.println("--- 1. 启动DOCX文档解析 ---");

        // 1. 调用解析器：获取解析后的 Method 列表
        // 假设 DocxParserImpl.parse() 直接返回 MethodEntity 列表，
        // 如果返回的是 DTO，您需要在这里用 Mapper 转换。
        List<DocxDto> methodsToIngest = ciisDocxParser.parse(inputStream);
        List<MethodEntity> methodEntities = graphMapper.mapToMethodEntities(methodsToIngest);
        if (methodsToIngest.isEmpty()) {
            System.out.println("文档解析器未提取到任何可入库的方法实体。");
            return List.of();
        }

        System.out.printf("--- 2. 解析完成，发现 %d 个接口方法 --- \n", methodsToIngest.size());
        // 3. 调用 Repository 持久化
        // Spring Data Neo4j 会自动处理 MethodEntity 及其关联的 ParameterEntity 和 FieldEntity
        List<MethodEntity> savedMethods = methodRepository.saveAll(methodEntities);

        System.out.printf("--- 3. 数据入库成功，共存储 %d 个方法及其关联数据 --- \n", savedMethods.size());

        return savedMethods;
    }

    public List<MethodEntity> getFullDocumentByMethod(String fullMethodName) {
        return methodEntityRepository.findRelatedMethodsByMethodNameAndSharedFullPath(fullMethodName);
    }


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
    private MethodEntity convertToMethodEntity(MethodFullDetailsProjection projection) {
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

        return (MethodEntity)projection;
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
