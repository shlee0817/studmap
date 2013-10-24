var Admin = {
    
    graph: null,
    
    init: function (floorId) {

        $('#adminContent').html("");

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
            url: window.basePath + 'Admin/GetFloorplanImage/0/' + floorId,
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
        this.graph = graph;
    },

    getGraph: function() {
        return { "Nodes": this.graph.getNodes(), "Edges": this.graph.getLinks() };
    },

    loadMaps: function() {
        $('.tab').removeClass('active');
        $(this).addClass('active');
        $('#adminContent').html("").load(window.basePath + "Admin/GetMaps");
    },

    loadFloors: function (mapId) {
        $('.tab').removeClass('active');
        $(this).addClass('active');
        $('#adminContent').html("").load(window.basePath + "Admin/GetFloorsForMap/" + mapId);
    },

    loadFloorplan: function (floorId) {
        $('.tab').removeClass('active');
        $(this).addClass('active');
        this.init(floorId);
    }
};