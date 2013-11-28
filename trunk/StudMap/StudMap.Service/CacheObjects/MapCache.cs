using QuickGraph;
using QuickGraph.Algorithms;
using QuickGraph.Algorithms.ShortestPath;
using StudMap.Core.Graph;
using StudMap.Data.Entities;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace StudMap.Service.CacheObjects
{
    public class MapCache : CacheObject
    {
        private const int TIMEOUT = 60;

        public MapCache(int mapId)
        {
            TimeoutInMinutes = TIMEOUT;
            ID = mapId;

            using (var entities = new MapsEntities())
            {
                Update(entities);
            }
        }

        private void Update(MapsEntities entities)
        {
            Nodes = CreateNodeDictionary(entities, ID);
            Edges = CreateEdgeListe(entities, ID);
            FindShortestPaths();
        }

        public int ID { get; set; }

        public Dictionary<int, Node> Nodes { get; set; }

        public List<Edge> Edges { get; set; }

        public FloydWarshallAllShortestPathAlgorithm<int, UndirectedEdge<int>> PathFinder { get; set; }

        private Dictionary<int, Node> CreateNodeDictionary(MapsEntities entities, int mapId)
        {
            var nodes = from node in entities.Nodes
                        where node.Floors.MapId == mapId
                        select new Node
                        {
                            Id = node.Id,
                            X = node.X,
                            Y = node.Y,
                            FloorId = node.FloorId
                        };

            return nodes.ToDictionary(n => n.Id);
        }

        private static List<Edge> CreateEdgeListe(MapsEntities entities, int mapId)
        {
            var edges = from edge in entities.Edges
                        where edge.Graphs.MapId == mapId
                        select new Edge
                        {
                            StartNodeId = edge.NodeStartId,
                            EndNodeId = edge.NodeEndId
                        };

            return edges.ToList();
        }

        private void FindShortestPaths()
        {
            // Ungerichteten Graphen anlegen

            var graph = new AdjacencyGraph<int, UndirectedEdge<int>>();
            foreach (int nodeId in Nodes.Keys)
            {
                graph.AddVertex(nodeId);
            }
            foreach (Edge edge in Edges)
            {
                graph.AddEdge(new UndirectedEdge<int>(edge.StartNodeId, edge.EndNodeId));
                graph.AddEdge(new UndirectedEdge<int>(edge.EndNodeId, edge.StartNodeId));
            }

            Func<UndirectedEdge<int>, double> edgeCost = e => GetEdgeCost(Nodes, e);

            PathFinder = new FloydWarshallAllShortestPathAlgorithm<int, UndirectedEdge<int>>(graph, edgeCost);
            PathFinder.Compute();
        }

        private static double GetEdgeCost(Dictionary<int, Node> nodesMap, UndirectedEdge<int> e)
        {
            Node startNode = nodesMap[e.Source];
            Node endNode = nodesMap[e.Target];

            decimal diffX = endNode.X - startNode.X;
            decimal diffY = endNode.Y - startNode.Y;
            // Unterschiede im Stockwerk beachten
            // Da Höhe unbekannt einfach irgendwelche Kosten festlegen
            decimal diffZ = endNode.FloorId != startNode.FloorId ? 0.2m : 0m;

            return Math.Sqrt((double)(diffX * diffX + diffY * diffY + diffZ * diffZ));
        }
    }
}