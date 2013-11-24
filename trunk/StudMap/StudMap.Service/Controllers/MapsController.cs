using System;
using System.Collections.Generic;
using System.Data;
using System.Linq;
using System.Web.Http;
using Elmah;
using QuickGraph;
using QuickGraph.Algorithms;
using StudMap.Core;
using StudMap.Core.Graph;
using StudMap.Core.Information;
using StudMap.Core.Maps;
using StudMap.Data.Entities;
using NodeInformation = StudMap.Core.Information.NodeInformation;

namespace StudMap.Service.Controllers
{
    public class MapsController : ApiController
    {
        private const string ServerAdminBasePath = "http://193.175.199.115/StudMapAdmin/";

        #region Map

        [HttpPost]
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
                ErrorSignal.FromCurrentContext().Raise(ex);
            }

            return result;
        }

        [HttpPost]
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
                ErrorSignal.FromCurrentContext().Raise(ex);
            }

            return result;
        }

        [HttpGet]
        public ListResponse<Map> GetMaps()
        {
            var result = new ListResponse<Map>();

            try
            {
                using (var entities = new MapsEntities())
                {
                    IQueryable<Map> maps = from map in entities.Maps
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
                ErrorSignal.FromCurrentContext().Raise(ex);
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
                    Floors insertedFloor = entities.Floors.Add(newFloor);
                    entities.SaveChanges();

                    result.Object = new Floor
                        {
                            Id = insertedFloor.Id,
                            MapId = insertedFloor.MapId,
                            Name = insertedFloor.Name,
                            ImageUrl = ServerAdminBasePath + insertedFloor.ImageUrl,
                            CreationTime = insertedFloor.CreationTime
                        };
                }
            }
            catch (DataException ex)
            {
                result.Status = RespsonseStatus.Error;
                result.ErrorCode = ResponseError.DatabaseError;
                result.ErrorMessage = ex.StackTrace;
                ErrorSignal.FromCurrentContext().Raise(ex);
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
                ErrorSignal.FromCurrentContext().Raise(ex);
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

                    IQueryable<Floor> floors = from floor in entities.Floors
                                               where floor.MapId == mapId
                                               select new Floor
                                                   {
                                                       Id = floor.Id,
                                                       MapId = mapId,
                                                       Name = floor.Name,
                                                       ImageUrl = ServerAdminBasePath + floor.ImageUrl,
                                                       CreationTime = floor.CreationTime
                                                   };
                    result.List = floors.ToList();
                }
            }
            catch (DataException ex)
            {
                result.SetError(ResponseError.DatabaseError);
                result.ErrorMessage = ex.StackTrace;
                ErrorSignal.FromCurrentContext().Raise(ex);
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
                    Floors floor = entities.Floors.FirstOrDefault(x => x.Id == floorId);
                    if (floor != null)
                    {
                        result.Status = RespsonseStatus.Ok;
                        result.Object = new Floor
                            {
                                Id = floor.Id,
                                ImageUrl = ServerAdminBasePath + floor.ImageUrl,
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
                ErrorSignal.FromCurrentContext().Raise(ex);
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
                    Floors floor = entities.Floors.FirstOrDefault(f => f.Id == floorId);
                    if (floor != null)
                    {
                        result.Object = ServerAdminBasePath + floor.ImageUrl;
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
                ErrorSignal.FromCurrentContext().Raise(ex);
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
                    Floors floor = entities.Floors.FirstOrDefault(x => x.Id == floorId);
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
                ErrorSignal.FromCurrentContext().Raise(ex);
            }
            return result;
        }

        #endregion

        #region Layer: Graph

        [HttpPost]
        public ObjectResponse<Graph> SaveGraphForFloor(int floorId, Graph newGraph, Graph deletedGraph)
        {
            var result = new ObjectResponse<Graph>();
            try
            {
                using (var entities = new MapsEntities())
                {
                    IQueryable<Floors> foundFloors = from floor in entities.Floors
                                                     where floor.Id == floorId
                                                     select floor;
                    if (!foundFloors.Any())
                    {
                        result.SetError(ResponseError.FloorIdDoesNotExist);
                        return result;
                    }

                    Floors targetFloor = foundFloors.First();
                    Graphs graph = entities.Graphs.FirstOrDefault(x => x.MapId == targetFloor.MapId);
                    if (graph == null)
                    {
                        entities.Graphs.Add(new Graphs
                            {
                                MapId = targetFloor.MapId,
                                CreationTime = DateTime.Now
                            });
                    }


                    // Zu entfernende Knoten und Kanten löschen
                    if (deletedGraph.Nodes != null)
                    {
                        foreach (
                            var node in
                                deletedGraph.Nodes.Select(dNode => entities.Nodes.FirstOrDefault(x => x.Id == dNode.Id))
                                            .Where(node => node != null))
                        {
                            entities.Nodes.Remove(node);
                        }
                    }

                    if (deletedGraph.Edges != null)
                    {
                        foreach (
                            var edge in
                                deletedGraph.Edges.Select(
                                    dEdge =>
                                    entities.Edges.FirstOrDefault(
                                        x => x.NodeStartId == dEdge.StartNodeId && x.NodeEndId == dEdge.EndNodeId))
                                            .Where(edge => edge != null))
                        {
                            entities.Edges.Remove(edge);
                        }
                    }



                    var nodeIdMap = new Dictionary<int, int>();

                    // Nodes in den Floor hinzufügen
                    if (newGraph.Nodes != null)
                    {
                        foreach (Node node in newGraph.Nodes)
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
                    if (newGraph != null && newGraph.Edges != null)
                    {
                        foreach (Edge edge in newGraph.Edges)
                        {

                            int nodeIdMapStartNodeId;
                            if (nodeIdMap.ContainsKey(edge.StartNodeId))
                                nodeIdMapStartNodeId = nodeIdMap[edge.StartNodeId];
                            else
                            {
                                var startNodeId = entities.Nodes.FirstOrDefault(x => x.Id == edge.StartNodeId);
                                nodeIdMapStartNodeId = startNodeId == null ? 0 : startNodeId.Id;
                            }

                            int nodeIdMapEndNodeId;
                            if (nodeIdMap.ContainsKey(edge.EndNodeId))
                                nodeIdMapEndNodeId = nodeIdMap[edge.EndNodeId];
                            else
                            {
                                var endNodeId = entities.Nodes.FirstOrDefault(x => x.Id == edge.EndNodeId);
                                nodeIdMapEndNodeId = endNodeId == null ? 0 : endNodeId.Id;
                            }

                            graph.Edges.Add(new Edges
                            {
                                NodeStartId = nodeIdMapStartNodeId,
                                NodeEndId = nodeIdMapEndNodeId,
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
                ErrorSignal.FromCurrentContext().Raise(ex);
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
                    entities.DeleteGraphFromFloor(floorId);
                }
            }
            catch (DataException ex)
            {
                result.Status = RespsonseStatus.Error;
                result.ErrorCode = ResponseError.DatabaseError;
                result.ErrorMessage = ex.StackTrace;
                ErrorSignal.FromCurrentContext().Raise(ex);
            }

            return result;
        }

        [HttpGet]
        public ObjectResponse<Graph> GetGraphForFloor(int floorId)
        {
            var result = new ObjectResponse<Graph>();

            try
            {
                using (var entities = new MapsEntities())
                {
                    Floors queriedFloor = entities.Floors.Find(floorId);
                    if (queriedFloor == null)
                    {
                        result.SetError(ResponseError.FloorIdDoesNotExist);
                        return result;
                    }

                    ICollection<Nodes> nodes = queriedFloor.Nodes;
                    IEnumerable<int> nodeIds = nodes.Select(n => n.Id);
                    int[] enumerable = nodeIds as int[] ?? nodeIds.ToArray();
                    IQueryable<Edges> edges = from edge in entities.Edges
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
                ErrorSignal.FromCurrentContext().Raise(ex);
            }

            return result;
        }

        [HttpGet]
        public ObjectResponse<NodeInformation> GetNodeInformationForNode(int nodeId)
        {
            var result = new ObjectResponse<NodeInformation>();

            try
            {
                using (var entities = new MapsEntities())
                {
                    Nodes node = entities.Nodes.Find(nodeId);
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
                    Data.Entities.NodeInformation queriedNodeInformation =
                        entities.NodeInformation.FirstOrDefault(x => x.NodeId == nodeId);
                    if (queriedNodeInformation == null)
                        return result;

                    result.Object.DisplayName = queriedNodeInformation.DisplayName;
                    result.Object.RoomName = queriedNodeInformation.RoomName;
                    result.Object.NFCTag = queriedNodeInformation.NFCTag;
                    result.Object.QRCode = queriedNodeInformation.QRCode;

                    var poi = queriedNodeInformation.PoIs;
                    if (poi == null)
                        return result;

                    result.Object.PoI = new PoI
                        {
                            Description = poi.Description,
                            Type = new PoiType
                                {
                                    Id = poi.PoiTypes.Id,
                                    Name = poi.PoiTypes.Name
                                }
                        };
                }
            }
            catch (DataException ex)
            {
                result.SetError(ResponseError.DatabaseError);
                result.ErrorMessage = ex.StackTrace;
                ErrorSignal.FromCurrentContext().Raise(ex);
            }

            return result;
        }

        [HttpGet]
        public ObjectResponse<FloorPlanData> GetFloorPlanData(int floorId)
        {
            var floorPlanData = new ObjectResponse<FloorPlanData> { Object = new FloorPlanData() };
            ObjectResponse<Graph> result = GetGraphForFloor(floorId);
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
                    // Schon vorhandene Nodeinformationen suchen
                    Data.Entities.NodeInformation nodeInformation =
                        entities.NodeInformation.FirstOrDefault(x => x.NodeId == nodeId);
                    bool poiTypeSelected = nodeInf.PoI.Type.Id != 0;
                    PoIs poi = null;

                    if (poiTypeSelected)
                    {
                        PoiTypes poiType = entities.PoiTypes.FirstOrDefault(x => x.Id == nodeInf.PoI.Type.Id);
                        if (poiType == null)
                        {
                            result.SetError(ResponseError.PoiTypeIdDoesNotExist);
                            return result;
                        }
                        nodeInf.PoI.Type.Name = poiType.Name;

                        // Falls keine PoIs in der Relation PoIs existieren, neuen PoIs erstellen
                        if (nodeInformation == null || nodeInformation.PoIs == null)
                        {
                            poi = entities.PoIs.Add(new PoIs
                            {
                                TypeId = nodeInf.PoI.Type.Id,
                                Description = nodeInf.PoI.Description
                            });
                        }
                        // Ansonsten vorhandenen PoI updaten
                        else
                        {
                            poi = entities.PoIs.FirstOrDefault(x => x.Id == nodeInformation.PoiId);
                            if (poi == null)
                            {
                                result.SetError(ResponseError.PoiDoesNotExist);
                                return result;
                            }
                            poi.TypeId = nodeInf.PoI.Type.Id;
                            poi.Description = nodeInf.PoI.Description;
                        }
                        entities.SaveChanges();
                    }

                    // Wenn es keine Nodeinformations gibt neue anlegen
                    if (nodeInformation == null)
                    {
                        nodeInformation = entities.NodeInformation.Add(new Data.Entities.NodeInformation
                            {
                                DisplayName = nodeInf.DisplayName,
                                RoomName = nodeInf.RoomName,
                                QRCode = nodeInf.QRCode,
                                NFCTag = nodeInf.NFCTag,

                                PoiId = poiTypeSelected ? (int?)poi.Id : null,
                                // Nur beim erstellen ausführen, NodeId bleibt gleich
                                NodeId = nodeId,
                                CreationTime = DateTime.Now
                            });
                        entities.SaveChanges();
                    }
                    else
                    {
                        // Aktualisieren
                        nodeInformation.DisplayName = nodeInf.DisplayName;
                        nodeInformation.RoomName = nodeInf.RoomName;
                        nodeInformation.NFCTag = nodeInf.NFCTag;
                        nodeInformation.QRCode = nodeInf.QRCode;

                        nodeInformation.PoiId = poiTypeSelected ? (int?)poi.Id : null;
                        entities.SaveChanges();
                    }

                    PoI returnPoi = new PoI();
                    if (poi != null)
                    {
                        returnPoi = new PoI
                                    {
                                        Description = poi.Description,
                                        Type = new PoiType(poi.PoiTypes.Id, poi.PoiTypes.Name)
                                    };
                    }

                    var node = entities.Nodes.First(n => n.Id == nodeId);
                    // Ergebnis aus der DB in Rückgabe Objekt schreiben
                    result.Object = new NodeInformation
                        {
                            DisplayName = nodeInformation.DisplayName,
                            PoI = returnPoi,
                            RoomName = nodeInformation.RoomName,
                            NFCTag = nodeInformation.NFCTag,
                            QRCode = nodeInformation.QRCode,
                            Node = new Node
                                {
                                    FloorId = node.FloorId,
                                    Id = node.Id,
                                    X = node.X,
                                    Y = node.Y
                                }
                        };
                }
            }
            catch (DataException ex)
            {
                result.SetError(ResponseError.DatabaseError);
                result.ErrorMessage = ex.StackTrace;
                ErrorSignal.FromCurrentContext().Raise(ex);
            }
            return result;
        }

        public ListResponse<Node> GetConnectedNodes(int nodeId)
        {
            var result = new ListResponse<Node>();

            try
            {
                using (var entities = new MapsEntities())
                {
                    bool nodeExists = entities.Nodes.Any(n => n.Id == nodeId);
                    if (!nodeExists)
                    {
                        result.SetError(ResponseError.NodeIdDoesNotExist);
                        return result;
                    }

                    var edges = from edge in entities.Edges
                                where edge.NodeStartId == nodeId || edge.NodeEndId == nodeId
                                select edge;

                    HashSet<int> connectedNodeIds = new HashSet<int>();
                    foreach (var edge in edges)
                    {
                        connectedNodeIds.Add(edge.NodeStartId);
                        connectedNodeIds.Add(edge.NodeEndId);
                    }
                    connectedNodeIds.Remove(nodeId);

                    var connectedNodes = from node in entities.Nodes
                                         where connectedNodeIds.Contains(node.Id)
                                         select node;

                    foreach (var node in connectedNodes)
                        result.List.Add(NodeFromDb(node));
                }
            }
            catch (DataException ex)
            {
                result.SetError(ResponseError.DatabaseError);
                result.ErrorMessage = ex.StackTrace;
                ErrorSignal.FromCurrentContext().Raise(ex);
            }

            return result;
        }

        public ListResponse<NodeInformation> GetNodeInformation(int mapId, int floorId)
        {
            var result = new ListResponse<NodeInformation>();
            try
            {
                using (var entities = new MapsEntities())
                {
                    var nodes = entities.NodeInformation.ToList();
                    if (mapId > 0)
                    {
                        nodes = nodes.Where(x => x.Nodes.Floors.MapId == mapId).ToList();
                    }
                    if (floorId > 0)
                    {
                        nodes = nodes.Where(x => x.Nodes.FloorId == floorId).ToList();
                    }
                    foreach (var node in nodes)
                        result.List.Add(NodeInformationFromDb(node));
                }
            }
            catch (DataException e)
            {
                result.SetError(ResponseError.DatabaseError);
                ErrorSignal.FromCurrentContext().Raise(e);
            }
            return result;
        }
        #endregion

        #region Layer: Navigation

        [HttpGet]
        public ListResponse<Node> GetRouteBetween(int mapId, int startNodeId, int endNodeId)
        {
            var result = new ListResponse<Node>();

            try
            {
                using (var entities = new MapsEntities())
                {
                    var routeNodes = new List<Node>();

                    IQueryable<Nodes> nodes = from node in entities.Nodes
                                              where node.Floors.MapId == mapId
                                              select node;

                    Dictionary<int, Nodes> nodesMap = nodes.ToDictionary(node => node.Id);

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

                    List<Edges> edges = (from edge in entities.Edges
                                         where edge.Graphs.MapId == mapId
                                         select edge).ToList();

                    var graph = new UndirectedGraph<int, Edge<int>>();

                    foreach (int nodeId in nodesMap.Keys)
                    {
                        graph.AddVertex(nodeId);
                    }

                    foreach (Edges edge in edges)
                    {
                        graph.AddEdge(new Edge<int>(edge.NodeStartId, edge.NodeEndId));
                    }

                    Func<Edge<int>, double> edgeCost = e => GetEdgeCost(nodesMap, e);

                    TryFunc<int, IEnumerable<Edge<int>>> tryGetPath = graph.ShortestPathsDijkstra(edgeCost, startNodeId);

                    IEnumerable<Edge<int>> routeEdges;
                    if (tryGetPath(endNodeId, out routeEdges))
                    {
                        // Eine Route existiert und beginnt damit mit dem Startknoten
                        routeNodes.Add(NodeFromDb(nodesMap[startNodeId]));

                        int lastNodeId = startNodeId;
                        foreach (var routeEdge in routeEdges)
                        {
                            // Es handelt sich um eine ungerichtete Kante, d.h. es muss
                            // geprüft werden, welcher Endknoten der Kante der zuletzt
                            // besuchte Knoten war
                            int nextNodeId = routeEdge.GetOtherVertex(lastNodeId);
                            Nodes dbNode = nodesMap[nextNodeId];
                            routeNodes.Add(NodeFromDb(dbNode));
                            lastNodeId = nextNodeId;
                        }

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
                ErrorSignal.FromCurrentContext().Raise(ex);
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

        private NodeInformation NodeInformationFromDb(Data.Entities.NodeInformation dbNodeInformation)
        {
            return new NodeInformation
                {
                    DisplayName = dbNodeInformation.DisplayName,
                    Node = NodeFromDb(dbNodeInformation.Nodes),
                    RoomName = dbNodeInformation.RoomName
                };
        }

        private double GetEdgeCost(Dictionary<int, Nodes> nodesMap, Edge<int> e)
        {
            Nodes startNode = nodesMap[e.Source];
            Nodes endNode = nodesMap[e.Target];

            decimal diffX = endNode.X - startNode.X;
            decimal diffY = endNode.Y - startNode.Y;
            // Unterschiede im Stockwerk beachten
            // Da Höhe unbekannt einfach irgendwelche Kosten festlegen
            decimal diffZ = endNode.FloorId != startNode.FloorId ? 0.2m : 0m;

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
                                          select new PoiType
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

        [HttpGet]
        public ListResponse<RoomAndPoI> GetPoIsForMap(int mapId)
        {
            var result = new ListResponse<RoomAndPoI>();
            try
            {
                using (var entities = new MapsEntities())
                {
                    result.List = entities.PoisForMap.Where(x => x.MapId == mapId).Select(x => new RoomAndPoI
                        {
                            Room = new Room
                            {
                                NodeId = x.NodeId,
                                RoomName = x.RoomName,
                                DisplayName = x.DisplayName
                            },
                            PoI = new PoI
                            {
                                Type = new PoiType
                                {
                                    Id = x.PoiTypeId,
                                    Name = x.PoiTypeName,
                                },
                                Description = x.PoiDescription
                            }
                        }).ToList();
                }
            }
            catch (DataException)
            {
                result.SetError(ResponseError.DatabaseError);
            }
            return result;
        }

        [HttpGet]
        public ListResponse<Room> GetRoomsForMap(int mapId)
        {
            var result = new ListResponse<Room>();
            try
            {
                using (var entities = new MapsEntities())
                {
                    var map = entities.Maps.FirstOrDefault(x => x.Id == mapId);
                    if (map == null)
                    {
                        result.SetError(ResponseError.MapIdDoesNotExist);
                        return result;
                    }

                    result.List = entities.NodeInformationForMap.Where(x => x.MapId == map.Id).Select(x => new Room
                        {
                            RoomName = x.RoomName,
                            DisplayName = x.DisplayName,
                            NodeId = x.NodeId

                        }).ToList();
                }
            }
            catch (DataException ex)
            {
                result.SetError(ResponseError.DatabaseError);
                ErrorSignal.FromCurrentContext().Raise(ex);
            }
            return result;
        }

        [HttpGet]
        public ObjectResponse<Node> GetNodeForNFC(int mapId, string nfcTag)
        {
            var result = new ObjectResponse<Node>();
            try
            {
                using (var entities = new MapsEntities())
                {
                    var nodeInformation =
                        entities.NodeInformation.FirstOrDefault(x => x.NFCTag == nfcTag);
                    if (nodeInformation == null)
                    {
                        result.SetError(ResponseError.NFCTagDoesNotExist);
                        return result;
                    }

                    var node = nodeInformation.Nodes;

                    result.Object = new Node
                        {
                            Id = node.Id,
                            FloorId = node.FloorId,
                            X = node.X,
                            Y = node.Y
                        };
                }
            }
            catch (DataException ex)
            {
                result.SetError(ResponseError.DatabaseError);
                ErrorSignal.FromCurrentContext().Raise(ex);
            }
            return result;
        }

        public ObjectResponse<Node> GetNodeForQRCode(int mapId, string qrCode)
        {
            var result = new ObjectResponse<Node>();
            try
            {
                using (var entities = new MapsEntities())
                {
                    Data.Entities.NodeInformation nodeInformation =
                        entities.NodeInformation.FirstOrDefault(x => x.QRCode == qrCode);
                    if (nodeInformation == null)
                    {
                        result.SetError(ResponseError.QRCodeDosNotExist);
                        return result;
                    }

                    Nodes node = nodeInformation.Nodes;

                    result.Object = new Node
                        {
                            Id = node.Id,
                            FloorId = node.FloorId,
                            X = node.X,
                            Y = node.Y
                        };
                }
            }
            catch (DataException ex)
            {
                result.SetError(ResponseError.DatabaseError);
                ErrorSignal.FromCurrentContext().Raise(ex);
            }
            return result;
        }

        #endregion // Layer: Information
    }
}