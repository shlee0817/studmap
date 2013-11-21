d3.floorplan.graph = function() {
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
        mouseupNode;

    var zoomScaleFactor = null,
        zoomX = null,
        zoomY = null;

    function resetMouseVars() {
        mousedownNode = null;
        mouseupNode = null;
    }

    d3.select("body").on("keydown", function() {

        //Entfernen löscht den ausgewählten Knoten
        if (selectedNode !== null && d3.event.keyCode === 46) {

            $('#NodeInformationDialog').html("Soll der Knoten " + selectedNode.id + " gelöscht werden?");
            $('#NodeInformationDialog').dialog({
                autoHeight: true,
                dialogClass: "no-close",
                modal: true,
                title: "Knoten löschen",
                appendTo: "#body",
                buttons: {
                    Löschen: function() {
                        for (var i = 0; i < nodes.length; i++) {
                            if (nodes[i].id === selectedNode.id) {
                                if (nodes[i].type === "new")
                                    nodes.splice(i, 1);
                                else
                                    nodes[i].type = "deleted";
                                break;
                            }
                        }
                        //var linksToBeKept = [];
                        for (i = 0; i < links.length; i++) {
                            if (links[i].target.id === selectedNode.id || links[i].source.id === selectedNode.id) {
                                links[i].type = "deleted";
                            }
                        }
                        //links = [].concat(linksToBeKept);
                        graph.start();
                        selectedNode = null;
                        $(this).dialog("close");
                    },
                    Abbrechen: function() {
                        $(this).dialog("close");
                    }
                }
            });


        }

    });

    function graph(g) {

        d3.select(".map-controls").on("mousedown", function() {
            mousedownNode = true;
        }).on("mouseup", function() {
            mouseupNode = true;
        });

        g.each(function(data) {
            if (!data) return;

            d3.select("svg").on("mousedown", function() {

                d3.select("svg").classed("active", true);

                console.log(d3.event);
                if (!d3.event.ctrlKey || mousedownNode || d3.event.button == 2) return;

                var transformStr = d3.select(".map-layers").attr("transform");
                if (transformStr === null || transformStr === undefined) {
                    zoomX = 0;
                    zoomY = 0;
                    zoomScaleFactor = 1;
                } else {
                    var transform = /translate\(([^,]*),([^)]*)\)scale\(([^)]*)\)/i;
                    transform.exec(transformStr);
                    zoomX = RegExp.$1;
                    zoomY = RegExp.$2;
                    zoomScaleFactor = RegExp.$3;
                }


                var mouse = d3.mouse(this);

                var xp = (mouse[0] - zoomX * 1) / zoomScaleFactor;
                var yp = (mouse[1] - zoomY * 1) / zoomScaleFactor;

                nodes.push({ id: ++nodeCount, fixed: true, x: xp, y: yp, type: "new" });
                graph.start();
            }).on("mouseup", function() {
                d3.select("svg").classed("active", false);
                resetMouseVars();
            });

            var gEl = d3.select(this);
            node = gEl.selectAll(".node");
            link = gEl.selectAll(".link");

            var points = data.Nodes;
            for (var i = 0; i < points.length; i++) {
                nodes.push({ id: points[i].Id, fixed: true, x: (points[i].X * width), y: (points[i].Y * height), type: "server" });
                if (points[i].Id > nodeCount) {
                    nodeCount = points[i].Id;
                }
            }
            var edges = data.Edges;
            for (i = 0; i < edges.length; i++) {
                links.push({ source: getNode(edges[i].StartNodeId), target: getNode(edges[i].EndNodeId), type: "server" });
            }
        });
        graph.start();
    }

    function getNode(ident) {

        for (var i = 0; i < nodes.length; i++) {
            if (nodes[i].id === ident)
                return nodes[i];
        }
        return null;
    }

    function connectFloorsDialog(nodeId) {
        $.ajax({
            url: window.basePath + 'Admin/GetConnectedNodes?nodeId=' + nodeId,
            success: function (result) {
                $("#NodeInformationDialog").html(result);
                $('#NodeInformationDialog').dialog({
                    dialogClass: "no-close",
                    modal: true,
                    appendTo: "#body",
                    autoHeight: true,
                    title: "Knoteninformationen (" + nodeId + ")",
                    buttons: {
                        Speichern: function () {
                            var displayName = $('input[id=inputDisplayName]').val();
                            var roomName = $('input[id=inputRoomName]').val();
                            var poiTypeId = $('#inputPoiTypeId').val();
                            var poiDescription = $('textarea#inputPoI').val();
                            var qrCode = $('input[id=inputQRCode]').val();
                            var nfcTag = $('input[id=inputNFCTag]').val();

                            var obj = {
                                nodeId: $('input[id=nodeId]').val(),
                                displayName: displayName,
                                roomName: roomName,
                                poiTypeId: poiTypeId,
                                poiDescription: poiDescription,
                                qrCode: qrCode,
                                nfcTag: nfcTag
                            };

                            $.ajax({
                                url: window.basePath + 'Admin/SaveNodeInformation',
                                data: JSON.stringify(obj),
                                contentType: "application/json; charset=utf-8",
                                dataType: "json",
                                type: "post",
                                success: function () {
                                    init(window.imageUrl);
                                }
                            });
                            $(this).dialog("close");
                        },
                        Abbrechen: function () {
                            $(this).dialog("close");
                        }
                    }
                });
            }
        });
    }

    graph.start = function () {

        var displayLinks = [], displayNodes = [];
        for (var i = 0; i < links.length; i++) {
            if (links[i].type !== "deleted") {
                displayLinks.push(links[i]);
            }
        }
        for (i = 0; i < nodes.length; i++) {
            if (nodes[i].type !== "deleted") {
                displayNodes.push(nodes[i]);
            }
        }


        link = link.data(displayLinks, function (d) { return d.source.id + "-" + d.target.id; });
        link.enter().insert("path", ".node").attr("class", "link").attr("d", function(d) {
            return "M" + d.target.x + "," + d.target.y + "L" + d.source.x + "," + d.source.y;
        });
        link.exit().remove();

        node = node.data(displayNodes, function (d) { return d.id; });
        // Node create
        node.enter()
            .append("circle")
            .attr("class", function() { return "node"; })
            .attr("cx", function(d) { return d.x; })
            .attr("cy", function(d) { return d.y; })
            .attr("r", function () { return 4; })
            .on('mousedown', function (d) {

                if (d3.event.shiftKey) {
                    console.log("Shift-Click pressed");
                    connectFloorsDialog(d.id);
                    return;
                }

                // Selektierte Knoten verbinden sich nicht mehr via Rechtsklick miteinander
                if (d3.event.button == 2)
                    return;

                mousedownNode = d;

                if (selectedNode === null) {
                    d3.select(this).attr("r", 7);
                    d3.select(this).style("opacity", 1);
                    selectedNode = mousedownNode;
                } else if (mousedownNode === selectedNode) {
                    d3.select(this).attr("r", 4);
                    d3.select(this).style("opacity", 0.6);
                    selectedNode = null;
                } else {

                    if (selectedNode === null)
                        return;

                    var source = selectedNode;
                    var target = mousedownNode;

                    var l;
                    l = links.filter(function(ilink) {
                        return (ilink.source === source && ilink.target === target);
                    })[0];

                    if (!l) {
                        l = { source: source, target: target, type: "new" };
                        links.push(l);
                    }

                    selectedNode = null;

                    d3.selectAll(".node").attr("r", 4).style("opacity", 0.6);

                    graph.start();
                }
            }).on("contextmenu", function(d) {
                d3.event.preventDefault();
                $.ajax({
                    url: window.basePath + 'Admin/GetNodeInformation?nodeId=' + d.id,
                    success: function(result) {
                        $("#NodeInformationDialog").html(result);
                        $('#NodeInformationDialog').dialog({
                            dialogClass: "no-close",
                            modal: true,
                            appendTo: "#body",
                            autoHeight: true,
                            title: "Knoteninformationen (" + d.id + ")",
                            buttons: {
                                Speichern: function() {
                                    var displayName = $('input[id=inputDisplayName]').val();
                                    var roomName = $('input[id=inputRoomName]').val();
                                    var poiTypeId = $('#inputPoiTypeId').val();
                                    var poiDescription = $('textarea#inputPoI').val();
                                    var qrCode = $('input[id=inputQRCode]').val();
                                    var nfcTag = $('input[id=inputNFCTag]').val();

                                    var obj = {
                                        nodeId: $('div[id=nodeId]').text(),
                                        displayName: displayName,
                                        roomName: roomName,
                                        poiTypeId: poiTypeId,
                                        poiDescription: poiDescription,
                                        qrCode : qrCode,
                                        nfcTag : nfcTag
                                    };

                                    $.ajax({
                                        url: window.basePath + 'Admin/SaveNodeInformation',
                                        data: JSON.stringify(obj),
                                        contentType: "application/json; charset=utf-8",
                                        dataType: "json",
                                        type: "post",
                                        success: function() {
                                            init(window.imageUrl);
                                        }
                                    });
                                    $(this).dialog("close");
                                },
                                Abbrechen: function() {
                                    $(this).dialog("close");
                                }
                            }
                        });
                    }
                });

            });
        node.exit().remove();
    };

    graph.xScale = function(scale) {
        if (!arguments.length) return x;
        x = scale;
        return graph;
    };

    graph.yScale = function(scale) {
        if (!arguments.length) return y;
        y = scale;
        return graph;
    };

    graph.id = function() {
        return id;
    };

    graph.title = function(n) {
        if (!arguments.length) return name;
        name = n;
        return graph;
    };

    graph.getNodes = function() {
        return nodes;
    };

    graph.getLinks = function() {
        return links;
    };

    return graph;
};

