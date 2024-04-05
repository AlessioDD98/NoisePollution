from django.http import request
from django.http.response import HttpResponse
from django.shortcuts import render

import psycopg2
import numpy as np
import random
import json
from django.views.decorators.csrf import csrf_exempt

from sklearn.model_selection import train_test_split
from sklearn.linear_model import LinearRegression
from sklearn.metrics import mean_squared_error
from sklearn.cluster import KMeans


db = psycopg2.connect(host="127.0.0.1", port = 5432, database="progetto", user="manu", password="7894")

def index(request):
    cur = db.cursor()

    cur.execute("SELECT ST_AsGeoJson(posizione),rumore,timestamp,privacy FROM public.rilevazioni WHERE privacy='dummy' or privacy='no' or posizione in (SELECT posizione FROM rilevazioni where privacy='gpsp' and reale=false)")
    rilevazioni = cur.fetchall()

    rilevazioni_gpsp = []
    rilevazioni_dummy = [] 
    rilevazioni_noprivacy = []

    for el in rilevazioni:
        if el[3] == 'gpsp':
            rilevazioni_gpsp.append(el)
        elif el[3] == 'dummy':
            rilevazioni_dummy.append(el)
        else:
            rilevazioni_noprivacy.append(el)

    rilevazioni_gpsp = np.array(rilevazioni_gpsp).tolist()
    rilevazioni_dummy = np.array(rilevazioni_dummy).tolist() 
    rilevazioni_noprivacy = np.array(rilevazioni_noprivacy).tolist()
    cur.close()

    return render(request, 'index.html',{'rilevazioni_gpsp':rilevazioni_gpsp, 'rilevazioni_dummy':rilevazioni_dummy,'rilevazioni_noprivacy':rilevazioni_noprivacy})

def previsioni(request):

    cur = db.cursor()

    with open('dataset.csv', 'r') as f:
        lines = f.readlines()
    
    rilevazioni = []

    for line in lines:
        l = line.split(",")
        row = [l[0],l[1],l[2].replace("\n","")]
        rilevazioni.append(row)

    rilevazioni = np.array(rilevazioni).tolist()

    cur.close()

    return render(request, 'previsioni.html', {'rilevazioni':rilevazioni})

@csrf_exempt
def previsione_rm(request):
    lat = request.POST.get("lat")
    long = request.POST.get("long")
    if lat == None or long == None or lat == "" or long == "":
        return HttpResponse("None")
        
    rm = calc_pred(float(lat),float(long))
    #return render(request, 'previsione_rm')
    return HttpResponse(rm)

def calc_pred(lat,long):

    dataset = np.genfromtxt('dataset.csv', delimiter = ',', usecols={0,1})
    clss = np.genfromtxt('dataset.csv', delimiter = ',', usecols={2})

    tot_mse = []
    coor_predicted = []

    random_state_var = []
    i = 0
    while i < 1500:
        rand = random.randint(5,15)
        i += rand
        random_state_var.append(i)

    for n in random_state_var:

        dataset_train, dataset_test, clss_train, clss_test = train_test_split(dataset, clss, test_size=0.10, random_state=n)

        coor_to_predict = [[lat,long]]
        np_coor = np.array(coor_to_predict)

        l_reg = LinearRegression()
        y_pred = l_reg.fit(dataset_train, clss_train).predict(dataset_test)
        pred_coor = l_reg.fit(dataset_train, clss_train).predict(np_coor)
        score = mean_squared_error(clss_test,y_pred)
        tot_mse.append(score)
        coor_predicted.append(pred_coor)

    min = np.min(tot_mse)
    index = np.where(tot_mse == min)
    index = index[0][0]
    best_rm = coor_predicted[index]

    return best_rm

@csrf_exempt
def kmeans_c(request):
    n = int(request.POST.get("n_c"))
    cur = db.cursor()

    cur.execute("SELECT ST_AsGeoJson(posizione),rumore,timestamp,privacy FROM public.rilevazioni WHERE privacy='dummy' or privacy='no' or posizione in (SELECT posizione FROM rilevazioni where privacy='gpsp' and reale=false)")
    rilevazioni = cur.fetchall()

    cur.close()

    all = []
    for el in rilevazioni:
        all.append(el[0])

    if n>=len(all):
        return HttpResponse("Maggiore")
    coordinates = []
    for el in all:
        dict = json.loads(el)
        coordinates.append(dict.get("coordinates"))
    
    coordinates = np.array(coordinates)
    kmeans = KMeans(n_clusters=int(n))
    kmeans.fit(coordinates)
    clusters = kmeans.fit_predict(coordinates)

    item_clusterized = []
    for i in range(0,len(clusters)):
        dict = {
            "lat":coordinates[i][0],
            "long":coordinates[i][1],
            "cluster": str(clusters[i])
        } 
        item_clusterized.append(dict)
    
    clusters_array = []
    for i in range(0,n):
        newlist = [json.dumps(x) for x in item_clusterized if x.get("cluster") == str(i)]

        clusters_array.append(newlist)

    return HttpResponse(clusters_array)



