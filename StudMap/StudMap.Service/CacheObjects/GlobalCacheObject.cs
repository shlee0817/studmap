using System.Collections.Generic;
using StudMap.Core.Maps;
using StudMap.Data.Entities;
using StudMap.Service.Services;

namespace StudMap.Service.CacheObjects
{
    /// <summary>
    ///     Dieses Cache-Objekt enthält globale Daten wie z.B. die Liste aller Karten.
    /// </summary>
    public class GlobalCacheObject : CacheObject
    {
        public GlobalCacheObject()
        {
            TimeoutInMinutes = 24 * 60;

            using (var entities = new MapsEntities())
            {
                Update(entities);
            }
        }

        public Dictionary<int, Map> Maps { get; set; }

        public Dictionary<int, Floor> Floors { get; set; }

        private void Update(MapsEntities entities)
        {
            UpdateMaps(entities);
            UpdateFloors(entities);
        }

        public void UpdateMaps(MapsEntities entities)
        {
            Maps = MapService.GetMaps(entities);
        }

        public void UpdateFloors(MapsEntities entities)
        {
            Floors = FloorService.GetAllFloors(entities);
        }
    }
}