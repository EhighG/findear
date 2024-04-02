package com.findear.batch.ours.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PoliceAcquiredBoardMatchingDto {

    private String id;

    private String depPlace;

    private String fdFilePathImg;

    private String fdPrdtNm;

    private String fdSbjt;

    private String clrNm;

    private String fdYmd;

    private String mainPrdtClNm;

}
