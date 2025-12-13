package com.yidiansishiyi.deepsight.ingestion.doc.model.dto;

import java.util.Arrays;
import java.util.List;

public interface DocxDto {

    String DOCX_TYPE_TABLE = "table";

    String DOCX_TYPE_TXT = "txt";

    List<String> INTER_FLAG = Arrays.asList("调用接口");

    List<String> REQUEST_FLAG = Arrays.asList("请求", "调用", "上行", "查询", "获取", "读取", "下载", "上传", "写入", "删除", "新增", "修改", "更新", "替换", "插入", "添加", "提交", "提交表单", "提交数据", "提交请求", "提交参数", "提交信息", "提交内容", "提交对象", "提交记录", "提交列表", "提交集合", "提交");

    List<String> RESPONSE_FLAG = Arrays.asList("响应", "返回", "返回值", "返回结果", "返回数据", "返回内容", "返回信息", "返回对象", "返回记录", "返回列表", "返回集合", "返回数组", "返回字符串", "返回数字", "返回布尔值", "返回日期", "返回时间", "返回文件", "返回图片", "返回视频", "返回音频", "返回文本");

}
