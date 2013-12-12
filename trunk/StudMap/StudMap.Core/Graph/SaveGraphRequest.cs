using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace StudMap.Core.Graph
{
    public class SaveGraphRequest
    {
        public int FloorId { get; set; }

        public Graph NewGraph { get; set; }

        public Graph DeletedGraph { get; set; }
    }
}
