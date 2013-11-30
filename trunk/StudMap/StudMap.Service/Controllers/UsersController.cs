using System;
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
using StudMap.Service.Services;

namespace StudMap.Service.Controllers
{
    public class UsersController : StudMapController
    {
        [HttpPost]
        public BaseResponse Register(string userName, string password)
        {
            var result = new BaseResponse();

            ExecuteUsers(entities =>
            {
                UserService.Register(entities, userName, password);
            }, result);

            return result;
        }

        [HttpPost]
        public BaseResponse Login(string userName, string password)
        {
            var result = new BaseResponse();

            ExecuteUsers(entities =>
            {
                UserService.Login(entities, userName, password);
            }, result);

            return result;
        }

        public BaseResponse Logout(string userName)
        {
            var result = new BaseResponse();

            ExecuteUsers(entities =>
            {
                UserService.Logout(entities, userName);
            }, result);

            return result;
        }

        [HttpGet]
        public ListResponse<User> GetActiveUsers()
        {
            var result = new ListResponse<User>();

            ExecuteUsers(entities =>
            {
                result.List = UserService.GetActiveUsers(entities);
            }, result);

            return result;
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
    }
}