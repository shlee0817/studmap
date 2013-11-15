namespace StudMap.Core.Graph
{
    public class NodeInformation
    {
        private int nodeID;

        public string DisplayName { get; set; }
        public string RoomName { get; set; }
        public Node Node { get; set; }
        public bool ReadOnly { get; set; }
        public string PoI { get; set; }

        public NodeInformation(Node node, string displayName, string roomName, string poI)
        {
            this.Node = node;
            this.DisplayName = displayName;
            this.RoomName = roomName;
            this.PoI = poI;
        }

        public NodeInformation()
        {
            this.DisplayName = "";
            this.RoomName = "";
            this.Node = new Node();
            this.ReadOnly = true;
            this.PoI = "";
        }

        public NodeInformation(int nodeID, string displayName, string roomName, string poI)
        {
            this.nodeID = nodeID;
            this.DisplayName = displayName;
            this.RoomName = roomName;
            this.PoI = poI;
        }
    }
}
