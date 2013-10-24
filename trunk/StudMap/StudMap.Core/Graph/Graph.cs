using System.Collections.Generic;

namespace StudMap.Core.Graph
{
    public class Graph
    {
        public int FloorId { get; set; }

        public IEnumerable<Edge> Edges { get; set; }

        public IEnumerable<Node> Nodes { get; set; }
    }
}
