
using System;
using System.Data.SqlClient; 

public partial class GetAllRoutesForMap : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        string sessionID = Request.QueryString["sessionID"];
        string phoneNumber = Request.QueryString["phoneNumber"];

        // our helper class to get data
        DbJsonReader reader = new DbJsonReader();

        Response.AppendHeader("Content-Type", "application/json");

        Response.Write(reader.getJsonString("prcGetAllRoutesForMap", "locations", 
            new SqlParameter("@sessionID", sessionID),
            new SqlParameter("@phoneNumber", phoneNumber)));
    }
}
