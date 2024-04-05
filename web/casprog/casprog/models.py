from django.db import models
#from django.contrib.gis.geos import Point
from django.contrib.gis.db import models

class Rilevazioni(models.Model):
    posizione = models.GeometryField()
    rumore = models.FloatField()
    timestamp = models.DateTimeField()
    
    def __str__(self):
        return "pos: " + str(self.posizione) + ",noise: " + str(self.rumore) + ",date: "+ str(self.timestamp)


class ApiRumoreMedio(models.Model):
    rumore_medio = models.FloatField()
    timestamp = models.CharField(max_length=50)
    def __str__(self):
        return self.rumore_medio