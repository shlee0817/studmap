using System.Web.Http;
using StudMap.Core;
using StudMap.Core.Graph;
using StudMap.Core.Information;
using StudMap.Core.Maps;
using StudMap.Service.Services;

namespace StudMap.Service.Controllers
{
    public class MapsController : StudMapController
    {
        #region Map

        [HttpPost]
        public ObjectResponse<Map> CreateMap(string mapName)
        {
            var result = new ObjectResponse<Map>();

            ExecuteMaps(entities =>
            {
                result.Object = MapService.CreateMap(entities, mapName);
            }, result);

            return result;
        }

        [HttpPost]
        public BaseResponse DeleteMap(int mapId)
        {
            var result = new BaseResponse();

            ExecuteMaps(entities => MapService.DeleteMap(entities, mapId), result);

            return result;
        }

        [HttpGet]
        public ListResponse<Map> GetMaps()
        {
            var result = new ListResponse<Map>();

            Execute(() =>
            {
                result.List = MapService.GetMapsCached();
            }, result);

            return result;
        }

        #endregion

        #region Floor

        [HttpPost]
        public ObjectResponse<Floor> CreateFloor(int mapId, string name = "")
        {
            var result = new ObjectResponse<Floor>();

            ExecuteMaps(entities =>
            {
                result.Object = FloorService.CreateFloor(entities, mapId, name);
            }, result);

            return result;
        }

        [HttpPost]
        public BaseResponse DeleteFloor(int floorId)
        {
            var result = new BaseResponse();

            ExecuteMaps(entities => FloorService.DeleteFloor(entities, floorId), result);

            return result;
        }

        [HttpGet]
        public ListResponse<Floor> GetFloorsForMap(int mapId)
        {
            var result = new ListResponse<Floor>();

            ExecuteMaps(entities =>
            {
                result.List = FloorService.GetFloorsForMapCached(mapId);
            }, result);

            return result;
        }

        [HttpGet]
        public ObjectResponse<Floor> GetFloor(int floorId)
        {
            var result = new ObjectResponse<Floor>();

            Execute(() =>
            {
                result.Object = FloorService.GetFloorCached(floorId);
            }, result);

            return result;
        }


        [HttpGet]
        public ObjectResponse<string> GetFloorplanImage(int floorId)
        {
            var result = new ObjectResponse<string>();

            Execute(() =>
            {
                result.Object = FloorService.GetFloorplanImageCached(floorId);
            }, result);

            return result;
        }

        [HttpPost]
        public ObjectResponse<Floor> UploadFloorImage(int floorId, string filename)
        {
            var result = new ObjectResponse<Floor>();

            ExecuteMaps(entities =>
            {
                result.Object = FloorService.UploadFloorImage(entities, floorId, filename);
            }, result);

            return result;
        }

        #endregion

        #region Layer: Graph

        [HttpPost]
        public ObjectResponse<Graph> SaveGraphForFloor(int floorId, Graph newGraph, Graph deletedGraph)
        {
            var result = new ObjectResponse<Graph>();

            ExecuteMaps(entities =>
            {
                result.Object = GraphService.SaveGraphForFloor(entities, floorId, newGraph, deletedGraph);
            }, result);

            return result;
        }

        [HttpPost]
        public BaseResponse DeleteGraphForFloor(int floorId)
        {
            var result = new BaseResponse();

            ExecuteMaps(entities => GraphService.DeleteGraphForFloor(entities, floorId), result);

            return result;
        }

        [HttpGet]
        public ObjectResponse<Graph> GetGraphForFloor(int floorId)
        {
            var result = new ObjectResponse<Graph>();

            Execute(() =>
            {
                result.Object = GraphService.GetGraphForFloorCached(floorId);
            }, result);

            return result;
        }

        [HttpGet]
        public ObjectResponse<FloorPlanData> GetFloorPlanData(int floorId)
        {
            var floorPlanData = new ObjectResponse<FloorPlanData> { Object = new FloorPlanData() };
            ObjectResponse<Graph> result = GetGraphForFloor(floorId);
            if (result.Status == RespsonseStatus.Ok)
            {
                floorPlanData.Object.Graph = result.Object;
            }
            return floorPlanData;
        }

