namespace StudMap.Core.Information
{
    public class PoiType
    {
        public int Id { get; set; }
        public string Name { get; set; }

        public PoiType()
        {
            Id = 0;
            Name = "";
        }

        public PoiType(int id, string name)
        {
            Id = id;
            Name = name;
        }
    }
}
