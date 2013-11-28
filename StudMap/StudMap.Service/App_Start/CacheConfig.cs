using MathNet.Numerics.Distributions;
using StudMap.Data.Entities;
using StudMap.Service.CacheObjects;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Caching;

namespace StudMap.Service.App_Start
{
    public class CacheConfig
    {
        public const string FINGERPRINT_CACHE = "Fingerprint";
        public const string MAP_CACHE = "Map:";

        /// <summary>
        /// Hier werden komplexe Strukturen aus der DB generiert und gecachet.
        /// </summary>
        public static void RegisterCacheObjects()
        {
            RegisterNodeDistribution();     
        }

        public static FingerprintCache RegisterNodeDistribution() 
        {
            var cacheObject = RegisterObject(FINGERPRINT_CACHE, 
                () => new FingerprintCache());

            return (FingerprintCache)cacheObject;
        }

        public static void RemoveNodeDistribution()
        {
            HttpRuntime.Cache.Remove(FINGERPRINT_CACHE);
        }

        public static MapCache RegisterMapCache(int mapId)
        {
            var cacheObject = RegisterObject(MAP_CACHE + mapId,
                () => new MapCache(mapId));

            return (MapCache)cacheObject;
        }

        public static void RemoveMapCache(int mapId)
        {
            HttpRuntime.Cache.Remove(MAP_CACHE + mapId);
        }

        private static object RegisterObject(string key, Func<CacheObject> createObject)
        {
            var cacheObject = createObject();
            HttpRuntime.Cache.Insert(key, cacheObject, null,
                DateTime.Now.AddMinutes(cacheObject.TimeoutInMinutes),
                Cache.NoSlidingExpiration, CacheItemPriority.High,
                (k, v, r) =>
                {
                    RegisterObject(key, createObject);
                });
            return cacheObject;
        }
    }
}