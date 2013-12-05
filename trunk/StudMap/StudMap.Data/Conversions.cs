using StudMap.Core.Graph;
using StudMap.Core.Information;
using StudMap.Core.Maps;
using StudMap.Data.Entities;
using System.Collections.Generic;
using System.Linq;

namespace StudMap.Data
{
    public class Conversions
    {
        public static Map ToMap(Maps dbMap)
        {
            return new Map
            {
                Id = dbMap.Id,
                Name = dbMap.Name
            };
        }

        public static Floor ToFloor(Floors dbFloor, string imageBasePath)
        {
            return new Floor
            {
                Id = dbFloor.Id,
                MapId = dbFloor.MapId,
                Name = dbFloor.Name,
                ImageUrl = imageBasePath + dbFloor.ImageUrl,
                CreationTime = dbFloor.CreationTime
            };
        }

        public static Graph ToGraph(int floorId, IEnumerable<Nodes> nodes, IEnumerable<Edges> edges)
        {
            return new Graph
            {
                FloorId = floorId,
                Edges = edges.ToList().Select(ToEdge).ToList(),
                Nodes = nodes.ToList().Select(ToNode).ToList()
            };
        }

        public static Node ToNode(Nodes dbNode)
        {
            return new Node
            {
                Id = dbNode.Id,
                X = dbNode.X,
                Y = dbNode.Y,
                FloorId = dbNode.FloorId,
                HasInformation = dbNode.NodeInformation.Any()
            };
        }

        public static Edge ToEdge(Edges dbEdge)
        {
            return new Edge
            {
                StartNodeId = dbEdge.NodeStartId,
                EndNodeId = dbEdge.NodeEndId
            };
        }

        public static Core.Information.NodeInformation ToNodeInformation(Data.Entities.NodeInformation dbInfo)
        {
            return ToNodeInformation(dbInfo, dbInfo.Nodes, dbInfo.PoIs);
        }

        public static Core.Information.NodeInformation ToNodeInformation(Data.Entities.NodeInformation dbInfo, Nodes dbNode, PoIs dbPoI)
        {
            return new Core.Information.NodeInformation
            {
                DisplayName = dbInfo.DisplayName,
                Node = Conversions.ToNode(dbNode),
                RoomName = dbInfo.RoomName,
                NFCTag = dbInfo.NFCTag,
                QRCode = dbInfo.QRCode,
                PoI = dbPoI == null ? new PoI() : ToPoI(dbInfo.PoIs)
            };
        }

        public static PoI ToPoI(PoIs dbPoI)
        {
            return new PoI
            {
                Description = dbPoI.Description,
                Type = ToPoiType(dbPoI.PoiTypes)
            };
        }

        public static PoI ToPoI(PoisForMap dbPoI)
        {
            return new PoI
            {
                Type = new PoiType
                {
                    Id = dbPoI.PoiTypeId,
                    Name = dbPoI.PoiTypeName,
                },
                Description = dbPoI.PoiDescription
            };
        }

        public static PoiType ToPoiType(PoiTypes dbPoiType)
        {
            return new PoiType
            {
                Id = dbPoiType.Id,
                Name = dbPoiType.Name
            };
        }

        public static RoomAndPoI ToRoomAndPoI(PoisForMap dbPoI)
        {
            return new RoomAndPoI
            {
                Room = ToRoom(dbPoI),
                PoI = ToPoI(dbPoI)
            };
        }

        public static Room ToRoom(PoisForMap dbPoI)
        {
            return new Room
            {
                NodeId = dbPoI.NodeId,
                RoomName = dbPoI.RoomName,
                DisplayName = dbPoI.DisplayName,
                FloorId = dbPoI.FloorId
            };
        }

        public static Room ToRoom(NodeInformationForMap dbInfo)
        {
            return new Room
            {
                RoomName = dbInfo.RoomName,
                DisplayName = dbInfo.DisplayName,
                NodeId = dbInfo.NodeId,
                FloorId = dbInfo.FloorId
            };
        }
    }
}
