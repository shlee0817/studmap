using System.Globalization;
using System.Web;
using System.Web.Mvc;
using StudMap.Core;
using StudMap.Core.Graph;
using StudMap.Core.Information;
using StudMap.Core.Maps;
using StudMap.Service.Controllers;
using System.Linq;

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
            ObjectResponse<Map> response = mapsCtrl.CreateMap(data.Name);
            return response.Status != RespsonseStatus.Error ? RedirectToAction("Index") : RedirectToAction("Error");
        }

        [Authorize(Roles = "Admins")]
        public ActionResult DeleteMap(int id)
        {
            var mapsCtrl = new MapsController();
            BaseResponse response = mapsCtrl.DeleteMap(id);
            ListResponse<Map> maps = mapsCtrl.GetMaps();
            return response.Status == RespsonseStatus.Error ? (ActionResult)RedirectToAction("Error") : View("_Maps", maps);
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
            ObjectResponse<Floor> response = mapsCtrl.CreateFloor(floor.MapId, floor.Name);

            if (response.Status == RespsonseStatus.Ok && data != null)
            {
                string filename = _serverUploadFolder + "\\Floors\\" + data.FileName;
                data.SaveAs(filename);

                mapsCtrl.UploadFloorImage(response.Object.Id, "Images/Floors/" + data.FileName);
            }
            ViewBag.MapId = mapId;
            return response.Status != RespsonseStatus.Error ? RedirectToAction("Index") : RedirectToAction("Error");
        }

        [Authorize(Roles = "Admins")]
        public ActionResult DeleteFloor(int mapId, int floorId)
        {
            var mapsCtrl = new MapsController();
            BaseResponse response = mapsCtrl.DeleteFloor(floorId);
            ListResponse<Floor> floors = mapsCtrl.GetFloorsForMap(mapId);
            return response.Status == RespsonseStatus.Error ? (ActionResult)RedirectToAction("Error") : View("_Floors", floors);
        }

        [Authorize(Roles = "Admins")]
        public JsonResult GetFloorplanImage(int mapId, int floorId)
        {
            var mapsCtrl = new MapsController();
            ObjectResponse<string> floor = mapsCtrl.GetFloorplanImage(floorId);
            floor.Object = floor.Object;
            return Json(floor, JsonRequestBehavior.AllowGet);
        }

        [Authorize(Roles = "Admins")]
        [HttpPost]
        public JsonResult SaveGraphForMap(int floorId, Graph newGraph, Graph deletedGraph)
        {
            var mapsCtrl = new MapsController();
            ObjectResponse<Graph> floor = mapsCtrl.SaveGraphForFloor(floorId, newGraph, deletedGraph);
            return Json(floor, JsonRequestBehavior.AllowGet);
        }

        [Authorize(Roles = "Admins")]
        [HttpPost]
        public JsonResult SaveNodeInformation(int nodeId, string displayName, string roomName, int poiTypeId,
                                              string poiDescription, string qrCode, string nfcTag)
        {
            var mapsCtrl = new MapsController();
            var nodeInf = new NodeInformation(new PoI
                                                  {
                                                      Description = poiDescription,
                                                      Type = new PoiType {Id = poiTypeId}
                                                  }, displayName, roomName, qrCode, nfcTag);
            ObjectResponse<NodeInformation> tmp = mapsCtrl.SaveNodeInformation(nodeId, nodeInf);
            return Json(tmp, JsonRequestBehavior.AllowGet);
        }

        public JsonResult GetFloorPlanData(int id)
        {
            var mapsCtrl = new MapsController();

            ObjectResponse<FloorPlanData> result = mapsCtrl.GetFloorPlanData(id);

            return Json(result, JsonRequestBehavior.AllowGet);
        }

        #region Partial Views

        [Authorize(Roles = "Admins")]
        [HttpGet]
        public ActionResult GetFloor(int id)
        {
            var mapsCtrl = new MapsController();
            ObjectResponse<Floor> floor = mapsCtrl.GetFloor(id);

            return PartialView("_Layer", floor);
        }

        [Authorize(Roles = "Admins")]
        [HttpGet]
        public ActionResult GetFloorsForMap(int id)
        {
            var mapsCtrl = new MapsController();
            ListResponse<Floor> floors = mapsCtrl.GetFloorsForMap(id);

            ViewBag.MapId = id;
            return PartialView("_Floors", floors);
        }

        [Authorize(Roles = "Admins")]
        [HttpGet]
        public ActionResult GetMaps()
        {
            var mapsCtrl = new MapsController();
            ListResponse<Map> maps = mapsCtrl.GetMaps();

            return PartialView("_Maps", maps);
        }

        [Authorize(Roles = "Admins")]
        [HttpGet]
        public ActionResult GetNodeInformation(int nodeId)
        {
            var mapsCtrl = new MapsController();
            ObjectResponse<NodeInformation> nodeInformation = mapsCtrl.GetNodeInformationForNode(nodeId);

            int typId = 0;
            if (nodeInformation.Object.PoI != null && nodeInformation.Object.PoI.Type != null)
                typId = nodeInformation.Object.PoI.Type.Id;

            ViewBag.PoiTypes = mapsCtrl.GetPoiTypes().List.Select(x => new SelectListItem
            {
                Selected = (x.Id == typId),
                Text = x.Name,
                Value = x.Id.ToString(CultureInfo.InvariantCulture)
            });
            return PartialView("_NodeInformation", nodeInformation.Object);
        }

        [Authorize(Roles = "Admins")]
        [HttpGet]
        public ActionResult GetConnectedNodes(int nodeId)
        {
            var mapsCtrl = new MapsController();
            ListResponse<Node> connectedNodes = mapsCtrl.GetConnectedNodes(nodeId);

            ViewBag.StartNodeId = nodeId;
            if (connectedNodes.Status == RespsonseStatus.Ok)
                return PartialView("_ConnectedNodes", connectedNodes.List);
            return RedirectToAction("Error");
        }

        // Keine Autorisierung erforderlich
        [HttpGet]
        public ActionResult NodeInfo(int nodeId)
        {
            var mapsCtrl = new MapsController();
            var result = mapsCtrl.GetFullNodeInformationForNode(nodeId);
            return View(result);
        }

        #endregion
    }
}