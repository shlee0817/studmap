namespace StudMap.Core.Information
{
    public class QRCode
    {
        public GeneralData GeneralData { get; set; }
        public StudMapData StudMapData { get; set; }

        public QRCode()
        {
            GeneralData = new GeneralData();
            StudMapData = new StudMapData();
        }
    }

    public class GeneralData
    {
        public string RoomName { get; set; }
        public string DisplayName { get; set; }
    }

    public class StudMapData
    {
        public int NodeId { get; set; }
        public string Url = "http://code.google.com/p/studmap";
    }
}
