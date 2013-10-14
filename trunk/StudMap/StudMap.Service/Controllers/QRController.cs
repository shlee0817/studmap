using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Threading.Tasks;
using System.Web;
using System.Web.Http;
using com.google.zxing;
using com.google.zxing.common;
using com.google.zxing.qrcode;

namespace StudMap.Service.Controllers
{
    public class QRController : ApiController
    {
        private readonly string _serverUploadFolder = HttpContext.Current.Server.MapPath("~/App_Data");

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
                var image = Image.FromFile(img.LocalFileName);
                var path = _serverUploadFolder + "\\" + img.Headers.ContentDisposition.FileName.Replace("\"", "");
                image.Save(path, ImageFormat.Jpeg);

                using (var bmp = Image.FromFile(path) as Bitmap)
                {
                    if (bmp != null)
                    {
                        var rgb = new RGBLuminanceSource(bmp, bmp.Width, bmp.Height);
                        var hybrid = new HybridBinarizer(rgb);
                        var binBitmap = new BinaryBitmap(hybrid);
                        var reader = new QRCodeReader();
                        var result = reader.decode(binBitmap);

                        if (result != null)
                        {
                            return result.Text;
                        }
                    }
                }
            }
            return "";
        }
    }
}
