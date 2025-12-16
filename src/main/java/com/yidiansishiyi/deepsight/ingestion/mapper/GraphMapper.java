package com.yidiansishiyi.deepsight.ingestion.mapper;

import com.yidiansishiyi.deepsight.graph.entity.common.AttributeEntity;
import com.yidiansishiyi.deepsight.graph.entity.common.DataStructureEntity;
import com.yidiansishiyi.deepsight.graph.entity.common.MethodEntity;
import com.yidiansishiyi.deepsight.graph.entity.common.ParameterEntity;
import com.yidiansishiyi.deepsight.ingestion.doc.model.dto.DocxDto;
import com.yidiansishiyi.deepsight.ingestion.doc.model.dto.RawApiData;
import com.yidiansishiyi.deepsight.ingestion.doc.model.dto.RawApiData.RawField;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 负责将解析后的 DTO 转换为 Neo4j 实体对象。
 * 遵循新的图谱设计：Method -> (REQUIRES) -> Parameter
 * Method -> (RETURNS) -> DataStructure -> (HAS_ATTRIBUTE) -> Attribute
 */
@Component
public class GraphMapper {

    /**
     * 将解析器输出的 List<DocxDto> 转换为 Neo4j 需要的 List<MethodEntity>
     * @param docxDtos 包含 RawApiData 的 DTO 列表
     * @return 准备写入图数据库的 MethodEntity 列表
     */
    public List<MethodEntity> mapToMethodEntities(List<DocxDto> docxDtos) {
        return docxDtos.stream()
                .filter(dto -> dto instanceof RawApiData)
                .map(dto -> (RawApiData) dto)
                .map(this::mapToMethodEntity)
                .collect(Collectors.toList());
    }

    /**
     * 将单个 RawApiData 转换为 MethodEntity，并构建其参数和返回结构
     */
    private MethodEntity mapToMethodEntity(RawApiData rawApiData) {
        MethodEntity methodEntity = new MethodEntity();

        // 1. 映射 MethodEntity 基础属性
        methodEntity.setFullPath(rawApiData.getFullPath());
        methodEntity.setMethodName(rawApiData.getMethodName());
        methodEntity.setDescription(rawApiData.getDescription());
        methodEntity.setCommonInterface(rawApiData.getCommonInterface());
        methodEntity.setHeadingPath(rawApiData.getHeadingPath());
        // 2. 映射 REQUIRES 关系 (ParameterEntity)
        if (rawApiData.getInputParams() != null) {
            List<ParameterEntity> params = rawApiData.getInputParams().stream()
                    .map(this::mapToParameterEntity)
                    .collect(Collectors.toList());
            methodEntity.setParameters(params); // 设置 REQUIRES 关系
        }

        if (rawApiData.getReturnField() != null) {
            List<ParameterEntity> params = rawApiData.getReturnField().stream()
                    .map(this::mapToParameterEntity)
                    .collect(Collectors.toList());
            methodEntity.setReturnStructure(params); // 设置 REQUIRES 关系
        }


        // 3. 映射 RETURNS 关系 (DataStructureEntity)
        // 这一步是将扁平的 RawField 列表封装成一个 DataStructureEntity
//        if (rawApiData.getReturnField() != null) {
//            // 创建顶级返回结构并设置属性列表
//            DataStructureEntity returnStructure = mapToReturnDataStructure(
//                    rawApiData.getReturnField(),
//                    "MethodReturnStructure_" + methodEntity.getMethodName() // 给予一个唯一名称
//            );
//            methodEntity.setReturnStructure(returnStructure); // 【关键修正：设置 RETURNS 关系】
//        }

        // 4. 搜索内容整合 (用于 RAG 检索)
        methodEntity.setSearchContent(buildSearchContent(rawApiData));

        return methodEntity;
    }

    /**
     * 将 RawField 列表封装成一个 DataStructureEntity (VO/DTO/Table)
     * @param rawFields 扁平的字段列表 (来自文档表格)
     * @param structureName 结构的名称
     * @return 包含属性的 DataStructureEntity
     */
    private DataStructureEntity mapToReturnDataStructure(List<RawField> rawFields, String structureName) {
        DataStructureEntity structure = new DataStructureEntity();
        structure.setName(structureName);
        structure.setDescription("方法返回的顶级数据结构。"); // 可以根据文档描述进一步完善

        // 将所有 RawField 转换为 AttributeEntity
        List<AttributeEntity> attributes = rawFields.stream()
                .map(this::mapToAttributeEntity)
                .collect(Collectors.toList());

        // 设置 HAS_ATTRIBUTE 关系
        structure.setAttributes(attributes);

        // TODO: 可以在这里添加逻辑来识别 Attributes 中是否存在复杂类型，
        // 并为这些复杂类型创建嵌套的 DataStructureEntity (HAS_NESTED_STRUCTURE)。
        return structure;
    }


    // 从全路径中提取方法短名称
    private String extractMethodShortName(String fullPath) {
        if (fullPath == null) return null;
        int lastDot = fullPath.lastIndexOf('.');
        if (lastDot != -1) {
            return fullPath.substring(lastDot + 1);
        }
        return fullPath;
    }

    // 映射入参 DTO 到 ParameterEntity
    private ParameterEntity mapToParameterEntity(RawField rawField) {
        ParameterEntity param = new ParameterEntity();
        param.setName(rawField.getName());
        param.setCnName(rawField.getCnName());
        param.setJavaType(rawField.getJavaType());
        param.setDbType(rawField.getDbType());
        param.setRequired(rawField.getRequired());
        param.setComment(rawField.getComment());
        param.setComplexTypeName(rawField.getComplexTypeName());
        return param;
    }

    // 映射出参 DTO 到 AttributeEntity
    private AttributeEntity mapToAttributeEntity(RawField rawField) {
        AttributeEntity attribute = new AttributeEntity();
        attribute.setName(rawField.getName());
        attribute.setCnName(rawField.getCnName());
        attribute.setJavaType(rawField.getJavaType());
        attribute.setDbType(rawField.getDbType());
        attribute.setComment(rawField.getComment());
        attribute.setRequired(rawField.getRequired());

        // attribute.setOwnerStructureName(...); // 这个可以在更复杂的解析中设置
        return attribute;
    }

    // 辅助方法：构建搜索内容字符串
    private String buildSearchContent(RawApiData rawApiData) {
        StringBuilder sb = new StringBuilder();
        sb.append(rawApiData.getMethodName()).append(" ");
        sb.append(rawApiData.getFullPath()).append(" ");
        if (rawApiData.getDescription() != null) {
            sb.append(rawApiData.getDescription()).append(" ");
        }
        return sb.toString().trim();
    }
}