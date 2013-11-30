using Elmah;
using StudMap.Core;
using StudMap.Data.Entities;
using StudMap.Service.Services;
using System;
using System.Data;
using System.Web.Http;
using System.Web.Mvc;

namespace StudMap.Service.Controllers
{
    public class StudMapController : ApiController
    {
        protected void Execute(Action<MapsEntities> func, BaseResponse result)
        {
            Execute(() =>
            {
                using (var entities = new MapsEntities())
                {
                    func(entities);
                }
            }, result);
        }

        protected void ExecuteUsers(Action<UserEntities> func, BaseResponse result)
        {
            Execute(() =>
            {
                using (var entities = new UserEntities())
                {
                    func(entities);
                }
            }, result);
        }

        protected void Execute(Action func, BaseResponse result)
        {
            try
            {
                func();
            }
            catch (ServiceException ex)
            {
                result.Status = RespsonseStatus.Error;
                result.ErrorCode = ex.ErrorCode;
                ErrorSignal.FromCurrentContext().Raise(ex);
            }
            catch (DataException ex)
            {
                result.Status = RespsonseStatus.Error;
                result.ErrorCode = ResponseError.DatabaseError;
                result.ErrorMessage = ex.StackTrace;
                ErrorSignal.FromCurrentContext().Raise(ex);
            }
            catch (Exception ex)
            {
                result.Status = RespsonseStatus.Error;
                result.ErrorCode = ResponseError.UnknownError;
                result.ErrorMessage = ex.StackTrace;
                ErrorSignal.FromCurrentContext().Raise(ex);
            }
        }
    }
}
