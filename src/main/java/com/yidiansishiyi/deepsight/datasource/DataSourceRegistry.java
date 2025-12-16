//package com.yidiansishiyi.deepsight.datasource;
//
//import jakarta.annotation.PostConstruct;
//import jakarta.annotation.Resource;
//import org.apache.poi.ss.formula.functions.T;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.Map;
//
///**
// * 数据源注册器
// *
// */
//@Component
//public class DataSourceRegistry {
//
//    @Resource
//    private PostDataSource postDataSource;
//
//    @Resource
//    private UserDataSource userDataSource;
//
//    @Resource
//    private PictureDataSource pictureDataSource;
//
//    private Map<String, DataSource<T>> typeDataSourceMap;
//
//    @Resource
//    private List<DataSource> dataSources;
//
//    @PostConstruct
//    public void doInit() {
//
//    }
//
//    public DataSource getDataSourceByType(String type) {
//        if (typeDataSourceMap == null) {
//            return null;
//        }
//        return typeDataSourceMap.get(type);
//    }
//}
