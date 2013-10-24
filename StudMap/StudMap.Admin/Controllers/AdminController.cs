using System.Collections.Generic;
using System.Web;
using System.Web.Mvc;
using StudMap.Admin.Models;
using StudMap.Core;
using StudMap.Core.Maps;
using StudMap.Service.Controllers;

namespace StudMap.Admin.Controllers
{
    public class AdminController : Controller
    {
        private readonly string _serverUploadFolder = System.Web.HttpContext.Current.Server.MapPath("~/App_Data");
        
        [Authorize(Roles = "Admins")]
        public ActionResult Index()
        {
            var mapsCtrl = new MapsController();
            var maps = mapsCtrl.GetMaps();

            return View(maps);
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
        [HttpPost]
        public ActionResult UploadFloorPlan(HttpPostedFileBase data)
        {
            if (data != null)
            {
                data.SaveAs(_serverUploadFolder + "\\floors\\" + data.FileName);
            }

            return View("Index");
        }

        [Authorize(Roles = "Admins")]
        [HttpGet]
        public ActionResult GetFloorsForMap(int id)
        {
            //TODO: Auslesen aus der Datenbank
            var floors = new List<FloorViewModel>
                {
                    new FloorViewModel
                        {
                            FloorId = 1,
                            FloorImageFile = _serverUploadFolder + "\\floors\\RN_Ebene_0.png",
                            MapId = 1
                        },
                    new FloorViewModel
                        {
                            FloorId = 2,
                            FloorImageFile = _serverUploadFolder + "\\floors\\RN_Ebene_1.png",
                            MapId = 1
                        },
                    new FloorViewModel
                        {
                            FloorId = 3,
                            FloorImageFile = _serverUploadFolder + "\\floors\\RN_Ebene_2.png",
                            MapId = 1
                        }
                };

            return PartialView("_Floors", floors);
        }
        
        [Authorize(Roles = "Admins")]
        public ActionResult GetFloorplanImage(int mapId, int floorId)
        {
            return File(_serverUploadFolder + "\\floors\\RN_Ebene_" + floorId + ".png", "image/png");
        }

        //Zu Testzwecken (später besser in die API)
        public ContentResult GetMapData(int id)
        {
            const string data = "{\"pathplot\":[{\"id\":\"flt-1\",\"classes\":\"planned\",\"points\":[{\"x\":23.8,\"y\":30.6},{\"x\":19.5,\"y\":25.7},{\"x\":14.5,\"y\":25.7},{\"x\":13.2,\"y\":12.3}]}],\"graph\":[{\"id\":\"flt-2\",\"classes\":\"planned\",\"points\":[{\"x\":23.8,\"y\":30.6},{\"x\":19.5,\"y\":25.7},{\"x\":14.5,\"y\":25.7},{\"x\":13.2,\"y\":12.3}]}]}";
            return Content(data, "application/json");
        }
    }
}
