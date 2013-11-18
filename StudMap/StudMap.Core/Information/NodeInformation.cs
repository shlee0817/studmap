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

        public NodeInformation(Node node, string displayName, string roomName, PoI poI)
        {
            Node = node;
            DisplayName = displayName;
            RoomName = roomName;
            PoI = poI;
        }

        public NodeInformation()
        {
            DisplayName = "";
            RoomName = "";
            Node = new Node();
            ReadOnly = true;
            PoI = new PoI();
        }

        public NodeInformation(string displayName, string roomName, PoI poI)
        {
            DisplayName = displayName;
            RoomName = roomName;
            PoI = poI;
        }
    }
}
