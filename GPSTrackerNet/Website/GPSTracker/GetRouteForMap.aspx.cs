// Please leave the link below with the source code, thank you.
// http://www.websmithing.com/portal/Programming/tabid/55/articleType/ArticleView/articleId/6/Google-Map-GPS-Cell-Phone-Tracker-Version-2.aspx

using System;
using System.Data.SqlClient; 

public partial class GetRouteForMap : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        string sessionID = Request.QueryString["sessionID"];
        string phoneNumber = Request.QueryString["phoneNumber"];

        // our helper class to get data
        DbXmlReader reader = new DbXmlReader();

        Response.AppendHeader("Content-Type", "text/xml");

        // sessionID and phoneNumber are the unique identifiers for routes if the phoneNumber is unique. 
        // the phoneNumber field is a string field and anything can be put in that field, but for 
        // uniqueness, the actual phone number should be used
        Response.Write(reader.getXmlString("prcGetRouteForMap", 
            new SqlParameter("@sessionID", sessionID),
            new SqlParameter("@phoneNumber", phoneNumber)));
    }
}
