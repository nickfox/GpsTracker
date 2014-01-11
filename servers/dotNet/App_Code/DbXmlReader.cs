
using System;
using System.Configuration;
using System.Data;
using System.Data.SqlClient;
using System.Xml;


public class DbXmlReader
{
    /// <summary>
    /// Gets xml from MSSQL Server and returns it as a string. 
    /// </summary>
    public DbXmlReader()
    {
    }

    /// <summary>
    /// This method takes an optional list of paramters.
    /// </summary>
    public string getXmlString(string storedProcedureName, params SqlParameter[] spParameterList)
    {
        SqlConnection sqlConnection = null;
        XmlReader xmlReader = null;
        string xmlString = "";

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
            xmlReader = cmd.ExecuteXmlReader();
            xmlReader.Read();

            // we are getting XML straight from the stored procedure, so add it to 
            // our XML string
            while (xmlReader.ReadState != ReadState.EndOfFile)
            {
                xmlString = xmlReader.ReadOuterXml();
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
            if (xmlReader != null)
            {
                xmlReader.Close();
            }
        }

        return xmlString;
    }

    private string GetConnectionString() // stored in web.config
    {
        return ConfigurationManager.ConnectionStrings
        ["MSSQLConnectionString"].ConnectionString;
    }
}

