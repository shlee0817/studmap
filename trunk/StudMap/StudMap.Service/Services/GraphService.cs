using StudMap.Core;
using StudMap.Core.Graph;
using StudMap.Core.Maps;
using StudMap.Data;
using StudMap.Data.Entities;
using StudMap.Service.CacheObjects;
using System;
using System.Collections.Generic;
using System.Linq;

namespace StudMap.Service.Services
{
    public class GraphService
    {
        public static Graph SaveGraphForFloor(MapsEntities entities, int floorId, Graph newGraph, Graph deletedGraph)
        {
            Floors targetFloor = entities.Floors.Find(floorId);
            if (targetFloor == null)
                throw new ServiceException(ResponseError.FloorIdDoesNotExist);

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
                foreach (Nodes node in
                    deletedGraph.Nodes.Select(dNode => entities.Nodes.FirstOrDefault(x => x.Id == dNode.Id))
                    .Where(node => node != null))
                {
                    entities.Nodes.Remove(node);
                }
            }

            if (deletedGraph.Edges != null)
            {
                foreach (
                    Edges edge in
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
            if (newGraph.Edges != null)
            {
                foreach (Edge edge in newGraph.Edges)
                {
                    int nodeIdMapStartNodeId;
                    if (nodeIdMap.ContainsKey(edge.StartNodeId))
                        nodeIdMapStartNodeId = nodeIdMap[edge.StartNodeId];
                    else
                    {
                        Nodes startNodeId = entities.Nodes.FirstOrDefault(x => x.Id == edge.StartNodeId);
                        nodeIdMapStartNodeId = startNodeId == null ? 0 : startNodeId.Id;
                    }

                    int nodeIdMapEndNodeId;
                    if (nodeIdMap.ContainsKey(edge.EndNodeId))
                        nodeIdMapEndNodeId = nodeIdMap[edge.EndNodeId];
                    else
                    {
                        Nodes endNodeId = entities.Nodes.FirstOrDefault(x => x.Id == edge.EndNodeId);
                        nodeIdMapEndNodeId = endNodeId == null ? 0 : endNodeId.Id;
                    }

                    if (graph != null)
                        graph.Edges.Add(new Edges
                        {
                            NodeStartId = nodeIdMapStartNodeId,
                            NodeEndId = nodeIdMapEndNodeId,
                            CreationTime = DateTime.Now
                        });
                }
            }

            entities.SaveChanges();

            StudMapCache.RemoveMap(targetFloor.MapId);
            
            return GetGraphForFloor(entities, floorId);
        }

        public static void DeleteGraphForFloor(MapsEntities entities, int floorId)
        {
            Floors floor = entities.Floors.Find(floorId);
            if (floor == null)
                throw new ServiceException(ResponseError.FloorIdDoesNotExist);
            int mapId = floor.MapId;

            entities.DeleteGraphFromFloor(floorId);

            StudMapCache.RemoveMap(mapId);
        }

        public static Graph GetGraphForFloor(MapsEntities entities, int floorId)
        {
            Floors queriedFloor = entities.Floors.Find(floorId);
            if (queriedFloor == null)
                throw new ServiceException(ResponseError.FloorIdDoesNotExist);

            // Array aus Node-IDs auf dem angeforderten Floor erstellen
            ICollection<Nodes> nodes = queriedFloor.Nodes;
            IEnumerable<int> nodeIds = nodes.Select(n => n.Id);
            int[] nodeIdArray = nodeIds as int[] ?? nodeIds.ToArray();

            // TODO: Hier wirklich auch Kanten zurückgeben, von denen nur
            //       ein Endknoten auf dem geforderten Floor ist?
            //       Erstmal nur Kanten, die komplett auf dem Floor sind zurückgeben
            IQueryable<Edges> edges = entities.Edges.Where(e =>
                nodeIdArray.Contains(e.NodeStartId) && nodeIdArray.Contains(e.NodeEndId));

            return Conversions.ToGraph(floorId, nodes, edges);
        }

        public static Graph GetGraphForFloorCached(int floorId)
        {
            Floor floor = StudMapCache.Global.Floors[floorId];
            if (floor == null)
                throw new ServiceException(ResponseError.FloorIdDoesNotExist);

            return StudMapCache.Map(floor.MapId).GraphsForFloor[floorId];
        }

        public static List<Node> GetConnectedNodes(MapsEntities entities, int nodeId)
        {
            bool nodeExists = entities.Nodes.Any(n => n.Id == nodeId);
            if (!nodeExists)
                throw new ServiceException(ResponseError.NodeIdDoesNotExist);

            // Node-IDs aller Endknoten der verbundenen Kanten sammeln
            IQueryable<Edges> edges = entities.Edges.Where(
                e => e.NodeStartId == nodeId || e.NodeEndId == nodeId);
            var connectedNodeIds = new HashSet<int>();
            foreach (Edges edge in edges)
            {
                connectedNodeIds.Add(edge.NodeStartId);
                connectedNodeIds.Add(edge.NodeEndId);
            }
            // Den angeforderten Knoten herausfiltern
            connectedNodeIds.Remove(nodeId);

            IQueryable<Nodes> connectedNodes = entities.Nodes.Where(
                n => connectedNodeIds.Contains(n.Id));

            return connectedNodes.ToList()
                .Select(Conversions.ToNode).ToList();
        }

        public static List<Edge> GetEdgeList(MapsEntities entities, int mapId)
        {
            return entities.Edges.Where(e => e.Graphs.MapId == mapId).ToList()
                .Select(Conversions.ToEdge).ToList();
        }

        public static Dictionary<int, Node> GetNodesDictionary(MapsEntities entities, int mapId)
        {
            return entities.Nodes.Where(n => n.Floors.MapId == mapId).ToList()
                .Select(Conversions.ToNode).ToDictionary(n => n.Id);
        }

        public static Dictionary<int, Graph> GetAllGraphs(MapsEntities entities, IEnumerable<int> floorIds)
        {
            return floorIds.ToDictionary(floorId => floorId, floorId => GetGraphForFloor(entities, floorId));
        }
    }
}