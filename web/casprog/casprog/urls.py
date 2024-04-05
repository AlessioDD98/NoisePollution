"""casprog URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/3.2/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.contrib import admin
from django.urls import path, include
import api
from . import views
from rest_framework import routers
from rest_framework_swagger.views import get_swagger_view


router = routers.DefaultRouter()

schema_view = get_swagger_view(title='User API')

urlpatterns = [
    path('admin/', admin.site.urls),
    path('api-auth/', include('rest_framework.urls')),
    path('', views.index, name="index"),
    path('previsioni.html/', views.previsioni, name="previsioni"),
    path('previsioni.html/previsione_rm/', views.previsione_rm, name="previsione_rm"),
    path('kmeans_clustering/', views.kmeans_c, name="kmeans_c"),
    path('api/', include('api.urls')),
    path('swagger/', schema_view)
]
