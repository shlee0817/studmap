namespace StudMap.Core.Graph
{
    public class NodeInformation
    {
        public string DisplayName { get; set; }
        public string RoomName { get; set; }
        public Node Node { get; set; }
        public bool ReadOnly { get; set; }
        public string PoI { get; set; }

        public NodeInformation(Node node, string displayName, string roomName, string poI)
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
            PoI = "";
        }

        public NodeInformation(string displayName, string roomName, string poI)
        {
            DisplayName = displayName;
            RoomName = roomName;
            PoI = poI;
        }
    }
}
