using System;
using System.Collections.Generic;
using System.Linq;
using StudMap.Core;
using StudMap.Core.Graph;
using StudMap.Core.Information;
using StudMap.Data;
using StudMap.Data.Entities;
using StudMap.Service.CacheObjects;
using NodeInformation = StudMap.Core.Information.NodeInformation;

namespace StudMap.Service.Services
{
    public class InformationService
    {
        public static NodeInformation GetNodeInformationForNode(MapsEntities entities, int nodeId)
        {
            var result = new NodeInformation();
            Nodes node = entities.Nodes.Find(nodeId);
            if (node == null)
                throw new ServiceException(ResponseError.NodeIdDoesNotExist);

            result.Node = Conversions.ToNode(node);

            Data.Entities.NodeInformation queriedNodeInformation =
                entities.NodeInformation.FirstOrDefault(x => x.NodeId == nodeId);
            // Wenn keine Infos hinterlegt sind, dann leere Info zurückgegeben
            // Wichtig: Das ist kein Fehlerfall
            if (queriedNodeInformation == null)
                return result;

            return Conversions.ToNodeInformation(queriedNodeInformation);
        }

        public static List<NodeInformation> GetNodeInformation(MapsEntities entities, int mapId, int floorId)
        {
            if (!StudMapCache.Global.Maps.ContainsKey(mapId))
                throw new ServiceException(ResponseError.MapIdDoesNotExist);
            if (!entities.Floors.Any(f => f.Id == floorId))
                throw new ServiceException(ResponseError.FloorIdDoesNotExist);

            List<Data.Entities.NodeInformation> nodes = entities.NodeInformation
                                                                .Where(
                                                                    n =>
                                                                    n.Nodes.Floors.MapId == mapId &&
                                                                    n.Nodes.FloorId == floorId)
                                                                .ToList();

            return nodes.Select(Conversions.ToNodeInformation).ToList();
        }

        public static NodeInformation SaveNodeInformation(MapsEntities entities, NodeInformation inputInfo)
        {
            if (!entities.Nodes.Any(n => n.Id == inputInfo.Node.Id))
                throw new ServiceException(ResponseError.NodeIdDoesNotExist);

            // Schon vorhandene Nodeinformationen suchen
            Data.Entities.NodeInformation nodeInformation =
                entities.NodeInformation.FirstOrDefault(x => x.NodeId == inputInfo.Node.Id);

            PoIs poi = CreateOrUpdatePoI(entities, inputInfo.PoI, nodeInformation);
            CreateOrUpdateNodeInfo(entities, inputInfo, nodeInformation, poi);

            return GetNodeInformationForNode(entities, inputInfo.Node.Id);
        }

        private static void CreateOrUpdateNodeInfo(MapsEntities entities, NodeInformation inputInfo, Data.Entities.NodeInformation nodeInformation, PoIs poi)
        {
            int? poiId = poi != null ? (int?) poi.Id : null;
            // Wenn es keine NodeInfo gibt, eine Neue anlegen
            if (nodeInformation == null)
            {
                entities.NodeInformation.Add(new Data.Entities.NodeInformation
                    {
                        DisplayName = inputInfo.DisplayName,
                        RoomName = inputInfo.RoomName,
                        QRCode = inputInfo.QRCode,
                        NFCTag = inputInfo.NFCTag,
                        PoiId = poiId,
                        NodeId = inputInfo.Node.Id,
                        CreationTime = DateTime.Now
                    });
            }
                // Ansonsten die bestehende NodeInfo aktualisieren
            else
            {
                nodeInformation.DisplayName = inputInfo.DisplayName;
                nodeInformation.RoomName = inputInfo.RoomName;
                nodeInformation.NFCTag = inputInfo.NFCTag;
                nodeInformation.QRCode = inputInfo.QRCode;
                nodeInformation.PoiId = poiId;
            }
            entities.SaveChanges();
        }

