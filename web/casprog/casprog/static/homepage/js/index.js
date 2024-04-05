//MAP INITIALIZATION
var map = new ol.Map({
  target: 'map',
  layers: [],
  view: 
    new ol.View({
      center: ol.proj.fromLonLat([0, 0]),
      zoom: 0
    })
});

var osmLayer = new ol.layer.Tile({
    source: 
      new ol.source.OSM({
      })
});

osmLayer.setVisible(true);
map.addLayer(osmLayer);

$('#kmeans_clustering').css('display','none')

//USERS POSITIONS WITH DESCRIPTION
var filltext = new ol.style.Fill({
  color: 'rgba(0,0,0,1)'
});

var positionstyle_dummy = new ol.style.Style({
  image: new ol.style.Circle({
    fill: new ol.style.Fill({
      color: 'rgba(255,0,0,0.4)'
    }),
    stroke: new ol.style.Stroke({
      color: '#FF0000',
      width: 1.25
    }),
    radius: 6
  }),
  fill: new ol.style.Fill({
    color: 'rgba(255,0,0,0.4)'
  }),
  stroke: new ol.style.Stroke({
    color: '#FF0000',
    width: 1.25
  })
})

var positionstyle_gpsp = new ol.style.Style({
  image: new ol.style.Circle({
    fill: new ol.style.Fill({
      color: 'rgba(0,255,0,0.4)'
    }),
    stroke: new ol.style.Stroke({
      color: '#00FF00',
      width: 1.25
    }),
    radius: 6
  }),
  fill: new ol.style.Fill({
    color: 'rgba(0,255,0,0.4)'
  }),
  stroke: new ol.style.Stroke({
    color: '#00FF00',
    width: 1.25
  })
})

var positionstyle_noprivacy = new ol.style.Style({
  image: new ol.style.Circle({
    fill: new ol.style.Fill({
      color: 'rgba(0,0,255,0.4)'
    }),
    stroke: new ol.style.Stroke({
      color: '#0000FF',
      width: 1.25
    }),
    radius: 6
  }),
  fill: new ol.style.Fill({
    color: 'rgba(0,0,255,0.4)'
  }),
  stroke: new ol.style.Stroke({
    color: '#0000FF',
    width: 1.25
  })
})

var positionstyle_all = new ol.style.Style({
  image: new ol.style.Circle({
    fill: new ol.style.Fill({
      color: 'rgba(255,255,0,0.4)'
    }),
    stroke: new ol.style.Stroke({
      color: '#FFFF00',
      width: 1.25
    }),
    radius: 6
  }),
  fill: new ol.style.Fill({
    color: 'rgba(255,255,0,0.4)'
  }),
  stroke: new ol.style.Stroke({
    color: '#FFFF00',
    width: 1.25
  })
})

var feature_collection_gpsp = {"type": "FeatureCollection", "features": []}
var feature_collection_dummy = {"type": "FeatureCollection", "features": []}
var feature_collection_noprivacy = {"type": "FeatureCollection", "features": []}
var feature_collection_all = {"type": "FeatureCollection", "features": []}

for(var i = 0;i<json_pos_dummy.length;i++){
  var feature = {
    "type": "Feature",
    "geometry": JSON.parse(json_pos_dummy[i]),
    "properties": {
      "Rumore": rumori_dummy[i],
      "Timestamp": timestamp_dummy[i],
      "Algoritmo": "Dummy Updates"
    }
  }
  feature_collection_dummy['features'].push(feature)
  feature_collection_all['features'].push(feature)
}

for(var i = 0;i<json_pos_gpsp.length;i++){
  var feature = {
    "type": "Feature",
    "geometry": JSON.parse(json_pos_gpsp[i]),
    "properties": {
      "Rumore": rumori_gpsp[i],
      "Timestamp": timestamp_gpsp[i],
      "Algoritmo": "GPS Perturbation"
    }
  }
  feature_collection_gpsp['features'].push(feature)
  feature_collection_all['features'].push(feature)
}

