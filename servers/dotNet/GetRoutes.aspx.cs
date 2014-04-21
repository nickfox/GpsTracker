
using System;

public partial class GetRoutes : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        // our helper class to get data
        DbJsonReader reader = new DbJsonReader();

        Response.AppendHeader("Content-Type", "application/json");

        Response.Write(reader.getJsonString("prcGetRoutes", "routes"));
    }
}
