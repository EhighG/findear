from django.shortcuts import render
from django.http import JsonResponse
import json
import pandas as pd

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
    processed_data = analyze_data(df)

    return processed_data

def analyze_data(data):
    # 데이터 분석 수행
    return data