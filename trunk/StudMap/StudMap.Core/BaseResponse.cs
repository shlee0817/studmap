namespace StudMap.Core
{
    public class BaseResponse
    {
        public BaseResponse(RespsonseStatus status = RespsonseStatus.Ok, ResponseError error = ResponseError.None, string errorMessage = "")
        {
            Status = status;
            ErrorCode = error;
            ErrorMessage = errorMessage;
        }

        public void SetError(ResponseError error)
        {
            Status = RespsonseStatus.Error;
            ErrorCode = ResponseError.MapIdDoesNotExist;
        }

        public void SetError(ResponseError error, string errorMessage)
        {
            SetError(error);
            ErrorMessage = errorMessage;
        }

        public RespsonseStatus Status { get; set; }

        public ResponseError ErrorCode { get; set; }

        public string ErrorMessage { get; set; }
    }
}
