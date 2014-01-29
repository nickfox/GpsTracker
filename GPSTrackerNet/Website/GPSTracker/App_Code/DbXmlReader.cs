// Please leave the link below with the source code, thank you.
// http://www.websmithing.com/portal/Programming/tabid/55/articleType/ArticleView/articleId/6/Google-Map-GPS-Cell-Phone-Tracker-Version-2.aspx

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
        string xmlString = "";

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
        XmlReader xmlr = cmd.ExecuteXmlReader();
        xmlr.Read();

        // we are getting XML straight from the stored procedure, so add it to 
        // our XML string
        while (xmlr.ReadState != ReadState.EndOfFile)
        {
            xmlString = xmlr.ReadOuterXml();
        }                       

        return xmlString;
    }

    private string GetConnectionString() // stored in web.config
    {
        return ConfigurationManager.ConnectionStrings
        ["RemoteConnectionString"].ConnectionString;
    }
}

