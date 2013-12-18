using System.Web.Http;
using StudMap.Core;
using StudMap.Core.Users;
using StudMap.Service.Services;

namespace StudMap.Service.Controllers
{
    public class UsersController : StudMapController
    {
        [HttpPost]
        public BaseResponse Register(string userName, string password)
        {
            var result = new BaseResponse();

            ExecuteUsers(entities => UserService.Register(entities, userName, password), result);

            return result;
        }

        [HttpPost]
        public BaseResponse Login(string userName, string password)
        {
            var result = new BaseResponse();

            ExecuteUsers(entities => UserService.Login(entities, userName, password), result);

            return result;
        }

        public BaseResponse Logout(string userName)
        {
            var result = new BaseResponse();

            ExecuteUsers(entities => UserService.Logout(entities, userName), result);

            return result;
        }

        [HttpGet]
        public ListResponse<User> GetActiveUsers()
        {
            var result = new ListResponse<User>();

            ExecuteUsers(entities => { result.List = UserService.GetActiveUsers(entities); }, result);

            return result;
        }
    }
}