d3.floorplan.graph = function () {
    var x = d3.scale.linear(),
		y = d3.scale.linear(),
		id = "fp-graph",
	    name = "graph";

    function graph(g) {
        g.each(function (data) {
            if (!data) return;

            $('.fp-graph').children().remove();
            showCircles(d3.select("." + id), data);
        });
    }

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

    function showCircles(el, circlesAsArray) {

        for (var i = 0; i < circlesAsArray.length; i++) {

            var circle = circlesAsArray[i];
            if (circle.HasInformation === true)
                drawCircle(el, circle.X, circle.Y, circle.Id, "active");
            else {
                drawCircle(el, circle.X, circle.Y, circle.Id, "inactive");
            }

            $('#' + circle.Id).on("click", {
                nodeId: circle.Id
            }, onclick);
        }
    }
    
    function onclick(event) {

        var nodeId = event.data.nodeId;

        console.log("Knoten clicked: " + nodeId);
        if (window.jsinterface) {
            window.jsinterface.punkt(nodeId);
        }
    }

    function drawCircle(el, cx, cy, nodeId, className) {

        var circle = el.append("circle")
            .attr("cx", cx * graph.xScale().range()[1])
            .attr("cy", cy * graph.yScale().range()[1])
            .attr("id", nodeId)
            .attr("r", 2)
            .attr("class", "node " + className);

        return circle;
    }

    return graph;
};