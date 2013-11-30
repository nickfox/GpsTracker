
using System;
using System.Diagnostics;
using System.Collections.Generic;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Navigation;
using Microsoft.Phone.Controls;
using Microsoft.Phone.Shell;
using GPSTracker.Resources;

using System.Device.Location;
using System.Threading.Tasks;
using Windows.Devices.Geolocation;
using System.IO.IsolatedStorage;
using System.Net.Http;

namespace GPSTracker
{
    public partial class MainPage : PhoneApplicationPage
    {
        bool firstTimeGettingPosition = true;
        bool tracking = false;
        int httpCount = 0;
        string sessionID;
        double totalDistanceInMeters = 0;
        GeoCoordinate previousPosition;

        // Constructor
        public MainPage()
        {
            InitializeComponent();
            BuildApplicationBar();
        }

        // When the page is loaded, make sure that you have obtained the users consent to use their location
        protected override void OnNavigatedTo(System.Windows.Navigation.NavigationEventArgs e)
        {
            if (IsolatedStorageSettings.ApplicationSettings.Contains("LocationConsent"))
            {
                // User has already opted in or out of Location
                return;
            }
            else
            {
                MessageBoxResult result =
                    MessageBox.Show("This app accesses your phone's location. Is that ok?",
                    "Location",
                    MessageBoxButton.OKCancel);

                if (result == MessageBoxResult.OK)
                {
                    IsolatedStorageSettings.ApplicationSettings["LocationConsent"] = true;
                }
                else
                {
                    IsolatedStorageSettings.ApplicationSettings["LocationConsent"] = false;
                }

                IsolatedStorageSettings.ApplicationSettings.Save();

                UpdateAppBar();
            }
        }

        private async void sendGPS(string latitude, string longitude, string accuracy, string speed, 
            string direction, string locationMethod)
        {
            try
            {
                /* example url
                 http://www.websmithing.com/gpstracker2/getgooglemap2.php?lat=47.473349&lng=-122.025035&mph=137&dir=0&mi=0&
                 dt=2008-04-17%2012:07:02&lm=0&h=291&w=240&zm=12&dis=25&pn=momosity&sid=11137&acc=95&iv=yes&info=momostuff
                 */

                httpCount++;

                string defaultUploadWebsite = "http://www.websmithing.com/gpstracker2/getgooglemap3.php";
                HttpContent httpContent = new FormUrlEncodedContent(new[]
                {
                    new KeyValuePair<string, string>("lat", latitude),
                    new KeyValuePair<string, string>("lng", longitude),
                    new KeyValuePair<string, string>("mph", speed),
                    new KeyValuePair<string, string>("dir", direction),
                    new KeyValuePair<string, string>("dt", DateTime.Now.ToString(@"yyyy-MM-dd\%20HH:mm:ss")), // formatted for mysql datetime format),
                    new KeyValuePair<string, string>("lm", locationMethod),
                    new KeyValuePair<string, string>("dis", (totalDistanceInMeters / 1609).ToString()), // in miles
                    new KeyValuePair<string, string>("pn", "momo1"), //Windows.Phone.System.Analytics.HostInformation.PublisherHostId),
                    new KeyValuePair<string, string>("sid", sessionID),
                    new KeyValuePair<string, string>("acc", accuracy),
                    new KeyValuePair<string, string>("iv", "yes"),
                    new KeyValuePair<string, string>("info",  "windowsphone-" + httpCount.ToString())
                });

                HttpClient httpClient = new HttpClient();
                HttpResponseMessage responseMessage = await httpClient.PostAsync(defaultUploadWebsite, httpContent);
                responseMessage.EnsureSuccessStatusCode();

                Debug.WriteLine(String.Format("{0:d/M/yy h:mm:ss tt} ", DateTime.Now) + "sendGPS statusCode: " +
                    responseMessage.StatusCode + " httpCount: " + httpCount.ToString());

                if (!App.RunningInBackground)
                {
                    Dispatcher.BeginInvoke(() =>
                    {
                        HttpCountTextBlock.Text = " " + httpCount.ToString();
                    });
                }
            }
            catch (Exception e)
            {
                Debug.WriteLine(String.Format("{0:d/M/yy h:mm:ss tt} ", DateTime.Now + " sendGPS error: " + e.Message));
            }
        }

