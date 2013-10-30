using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Entity;
using System.Diagnostics;
using System.Linq;
using System.Web.Helpers;
using System.Web.Http;
using StudMap.Core;
using StudMap.Core.Users;
using StudMap.Data.Entities;

namespace StudMap.Service.Controllers
{
    public class UsersController : ApiController
    {
        [HttpPost]
        public BaseResponse Register(string userName, string password)
        {
            try
            {
                using (var entities = new UserEntities())
                {
                    if (ExistUserAlready(userName, entities))
                        return new BaseResponse
                        {
                            Status = RespsonseStatus.Error,
                            ErrorCode = ResponseError.UserNameDuplicate
                        };
                    return CreateUserAndAccount(userName, password, entities);
                }
            }
            catch (DataException)
            {
                return new BaseResponse {Status = RespsonseStatus.Error, ErrorCode = ResponseError.DatabaseError};
            }
        }

        [HttpPost]
        public BaseResponse Login(string userName, string password)
        {
            try
            {
                using (var entities = new UserEntities())
                {
                    var user = entities.UserProfile.FirstOrDefault(u => u.UserName == userName);
                    if (user == null)
                        return new BaseResponse{Status=RespsonseStatus.Error, ErrorCode= ResponseError.LoginInvalid};

                    var membership = entities.webpages_Membership.First(m => m.UserId == user.UserId);
                    if (!Crypto.VerifyHashedPassword(membership.Password, password))
                        return new BaseResponse { Status = RespsonseStatus.Error, ErrorCode = ResponseError.LoginInvalid };

                    var activeUser = entities.ActiveUsers.FirstOrDefault(u => u.UserId == user.UserId);
                    if (activeUser != null)
                        activeUser.LoginDate = DateTime.Now;
                    else
                        entities.ActiveUsers.Add(new ActiveUsers {UserId = user.UserId, LoginDate = DateTime.Now});

                    entities.SaveChanges();

                    return new BaseResponse {Status = RespsonseStatus.Ok, ErrorCode = ResponseError.None};
                }
            }
            catch (DataException)
            {
                return new BaseResponse {Status = RespsonseStatus.Error, ErrorCode = ResponseError.DatabaseError};
            }
        }

        public BaseResponse Logout(string userName)
        {
            try
            {
                using (var entities = new UserEntities())
                {
                    var user = entities.UserProfile.FirstOrDefault(u => u.UserName == userName);
                    if (user == null)
                        return new BaseResponse {Status = RespsonseStatus.Error, ErrorCode = ResponseError.LoginInvalid};

                    var activeUser = entities.ActiveUsers.FirstOrDefault(u => u.UserId == user.UserId);
                    if (activeUser != null)
                    {
                        entities.ActiveUsers.Remove(activeUser);
                        entities.SaveChanges();
                    }
                    return new BaseResponse {Status = RespsonseStatus.Ok, ErrorCode = ResponseError.None};
                }
            }
            catch (DataException)
            {
                return new BaseResponse {Status = RespsonseStatus.Error, ErrorCode = ResponseError.DatabaseError};
            }
        }

        public ListResponse<User> GetActiveUsers()
        {
            try
            {
                using (var entities = new UserEntities())
                {
                    var response = new ListResponse<User> { ErrorCode = ResponseError.None, Status = RespsonseStatus.Ok };
                    foreach (var activeUser in entities.ActiveUsers)
                        response.List.Add(new User
                        {
                            Name = entities.UserProfile.First(u => u.UserId == activeUser.UserId).UserName
                        });
                    return response;
                }
            }
            catch (DataException)
            {
                return new ListResponse<User> {Status = RespsonseStatus.Error, ErrorCode = ResponseError.DatabaseError};
            }
        }

        #region Private Helpers

        private static bool ExistUserAlready(string userName, UserEntities entities)
        {
            return entities.UserProfile.Any(profile => profile.UserName == userName);
        }

        private static BaseResponse CreateUserAndAccount(string userName, string password, UserEntities entities)
        {
            var user = entities.UserProfile.Add(new UserProfile
            {
                UserName = userName,
                webpages_Roles =
                    new List<webpages_Roles> { entities.webpages_Roles.First(role => role.RoleName == "Users") }
            });
            entities.SaveChanges();

            entities.webpages_Membership.Add(new webpages_Membership
            {
                UserId = user.UserId,
                CreateDate = DateTime.Now,
                IsConfirmed = true,
                Password = Crypto.HashPassword(password),
                PasswordSalt = string.Empty,
                PasswordChangedDate = DateTime.Now,
            });
            entities.SaveChanges();

            return new BaseResponse { Status = RespsonseStatus.Ok, ErrorCode = ResponseError.None };
        }
        #region Debugging

        private static Exception GetInnerstException(Exception e)
        {
            var ex = e;
            while (ex.InnerException != null)
                ex = ex.InnerException;
            return ex;
        }

        private static bool HasValidationErrors(DbContext entities)
        {
            var hasErrors = false;
            var errors = entities.GetValidationErrors();

            foreach (var error in errors)
            {
                hasErrors = true;
                Debug.WriteLine("Entity {0} is not valid! OriginalValues={1}; CurrentValues={2}", error.Entry.Entity,
                    error.Entry.OriginalValues, error.Entry.CurrentValues);

                foreach (var validationError in error.ValidationErrors)
                    Debug.WriteLine("   {0}: {1}", validationError.PropertyName, validationError.ErrorMessage);
            }
            return hasErrors;
        }
        #endregion // Debuggin

        #endregion // Private Helpers
    }
}
