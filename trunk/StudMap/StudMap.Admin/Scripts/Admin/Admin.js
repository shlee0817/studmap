var Admin = {
    
    graph: null,
    mapId: 0,
    floorId: 0,
    
    init: function (mapId, floorId) {

        $('#adminContent').html("");
        $.ajax({
            url: window.basePath + 'Admin/GetFloorplanImage/' + mapId + '/' + floorId,
            success: function (result) {
                
                var xscale = d3.scale.linear()
                .domain([0, 50.0])
                .range([0, 720]),
                yscale = d3.scale.linear()
                    .domain([0, 33.79])
                    .range([0, 487]),
                map = d3.floorplan().xScale(xscale).yScale(yscale),
                imagelayer = d3.floorplan.imagelayer(),
                pathplot = d3.floorplan.pathplot(),
                mapdata = {},
                graph = d3.floorplan.graph();

                mapdata[imagelayer.id()] = [{
                    url: result.ImageUrl,
                    x: 0,
                    y: 0,
                    height: 33.79,
                    width: 50.0
                }];

                map.addLayer(imagelayer)
                    .addLayer(pathplot)
                    .addLayer(graph);

                d3.json(window.basePath + "Admin/GetMapData/" + floorId, function (data) {
                    mapdata[pathplot.id()] = data.pathplot;
                    mapdata[graph.id()] = data.graph;

                    d3.select("#adminContent").append("svg")
                        .attr("height", 487).attr("width", 720)
                        .datum(mapdata).call(map);
                });
                Admin.graph = graph;
            }
        });
    },

    getGraph: function () {

        var edges = [];
        var _edges = this.graph.getLinks();
        for (var i = 0; i < _edges.length; i++) {
            var edge = {
                "StartNodeId": _edges[i].source.id,
                "EndNodeId": _edges[i].target.id
            };
            edges.push(edge);
        }

        var nodes = [];
        var _nodes = this.graph.getNodes();
        for (i = 0; i < _nodes.length; i++) {
            var node = {
                "Id": _nodes[i].id,
                "X": _nodes[i].x,
                "Y": _nodes[i].y
            };
            nodes.push(node);
        }
        return { "Nodes": nodes, "FloorId": this.floorId, "Edges": edges };
    },

    saveGraph: function() {

        var obj = {
            mapId : this.mapId,
            graph : this.getGraph()
        };

        $.ajax({
            url: window.basePath + 'Admin/SaveGraphForMap',
            data: JSON.stringify(obj),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            type: "post",
            success: function (result) {
            }
        });
    },

    loadMaps: function() {
        $('.tab').removeClass('active').removeClass("selectable");
        $(this).addClass('active');
        $('#adminContent').html("").load(window.basePath + "Admin/GetMaps");
    },

    loadFloors: function (mapId) {
        this.mapId = mapId;
        $('.tab').removeClass('active');
        $(this).addClass('active');
        $('#adminContent').html("").load(window.basePath + "Admin/GetFloorsForMap/" + mapId);
    },

    loadFloorplan: function (mapId, floorId) {
        this.mapId = mapId;
        this.floorId = floorId;
        $('.tab').removeClass('active');
        $(this).addClass('active');
        this.init(mapId, floorId);
    }
};