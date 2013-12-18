using StudMap.Core.Maps;

namespace StudMap.Core.Information
{
    public class FullNodeInformation
    {
        public Map Map { get; set; }

        public Floor Floor { get; set; }

        public NodeInformation Info { get; set; }
    }
}