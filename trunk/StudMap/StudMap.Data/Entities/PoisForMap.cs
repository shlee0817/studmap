//------------------------------------------------------------------------------
// <auto-generated>
//    This code was generated from a template.
//
//    Manual changes to this file may cause unexpected behavior in your application.
//    Manual changes to this file will be overwritten if the code is regenerated.
// </auto-generated>
//------------------------------------------------------------------------------

namespace StudMap.Data.Entities
{
    using System;
    using System.Collections.Generic;
    
    public partial class PoisForMap
    {
        public int MapId { get; set; }
        public int PoiTypeId { get; set; }
        public string PoiTypeName { get; set; }
        public int PoiId { get; set; }
        public string PoiDescription { get; set; }
        public int NodeId { get; set; }
        public string DisplayName { get; set; }
        public string RoomName { get; set; }
        public int FloorId { get; set; }
    }
}
