using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace StudMap.Core.WLAN
{
    public class LocationAPScan
    {
        public string MAC { get; set; }

        public int RSS { get; set; }
    }

    public class LocationRequest
    {
        public LocationRequest()
        {
            Scans = new List<LocationAPScan>();
        }

        public int MapId { get; set; }

        // TODO: Entfernen, wenn nicht mehr benötigt (Testzwecke)
        public int NodeCount { get; set; }

        public IEnumerable<LocationAPScan> Scans { get; set; }
    }
}