        private void TrackLocation_Click(object sender, RoutedEventArgs e)
        {
            if ((bool)IsolatedStorageSettings.ApplicationSettings["LocationConsent"] != true)
            {
                // The user has opted out of Location.
                StatusTextBlock.Text = " You have opted out of location. Use the app bar to turn location back on";
                return;
            } 

            if (!tracking)
            {
                // If not currently tacking, create a new Geolocator and set options.
                // Assigning the PositionChanged event handler begins location acquisition.

                if (App.Geolocator == null)
                {
                    // Use the app's global Geolocator variable
                    App.Geolocator = new Geolocator();
                }

                // note that you cannot use both ReportInterval and MovementThreshold. MovementThreshold will override ReportInterval
                App.Geolocator.ReportInterval = 60000; // 1 minute
                
                //App.Geolocator.DesiredAccuracy = PositionAccuracy.High;
                App.Geolocator.DesiredAccuracyInMeters = 100;
                
                App.Geolocator.StatusChanged += geolocator_StatusChanged;
                App.Geolocator.PositionChanged += geolocator_PositionChanged;

                tracking = true;
                sessionID = Guid.NewGuid().ToString();
                TrackLocationButton.Content = "stop tracking";
            }
            else
            {
                // To stop location acquisition, remove the position changed and status changed event handlers.
                App.Geolocator.PositionChanged -= geolocator_PositionChanged;
                App.Geolocator.StatusChanged -= geolocator_StatusChanged;
                App.Geolocator = null;

                httpCount = 0;
                totalDistanceInMeters = 0;
                tracking = false;
                TrackLocationButton.Content = "start tracking";
                StatusTextBlock.Text = " stopped";
            }
        }

        // The PositionChanged event is raised when new position data is available
        void geolocator_PositionChanged(Geolocator sender, PositionChangedEventArgs args)
        {
            string latitude = args.Position.Coordinate.Latitude.ToString("0.000000");
            string longitude = args.Position.Coordinate.Longitude.ToString("0.000000");
            string accuracy = args.Position.Coordinate.Accuracy.ToString();
            string speed = (args.Position.Coordinate.Speed / 1609 * 3600).ToString(); // in miles per hour
            string direction = args.Position.Coordinate.Heading.ToString();
            string locationMethod = args.Position.Coordinate.PositionSource.ToString();

            // note that this is the System.Device.Location.GeoCordinate class with a capital C that has GetDistanceTo method
            GeoCoordinate currentPosition = new GeoCoordinate(args.Position.Coordinate.Latitude, args.Position.Coordinate.Longitude);

            if (firstTimeGettingPosition)
            {
                firstTimeGettingPosition = false;
            }
            else 
            {
                Double distance = currentPosition.GetDistanceTo(previousPosition);
                totalDistanceInMeters += distance;
            }

            previousPosition = currentPosition;

           // if (args.Position.Coordinate.Accuracy < 100.0) // in meters
           // {
            this.sendGPS(latitude, longitude, accuracy, speed, direction, locationMethod);
           // }

            if (!App.RunningInBackground)
            {
                Debug.WriteLine(String.Format("{0:d/M/yy h:mm:ss tt}", DateTime.Now)
                    + " positionChanged foreground: "
                    + args.Position.Coordinate.PositionSource.ToString() + " accuracy: "
                    + args.Position.Coordinate.Accuracy.ToString() + "m");

                Dispatcher.BeginInvoke(() =>
                {
                    LatitudeTextBlock.Text = " " + latitude;
                    LongitudeTextBlock.Text = " " + longitude;
                    TimeTextBlock.Text = String.Format(" {0:d/M/yy h:mm:ss tt}", DateTime.Now);
                });
            }
            else
            {
                Debug.WriteLine(String.Format("{0:d/M/yy h:mm:ss tt}", DateTime.Now)
                    + " positionChanged background: "
                    + args.Position.Coordinate.PositionSource.ToString() + " accuracy: "
                    + args.Position.Coordinate.Accuracy.ToString() + "m");
            }
        }

