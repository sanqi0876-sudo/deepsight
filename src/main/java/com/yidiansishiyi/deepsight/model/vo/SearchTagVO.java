package com.yidiansishiyi.deepsight.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 标签视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchTagVO {
    private String enName; // 字段英文名
    private String cnName; // 注解定义的中文名

    // 重写 equals 和 hashCode 确保在多类合并时 Set 能自动去重
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchTagVO that = (SearchTagVO) o;
        return Objects.equals(enName, that.enName) && Objects.equals(cnName, that.cnName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enName, cnName);
    }
}