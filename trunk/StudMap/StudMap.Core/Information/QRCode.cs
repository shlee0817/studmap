namespace StudMap.Core.Information
{
    public class QRCode
    {
        public General General { get; set; }
        public StudMap StudMap { get; set; }

        public QRCode()
        {
            General = new General();
            StudMap = new StudMap();
        }
    }

    public class General
    {
        public string RoomName { get; set; }
        public string DisplayName { get; set; }
    }

    public class StudMap
    {
        public int NodeId { get; set; }
        public string Url = "http://code.google.com/p/studmap";
    }
}
