from django.shortcuts import render
from django.http import JsonResponse

# Create your views here.

def lost_matching(request):
    return JsonResponse({'message':'success'}, status = 200)