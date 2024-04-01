#!/usr/bin/env python
# coding: utf-8
import pandas as pd
import numpy as np
import fasttext
from sklearn.preprocessing import MinMaxScaler
import haversine.haversine as hv
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from kiwipiepy import Kiwi
import re
from dotenv import load_dotenv
import os
import time
import logging
logger = logging.getLogger(__name__)
# 코드 실행 시작 시간 측정
start_time = time.time()

# .env 파일에서 환경 변수를 불러오기
load_dotenv()
path = os.getenv("MODEL_PATH")
model = fasttext.load_model(path)
model.get_words(on_unicode_error='ignore')
def word_cosine_similarity(word1, word2):
    return np.dot(model[word1], model[word2]) / (np.linalg.norm(model[word1]) * np.linalg.norm(model[word2]))

testLost = {
        "lostBoardId" : 1,
        "productName" : "어린이 카드지갑",
        "color" : "빨강",
        "categoryName" : "지갑",
        "description" : "물품에 대한 설명",
        "aiDescription": "",
        "lostAt" : "2024-03-26 00:00:00",
        "xpos" :127.04,
        "ypos" : 37.5013
    }
testFound = [
        {
            "acquiredBoardId" : 1,
            "productName" : "여성 장지갑",
            "color" : "빨강",
            "categoryName" : "지갑",
            "description" : "왼쪽 상단에 흠집이 가있습니다.",
            "xpos" : 126.933,
            "ypos" : 37.545,
            "registeredAt" : "2024-03-25 00:00:00"
        },
        {
            "acquiredBoardId" : 2,
            "productName" : "에르메스",
            "color" : "파랑",
            "categoryName" : "지갑",
            "description" : "모델은 미상입니다. 한정판으로 보입니다.",
            "xpos" : 127.05,
            "ypos" : 37.5034,
            "registeredAt" : "2024-03-25 00:00:00"
        },
        {
            "acquiredBoardId" : 3,
            "productName" : "코끼리 그림 박힌 지갑",
            "color" : "초록",
            "categoryName" : "지갑",
            "description" :     "물품에 대한 설명",
            "xpos" : 126.23,
            "ypos" : 35.24,
            "registeredAt" : "2024-03-25 00:00:00"
        }
    ]
from .models import matchModel
testModel = matchModel()
testModel.setData(testLost, testFound)
testModel.preprocess('findear')
testModel.calColor()
print(testModel.score)

colors = {
    '화이트': 'FFFFFF',
    '블랙': '000000',
    '레드':'FF0000',
    '오렌지': 'FFA500',
    '옐로우': 'FFFF00',
    '그린': '008000',
    '블루': '0000FF',
    '브라운': '8B4513',
    '퍼플': '800080',
    '보라색': 'FF1493',
    '그레이': '808080',
    # '기타': ''
}

print(colors.keys())
color_list = colors.keys()

