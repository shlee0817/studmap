using StudMap.Core;
using StudMap.Core.Maps;
using StudMap.Data;
using StudMap.Data.Entities;
using StudMap.Service.CacheObjects;
using System;
using System.Collections.Generic;
using System.Linq;

namespace StudMap.Service.Services
{
    public class FloorService
    {
        // TODO: Auslagern in Config oder so...
        private const string ServerAdminBasePath = "http://193.175.199.115/StudMapAdmin/";

        public static Floor CreateFloor(MapsEntities entities, int mapId, string name)
        {
            if (!MapService.MapExists(mapId))
                throw new ServiceException(ResponseError.MapIdDoesNotExist);

            var newFloor = new Floors
            {
                MapId = mapId,
                Name = name,
                ImageUrl = "",
                CreationTime = DateTime.Now
            };
            Floors insertedFloor = entities.Floors.Add(newFloor);
            entities.SaveChanges();

            StudMapCache.RemoveMap(mapId);

            return Conversions.ToFloor(insertedFloor, ServerAdminBasePath);
        }

        public static void DeleteFloor(MapsEntities entities, int floorId)
        {
            Floors floor = entities.Floors.Find(floorId);
            if (floor == null)
                throw new ServiceException(ResponseError.FloorIdDoesNotExist);
            int mapId = floor.MapId;

            entities.DeleteFloor(floorId);

            StudMapCache.RemoveMap(mapId);
        }

        public static List<Floor> GetFloorsForMap(MapsEntities entities, int mapId)
        {
            bool mapExists = StudMapCache.Global.Maps.ContainsKey(mapId);
            if (!mapExists)
                throw new ServiceException(ResponseError.MapIdDoesNotExist);

            return entities.Floors.Where(f => f.MapId == mapId).ToList()
                .Select(f => Conversions.ToFloor(f, ServerAdminBasePath)).ToList();
        }

        public static List<Floor> GetFloorsForMapCached(int mapId)
        {
            if (!MapService.MapExists(mapId))
                throw new ServiceException(ResponseError.MapIdDoesNotExist);

            return StudMapCache.Map(mapId).Floors;
        }

        public static Dictionary<int, Floor> GetAllFloors(MapsEntities entities)
        {
            return entities.Floors.ToList()
                .Select(f => Conversions.ToFloor(f, ServerAdminBasePath))
                .ToDictionary(f => f.Id);
        }


        public static Floor GetFloor(MapsEntities entities, int floorId)
        {
            Floors floor = entities.Floors.FirstOrDefault(x => x.Id == floorId);
            if (floor == null)
                throw new ServiceException(ResponseError.FloorIdDoesNotExist);

            return Conversions.ToFloor(floor, ServerAdminBasePath);
        }

        public static Floor GetFloorCached(int floorId)
        {
            var cache = StudMapCache.Global.Floors;
            if (!cache.ContainsKey(floorId))
                throw new ServiceException(ResponseError.FloorIdDoesNotExist);
            
            return cache[floorId];
        }

        public static string GetFloorplanImage(MapsEntities entities, int floorId)
        {
            Floor floor = GetFloor(entities, floorId);
            return ServerAdminBasePath + floor.ImageUrl;
        }

        public static string GetFloorplanImageCached(int floorId)
        {
            Floor floor = GetFloorCached(floorId);
            return ServerAdminBasePath + floor.ImageUrl;
        }

        public static Floor UploadFloorImage(MapsEntities entities, int floorId, string filename)
        {
            Floors floor = entities.Floors.FirstOrDefault(x => x.Id == floorId);
            if (floor == null)
                throw new ServiceException(ResponseError.FloorIdDoesNotExist);
            
            floor.ImageUrl = filename;
            entities.SaveChanges();

            return Conversions.ToFloor(floor, ServerAdminBasePath);
        }
    }
}