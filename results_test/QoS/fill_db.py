import numpy as np
import psycopg2
import random
from datetime import datetime,timedelta
import json
import math
import statistics
from qos import qosGPSP,qosDummy,qosNoP

def gpsp():

    for i in range(0,100):

        lat = round(random.uniform(11.3, 11.4), 9)
        long = round(random.uniform(44.45, 44.55), 9)
        gj = "{\"type\":\"Point\", \"coordinates\":["+str(lat)+","+str(long)+"] }"
        latf = str(lat)[:6]
        longf = str(long)[:6]

        gjf = "{\"type\":\"Point\", \"coordinates\":["+latf+","+longf+"] }"

        rumore = round(random.uniform(40, 60), 8)
        dt = datetime.now() + timedelta(hours=2)
        date = dt.strftime("%d/%m/%Y")
        ora = dt.strftime("%H:%M:%S")
        timestamp = date + " " + ora

        query = "INSERT INTO rilevazioni (posizione,rumore,timestamp, reale, privacy) VALUES (ST_GeomFromGeoJSON('"+gj+"'),"+str(rumore)+", TIMESTAMP '"+timestamp+"',true,'gpsp')"
        cur.execute(str(query))
        db.commit()        
        query = "INSERT INTO rilevazioni (posizione,rumore,timestamp, reale, privacy) VALUES (ST_GeomFromGeoJSON('"+gjf+"'),"+str(rumore)+", TIMESTAMP '"+timestamp+"',false,'gpsp')"
        cur.execute(str(query))
        db.commit()   

def dummy():

    for i in range(0,100):

        rumore = round(random.uniform(40, 80), 8)
        dt = datetime.now() + timedelta(hours=2)
        date = dt.strftime("%d/%m/%Y")
        ora = dt.strftime("%H:%M:%S")
        timestamp = date + " " + ora

        lat = round(random.uniform(11.3, 11.4), 9)
        long = round(random.uniform(44.45, 44.55), 9)
        gj = "{\"type\":\"Point\", \"coordinates\":["+str(lat)+","+str(long)+"] }"
    
        for j in range(0,5):  
                lat_long = testranpos(long, lat)
                gjf = "{\"type\":\"Point\", \"coordinates\":["+str(lat_long[0])+","+str(lat_long[1])+"] }"
                query = "INSERT INTO rilevazioni (posizione,rumore,timestamp, reale, privacy) VALUES (ST_GeomFromGeoJSON('"+gjf+"'),"+str(rumore)+", TIMESTAMP '"+timestamp+"',false,'dummy')"
                cur.execute(str(query))
                db.commit()  
    
        query = "INSERT INTO rilevazioni (posizione,rumore,timestamp, reale, privacy) VALUES (ST_GeomFromGeoJSON('"+gj+"'),"+str(rumore)+", TIMESTAMP '"+timestamp+"',true,'dummy')"
        cur.execute(str(query))
        db.commit()   

def nop():

    for i in range(0,100):

        lat = round(random.uniform(11.3, 11.4), 9)
        long = round(random.uniform(44.45, 44.55), 9)
        gj = "{\"type\":\"Point\", \"coordinates\":["+str(lat)+","+str(long)+"] }"
        
        rumore = round(random.uniform(40, 60), 8)
        dt = datetime.now() + timedelta(hours=2)
        date = dt.strftime("%d/%m/%Y")
        ora = dt.strftime("%H:%M:%S")
        timestamp = date + " " + ora

        query = "INSERT INTO rilevazioni (posizione,rumore,timestamp, reale, privacy) VALUES (ST_GeomFromGeoJSON('"+gj+"'),"+str(rumore)+", TIMESTAMP '"+timestamp+"',true,'no')"
        cur.execute(str(query))
        db.commit()     

