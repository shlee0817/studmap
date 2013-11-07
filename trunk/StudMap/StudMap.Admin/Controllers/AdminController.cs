using System.Web;
using System.Web.Mvc;
using StudMap.Admin.Models;
using StudMap.Core;
using StudMap.Core.Maps;
using StudMap.Service.Controllers;
using Graph = StudMap.Core.Graph.Graph;

namespace StudMap.Admin.Controllers
{
    public class AdminController : Controller
    {
        private readonly string _serverUploadFolder = System.Web.HttpContext.Current.Server.MapPath("~/Images");

        [Authorize(Roles = "Admins")]
        public ActionResult Index()
        {
            return View();
        }

        [Authorize(Roles = "Admins")]
        public ActionResult CreateMap()
        {
            return View();
        }

        [Authorize(Roles = "Admins")]
        [ValidateAntiForgeryToken]
        [HttpPost]
        public ActionResult CreateMap(Map data)
        {
            var mapsCtrl = new MapsController();
            var response = mapsCtrl.CreateMap(data.Name);
            return response.Status == RespsonseStatus.Error ? View("Error") : View("Index");
        }

        [Authorize(Roles = "Admins")]
        public ActionResult CreateFloor(int mapId)
        {
            ViewBag.MapId = mapId;
            return View();
        }

        [Authorize(Roles = "Admins")]
        [ValidateAntiForgeryToken]
        [HttpPost]
        public ActionResult CreateFloor(int mapId, Floor floor, HttpPostedFileBase data)
        {
            var mapsCtrl = new MapsController();
            var response = mapsCtrl.CreateFloor(floor.MapId, floor.Name);

            if (response.Status == RespsonseStatus.Ok && data != null)
            {
                var filename = _serverUploadFolder + "\\Floors\\" + data.FileName;
                data.SaveAs(filename);

                mapsCtrl.UploadFloorImage(response.Object.Id, "Images/Floors/" + data.FileName);
            }
            var floors = mapsCtrl.GetFloorsForMap(mapId);
            ViewBag.MapId = mapId;
            return response.Status == RespsonseStatus.Error ? View("Error") : View("Index");
        }

        #region Partial Views

        [Authorize(Roles = "Admins")]
        [HttpGet]
        public ActionResult GetFloor(int id)
        {
            var mapsCtrl = new MapsController();
            var floor = mapsCtrl.GetFloor(id);

            return PartialView("_Layer", floor);
        }

        [Authorize(Roles = "Admins")]
        [HttpGet]
        public ActionResult GetFloorsForMap(int id)
        {
            var mapsCtrl = new MapsController();
            var floors = mapsCtrl.GetFloorsForMap(id);

            ViewBag.MapId = id;
            return PartialView("_Floors", floors);
        }

        [Authorize(Roles = "Admins")]
        [HttpGet]
        public ActionResult GetMaps()
        {
            var mapsCtrl = new MapsController();
            var maps = mapsCtrl.GetMaps();

            return PartialView("_Maps", maps);
        }
        #endregion

        [Authorize(Roles = "Admins")]
        public JsonResult GetFloorplanImage(int mapId, int floorId)
        {
            var mapsCtrl = new MapsController();
            var floor = mapsCtrl.GetFloorplanImage(floorId);
            return Json(floor, JsonRequestBehavior.AllowGet);
        }

        [Authorize(Roles = "Admins")]
        [HttpPost]
        public JsonResult SaveGraphForMap(int floorId, Graph graph)
        {
            var mapsCtrl = new MapsController();
            var floor = mapsCtrl.SaveGraphForFloor(floorId, graph);
            return Json(floor, JsonRequestBehavior.AllowGet);
        }

        public JsonResult GetMapData(int id)
        {
            var floorPlanData = new FloorPlanData();
            var mapsCtrl = new MapsController();
            
            var result = mapsCtrl.GetGraphForFloor(id);
            if (result.Status == RespsonseStatus.Ok)
            {
                floorPlanData.Graph = result.Object;
            }

            return Json(floorPlanData, JsonRequestBehavior.AllowGet);
        }
    }
}
