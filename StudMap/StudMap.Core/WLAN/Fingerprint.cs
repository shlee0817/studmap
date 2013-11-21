using System.Collections.Generic;

namespace StudMap.Core.WLAN
{
    public class Fingerprint
    {
        public int NodeId { get; set; }
        public IEnumerable<AccessPointScan> AccessPointScans { get; set; }

        public Fingerprint()
        {
            AccessPointScans = new List<AccessPointScan>();
        }
    }
}
