package com.findear.batch.police.service;

import com.findear.batch.police.domain.PoliceAcquiredData;
import com.findear.batch.police.exception.PoliceException;
import com.findear.batch.police.repository.PoliceAcquiredDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.transaction.Transactional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class PoliceAcquiredDataService {

    private final PoliceAcquiredDataRepository policeAcquiredDataRepository;

    private final RestHighLevelClient restHighLevelClient;

    @Value("${my.secret-key}")
    private String secretKey;

//    private Long id = 1L;

    public void deleteDatas() {

        policeAcquiredDataRepository.deleteAll();
    }

    public List<PoliceAcquiredData> searchAllDatas() {
        try {
            List<PoliceAcquiredData> allDatas = new ArrayList<>();
            String searchAfter = null;
            int pageSize = 200; // 페이지당 가져올 문서 수

            while (true) {
                SearchRequest searchRequest = new SearchRequest("police_acquired_data");
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                searchSourceBuilder.query(QueryBuilders.matchAllQuery());
                searchSourceBuilder.size(pageSize);
                searchSourceBuilder.fetchSource(new String[]{"id", "atcId", "depPlace", "fdFilePathImg", "fdPrdtNm", "fdSbjt", "clrNm", "fdYmd", "prdtClNm", "mainPrdtClNm", "subPrdtClNm"}, null);

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
                    allDatas.add(convertToPoliceData(hit));
                }

                searchAfter = getLastSortValue(hits);
                System.out.println("searchAfter = " + searchAfter);
            }

            return allDatas;

        } catch (Exception e) {

            e.printStackTrace();
        }
        return null;
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

    private PoliceAcquiredData convertToPoliceData(SearchHit hit) {

        Map<String, Object> sourceAsMap = hit.getSourceAsMap();

        if(sourceAsMap.get("fdSbjt") == null) {

            return new PoliceAcquiredData(
                    Long.parseLong(sourceAsMap.get("id").toString()),
                    sourceAsMap.get("atcId").toString(),
                    sourceAsMap.get("depPlace").toString(),
                    sourceAsMap.get("fdFilePathImg").toString(),
                    sourceAsMap.get("fdPrdtNm").toString(),
                    sourceAsMap.get("clrNm").toString(),
                    sourceAsMap.get("fdYmd").toString(),
                    sourceAsMap.get("prdtClNm").toString(),
                    sourceAsMap.get("mainPrdtClNm").toString(),
                    sourceAsMap.getOrDefault("subPrdtClNm", "").toString()
            );
        } else if(sourceAsMap.get("clrNm") == null) {

            return new PoliceAcquiredData(

                    Long.parseLong(sourceAsMap.get("id").toString()),
                    sourceAsMap.get("atcId").toString(),
                    sourceAsMap.get("depPlace").toString(),
                    sourceAsMap.get("fdFilePathImg").toString(),
                    sourceAsMap.get("fdPrdtNm").toString(),
                    sourceAsMap.get("fdSbjt").toString(),
                    sourceAsMap.get("fdYmd").toString(),
                    sourceAsMap.get("prdtClNm").toString(),
                    sourceAsMap.get("mainPrdtClNm").toString(),
                    sourceAsMap.getOrDefault("subPrdtClNm", "").toString()
            );
        } else if(sourceAsMap.get("depPlace") == null) {

            return new PoliceAcquiredData(

                    Long.parseLong(sourceAsMap.get("id").toString()),
                    sourceAsMap.get("atcId").toString(),
                    sourceAsMap.get("fdFilePathImg").toString(),
                    sourceAsMap.get("fdPrdtNm").toString(),
                    sourceAsMap.get("fdSbjt").toString(),
                    sourceAsMap.get("clrNm").toString(),
                    sourceAsMap.get("fdYmd").toString(),
                    sourceAsMap.get("prdtClNm").toString(),
                    sourceAsMap.get("mainPrdtClNm").toString(),
                    sourceAsMap.getOrDefault("subPrdtClNm", "").toString()
            );
        }
        else {

            return new PoliceAcquiredData(

                    Long.parseLong(sourceAsMap.get("id").toString()),
                    sourceAsMap.get("atcId").toString(),
                    sourceAsMap.get("depPlace").toString(),
                    sourceAsMap.get("fdFilePathImg").toString(),
                    sourceAsMap.get("fdPrdtNm").toString(),
                    sourceAsMap.get("fdSbjt").toString(),
                    sourceAsMap.get("clrNm").toString(),
                    sourceAsMap.get("fdYmd").toString(),
                    sourceAsMap.get("prdtClNm").toString(),
                    sourceAsMap.get("mainPrdtClNm").toString(),
                    sourceAsMap.getOrDefault("subPrdtClNm", "").toString()
            );
        }

    }

    public Page<PoliceAcquiredData> searchByPage(int page, int size) {

        return policeAcquiredDataRepository.findAll(PageRequest.of(page, size));
    }


    public void savePoliceData() {

        try {

            // elastic search 모든 데이터 삭제
            deleteDatas();
            log.info("데이터 삭제 성공");

            String startDate = "20150101";
            LocalDateTime today = LocalDateTime.now();
            String todaysDate = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            log.info(todaysDate + "까지 데이터 저장");

            String numOfRows = "50000";

            BufferedReader rd;

            List<String> responses = new ArrayList<>();

            // 경찰청 산하 습득물 데이터 처리
            int count = 0;
            for(int pageNo = 1; ; pageNo++) {

                /*URL*/
                String urlBuilder = "http://apis.data.go.kr/1320000/LosPtfundInfoInqireService/getPtLosfundInfoAccToClAreaPd" + "?" + URLEncoder.encode("serviceKey", StandardCharsets.UTF_8) + "=" + secretKey + /*Service Key*/
                        "&" + URLEncoder.encode("pageNo", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(String.valueOf(pageNo), StandardCharsets.UTF_8) + /*페이지번호*/
                        "&" + URLEncoder.encode("numOfRows", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(numOfRows, StandardCharsets.UTF_8) + /*한 페이지 결과 수*/
                        "&" + URLEncoder.encode("PRDT_CL_CD_01", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("", StandardCharsets.UTF_8) + /*대분류*/
                        "&" + URLEncoder.encode("PRDT_CL_CD_02", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("", StandardCharsets.UTF_8) + /*중분류*/
                        "&" + URLEncoder.encode("CLR_CD", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("", StandardCharsets.UTF_8) + /*습득물 색상*/
                        "&" + URLEncoder.encode("START_YMD", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(startDate, StandardCharsets.UTF_8) + /*검색시작일*/
                        "&" + URLEncoder.encode("END_YMD", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(todaysDate, StandardCharsets.UTF_8) + /*검색종료일*/
                        "&" + URLEncoder.encode("N_FD_LCT_CD", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("", StandardCharsets.UTF_8); /*습득지역*/

                URL url = new URL(urlBuilder);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-type", "application/json");

                System.out.println("Response code: " + conn.getResponseCode());

                if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                    rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }

                boolean isEnd = false;
                String line;
                while ((line = rd.readLine()) != null) {

                    if(!(line.contains("atcId")) && count == 0) {
                        count++;
                        continue;
                    }

                    if(!(line.contains("atcId")) && count == 1) {
                        isEnd = true;
                        break;
                    }

                    log.info(line);

                    responses.add(line);
                }

                rd.close();
                conn.disconnect();

                if(isEnd) {
                    break;
                }
            }


            // 경찰청 습득물 데이터 처리
            count = 0;
            for(int pageNo = 1; ; pageNo++) {

                /*URL*/
                String urlBuilder = "http://apis.data.go.kr/1320000/LosfundInfoInqireService/getLosfundInfoAccToClAreaPd" + "?" + URLEncoder.encode("serviceKey", StandardCharsets.UTF_8) + "=" + secretKey + /*Service Key*/
                        "&" + URLEncoder.encode("pageNo", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(String.valueOf(pageNo), StandardCharsets.UTF_8) + /*페이지번호*/
                        "&" + URLEncoder.encode("numOfRows", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(numOfRows, StandardCharsets.UTF_8) + /*한 페이지 결과 수*/
                        "&" + URLEncoder.encode("PRDT_CL_CD_01", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("", StandardCharsets.UTF_8) + /*대분류*/
                        "&" + URLEncoder.encode("PRDT_CL_CD_02", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("", StandardCharsets.UTF_8) + /*중분류*/
                        "&" + URLEncoder.encode("CLR_CD", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("", StandardCharsets.UTF_8) + /*습득물 색상*/
                        "&" + URLEncoder.encode("START_YMD", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(startDate, StandardCharsets.UTF_8) + /*검색시작일*/
                        "&" + URLEncoder.encode("END_YMD", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(todaysDate, StandardCharsets.UTF_8) + /*검색종료일*/
                        "&" + URLEncoder.encode("N_FD_LCT_CD", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("", StandardCharsets.UTF_8); /*습득지역*/

                URL url = new URL(urlBuilder);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-type", "application/json");

                System.out.println("Response code: " + conn.getResponseCode());

                if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                    rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }

                boolean isEnd = false;
                String line;
                while ((line = rd.readLine()) != null) {

                    if(!(line.contains("atcId")) && count == 0) {
                        count++;
                        continue;
                    }

                    if(!(line.contains("atcId")) && count == 1) {
                        isEnd = true;
                        break;
                    }

                    log.info(line);

                    responses.add(line);
                }


                rd.close();
                conn.disconnect();

                if(isEnd) {
                    break;
                }
            }

            // 업로드 및 lastTrs 업데이트 실행
            readandSaveData(responses);

        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }


    /**
     * 각 속성값 문자열로 추출
     * @param responses
     * @return
     */
    private void readandSaveData(List<String> responses) {

        try {

            Long id = 1L;

            // 결과값 List
            List<String[]> rowDatas = new ArrayList<>();

            List<PoliceAcquiredData> policeAcquiredDataList = new ArrayList<>();

            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();

            for (String response : responses) {

                // String -> document 변환
                Document xmlDoc = builder.parse(new ByteArrayInputStream(response.getBytes()));
                xmlDoc.getDocumentElement().normalize();

                Element root = xmlDoc.getDocumentElement();
                NodeList items = root.getElementsByTagName("item");

                // 데이터 읽어오기
                int length = items.getLength();

                String[] targetTagNames = {"atcId", "depPlace", "fdFilePathImg",
                        "fdPrdtNm", "fdSbjt", "fdYmd", "prdtClNm"};

                for (int i = 0; i < length; i++) {

                    PoliceAcquiredData policeAcquiredData;

                    Element item = (Element) items.item(i);
                    String[] rowData = new String[7];


                    for (int j = 0; j < targetTagNames.length; j++) {
                        Node valueNode = item.getElementsByTagName(targetTagNames[j]).item(0);
                        String value = (valueNode == null) ? null : valueNode.getTextContent();

                        // null값 있는(잘못된) 데이터는 pass
                        if (value == null) continue;

                        rowData[j] = value.trim();

                    }

                    String[] array;
                    String fdSbjt = "";
                    String clrNm = "";
                    // 색상 컬럼 분류 로직
                    if(rowData[4] != null) {
                        fdSbjt = rowData[4];
                        array = fdSbjt.split("\\(");

                        if(array.length < 2) {
                            clrNm = null;
                        }
                        else {
                            clrNm = array[1];
                        }
                    }

                    // 대분류, 소분류 컬럼 분류 로직
                    String prdtClNm = rowData[6];
                    array = prdtClNm.split(" ");
                    String mainPrdtClNm = array[0];

                    String subPrdtClNm;
                    if(array.length != 3) {
                        subPrdtClNm = null;
                    }
                    else {

                        subPrdtClNm = array[2];
                    }

                    policeAcquiredData = new PoliceAcquiredData(id++,
                            rowData[0], rowData[1], rowData[2], rowData[3], rowData[4],
                            clrNm, rowData[5], rowData[6], mainPrdtClNm, subPrdtClNm);

                    policeAcquiredDataList.add(policeAcquiredData);

                }

                policeAcquiredDataRepository.saveAll(policeAcquiredDataList);

                policeAcquiredDataList.clear();

                log.info("데이터 저장 완료");

            }

            log.info("모든 데이터 저장 완료");

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public List<PoliceAcquiredData> search(int page, int size, String category,
                                           String startDate, String endDate) {

        try {

//            if(category != null) {
//
//                if(startDate != null)
//            }
//            List<PoliceAcquiredData> datas =
//
//
            List<Long> searchIds = new ArrayList<>();
            int start = size * page - size + 1;
            int end = size * page;

            for(int i = start; i <= end; i++) {
                searchIds.add((long) i);
            }

            Long totalCount = policeAcquiredDataRepository.count();
            System.out.println("전체 갯수 : " + totalCount);

            List<PoliceAcquiredData> resultList = (List<PoliceAcquiredData>) policeAcquiredDataRepository.findAllById(searchIds);

            return resultList;

        } catch (Exception e) {
            throw new PoliceException(e.getMessage());
        }
    }
}
