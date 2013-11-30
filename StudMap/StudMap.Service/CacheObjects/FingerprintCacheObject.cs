using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using MathNet.Numerics.Distributions;
using StudMap.Data.Entities;
using StudMap.Service.Services;

namespace StudMap.Service.CacheObjects
{
    public class FingerprintCacheObject : CacheObject
    {
        public int MapID { get; set; }

        public Dictionary<int, Dictionary<int, Normal>> NodeDistributions { get; set; }

        public Dictionary<string, int> MACtoAP { get; set; }

        private const int TIMEOUT = 60;

        public FingerprintCacheObject(int mapId)
        {
            TimeoutInMinutes = TIMEOUT;
            MapID = mapId;

            using (var entities = new MapsEntities())
            {
                Update(entities);
            }
        }

        public void Update(MapsEntities entities)
        {
            MACtoAP = FingerprintService.CreateMACtoAPLookup(entities);
            NodeDistributions = FingerprintService.ComputeNodeDistribution(entities);
        }
    }
}