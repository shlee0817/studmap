using System.Web.Mvc;

namespace StudMap.Service.Controllers
{
    public class TestsController : Controller
    {
        //
        // GET: /Tests/OCR
        public ActionResult OCR()
        {
            return View();
        }
        
        public ActionResult QR()
        {
            return View();
        }
    }
}
