using System.Collections.Generic;

namespace StudMap.Core.WLAN
{
    public class Fingerprint
    {
        public Fingerprint()
        {
            AccessPointScans = new List<AccessPointScan>();
        }

        public int NodeId { get; set; }
        public IEnumerable<AccessPointScan> AccessPointScans { get; set; }
    }
}