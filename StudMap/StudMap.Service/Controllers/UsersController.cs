﻿using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Entity;
using System.Data.Entity.Validation;
using System.Diagnostics;
using System.Linq;
using System.Web.Helpers;
using System.Web.Http;
using Elmah;
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
            catch (DataException ex)
            {
                ErrorSignal.FromCurrentContext().Raise(ex);
                return new BaseResponse
                    {
                        Status = RespsonseStatus.Error,
                        ErrorCode = ResponseError.DatabaseError,
                        ErrorMessage = ex.StackTrace
                    };
            }
        }

        [HttpPost]
        public BaseResponse Login(string userName, string password)
        {
            try
            {
                using (var entities = new UserEntities())
                {
                    UserProfile user = entities.UserProfile.FirstOrDefault(u => u.UserName == userName);
                    if (user == null)
                        return new BaseResponse {Status = RespsonseStatus.Error, ErrorCode = ResponseError.LoginInvalid};

                    webpages_Membership membership = entities.webpages_Membership.First(m => m.UserId == user.UserId);
                    if (!Crypto.VerifyHashedPassword(membership.Password, password))
                        return new BaseResponse {Status = RespsonseStatus.Error, ErrorCode = ResponseError.LoginInvalid};

                    ActiveUsers activeUser = entities.ActiveUsers.FirstOrDefault(u => u.UserId == user.UserId);
                    if (activeUser != null)
                        activeUser.LoginDate = DateTime.Now;
                    else
                        entities.ActiveUsers.Add(new ActiveUsers {UserId = user.UserId, LoginDate = DateTime.Now});

                    entities.SaveChanges();

                    return new BaseResponse {Status = RespsonseStatus.Ok, ErrorCode = ResponseError.None};
                }
            }
            catch (DataException ex)
            {
                ErrorSignal.FromCurrentContext().Raise(ex);
                return new BaseResponse
                    {
                        Status = RespsonseStatus.Error,
                        ErrorCode = ResponseError.DatabaseError,
                        ErrorMessage = ex.StackTrace
                    };
            }
        }

        public BaseResponse Logout(string userName)
        {
            try
            {
                using (var entities = new UserEntities())
                {
                    UserProfile user = entities.UserProfile.FirstOrDefault(u => u.UserName == userName);
                    if (user == null)
                        return new BaseResponse {Status = RespsonseStatus.Error, ErrorCode = ResponseError.LoginInvalid};

                    ActiveUsers activeUser = entities.ActiveUsers.FirstOrDefault(u => u.UserId == user.UserId);
                    if (activeUser != null)
                    {
                        entities.ActiveUsers.Remove(activeUser);
                        entities.SaveChanges();
                    }
                    return new BaseResponse {Status = RespsonseStatus.Ok, ErrorCode = ResponseError.None};
                }
            }
            catch (DataException ex)
            {
                ErrorSignal.FromCurrentContext().Raise(ex);
                return new BaseResponse
                    {
                        Status = RespsonseStatus.Error,
                        ErrorCode = ResponseError.DatabaseError,
                        ErrorMessage = ex.StackTrace
                    };
            }
        }

        public ListResponse<User> GetActiveUsers()
        {
            try
            {
                using (var entities = new UserEntities())
                {
                    var response = new ListResponse<User> {ErrorCode = ResponseError.None, Status = RespsonseStatus.Ok};
                    foreach (ActiveUsers activeUser in entities.ActiveUsers)
                        response.List.Add(new User
                            {
                                Name = entities.UserProfile.First(u => u.UserId == activeUser.UserId).UserName
                            });
                    return response;
                }
            }
            catch (DataException ex)
            {
                ErrorSignal.FromCurrentContext().Raise(ex);
                return new ListResponse<User>
                    {
                        Status = RespsonseStatus.Error,
                        ErrorCode = ResponseError.DatabaseError,
                        ErrorMessage = ex.StackTrace
                    };
            }
        }

        /// <summary>
        /// Nach dieser Zeit wird ein aktiver Benutzer als inaktiv erkannt und abgemeldet.
        /// </summary>
        private const int ACTIVE_USER_TIMEOUT_SECONDS = 15 * 60;

        static internal void CheckActiveUsers()
        {
            try
            {
                using (var entities = new UserEntities())
                {
                    var timeout = DateTime.Now.AddSeconds(-ACTIVE_USER_TIMEOUT_SECONDS);
                    var inactiveUsers = from user in entities.ActiveUsers
                                        where user.LoginDate < timeout
                                        select user;

                    entities.ActiveUsers.RemoveRange(inactiveUsers);
                    entities.SaveChanges();
                }
            }
            catch (Exception ex)
            {
                ErrorSignal.FromCurrentContext().Raise(ex);
            }
        }

        #region Private Helpers

        private static bool ExistUserAlready(string userName, UserEntities entities)
        {
            return entities.UserProfile.Any(profile => profile.UserName == userName);
        }

        private static BaseResponse CreateUserAndAccount(string userName, string password, UserEntities entities)
        {
            UserProfile user = entities.UserProfile.Add(new UserProfile
                {
                    UserName = userName,
                    webpages_Roles =
                        new List<webpages_Roles> {entities.webpages_Roles.First(role => role.RoleName == "Users")}
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

            return new BaseResponse {Status = RespsonseStatus.Ok, ErrorCode = ResponseError.None};
        }

        #region Debugging

        private static Exception GetInnerstException(Exception e)
        {
            Exception ex = e;
            while (ex.InnerException != null)
                ex = ex.InnerException;
            return ex;
        }

        private static bool HasValidationErrors(DbContext entities)
        {
            bool hasErrors = false;
            IEnumerable<DbEntityValidationResult> errors = entities.GetValidationErrors();

            foreach (DbEntityValidationResult error in errors)
            {
                hasErrors = true;
                Debug.WriteLine("Entity {0} is not valid! OriginalValues={1}; CurrentValues={2}", error.Entry.Entity,
                                error.Entry.OriginalValues, error.Entry.CurrentValues);

                foreach (DbValidationError validationError in error.ValidationErrors)
                    Debug.WriteLine("   {0}: {1}", validationError.PropertyName, validationError.ErrorMessage);
            }
            return hasErrors;
        }

        #endregion // Debuggin

        #endregion // Private Helpers
    }
}