using StudMap.Core;
using System;

namespace StudMap.Service.Services
{
    public class ServiceException : Exception
    {
        public ResponseError ErrorCode { get; set; }

        public ServiceException(ResponseError errorCode)
        {
            ErrorCode = errorCode;
        }
    }
}