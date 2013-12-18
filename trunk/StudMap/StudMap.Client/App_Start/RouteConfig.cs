using System.Web.Mvc;
using System.Web.Routing;

namespace StudMap.Client.App_Start
{
    public class RouteConfig
    {
        public static void RegisterRoutes(RouteCollection routes)
        {
            routes.IgnoreRoute("{resource}.axd/{*pathInfo}");

            routes.MapRoute("FloorPlan", "{controller}/{action}/{mapId}/{floorId}",
                            new
                                {
                                    controller = "Home",
                                    action = "FloorPlan",
                                    mapId = UrlParameter.Optional,
                                    floorId = UrlParameter.Optional
                                }
                );
        }
    }
}