def calc_qos():
    query_gpsp = "SELECT ST_AsGeoJSON(posizione) FROM rilevazioni where privacy='gpsp'"  
    cur.execute(query_gpsp)
    gpsp=cur.fetchall()
    query_dummy = "SELECT ST_AsGeoJSON(posizione) FROM rilevazioni where privacy='dummy'"
    cur.execute(query_dummy)
    dummy=cur.fetchall()
    query_nop = "SELECT ST_AsGeoJSON(posizione) FROM rilevazioni where privacy='no'"
    cur.execute(query_nop)
    no=cur.fetchall()

    new_g = []
    new_d = []
    new_n = []

    for i in range(0,len(gpsp),2):
        temp = [gpsp[i][0],gpsp[i+1][0]]
        new_g.append(temp)
    
    for i in range(0,len(dummy),6):
        temp = []
        for j in range(i,i+6):
            temp.append(dummy[j][0])
        new_d.append(temp)

    for i in range(0,len(no)):
        new_n.append(no[i][0])

    index_ran = []

    for i in range(0,50):
        ran = random.randint(0,199)
        while ran in index_ran:
            ran = random.randint(0,199)
        index_ran.append(ran)

    qos_arr_g = []
    qos_arr_d = []
    qos_arr_n = []

    for n in index_ran:
        print(len(new_g), " ", n)
        qos_arr_g.append(new_g[n])
        qos_arr_d.append(new_d[n])
        qos_arr_n.append(new_n[n])

    qos_g = []
    qos_d = []
    qos_n = []

    header = "False,Vere,QoS\n"

    f = open('gpsp.csv','w')
    f.write(header)
    for el in qos_arr_g:
        qos = qosGPSP(el[1],el[0])
        qos_g.append(qos)
        line = ""+str(json.loads(el[1]).get("coordinates"))+","+str(json.loads(el[0]).get("coordinates"))+","+str(qos)+"\n"
        f.write(line)
    f.close()

    f = open('nop.csv','w')
    f.write(header)
    for el in qos_arr_n:
        qos = qosNoP(el)
        qos_n.append(qos)
        line = ""+str(json.loads(el).get("coordinates"))+","+str(qos)+"\n"
        f.write(line)
    f.close()
    
    f = open('dummy.csv','w')
    f.write(header)
    for el in qos_arr_d:
        dict = {'features':[]}
        k = 0
        for elem in el:
            feat = {}
            if k == len(el)-1:
                feat = {'geometry':json.loads(elem), 'properties':{'Reale':'true'} }
            else:
                feat = {'geometry':json.loads(elem), 'properties':{'Reale':'false'} }
            k += 1
            dict.get("features").append(feat)
        qos = qosDummy(dict)
        qos_d.append(qos)
        for l in range(0,5):
            line = ""+str(json.loads(el[l]).get("coordinates"))+","+str(json.loads(el[5]).get("coordinates"))+","+str(qos)+"\n"
            f.write(line)
    f.close()

    f = open('statistics.csv','a')
    f.write("\nAlgoritmi,Media,Mediana,Differenza Media-Mediana\n")
    
    f.write("GPSP,"+str(statistics.mean(qos_g))+ ","+ str(statistics.median(qos_g))+ ","+ str((statistics.mean(qos_g)-statistics.median(qos_g)))+"\n")
    f.write("Dummy,"+str(statistics.mean(qos_d))+ ","+ str(statistics.median(qos_d))+ ","+ str((statistics.mean(qos_d)-statistics.median(qos_d)))+"\n")
    f.write("NoP,"+str(statistics.mean(qos_n))+ ","+ str(statistics.median(qos_n))+ ","+ str((statistics.mean(qos_n)-statistics.median(qos_n)))+"\n")
        
       
def testranpos(x0,y0):

    radious_in_degrees = 3000/111000.0

    u = random.random()
    v = random.random()
    w = radious_in_degrees * math.sqrt(u)
    t = 2 * math.pi * v
    x = w * math.cos(t)
    y = w * math.sin(t)

    new_x = x / math.cos(math.radians(y0))
    foundLongitude = new_x + x0
    foundLatitude = y + y0
    #print(foundLatitude, foundLongitude)

    return [foundLatitude, foundLongitude]
        
if __name__=="__main__":

    db = psycopg2.connect(host="127.0.0.1", port = 5432, database="progetto", user="manu", password="7894")

    cur = db.cursor()
    # gpsp()
    # dummy()
    # nop()
    calc_qos()
    #testranpos(11.43244,44.874230)
    cur.close()

















