// Please leave the link below with the source code, thank you.
// http://www.websmithing.com/portal/Programming/tabid/55/articleType/ArticleView/articleId/6/Google-Map-GPS-Cell-Phone-Tracker-Version-2.aspx

using System;
using System.Configuration;
using System.Data;
using System.Data.SqlClient;


/// <summary>
/// Inserts or updates MSSQL using stored procedure.
/// Returns new identity column ID if successful or 0 if not.
/// </summary>
public class DbWriter
{
    public DbWriter()
    {
    }

    /// <summary>
    /// This method takes an optional list of parameters.
    /// </summary>
    public string updateDB(string storedProcedureName, params SqlParameter[] spParameterList)
    {
        string identityColumnID = "0";

        SqlConnection conn = new SqlConnection();
        conn.ConnectionString = GetConnectionString();

        SqlCommand cmd = new SqlCommand();
        cmd.Connection = conn;
        cmd.CommandText = storedProcedureName;
        cmd.CommandType = CommandType.StoredProcedure;

        // optional list of parameters for stored procedure
        if (spParameterList.Length > 0)
        {
            for (int i = 0; i < spParameterList.Length; i++)
            {
                cmd.Parameters.Add(spParameterList[i]);
            }
        }

        conn.Open();
        SqlDataReader reader = cmd.ExecuteReader();

        // if we have successfully executed the stored procedure then 
        // get the identityColumnID from the DB
        if (reader.Read())
        {
            identityColumnID = reader.GetInt32(0).ToString();
        }

        reader.Close();
        conn.Close();

        return identityColumnID;
    }

    private string GetConnectionString()
    {
        return ConfigurationManager.ConnectionStrings
            ["RemoteConnectionString"].ConnectionString;
    }
}


