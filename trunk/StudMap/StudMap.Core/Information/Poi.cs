namespace StudMap.Core.Information
{
    public class PoI
    {
        public PoiType Type { get; set; }
        public string Description { get; set; }
        public int NodeId { get; set; }

        public PoI()
        {
            Type = new PoiType();
            Description = string.Empty;
            NodeId = 0;
        }

        public PoI(PoiType type, string description = "", int nodeID = 0)
        {
            Type = type;
            Description = description;
            NodeId = nodeID;
        }
    }
}
