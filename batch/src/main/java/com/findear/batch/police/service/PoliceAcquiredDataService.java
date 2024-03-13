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

@RequiredArgsConstructor
@Service
public class PoliceAcquiredDataService {

    private final PoliceAcquiredDataRepository policeAcquiredDataRepository;

    @Value("${my.secret-key}")
    private String secretKey;

    private Long id = 1L;
    public void saveData(SaveDataRequestDto saveDataRequestDto) {

        PoliceAcquiredData policeAcquiredData = PoliceAcquiredData.builder()
                        .id(id++).atcId(saveDataRequestDto.getAtcId())
                        .clrNm(saveDataRequestDto.getClrNm()).addr(saveDataRequestDto.getAddr())
                        .fdSn(saveDataRequestDto.getFdSn()).fdYmd(saveDataRequestDto.getFdYmd())
                        .rnum(saveDataRequestDto.getRnum()).depPlace(saveDataRequestDto.getDepPlace())
                        .fdFilePathImg(saveDataRequestDto.getFdFilePathImg()).atcId(saveDataRequestDto.getAtcId())
                        .fdSbjt(saveDataRequestDto.getFdSbjt()).fdPrdtNm(saveDataRequestDto.getFdPrdtNm())
                        .prdtClNm(saveDataRequestDto.getPrdtClNm()).subPrdtClNm(saveDataRequestDto.getSubPrdtClNm())
                        .build();

        policeAcquiredDataRepository.save(policeAcquiredData);
    }

    public void deleteDatas() {

        policeAcquiredDataRepository.deleteAll();
    }

    public Page<PoliceAcquiredData> searchAllDatas() {

        return (Page<PoliceAcquiredData>) policeAcquiredDataRepository.findAll();
    }

