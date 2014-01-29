// Please leave the link below with the source code, thank you.
// http://www.websmithing.com/portal/Programming/tabid/55/articleType/ArticleView/articleId/6/Google-Map-GPS-Cell-Phone-Tracker-Version-2.aspx

using System;
using System.Configuration;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Net;
using System.Text;
using System.Data.SqlClient;

public partial class GetGoogleMap2 : System.Web.UI.Page {
    private MemoryStream stream = null;
    private Image image = null;
    //private string key = "ABQIAAAAQ35Hu3xqOoeD50UMgBW0cBQEt3eA6mol2Np5q6SKw0EDVXpM9hRExX__LZW3RbLXHuLKZlwC0oypOw"; // websmithing
    private string url = "http://maps.google.com/staticmap";

    private string height;
    private string width;
    private string latitude;
    private string longitude;
    private string zoom;

    protected void Page_Load(object sender, EventArgs e) {
        height = Request.QueryString["h"];
        width = Request.QueryString["w"];
        latitude = Request.QueryString["lat"];
        longitude = Request.QueryString["lng"];
        zoom = Request.QueryString["zm"];

        string mph = Request.QueryString["mph"];
        string direction = Request.QueryString["dir"];
        string distance = Request.QueryString["dis"];
        string date = Server.UrlDecode(Request.QueryString["dt"]);

        // convert to DateTime format
        date = getDateFromJavaDate(date);

        string locationMethod = Server.UrlDecode(Request.QueryString["lm"]);

        string phoneNumber = Request.QueryString["pn"];
        string sessionID = Request.QueryString["sid"];
        string accuracy = Request.QueryString["acc"];
        string locationIsValid = Request.QueryString["iv"];
        string extraInfo = Request.QueryString["info"];

        // our helper class to update the database
        DbWriter dbw = new DbWriter();

        try {

            // update the database with our GPS data from the phone
            dbw.updateDB("prcSaveGPSLocation2",
                new SqlParameter("@lat", latitude),
                new SqlParameter("@lng", longitude),
                new SqlParameter("@mph", mph),
                new SqlParameter("@direction", direction),
                new SqlParameter("@distance", distance),
                new SqlParameter("@date", date),
                new SqlParameter("@locationMethod", locationMethod),

                new SqlParameter("@phoneNumber", phoneNumber),
                new SqlParameter("@sessionID", sessionID),
                new SqlParameter("@accuracy", accuracy),
                new SqlParameter("@locationIsValid", locationIsValid),
                new SqlParameter("@extraInfo", extraInfo));

            image = getMap();

            // here we take our Google map image and send it out as a .png 
            // all phones handle png images
            stream = new MemoryStream();
            image.Save(stream, ImageFormat.Png);
            Response.ContentType = "image/png";
            stream.WriteTo(Response.OutputStream);

            Response.Flush();
        }
        catch (Exception ex) {
            Response.Write(ex.Message);
        }
        finally {
            if (stream != null) {
                stream.Dispose();
            }
            if (image != null) {
                image.Dispose();
            }
        }
    }

    // using the parameters from the phone, build a url string and get the Google map image 
    private Image getMap() {
        try {
            StringBuilder sb = new StringBuilder(url);
            sb.Append("?markers=");
            sb.Append(latitude);
            sb.Append(",");
            sb.Append(longitude);
            sb.Append(",blueu&zoom=");
            sb.Append(zoom);
            sb.Append("&size=");
            sb.Append(width.ToString());
            sb.Append("x");
            sb.Append(height.ToString());
            sb.Append("&maptype=mobile&key=");
            sb.Append(GetGoogleMapKey());

            HttpWebRequest WebReq = (HttpWebRequest)WebRequest.Create(sb.ToString());
            HttpWebResponse WebResp = (HttpWebResponse)WebReq.GetResponse();
            Stream stream = WebResp.GetResponseStream();

            Image image = Image.FromStream(stream);

            stream.Close();

            return image;
        }
        catch (Exception e) {
            throw new Exception(e.Message);
        }
    }

    // parse the date string coming from the phone and convert it to a .net DateTime format
    private string getDateFromJavaDate(string date) {
        StringBuilder sb;
        if (date.IndexOf("G") > 0) // GMT time
        {
            sb = new StringBuilder(date.Substring(0, date.IndexOf("G")));
        }
        else if (date.IndexOf("U") > 0) // UTC time
        {
            sb = new StringBuilder(date.Substring(0, date.IndexOf("U")));
        }
        else {
            sb = new StringBuilder(date);
        }
        sb.Append(date.Substring(date.Length - 4, 4));
        DateTime dt = DateTime.ParseExact(sb.ToString(), "ddd MMM dd HH:mm:ss yyyy",
        System.Globalization.CultureInfo.InvariantCulture);

        return dt.ToString();
    }

    private string GetGoogleMapKey() { // stored in web.config
        return ConfigurationManager.AppSettings["GoogleMapKey"];
    }
}
