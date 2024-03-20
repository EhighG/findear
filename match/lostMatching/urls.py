from django.urls import path

from . import views

urlpatterns = [
        # path('', views.index, name='index'),
        path('lost-matching', views.lost_matching, name='lost-matching'),
]
