import numpy as np
import psycopg2
import random
import statistics

from sklearn.model_selection import train_test_split
from sklearn.linear_model import LinearRegression
from sklearn.metrics import mean_squared_error


db = psycopg2.connect(host="127.0.0.1", port = 5432, database="progetto", user="manu", password="7894")

def fill_dataset():

    f = open('dataset.csv', 'a')
    for i in range(1,10000):
        lat = str(round(random.uniform(-50, 50), 6))
        lon = str(round(random.uniform(-120, 120), 6))
        v1 = str(lat+','+lon)
        v2 = str(round(random.uniform(40.0, 70.0), 9))
        
        f.write(v1+','+v2+'\n')

    f.close()

def prova():
    #interpolazione dei dati
    #oppure ML
    dataset = np.genfromtxt('dataset.csv', delimiter = ',', usecols={0,1})
    clss = np.genfromtxt('dataset.csv', delimiter = ',', usecols={2})

    f = open('results.csv', 'w')
    f.write("Lat,Long,Rumore,MSE")
    min_mse = []

    for i in range(0,50):

        lat = str(round(random.uniform(-60, 60), 6))
        lon = str(round(random.uniform(-130, 130), 6))

        random_state_var = []
        i = 0

        while i < 1500:
            rand = random.randint(5,15)
            i += rand
            random_state_var.append(i)
        
        tot_mse = []
        coor_predicted = []

        for n in random_state_var:

            dataset_train, dataset_test, clss_train, clss_test = train_test_split(dataset, clss, test_size=0.10, random_state=n)

            coor_to_predict = [[lat,lon]]
            np_coor = np.array(coor_to_predict)

            #HO CANCELLATO IL DUMMY CLASSIFIER PERCHÈ NELLE PROVE NON È MAI USCITO COME IL MIGLIORE

            #LI FACCIO ANDARE PER 100 VOLTE E VEDO QUALE HA AVUTO PER PIÙ VOLTE IL MINIMO E SCELGO QUELLO
            #HA VINTO RANDOM FOREST

            """ gnb = GaussianNB()
            y_pred = gnb.fit(dataset_train, clss_train).predict(dataset_test)
            #pred_coor = gnb.fit(dataset_train, clss_train).predict(np_coor)
            print("GaussianNB: ", mean_squared_error(clss_test,y_pred))
            v1 = mean_squared_error(clss_test,y_pred)

            perceptron_classifier = Perceptron(max_iter=500, random_state=None)
            y_pred = perceptron_classifier.fit(dataset_train, clss_train).predict(dataset_test)
            #pred_coor = perceptron_classifier.fit(dataset_train, clss_train).predict(np_coor)
            print('Perceptron classifier score: ', mean_squared_error(clss_test,y_pred)) 
            v2 = mean_squared_error(clss_test,y_pred) """

            #TESTATO IL RANDOM FOREST C. CON LE REGRESSIONI E DA RISULTATI PEGGIORI CON MSE
            #DAI TEST IL MIGLIORE È LA REGRESSIONE LINEARE SEMPLICE

            """ random_forest = RandomForestClassifier(n_estimators=10, max_depth=5, random_state=None)
            y_pred = random_forest.fit(dataset_train, clss_train_rf).predict(dataset_test)
            #pred_coor = random_forest.fit(dataset_train, clss_train).predict(np_coor)
            print('Random forest classifier score: ', random_forest.score(dataset_test,clss_test_rf))
            print("mse rfc: ",mean_squared_error(clss_test_rf,y_pred))
            

            random_forest = RandomForestRegressor(n_estimators=10, max_depth=5, random_state=None)
            y_pred = random_forest.fit(dataset_train, clss_train).predict(dataset_test)
            #pred_coor = random_forest.fit(dataset_train, clss_train).predict(np_coor)
            print('Random forest classifier score: ', random_forest.score(dataset_test,clss_test))
            print("mse rfr: ",mean_squared_error(clss_test,y_pred))
            tot_mse.append(r2_score(clss_test,y_pred)) """

            l_reg = LinearRegression()
            y_pred = l_reg.fit(dataset_train, clss_train).predict(dataset_test)
            pred_coor = l_reg.fit(dataset_train, clss_train).predict(np_coor)
            score = mean_squared_error(clss_test,y_pred)
            tot_mse.append(score)
            coor_predicted.append(pred_coor)
        
        min = np.min(tot_mse)
        min_mse.append(min)
        index = np.where(tot_mse == min)
        index = index[0][0]
        best_rm = coor_predicted[index]
        f.write("\n"+str(lat)+","+str(lon)+","+str(best_rm)[1:-1]+","+str(min))
    f.close()

    print(statistics.mean(min_mse))



#fill_dataset()
prova()