        public ListResponse<Node> GetConnectedNodes(int nodeId)
        {
            var result = new ListResponse<Node>();

            ExecuteMaps(entities =>
            {
                result.List = GraphService.GetConnectedNodes(entities, nodeId);
            }, result);

            return result;
        }

        #endregion

        #region Layer: Navigation

        [HttpGet]
        public ListResponse<Node> GetRouteBetween(int mapId, int startNodeId, int endNodeId)
        {
            var result = new ListResponse<Node>();

            ExecuteMaps(entities =>
            {
                result.List = NavigationService.GetRouteBetweenCached(mapId, startNodeId, endNodeId);
            }, result);

            return result;
        }

        #endregion

        #region Layer: Information

        [HttpGet]
        public ObjectResponse<NodeInformation> GetNodeInformationForNode(int nodeId)
        {
            var result = new ObjectResponse<NodeInformation>();

            ExecuteMaps(entities =>
            {
                result.Object = InformationService.GetNodeInformationForNode(entities, nodeId);
            }, result);

            return result;
        }

        [HttpGet]
        public ListResponse<NodeInformation> GetNodeInformation(int mapId, int floorId)
        {
            var result = new ListResponse<NodeInformation>();

            ExecuteMaps(entities =>
            {
                result.List = InformationService.GetNodeInformation(entities, mapId, floorId);
            }, result);

            return result;
        }

        [HttpGet]
        public ObjectResponse<FullNodeInformation> GetFullNodeInformationForNode(int nodeId)
        {
            var result = new ObjectResponse<FullNodeInformation>();

            ExecuteMaps(entities =>
            {
                result.Object = InformationService.GetFullNodeInformationForNode(entities, nodeId);
            }, result);

            return result;
        }

        // TODO: nodeId Parameter entfernen und in nodeInfo setzen?
        [HttpPost]
        public ObjectResponse<NodeInformation> SaveNodeInformation(int nodeId, NodeInformation nodeInf)
        {
            var result = new ObjectResponse<NodeInformation>();

            ExecuteMaps(entities =>
            {
                nodeInf.Node = new Node { Id = nodeId };
                result.Object = InformationService.SaveNodeInformation(entities, nodeInf);
            }, result);

            return result;
        }

        [HttpGet]
        public ListResponse<PoiType> GetPoiTypes()
        {
            var result = new ListResponse<PoiType>();

            ExecuteMaps(entities =>
            {
                result.List = InformationService.GetPoiTypes(entities);
            }, result);

            return result;
        }

        [HttpGet]
        public ListResponse<RoomAndPoI> GetPoIsForMap(int mapId)
        {
            var result = new ListResponse<RoomAndPoI>();

            ExecuteMaps(entities =>
            {
                result.List = InformationService.GetPoIsForMap(entities, mapId);
            }, result);

            return result;
        }

        [HttpGet]
        public ListResponse<Room> GetRoomsForMap(int mapId)
        {
            var result = new ListResponse<Room>();

            ExecuteMaps(entities =>
            {
                result.List = InformationService.GetRoomsForMap(entities, mapId);
            }, result);

            return result;
        }

        [HttpGet]
        public ObjectResponse<Node> GetNodeForNFC(int mapId, string nfcTag)
        {
            var result = new ObjectResponse<Node>();

            ExecuteMaps(entities =>
            {
                result.Object = InformationService.GetNodeForNFC(entities, mapId, nfcTag);
            }, result);

            return result;
        }

        [HttpPost]
        public BaseResponse SaveNFCForNode(int nodeId, string nfcTag)
        {
            var result = new BaseResponse();

            ExecuteMaps(entities => InformationService.SaveNFCForNode(entities, nodeId, nfcTag), result);

            return result;
        }

        [HttpGet]
        public ObjectResponse<Node> GetNodeForQRCode(int mapId, string qrCode)
        {
            var result = new ObjectResponse<Node>();

            ExecuteMaps(entities =>
            {
                result.Object = InformationService.GetNodeForQRCode(entities, mapId, qrCode);
            }, result);

            return result;
        }

        public BaseResponse SaveQRCodeForNode(int nodeId, string qrCode)
        {
            var result = new BaseResponse();

            ExecuteMaps(entities => InformationService.SaveQRCodeForNode(entities, nodeId, qrCode), result);

            return result;
        }

        #endregion // Layer: Information
    }
}