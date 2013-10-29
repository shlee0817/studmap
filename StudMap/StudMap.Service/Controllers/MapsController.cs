using System;
using System.Collections.Generic;
using System.Data;
using System.Linq;
using System.Web.Http;
using StudMap.Core;
using StudMap.Core.Graph;
using StudMap.Core.Maps;
using StudMap.Data.Entities;

namespace StudMap.Service.Controllers
{
    public class MapsController : ApiController
    {
        #region Map

        public MapsResponse CreateMap(string mapName)
        {
            var result = new MapsResponse();

            try
            {
                using (var entities = new MapsEntities())
                {
                    var newMap = new Maps
                        {
                            Name = mapName,
                            CreationTime = DateTime.Now
                        };
                    Maps insertedMap = entities.Maps.Add(newMap);
                    entities.SaveChanges();

                    result.Map = new Map
                        {
                            Id = insertedMap.Id,
                            Name = insertedMap.Name
                        };
                }
            }
            catch (DataException)
            {
                result.Status = RespsonseStatus.Error;
                result.ErrorCode = ResponseError.DatabaseError;
            }

            return result;
        }

        public BaseResponse DeleteMap(int mapId)
        {
            var result = new BaseResponse();

            try
            {
                using (var entities = new MapsEntities())
                {
                    var mapToDelete = entities.Maps.Find(mapId);
                    if (mapToDelete == null)
                        return result;
                    entities.Maps.Remove(mapToDelete);
                    entities.SaveChanges();
                }
            }
            catch (DataException)
            {
                result.Status = RespsonseStatus.Error;
                result.ErrorCode = ResponseError.DatabaseError;
            }

            return result;
        }

        public ListResponse<Map> GetMaps()
        {
            var result = new ListResponse<Map>();

            try
            {
                using (var entities = new MapsEntities())
                {
                    var maps = from map in entities.Maps
                                           select new Map
                                               {
                                                   Id = map.Id,
                                                   Name = map.Name
                                               };
                    result.List = maps.ToList();
                }
            }
            catch (DataException)
            {
                result.Status = RespsonseStatus.Error;
                result.ErrorCode = ResponseError.DatabaseError;
            }

            return result;
        }

        #endregion

        #region Floor

        [HttpPost]
        public FloorsResponse CreateFloor(int mapId)
        {
            //TODO Post Body auslesen 
            return new FloorsResponse
                {
                    Status = RespsonseStatus.Ok,
                    ErrorCode = ResponseError.None,
                    Floor = new Floor
                        {
                            Id = 1,
                            ImageUrl = "",
                            MapId = 1
                        }
                };
        }

        public BaseResponse DeleteFloor(int floorId)
        {
            //TODO Implement
            return new BaseResponse
                {
                    Status = RespsonseStatus.Ok,
                    ErrorCode = ResponseError.None
                };
        }

        public ListResponse<Floor> GetFloorsForMap(int mapId)
        {
            var result = new ListResponse<Floor>();

            try
            {
                using (var entities = new MapsEntities())
                {
                    bool mapExists = entities.Maps.Any(m => m.Id == mapId);
                    if (!mapExists)
                    {
                        result.SetError(ResponseError.MapIdDoesNotExist);
                        return result;
                    }

                    var floors = from floor in entities.Floors
                               select new Floor
                               {
                                   Id = floor.Id,
                                   MapId = mapId
                               };
                    result.List = floors.ToList();
                }
            }
            catch (DataException)
            {
                result.Status = RespsonseStatus.Error;
                result.ErrorCode = ResponseError.DatabaseError;
            }

            return result;
        }

        [HttpPost]
        public FloorsResponse UploadFloorImage(int floorId, [FromBody] object floor)
        {
            //TODO Implement 
            return new FloorsResponse
                {
                    Status = RespsonseStatus.Ok,
                    ErrorCode = ResponseError.None,
                    Floor = new Floor
                        {
                            Id = 1,
                            ImageUrl = "",
                            MapId = 1
                        }
                };
        }

        #endregion

        #region Layer: Graph

        public GraphResponse SaveGraphForFloor(int floorId, Graph graph)
        {
            //TODO Implement
            return new GraphResponse
                {
                    Status = RespsonseStatus.Ok,
                    ErrorCode = ResponseError.None,
                    Graph = new Graph
                        {
                            Edges = new List<Edge>(),
                            Nodes = new List<Node>()
                        }
                };
        }

        public GraphResponse GetGraphForFloor(int floorId)
        {
            //TODO Implement
            return new GraphResponse
                {
                    Status = RespsonseStatus.Ok,
                    ErrorCode = ResponseError.None,
                    Graph = new Graph
                        {
                            Edges = new List<Edge>(),
                            Nodes = new List<Node>()
                        }
                };
        }

        #endregion
    }
}