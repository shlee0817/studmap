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
        public FloorsResponse CreateFloor(int mapId, string name = "")
        {
            var result = new FloorsResponse();

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

                    var newFloor = new Floors
                    {
                        MapId = mapId,
                        Name = name,
                        ImageUrl = "",
                        CreationTime = DateTime.Now
                    };
                    var insertedFloor = entities.Floors.Add(newFloor);
                    entities.SaveChanges();

                    result.Floor = new Floor
                    {
                        Id = insertedFloor.Id,
                        MapId = insertedFloor.MapId,
                        Name = insertedFloor.Name,
                        ImageUrl = insertedFloor.ImageUrl,
                        CreationTime = insertedFloor.CreationTime
                    };
                }
            }
            catch (DataException )
            {
                result.Status = RespsonseStatus.Error;
                result.ErrorCode = ResponseError.DatabaseError;
            }

            return result;
        }

        [HttpPost]
        public BaseResponse DeleteFloor(int floorId)
        {
            var result = new BaseResponse();

            try
            {
                using (var entities = new MapsEntities())
                {
                    var floorToDelete = entities.Floors.Find(floorId);
                    if (floorToDelete == null)
                        return result;
                    entities.Floors.Remove(floorToDelete);
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
                                   MapId = mapId,
                                   Name =  floor.Name,
                                   ImageUrl = floor.ImageUrl,
                                   CreationTime = floor.CreationTime
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

        public FloorImageResponse GetFloorplanImage(int floorId)
        {
            var result = new FloorImageResponse();

            try
            {
                using (var entities = new MapsEntities())
                {
                    var floor = entities.Floors.FirstOrDefault(f => f.Id == floorId);
                    if (floor != null)
                    {
                        result.ImageUrl = floor.ImageUrl;
                    }
                    else
                    {
                        result.Status = RespsonseStatus.Error;
                        result.ErrorCode = ResponseError.FloorIdDoesNotExist;
                    }
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

        [HttpPost]
        public GraphResponse SaveGraphForFloor(int floorId, Graph graph)
        {
            var result = new GraphResponse();

            try
            {
                using (var entities = new MapsEntities())
                {
                    // Zuerst den alten Graphen löschen
                    var deleteResult = DeleteGraphForFloor(floorId);
                    if (deleteResult.Status == RespsonseStatus.Error)
                    {
                        result.SetError(deleteResult.ErrorCode);
                        return result;
                    }

                    var foundFloors = from floor in entities.Floors
                                      where floor.Id == floorId
                                      select floor;
                    if (!foundFloors.Any())
                    {
                        result.SetError(ResponseError.FloorIdDoesNotExist);
                        return result;
                    }

                    Dictionary<int, int> nodeIdMap = new Dictionary<int, int>();
                    
                    // Nodes in den Floor hinzufügen
                    foreach (Node node in graph.Nodes) 
                    {
                        var newNode = new Nodes {
                            FloorId = floorId,
                            X = node.X,
                            Y = node.Y,
                            CreationTime = DateTime.Now
                        };
                        entities.Nodes.Add(newNode);
                        entities.SaveChanges();
                        nodeIdMap.Add(node.Id, newNode.Id);
                    }
                    
                    // Edges im Graph hinzufügen
                    Floors targetFloor = foundFloors.First();
                    Graphs newGraph = entities.Graphs.Add(new Graphs {
                        MapId  = targetFloor.MapId,
                        CreationTime = DateTime.Now
                    });

                    foreach (Edge edge in graph.Edges)
                    {
                        newGraph.Edges.Add(new Edges
                        {
                            NodeStartId = nodeIdMap[edge.StartNodeId],
                            NodeEndId = nodeIdMap[edge.EndNodeId],
                            CreationTime = DateTime.Now
                        });
                    }

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

        [HttpPost]
        public BaseResponse DeleteGraphForFloor(int floorId)
        {
            var result = new BaseResponse();

            try
            {
                using (var entities = new MapsEntities())
                {
                    var floorToDelete = entities.Floors.Find(floorId);
                    if (floorToDelete == null)
                        return result;

                    var nodes = floorToDelete.Nodes;
                    var nodeIds = nodes.Select(n => n.Id);
                    var edges = from edge in entities.Edges
                                where nodeIds.Contains(edge.NodeStartId)
                                || nodeIds.Contains(edge.NodeEndId)
                                select edge;
                    
                    entities.Edges.RemoveRange(edges);
                    entities.Nodes.RemoveRange(nodes);
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