def findear_matching(lostBoard, acquiredBoardList):
    
    df = pd.DataFrame(acquiredBoardList)
    
    # 데이터 타입 변환
    lostBoard['lostBoardId'] = int(lostBoard['lostBoardId'])
    lostBoard["xpos"] = float(lostBoard["xpos"])
    lostBoard["ypos"] = float(lostBoard["ypos"])
    
    df['acquiredBoardId'] = df['acquiredBoardId'].astype(int)
    df['xpos'] = df['xpos'].astype(float)
    df['ypos'] = df['ypos'].astype(float)

    logger.warning(df)
    logger.warning(lostBoard)
    # # make whole count table
    score = pd.DataFrame()
    score['id'] = df['acquiredBoardId']
    score['name'] = 0
    score['color'] = 0
    score['place'] = 0
    score['desc'] = 0
    print(score)

    # # matching by name
    lostName = lostBoard['productName'].replace(' ','')
    lst = df['productName'].map(lambda x: x.replace(' ', '')).to_list()

    # print('lost name', lostName)
    tmplst = []
    for i, val  in enumerate(lst):
        tmp = word_cosine_similarity(lostName, val)
        tmplst.append(tmp)
        # print(tmp,i)
    score['name'] = tmplst

    # # matchin by color


    if lostBoard['color'] in color_list: # 분실물 색상이 기타 혹은 다른 것이면 점수에 반영 안하도록
        lostcolor = colors[lostBoard['color']]
        lostRed = int(lostcolor[:2], base=16)
        lostGreen = int(lostcolor[2:4], base=16)
        lostBlue = int(lostcolor[4:], base=16)

        tmplst = []
        for i in df['color']:
            if i in color_list:
                color = colors[i]
                red = int(color[:2], base=16)
                green = int(color[2:4], base=16)
                blue = int(color[4:], base=16)
                tmp = np.sqrt(0.3 * ((lostRed - red) ** 2) + 0.59 * ((lostGreen - green) ** 2) + 0.11 * ((lostBlue - blue) ** 2))
                print(tmp)
                tmplst.append([tmp])
            else:  # 습득물 색상이 기타 혹은 다른 것이라면
                tmplst.append([255])

        minmaxScaler = MinMaxScaler().fit([[0],[255]])
        X_train_minmax = minmaxScaler.transform(tmplst)
        nplst = 1- np.array(X_train_minmax).squeeze()

        score['color'] = nplst
        print('color', score.color)

    # # matching by place
    tmpdf = df.copy()
    criteria = 20
    
    tmplst = []

    for x,y in zip(tmpdf['xpos'], tmpdf['ypos']):
        dist = hv((y,x), ( lostBoard['ypos'],lostBoard['xpos']), unit='km')
        # print(dist)
        tmplst.append([dist])

    minmaxScaler = MinMaxScaler().fit([[0],[20]])
    X_train_minmax = minmaxScaler.transform(tmplst)
    nplst = 1- np.array(X_train_minmax).squeeze()
    # print(nplst)
    score['place'] = nplst

    # # matching by text

    kiwi = Kiwi()
    stem_tag = ['NNG', 'NNP', 'VA'] 
    
    # 습득물 설명 형태소 분석 및 토큰화
    tokenized_text_data = []
    for text in df['description']:
        # 영어 및 숫자 추출
        english_words_and_numbers = re.findall(r'[A-Za-z0-9]+', text)
        
        tokenized_text = kiwi.tokenize(text)
        stems = [t.form for t in tokenized_text if t.tag in stem_tag]
        tokenized_text_data.append(' '.join(stems + english_words_and_numbers))
    
    # 분실물 설명 형태소 분석 및 토큰화
    lostText_english_words_and_numbers = re.findall(r'[A-Za-z0-9]+', lostBoard['description'])
    lostText = kiwi.tokenize(lostBoard['description'])
    lostToken = [t.form for t in lostText if t.tag in stem_tag] + lostText_english_words_and_numbers
    
    # 단어 임베딩 후 평균 내어 문서 벡터 구하기
    lostDoc = np.array(model[''],)
    for i in lostToken:
        emb = np.array(model[i]) 
        lostDoc +=emb 
    lostDoc /= len(lostToken)

    findLst = []
    for i in tokenized_text_data:
        tokens = i.split()
        emb = model['']
        for i in tokens:
            emb += model[i]
        emb /= len(i)
        findLst.append(emb)
        
    # 분실물과 습특물 text 벡터 내적
    findScore = []
    def tt(word1, word2):
        return np.dot(word1, word2) / (np.linalg.norm(word1) * np.linalg.norm(word2))
    for i in findLst:
        findScore.append(tt(i,lostDoc))

    score['desc'] = findScore
    # 데이터 정렬 및 변환
     
    # id 열 제외 열 값 평균 계산
    score['mean_value'] = score.iloc[:, 1:].mean(axis=1)
    score['mean_value'] = score['mean_value'].round(decimals=5)
    
    # 평균 값을 기준으로 DataFrame 정렬
    df_sorted = score.sort_values(by='mean_value', ascending=False)
    print(df_sorted)
    
    # 데이터프레임 순회하며 반환 형태로 변환하는 리스트 컴프리헨션
    lostBoardId = lostBoard["lostBoardId"]
    result_data = [
        {"lostBoardId": int(lostBoardId), "acquiredBoardId": int(row["id"]), "similarityRate": row["mean_value"]}
        for index, row in df_sorted.loc[:, ['id', 'mean_value']].iterrows()
    ]
    
    print(result_data)
    return result_data  

