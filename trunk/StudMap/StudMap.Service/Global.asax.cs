using System.Web;
using System.Web.Http;
using System.Web.Mvc;
using System.Web.Optimization;
using System.Web.Routing;
using StudMap.Service.App_Start;
using System.Web.Caching;
using System;
using StudMap.Service.Controllers;

namespace StudMap.Service
{
    // Note: For instructions on enabling IIS6 or IIS7 classic mode, 
    // visit http://go.microsoft.com/?LinkId=9394801

    public class WebApiApplication : HttpApplication
    {
        protected void Application_Start()
        {
            AreaRegistration.RegisterAllAreas();

            WebApiConfig.Register(GlobalConfiguration.Configuration);
            FilterConfig.RegisterGlobalFilters(GlobalFilters.Filters);
            RouteConfig.RegisterRoutes(RouteTable.Routes);
            BundleConfig.RegisterBundles(BundleTable.Bundles);

            AddTask("CheckActiveUsers", 5 * 60);
        }

        private static CacheItemRemovedCallback OnCacheRemove = null;

        private void AddTask(string name, int seconds)
        {
            OnCacheRemove = new CacheItemRemovedCallback(CacheItemRemoved);
            HttpRuntime.Cache.Insert(name, seconds, null,
                DateTime.Now.AddSeconds(seconds), Cache.NoSlidingExpiration,
                CacheItemPriority.NotRemovable, OnCacheRemove);
        }

        public void CacheItemRemoved(string key, object value, CacheItemRemovedReason reason)
        {
            UsersController.CheckActiveUsers();

            // Neuen Task anlegen, damit dieser im nächsten Intervall wieder ausgeführt wird
            AddTask(key, Convert.ToInt32(value));
        }
    }
}