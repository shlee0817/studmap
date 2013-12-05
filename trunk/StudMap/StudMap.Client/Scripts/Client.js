function StudMapClient(mapId, floorId, imageUrl) {

    //Übergebene Variablen
    this.mapId = mapId;
    this.floorId = floorId;
    this.imageUrl = imageUrl;

    //Private Variablen
    this.startPoint = null;
    this.endPoint = null;
    this.highlightedPoint = null;
    this.rangeEndX = 0;
    this.rangeEndY = 0;
    this.map = null;
    this.pathList = null;
    
    //Konstanten
    this.radius = 2;
    this.selectedRadius = 4;
    this.lineThickness = 2;
    this.strokeColor = "#83c32d";
    this.startNodeColor = "blue";
    this.endNodeColor = "red";
    this.nodeColor = "#83c32d";
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
        that.rangeEndX = width;
        that.rangeEndY = height;

        var xscale = d3.scale.linear()
            .domain([domainStartX, domainEndX])
            .range([rangeStartX, that.rangeEndX]);
        var yscale = d3.scale.linear()
            .domain([domainStartY, domainEndY])
            .range([rangeStartY, that.rangeEndY]);
        that.map = d3.floorplan().xScale(xscale).yScale(yscale);
        var imagelayer = d3.floorplan.imagelayer();
        var mapdata = {};


        mapdata[imagelayer.id()] = [{
            url: that.imageUrl,
            x: 0,
            y: 0,
            height: domainEndX,
            width: domainEndY
        }];
        that.map.addLayer(imagelayer);
        svgContainer.datum(mapdata).call(that.map);

        d3.select(".map-layers").append("g").attr("id", "circles");
        d3.select(".map-layers").append("g").attr("id", "path");

        that.loadAndDrawFloorPlanData();
    };
    floorPlanImage.src = this.imageUrl;
};


//Setter
StudMapClient.prototype.setStartPoint = function (nodeId) {

    if (this.startPoint) {
        this.startPoint.setAttribute("fill", "#83c32d");
        this.startPoint.setAttribute("r", this.radius);
    }
    this.startPoint = document.getElementById(nodeId);

    this.startPoint.setAttribute("fill", "blue");
    this.startPoint.setAttribute("r", 4);

    this.testPath();
};

StudMapClient.prototype.setEndPoint = function (nodeId) {

    if (this.endPoint) {
        this.endPoint.setAttribute("fill", "#83c32d");
        this.endPoint.setAttribute("r", this.radius);
    }

    this.endPoint = document.getElementById(nodeId);

    this.endPoint.setAttribute("fill", "red");
    this.endPoint.setAttribute("r", 4);

    this.testPath();
};
//Setter


//Zeichnen
StudMapClient.prototype.showPath = function (startNodeId, endNodeId) {


    this.clearPath();

    var that = this;
    var params = [];
    var map = new Param("mapId", this.mapId);
    var startNode = new Param("startNodeId", startNodeId);
    var endNode = new Param("endNodeId", endNodeId);
    params.push(map);
    params.push(startNode);
    params.push(endNode);
    
    this.load("Maps", "GetRouteBetween", params, function (data) {
        this.pathList = data.List;
        that.drawPath(data.List);
        document.getElementById('path').style.display = 'block';
        document.getElementById('circles').style.display = 'none';
    });
};

StudMapClient.prototype.highlightPoint = function (nodeId, color) {

    this.highlightPoint(nodeId, color, this.radius);
};

StudMapClient.prototype.highlightPoint = function (nodeId, color, radius) {

    if (this.highlightedPoint)
    {
        this.highlightedPoint.attr("fill", "#83c32d")
        .attr("r", 2);
    }


    $('#' + nodeId).attr("fill", color)
        .attr("r", radius);

    this.highlightedPoint = $('#' + nodeId);
};

