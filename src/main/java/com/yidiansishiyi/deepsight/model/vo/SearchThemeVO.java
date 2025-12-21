package com.yidiansishiyi.deepsight.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 主题视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchThemeVO {
    private String themeName; // 主题展示名
    private String themeCode; // 主题唯一标识
    private Set<SearchTagVO> tags; // 标签集合 (使用 Set 自动去重)
}