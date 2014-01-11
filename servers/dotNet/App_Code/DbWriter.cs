
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
        SqlDataReader sqlDataReader = null;;
        SqlConnection sqlConnection = null;

        try
        {

            sqlConnection = new SqlConnection();
            sqlConnection.ConnectionString = GetConnectionString();

            SqlCommand cmd = new SqlCommand();
            cmd.Connection = sqlConnection;
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

            sqlConnection.Open();
            sqlDataReader = cmd.ExecuteReader();

            // if we have successfully executed the stored procedure then 
            // get the identityColumnID from the DB
            if (sqlDataReader.Read())
            {
                identityColumnID = sqlDataReader.GetInt32(0).ToString();
            }
        } catch (Exception e) {
            Console.WriteLine("updateDB error: " + e.Message);
        } finally {
            if (sqlConnection != null)
            {
                sqlConnection.Close();
            }
            if (sqlDataReader != null)
            {
                sqlDataReader.Close();
            }
        }

        return identityColumnID;
    }

    private string GetConnectionString()
    {
        return ConfigurationManager.ConnectionStrings
            ["MSSQLConnectionString"].ConnectionString;
    }
}


