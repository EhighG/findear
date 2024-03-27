package com.findear.batch.ours.dto;

import lombok.Data;

@Data
public class MatchingFindearDatasToAiResDto {

    private Long matchingId;

    private Long lostBoardId;

    private Long acquiredBoardId;

    private Float simulerityRate;

}
