from django.shortcuts import render
from django.http import JsonResponse
import json
import pandas as pd
from numpyencoder import NumpyEncoder

# Create your views here.

def lost_matching(request):
    if request.method == 'POST':
        # try:
        #     body = json.loads(request.body)
        # except JSONDecodeErorr:
        #     return JsonResponse({'error':'invalid json'}, status=400)
        body = json.loads(request.body)
        data = process_lost_item_data(body)
    return JsonResponse({ 'message':'success', 'result':data }, status = 200)

def process_lost_item_data(found_item_info):
    # 받은 데이터를 이용하여 DataFrame 생성
    df = pd.DataFrame([found_item_info])

    # 데이터 처리 수행
    processed_data = analyze_lost_data(df)

    return processed_data

def analyze_lost_data(data):
    # 데이터 분석 수행
    return data

def findear_matching(request):
    if request.method == 'POST':
        # try:
        #     body = json.loads(request.body)
        # except JSONDecodeErorr:
        #     return JsonResponse({'error':'invalid json'}, status=400)
        body = json.loads(request.body)
        # print(body)
        data = process_findear_item_data(body)
        # print(data)
    return JsonResponse({ 'message':'success', 'result':data }, status = 200)
    # return JsonResponse({ 'message':'success', 'result':body }, status = 200)

def process_findear_item_data(found_item_info):
    # 받은 데이터를 이용하여 DataFrame 생성
    df = pd.DataFrame([found_item_info])
    
    print(df)

    # 데이터 처리 수행
    processed_data = analyze_findear_data(df)
    
    # json.dumps(processed_data, cls=NumpyEncoder, ensure_ascii=False)

    return processed_data

def analyze_findear_data(data):
    # 데이터 분석 수행
    data_list = [
		{
			"lostBoardId" : 1,
			"acquiredBoardId" : 1,
			"simulerityRate" : 0.9
		},
		{
			"lostBoardId" : 1,
			"acquiredBoardId" : 2,
			"simulerityRate" : 0.8
		},
		{
			"lostBoardId" : 1,
			"acquiredBoardId" : 5,
			"simulerityRate" : 0.9
		}
	]
    return data_list