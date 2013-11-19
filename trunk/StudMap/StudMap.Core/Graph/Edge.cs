namespace StudMap.Core.Graph
{
    /// <summary>
    /// Repräsentiert eine bidirektionale Kante zwischen zwei Knoten in einem Graphen.
    /// </summary>
    public class Edge
    {
        /// <summary>
        /// ID des Knoten, bei dem die Kante beginnt.
        /// </summary>
        public int StartNodeId { get; set; }

        /// <summary>
        /// ID des Knotens, bei dem die Kante endet.
        /// </summary>
        public int EndNodeId { get; set; }
    }
}
