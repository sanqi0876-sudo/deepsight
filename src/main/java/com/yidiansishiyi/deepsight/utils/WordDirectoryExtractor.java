package com.yidiansishiyi.deepsight.utils;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import com.yidiansishiyi.deepsight.ingestion.doc.model.dto.DocxDto;
import com.yidiansishiyi.deepsight.ingestion.doc.model.dto.RawDocxContentDto;
import org.apache.poi.xwpf.usermodel.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WordDirectoryExtractor {

    public static List<RawDocxContentDto> extractDirectoryStructure(String filePath) {
        InputStream stream = ResourceUtil.getStream(filePath);
        return extractDirectoryStructure(stream);
    }

    public static List<RawDocxContentDto> extractDirectoryStructure(InputStream stream) {
        List<RawDocxContentDto> strings = null;
        try (XWPFDocument document = new XWPFDocument(stream)) {
            strings = new ArrayList<>();
            List<IBodyElement> elements = document.getBodyElements();
            for (IBodyElement element : elements) {
                RawDocxContentDto rawDocxContentDto = new RawDocxContentDto();
                if (element instanceof XWPFParagraph) {
                    String style = ((XWPFParagraph) element).getStyle() == null ? DocxDto.DOCX_TYPE_TXT : ((XWPFParagraph) element).getStyle() ;
                    String text = ((XWPFParagraph) element).getText();
                    rawDocxContentDto.setStyle(style);
                    rawDocxContentDto.setContent(text);
                } else if (element instanceof XWPFTable) {
                    rawDocxContentDto.setStyle(DocxDto.DOCX_TYPE_TABLE);
                    XWPFTable table = (XWPFTable) element;
                    List<Map<String, String>> maps = convertTableToListOfMaps(table);
                    String jsonStr = JSONUtil.toJsonStr(maps);
                    rawDocxContentDto.setContent(jsonStr);
                }
                strings.add(rawDocxContentDto);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strings;
    }

    /**
     * 将 XWPFTable 转换为 List<Map<String, String>> 的工具方法。
     * 假设表格第一行是 Map 的 Key (标题行)。
     * * @param table 要转换的 XWPFTable 对象
     *
     * @return 结构化的 List Map 数据
     */
    public static List<Map<String, String>> convertTableToListOfMaps(XWPFTable table) {
        List<Map<String, String>> tableData = new ArrayList<>();

        // 检查表格是否为空
        if (table == null || table.getRows().isEmpty()) {
            return tableData;
        }

        // 1. 提取标题行 (Keys)
        XWPFTableRow headerRow = table.getRow(0);
        List<String> headers = headerRow.getTableCells().stream()
                .map(cell -> cell.getText().trim())
                .collect(Collectors.toList());

        // 2. 遍历数据行 (从索引 1 开始)
        for (int i = 1; i < table.getRows().size(); i++) {
            XWPFTableRow dataRow = table.getRow(i);
            List<XWPFTableCell> cells = dataRow.getTableCells();

            Map<String, String> rowMap = new HashMap<>();

            // 3. 遍历单元格，将 (Header -> CellText) 存入 Map
            // 注意：这里需要处理数据行可能比标题行短的情况
            for (int j = 0; j < headers.size() && j < cells.size(); j++) {
                String header = headers.get(j);
                String cellValue = cells.get(j).getText().trim();

                // 只有当标题不为空时才存入 Map (避免空列头)
                if (!header.isEmpty()) {
                    rowMap.put(header, cellValue);
                }
            }

            // 只有当 Map 非空时，才认为是一条有效数据
            if (!rowMap.isEmpty()) {
                tableData.add(rowMap);
            }
        }

        return tableData;
    }

    public static void main(String[] args) {
        WordDirectoryExtractor extractor = new WordDirectoryExtractor();
        extractor.extractDirectoryStructure("path/to/your/document.docx");
    }
}