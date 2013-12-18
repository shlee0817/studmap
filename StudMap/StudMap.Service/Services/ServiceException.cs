using System;
using StudMap.Core;

namespace StudMap.Service.Services
{
    public class ServiceException : Exception
    {
        public ServiceException(ResponseError errorCode)
        {
            ErrorCode = errorCode;
        }

        public ResponseError ErrorCode { get; set; }
    }
}