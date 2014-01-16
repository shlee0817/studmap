function StudMapClient(mapId, floorId, imageUrl) {

    //Übergebene Variablen
    this.mapId = mapId;
    this.floorId = floorId;
    this.imageUrl = imageUrl;

    //Private Variablen
    this.startPointId = null;
    this.endPointId = null;
    this.highlightedPoint = null;
    this.map = null;

    //Konstanten
    this.radius = 2;
    this.graphLayer = "graph";
    this.pathPlotLayer = "pathplot";
}

//Initialisierung
StudMapClient.prototype.init = function () {
    var svgContainer = d3.select("#svgMap");
    var that = this;

    var floorPlanImage = new Image();
    floorPlanImage.onload = function () {

        var imageWidth = floorPlanImage.width;
        var imageHeight = floorPlanImage.height;
        var domainStartX = 0;
        var domainEndX = 1;
        var rangeStartX = 0;
        var domainStartY = 0;
        var domainEndY = 1;
        var rangeStartY = 0;

        width = window.innerWidth;
        height = imageHeight / imageWidth * width;
        var rangeEndX = width;
        var rangeEndY = height;

        var xscale = d3.scale.linear()
            .domain([domainStartX, domainEndX])
            .range([rangeStartX, rangeEndX]);
        var yscale = d3.scale.linear()
            .domain([domainStartY, domainEndY])
            .range([rangeStartY, rangeEndY]);
        that.map = d3.floorplan().xScale(xscale).yScale(yscale);
        var imagelayer = d3.floorplan.imagelayer();
        var graphlayer = d3.floorplan.graph();

        graphlayer.xScale(xscale);
        graphlayer.yScale(yscale);

        var pathplotlayer = d3.floorplan.pathplot();

        var mapdata = {};
        mapdata[imagelayer.id()] = [{
            url: that.imageUrl,
            x: 0,
            y: 0,
            height: domainEndX,
            width: domainEndY
        }];

        that.map.addLayer(imagelayer);
        that.map.addLayer(pathplotlayer);
        that.map.addLayer(graphlayer);
        console.log("Layer added");

        svgContainer.datum(mapdata).call(that.map);
        console.log("Data provided");

        that.loadAndDrawFloorPlanData();
    };
    floorPlanImage.src = this.imageUrl;
};


//Setter
StudMapClient.prototype.setStartPoint = function (nodeId) {

    this.startPointId = nodeId;

    this.highlightPoint(this.startPointId, 3);

    this.showPath();
};

StudMapClient.prototype.setEndPoint = function (nodeId) {

    this.endPointId = nodeId;

    this.highlightPoint(this.endPointId, 3);
    
    this.showPath();
};
//Setter


//Zeichnen
StudMapClient.prototype.showPath = function () {

    if (!this.startPointId || !this.endPointId)
        return;
    
    if (this.endPointId === this.startPointId) {

        if (window.jsinterface) {
            window.jsinterface.onNavigationCompleted();
        }
        $('.pathplot').children().remove();
        this.changeLayerVisibility(this.graphLayer, true);
        this.changeLayerVisibility(this.pathPlotLayer, false);
        return;
    }

    $('.pathplot').children().remove();
    var that = this;
    var params = [];
    var map = new Param("mapId", this.mapId);
    var startNode = new Param("startNodeId", this.startPointId);
    var endNode = new Param("endNodeId", this.endPointId);
    params.push(map);
    params.push(startNode);
    params.push(endNode);

    this.load("Maps", "GetRouteBetween", params, function (data) {

        if (!data || !data.List)
            return;

        that.changeLayerVisibility(that.graphLayer, true);
        that.changeLayerVisibility(that.pathPlotLayer, false);

        that.pathList = data.List;
        that.map.callPathplotLayer(data);
    });
};

StudMapClient.prototype.highlightPoint = function (nodeId, radius) {

    if (!radius)
        radius = this.radius;
    var className;
    if (this.highlightedPoint) {
        className = this.highlightedPoint.attr("class");
        className = className.replace("highlighted", "");
        this.highlightedPoint.attr("class", className)
            .attr("r", this.radius);
    }


    this.highlightedPoint = $('#' + nodeId);
    className = this.highlightedPoint.attr("class");
    className += " highlighted";
    this.highlightedPoint.attr("class", className)
            .attr("r", radius);
    
    this.changeLayerVisibility(this.graphLayer, false);
    this.changeLayerVisibility(this.pathPlotLayer, true);
};
//Zeichnen


//Delete and Clear
StudMapClient.prototype.clearMap = function () {

    $('.pathplot').children().remove();
    this.changeLayerVisibility(this.graphLayer, false);
    this.changeLayerVisibility(this.pathPlotLayer, true);
};

StudMapClient.prototype.resetMap = function () {

    this.startPointId = null;
    this.endPointId = null;
    this.clearMap();

    $('.highlighted').attr("class", "node active").attr("r", 2);
};

StudMapClient.prototype.resetZoom = function () {

    $('.map-layers').removeAttr("transform");
};
//Delete and Clear

StudMapClient.prototype.zoomToNode = function (nodeId) {

    var node = $('#' + nodeId);

    if (node.length < 1)
        return;

    var scale = 5;
    var cx = node.attr("cx") * 1;
    var cy = node.attr("cy") * 1;
    var translateX = cx * (1 - scale);
    var translateY = cy * (1 - scale);

    this.map.zoom(translateX, translateY, scale);
};

StudMapClient.prototype.load = function (controller, method, params, callback) {

    if (!controller || !method)
        return;

    var url = "http://";
    if (window.location.hostname === "localhost") {
        url += "localhost:1129/";
    } else {
        url += "193.175.199.115/StudMapService/";
    }
    url += "api/" + controller + "/" + method;
    var paramStr = "";

    if (params) {
        paramStr += "?";
        for (var i = 0; i < params.length; i++) {

            var param = params[i];
            paramStr += param.key + "=" + param.value;

            if (i < params.length - 1) {
                paramStr += "&";
            }
        }
    }

    url = url + paramStr;
    d3.json(url, callback);
};

StudMapClient.prototype.loadAndDrawFloorPlanData = function () {

    var params = [];
    var floorId = new Param("floorid", this.floorId);
    params.push(floorId);
    var that = this;

    this.load("Maps", "GetGraphForFloor", params, function (data) {

        if (!data || !data.Object || !data.Object.Nodes)
            return;

        console.log("Data loaded");
        that.map.callGraphLayer(data.Object.Nodes);

        if (window.jsinterface) {
            window.jsinterface.onFinish();
        }
    });
};

StudMapClient.prototype.changeLayerVisibility = function(layerClass, hide) {

    var el = $("." + layerClass);

    if (!el || el.length === 0)
        return;

    if (hide)
        el.hide();
    else 
        el.show();
    
};

function Param(key, value) {
    this.key = key;
    this.value = value;
}