// {"List":[{"Id":966,"X":0.3787,"Y":0.1263,"FloorId":1012},{"Id":967,"X":0.3689,"Y":0.1059,"FloorId":1012}],"Status":1,"ErrorCode":0,"ErrorMessage":""}
StudMapClient.prototype.drawPath = function (pathAsArray) {

    if (pathAsArray.length < 2)
        return;

    for (var i = 0; i < pathAsArray.length; i++) {

        if (pathAsArray[i].FloorId == this.floorId) {

            var startNode = pathAsArray[i];
            
            //Der letzte Knoten kann nicht mehr verbunden werden
            if (i < pathAsArray.length - 1) {

                var endNode = pathAsArray[i + 1];
                //Pfade anlegen
                this.drawLine("#path", startNode.X, startNode.Y, endNode.X, endNode.Y);
            }
            
            if (i == 0) {
                //Startknoten markieren
                this.drawCircle("#path", startNode.X, startNode.Y, startNode.id, this.selectedRadius, this.startNodeColor);
            } else if (i == pathAsArray.length - 1) {
                //Endknoten markieren
                this.drawCircle("#path", startNode.X, startNode.Y, startNode.id, this.selectedRadius, this.endNodeColor);
            } else {
                //Zwischenknoten markieren
                this.drawCircle("#path", startNode.X, startNode.Y, startNode.id, this.radius, null);
            }
        }

    }
};

StudMapClient.prototype.showCircles = function (circlesAsArray) {

    for (var i = 0; i < circlesAsArray.length; i++) {

        var circle = circlesAsArray[i];
        this.drawCircle("#circles", circle.x, circle.y, circle.id, this.radius, null);

        $('#' + circle.id).on("touchstart", {
            nodeId: circlesAsArray[i].id
        }, this.circle_click);
    }
    if (window.jsinterface) {
        window.jsinterface.onFinish();
    }
};

StudMapClient.prototype.drawCircle = function(el, x, y, id, r, fill) {
    
    var circle = d3.select(el).append("circle")
        .attr("cx", x * this.rangeEndX)
        .attr("cy", y * this.rangeEndY)
        .attr("id", id)
        .attr("stroke", "transparent")
        .attr("fill", "#83c32d")
        .attr("stroke-width", 5)
        .attr("r", r);
    if (fill)
        circle.attr("fill", fill);

    return circle;
};

StudMapClient.prototype.drawLine = function(el, x1, y1, x2, y2) {
    
    var line = d3.select(el).append("line")
              .attr("x1", x1 * this.rangeEndX)
              .attr("y1", y1 * this.rangeEndY)
              .attr("x2", x2 * this.rangeEndX)
              .attr("y2", y2 * this.rangeEndY)
              .attr("stroke-width", this.lineThickness)
              .attr("stroke", this.strokeColor);

    return line;
};
//Zeichnen


//Delete and Clear
StudMapClient.prototype.clearPath = function () {

    $('#path').empty();
};

StudMapClient.prototype.clearMap = function () {

    $('#circles').empty();
};

StudMapClient.prototype.resetMap = function () {

    this.clearMap();

    this.loadAndDrawFloorPlanData();

    this.startPoint = null;
    this.endPoint = null;

    this.clearPath();

    $('#circles').show();
};

StudMapClient.prototype.resetZoom = function () {

    $('.map-layers').removeAttr("transform");
};
//Delete and Clear

StudMapClient.prototype.zoomToNode = function(nodeId) {

    var node = $('#' + nodeId);

    if (node.length < 1)
        return;

    var scale = 5;
    var cx = node.attr("cx") * 1;
    var cy = node.attr("cy") * 1;
    var translateX = cx*(1 - scale);
    var translateY = cy*(1 - scale);

    this.map.zoom(translateX, translateY, scale);
};

StudMapClient.prototype.circle_click = function (event) {

    var nodeId = event.data.nodeId;

    if (window.jsinterface) {
        window.jsinterface.punkt(nodeId);
    }
};

StudMapClient.prototype.testPath = function () {

    if (this.startPoint && this.endPoint) {
        this.showPath(this.startPoint.id, this.endPoint.id);
    }
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

StudMapClient.prototype.loadAndDrawFloorPlanData = function() {

    var params = [];
    var floorId = new Param("floorid", this.floorId);
    params.push(floorId);
    var that = this;
    
    this.load("Maps", "GetFloorPlanData", params, function (data) {

        if (!data || !data.Object || !data.Object.Graph || !data.Object.Graph.Nodes)
            return null;
        
        var nodes = data.Object.Graph.Nodes;
        for (var i = 0; i < nodes.length; i++) {

            var node = nodes[i];
            var circle =
                [
                    {
                        x: node.X,
                        y: node.Y,
                        id: node.Id
                    }];

            that.showCircles(circle);
        }
    });
};

function Param(key, value) {
    this.key = key;
    this.value = value;
}