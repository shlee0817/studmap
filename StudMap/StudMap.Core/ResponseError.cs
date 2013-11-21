namespace StudMap.Core
{
    public enum ResponseError
    {
        None = 0,

        #region General
        DatabaseError = 1,
        #endregion

        #region Register
        UserNameDuplicate = 101,
        UserNameInvalid = 102,
        PasswordInvalid = 103,
        #endregion

        #region Login
        LoginInvalid = 110,
        #endregion

        #region Maps
        MapIdDoesNotExist = 201,
        FloorIdDoesNotExist = 202,
        NodeIdDoesNotExist = 203,
        #endregion

        #region Navigation
        NoRouteFound = 301,
        StartNodeNotFound = 302,
        EndNodeNotFound = 303,
        #endregion

        #region Information
        PoiTypeIdDoesNotExist = 401,
        NFCTagDoesNotExist = 402,
        QRCodeDosNotExist = 403,
        PoiDoesNotExist = 404,
        #endregion // Information
    }
}
