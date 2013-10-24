namespace StudMap.Core
{
    public class BaseResponse
    {
        public BaseResponse(RespsonseStatus status = RespsonseStatus.Ok, ResponseError error = ResponseError.None)
        {
            Status = status;
            ErrorCode = error;
        }

        public RespsonseStatus Status { get; set; }

        public ResponseError ErrorCode { get; set; }      
    }
}
