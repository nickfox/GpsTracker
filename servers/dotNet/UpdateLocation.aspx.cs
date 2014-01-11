
using System;
using System.IO;
using System.Net;
using System.Text;
using System.Data.SqlClient;

public partial class UpdateLocation : System.Web.UI.Page {

    protected void Page_Load(object sender, EventArgs e) {
        string latitude = Request.Form["latitude"];
        string longitude = Request.Form["longitude"];
        string speed = Request.Form["speed"];
        string direction = Request.Form["direction"];
        string distance = Request.Form["distance"];
        string date = Server.UrlDecode(Request.Form["date"]);

        // convert to DateTime format
        date = convertFromMySqlDate(date);

        string locationMethod = Server.UrlDecode(Request.Form["locationmethod"]);
        string phoneNumber = Request.Form["phonenumber"];
        string sessionID = Request.Form["sessionid"];
        string accuracy = Request.Form["accuracy"];
        string eventType = Request.Form["eventtype"];
        string extraInfo = Request.Form["extrainfo"];

        // our helper class to update the database
        DbWriter dbw = new DbWriter();

        try {

            // update the database with our GPS data from the phone
            dbw.updateDB("prcSaveGPSLocation",
                new SqlParameter("@latitude", latitude),
                new SqlParameter("@longitude", longitude),
                new SqlParameter("@speed", speed),
                new SqlParameter("@direction", direction),
                new SqlParameter("@distance", distance),
                new SqlParameter("@date", date),
                new SqlParameter("@locationMethod", locationMethod),
                new SqlParameter("@phoneNumber", phoneNumber),
                new SqlParameter("@sessionID", sessionID),
                new SqlParameter("@accuracy", accuracy),
                new SqlParameter("@eventType", eventType),
                new SqlParameter("@extraInfo", extraInfo));
        }
        catch (Exception ex) {
            Response.Write(ex.Message);
        }
    }

    // convert to datetime string
    private string convertFromMySqlDate(string date) {
        DateTime dt = DateTime.ParseExact(date, "yyyy-MM-dd HH:mm:ss",
        System.Globalization.CultureInfo.InvariantCulture);
        return dt.ToString();
    }
}
