using StudMap.Core.Graph;

namespace StudMap.Core.Information
{
    public class NodeInformation
    {
        public string DisplayName { get; set; }
        public string RoomName { get; set; }
        public Node Node { get; set; }
        public bool ReadOnly { get; set; }
        public PoI PoI { get; set; }
        public string QRCode { get; set; }
        public string NFCTag { get; set; }

        public NodeInformation(Node node, string displayName, string roomName, PoI poI, string qrCode, string nfcTag)
        {
            Node = node;
            DisplayName = displayName;
            RoomName = roomName;
            PoI = poI;
            NFCTag = nfcTag;
            QRCode = qrCode;
        }

        public NodeInformation()
        {
            DisplayName = "";
            RoomName = "";
            Node = new Node();
            ReadOnly = true;
            PoI = new PoI();
            NFCTag = "";
            QRCode = "";
        }

        public NodeInformation(string displayName, string roomName, PoI poI, string qrCode, string nfcTag)
        {
            DisplayName = displayName;
            RoomName = roomName;
            PoI = poI;
            NFCTag = nfcTag;
            QRCode = qrCode;
        }

        public NodeInformation(string displayName, string roomName, PoI poI)
        {
            DisplayName = displayName;
            RoomName = roomName;
            PoI = poI;
        }
    }
}
