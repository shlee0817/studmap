using System.Web.Http;
using StudMap.Core;
using StudMap.Core.Graph;
using StudMap.Core.WLAN;
using StudMap.Service.Services;

namespace StudMap.Service.Controllers
{
    public class FingerprintController : StudMapController
    {
        [HttpPost]
        public BaseResponse SaveFingerprintForNode(int nodeId, Fingerprint fingerprint)
        {
            var result = new BaseResponse();

            Execute(entities => FingerprintService.SaveFingerprintForNode(entities, nodeId, fingerprint), result);

            return result;
        }

        [HttpPost]
        public ObjectResponse<Node> GetNodeForFingerprint(LocationRequest request)
        {
            return new ObjectResponse<Node>();
        }

        [HttpPost]
        public ListResponse<NodeProbability> GetNodeProbabiltyForScan(LocationRequest request)
        {
            var result = new ListResponse<NodeProbability>();

            Execute(entities =>
            {
                result.List = FingerprintService.GetNodeProbabiltyForScan(entities, request);
            }, result);

            return result;
        }
    }
}
