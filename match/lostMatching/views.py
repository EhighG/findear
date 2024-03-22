from django.shortcuts import render
from django.http import JsonResponse
import json

# Create your views here.

def lost_matching(request):
    if request.method == 'POST':
        # try:
        #     body = json.loads(request.body)
        # except JSONDecodeErorr:
        #     return JsonResponse({'error':'invalid json'}, status=400)
        body = json.loads(request.body)
    return JsonResponse({'message':'success'}, status = 200)