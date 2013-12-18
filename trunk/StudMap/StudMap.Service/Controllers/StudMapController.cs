using System;
using System.Data;
using System.Web.Http;
using Elmah;
using StudMap.Core;
using StudMap.Data.Entities;
using StudMap.Service.Services;

namespace StudMap.Service.Controllers
{
    public class StudMapController : ApiController
    {
        private void ExecuteGeneric<TEntities>(Action<TEntities> func, BaseResponse result)
            where TEntities : IDisposable, new()
        {
            Execute(() =>
                {
                    using (var entities = new TEntities())
                    {
                        func(entities);
                    }
                }, result);
        }

        protected void ExecuteMaps(Action<MapsEntities> func, BaseResponse result)
        {
            ExecuteGeneric(func, result);
        }

        protected void ExecuteUsers(Action<UserEntities> func, BaseResponse result)
        {
            ExecuteGeneric(func, result);
        }

        protected void Execute(Action func, BaseResponse result)
        {
            try
            {
                func();
            }
            catch (ServiceException ex)
            {
                result.SetError(ex.ErrorCode);
                ErrorSignal.FromCurrentContext().Raise(ex);
            }
            catch (DataException ex)
            {
                result.SetError(ResponseError.DatabaseError, ex.StackTrace);
                ErrorSignal.FromCurrentContext().Raise(ex);
            }
            catch (Exception ex)
            {
                result.SetError(ResponseError.UnknownError, ex.StackTrace);
                ErrorSignal.FromCurrentContext().Raise(ex);
            }
        }
    }
}