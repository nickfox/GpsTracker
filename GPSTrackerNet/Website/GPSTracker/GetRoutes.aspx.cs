// Please leave the link below with the source code, thank you.
// http://www.websmithing.com/portal/Programming/tabid/55/articleType/ArticleView/articleId/6/Google-Map-GPS-Cell-Phone-Tracker-Version-2.aspx

using System;

public partial class GetRoutes : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        // our helper class to get data
        DbXmlReader reader = new DbXmlReader();

        Response.AppendHeader("Content-Type", "text/xml");

        Response.Write(reader.getXmlString("prcGetRoutes"));
    }
}
