package com.yidiansishiyi.deepsight.utils;

/**
 * 结构化存储 Word 章节标题的节点
 */
public class HeadingItem {
    private final String text;
    private final int level; // 1, 2, 3... 对应 Word 的 Heading1, Heading2...

    public HeadingItem(String text, int level) {
        this.text = text;
        this.level = level;
    }

    public String getText() {
        return text;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return "Level " + level + ": " + text;
    }
}