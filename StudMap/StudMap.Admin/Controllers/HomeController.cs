using System.Web;
using System.Web.Mvc;

namespace StudMap.Admin.Controllers
{
    public class HomeController : Controller
    {
        private readonly string _serverUploadFolder = System.Web.HttpContext.Current.Server.MapPath("~/App_Data");

        public ActionResult Index()
        {
            return View();
        }

        [Authorize(Roles = "Admins")]
        public ActionResult Admin()
        {
            return View();
        }

        [Authorize(Roles = "Admins")]
        [HttpPost]
        public void UploadMap(HttpPostedFileBase data)
        {
            if (data != null)
            {
                data.SaveAs(_serverUploadFolder + "\\" + data.FileName);
            }
        }
    }
}
