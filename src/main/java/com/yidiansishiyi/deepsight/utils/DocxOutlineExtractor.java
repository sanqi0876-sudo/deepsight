package com.yidiansishiyi.deepsight.utils;

import cn.hutool.core.io.resource.ResourceUtil;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 专门用于从 DOCX 文档中提取使用 Word 内置样式（Heading1, Heading2等）定义的章节标题。
 */
public class DocxOutlineExtractor {

    // 正则表达式用于匹配 Word 的内置标题样式 ID: "Heading1", "Heading2", ..., "Heading9"
    private static final Pattern HEADING_STYLE_PATTERN = Pattern.compile("Heading(\\d+)");

    /**
     * 从 DOCX 文档流中提取所有 Word 目录标题。
     * @param inputStream DOCX 文件的输入流
     * @return 提取到的结构化标题列表 (HeadingItem)
     */
    public static List<HeadingItem> extractOutlineHeadings(InputStream inputStream) {
        List<HeadingItem> headings = new ArrayList<>();

        try (XWPFDocument document = new XWPFDocument(inputStream)) {

            for (IBodyElement element : document.getBodyElements()) {
                if (element instanceof XWPFParagraph) {
                    XWPFParagraph paragraph = (XWPFParagraph) element;
                    String styleId = paragraph.getStyleID();
                    
                    if (styleId == null) {
                        continue;
                    }

                    Matcher matcher = HEADING_STYLE_PATTERN.matcher(styleId);

                    if (matcher.find()) {
                        // 提取匹配到的标题级别（例如 "Heading1" -> 1）
                        int level = Integer.parseInt(matcher.group(1));
                        String text = paragraph.getText().trim();

                        if (!text.isEmpty()) {
                            headings.add(new HeadingItem(text, level));
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("提取 DOCX 目录标题失败: " + e.getMessage());
            e.printStackTrace();
        }

        return headings;
    }

    // =================================================================
    // 测试方法
    // =================================================================
    public static void main(String[] args) {
        DocxOutlineExtractor extractor = new DocxOutlineExtractor();

        // 替换为您的测试文件名
        String testFileName = "ciis/doc/4.第四章-交易对手信息.docx";

        try (InputStream testFileStream = ResourceUtil.getStream(testFileName)) {

            if (testFileStream == null) {
                System.err.println("错误: 未找到文件 " + testFileName + "。请检查路径是否正确。");
                return;
            }

            List<HeadingItem> headings = extractor.extractOutlineHeadings(testFileStream);

            System.out.println("--- 从文档中提取的 Word 目录标题 ---");
            for (HeadingItem item : headings) {
                // 打印格式化后的标题，更容易看出层级
                System.out.printf("%s%s\n", "  ".repeat(item.getLevel() - 1), item.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}