using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Entity;
using System.Linq;
using System.Web.Http;
using Microsoft.Ajax.Utilities;
using Microsoft.Data.OData.Query.SemanticAst;
using StudMap.Core;
using StudMap.Core.Graph;
using StudMap.Core.Information;
using StudMap.Core.Maps;
using StudMap.Data.Entities;
using QuickGraph;
using QuickGraph.Algorithms;
using NodeInformation = StudMap.Core.Information.NodeInformation;

namespace StudMap.Service.Controllers
{
    public class MapsController : ApiController
    {
        #region Map

        public ObjectResponse<Map> CreateMap(string mapName)
        {
            var result = new ObjectResponse<Map>();

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

                    result.Object = new Map
                        {
                            Id = insertedMap.Id,
                            Name = insertedMap.Name
                        };
                }
            }
            catch (DataException ex)
            {
                result.Status = RespsonseStatus.Error;
                result.ErrorCode = ResponseError.DatabaseError;
                result.ErrorMessage = ex.StackTrace;
                Elmah.ErrorSignal.FromCurrentContext().Raise(ex);
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
                    entities.DeleteMap(mapId);
                }
            }
            catch (DataException ex)
            {
                result.Status = RespsonseStatus.Error;
                result.ErrorCode = ResponseError.DatabaseError;
                result.ErrorMessage = ex.StackTrace;
                Elmah.ErrorSignal.FromCurrentContext().Raise(ex);
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
            catch (DataException ex)
            {
                result.Status = RespsonseStatus.Error;
                result.ErrorCode = ResponseError.DatabaseError;
                result.ErrorMessage = ex.StackTrace;
                Elmah.ErrorSignal.FromCurrentContext().Raise(ex);
            }

            return result;
        }

        #endregion

        #region Floor

        [HttpPost]
        public ObjectResponse<Floor> CreateFloor(int mapId, string name = "")
        {
            var result = new ObjectResponse<Floor>();

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

                    result.Object = new Floor
                    {
                        Id = insertedFloor.Id,
                        MapId = insertedFloor.MapId,
                        Name = insertedFloor.Name,
                        ImageUrl = insertedFloor.ImageUrl,
                        CreationTime = insertedFloor.CreationTime
                    };
                }
            }
            catch (DataException ex)
            {
                result.Status = RespsonseStatus.Error;
                result.ErrorCode = ResponseError.DatabaseError;
                result.ErrorMessage = ex.StackTrace;
                Elmah.ErrorSignal.FromCurrentContext().Raise(ex);
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
                    entities.DeleteFloor(floorId);
                }
            }
            catch (DataException ex)
            {
                result.Status = RespsonseStatus.Error;
                result.ErrorCode = ResponseError.DatabaseError;
                result.ErrorMessage = ex.StackTrace;
                Elmah.ErrorSignal.FromCurrentContext().Raise(ex);
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
            catch (DataException ex)
            {
                result.Status = RespsonseStatus.Error;
                result.ErrorCode = ResponseError.DatabaseError;
                result.ErrorMessage = ex.StackTrace;
                Elmah.ErrorSignal.FromCurrentContext().Raise(ex);
            }

            return result;
        }

        public ObjectResponse<Floor> GetFloor(int floorId)
        {
            var result = new ObjectResponse<Floor>();
            try
            {
                using (var entities = new MapsEntities())
                {
                    var floor = entities.Floors.FirstOrDefault(x => x.Id == floorId);
                    if (floor != null)
                    {
                        result.Status = RespsonseStatus.Ok;
                        result.Object = new Floor
                        {
                            Id = floor.Id,
                            ImageUrl = floor.ImageUrl,
                            Name = floor.Name,
                            MapId = floor.MapId
                        };
                    }
                    else
                    {
                        result.Status = RespsonseStatus.Error;
                        result.ErrorCode = ResponseError.FloorIdDoesNotExist;
                    }
                }
            }
            catch (DataException ex)
            {
                result.Status = RespsonseStatus.Error;
                result.ErrorCode = ResponseError.DatabaseError;
                result.ErrorMessage = ex.StackTrace;
                Elmah.ErrorSignal.FromCurrentContext().Raise(ex);
            }
            return result;
        }

        public ObjectResponse<string> GetFloorplanImage(int floorId)
        {
            var result = new ObjectResponse<string>();

            try
            {
                using (var entities = new MapsEntities())
                {
                    var floor = entities.Floors.FirstOrDefault(f => f.Id == floorId);
                    if (floor != null)
                    {
                        result.Object = floor.ImageUrl;
                    }
                    else
                    {
                        result.Status = RespsonseStatus.Error;
                        result.ErrorCode = ResponseError.FloorIdDoesNotExist;
                    }
                }
            }
            catch (DataException ex)
            {
                result.Status = RespsonseStatus.Error;
                result.ErrorCode = ResponseError.DatabaseError;
                result.ErrorMessage = ex.StackTrace;
                Elmah.ErrorSignal.FromCurrentContext().Raise(ex);
            }
            return result;
        }

        [HttpPost]
        public ObjectResponse<Floor> UploadFloorImage(int floorId, string filename)
        {
            var result = new ObjectResponse<Floor>();
            try
            {
                using (var entities = new MapsEntities())
                {
                    var floor = entities.Floors.FirstOrDefault(x => x.Id == floorId);
                    if (floor != null)
                    {
                        floor.ImageUrl = filename;

                        entities.SaveChanges();

                        result.Status = RespsonseStatus.Ok;
                        result.Object = new Floor
                            {
                                Id = floor.Id,
                                ImageUrl = floor.ImageUrl,
                                Name = floor.Name
                            };
                    }
                    else
                    {
                        result.Status = RespsonseStatus.Error;
                        result.ErrorCode = ResponseError.FloorIdDoesNotExist;
                    }
                }
            }
            catch (DataException ex)
            {
                result.Status = RespsonseStatus.Error;
                result.ErrorCode = ResponseError.DatabaseError;
                result.ErrorMessage = ex.StackTrace;
                Elmah.ErrorSignal.FromCurrentContext().Raise(ex);
            }
            return result;
        }

        #endregion

        #region Layer: Graph

        [HttpPost]
        public ObjectResponse<Graph> SaveGraphForFloor(int floorId, Graph graph)
        {
            var result = new ObjectResponse<Graph>();
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

                    var nodeIdMap = new Dictionary<int, int>();
                    
                    // Nodes in den Floor hinzufügen
                    if (graph.Nodes != null)
                    {
                        foreach (Node node in graph.Nodes)
                        {
                            var newNode = new Nodes
                            {
                                FloorId = floorId,
                                X = node.X,
                                Y = node.Y,
                                CreationTime = DateTime.Now
                            };
                            entities.Nodes.Add(newNode);
                            entities.SaveChanges();
                            nodeIdMap.Add(node.Id, newNode.Id);
                        }
                    }
                    
                    // Edges im Graph hinzufügen
                    Floors targetFloor = foundFloors.First();
                    Graphs newGraph = entities.Graphs.Add(new Graphs {
                        MapId  = targetFloor.MapId,
                        CreationTime = DateTime.Now
                    });

                    if (graph.Edges != null)
                    {
                        foreach (Edge edge in graph.Edges)
                        {
                            newGraph.Edges.Add(new Edges
                            {
                                NodeStartId = nodeIdMap[edge.StartNodeId],
                                NodeEndId = nodeIdMap[edge.EndNodeId],
                                CreationTime = DateTime.Now
                            });
                        }
                    }

                    entities.SaveChanges();
                }
            }
            catch (DataException ex)
            {
                result.Status = RespsonseStatus.Error;
                result.ErrorCode = ResponseError.DatabaseError;
                result.ErrorMessage = ex.StackTrace;
                Elmah.ErrorSignal.FromCurrentContext().Raise(ex);
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
                    var floorToClear = entities.Floors.Find(floorId);
                    if (floorToClear == null)
                        return result;

                    var nodes = floorToClear.Nodes;
                    var nodeIds = nodes.Select(n => n.Id);
                    var enumerable = nodeIds as int[] ?? nodeIds.ToArray();
                    var edges = from edge in entities.Edges
                                where enumerable.Contains(edge.NodeStartId)
                                || enumerable.Contains(edge.NodeEndId)
                                select edge;
                    
                    entities.Edges.RemoveRange(edges);
                    entities.Nodes.RemoveRange(nodes);
                    entities.SaveChanges();
                }
            }
            catch (DataException ex)
            {
                result.Status = RespsonseStatus.Error;
                result.ErrorCode = ResponseError.DatabaseError;
                result.ErrorMessage = ex.StackTrace;
                Elmah.ErrorSignal.FromCurrentContext().Raise(ex);
            }

            return result;
        }

        public ObjectResponse<Graph> GetGraphForFloor(int floorId)
        {
            var result = new ObjectResponse<Graph>();

            try
            {
                using (var entities = new MapsEntities())
                {
                    var queriedFloor = entities.Floors.Find(floorId);
                    if (queriedFloor == null)
                    {
                        result.SetError(ResponseError.FloorIdDoesNotExist);
                        return result;
                    }

                    var nodes = queriedFloor.Nodes;
                    var nodeIds = nodes.Select(n => n.Id);
                    var enumerable = nodeIds as int[] ?? nodeIds.ToArray();
                    var edges = from edge in entities.Edges
                                where enumerable.Contains(edge.NodeStartId)
                                || enumerable.Contains(edge.NodeEndId)
                                select edge;

                    var graph = new Graph
                    {
                        FloorId = floorId,
                        Edges = (from edge in edges
                                select new Edge
                                {
                                    StartNodeId = edge.NodeStartId,
                                    EndNodeId = edge.NodeEndId
                                }).ToList(),
                        Nodes = (from node in nodes
                                select new Node
                                {
                                    Id = node.Id,
                                    X = node.X,
                                    Y = node.Y
                                }).ToList()
                    };

                    result.Object = graph;
                }
            }
            catch (DataException ex)
            {
                result.Status = RespsonseStatus.Error;
                result.ErrorCode = ResponseError.DatabaseError;
                result.ErrorMessage = ex.StackTrace;
                Elmah.ErrorSignal.FromCurrentContext().Raise(ex);
            }

            return result;
        }

        public ObjectResponse<NodeInformation> GetNodeInformationForNode(int nodeId)
        {
            var result = new ObjectResponse<NodeInformation>();

            try
            {
                using (var entities = new MapsEntities())
                {
                    var node = entities.Nodes.Find(nodeId);
                    result.Object = new NodeInformation();
                    if (node == null)
                        return result;

                    result.Object.Node = new Node
                        {
                            FloorId = node.FloorId,
                            X = node.X,
                            Y = node.Y,
                            Id = node.Id
                        };
                    var queriedNodeInformation = entities.NodeInformation.FirstOrDefault(x => x.NodeId == nodeId);
                    if (queriedNodeInformation == null)
                        return result;

                    result.Object.DisplayName = queriedNodeInformation.DisplayName;
                    result.Object.RoomName = queriedNodeInformation.RoomName;
                }
            }
            catch (DataException ex)
            {
                result.Status = RespsonseStatus.Error;
                result.ErrorCode = ResponseError.DatabaseError;
                result.ErrorMessage = ex.StackTrace;
                Elmah.ErrorSignal.FromCurrentContext().Raise(ex);
            }

            return result;
        }

        [HttpGet]
        public ObjectResponse<FloorPlanData> GetFloorPlanData(int floorId)
        {
            var floorPlanData = new ObjectResponse<FloorPlanData> {Object = new FloorPlanData()};
            var result = GetGraphForFloor(floorId);
            if (result.Status == RespsonseStatus.Ok)
            {
                floorPlanData.Object.Graph = result.Object;
            }
            return floorPlanData;
        }
        
        [HttpPost]
        public ObjectResponse<NodeInformation> SaveNodeInformation(int nodeId, NodeInformation nodeInf)
        {
            var result = new ObjectResponse<NodeInformation>();
            try
            {
                using (var entities = new MapsEntities())
                {
                    //Schon vorhandene Nodeinformationen suchen
                    var nodeInformation = entities.NodeInformation.FirstOrDefault(x => x.NodeId == nodeId);
                    bool poiTypeSelected = nodeInf.PoI.Type.Id != 0;
                    PoIs poi = null;

                    if (poiTypeSelected)
                    {
                        var poiType = entities.PoiTypes.FirstOrDefault(x => x.Id == nodeInf.PoI.Type.Id);
                        if (poiType == null)
                        {
                            result.SetError(ResponseError.PoiTypeIdDoesNotExist);
                            return result;
                        }
                        nodeInf.PoI.Type.Name = poiType.Name;

                        poi = entities.PoIs.Add(new PoIs()
                        {
                            TypeId = nodeInf.PoI.Type.Id,
                            Description = nodeInf.PoI.Description
                        });
                        entities.SaveChanges();
                    }
                        

                    //Wenn es keine Nodeinformations gibt neue anlegen
                    if (nodeInformation == null)
                    {
                        nodeInformation = entities.NodeInformation.Add(new Data.Entities.NodeInformation
                        {
                            DisplayName = nodeInf.DisplayName,
                            RoomName = nodeInf.RoomName,
                            PoiId = poiTypeSelected ? (int?)poi.Id : null,
                            NodeId = nodeId,
                            CreationTime = DateTime.Now
                        });
                        entities.SaveChanges();
                    }
                    else
                    {
                        //Aktualisieren
                        nodeInformation.DisplayName = nodeInf.DisplayName;
                        nodeInformation.RoomName = nodeInf.RoomName;

                        nodeInformation.PoiId = poiTypeSelected ? (int?) poi.Id : null;
                        entities.SaveChanges();
                    }

                    //Ergebnis aus der DB in Rückgabe Objekt schreiben
                    result.Object = new NodeInformation
                    {
                        DisplayName = nodeInformation.DisplayName,
                        PoI = nodeInf.PoI,
                        RoomName = nodeInformation.RoomName,
                        Node = new Node
                        {
                            FloorId = nodeInformation.Nodes.FloorId,
                            Id = nodeInformation.Nodes.Id,
                            X = nodeInformation.Nodes.X,
                            Y = nodeInformation.Nodes.Y
                        }
                    };
                }
            }
            catch (DataException ex)
            {
                result.Status = RespsonseStatus.Error;
                result.ErrorCode = ResponseError.DatabaseError;
                result.ErrorMessage = ex.StackTrace;
                Elmah.ErrorSignal.FromCurrentContext().Raise(ex);
            }
            return result;
        }
        #endregion

        #region Layer: Navigation

        public ListResponse<Node> GetRouteBetween(int mapId, int startNodeId, int endNodeId)
        {
            var result = new ListResponse<Node>();

            try
            {
                using (var entities = new MapsEntities())
                {

                    var routeNodes = new List<Node>();

                    var nodes = from node in entities.Nodes
                                where node.Floors.MapId == mapId
                                select node;

                    var nodesMap = nodes.ToDictionary(node => node.Id);

                    if (!nodesMap.ContainsKey(startNodeId))
                    {
                        result.SetError(ResponseError.StartNodeNotFound);
                        return result;
                    }
                    if (!nodesMap.ContainsKey(endNodeId))
                    {
                        result.SetError(ResponseError.EndNodeNotFound);
                        return result;
                    }

                    var edges = (from edge in entities.Edges
                                where edge.Graphs.MapId == mapId
                                select edge).ToList();

                    var graph = new BidirectionalGraph<int, Edge<int>>();

                    foreach (var nodeId in nodesMap.Keys)
                    {
                        graph.AddVertex(nodeId);
                    }

                    foreach (var edge in edges)
                    {
                        graph.AddEdge(new Edge<int>(edge.NodeStartId, edge.NodeEndId));
                    }

                    Func<Edge<int>, double> edgeCost = e => GetEdgeCost(nodesMap, e);

                    var tryGetPath = graph.ShortestPathsDijkstra(edgeCost, startNodeId);

                    IEnumerable<Edge<int>> routeEdges;
                    if (tryGetPath(endNodeId, out routeEdges))
                    {
                        Edge<int> lastEdge = null;
                        foreach (var routeEdge in routeEdges)
                        {
                            var dbNode = nodesMap[routeEdge.Source];
                            routeNodes.Add(NodeFromDb(dbNode));
                            lastEdge = routeEdge;
                        }

                        if (lastEdge == null)
                            routeNodes.Add(NodeFromDb(nodesMap[startNodeId]));
                        else
                            routeNodes.Add(NodeFromDb(nodesMap[lastEdge.Target]));

                        result.List = routeNodes;
                    }
                    else
                    {
                        result.SetError(ResponseError.NoRouteFound);
                        return result;
                    }
                }
            }
            catch (DataException ex)
            {
                result.Status = RespsonseStatus.Error;
                result.ErrorCode = ResponseError.DatabaseError;
                result.ErrorMessage = ex.StackTrace;
                Elmah.ErrorSignal.FromCurrentContext().Raise(ex);
            }

            return result;
        }

        private Node NodeFromDb(Nodes dbNode)
        {
            return new Node
            {
                Id = dbNode.Id,
                X = dbNode.X,
                Y = dbNode.Y,
                FloorId = dbNode.FloorId
            };
        }

        private double GetEdgeCost(Dictionary<int, Nodes> nodesMap, Edge<int> e)
        {
            var startNode = nodesMap[e.Source];
            var endNode = nodesMap[e.Target];

            var diffX = endNode.X - startNode.X;
            var diffY = endNode.Y - startNode.Y;
            // Unterschiede im Stockwerk beachten
            // Da Höhe unbekannt einfach irgendwelche Kosten festlegen
            var diffZ = endNode.FloorId != startNode.FloorId ? 0.2m : 0m;

            return Math.Sqrt((double)(diffX * diffX + diffY * diffY + diffZ * diffZ));
        }

        #endregion

        #region Layer: Information

        public ListResponse<PoiType> GetPoiTypes()
        {
            var result = new ListResponse<PoiType>();
            try
            {
                using (var entities = new MapsEntities())
                {
                    result.List.Add(new PoiType { Id = 0, Name = "Kein" });
                    result.List.AddRange((from type in entities.PoiTypes
                        select new PoiType()
                        {
                            Id = type.Id,
                            Name = type.Name
                        }));
                }
            }
            catch (DataException)
            {
                result.SetError(ResponseError.DatabaseError);
            }
            return result;
        }

        public ListResponse<PoI> GetPoIs()
        {
            var result = new ListResponse<PoI>();
            try
            {
                using (var entities = new MapsEntities())
                {

                    result.List = (from poi in entities.PoIs
                        select new PoI
                        {
                            Type = new PoiType()
                            {
                                Id = poi.PoiTypes.Id,
                                Name = poi.PoiTypes.Name
                            },
                            Description = poi.Description,

                        }).ToList();
                }
            }
            catch (DataException)
            {
                result.SetError(ResponseError.DatabaseError);
            }
            return result;
        }

        #endregion // Layer: Information
    }
}