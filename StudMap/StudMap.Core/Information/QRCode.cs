namespace StudMap.Core.Information
{
    public class QRCode
    {
        public QRCode()
        {
            General = new General();
            StudMap = new StudMap();
        }

        public General General { get; set; }
        public StudMap StudMap { get; set; }
    }

    public class General
    {
        public string RoomName { get; set; }
        public string DisplayName { get; set; }
    }

    public class StudMap
    {
        public string Url = "http://code.google.com/p/studmap";
        public int NodeId { get; set; }
    }
}