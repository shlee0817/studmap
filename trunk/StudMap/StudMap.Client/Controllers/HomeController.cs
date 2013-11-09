using System.Web.Mvc;
using StudMap.Service.Controllers;

namespace StudMap.Client.Controllers
{
    public class HomeController : Controller
    {
        public ActionResult Index()
        {
            return View();
        }

        public ActionResult FloorPlan(int mapId, int floorId)
        {
            var mapsCtrl = new MapsController();
            var result = mapsCtrl.GetFloor(floorId);
            return View(result);
        }
    }
}
