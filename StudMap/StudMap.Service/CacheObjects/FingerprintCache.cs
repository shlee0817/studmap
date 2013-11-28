using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using MathNet.Numerics.Distributions;
using StudMap.Data.Entities;

namespace StudMap.Service.CacheObjects
{
    public class FingerprintCache : CacheObject
    {
        private const int TIMEOUT = 24 * 60;

        public Dictionary<int, Dictionary<int, Normal>> NodeDistributions { get; set; }

        public Dictionary<string, int> MACtoAP { get; set; }

        public FingerprintCache()
        {
            TimeoutInMinutes = TIMEOUT;
            using (var entities = new MapsEntities())
            {
                Update(entities);
            }
        }

        public void Update(MapsEntities entities)
        {
            NodeDistributions = CalculateNodeDist(entities);
            MACtoAP = CreateMACtoAPLookup(entities);
        }

        private static Dictionary<int, Dictionary<int, Normal>> CalculateNodeDist(MapsEntities entities)
        {
            var nodeDistributions = new Dictionary<int, Dictionary<int, Normal>>();
            foreach (var dist in entities.RSSDistribution)
            {
                if (!nodeDistributions.ContainsKey(dist.NodeId))
                    nodeDistributions.Add(dist.NodeId, new Dictionary<int, Normal>());

                // Standardabweichung auf einen beliebigen Wert setzen, wenn 
                nodeDistributions[dist.NodeId].Add(dist.AccessPointId,
                    Normal.WithMeanStdDev(dist.AvgRSS ?? 0, dist.StDevRSS ?? 1.0));
            }
            return nodeDistributions;
        }

        private static Dictionary<string, int> CreateMACtoAPLookup(MapsEntities entities)
        {
            return entities.AccessPoints.ToDictionary(ap => ap.MAC, ap => ap.Id);
        }
    }
}