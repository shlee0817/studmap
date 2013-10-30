d3.floorplan.graph = function () {
    var x = d3.scale.linear(),
        y = d3.scale.linear(),
        id = "fp-graph-" + new Date().valueOf(),
        name = "graph",
        nodeCount = 0,
        nodes = [],
        links = [],
        node = null,
        link = null;

    var selectedNode = null,
    mousedownNode = null,
    mouseupNode = null;

    function resetMouseVars() {
        mousedownNode = null;
        mouseupNode = null;
    }

    function graph(g) {
        g.each(function(data) {
            if (!data) return;

            d3.select("svg").on("mousedown", function () {

                d3.select("svg").classed("active", true);

                if (d3.event.ctrlKey || mousedownNode) return;

                var mouse = d3.mouse(this);

                nodes.push({ id: ++nodeCount, fixed: true, x: mouse[0], y: mouse[1] });
                graph.start();
            }).on("mouseup", function () {
                d3.select("svg").classed("active", false);
                resetMouseVars();
            });

            var g = d3.select(this);
            node = g.selectAll(".node");
            link = g.selectAll(".link");

            var points = data.Nodes;
            for (var i = 0; i < points.length; i++) {
                nodes.push({ id: points[i].Id, fixed: true, x: points[i].X, y: points[i].Y });
                if (points[i].Id > nodeCount) {
                    nodeCount = points[i].Id;
                }
            }
            var edges = data.Edges;
            for (i = 0; i < points.length; i++) {
                links.push({ source: getNode(edges[i].StartNodeId), target: getNode(edges[i].EndNodeId) });
            }
        });
        graph.start();
    }
    
    function getNode(id) {

        for (var i = 0; i < nodes.length; i++) {
            if (nodes[i].id == id)
                return nodes[i];
        }
        return null;
    }

    graph.start = function () {
        link = link.data(links, function (d) { return d.source.id + "-" + d.target.id; });
        link.enter().insert("path", ".node").attr("class", "link").attr("d", function (d) {
            return "M" + d.target.x + "," + d.target.y + "L" + d.source.x + "," + d.source.y;
        });
        link.exit().remove();

        node = node.data(nodes, function (d) { return d.id; });
        node.enter()
            .append("circle")
            .attr("class", function () { return "node"; })
            .attr("cx", function (d) { return d.x; })
            .attr("cy", function (d) { return d.y; })
            .attr("r", 8)
            .on('mousedown', function (d) {
                if (d3.event.ctrlKey) return;

                mousedownNode = d;
                if (mousedownNode === selectedNode) selectedNode = null;
                else selectedNode = mousedownNode;
            })
            .on('mouseup', function (d) {
                if (!mousedownNode) return;

                mouseupNode = d;
                if (mouseupNode === mousedownNode) { resetMouseVars(); return; }


                var source = mousedownNode;
                var target = mouseupNode;

                var l;
                l = links.filter(function (l) {
                    return (l.source === source && l.target === target);
                })[0];

                if (!l) {
                    l = { source: source, target: target };
                    links.push(l);
                }

                selectedNode = null;
                graph.start();
            });
        node.exit().remove();
    };

    graph.xScale = function (scale) {
        if (!arguments.length) return x;
        x = scale;
        return graph;
    };

    graph.yScale = function (scale) {
        if (!arguments.length) return y;
        y = scale;
        return graph;
    };

    graph.id = function () {
        return id;
    };

    graph.title = function (n) {
        if (!arguments.length) return name;
        name = n;
        return graph;
    };

    graph.getNodes = function () {
        return nodes;
    };

    graph.getLinks = function () {
        return links;
    };

    return graph;
};

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

function init(imageUrl) {
    $('#floorplan').html("");

    mapdata[imagelayer.id()] = [{
        url: imageUrl,
        x: 0,
        y: 0,
        height: 33.79,
        width: 50.0
    }];

    map.addLayer(imagelayer)
        .addLayer(pathplot)
        .addLayer(graph);

    d3.json(window.basePath + "Admin/GetMapData/" + floorId, function (data) {
        
        mapdata[pathplot.id()] = data.Pathplot;
        mapdata[graph.id()] = data.Graph;

        d3.select("#floorplan").append("svg")
            .attr("height", 487).attr("width", 720)
            .datum(mapdata).call(map);
    });
}

function getGraph() {

    var edges = [];
    var links = graph.getLinks();
    for (var i = 0; i < links.length; i++) {
        var edge = {
            "StartNodeId": links[i].source.id,
            "EndNodeId": links[i].target.id
        };
        edges.push(edge);
    }

    var _nodes = [];
    var nodes = graph.getNodes();
    for (i = 0; i < nodes.length; i++) {
        var node = {
            "Id": nodes[i].id,
            "X": nodes[i].x,
            "Y": nodes[i].y
        };
        _nodes.push(node);
    }
    return { "Nodes": _nodes, "FloorId": this.floorId, "Edges": edges };
};

function saveGraph() {

    var obj = {
        floorId: floorId,
        graph: getGraph()
    };

    $("body").addClass("loading");
    $.ajax({
        url: window.basePath + 'Admin/SaveGraphForMap',
        data: JSON.stringify(obj),
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        type: "post",
        success: function (result) {
            $("body").removeClass("loading");
            init(imageUrl);
        }
    });
};

init(imageUrl);