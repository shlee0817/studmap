using System.Collections.Generic;

namespace StudMap.Core
{
    public class ListResponse<Content> : BaseResponse
    {
        public ListResponse()
        {
            List = new List<Content>();           
        }

        public List<Content> List { get; set; }
    }
}