for(var i = 0;i<json_pos_noprivacy.length;i++){
  var feature = {
    "type": "Feature",
    "geometry": JSON.parse(json_pos_noprivacy[i]),
    "properties": {
      "Rumore": rumori_noprivacy[i],
      "Timestamp": timestamp_noprivacy[i],
      "Algoritmo": "No Privacy"
    }
  }
  feature_collection_noprivacy['features'].push(feature)
  feature_collection_all['features'].push(feature)
}

//console.log(JSON.stringify(feature_collection))

var features_dummy = new ol.format.GeoJSON().readFeatures(feature_collection_dummy, {
  dataProjection: "EPSG:4326",
  featureProjection: "EPSG:3857"
}); 

var features_gpsp = new ol.format.GeoJSON().readFeatures(feature_collection_gpsp, {
  dataProjection: "EPSG:4326",
  featureProjection: "EPSG:3857"
}); 

var features_noprivacy = new ol.format.GeoJSON().readFeatures(feature_collection_noprivacy, {
  dataProjection: "EPSG:4326",
  featureProjection: "EPSG:3857"
}); 

var features_all = new ol.format.GeoJSON().readFeatures(feature_collection_all, {
  dataProjection: "EPSG:4326",
  featureProjection: "EPSG:3857"
});
/* var urlsource = new ol.source.Vector({
  features: features
}) */

var urlsource_dummy = new ol.source.Vector({
  features: features_dummy
})

var positions_dummy = new ol.layer.Vector({
  title: 'positions Layer',
  source: urlsource_dummy,
  style: positionstyle_dummy
});

var urlsource_gpsp = new ol.source.Vector({
  features: features_gpsp
})

var positions_gpsp = new ol.layer.Vector({
  title: 'positions Layer',
  source: urlsource_gpsp,
  style: positionstyle_gpsp
});

var urlsource_noprivacy = new ol.source.Vector({
  features: features_noprivacy
})

var positions_noprivacy = new ol.layer.Vector({
  title: 'positions Layer',
  source: urlsource_noprivacy,
  style: positionstyle_noprivacy
});

var urlsource_all = new ol.source.Vector({
  features: features_all
})

var positions_all = new ol.layer.Vector({
  title: 'positions Layer',
  source: urlsource_all,
  style: positionstyle_all
});

$('#kmeansform').hide();
$('input:radio[name=visual]:nth(0)').attr('checked',true);

positions_dummy.setVisible(true)
map.addLayer(positions_dummy);

positions_gpsp.setVisible(true)
map.addLayer(positions_gpsp);

positions_noprivacy.setVisible(true)
map.addLayer(positions_noprivacy);

positions_all.setVisible(false)
map.addLayer(positions_all);

function setGPSPpos() {
  if(document.getElementById('gpsp').checked){
    positions_gpsp.setVisible(true)
  }
  else{
    positions_gpsp.setVisible(false)
  }
}

function setDummypos() {

  if(document.getElementById('dummy').checked){
    positions_dummy.setVisible(true)
  }
  else{
    positions_dummy.setVisible(false)
  }
}

function setNoPripos() {
  if(document.getElementById('noprivacy').checked){
    positions_noprivacy.setVisible(true)
  }
  else{
    positions_noprivacy.setVisible(false)
  }
}

//popup position and noise
const container = document.getElementById('popup');
const content = document.getElementById('popup-content');
const closer = document.getElementById('popup-closer');

var popup = new ol.Overlay({
  element: container,
  autoPan: true,
  autoPanAnimation: {
    duration: 250,
  },
});

//popup closer
closer.onclick = function () {
  popup.setPosition(undefined);
  closer.blur();
  return false;
};

map.addOverlay(popup);

var pos = 0
var features_popup_array = []

