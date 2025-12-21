package com.yidiansishiyi.deepsight.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 聚合搜索
 */
@Data
public class SearchVO implements Serializable {

    List<SearchThemeVO> searchThemeVOList;

    Object sendBody;

    private static final long serialVersionUID = 1L;

}
