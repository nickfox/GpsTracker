using System;
using System.Configuration;
using System.Data;
using System.Data.SqlClient;
using System.Text;

public class DbJsonReader
{
    /// <summary>
    /// creates a json array from ms sql server data
    /// </summary>
    public DbJsonReader()
    {
    }

    /// <summary>
    /// This method takes an optional list of paramters.
    /// </summary>
    public string getJsonString(string storedProcedureName, string jsonRootName, params SqlParameter[] spParameterList)
    {
        SqlConnection sqlConnection = null;
        SqlDataReader dataReader = null;
        StringBuilder jsonStringBuilder = new StringBuilder("");

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
            dataReader = cmd.ExecuteReader();

            jsonStringBuilder.Append("{ \"");
            jsonStringBuilder.Append(jsonRootName);
            jsonStringBuilder.Append("\": [");

            if (dataReader.HasRows) 
            {
                while (dataReader.Read())
                {
                    jsonStringBuilder.Append(dataReader.GetString(0));
                    jsonStringBuilder.Append(",");
                }
            }

            if (jsonStringBuilder.ToString().EndsWith(","))
            {
                jsonStringBuilder = jsonStringBuilder.Remove(jsonStringBuilder.Length - 1, 1);
            }

        }
        catch (Exception e) 
        {
            Console.WriteLine("updateDB error: " + e.Message);
        }
        finally 
        {
            if (sqlConnection != null) 
            {
                sqlConnection.Close();
            }
            if (dataReader != null) 
            {
                dataReader.Close();
            }
        }

        jsonStringBuilder.Append("] }");
        return jsonStringBuilder.ToString();
    }

    private string GetConnectionString() // stored in web.config
    {
        return ConfigurationManager.ConnectionStrings
        ["MSSQLConnectionString"].ConnectionString;
    }
}