var imageWidth = 0, imageHeight = 0;
var width, height;
var domainStartX = 0, domainEndX = 1;
var rangeStartX = 0, rangeEndX;
var domainStartY = 0, domainEndY = 1;
var rangeStartY = 0, rangeEndY;

var xscale,
    yscale,
    map,
    imagelayer = d3.floorplan.imagelayer(),
    pathplot = d3.floorplan.pathplot(),
    mapdata = {},
    graph;

function init(imageUrl) {

    var el = $("<img />").css("visibility", "hidden").attr("src", imageUrl);
    $('#body').append(el);
    window.setTimeout(function() {
        imageWidth = $(el).width();
        imageHeight = $(el).height();
        $(el).remove();
        width = $('#adminContent').width();
        height = imageHeight / imageWidth * width;
        rangeEndX = width;
        rangeEndY = height;

        xscale = d3.scale.linear()
            .domain([domainStartX, domainEndX])
            .range([rangeStartX, rangeEndX]);
        yscale = d3.scale.linear()
            .domain([domainStartY, domainEndY])
            .range([rangeStartY, rangeEndY]);
        map = d3.floorplan().xScale(xscale).yScale(yscale);
        graph = d3.floorplan.graph();

        $('#floorplan').html("");

        mapdata[imagelayer.id()] = [{
            url: imageUrl,
            x: 0,
            y: 0,
            height: domainEndX,
            width: domainEndY
        }];

        map.addLayer(imagelayer)
            .addLayer(pathplot)
            .addLayer(graph);

        d3.json(window.basePath + "Admin/GetFloorPlanData/" + floorId, function(data) {

            mapdata[pathplot.id()] = data.Object.Pathplot;
            mapdata[graph.id()] = data.Object.Graph;

            d3.select("#floorplan").append("svg")
                .attr("width", width)
                .attr("height", height)
                .datum(mapdata).call(map);
        });
    }, 1000);
}

