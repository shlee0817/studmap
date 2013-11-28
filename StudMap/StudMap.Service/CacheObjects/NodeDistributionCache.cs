using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using MathNet.Numerics.Distributions;
using StudMap.Data.Entities;

namespace StudMap.Service.CacheObjects
{
    public class NodeDistributionCache : CacheObject
    {
        private const int TIMEOUT = 24 * 60;

        public Dictionary<int, Dictionary<int, Normal>> NodeDistributions { get; set; }

        public NodeDistributionCache()
        {
            TimeoutInMinutes = TIMEOUT;
            using (var entities = new MapsEntities())
            {
                Update(entities);
            }
        }

        public void Update(MapsEntities entities)
        {
            var nodeDistributions = new Dictionary<int, Dictionary<int, Normal>>();
            foreach (var dist in entities.RSSDistribution)
            {
                if (!nodeDistributions.ContainsKey(dist.NodeId))
                    nodeDistributions.Add(dist.NodeId, new Dictionary<int, Normal>());

                nodeDistributions[dist.NodeId].Add(dist.AccessPointId,
                    Normal.WithMeanStdDev(dist.AvgRSS ?? 0, dist.StDevRSS ?? 0.0));
            }
            NodeDistributions = nodeDistributions;
        }
    }
}