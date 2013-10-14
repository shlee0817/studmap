using System.Drawing;
using System.Drawing.Imaging;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Threading.Tasks;
using System.Web;
using System.Web.Http;
using OCR.TesseractWrapper;

namespace StudMap.Service.Controllers
{
    public class OCRController : ApiController
    {
        [HttpPost]
        public async Task<string> GetLocationFromImage()
        {

            // Verify that this is an HTML Form file upload request
            if (!Request.Content.IsMimeMultipartContent("form-data"))
            {
                throw new HttpResponseException(Request.CreateResponse(HttpStatusCode.UnsupportedMediaType));
            }

            // Create a stream provider for setting up output streams
            var streamProvider = new MultipartFormDataStreamProvider(_serverUploadFolder);

            // Read the MIME multipart asynchronously content using the stream provider we just created.
            await Request.Content.ReadAsMultipartAsync(streamProvider);

            var img = streamProvider.FileData.FirstOrDefault();

            if (img != null)
            {
                var image = System.Drawing.Image.FromFile(img.LocalFileName);
                image.Save(_serverUploadFolder + "\\1.jpg", ImageFormat.Jpeg);
                
                
                var tesseract = new TesseractProcessor();

                using (var bmp = Bitmap.FromFile(_serverUploadFolder + "\\3.jpg") as Bitmap)
                {
                    var success = tesseract.Init(_serverUploadFolder + @"\tessdata\", "deu", (int)eOcrEngineMode.OEM_DEFAULT);
                    if (!success)
                    {
                        return "Failed to initialize tesseract.";
                    }
                    var word = tesseract.Recognize(bmp);

                    return word;
                }
            }
            return "";
/*
            if (data != null)
            {
                string[] extensions = { ".png", ".jpg", ".bmp" };
                if (extensions.All(extension => extension != data.ContentType))
                {
                    //Fehler
                    return "Kein Png";
                }

                if (data.ContentLength == 0)
                {
                    //Fehler
                    return "Kein Content";
                }
                var image = System.Drawing.Image.FromStream(data.InputStream);

                var tesseract = new TesseractProcessor();
                string word = tesseract.Recognize(image);
                return word;
            }
            return "1";
*/
        }

        private readonly string _serverUploadFolder = HttpContext.Current.Server.MapPath("~/App_Data");
    }
}
