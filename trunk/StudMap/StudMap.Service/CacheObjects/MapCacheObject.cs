using QuickGraph;
using QuickGraph.Algorithms;
using QuickGraph.Algorithms.ShortestPath;
using StudMap.Core.Graph;
using StudMap.Core.Maps;
using StudMap.Data.Entities;
using StudMap.Service.Services;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace StudMap.Service.CacheObjects
{
    public class MapCacheObject : CacheObject
    {
        public int ID { get; set; }

        public Dictionary<int, Node> Nodes { get; set; }

        public List<Edge> Edges { get; set; }

        public List<Floor> Floors { get; set; }

        public Dictionary<int, Graph> GraphsForFloor { get; set; }

        public FloydWarshallAllShortestPathAlgorithm<int, UndirectedEdge<int>> PathFinder { get; set; }

        private const int TIMEOUT = 60;

        public MapCacheObject(int mapId)
        {
            TimeoutInMinutes = TIMEOUT;
            ID = mapId;

            using (var entities = new MapsEntities())
            {
                Update(entities);
            }
        }

        public void UpdateGraph(MapsEntities entities)
        {
            Nodes = GraphService.GetNodesDictionary(entities, ID);
            Edges = GraphService.GetEdgeList(entities, ID);
            GraphsForFloor = GraphService.GetAllGraphs(entities, Floors.Select(f => f.Id));
            PathFinder = NavigationService.ComputeShortestPaths(Nodes, Edges); 
        }

        public void UpdateFloors(MapsEntities entities)
        {
            Floors = FloorService.GetFloorsForMap(entities, ID);
        }

        private void Update(MapsEntities entities)
        {
            UpdateFloors(entities);
            UpdateGraph(entities);
        }
    }
}