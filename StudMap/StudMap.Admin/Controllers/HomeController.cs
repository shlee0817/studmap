using System.Web.Mvc;

namespace StudMap.Admin.Controllers
{
    public class HomeController : Controller
    {
        public ActionResult Index()
        {
            return View();
        }

        [Authorize(Roles = "Admins")]
        public ActionResult Admin()
        {
            return View();
        }
    }
}
