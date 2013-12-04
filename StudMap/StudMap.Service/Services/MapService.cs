using StudMap.Core.Maps;
using StudMap.Data;
using StudMap.Data.Entities;
using StudMap.Service.CacheObjects;
using System;
using System.Collections.Generic;
using System.Linq;

namespace StudMap.Service.Services
{
    public class MapService
    {
        public static Map CreateMap(MapsEntities entities, string mapName)
        {
            var newMap = new Maps
            {
                Name = mapName,
                CreationTime = DateTime.Now
            };
            Maps insertedMap = entities.Maps.Add(newMap);
            entities.SaveChanges();

            StudMapCache.Global.UpdateMaps(entities);

            return Conversions.ToMap(insertedMap);
        }

        public static void DeleteMap(MapsEntities entities, int mapId)
        {
            entities.DeleteMap(mapId);

            StudMapCache.Global.UpdateMaps(entities);
        }

        public static Dictionary<int, Map> GetMaps(MapsEntities entities)
        {
            var maps = entities.Maps.ToList().Select(Conversions.ToMap);
            return maps.ToDictionary(m => m.Id);
        }

        public static IEnumerable<Map> GetMapsCached()
        {
            return StudMapCache.Global.Maps.Values;
        }

        public static bool MapExists(int mapId)
        {
            return StudMapCache.Global.Maps.ContainsKey(mapId);
        }

        public static Map GetMap(MapsEntities entities, int mapId)
        {
            if (MapExists(mapId))
                return StudMapCache.Global.Maps[mapId];
            else
                return new Map();
        }
    }
}