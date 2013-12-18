using System.Collections.Generic;

namespace StudMap.Core.Graph
{
    //"{\"pathplot\":[
    //{\"id\":\"flt-1\",\"classes\":\"planned\",\"points\":[{\"x\":23.8,\"y\":30.6},{\"x\":19.5,\"y\":25.7},{\"x\":14.5,\"y\":25.7},{\"x\":13.2,\"y\":12.3}]}
    //],
    //\"graph\":[{\"id\":\"flt-2\",\"classes\":\"planned\",
    //\"points\":[{\"x\":23.8,\"y\":30.6},{\"x\":19.5,\"y\":25.7},{\"x\":14.5,\"y\":25.7},{\"x\":13.2,\"y\":12.3}]}]}";
    public class FloorPlanData
    {
        public Pathplot Pathplot { get; set; }
        public Graph Graph { get; set; }
    }

    public class Pathplot
    {
        public string Classes = "planned";
        public string Id = "flt-1";
        public List<Node> Points = new List<Node>();
    }
}