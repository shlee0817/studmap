using System.Collections.Generic;
using System.Web;
using System.Web.Mvc;
using StudMap.Admin.Models;

namespace StudMap.Admin.Controllers
{
    public class AdminController : Controller
    {
        private readonly string _serverUploadFolder = System.Web.HttpContext.Current.Server.MapPath("~/App_Data");
        
        [Authorize(Roles = "Admins")]
        public ActionResult Index()
        {
            var maps = new List<MapViewModel>
                {
                    new MapViewModel
                        {
                            MapId = 1,
                            Name = "Westfälische Hochschule"
                        }
                };

            return View(maps);
        }

        [Authorize(Roles = "Admins")]
        public ActionResult AddMap()
        {
            return View();
        }

        [Authorize(Roles = "Admins")]
        [ValidateAntiForgeryToken]
        [HttpPost]
        public ActionResult AddMap(MapModel data)
        {
            //TODO: Map in der Datenbank speichern
            return View();
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
                            FloorImageFile = _serverUploadFolder + "\\floors\\Smaple_Floorplan1.jpg",
                            MapId = 1
                        },
                    new FloorViewModel
                        {
                            FloorId = 2,
                            FloorImageFile = _serverUploadFolder + "\\floors\\Smaple_Floorplan2.jpg",
                            MapId = 1
                        },
                    new FloorViewModel
                        {
                            FloorId = 3,
                            FloorImageFile = _serverUploadFolder + "\\floors\\Smaple_Floorplan3.jpg",
                            MapId = 1
                        }
                };

            return PartialView("_Floors", floors);
        }
        
        [Authorize(Roles = "Admins")]
        public ActionResult GetFloorplanImage(int mapId, int floorId)
        {
            return File(_serverUploadFolder + "\\floors\\Sample_Floorplan" + floorId + ".jpg", "image/jpeg");
        }

        //Zu Testzwecken (später besser in die API)
        public ContentResult GetMapData(int id)
        {
            const string data = "{\"overlays\":{\"polygons\":[{\"id\":\"p1\",\"name\":\"kitchen\",\"points\":[{\"x\":2.513888888888882,\"y\":8.0},{\"x\":6.069444444444433,\"y\":8.0},{\"x\":6.069444444444434,\"y\":5.277535934291582},{\"x\":8.20833333333332,\"y\":2.208151950718685},{\"x\":13.958333333333323,\"y\":2.208151950718685},{\"x\":16.277777777777825,\"y\":5.277535934291582},{\"x\":16.277777777777803,\"y\":10.08151950718685},{\"x\":17.20833333333337,\"y\":10.012135523613962},{\"x\":17.27777777777782,\"y\":18.1387679671458},{\"x\":2.513888888888882,\"y\":18.0}]}]},\"pathplot\":[{\"id\":\"flt-1\",\"classes\":\"planned\",\"points\":[{\"x\":23.8,\"y\":30.6},{\"x\":19.5,\"y\":25.7},{\"x\":14.5,\"y\":25.7},{\"x\":13.2,\"y\":12.3}]}],\"graph\":[{\"id\":\"flt-2\",\"classes\":\"planned\",\"points\":[{\"x\":23.8,\"y\":30.6},{\"x\":19.5,\"y\":25.7},{\"x\":14.5,\"y\":25.7},{\"x\":13.2,\"y\":12.3}]}]}";
            return Content(data, "application/json");
        }
    }
}
