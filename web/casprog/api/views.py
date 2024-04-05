from rest_framework.response import Response
from rest_framework import status
from rest_framework.decorators import api_view
from django.shortcuts import render
import psycopg2
from datetime import datetime, timedelta
import json
from .qos import qosDummy, qosGPSP, qosNoP

db = psycopg2.connect(host="127.0.0.1", port = 5432, database="progetto", user="manu", password="7894")

def rumore_medio_gpsp_noprivacy(geojson):

    cur = db.cursor()

    rmedio = -1

    try:

        query = "SELECT AVG(rumore) FROM public.rilevazioni WHERE ST_DWithin(public.rilevazioni.posizione::geography,ST_GeomFromGeoJSON('"+geojson+"')::geography,3000) and (privacy='dummy' or privacy='no' or posizione in (SELECT posizione FROM rilevazioni where privacy='gpsp' and reale=false));"

        cur.execute(str(query))
        db.commit()
        rm = cur.fetchall()[0][0]
        
        if rm != None :
            rmedio = rm
    except:
        db.rollback()

    return rmedio

def rumore_medio_dummy(geojson):
    qos = -1
    data = { "rumore_medio": [], "posizione": [], "qos":qos}

    cur = db.cursor()

    features = geojson.get("features")

    try:
        for el in features:
            query = "SELECT AVG(rumore) FROM public.rilevazioni WHERE ST_DWithin(public.rilevazioni.posizione::geography,ST_GeomFromGeoJSON('"+str(el.get("geometry")).replace("'","\"")+"')::geography,300) and (privacy='dummy' or privacy='no' or posizione in (SELECT posizione FROM rilevazioni where privacy='gpsp' and reale=false));"
            cur.execute(str(query))
            db.commit()
            rm = cur.fetchall()[0][0]
            if rm != None:
                data.get("rumore_medio").append(rm)
                data.get("posizione").append(el.get("geometry").get("coordinates"))  
            else:
                data.get("rumore_medio").append(-1)
                data.get("posizione").append(el.get("geometry").get("coordinates"))  
    except:
        db.rollback()

    data["qos"] = qosDummy(geojson)
    cur.close()

    return data 

@api_view(['POST'])
def postAVGNoiseGPSP(request):
    gj = request.data.get('geojson')
    geojson = json.loads(gj)

    point_t = ""
    point_f = ""

    for el in geojson["features"]:
        reale = el.get("properties").get("Reale")
        if reale == "true":
            point_t = str(el.get("geometry")).replace("'","\"")
        else:
            print(reale)
            point_f = str(el.get("geometry")).replace("'","\"")
            print(point_f)

    rmedio = rumore_medio_gpsp_noprivacy(point_f)
    qos = qosGPSP(point_f,point_t)
    #invia anche il qos
    data = { "rumore_medio": rmedio, "qos":qos }

    return Response(data, status=status.HTTP_200_OK)

@api_view(['POST'])
def postAVGNoiseNoPrivacy(request):
    geojson = request.data.get('geojson')
    
    rmedio = rumore_medio_gpsp_noprivacy(geojson)
    #invia anche il qos
    qos = qosNoP(geojson)
    data = { "rumore_medio": rmedio, "qos": qos }

    return Response(data, status=status.HTTP_200_OK)

@api_view(['POST'])
def postAVGNoiseDummy(request):

    gj = request.data.get('geojson')
    geojson = json.loads(gj)

    data = rumore_medio_dummy(geojson)

    return Response(data, status=status.HTTP_200_OK)


@api_view(['POST'])
def postInsertRilevazioniGPSP(request):

    gj = request.data.get('geojson')
    geojson = json.loads(gj)
    rmedio = 0.0
    cur = db.cursor()

    #ce ne sono due, una posizione perturbata e una reale
    features = geojson.get("features")

    point_t = ""
    point_f = ""

    for el in features:
        p = el["geometry"]
        pt = str(p).replace("'","\"")
        point = str(pt)
        properties = el["properties"]
        rumore = properties["Rumore"]
        bool_reale = properties["Reale"]
        if bool_reale == "false":
            rmedio = rumore_medio_gpsp_noprivacy(point)
            point_f = point
        else:
            point_t = point
        dt = datetime.now() + timedelta(hours=2)
        date = dt.strftime("%d/%m/%Y")
        ora = dt.strftime("%H:%M:%S")
        timestamp = date + " " + ora
        
        query = "INSERT INTO rilevazioni (posizione,rumore,timestamp,reale,privacy) VALUES (ST_GeomFromGeoJSON('"+point+"'),"+str(rumore)+", TIMESTAMP '"+timestamp+"',"+bool_reale+",'gpsp')"

        cur.execute(str(query))
        db.commit()         


    cur.close()
    qos = qosGPSP(point_f,point_t)
    #invia anche il qos
    data = {
        "rumore_medio": rmedio,
        "qos": qos
    }

    return Response(data, status=status.HTTP_201_CREATED)

@api_view(['POST'])
def postInsertRilevazioniNoPrivacy(request):

    gj = request.data.get('geojson')
    geojson = json.loads(gj)
    
    cur = db.cursor()

    p = geojson["geometry"]
    pt = str(p).replace("'","\"")
    point = str(pt)
    rmedio = rumore_medio_gpsp_noprivacy(point)
    properties = geojson["properties"]
    rumore = properties["Rumore"]
    dt = datetime.now() + timedelta(hours=2)
    date = dt.strftime("%d/%m/%Y")
    ora = dt.strftime("%H:%M:%S")
    timestamp = date + " " + ora
    query = "INSERT INTO rilevazioni (posizione,rumore,timestamp,reale,privacy) VALUES (ST_GeomFromGeoJSON('"+point+"'),"+str(rumore)+", TIMESTAMP '"+timestamp+"', true, 'no')"
    cur.execute(str(query))
    db.commit()         
    qos = qosNoP(point)
    #invia anche il qos
    data = {
        "rumore_medio": rmedio,
        "qos": qos
    }

    return Response(data, status=status.HTTP_201_CREATED)

@api_view(['POST'])
def postInsertRilevazioniDUMMY(request):

    gj = request.data.get('geojson')
    geojson = json.loads(gj)
    print(gj)
    data = rumore_medio_dummy(geojson)
    cur = db.cursor()

    #ce ne sono due, una posizione perturbata e una reale
    features = geojson.get("features")

    for el in features:
        p = el["geometry"]
        pt = str(p).replace("'","\"")
        point = str(pt)
        properties = el["properties"]
        rumore = properties["Rumore"]
        bool_reale = properties["Reale"]
        dt = datetime.now() + timedelta(hours=2)
        date = dt.strftime("%d/%m/%Y")
        ora = dt.strftime("%H:%M:%S")
        timestamp = date + " " + ora
        query = "INSERT INTO rilevazioni (posizione,rumore,timestamp,reale,privacy) VALUES (ST_GeomFromGeoJSON('"+point+"'),"+str(rumore)+", TIMESTAMP '"+timestamp+"',"+bool_reale+",'dummy')"
        cur.execute(str(query))
        db.commit()          

    #invia anche il qos

    return Response(data, status=status.HTTP_201_CREATED)


