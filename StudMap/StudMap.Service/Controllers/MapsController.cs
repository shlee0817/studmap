using System.Collections.Generic;
using System.Web.Http;
using StudMap.Core;
using StudMap.Core.Graph;
using StudMap.Core.Maps;
using StudMap.Data.Entities;
using System.Data;
using System.Linq;

namespace StudMap.Service.Controllers
{
    public class MapsController : ApiController
    {
        #region Map
        public MapsResponse CreateMap(string mapName)
        {
            //TODO Implement
            return new MapsResponse
                {
                    Status = RespsonseStatus.Ok,
                    ErrorCode = ResponseError.None,
                    Map = new Map { Id = 1, Name = "WHS" }
                };
        }

        public BaseResponse DeleteMap(int mapId)
        {
            //TODO Implement
            return new BaseResponse
                {
                    Status = RespsonseStatus.Ok,
                    ErrorCode = ResponseError.None
                };
        }

        public ListResponse<Map> GetMaps()
        {
            var result = new ListResponse<Map>();

            try
            {
                using (var entities = new MapEntities())
                {
                    var maps = from map in entities.Maps
                               select new Map
                               {
                                   Id = map.Id,
                                   Name = map.Name
                               };
                    result.List = maps.ToList();
                }
            }
            catch (DataException ex)
            {
                result.Status = RespsonseStatus.Error;
                result.ErrorCode = ResponseError.DatabaseError;
            }

            return result;
        }
        #endregion

        #region Floor
        
        [HttpPost]
        public FloorsResponse CreateFloor(int mapId)
        {
            //TODO Post Body auslesen 
            return new FloorsResponse
                {
                    Status = RespsonseStatus.Ok,
                    ErrorCode = ResponseError.None,
                    Floor = new Floor
                        {
                            Id = 1,
                            ImageUrl = "",
                            MapId = 1
                        }
                };
        }

        public BaseResponse DeleteFloor(int floorId)
        {
            //TODO Implement
            return new BaseResponse
            {
                Status = RespsonseStatus.Ok,
                ErrorCode = ResponseError.None
                
            };
        }

        public IEnumerable<Floor> GetFloorsForMap(int mapId)
        {
            //TODO Implement
            return new List<Floor>();
        }

        [HttpPost]
        public FloorsResponse UploadFloorImage(int floorId, [FromBody]object floor)
        {
            //TODO Implement 
            return new FloorsResponse
            {
                Status = RespsonseStatus.Ok,
                ErrorCode = ResponseError.None,
                Floor = new Floor
                {
                    Id = 1,
                    ImageUrl = "",
                    MapId = 1
                }
            };
        }

        #endregion

        #region Layer: Graph
        public GraphResponse SaveGraphForFloor(int floorId, Graph graph)
        {
            //TODO Implement
            return new GraphResponse
                {
                    Status = RespsonseStatus.Ok,
                    ErrorCode = ResponseError.None,
                    Graph = new Graph
                        {
                            Edges = new List<Edge>(),
                            Nodes = new List<Node>()
                        }
                };
        }

        public GraphResponse GetGraphForFloor(int floorId)
        {
            //TODO Implement
            return new GraphResponse
            {
                Status = RespsonseStatus.Ok,
                ErrorCode = ResponseError.None,
                Graph = new Graph
                {
                    Edges = new List<Edge>(),
                    Nodes = new List<Node>()
                }
            };
        }
        #endregion
    }
}
