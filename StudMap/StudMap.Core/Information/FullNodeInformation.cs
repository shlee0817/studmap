using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using StudMap.Core.Maps;

namespace StudMap.Core.Information
{
    public class FullNodeInformation
    {
        public Map Map { get; set; }

        public Floor Floor { get; set; }

        public NodeInformation Info { get; set; }
    }
}