        private static PoIs CreateOrUpdatePoI(MapsEntities entities, PoI inputPoI,
                                              Data.Entities.NodeInformation nodeInformation)
        {
            if (inputPoI.Type.Id == 0)
                return null;

            PoiTypes poiType = entities.PoiTypes.FirstOrDefault(x => x.Id == inputPoI.Type.Id);
            if (poiType == null)
                throw new ServiceException(ResponseError.PoiTypeIdDoesNotExist);

            inputPoI.Type.Name = poiType.Name;

            PoIs poi;
            // Falls zu dem Knoten noch kein PoI angelegt wurde, einen neuen erstellen
            if (nodeInformation == null || nodeInformation.PoIs == null)
            {
                poi = entities.PoIs.Add(new PoIs
                    {
                        TypeId = inputPoI.Type.Id,
                        Description = inputPoI.Description
                    });
            }
                // Ansonsten vorhandenen PoI aktualisieren
            else
            {
                poi = entities.PoIs.FirstOrDefault(x => x.Id == nodeInformation.PoiId);
                // TODO: Die PoI-ID kommt aus der DB und kann eig. nicht ungültig sein
                if (poi == null)
                    throw new ServiceException(ResponseError.PoiDoesNotExist);

                poi.TypeId = inputPoI.Type.Id;
                poi.Description = inputPoI.Description;
            }
            entities.SaveChanges();

            return poi;
        }

        public static IEnumerable<PoiType> GetPoiTypes(MapsEntities entities)
        {
            var typeList = new List<PoiType> {new PoiType {Id = 0, Name = "Kein"}};
            // TODO: Der String "Kein" sollte wahrscheinlich irgendwo in eine Config oder in die DB
            typeList.AddRange(entities.PoiTypes.ToList().Select(Conversions.ToPoiType));
            return typeList;
        }

        public static List<RoomAndPoI> GetPoIsForMap(MapsEntities entities, int mapId)
        {
            if (!StudMapCache.Global.Maps.ContainsKey(mapId))
                throw new ServiceException(ResponseError.MapIdDoesNotExist);

            return entities.PoisForMap.Where(x => x.MapId == mapId).ToList()
                           .Select(Conversions.ToRoomAndPoI).ToList();
        }

        public static List<Room> GetRoomsForMap(MapsEntities entities, int mapId)
        {
            if (!StudMapCache.Global.Maps.ContainsKey(mapId))
                throw new ServiceException(ResponseError.MapIdDoesNotExist);

            return entities.NodeInformationForMap.Where(x => x.MapId == mapId).ToList()
                           .Select(Conversions.ToRoom).ToList();
        }

        public static Node GetNodeForNFC(MapsEntities entities, int mapId, string nfcTag)
        {
            Data.Entities.NodeInformation nodeInformation =
                entities.NodeInformation.FirstOrDefault(x => x.NFCTag == nfcTag);
            if (nodeInformation == null)
                throw new ServiceException(ResponseError.NFCTagDoesNotExist);

            return Conversions.ToNode(nodeInformation.Nodes);
        }

        public static void SaveNFCForNode(MapsEntities entities, int nodeId, string nfcTag)
        {
            Data.Entities.NodeInformation nodeInformation =
                entities.NodeInformation.FirstOrDefault(x => x.Nodes.Id == nodeId);

            if (nodeInformation == null)
                throw new ServiceException(ResponseError.NodeIdDoesNotExist);
            if (entities.NodeInformation.Any(x => x.NFCTag == nfcTag))
                throw new ServiceException(ResponseError.NFCTagAllreadyAssigned);
            if (String.IsNullOrWhiteSpace(nfcTag))
                throw new ServiceException(ResponseError.NFCTagIsNullOrEmpty);

            nodeInformation.NFCTag = nfcTag;
            entities.SaveChanges();
        }

        public static Node GetNodeForQRCode(MapsEntities entities, int mapId, string qrCode)
        {
            Data.Entities.NodeInformation nodeInformation =
                entities.NodeInformation.FirstOrDefault(x => x.QRCode == qrCode);
            if (nodeInformation == null)
                throw new ServiceException(ResponseError.QRCodeDosNotExist);

            return Conversions.ToNode(nodeInformation.Nodes);
        }

        public static void SaveQRCodeForNode(MapsEntities entities, int nodeId, string qrCode)
        {
            Data.Entities.NodeInformation nodeInformation =
                entities.NodeInformation.FirstOrDefault(x => x.Nodes.Id == nodeId);

            if (nodeInformation == null)
                throw new ServiceException(ResponseError.NodeIdDoesNotExist);
            if (String.IsNullOrWhiteSpace(qrCode))
                throw new ServiceException(ResponseError.QRCodeIsNullOrEmpty);

            nodeInformation.QRCode = qrCode;
            entities.SaveChanges();
        }

        public static FullNodeInformation GetFullNodeInformationForNode(MapsEntities entities, int nodeId)
        {
            var info = GetNodeInformationForNode(entities, nodeId);
            var floor = FloorService.GetFloor(entities, info.Node.FloorId);
            var map = MapService.GetMap(entities, floor.MapId);

            return new FullNodeInformation
            {
                Map = map,
                Floor = floor,
                Info = info
            };
        }
    }
}