// Please leave the link below with the source code, thank you.
// http://www.websmithing.com/portal/Programming/tabid/55/articleType/ArticleView/articleId/6/Google-Map-GPS-Cell-Phone-Tracker-Version-2.aspx

using System;
using System.Configuration;
using System.Web.UI;

public partial class DisplayMap2 : System.Web.UI.Page {
    protected void Page_Load(object sender, EventArgs e) {
        // the application uses the Google map key on 2 different pages so the key is stored in
        // web.config. the code below gets the key and then injects this javascript code onto the
        // web page. It will be used to get the Google javascript library for maps. The reason we
        // go through all this is so that we can store the Google key in one location.
        
        string url = "http://maps.google.com/maps?file=api&amp;v=2.x&amp;key=" + GetGoogleMapKey();

        if (!ClientScript.IsClientScriptBlockRegistered("googleKey")) {
            String scriptString = "<script src=";
            scriptString += url;
            scriptString += " type=text/javascript></script>";
            ClientScript.RegisterClientScriptBlock(this.GetType(), "googleKey", scriptString);
        }
    }

    private string GetGoogleMapKey() { // stored in web.config
        return ConfigurationManager.AppSettings["GoogleMapKey"];
    }
}
