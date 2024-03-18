package com.findear.batch.police.service;

import com.findear.batch.police.domain.PoliceAcquiredData;
import com.findear.batch.police.dto.SaveDataRequestDto;
import com.findear.batch.police.repository.PoliceAcquiredDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
@Service
public class PoliceAcquiredDataService {

    private final PoliceAcquiredDataRepository policeAcquiredDataRepository;

    @Value("${my.secret-key}")
    private String secretKey;

    private Long id = 1L;

    public void saveData(SaveDataRequestDto saveDataRequestDto) {

//        PoliceAcquiredData policeAcquiredData = PoliceAcquiredData.builder()
//                        .id(id++).atcId(saveDataRequestDto.getAtcId())
//                        .fdYmd(saveDataRequestDto.getFdYmd())
//                        .depPlace(saveDataRequestDto.getDepPlace())
//                        .fdFilePathImg(saveDataRequestDto.getFdFilePathImg()).atcId(saveDataRequestDto.getAtcId())
//                        .fdSbjt(saveDataRequestDto.getFdSbjt()).fdPrdtNm(saveDataRequestDto.getFdPrdtNm())
//                        .prdtClNm(saveDataRequestDto.getPrdtClNm())
//                        .build();
//
//        policeAcquiredDataRepository.save(policeAcquiredData);
    }

    public void deleteDatas() {

        policeAcquiredDataRepository.deleteAll();
    }

    public Page<PoliceAcquiredData> searchAllDatas() {

        return (Page<PoliceAcquiredData>) policeAcquiredDataRepository.findAll();
    }

    public void savePoliceData() {

        try {

            List<String> urlLinks = new ArrayList<>();

            /*URL*/
            String urlBuilder = "http://apis.data.go.kr/1320000/LosPtfundInfoInqireService/getPtLosfundInfoAccToClAreaPd" + "?" + URLEncoder.encode("serviceKey", StandardCharsets.UTF_8) + "=" + secretKey + /*Service Key*/
                    "&" + URLEncoder.encode("pageNo", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("", StandardCharsets.UTF_8) + /*페이지번호*/
                    "&" + URLEncoder.encode("numOfRows", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("10", StandardCharsets.UTF_8) + /*한 페이지 결과 수*/
                    "&" + URLEncoder.encode("PRDT_CL_CD_01", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("", StandardCharsets.UTF_8) + /*대분류*/
                    "&" + URLEncoder.encode("PRDT_CL_CD_02", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("", StandardCharsets.UTF_8) + /*중분류*/
                    "&" + URLEncoder.encode("CLR_CD", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("", StandardCharsets.UTF_8) + /*습득물 색상*/
                    "&" + URLEncoder.encode("START_YMD", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("20170302", StandardCharsets.UTF_8) + /*검색시작일*/
                    "&" + URLEncoder.encode("END_YMD", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("20170802", StandardCharsets.UTF_8) + /*검색종료일*/
                    "&" + URLEncoder.encode("N_FD_LCT_CD", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("", StandardCharsets.UTF_8); /*습득지역*/
            URL url = new URL(urlBuilder);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/json");

            System.out.println("Response code: " + conn.getResponseCode());

            BufferedReader rd;
            if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();
            conn.disconnect();

            System.out.println(sb);

            List<String> responses = new ArrayList<>();
            responses.add(sb.toString());

            System.out.println("responses : ");

            for(String res : responses) {
                System.out.println(res);
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
            // 결과값 List
            List<String[]> rowDatas = new ArrayList<>();

            List<PoliceAcquiredData> policeAcquiredDataList = new ArrayList<>();

            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();

            for (String response : responses) {

                System.out.println("response : " + response);

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

                        System.out.println("value = " + value);

                        // null값 있는(잘못된) 데이터는 pass
                        if (value == null) continue;

                        rowData[j] = value.trim();

                    }

                    // 색상 컬럼 분류 로직
                    String fdSbjt = rowData[4];
                    String[] array = fdSbjt.split("\\(");
                    String clrNm = array[1];

                    // 대분류, 소분류 컬럼 분류 로직
                    String prdtClNm = rowData[6];
                    array = prdtClNm.split(" ");
                    String mainPrdtClNm = array[0];
                    String subPrdtClNm = array[2];

                    policeAcquiredData = new PoliceAcquiredData(id++,
                            rowData[0], rowData[1], rowData[2], rowData[3], rowData[4],
                            clrNm, rowData[5], rowData[6], mainPrdtClNm, subPrdtClNm);

                    policeAcquiredDataList.add(policeAcquiredData);

                }

                policeAcquiredDataRepository.saveAll(policeAcquiredDataList);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
