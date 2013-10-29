using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.Linq;
using System.Web.Http;
using StudMap.Core;
using StudMap.Core.Users;
using StudMap.Data.Entities;

namespace StudMap.Service.Controllers
{
    public class UsersController : ApiController
    {
        // localhost:1129/api/Users/Register?userName=test&password=sicher
        [HttpPost]
        public BaseResponse Register(string userName, string password)
        {
            var result = new BaseResponse();
            try
            {
                using (var entities = new UserEntities())
                {
                    var newMembership = entities.webpages_Membership.Add(new webpages_Membership
                    {
                        CreateDate = DateTime.Now,
                        IsConfirmed = true,
                        Password = password,
                        PasswordChangedDate = DateTime.Now,

                    });

                    var newUserProfile = new UserProfile()
                    {
                        UserId = newMembership.UserId,
                        UserName = userName,
                        webpages_Roles = new Collection<webpages_Roles>(),
                    };
                    newUserProfile.webpages_Roles.Add(entities.webpages_Roles.First(role => role.RoleName == "Users"));

                    entities.SaveChanges();
                }
            }
            catch (Exception)
            {
                result.Status = RespsonseStatus.Error;
                result.ErrorCode = ResponseError.DatabaseError;
            }
            return result;
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

        public ListResponse<User> GetActiveUsers()
        {
            var response = new ListResponse<User> {ErrorCode = ResponseError.None, Status = RespsonseStatus.Ok};
            try
            {
                using (var entities = new UserEntities())
                {
                    foreach (var userProfile in entities.UserProfile)
                        response.List.Add(new User {Name = userProfile.UserName});
                }
            }
            catch (Exception e)
            {
                Debug.WriteLine("Exception in GetActiveUsers: " + e.Message);
                Debug.WriteLine(e.StackTrace);

                response.Status = RespsonseStatus.Error;
                response.ErrorCode = ResponseError.DatabaseError;
            }
            return response;
        }
    }
}
