using System;
using System.Web.Http;
using StudMap.Core;
using StudMap.Core.Graph;
using StudMap.Core.WLAN;
using StudMap.Data.Entities;
using System.Linq;

namespace StudMap.Service.Controllers
{
    public class FingerprintController : ApiController
    {
        [HttpPost]
        public BaseResponse SaveFingerprintForNode(int nodeId, Fingerprint fingerprint)
        {
            var result = new BaseResponse();

            if (fingerprint == null)
            {
                result.SetError(ResponseError.FingeprintIsNotDefined);
                return result;
            } 
            if (fingerprint.AccessPointScans == null || !fingerprint.AccessPointScans.Any())
                return result;

            try
            {
                using (var entities = new MapsEntities())
                {
                    var node = entities.Nodes.FirstOrDefault(x => x.Id == nodeId);
                    if (node == null)
                    {
                        result.SetError(ResponseError.NodeIdDoesNotExist);
                        return result;
                    }

                    var fingerprints = entities.Fingerprints.Add(new Fingerprints
                        {
                            NodeId = node.Id
                        });

                    entities.SaveChanges();

                    foreach (var apScan in fingerprint.AccessPointScans)
                    {
                        var ap = entities.AccessPoints.FirstOrDefault(x => x.MAC == apScan.AccessPoint.MAC);
                        if (ap == null)
                        {
                            ap = entities.AccessPoints.Add(new AccessPoints
                                {
                                    MAC = apScan.AccessPoint.MAC
                                });

                            entities.SaveChanges();
                        }

                        entities.AccessPointScans.Add(new AccessPointScans
                            {
                                AccessPointId = ap.Id,
                                RecievedSignalStrength = apScan.ReceivedSignalStrength,
                                FingerprintId = fingerprints.Id
                            });
                    }

                    entities.SaveChanges();
                }
            }
            catch (Exception e)
            {
                result.SetError(ResponseError.DatabaseError);
                Elmah.ErrorSignal.FromCurrentContext().Raise(e);
            }
            return result;
        }

        [HttpPost]
        public ObjectResponse<Node> GetNodeForFingerprint(Fingerprint fingerprint, double factor)
        {
            return new ObjectResponse<Node>();
        }
    }
}
