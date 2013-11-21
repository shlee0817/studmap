namespace StudMap.Core.WLAN
{
    public class AccessPoint
    {
        public int Id { get; set; }
        public string MAC { get; set; }
        public int ReceivedSignalStrength { get; set; }

        public AccessPoint()
        {
            MAC = string.Empty;
        }
    }
}
