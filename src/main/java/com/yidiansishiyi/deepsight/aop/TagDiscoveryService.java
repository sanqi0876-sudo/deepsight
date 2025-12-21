package com.yidiansishiyi.deepsight.aop;

import cn.hutool.core.collection.CollUtil;
import com.yidiansishiyi.deepsight.annotation.SearchTag;
import com.yidiansishiyi.deepsight.annotation.SearchTheme;
import com.yidiansishiyi.deepsight.model.vo.SearchTagVO;
import com.yidiansishiyi.deepsight.model.vo.SearchThemeVO;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TagDiscoveryService {

    public static List<SearchThemeVO> searchThemeVOList;

    @PostConstruct
    void initSearchThemeVOList() {
        searchThemeVOList = getAllThemesAndTags();
    }

//    public List<String> getTagList(List<String> searchTextList) {
//        if (CollUtil.isEmpty(searchThemeVOList) || CollUtil.isEmpty(searchTextList)) {
//            return Collections.emptyList();
//        }
//        searchTextList.stream()
//                .filter(searchTextList.contains(SearchThemeVO::get))
//    }

    /**
     * 获取所有主题和标签
     *
     * @return
     */
    public List<SearchThemeVO> getAllThemesAndTags() {
        // 使用 Map 暂存，以便合并相同 themeCode 的不同类
        Map<String, SearchThemeVO> themeContainer = new LinkedHashMap<>();

        ClassPathScanningCandidateComponentProvider provider =
                new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(SearchTheme.class));

        // 扫描包路径
        Set<BeanDefinition> components = provider.findCandidateComponents("com.yidiansishiyi.deepsight.graph.entity.common");

        for (BeanDefinition component : components) {
            try {
                Class<?> clazz = Class.forName(component.getBeanClassName());
                SearchTheme themeAnno = clazz.getAnnotation(SearchTheme.class);
                String code = themeAnno.code();

                // 1. 获取或初始化主题对象
                SearchThemeVO themeVO = themeContainer.computeIfAbsent(code, k -> SearchThemeVO.builder()
                        .themeName(themeAnno.name())
                        .themeCode(code)
                        .tags(new LinkedHashSet<>()) // 保证顺序且去重
                        .build());

                // 2. 扫描字段并加入标签集合
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.isAnnotationPresent(SearchTag.class)) {
                        SearchTag tagAnno = field.getAnnotation(SearchTag.class);
                        SearchTagVO tagVO = SearchTagVO.builder()
                                .enName(field.getName())
                                .cnName(tagAnno.value())
                                .build();
                        themeVO.getTags().add(tagVO);
                    }
                }
            } catch (Exception e) {
                // 线上环境建议使用 log.error
                e.printStackTrace();
            }
        }
        return new ArrayList<>(themeContainer.values());
    }
}