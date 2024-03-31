package com.findear.batch.ours.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(indexName = "police_matching_log")
public class PoliceMatchingLog {

    @Id
    private Long policeMatchingLogId;

    private Long lostBoardId;

    private Long acquiredBoardId;

    private Float simulerityRate;

    private LocalDateTime matchingAt;

    @Builder
    public PoliceMatchingLog(Long policeMatchingLogId, Long lostBoardId,
                              Long acquiredBoardId, Float simulerityRate) {

        this.policeMatchingLogId = policeMatchingLogId;
        this.lostBoardId = lostBoardId;
        this.acquiredBoardId = acquiredBoardId;
        this.simulerityRate = simulerityRate;
        this.matchingAt = LocalDateTime.now();
    }
}
