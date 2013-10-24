using System.Collections.Generic;
using System.Web.Http;
using StudMap.Core;
using StudMap.Core.Users;

namespace StudMap.Service.Controllers
{
    public class UsersController : ApiController
    {
        public BaseResponse Register(string userName, string password)
        {
            //TODO: Implement
            return new BaseResponse
                {
                    Status = RespsonseStatus.Ok,
                    ErrorCode = ResponseError.None
                };
        }

        public BaseResponse Login(string userName, string password)
        {
            //TODO: Implement
            return new BaseResponse
            {
                Status = RespsonseStatus.Ok,
                ErrorCode = ResponseError.None
            };
        }

        public IEnumerable<User> GetActiveUsers()
        {
            //TODO: Implement
            return new List<User>();
        }
    }
}
