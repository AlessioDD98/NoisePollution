import json
import psycopg2

db = psycopg2.connect(host="127.0.0.1", port = 5432, database="progetto", user="manu", password="7894")

def qosGPSP(point_false,point_true):

    cur = db.cursor()

    mse = 0
    rmedio_true = 0.0
    rmedio_false = 0.0
    divisore = 0

    query_rmedio_true = "SELECT AVG(rumore) FROM public.rilevazioni WHERE reale=true AND ST_DWithin(public.rilevazioni.posizione::geography,ST_GeomFromGeoJSON('"+point_true+"')::geography,3000);"
    query_rmedio_false = "SELECT AVG(rumore) FROM public.rilevazioni WHERE ST_DWithin(public.rilevazioni.posizione::geography,ST_GeomFromGeoJSON('"+point_false+"')::geography,3000) and (privacy='dummy' or privacy='no' or posizione in (SELECT posizione FROM rilevazioni where privacy='gpsp' and reale=false));"

    cur.execute(query_rmedio_false)
    rmedio_false = cur.fetchall()[0][0]
    cur.execute(query_rmedio_true)
    rmedio_true = cur.fetchall()[0][0]
    

    if rmedio_false != None and rmedio_true != None:
        mse += (rmedio_false - rmedio_true)**2
        divisore += 1

    
    if divisore != 0:
        qos = mse / divisore
    else:
        qos = (-1)

    cur.close()

    return qos 

def qosDummy(geojson):
    
    cur = db.cursor()

    mse = 0
    rmedio_true = 0
    rmedio_false = []
    feat = geojson.get("features")

    for el in feat:

        point = str(el.get("geometry")).replace("'","\"")
        reale = el.get("properties").get("Reale")
        
        if reale == "true":

            query_rmedio_true = "SELECT AVG(rumore) FROM public.rilevazioni WHERE reale=true AND ST_DWithin(public.rilevazioni.posizione::geography,ST_GeomFromGeoJSON('"+point+"')::geography,3000);"

            try:
                cur.execute(query_rmedio_true)
                rmedio_true = cur.fetchall()[0][0]
            except:
                db.rollback()

        else:

            query_rmedio_false = "SELECT AVG(rumore) FROM public.rilevazioni WHERE ST_DWithin(public.rilevazioni.posizione::geography,ST_GeomFromGeoJSON('"+point+"')::geography,3000) and (privacy='dummy' or privacy='no' or posizione in (SELECT posizione FROM rilevazioni where privacy='gpsp' and reale=false));"

            try:
                cur.execute(query_rmedio_false)
                rmedio_false.append(cur.fetchall()[0][0])
                
            except:
                db.rollback()
            
    divisore = 0
    for i in range(0,len(rmedio_false)):
        if rmedio_false[i] != None and rmedio_true != None:
            mse += (rmedio_false[i]-rmedio_true)**2
            divisore += 1

    if divisore != 0:
        qos = mse / divisore
    else:
        qos = (-1)

    cur.close()

    return qos 

def qosNoP(point):

    cur = db.cursor()
    divisore = 0
    mse = 0

    query_rmedio_true = "SELECT AVG(rumore) FROM public.rilevazioni WHERE reale=true AND ST_DWithin(public.rilevazioni.posizione::geography,ST_GeomFromGeoJSON('"+point+"')::geography,3000);"
    query_rmedio_false = "SELECT AVG(rumore) FROM public.rilevazioni WHERE ST_DWithin(public.rilevazioni.posizione::geography,ST_GeomFromGeoJSON('"+point+"')::geography,3000) and (privacy='dummy' or privacy='no' or posizione in (SELECT posizione FROM rilevazioni where privacy='gpsp' and reale=false));"

    cur.execute(query_rmedio_false)
    rmedio_false = cur.fetchall()[0][0]
    cur.execute(query_rmedio_true)
    rmedio_true = cur.fetchall()[0][0]

    if rmedio_false != None and rmedio_true != None:
        mse += (rmedio_false - rmedio_true)**2
        divisore += 1

    if divisore != 0:
        qos = mse / divisore
    else:
        qos = (-1)

    cur.close()

    return qos
