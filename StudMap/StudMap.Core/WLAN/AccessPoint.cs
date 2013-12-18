namespace StudMap.Core.WLAN
{
    public class AccessPoint
    {
        public AccessPoint()
        {
            MAC = string.Empty;
        }

        public int Id { get; set; }
        public string MAC { get; set; }
    }
}