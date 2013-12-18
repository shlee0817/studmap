using System;
using System.Collections.Generic;
using QuickGraph;
using QuickGraph.Algorithms.ShortestPath;
using StudMap.Core;
using StudMap.Core.Graph;
using StudMap.Service.CacheObjects;

namespace StudMap.Service.Services
{
    public class NavigationService
    {
        public static List<Node> GetRouteBetweenCached(int mapId, int startNodeId, int endNodeId)
        {
            if (!MapService.MapExists(mapId))
                throw new ServiceException(ResponseError.MapIdDoesNotExist);

            MapCacheObject cache = StudMapCache.Map(mapId);
            Dictionary<int, Node> nodesMap = cache.Nodes;
            if (!nodesMap.ContainsKey(startNodeId))
                throw new ServiceException(ResponseError.StartNodeNotFound);
            if (!nodesMap.ContainsKey(endNodeId))
                throw new ServiceException(ResponseError.EndNodeNotFound);

            var routeNodes = new List<Node>();
            IEnumerable<UndirectedEdge<int>> routeEdges;
            if (cache.PathFinder.TryGetPath(startNodeId, endNodeId, out routeEdges))
            {
                // Eine Route existiert und beginnt damit mit dem Startknoten
                routeNodes.Add(nodesMap[startNodeId]);

                int lastNodeId = startNodeId;
                foreach (var routeEdge in routeEdges)
                {
                    // Es handelt sich um eine ungerichtete Kante, d.h. es muss
                    // geprüft werden, welcher Endknoten der Kante der zuletzt
                    // besuchte Knoten war
                    int nextNodeId = routeEdge.GetOtherVertex(lastNodeId);
                    routeNodes.Add(nodesMap[nextNodeId]);
                    lastNodeId = nextNodeId;
                }

                return routeNodes;
            }
            throw new ServiceException(ResponseError.NoRouteFound);
        }

        public static FloydWarshallAllShortestPathAlgorithm<int, UndirectedEdge<int>>
            ComputeShortestPaths(Dictionary<int, Node> nodes, IEnumerable<Edge> edges)
        {
            var graph = new AdjacencyGraph<int, UndirectedEdge<int>>();
            foreach (int nodeId in nodes.Keys)
            {
                graph.AddVertex(nodeId);
            }
            foreach (Edge edge in edges)
            {
                graph.AddEdge(new UndirectedEdge<int>(edge.StartNodeId, edge.EndNodeId));
                graph.AddEdge(new UndirectedEdge<int>(edge.EndNodeId, edge.StartNodeId));
            }

            Func<UndirectedEdge<int>, double> edgeCost = e => GetEdgeCost(nodes, e);
            var pathFinder = new FloydWarshallAllShortestPathAlgorithm<int, UndirectedEdge<int>>(graph, edgeCost);
            pathFinder.Compute();
            return pathFinder;
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

            return Math.Sqrt((double) (diffX*diffX + diffY*diffY + diffZ*diffZ));
        }
    }
}