function getGraph(type) {

    var edges = [];
    var links = graph.getLinks();
    for (var i = 0; i < links.length; i++) {
        if (links[i].type === type) {
            var edge = {
                "StartNodeId": links[i].source.id,
                "EndNodeId": links[i].target.id
            };
            edges.push(edge);
        }
    }

    var _nodes = [];
    var nodes = graph.getNodes();
    for (i = 0; i < nodes.length; i++) {
        if (nodes[i].type === type) {
            var node = {
                "Id": nodes[i].id,
                "X": (nodes[i].x / width),
                "Y": (nodes[i].y / height)
            };
            _nodes.push(node);
        }
    }
    return { "Nodes": _nodes, "FloorId": this.floorId, "Edges": edges };
}


function saveGraph() {

    var obj = {
        floorId: floorId,
        newGraph: getGraph("new"),
        deletedGraph: getGraph("deleted")
    };

    $("body").addClass("loading");
    $.ajax({
        url: window.basePath + 'Admin/SaveGraphForMap',
        data: JSON.stringify(obj),
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        type: "post",
        success: function() {
            $("body").removeClass("loading");
            init(window.imageUrl);
        }
    });
}

function deleteEdge(startNodeId, endNodeId) {
    console.log("deleteEdge(" + startNodeId + ", " + endNodeId + ")");
    // TODO: Gelöschte Kanten zu einer Liste hinzufügen
}

function addNewEdge(startNodeId) {
    var endNodeId = $('#inputNewEdge').val();
    console.log("addNewEdge(" + startNodeId + ", " + endNodeId + ")");
    // TODO: Hinzugefügte Kanten zu einer Liste hinzufügen
}

// TODO: Gelöschte + Hinzugefügte Kanten an WebService weiterleiten

init(imageUrl);