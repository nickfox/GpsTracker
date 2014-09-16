
using System;
using System.Data.SqlClient; 

public partial class DeleteRoute : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        string sessionID = Request.QueryString["sessionID"];

        DbWriter writer = new DbWriter();
        
        try {
            writer.updateDB("prcDeleteRoute",
                new SqlParameter("@sessionID", sessionID));
        }
        catch (Exception ex) {
            Response.Write(ex.Message);
        }
    
        Response.Write("0");
    }
}
