package com.yidiansishiyi.deepsight.utils;

import java.util.*;
import java.util.stream.Collectors;

public class MarkdownDocumentBuilder {

//    public String buildMarkdown(List<Map<String, Object>> records) {
//        if (records == null || records.isEmpty()) return "";
//
//        // 1. 按 ID 排序确保时序正确
////        records.sort(Comparator.comparing(r -> Long.parseLong(r.get("id").toString())));
//
//        StringBuilder md = new StringBuilder();
//
//        // 2. 按 fullPath 分组处理（保持排序后的顺序）
//        String currentPath = "";
//
//        for (Map<String, Object> record : records) {
//            String fullPath = (String) record.get("fullPath");
//
//            // 如果路径切换，增加大标题
//            if (fullPath != null && !fullPath.equals(currentPath)) {
//                md.append("\n# ").append(fullPath.replaceAll(" > ", " / ")).append("\n\n");
//                currentPath = fullPath;
//            }
//
//            // 3. 处理标题 (headingPath)
//            String headingPath = (String) record.get("headingPath");
//            if (headingPath != null && headingPath.contains("#")) {
//                String[] parts = headingPath.split("#");
//                int level = Integer.parseInt(parts[0]);
//                level = level > 6 ?  6 : level;
//                String title = parts[1];
//                md.append("#".repeat(level)).append(" ").append(title).append("\n\n");
//            }
//
//            // 4. 处理 methodName (如果是接口调用部分)
//            if (record.get("methodName") != null) {
//                md.append("**接口方法名:** `").append(record.get("methodName")).append("`\n\n");
//            }
//
//            // 5. 处理描述信息
//            if (record.get("description") != null) {
//                md.append(record.get("description")).append("\n\n");
//            }
//
//            // 6. 渲染参数表格 (parameters)
//            List<Map<String, Object>> params = (List<Map<String, Object>>) record.get("parameters");
//            if (params != null && !params.isEmpty()) {
//                md.append("| 参数名称 | 数据类型 |\n| :--- | :--- |\n");
//                for (Map<String, Object> p : params) {
//                    md.append(String.format("| %s | %s |\n", p.get("name"), p.get("javaType")));
//                }
//                md.append("\n");
//            }
//
//            // 7. 渲染返回结构表格 (returnStructure)
//            Map<String, Object> rs = (Map<String, Object>) record.get("returnStructure");
//            if (rs != null) {
//                // 如果这是列表中的第一个返回字段，打印表头
//                md.append("| 字段名称 | 中文名称 | 类型 | 必填 | 说明 |\n");
//                md.append("| :--- | :--- | :--- | :--- | :--- |\n");
//                renderReturnRow(md, rs);
//                md.append("\n");
//            }
//        }
//
//        return md.toString();
//    }

    public String buildMarkdown(List<Map<String, Object>> records) {
        if (records == null || records.isEmpty()) return "";

        // 1. 严格按 ID 排序，确保文档流从上到下
        records.sort(Comparator.comparing(r -> Long.parseLong(r.get("id").toString())));

        StringBuilder md = new StringBuilder();
        String lastPath = "";
        String lastHeading = "";
        boolean isTableActive = false; // 标记当前是否正在写表格

        for (Map<String, Object> record : records) {
            String fullPath = (String) record.get("fullPath");
            String headingPath = (String) record.get("headingPath");

            // 维度 1: 路径切换 (大标题)
            if (fullPath != null && !fullPath.equals(lastPath)) {
                if (isTableActive) { md.append("\n"); isTableActive = false; }
                md.append("\n# ").append(fullPath.trim()).append("\n\n");
                lastPath = fullPath;
            }

            // 维度 2 & 3: 标题切换 (headingPath 解析)
            if (headingPath != null && !headingPath.equals(lastHeading)) {
                if (isTableActive) { md.append("\n"); isTableActive = false; }

                // 解析 "6#返回信息" 这种格式
                String[] parts = headingPath.split("#");
                int parseInt = Integer.parseInt(parts[0]);
                int level = Math.min(parseInt, 6);
                String title = parts[1];
                md.append("#".repeat(level)).append(" ").append(title).append("\n\n");
                lastHeading = headingPath;
            }

            // 处理描述文本 (Description)
            if (record.get("description") != null) {
                md.append(record.get("description")); // 注意：这里不要强行加换行，有些描述是表头说明
            }

            // --- 核心合并逻辑：处理返回参数 (returnStructure) ---
            Map<String, Object> rs = (Map<String, Object>) record.get("returnStructure");
            if (rs != null) {
                // 如果是该标题下的第一个字段，则初始化表头
                if (!isTableActive) {
                    md.append("\n| 字段名称 | 中文名称 | 类型 | 必填 | 说明 |\n");
                    md.append("| :--- | :--- | :--- | :--- | :--- |\n");
                    isTableActive = true;
                }
                // 合并变量：直接追加行
                md.append(String.format("| %s | %s | %s | %s | %s |\n",
                        rs.getOrDefault("name", "-"),
                        rs.getOrDefault("cnName", "-"),
                        rs.getOrDefault("javaType", "-"),
                        rs.getOrDefault("required", "-"),
                        rs.getOrDefault("comment", "-")
                ));
            } else {
                // 如果当前 record 没有表格数据，且之前表格在开启状态，则关闭表格
                if (isTableActive) {
                    md.append("\n");
                    isTableActive = false;
                }
            }

            // 处理请求参数 (parameters)
            List<Map<String, Object>> params = (List<Map<String, Object>>) record.get("parameters");
            if (params != null && !params.isEmpty()) {
                md.append("\n| 参数名称 | 数据类型 |\n| :--- | :--- |\n");
                for (Map<String, Object> p : params) {
                    md.append(String.format("| %s | %s |\n", p.get("name"), p.get("javaType")));
                }
                md.append("\n");
            }
        }
        return md.toString();
    }

    private void renderReturnRow(StringBuilder md, Map<String, Object> rs) {
        md.append(String.format("| %s | %s | %s | %s | %s |\n",
                rs.getOrDefault("name", "-"),
                rs.getOrDefault("cnName", "-"),
                rs.getOrDefault("javaType", "-"),
                rs.getOrDefault("required", "-"),
                rs.getOrDefault("comment", "-")
        ));
    }
}