# 코드 실행 종료 시간 측정
end_time = time.time()

# 시작 시간과 종료 시간의 차이를 계산하여 실행 시간 출력
execution_time = end_time - start_time
print(f"matching.py 실행 시간: {execution_time} 초")


def lost112_matching(lostBoard, acquiredBoardList):
    '''
    '''
    testModel.defineOrigin('lost')    
    df = pd.DataFrame(acquiredBoardList)
    
    # 데이터 타입 변환
    lostBoard['lostBoardId'] = int(lostBoard['lostBoardId'])
    lostBoard["xpos"] = float(lostBoard["xpos"])
    lostBoard["ypos"] = float(lostBoard["ypos"])
    
    df['acquiredBoardId'] = df['acquiredBoardId'].astype(int)

    logger.warning(df)
    logger.warning(lostBoard)
    # # make whole count table
    score = pd.DataFrame()
    score['id'] = df['acquiredBoardId']
    score['name'] = 0
    #score['color'] = 0
    #score['place'] = 0
    score['desc'] = 0

    # # matching by name
    lostName = lostBoard['productName'].replace(' ','')
    lst = df['productName'].map(lambda x: x.replace(' ', '')).to_list()

    # print('lost name', lostName)
    tmplst = []
    for i, val  in enumerate(lst):
        tmp = word_cosine_similarity(lostName, val)
        tmplst.append(tmp)
        # print(tmp,i)
    score['name'] = tmplst



    # # matching by text

    kiwi = Kiwi()
    stem_tag = ['NNG', 'NNP', 'VA'] 
    
    # 습득물 설명 형태소 분석 및 토큰화
    tokenized_text_data = []
    for text in df['description']:
        # 영어 및 숫자 추출
        english_words_and_numbers = re.findall(r'[A-Za-z0-9]+', text)
        
        tokenized_text = kiwi.tokenize(text)
        stems = [t.form for t in tokenized_text if t.tag in stem_tag]
        tokenized_text_data.append(' '.join(stems + english_words_and_numbers))
    
    # 분실물 설명 형태소 분석 및 토큰화
    lostText_english_words_and_numbers = re.findall(r'[A-Za-z0-9]+', lostBoard['description'])
    lostText = kiwi.tokenize(lostBoard['description'])
    lostToken = [t.form for t in lostText if t.tag in stem_tag] + lostText_english_words_and_numbers
    
    # 단어 임베딩 후 평균 내어 문서 벡터 구하기
    lostDoc = np.array(model[''],)
    for i in lostToken:
        emb = np.array(model[i]) 
        lostDoc +=emb 
    lostDoc /= len(lostToken)

    findLst = []
    for i in tokenized_text_data:
        tokens = i.split()
        emb = model['']
        for i in tokens:
            emb += model[i]
        emb /= len(i)
        findLst.append(emb)
        
    # 분실물과 습특물 text 벡터 내적
    findScore = []
    def tt(word1, word2):
        return np.dot(word1, word2) / (np.linalg.norm(word1) * np.linalg.norm(word2))
    for i in findLst:
        findScore.append(tt(i,lostDoc))

    score['desc'] = findScore
    # 데이터 정렬 및 변환
     
    # id 열 제외 열 값 평균 계산
    score['mean_value'] = score.iloc[:, 1:].mean(axis=1)
    score['mean_value'] = score['mean_value'].round(decimals=5)
    
    # 평균 값을 기준으로 DataFrame 정렬
    df_sorted = score.sort_values(by='mean_value', ascending=False)
    print(df_sorted)
    
    # 데이터프레임 순회하며 반환 형태로 변환하는 리스트 컴프리헨션
    lostBoardId = lostBoard["lostBoardId"]
    result_data = [
        {"lostBoardId": int(lostBoardId), "acquiredBoardId": int(row["id"]), "similarityRate": row["mean_value"]}
        for index, row in df_sorted.loc[:, ['id', 'mean_value']].iterrows()
    ]
    
    print(result_data)
    return result_data  


    