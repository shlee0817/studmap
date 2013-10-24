namespace StudMap.Core
{
    public class BaseResponse
    {
        public RespsonseStatus Status { get; set; }

        public ResponseError ErrorCode { get; set; }      
    }
}
