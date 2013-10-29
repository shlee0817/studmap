using System;
namespace StudMap.Core.Maps
{
    public class Floor
    {
        public int Id { get; set; }

        public int MapId { get; set; }

        public string Name { get; set; }

        public string ImageUrl { get; set; }

        public DateTime CreationTime { get; set; }
    }
}
