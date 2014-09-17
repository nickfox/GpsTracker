
using System;
using System.IO;
using System.Net;
using System.Text;
using System.Data.SqlClient;

public partial class UpdateLocation : System.Web.UI.Page {

    protected void Page_Load(object sender, EventArgs e) {

        // http://localhost/gpstracker/UpdateLocation.aspx?longitude=-122.0214996&latitude=47.4758847&extrainfo=0&username=momo&distance=0.012262854&date=2014-09-16%2B17%253A49%253A57&direction=0&accuracy=65&phonenumber=867-5309&eventtype=android&sessionid=0a6dfd74-df4d-466e-b1b8-23234ef57512&speed=0&locationmethod=fused

        string latitude = Request.QueryString["latitude"];
        string longitude = Request.QueryString["longitude"];
        string speed = Request.QueryString["speed"];
        string direction = Request.QueryString["direction"];
        string distance = Request.QueryString["distance"];
        string date = Server.UrlDecode(Request.QueryString["date"]);

        // convert to DateTime format
        date = convertFromMySqlDate(date);

        string locationMethod = Server.UrlDecode(Request.QueryString["locationmethod"]);
        string phoneNumber = Request.QueryString["phonenumber"];
        string userName = Request.QueryString["username"];
        string sessionID = Request.QueryString["sessionid"];
        string accuracy = Request.QueryString["accuracy"];
        string eventType = Request.QueryString["eventtype"];
        string extraInfo = Request.QueryString["extrainfo"];

        // our helper class to update the database
        DbWriter dbw = new DbWriter();
        string returnValue = "";
        try {

            // update the database with our GPS data from the phone
            returnValue = dbw.updateDB("prcSaveGPSLocation",
                new SqlParameter("@latitude", latitude),
                new SqlParameter("@longitude", longitude),
                new SqlParameter("@speed", speed),
                new SqlParameter("@direction", direction),
                new SqlParameter("@distance", distance),
                new SqlParameter("@date", date),
                new SqlParameter("@locationMethod", locationMethod),
                new SqlParameter("@phoneNumber", phoneNumber),
                new SqlParameter("@userName", userName),
                new SqlParameter("@sessionID", sessionID),
                new SqlParameter("@accuracy", accuracy),
                new SqlParameter("@eventType", eventType),
                new SqlParameter("@extraInfo", extraInfo));

        }
        catch (Exception ex) {
            Response.Write(ex.Message);
        }

        Response.Write(date + ": " +  returnValue);
    }

    // convert to datetime string
    private string convertFromMySqlDate(string date) {
        DateTime dt = DateTime.ParseExact(date, "yyyy-MM-dd HH:mm:ss",
        System.Globalization.CultureInfo.InvariantCulture);
        return dt.ToString();
    }
}
