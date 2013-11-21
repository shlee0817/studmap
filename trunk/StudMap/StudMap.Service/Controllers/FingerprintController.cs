using System.Web.Http;
using StudMap.Core;
using StudMap.Core.Graph;
using StudMap.Core.WLAN;

namespace StudMap.Service.Controllers
{
    public class FingerprintController : ApiController
    {
        [HttpPost]
        public BaseResponse SaveFingerprintForNode(int nodeId, Fingerprint fingerprint)
        {
            return new BaseResponse();
        }

        [HttpPost]
        public ObjectResponse<Node> GetNodeForFingerprint(Fingerprint fingerprint, double factor)
        {
            return new ObjectResponse<Node>();
        }
    }
}
