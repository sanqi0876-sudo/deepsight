package com.yidiansishiyi.deepsight.ingestion.doc.model.dto;

import lombok.Data;

@Data
public class RawDocxContentDto implements DocxDto{

    private String style;

    private String content;

}
