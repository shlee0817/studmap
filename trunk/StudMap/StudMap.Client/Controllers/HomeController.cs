using System.Web.Mvc;
using StudMap.Core;
using StudMap.Core.Maps;
using StudMap.Service.Controllers;

namespace StudMap.Client.Controllers
{
    public class HomeController : Controller
    {
       public ActionResult FloorPlan(int mapId, int floorId)
        {
            var mapsCtrl = new MapsController();
            ObjectResponse<Floor> result = mapsCtrl.GetFloor(floorId);
            return View(result);
        }

        public ActionResult Impressum()
        {
            return View();
        }
    }
}