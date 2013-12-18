using System.Collections.Generic;
using MathNet.Numerics.Distributions;
using StudMap.Data.Entities;
using StudMap.Service.Services;

namespace StudMap.Service.CacheObjects
{
    public class FingerprintCacheObject : CacheObject
    {
        private const int Timeout = 60;

        public FingerprintCacheObject(int mapId)
        {
            TimeoutInMinutes = Timeout;
            MapId = mapId;

            using (var entities = new MapsEntities())
            {
                Update(entities);
            }
        }

        public int MapId { get; set; }

        public Dictionary<int, Dictionary<int, Normal>> NodeDistributions { get; set; }

        public Dictionary<string, int> MACtoAP { get; set; }

        public void Update(MapsEntities entities)
        {
            MACtoAP = FingerprintService.CreateMACtoAPLookup(entities);
            NodeDistributions = FingerprintService.ComputeNodeDistribution(entities);
        }
    }
}