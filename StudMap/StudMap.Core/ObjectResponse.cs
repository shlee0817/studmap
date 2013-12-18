namespace StudMap.Core
{
    public class ObjectResponse<T> : BaseResponse
    {
        public T Object { get; set; }
    }
}