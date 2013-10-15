﻿var Admin = {
    
    init: function (floorId) {

        $('#map').html("");

        var xscale = d3.scale.linear()
            .domain([0, 50.0])
            .range([0, 720]),
            yscale = d3.scale.linear()
                .domain([0, 33.79])
                .range([0, 487]),
            map = d3.floorplan().xScale(xscale).yScale(yscale),
            imagelayer = d3.floorplan.imagelayer(),
            pathplot = d3.floorplan.pathplot(),
            overlays = d3.floorplan.overlays().editMode(true),
            mapdata = {};

        mapdata[imagelayer.id()] = [{
            url: window.imgBasePath + 'Sample_Floorplan.jpg',
            x: 0,
            y: 0,
            height: 33.79,
            width: 50.0
        }];

        map.addLayer(imagelayer)
            .addLayer(pathplot)
            .addLayer(overlays);

        d3.json("GetMapData/" + floorId, function (data) {
            mapdata[overlays.id()] = data.overlays;
            mapdata[pathplot.id()] = data.pathplot;

            d3.select("#map").append("svg")
                .attr("height", 487).attr("width", 720)
                .datum(mapdata).call(map);
        });
    },
    
    loadFloorplan: function(floorId) {
        this.init(floorId);
    }
};