        // The StatusChanged event is raised when the status of the location service changes.
        void geolocator_StatusChanged(Geolocator sender, StatusChangedEventArgs args)
        {       
            string status = "";

            switch (args.Status)
            {
                case PositionStatus.Ready:
                    // Location data is available.
                    status = " ready";
                    break;
                case PositionStatus.Initializing:
                    // The location provider is initializing. This is the status if a GPS is the source of location data and
                    // the GPS receiver does not yet have the required number of satellites in view to obtain an accurate position.
                    status = " initializing";
                    break;
                case PositionStatus.NoData:
                    // No location data is available from any location provider. LocationStatus will have this value if the 
                    // application calls GetGeopositionAsync or registers an event handler for the PositionChanged event, before data 
                    // is available from a location sensor. Once data is available LocationStatus transitions to the Ready state.
                    status = " no data";
                    break;
                case PositionStatus.Disabled:
                    // The location provider is disabled. This status indicates that the user has not granted the 
                    // application permission to access location.
                    status = " location is disabled in phone settings";
                    break;
                case PositionStatus.NotInitialized:
                    // An operation to retrieve location has not yet been initialized. LocationStatus will have this value if the 
                    // application has not yet called GetGeopositionAsync or registered an event handler for the PositionChanged event.
                    break;
                case PositionStatus.NotAvailable:
                    status = " not available";
                    // Not used in WindowsPhone, Windows desktop uses this value to signal that there is no hardware capable to 
                    // acquire location information.
                    break;
            }

            if (!App.RunningInBackground)
            {
                Dispatcher.BeginInvoke(() =>
                {
                    StatusTextBlock.Text = status;
                });
            }

            Debug.WriteLine(String.Format("{0:d/M/yy h:mm:ss tt} ", DateTime.Now) + "statusChanged:" + status);
        }

        // When the page is removed from the backstack, remove the event handlers to stop location acquisition
        protected override void OnRemovedFromJournal(System.Windows.Navigation.JournalEntryRemovedEventArgs e)
        {
            if (App.Geolocator != null)
            {
                App.Geolocator.PositionChanged -= geolocator_PositionChanged;
                App.Geolocator.StatusChanged -= geolocator_StatusChanged;
                App.Geolocator = null;
            }
        }

        // Allow the user to toggle opting in and out of location with an ApplicationBar menu item.
        private void BuildApplicationBar()
        {
            // Set the page's ApplicationBar to a new instance of ApplicationBar.
            ApplicationBar = new ApplicationBar();

            ApplicationBarMenuItem menuItem = new ApplicationBarMenuItem();
            menuItem.Text = "loading";
    
            menuItem.Click += menuItem_Click;
            ApplicationBar.MenuItems.Add(menuItem);
            ApplicationBar.IsMenuEnabled = true;
        }

        void menuItem_Click(object sender, EventArgs e)
        {
            if ((bool)IsolatedStorageSettings.ApplicationSettings["LocationConsent"] == true)
            {
                IsolatedStorageSettings.ApplicationSettings["LocationConsent"] = false;
            }
            else
            {
                IsolatedStorageSettings.ApplicationSettings["LocationConsent"] = false;
            }
            UpdateAppBar();
        }

        void UpdateAppBar()
        {
            ApplicationBarMenuItem menuItem = (ApplicationBarMenuItem)ApplicationBar.MenuItems[0];

            if ((bool)IsolatedStorageSettings.ApplicationSettings["LocationConsent"] == false)
            {
                menuItem.Text = "opt in to location";
            }
            else
            {
                menuItem.Text = "opt out of location";
            }
        }         
    }
}