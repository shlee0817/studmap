using StudMap.Core.Information;

namespace StudMap.Maintenance.Models
{
    public class QRCodeNode
    {
        public string RoomName { get; set; }

        public string DisplayName { get; set; }

        public int NodeId { get; set; }

        public int FloorId { get; set; }

        public QRCodeNode(NodeInformation ni)
        {
            if (ni == null)
                return;

            RoomName = ni.RoomName;
            DisplayName = ni.DisplayName;

            if (ni.Node == null) return;
            NodeId = ni.Node.Id;
            FloorId = ni.Node.FloorId;
        }
    }
}
