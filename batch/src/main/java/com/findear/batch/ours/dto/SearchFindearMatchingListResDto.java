package com.findear.batch.ours.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
public class SearchFindearMatchingListResDto {

    private Long findearMatchingLogId;

    private Long lostBoardId;

    private Long acquiredBoardId;

    private Float similarityRate;

    private String matchingAt;

    public SearchFindearMatchingListResDto(Long findearMatchingLogId, Long lostBoardId,
                                           Long acquiredBoardId, Float similarityRate, String matchingAt) {
        this.findearMatchingLogId = findearMatchingLogId;
        this.lostBoardId = lostBoardId;
        this.acquiredBoardId = acquiredBoardId;
        this.similarityRate = similarityRate;
        this.matchingAt = matchingAt;
    }
}
