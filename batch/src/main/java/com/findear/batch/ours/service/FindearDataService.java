package com.findear.batch.ours.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.findear.batch.common.exception.FindearException;
import com.findear.batch.ours.domain.AcquiredBoard;
import com.findear.batch.ours.domain.FindearMatchingLog;
import com.findear.batch.ours.domain.LostBoard;
import com.findear.batch.ours.dto.*;
import com.findear.batch.ours.repository.AcquiredBoardRepository;
import com.findear.batch.ours.repository.FindearMatchingLogRepository;
import com.findear.batch.ours.repository.LostBoardRepository;
import com.findear.batch.police.domain.PoliceAcquiredData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class FindearDataService {

    private final FindearMatchingLogRepository findearMatchingLogRepository;
    private final LostBoardRepository lostBoardRepository;
    private final AcquiredBoardRepository acquiredBoardRepository;
    private final RestHighLevelClient restHighLevelClient;

    public List<MatchingFindearDatasToAiResDto> matchingFindearDatasBatch() {

        try {

            // 찾아지지 않은 분실물 게시글 모두 조회
            List<LostBoard> lostBoardList = lostBoardRepository.findAllWithBoardByStatusOngoing();

            for(LostBoard l : lostBoardList) {

                // 분실물 게시글 정보
                LostBoardMatchingDto lostBoardMatchingDto = LostBoardMatchingDto.builder()
                        .lostBoardId(l.getId().toString())
                        .productName(l.getBoard().getProductName())
                        .color(l.getBoard().getColor())
                        .categoryName(l.getBoard().getCategoryName())
                        .description(l.getBoard().getAiDescription())
                        .lostAt(l.getLostAt().toString())
                        .xPos(l.getXPos().toString())
                        .yPos(l.getYPos().toString()).build();

                LocalDate dateTime = LocalDate.parse(lostBoardMatchingDto.getLostAt(), DateTimeFormatter.ISO_DATE);

                // 카테고리가 같고, 분실 일자 이후에 등록된 게시글 전송
                List<AcquiredBoard> acquiredBoardList = acquiredBoardRepository
                        .findAllWithBoardByCategoryAndAfterLostAt(lostBoardMatchingDto.getCategoryName(),
                                dateTime.atStartOfDay());

                // request dto 생성
                MatchingFindearDatasToAiReqDto matchingFindearDatasToAiReqDto = MatchingFindearDatasToAiReqDto
                        .builder().lostBoard(lostBoardMatchingDto).acquiredBoardList(new ArrayList<>()).build();

                for(AcquiredBoard ab : acquiredBoardList) {
                    matchingFindearDatasToAiReqDto.getAcquiredBoardList()
                            .add(AcquiredBoardMatchingDto.builder()
                                    .acquiredBoardId(ab.getId().toString())
                                    .productName(ab.getBoard().getProductName())
                                    .color(ab.getBoard().getColor())
                                    .categoryName(ab.getBoard().getCategoryName())
                                    .description(ab.getBoard().getAiDescription())
                                    .xPos(ab.getXPos().toString())
                                    .yPos(ab.getYPos().toString())
                                    .registeredAt(ab.getBoard().getRegisteredAt().toString())
                                    .build());
                }

                // ai 서버로 요청
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

                HttpEntity<?> requestEntity = new HttpEntity<>(matchingFindearDatasToAiReqDto, headers);

                String serverURL = "https://j10a706.p.ssafy.io/match/matching/findear";

                RestTemplate restTemplate = new RestTemplate();

                ResponseEntity<Map> response = restTemplate.postForEntity(serverURL, requestEntity, Map.class);

                System.out.println("response : " + response.getBody());

            }

            return null;

        } catch (Exception e) {
            throw new FindearException(e.getMessage());
        }
    }
    public List<MatchingFindearDatasToAiResDto> matchingFindearDatas(LostBoardMatchingDto lostBoardMatchingDto) {

        try {

            log.info("분실물 매칭 service");
            // request dto 생성
            MatchingFindearDatasToAiReqDto matchingFindearDatasToAiReqDto = MatchingFindearDatasToAiReqDto
                    .builder().lostBoard(lostBoardMatchingDto).acquiredBoardList(new ArrayList<>()).build();

            log.info("request dto 생성 완료");

            LocalDate dateTime = LocalDate.parse(lostBoardMatchingDto.getLostAt(), DateTimeFormatter.ISO_DATE);

            // 카테고리가 같고, 분실 일자 이후에 등록된 게시글 전송
            List<AcquiredBoard> acquiredBoardList = acquiredBoardRepository
                    .findAllWithBoardByCategoryAndAfterLostAt(lostBoardMatchingDto.getCategoryName(), dateTime.atStartOfDay());

            log.info("카테고리가 같고, 분실 일자 이후에 등록된 게시글 전송");
            for(AcquiredBoard ab : acquiredBoardList) {

                matchingFindearDatasToAiReqDto.getAcquiredBoardList()
                        .add(AcquiredBoardMatchingDto.builder()
                                .acquiredBoardId(ab.getBoard().getId().toString())
                                .productName(ab.getBoard().getProductName())
                                .color(ab.getBoard().getColor())
                                .categoryName(ab.getBoard().getCategoryName())
                                .description(ab.getBoard().getAiDescription())
                                .xPos(ab.getXPos().toString())
                                .yPos(ab.getYPos().toString())
                                .registeredAt(ab.getBoard().getRegisteredAt().toString())
                                .build());
            }

            log.info("들어온 데이터 : " + matchingFindearDatasToAiReqDto.toString());

            log.info("ai 서버로 요청");
            // ai 서버로 요청
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

            HttpEntity<?> requestEntity = new HttpEntity<>(matchingFindearDatasToAiReqDto, headers);

            String serverURL = "https://j10a706.p.ssafy.io/match/matching/findear";

            RestTemplate restTemplate = new RestTemplate();

            log.info("요청된 데이터 : " + requestEntity.getBody());
            ResponseEntity<Map> response = restTemplate.postForEntity(serverURL, requestEntity, Map.class);

            List<Map<String, Object>> resultList = (List<Map<String, Object>> ) response.getBody().get("result");

            if(resultList == null) {

                return null;
            }
            log.info("매칭 결과 : " + response.getBody());

            List<MatchingFindearDatasToAiResDto> result = new ArrayList<>();
            List<FindearMatchingLog> findearMatchingLogList = new ArrayList<>();

            Long findearMatchingId = findearMatchingLogRepository.count() + 1;
            System.out.println("아이디 : " + findearMatchingId);

            for(Map<String, Object> res : resultList) {

                MatchingFindearDatasToAiResDto matchingFindearDatasToAiResDto = MatchingFindearDatasToAiResDto.builder()
                        .lostBoardId(res.get("lostBoardId"))
                        .acquiredBoardId(res.get("acquiredBoardId"))
                        .similarityRate(res.get("similarityRate")).build();

                result.add(matchingFindearDatasToAiResDto);

                FindearMatchingLog newFindearMatchingLog = FindearMatchingLog.builder()
                        .findearMatchingLogId(findearMatchingId++)
                        .lostBoardId(Long.parseLong(String.valueOf(matchingFindearDatasToAiResDto.getLostBoardId())))
                        .acquiredBoardId(Long.parseLong(String.valueOf(matchingFindearDatasToAiResDto.getAcquiredBoardId())))
                        .similarityRate(Float.parseFloat(String.valueOf(matchingFindearDatasToAiResDto.getSimilarityRate())))
                        .matchingAt(LocalDateTime.now().toString())
                        .build();

                findearMatchingLogList.add(newFindearMatchingLog);
            }

            findearMatchingLogRepository.saveAll(findearMatchingLogList);

            return result;

        } catch (Exception e) {

            throw new FindearException(e.getMessage());
        }
    }

    public SearchBoardMatchingList searchBoardMatchingList(int page, int size, Long lostBoardId) {

        try {
            LostBoard findLostBoard = lostBoardRepository.findById(lostBoardId)
                    .orElseThrow(() -> new FindearException("해당 분실물이 존재하지 않습니다."));

            List<SearchFindearMatchingListResDto> boardMatchingList = new ArrayList<>();

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(QueryBuilders.matchQuery("lostBoardId", lostBoardId));

            // 검색 요청 생성
            SearchRequest searchRequest = new SearchRequest("findear_matching_log");
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(boolQueryBuilder);
            searchSourceBuilder.sort(SortBuilders.fieldSort("similarityRate").order(SortOrder.DESC)); // similarityRate 내림차순으로 정렬
            searchRequest.source(searchSourceBuilder);

            SearchHits hits;
            try {
                // 검색 실행
                hits = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT).getHits();
            } catch (IOException e) {
                throw new FindearException(e.getMessage());
            }

            // 검색 결과를 리스트에 추가
            for (SearchHit hit : hits) {
                Long findearMatchingLogId = Long.parseLong(hit.getSourceAsMap().get("findearMatchingLogId").toString());
                Long acquiredBoardId = Long.parseLong(hit.getSourceAsMap().get("acquiredBoardId").toString());
                Float similarityRate = Float.parseFloat(hit.getSourceAsMap().get("similarityRate").toString());
                String matchingAt = (String) hit.getSourceAsMap().get("matchingAt");

                SearchFindearMatchingListResDto dto = new SearchFindearMatchingListResDto(
                        findearMatchingLogId, lostBoardId, acquiredBoardId, similarityRate, matchingAt
                );

                boardMatchingList.add(dto);
            }

            int from = (page - 1) * size;
            int to = page * size;

            if(to > boardMatchingList.size()) {
                to = boardMatchingList.size();
            }

            List<SearchFindearMatchingListResDto> result = new ArrayList<>();
            for(int i=from; i<to; i++) {
                result.add(boardMatchingList.get(i));
            }

            SearchBoardMatchingList searchBoardMatchingList = SearchBoardMatchingList.builder()
                    .matchingList(result)
                    .totalCount(boardMatchingList.size()).build();

            return searchBoardMatchingList;

        } catch (Exception e) {

            throw new FindearException(e.getMessage());
        }
    }

    public SearchBestMatchingListDto searchBestMatchingList(int page, int size, Long memberId) {

        try {

            List<LostBoard> lostBoardList = lostBoardRepository.findAllWithBoardByMemberId(memberId);
            List<Long> lostBoardMatchingIds = new ArrayList<>();

            for (LostBoard lb : lostBoardList) {
                lostBoardMatchingIds.add(lb.getId());
            }

            List<SearchFindearMatchingListResDto> bestMatchesList = new ArrayList<>();

            for (Long id : lostBoardMatchingIds) {
                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                boolQueryBuilder.must(QueryBuilders.matchQuery("lostBoardId", id));

                // 검색 요청 생성
                SearchRequest searchRequest = new SearchRequest("findear_matching_log");
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                searchSourceBuilder.query(boolQueryBuilder);
                searchSourceBuilder.size(1); // 각 "lostBoardId" 별로 가장 높은 similarityRate를 가진 1개의 문서만 가져오기 위해 size를 1로 설정
                searchSourceBuilder.sort(SortBuilders.fieldSort("similarityRate").order(SortOrder.DESC)); // similarityRate 내림차순으로 정렬
                searchRequest.source(searchSourceBuilder);

                // 검색 실행
                SearchHits hits = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT).getHits();

                // 가장 높은 similarityRate의 결과를 리스트에 추가
                if (hits.getTotalHits().value > 0) {
                    SearchHit bestMatchHit = hits.getAt(0); // 가장 높은 similarityRate를 가진 문서
                    Long findearMatchingLogId = Long.parseLong(bestMatchHit.getSourceAsMap().get("findearMatchingLogId").toString());
                    Long acquiredBoardId = Long.parseLong(bestMatchHit.getSourceAsMap().get("acquiredBoardId").toString());
                    Float similarityRate = Float.parseFloat(bestMatchHit.getSourceAsMap().get("similarityRate").toString());
                    String matchingAt = (String) bestMatchHit.getSourceAsMap().get("matchingAt");

                    SearchFindearMatchingListResDto dto = new SearchFindearMatchingListResDto(
                            findearMatchingLogId, id, acquiredBoardId, similarityRate, matchingAt
                    );

                    bestMatchesList.add(dto);
                }
            }

            int from = (page - 1) * size;
            int to = page * size;

            if(to > bestMatchesList.size()) {
                to = bestMatchesList.size();
            }

            List<SearchFindearMatchingListResDto> result = new ArrayList<>();
            for(int i=from; i<to; i++) {
                result.add(bestMatchesList.get(i));
            }

            SearchBestMatchingListDto searchBestMatchingListDto = SearchBestMatchingListDto.builder()
                    .matchingList(result)
                    .totalCount(bestMatchesList.size()).build();

            return searchBestMatchingListDto;

        } catch (IOException e) {
            throw new FindearException(e.getMessage());
        }
    }

    public List<SearchFindearMatchingListResDto> searchAllFindearMatchingList() {

        List<SearchFindearMatchingListResDto> allDatas = new ArrayList<>();
        String searchAfter = null;
        int pageSize = 200; // 페이지당 가져올 문서 수

        try {

            while (true) {
                SearchRequest searchRequest = new SearchRequest("findear_matching_log");
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                searchSourceBuilder.query(QueryBuilders.matchAllQuery());
                searchSourceBuilder.size(pageSize);
                searchSourceBuilder.fetchSource(new String[]{"findearMatchingLogId", "lostBoardId", "acquiredBoardId", "similarityRate", "matchingAt"}, null);

                if (searchAfter != null) {
                    searchSourceBuilder.sort("_doc");
                    searchSourceBuilder.searchAfter(new Object[]{searchAfter});
                }

                searchRequest.source(searchSourceBuilder);
                SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

                SearchHit[] hits = searchResponse.getHits().getHits();
                if (hits.length == 0) {
                    break;
                }

                for (SearchHit hit : hits) {
                    allDatas.add(convertToFindearMatchingLog(hit));
                }

                searchAfter = getLastSortValue(hits);
                System.out.println("searchAfter = " + searchAfter);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return allDatas;
    }

    private String getLastSortValue(SearchHit[] hits) {
        SearchHit lastHit = hits[hits.length - 1];
        Object[] sortValues = lastHit.getSortValues();
        if (sortValues != null && sortValues.length > 0) {
            return sortValues[0].toString();
        } else {
            return lastHit.getId();
        }
    }


    private SearchFindearMatchingListResDto convertToFindearMatchingLog(SearchHit hit) {

        Map<String, Object> sourceAsMap = hit.getSourceAsMap();

        return new SearchFindearMatchingListResDto(

                Long.parseLong(sourceAsMap.get("findearMatchingLogId").toString()),
                Long.parseLong(sourceAsMap.get("lostBoardId").toString()),
                Long.parseLong(sourceAsMap.get("acquiredBoardId").toString()),
                Float.parseFloat(sourceAsMap.get("similarityRate").toString()),
                sourceAsMap.get("matchingAt").toString()
        );

    }


    public void deleteFindearMatchingDatas() {

        findearMatchingLogRepository.deleteAll();
    }

    public Page<FindearMatchingLog> testApi() {

        Page<FindearMatchingLog> result = (Page<FindearMatchingLog>) findearMatchingLogRepository.findAll();

        return result;
    }


}
