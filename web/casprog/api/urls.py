from django.conf.urls import url
from django.urls import path, include

from . import views

urlpatterns = [
    path('postAVGNoiseGPSP/', views.postAVGNoiseGPSP, name="postAVGNoiseGPSP" ),
    path('postAVGNoiseNoPrivacy/', views.postAVGNoiseNoPrivacy, name="postAVGNoiseNoPrivacy" ),
    path('postInsertRilevazioniNoPrivacy/', views.postInsertRilevazioniNoPrivacy, name="postInsertRilevazioniNoPrivacy" ),
    path('postAVGNoiseDummy/', views.postAVGNoiseDummy, name="postAVGNoiseDummy" ),
    path('postInsertRilevazioniGPSP/', views.postInsertRilevazioniGPSP, name="postInsertRilevazioniGPSP" ),
    path('postInsertRilevazioniDUMMY/', views.postInsertRilevazioniDUMMY, name="postInsertRilevazioniDUMMY" )
]