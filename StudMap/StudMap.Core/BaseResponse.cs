namespace StudMap.Core
{
    public class BaseResponse
    {
        public BaseResponse(RespsonseStatus status = RespsonseStatus.Ok, ResponseError error = ResponseError.None)
        {
            Status = status;
            ErrorCode = error;
        }

        public void SetError(ResponseError error)
        {
            Status = RespsonseStatus.Error;
            ErrorCode = ResponseError.MapIdDoesNotExist;
        }

        public RespsonseStatus Status { get; set; }

        public ResponseError ErrorCode { get; set; }      
    }
}