function popupString(e, features, len){
  pos = 0
  const coordinate_popup = e.coordinate
  if(len == 1){
    const feat_coord = features[0].getGeometry().getCoordinates()
    const coordinate = ol.proj.transform(feat_coord, 'EPSG:3857', 'EPSG:4326');
    
    //$(element).popover('dispose');
    content.innerHTML = '<div id="popover-content"><span>Posizione:</span><br>' +
                        '<span class="popupLatLon">Lat: ' + coordinate[0] + '</span><br>' + 
                        '<span class="popupLatLon">Long: ' + coordinate[1] + '</span><br>' + 
                        '<span>Rumore: '+ features[0].get("Rumore") +'</span><br>' +
                        '<span>Timestamp: '+ features[0].get("Timestamp") +'</span><br>'+
                        '<span>Algoritmo: '+ features[0].get("Algoritmo") +'</span><br></div>' 
    popup.setPosition(coordinate_popup);
  } else {
    features_popup_array = []
    for (var i = 0; i<len; i++){
      var feat_coord = features[i].getGeometry().getCoordinates()
      var coordinate = ol.proj.transform(feat_coord, 'EPSG:3857', 'EPSG:4326');
      innerHTML = '<div id="popover-content"><span>Ci sono ' + len + ' posizioni:</span><br>' +
                  '<span>Posizione ' + (i+1) + '</span><br>'+
                  '<span class="popupLatLon">Lat: ' + coordinate[0] + '</span><br>' + 
                  '<span class="popupLatLon">Long: ' + coordinate[1] + '</span><br>' + 
                  '<span>Rumore: '+ features[i].get("Rumore") +'</span><br>' +
                  '<span>Timestamp: '+ features[i].get("Timestamp") +'</span><br>' +
                  '<span>Algoritmo: '+ features[i].get("Algoritmo") +'</span><br><br>' +
                  '<input type="button" value="Prev" name="prev" id="prevPos" onclick="prevPopup()">' +
                  '<input type="button" value="Next" name="next" id="nextPos" onclick="nextPopup()"></div>'
      
      features_popup_array.push(innerHTML)
    }
    content.innerHTML = features_popup_array[0]
    popup.setPosition(coordinate_popup); 
  }
}



function nextPopup(){
  len = features_popup_array.length
  if(pos != len-1){
    console.log("next")
    pos += 1
    content.innerHTML = features_popup_array[pos]
  }
}

function prevPopup(){
  if(pos != 0){
    pos -= 1
    content.innerHTML = features_popup_array[pos]
  }
}

function popupPositions(e) {

  const features = map.getFeaturesAtPixel(e.pixel)
  
  if(features){
    if (features.length > 1) {
      popupString(e, features, features.length)
      
    }
    else {
      popupString(e, features, 1)
    }
  }
  
}

//event listner for popup on position page
var singleclick_key = map.on('singleclick', popupPositions)

//CLASTERING
const distanceInput = document.getElementById('distance');
const minDistanceInput = document.getElementById('min-distance');

const clusterSource = new ol.source.Cluster({
  distance: parseInt(distanceInput.value, 10),
  minDistance: parseInt(minDistanceInput.value, 10),
  source: urlsource_all
});

const styleCache = {};
const clusters = new ol.layer.Vector({
  source: clusterSource,
  style: function (feature) {
    const size = feature.get('features').length;
    let style = styleCache[size];
    if (!style) {
      style = new ol.style.Style({
        image: new ol.style.Circle({
          radius: 10,
          stroke: new ol.style.Stroke({
            color: '#00FFFF',
            width: 1.25
          }),
          fill: new ol.style.Fill({
            color: 'rgba(0,255,255,0.4)'
          })
        }),
        text: new ol.style.Text({
          text: size.toString(),
          fill: filltext 
        }),
      });
      styleCache[size] = style;
    }
    return style;
  },
});

clusters.setVisible(false)
map.addLayer(clusters)

distanceInput.addEventListener('input', function () {
  clusterSource.setDistance(parseInt(distanceInput.value, 10));
});

