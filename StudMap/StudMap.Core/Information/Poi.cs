namespace StudMap.Core.Information
{
    public class PoI
    {
        public PoI()
        {
            Type = new PoiType();
            Description = string.Empty;
        }

        public PoI(PoiType type, string description = "")
        {
            Type = type;
            Description = description;
        }

        public PoiType Type { get; set; }
        public string Description { get; set; }
    }
}