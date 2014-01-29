// Please leave the link below with the source code, thank you.
// http://www.websmithing.com/portal/Programming/tabid/55/articleType/ArticleView/articleId/6/Google-Map-GPS-Cell-Phone-Tracker-Version-2.aspx

using System;
using System.Data.SqlClient; 

public partial class DeleteRoute : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        string sessionID = Request.QueryString["sessionID"];
        string phoneNumber = Request.QueryString["phoneNumber"];

        // our helper class to get data
        DbXmlReader reader = new DbXmlReader();

        Response.AppendHeader("Content-Type", "text/xml");

        // actually we are getting a response back here, but the result will be a deleted route on the webpage
        Response.Write(reader.getXmlString("prcDeleteRoute",
            new SqlParameter("@sessionID", sessionID),
            new SqlParameter("@phoneNumber", phoneNumber)));

    }
}
