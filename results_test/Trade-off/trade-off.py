import psycopg2
import random
import json
from qos import qosGPSP

db = psycopg2.connect(host="127.0.0.1", port = 5432, database="progetto", user="manu", password="7894")

def calc():
    cur = db.cursor()

    query_gpsp = "SELECT ST_AsGeoJSON(posizione) FROM rilevazioni where privacy='gpsp' and reale='true'"  
    cur.execute(query_gpsp)
    gpsp=cur.fetchall()

    new_g = []

    for i in range(0,len(gpsp),2):
        temp = [gpsp[i][0],gpsp[i+1][0]]
        new_g.append(temp)
        print(temp)

    random_index = []

    for i in range(0,10):
        r = random.randint(0,len(new_g)-1)
        while r in random_index:
            r = random.randint(0,len(new_g)-1)
        random_index.append(r)

    alfa = [0.25,0.5,0.75,1]
    headline = "Lat,Long,a=0.25,a=0.5,a=0.75,a=1\n"
    f = open("trade-off.csv",'w')
    f.write(headline)
    for n in random_index:
        trade_off_array = []
        distance_array = []
        for a in alfa:
            gj = json.loads(new_g[n][0])
            coor = gj["coordinates"]

            lat = str(coor[0])
            long = str(coor[1])

            point_idx = lat.index(".")
            decimal_lat = len(lat)-point_idx
            decimal_lat = a*decimal_lat
            lat_f = lat[:point_idx+int(decimal_lat)+1]

            point_idx = long.index(".")
            decimal_long = len(long)-point_idx-1
            decimal_long = a*decimal_long
            long_f = long[:point_idx+int(decimal_long)+1]

            coor_f = [float(lat_f),float(long_f)]
            gj_f = gj
            gj_f["coordinates"] = coor_f
            gj_f = str(gj_f).replace("'","\"")
            gj = new_g[n][0]
            """ print(gj)
            print(gj_f) """

            cur.execute("SELECT ST_Distance(ST_GeomFromGeoJSON('"+gj+"')::geography,ST_GeomFromGeoJSON('"+gj_f+"')::geography)")
            distance = cur.fetchone()

            qos = qosGPSP(gj_f,gj)

            if qos != -1:
                qos_range = (qos*100)/35
                distance_range = (distance[0]*100)/60000
                trade_off = (a*qos_range)+((1-a)*float(distance_range))

            else:
                trade_off = -1
            trade_off_array.append(trade_off)

        line = lat+","+long+","+str(trade_off_array[0])+","+str(trade_off_array[1])+","+str(trade_off_array[2])+","+str(trade_off_array[3])+"\n"
        f.write(line)
    f.close()


if __name__ == "__main__":
    calc()

    print(qosGPSP("{\"type\":\"Point\", \"coordinates\":[11.318482215,44.490048222]}","{\"type\":\"Point\", \"coordinates\":[11.318482215,44.490048222]}"))