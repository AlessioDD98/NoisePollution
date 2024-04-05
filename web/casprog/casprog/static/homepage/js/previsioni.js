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

var feature_collection_all = {"type": "FeatureCollection", "features": []}

/* for(var i = 0;i<json_pos.length;i++){
    var feature = {
      "type": "Feature",
      "geometry": JSON.parse(json_pos[i]),
      "properties": {
        "Rumore": rumori[i],
        "Timestamp": timestamp[i],
      }
    }
    feature_collection_all['features'].push(feature)
} */

for(var i = 0;i<longs.length;i++){
  var feature = {
    "type": "Feature",
    "geometry": { "type":"Point", "coordinates": [lats[i],longs[i]] },
    "properties": {
      "Rumore": rms[i]
    }
  }
  feature_collection_all['features'].push(feature)
}

var features_all = new ol.format.GeoJSON().readFeatures(feature_collection_all, {
    dataProjection: "EPSG:4326",
    featureProjection: "EPSG:3857"
});

var urlsource_all = new ol.source.Vector({
    features: features_all
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
  
var positions_all = new ol.layer.Vector({
    title: 'positions Layer',
    source: urlsource_all,
    style: positionstyle_all
});



positions_all.setVisible(true)
map.addLayer(positions_all);

var features_popup_array = []
function popupString(e, features, len){
  pos = 0
  const coordinate_popup = e.coordinate
  if(len == 1){
    const feat_coord = features[0].getGeometry().getCoordinates()
    const coordinate = ol.proj.transform(feat_coord, 'EPSG:3857', 'EPSG:4326');
    
    content.innerHTML = '<div id="popover-content"><span>Posizione:</span><br>' +
                        '<span class="popupLatLon">Lat: ' + coordinate[0] + '</span><br>' + 
                        '<span class="popupLatLon">Long: ' + coordinate[1] + '</span><br>' + 
                        '<span>Rumore: '+ features[0].get("Rumore") +'</span><br><div>' 
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
                  '<input type="button" value="Prev" name="prev" id="prevPos" onclick="prevPopup()">' +
                  '<input type="button" value="Next" name="next" id="nextPos" onclick="nextPopup()"></div>'
      
      features_popup_array.push(innerHTML)
    }
    content.innerHTML = features_popup_array[0]
    popup.setPosition(coordinate_popup); 
  }
}

var pos = 0

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
  console.log(feature)
  if(features[0]){
    if (features.length > 1) {
      popupString(e, features, features.length)
      
    }
    else {
      popupString(e, features, 1)
    }
  } else {

    const coordinate = e.coordinate;
    const hdms = ol.proj.toLonLat(coordinate);

    $.post("previsione_rm/", {"lat":hdms[0], "long":hdms[1]}, function (data) {
      
      console.log(hdms)
      content.innerHTML = '<div id="popover-content"><span>Posizione</span><br>' +
                          '<span class="popupLatLon">Lat: ' + hdms[0] + '</span><br>' + 
                          '<span class="popupLatLon">Long: ' + hdms[1] + '</span><br>' + 
                          '<span>Previsione rumore: '+ data +'</span><br>'
      popup.setPosition(coordinate);

    });
    
  }
}

var singleclick_key = map.on('singleclick', popupPositions)

$("#predict").click( function() {
  var lat = $('#lat_pred').val()
  var long = $('#long_pred').val()

  $.post("previsione_rm/", {"lat":lat, "long":long}, function (data) {
      if (data == "None"){
        $('#rm_predicted').html("Insert Lat and Long!")
      } else {
        $('#rm_predicted').html(data)
      }
  });
});



