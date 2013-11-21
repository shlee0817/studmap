using System.Collections.Generic;

namespace StudMap.Core.WLAN
{
    public class Fingerprint
    {
        public int NodeId { get; set; }
        public IEnumerable<AccessPoint> AccessPoints { get; set; }
        public int ReceivedSignalStrength { get; set; }

        public Fingerprint()
        {
            AccessPoints = new List<AccessPoint>();
        }
    }
}
