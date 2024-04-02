package com.findear.batch.ours.dto;

import lombok.Builder;

import java.util.List;

@Builder
public class SearchPoliceBoardMatchingListDto {

    List<SearchPoliceMatchingListResDto> matchingList;

    private int totalCount;
}
