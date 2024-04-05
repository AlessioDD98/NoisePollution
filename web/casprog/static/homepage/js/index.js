//MAP INITIALIZATION
var map = new ol.Map({
  target: 'map',
  layers: [],
  view: 
    new ol.View({
      center: ol.proj.fromLonLat([11.3394883, 44.4938134]),
      zoom: 13
    })
});

var osmLayer = new ol.layer.Tile({
    source: 
      new ol.source.OSM({
      })
});

osmLayer.setVisible(true);
map.addLayer(osmLayer);


//USERS POSITIONS WITH DESCRIPTION
var fill = new ol.style.Fill({
  color: 'rgba(0,0,255,0.4)'
});

var filltext = new ol.style.Fill({
  color: 'rgba(255,255,255,1)'
});

var stroke = new ol.style.Stroke({
  color: '#FF0000',
  width: 1.25
});

var positionstyle = new ol.style.Style({
  image: new ol.style.Circle({
    fill: fill,
    stroke: stroke,
    radius: 6
  }),
  fill: fill,
  stroke: stroke
})

var feature_collection = {"type": "FeatureCollection", "features": []}

for(var i = 0;i<json_pos.length;i++){
  var feature = {
    "type": "Feature",
    "geometry": JSON.parse(json_pos[i]),
    "properties": {
      "Rumore": rumori[i],
      "Timestamp": timestamp[i]
    }
  }
  feature_collection['features'].push(feature)
}

console.log(JSON.stringify(feature_collection))

var features = new ol.format.GeoJSON().readFeatures(feature_collection, {
  dataProjection: "EPSG:4326",
  featureProjection: "EPSG:3857"
}); 

/* var urlsource = new ol.source.Vector({
  features: features
}) */

var urlsource = new ol.source.Vector({
  features: features
})

var positions = new ol.layer.Vector({
  title: 'positions Layer',
  source: urlsource,
  style: positionstyle
});

$('#kmeansform').hide();
$('input:radio[name=visual]:nth(0)').attr('checked',true);

positions.setVisible(true)
map.addLayer(positions);

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

function popupPositions(e) {

  var feature = map.forEachFeatureAtPixel(e.pixel, function (feature) {
    console.log(feature)
    return feature;
  });
  
  if (feature) {
    
    const coordinate = e.coordinate;
    const hdms = ol.proj.toLonLat(coordinate);
  
    //$(element).popover('dispose');
    content.innerHTML = '<div id="popover-content"><span>Posizione:</span><br>' +
                        '<span class="popupLatLon">Lat: ' + hdms[0] + '</span><br>' + 
                        '<span class="popupLatLon">Long: ' + hdms[1] + '</span><br>' + 
                        '<span>Rumore: '+ feature.get("Rumore") +'</span><br>' +
                        '<span>Timestamp: '+ feature.get("Timestamp") +'</span><br>'
    popup.setPosition(coordinate);
    
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
  source: urlsource,
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
          stroke: stroke,
          fill: fill
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
      // Get clustered Coordinates
      const features = clickedFeatures[0].get('features');
      if (features.length > 1) {
        const extent = ol.extent.boundingExtent(
          features.map((r) => r.getGeometry().getCoordinates())
        );
        map.getView().fit(extent, {duration: 1000, padding: [50, 50, 50, 50]});
      } 
      else {
        
        const coordinate = e.coordinate;
        const hdms = ol.proj.toLonLat(coordinate);

        console.log(hdms[0])
        //$(element).popover('dispose');
        content.innerHTML = '<div id="popover-content"><span>Posizione:</span><br>' +
                            '<span class="popupLatLon">Lat: ' + hdms[0] + '</span><br>' + 
                            '<span class="popupLatLon">Long: ' + hdms[1] + '</span><br>' + 
                            '<span>Rumore: '+ features[0].get("Rumore") +'</span><br>' +
                            '<span>Timestamp: '+ features[0].get("Timestamp") +'</span><br>'
        popup.setPosition(coordinate);
      } 
    }
  });
}

//event listener for popup with clusters
var clustersEvent = map.on('click', popupClusters)


//HEATMAP

const heatmap = new ol.layer.Heatmap({
  source: urlsource,
  blur: 10,
  radius: 7.5,
  weight: function (feature) {

    const lat = feature.get("Rumore")
    return lat;
  },
});

heatmap.setVisible(false);
map.addLayer(heatmap);



//RADIO BUTTON ACTIONS

$('input[type="radio"][value="Positions"]').click(function(){
  $('#kmeansform').hide();
  singleclick_key = map.on('click',popupPositions)
  positions.setVisible(true)
  clusters.setVisible(false)
  heatmap.setVisible(false)
})



$('input[type="radio"][value="KMeans"]').click(function() {
  $('#kmeansform').show();
  positions.setVisible(false)
  clusters.setVisible(true)
  ol.Observable.unByKey(singleclick_key)
  clustersEvent = map.on('click', popupClusters)
  heatmap.setVisible(false)
})


$('input[type="radio"][value="Heatmap"]').click(function(){
  $('#kmeansform').hide();
  ol.Observable.unByKey(singleclick_key)
  ol.Observable.unByKey(clustersEvent)
  positions.setVisible(false)
  clusters.setVisible(false)
  heatmap.setVisible(true)
})

























