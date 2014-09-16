
using System;
using System.Data.SqlClient; 

public partial class GetRouteForMap : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        string sessionID = Request.QueryString["sessionID"];

        // our helper class to get data
        DbJsonReader reader = new DbJsonReader();

        Response.AppendHeader("Content-Type", "application/json");

        Response.Write(reader.getJsonString("prcGetRouteForMap", "locations", 
            new SqlParameter("@sessionID", sessionID)));
    }
}