minDistanceInput.addEventListener('input', function () {
  clusterSource.setMinDistance(parseInt(minDistanceInput.value, 10));
});

function popupClusters(e){

  clusters.getFeatures(e.pixel).then((clickedFeatures) => {
    if (clickedFeatures.length) {

      const features = clickedFeatures[0].get('features');
      const featuresLength = features.length
      if (featuresLength > 1) {
        all_equal = true

        for (var i = 0; i<featuresLength; i++){

          i_pos = features[i].getGeometry().getCoordinates()
          const coordinate = ol.proj.transform(i_pos, 'EPSG:3857', 'EPSG:4326');
          
          for (var j = 0; j<features.length;j++){
            feat_coor = features[j].getGeometry().getCoordinates()
            feature_pos = ol.proj.transform(feat_coor, 'EPSG:3857', 'EPSG:4326')
            if(coordinate[0] != feature_pos[0] && coordinate[1] != feature_pos[1] ){
              all_equal = false
              break;
            }
          }
        }
        
        console.log(all_equal)
        if (!all_equal){
          const extent = ol.extent.boundingExtent(
            features.map((r) => r.getGeometry().getCoordinates())
          );
          map.getView().fit(extent, {duration: 1000, padding: [100, 100, 100, 100]});
        } else { 
          popupString(e, features, featuresLength)
        }
      } 
      else {
        popupString(e, features, 1)
      } 
    }
  });
}

//event listener for popup with clusters
var clustersEvent = map.on('click', popupClusters)


/* 

  KMEANS CLUSTERING

 */

function create_style_clusters(color){

  var positionstyle_cluster = new ol.style.Style({
    image: new ol.style.Circle({
      fill: new ol.style.Fill({
        color: color
      }),
      stroke: new ol.style.Stroke({
        color: color,
        width: 1.25
      }),
      radius: 6
    }),
    fill: new ol.style.Fill({
      color: color
    }),
    stroke: new ol.style.Stroke({
      color: color,
      width: 1.25
    })
  })

  return positionstyle_cluster
}

function create_urlsource_clusters(cluster_features){

  var features_cluster = new ol.format.GeoJSON().readFeatures(cluster_features, {
    dataProjection: "EPSG:4326",
    featureProjection: "EPSG:3857"
  });

  var urlsource_cluster = new ol.source.Vector({
    features: features_cluster
  })

  return urlsource_cluster
}

const nClusters = document.getElementById('n-clusters')
var colors = ['#FF0000','#0000FF','#00FF00','#FFFF00','#FF00FF','#FFFFFF','#00FFFF','#990099','#009999','#999900','#999999']
var positions_kmeans_array = []
var n_clusters_val = $('#n_clusters_val')
n_clusters_val.html(nClusters.value)

nClusters.addEventListener('input', function () {
  $('#n_clusters_val').html(nClusters.value)
  kmeans_layer()
});

function azzerate_clusters_layer(){
  if(positions_kmeans_array){
    for(var i = 0; i<positions_kmeans_array.length; i++){
      positions_kmeans_array[i].setVisible(false)
      map.removeLayer(positions_kmeans_array[i])
    }
    positions_kmeans_array = []
  }
}

function kmeans_layer(){
  azzerate_clusters_layer()
  create_clusters()
}

