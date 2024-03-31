package com.findear.batch.ours.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class SearchFindearMatchingListResDto {

    private Long findearMatchingLogId;

    private Long lostBoardId;

    private Long acquiredBoardId;

    private Float simulerityRate;

    private LocalDateTime matchingAt;
}
