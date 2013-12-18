using System;
using System.Web;
using System.Web.Caching;

namespace StudMap.Service.CacheObjects
{
    public class StudMapCache
    {
        /// <summary>
        ///     Globale Daten werden unter diesem Schlüssel gecachet.
        /// </summary>
        private const string GlobalCacheKey = "Global";

        /// <summary>
        ///     Für jede Map wird ein Cache-Eintrag einglegt, dessen
        ///     Zugriffsschlüssel dem hier definierten Format entspricht.
        ///     {0} wird mit der entsprechenden Map-ID ersetzt.
        /// </summary>
        private const string MapCacheKey = "Map:{0}";

        /// <summary>
        ///     Für jede Map wird ein Cache-Eintrag einglegt, dessen
        ///     Zugriffsschlüssel dem hier definierten Format entspricht.
        ///     {0} wird mit der entsprechenden Map-ID ersetzt.
        /// </summary>
        private const string FingerprintCacheKey = "Fingerprint:{0}";

        public static GlobalCacheObject Global
        {
            get { return GetGlobalCache(); }
        }

        public static MapCacheObject Map(int mapId)
        {
            return GetMapCache(mapId);
        }

        public static FingerprintCacheObject Fingerprint(int mapId)
        {
            return GetFingerprintCache(mapId);
        }

        public static void RemoveGlobal()
        {
            RemoveObject(GlobalCacheKey);
        }

        public static void RemoveMap(int mapId)
        {
            RemoveObject(String.Format(MapCacheKey, mapId));
        }

        public static void RemoveFingerprint(int mapId)
        {
            RemoveObject(String.Format(FingerprintCacheKey, mapId));
        }

        private static GlobalCacheObject GetGlobalCache()
        {
            GlobalCacheObject cacheObject = (GlobalCacheObject) HttpRuntime.Cache.Get(GlobalCacheKey) ??
                                            (GlobalCacheObject)
                                            RegisterObject(GlobalCacheKey, () => new GlobalCacheObject());

            return cacheObject;
        }

        private static MapCacheObject GetMapCache(int mapId)
        {
            string cacheKey = String.Format(MapCacheKey, mapId);
            MapCacheObject cacheObject = (MapCacheObject) HttpRuntime.Cache.Get(cacheKey) ??
                                         (MapCacheObject) RegisterObject(cacheKey, () => new MapCacheObject(mapId));

            return cacheObject;
        }

        private static FingerprintCacheObject GetFingerprintCache(int mapId)
        {
            string cacheKey = String.Format(FingerprintCacheKey, mapId);
            FingerprintCacheObject cacheObject = (FingerprintCacheObject) HttpRuntime.Cache.Get(cacheKey) ??
                                                 (FingerprintCacheObject)
                                                 RegisterObject(cacheKey, () => new FingerprintCacheObject(mapId));

            return cacheObject;
        }

        private static object RegisterObject(string key, Func<CacheObject> createObject)
        {
            CacheObject cacheObject = createObject();
            HttpRuntime.Cache.Insert(key, cacheObject, null,
                                     DateTime.Now.AddMinutes(cacheObject.TimeoutInMinutes),
                                     Cache.NoSlidingExpiration);
            return cacheObject;
        }

        private static void RemoveObject(string key)
        {
            HttpRuntime.Cache.Remove(key);
        }
    }
}