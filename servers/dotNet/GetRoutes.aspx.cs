
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