    public void savePoliceData() {

        try {

            /*URL*/
            String urlBuilder = "http://apis.data.go.kr/1320000/LosPtfundInfoInqireService/getPtLosfundInfoAccToClAreaPd" + "?" + URLEncoder.encode("serviceKey", StandardCharsets.UTF_8) + "=" + secretKey + /*Service Key*/
                    "&" + URLEncoder.encode("pageNo", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("", StandardCharsets.UTF_8) + /*페이지번호*/
                    "&" + URLEncoder.encode("numOfRows", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("100", StandardCharsets.UTF_8) + /*한 페이지 결과 수*/
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

            // 업로드 및 lastTrs 업데이트 실행
//            String result = readData(responses);

            ///////////////////////////////////////////////////////////////////////////////////////


        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

//    /**
//     * 각 속성값 문자열로 추출
//     * @param responses
//     * @return
//     */
//    private String readData(List<String> responses) {
//        try {
//            // 결과값 List
//            List<String[]> rowDatas = new ArrayList<>();
//
//            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder builder = builderFactory.newDocumentBuilder();
//
//            for (String response : responses) {
//
//                // String -> document 변환
//                Document xmlDoc = builder.parse(new ByteArrayInputStream(response.getBytes()));
//                xmlDoc.getDocumentElement().normalize();
//
//                Element root = xmlDoc.getDocumentElement();
//                NodeList items = root.getElementsByTagName("item"); // 반환된 item(실거래가) 리스트
//
//                // 데이터 읽어오기
//                int length = items.getLength();
//
//                String[] targetTagNames = {"atcId", "depPlace", "addr", "fdFilePathImg",
//                        "fdPrdtNm", "fdSbjt", "fdSn", "fdYmd", "prdtClNm", "rnum"};
//
//                Itemloop : for (int i = 0; i < length; i++) {
//                    Element item = (Element) items.item(i);
//                    String[] rowData = new String[8];
//
//                    for (int j = 0; j < targetTagNames.length; j++) {
//                        Node valueNode = item.getElementsByTagName(targetTagNames[j]).item(0);
//                        String value = (valueNode == null) ? null : valueNode.getTextContent();
//
//                        // null값 있는(잘못된) 데이터는 pass
//                        if (value == null) continue Itemloop;
//
//                        rowData[j] = value.trim();
//                    }
//
//                    // 전일 자 데이터만 업로드
//                    String currentMonth = String.valueOf(LocalDate.now().getMonthValue());
//                    String yesterday = String.valueOf(LocalDate.now().getDayOfMonth() - 1);
//
//                    if (rowData[6].equals(yesterday) && rowData[5].equals(currentMonth)) rowDatas.add(rowData);
//                }
//            }
//
//            String result;
//
//            if (rowDatas.size() > 0) result = createObject(rowDatas) + " row 업데이트";
//            else result = "업로드할 데이터 없음";
//
//            return result;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "데이터 읽기 실패";
//        }
//    }

//    private String createObject(List<String[]> rowDatas) {
//        // 저장할 부동산
//        Set<PoliceAcquiredData> policeAcquiredDatas = new HashSet<>();
//
////        // 중복 제거에 사용됨. (부동산, 거래일) 별로 1개의 데이터만 남김.(key : realEstateIdx + " " + transactionTime, value : transaction)
////        Map<String, List<RealEstateTransaction>> transactions = new HashMap<>();
////
////        // realEstates 먼저 업로드 - realEstateId가 없으므로
//
////        for (String[] rowData : rowDatas) {
////            Integer regionIdx = Integer.parseInt(rowData[2] + rowData[3].substring(0, 3));
////
////            String realEstateName = rowData[4];
////
////            policeAcquiredDatas.add(PoliceAcquiredData.builder()
////                    .name(realEstateName)
////                    .regionId(regionIdx)
////                    .build());
////        }
////        realEstateUploadDao.uploadRealEstates(realEstates);
//
//        // id 생성된 부동산 리스트 가져오기
//        List<RealEstate> realEstatesInDB = realEstateUploadDao.getRealEstates();
//        // rowData 형태  : {"거래금액", "년", "법정동시군구코드", "법정동읍면동코드", "아파트", "월", "일", "전용면적"};
//
//        for (String[] rowData : rowDatas) {
//            Long price = Long.parseLong(rowData[0].replaceAll(",", "")) * 10000;
//            float size = Float.parseFloat(rowData[7]);
//
//            int avgPrice = Math.round(price/size);
//
//            // 거래일
//            String year = rowData[1];
//            String month = rowData[5];
//            String day = rowData[6];
//
//            String transactionDate = year + "-" + month + "-" + day;
//
//            // realEstateIdx 찾기
//            int realEstateId = -1;
//
//            int regionId = Integer.parseInt(rowData[2] + rowData[3].substring(0, 3));
//
//            for (RealEstate realEstate : realEstatesInDB) {
//                if (realEstate.getName().equals(rowData[4]) && realEstate.getRegionId() == regionId) {
//                    realEstateId = realEstate.getId();
//                    break;
//                }
//            }
//
//            String key = realEstateId + " " + transactionDate;
//
//            transactions.putIfAbsent(key, new ArrayList<>());
//
//            transactions.get(key).add(RealEstateTransaction.builder()
//                    .price(avgPrice)
//                    .date(transactionDate)
//                    .realEstateId(realEstateId)
//                    .build());
//        }
//
//        // 중복 제거된 데이터를 담을 리스트
//        List<RealEstateTransaction> transactionList = new ArrayList<>();
//
//        // 중복 데이터 합치기
//        for (List<RealEstateTransaction> trList : transactions.values()) {
//            if (trList.size() == 1) {
//                transactionList.add(trList.get(0));
//                continue;
//            }
//
//            long sumOfPrice = 0;
//
//            for (RealEstateTransaction tr : trList) {
//                sumOfPrice += tr.getPrice();
//            }
//            int price = (int) (sumOfPrice/trList.size());
//            trList.get(0).setPrice(price);
//
//            transactionList.add(trList.get(0));
//        }
//
//        // 데이터 업로드
//        Set<Integer> updatedRealEstateIdx = realEstateUploadDao.uploadTransactions(transactionList);
//        // lastTrs 및 거래 테이블들 업데이트
//
//        realEstateUploadDao.updateTrs();
//
//        return String.valueOf(transactionList.size());
//    }


}
