using System.Web;
using System.Web.Mvc;
using StudMap.Core;
using StudMap.Core.Graph;
using StudMap.Core.Information;
using StudMap.Core.Maps;
using StudMap.Service.Controllers;

namespace StudMap.Admin.Controllers
{
    public class AdminController : Controller
    {
        private readonly string _serverUploadFolder = System.Web.HttpContext.Current.Server.MapPath("~/Images");
        
        [Authorize(Roles = "Admins")]
        public ActionResult Index(string partialViewName, object viewModel)
        {
            if (string.IsNullOrWhiteSpace(partialViewName))
            {
                ViewBag.PartialViewName = "_Maps";
                var mapsCtrl = new MapsController();
                viewModel = mapsCtrl.GetMaps();
                
            }
            else
                ViewBag.PartialViewName = partialViewName;
            return View("Index", viewModel);
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
            var viewModel = mapsCtrl.GetMaps();
            return response.Status == RespsonseStatus.Error ? View("Error") : Index("_Maps", viewModel);
        }

        [Authorize(Roles = "Admins")]
        public ActionResult DeleteMap(int id)
        {
            var mapsCtrl = new MapsController();
            var response = mapsCtrl.DeleteMap(id);
            var maps = mapsCtrl.GetMaps();
            return response.Status == RespsonseStatus.Error ? View("Error") : View("_Maps", maps);
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
            return response.Status == RespsonseStatus.Error ? View("Error") : Index("_Floors", floors);
        }

        [Authorize(Roles = "Admins")]
        public ActionResult DeleteFloor(int mapId, int floorId)
        {
            var mapsCtrl = new MapsController();
            var response = mapsCtrl.DeleteFloor(floorId);
            var floors = mapsCtrl.GetFloorsForMap(mapId);
            return response.Status == RespsonseStatus.Error ? View("Error") : View("_Floors", floors);
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

        [Authorize(Roles = "Admins")]
        [HttpGet]
        public ActionResult GetNodeInformation(int nodeId, bool readOnly)
        {
            var mapsCtrl = new MapsController();
            var nodeInformation = mapsCtrl.GetNodeInformationForNode(nodeId);

            nodeInformation.Object.ReadOnly = readOnly;

            ViewBag.PoiTypes = mapsCtrl.GetPoiTypes().List;
            return PartialView("_NodeInformation", nodeInformation.Object);
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

        [Authorize(Roles = "Admins")]
        [HttpPost]
        public JsonResult SaveNodeInformation(int nodeId, string displayName, string roomName, int poiTypeId, string poiDescription)
        {
            var mapsCtrl = new MapsController();
            var nodeInf = new NodeInformation(displayName, roomName, new PoI { Description = poiDescription, Type = new PoiType() { Id = poiTypeId } });
            var tmp = mapsCtrl.SaveNodeInformation(nodeId, nodeInf);
            return Json(tmp, JsonRequestBehavior.AllowGet);
        }

        public JsonResult GetFloorPlanData(int id)
        {
            var mapsCtrl = new MapsController();
            
            var result = mapsCtrl.GetFloorPlanData(id);

            return Json(result, JsonRequestBehavior.AllowGet);
        }
    }
}