function create_clusters(){

  var n = nClusters.value

  $.post("kmeans_clustering/", {"n_c":n}, function (data) {

    if(data == "Maggiore"){
      n_clusters_val.html("Numero di cluster maggiore del numero delle rilevazioni!")
    }
    else{
      data = data.substring(2,data.length-2)
      data = data.replaceAll("']['","', '")
      split = data.split("', '")
      
      json_clusters = []
      for(var i = 0; i<split.length; i++){
        json = JSON.parse(split[i])
        json_clusters.push(json)
      }
      console.log(json_clusters.length)
      console.log( feature_collection_all["features"].length)
      var iesimo_cluster = {"type": "FeatureCollection", "features": []}

      for(var i = 0; i<n; i++){
        iesimo_cluster = {"type": "FeatureCollection", "features": []}
        for(var j = 0;j<json_clusters.length; j++){
          if(json_clusters[j]["cluster"] == i.toString()){
            var lat = json_clusters[j]["lat"]
            var long = json_clusters[j]["long"]
            var trovato = false
            var index = 0
            for(var k = 0; k<feature_collection_all["features"].length; k++){
              var feat_lat = feature_collection_all["features"][k]["geometry"]["coordinates"][0]
              var feat_long = feature_collection_all["features"][k]["geometry"]["coordinates"][1]
              if(lat === feat_lat && long === feat_long){
                trovato = true
                index = k
              }
            }
            if(trovato)
              iesimo_cluster["features"].push(feature_collection_all["features"][index])
          }
        }
        color = colors[i]

        style = create_style_clusters(color)
        console.log(iesimo_cluster)
        urlsource = create_urlsource_clusters(iesimo_cluster)

        var pos_kmeans = new ol.layer.Vector({
          title: 'positions Layer',
          source: urlsource,
          style: style
        })
      
        positions_kmeans_array.push(pos_kmeans)

        positions_kmeans_array[i].setVisible(true)
        map.addLayer(positions_kmeans_array[i])
      }
    }

  });
}



//HEATMAP
const heatmap = new ol.layer.Heatmap({
  source: urlsource_all,
  blur: 15,
  radius: 7.5,
  weight: function (feature) {

    const rumore = feature.get("Rumore")
    return rumore;
  },
});

heatmap.setVisible(false);
map.addLayer(heatmap);

//RADIO BUTTON ACTIONS
$('input[type="radio"][value="Positions"]').click(function(){
  $('#kmeansform').css('display','none');
  $('#checkbox_choice').css('display','block');
  $('#kmeans_clustering').css('display','none')
  azzerate_clusters_layer()
  singleclick_key = map.on('click',popupPositions)
  ol.Observable.unByKey(clustersEvent)
  positions_dummy.setVisible(true)
  positions_gpsp.setVisible(true)
  positions_noprivacy.setVisible(true)
  clusters.setVisible(false)
  heatmap.setVisible(false)
})

$('input[type="radio"][value="KMeans"]').click(function() {
  $('#kmeansform').css('display','none');
  $('#checkbox_choice').css('display','none');
  $('#kmeans_clustering').css('display','block')
  azzerate_clusters_layer()
  create_clusters()
  positions_dummy.setVisible(false)
  positions_gpsp.setVisible(false)
  positions_noprivacy.setVisible(false)
  clusters.setVisible(false)
  ol.Observable.unByKey(singleclick_key)
  clustersEvent = map.on('click', popupClusters)
  heatmap.setVisible(false)
})

$('input[type="radio"][value="Clustering"]').click(function() {
  $('#kmeansform').css('display','block');
  $('#checkbox_choice').css('display','none');
  $('#kmeans_clustering').css('display','none')
  azzerate_clusters_layer()
  positions_dummy.setVisible(false)
  positions_gpsp.setVisible(false)
  positions_noprivacy.setVisible(false)
  clusters.setVisible(true)
  ol.Observable.unByKey(singleclick_key)
  ol.Observable.unByKey(clustersEvent)
  clustersEvent = map.on('click', popupClusters)
  heatmap.setVisible(false)
})

$('input[type="radio"][value="Heatmap"]').click(function(){
  $('#kmeansform').css('display','none');
  $('#checkbox_choice').css('display','none');
  $('#kmeans_clustering').css('display','none')
  azzerate_clusters_layer()
  ol.Observable.unByKey(singleclick_key)
  ol.Observable.unByKey(clustersEvent)
  positions_dummy.setVisible(false)
  positions_gpsp.setVisible(false)
  positions_noprivacy.setVisible(false)
  clusters.setVisible(false)
  heatmap.setVisible(true)
})


