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
        g.each(function (data) {
            if (!data) return;

            d3.select("svg").on("mousedown", function () {
                

                d3.select("svg").classed("active", true);

                if (d3.event.ctrlKey || mousedownNode) return;
                
                nodes.push({ id: ++nodeCount, fixed: true, x: d3.event.offsetX, y: d3.event.offsetY });
                graph.start();
            }).on("mouseup", function () {
                d3.select("svg").classed("active", false);
                resetMouseVars();
            });

            var g = d3.select(this);
            node = g.selectAll(".node");
            link = g.selectAll(".link");
        });
    }

    graph.start = function () {
        link = link.data(links, function (d) { return d.source.id + "-" + d.target.id; });
        link.enter().insert("path", ".node").attr("class", "link").attr("d", function(d) {
            return "M" + d.target.x + "," + d.target.y + "L" + d.source.x + "," + d.source.y;
        });
        link.exit().remove();

        node = node.data(nodes, function (d) { return d.id; });
        node.enter()
            .append("circle")
            .attr("class", function (d) { return "node"; })
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

    graph.getNodes = function() {
        return nodes;
    };

    graph.getLinks = function() {
        return links;
    };

    return graph;
};