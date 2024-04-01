package com.findear.batch.ours.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SearchBestMatchingListDto {

    List<SearchFindearMatchingListResDto> matchingList;

    private int totalCount;
}
