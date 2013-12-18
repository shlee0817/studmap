//
//   Copyright 2012 David Ciarletta
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//

d3.floorplan.pathplot = function() {
	var x = d3.scale.linear(),
	y = d3.scale.linear(),
	line = d3.svg.line()
		.x(function(d) { return x(d.X); })
		.y(function(d) { return y(d.Y); }),
	id = "fp-pathplot",
	name = "pathplot",
	pointFilter = function(d) {
	    return d.List.filter(function(node) {

	        if (d.List.length === 0)
	            return false;

	        if (node.FloorId !== d.List[0].FloorId)
	            d.floorMustBeChanged = true;
	        
	        return node.FloorId === d.List[0].FloorId;
	    });
	};
	
	function pathplot(g) {
		g.each(function(data) {
			if (!data) return;
			
		    var g = d3.select(this),
			path = g.append("path");
			
			path.attr("vector-effect", "non-scaling-stroke")
			    .attr("fill", "none").attr("class", function () { return "path"; })
		        .attr("marker-start", "url(#StartMarker)")
		        .attr("marker-end", function (d) { return d.floorMustBeChanged ? "url(#ChangeMarker)" : "url(#EndMarker)"; })
			    .attr("d", function(d,i) { return line(pointFilter(d,i)); });
		});
	}
	
	pathplot.xScale = function(scale) {
		if (! arguments.length) return x;
		x = scale;
		return pathplot;
	};
	
	pathplot.yScale = function(scale) {
		if (! arguments.length) return y;
		y = scale;
		return pathplot;
	};

	pathplot.id = function() {
		return id;
	};
	
	pathplot.title = function(n) {
		if (! arguments.length) return name;
		name = n;
		return pathplot;
	};

	pathplot.pointFilter = function(fn) {
		if (! arguments.length) return pointFilter;
		pointFilter = fn;
		return pathplot;
	};
	
	return pathplot;
};