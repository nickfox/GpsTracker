
using System;
using System.Data.SqlClient; 

public partial class GetAllRoutesForMap : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        // our helper class to get data
        DbJsonReader reader = new DbJsonReader();

        Response.AppendHeader("Content-Type", "application/json");

        Response.Write(reader.getJsonString("prcGetAllRoutesForMap", "locations"));
    }
}
