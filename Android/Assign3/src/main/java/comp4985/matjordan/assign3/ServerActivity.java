/*---------------------------------------------------------------------------------------
--	Source File:		ServerActivity.java - A class that receives information from a client.
--
--	Classes:		ServerActivity - public class
--                  ServerThread - private class
--                  ClientThread - private class
--                  GetAddressTask - private class
--
--	Methods:
--				protected void onCreate(Bundle savedInstanceState)              (ServerActivity)
--				public void btn_server_begin_click(View view)                   (ServerActivity)
--              public void btn_server_stop_click(View view)                    (ServerActivity)
--              private void HandlePacket(ClientThread client, IPacket packet)  (ServerActivity)
--              private void Update_Client(ClientThread client)                 (ServerActivity)
--              public void btn_normal_map(View view)                           (ServerActivity)
--              public void btn_hybrid_map(View view)                           (ServerActivity)
--
--              public ServerThread(ServerActivity parent, int port)            (ServerThread)
--              public void stop_server()                                       (ServerThread)
--              public void run()                                               (ServerThread)
--
--              public ClientThread(ServerActivity parent, int port)            (ClientThread)
--              public void run()                                               (ClientThread)
--
--              public GetAddressTask(Context context)                          (GetAddressTask)
--              protected void onPostExecute(String address)                    (GetAddressTask)
--              protected String doInBackground(LatLng... params)               (GetAddressTask)
--
--
--	Date:			March 4, 2014
--
--	Revisions:		(Date and Description)
--
--	Designer:		Mat Siwoski
--                  Jordan Marling
--
--	Programmer:		Mat Siwoski
--                  Jordan Marling
--
--	Notes:
--	This class allows a user to specify a port for the server to start on. The user can
--  then start the server which waits for clients to connect to it. Once the client connects
--  the server starts a thread to listen for packets coming in from the client. It then
--  updates the google maps API according to information from the client and displays the
--  map.
--
--  The user can also specify if text to speech is to be used.
--
--
---------------------------------------------------------------------------------------*/
package comp4985.matjordan.assign3;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ServerActivity extends Activity {

    private ServerThread server;
    private TextToSpeech tts;
    private boolean use_tts = false;

    static final LatLng BURNABY = new LatLng(49.2484, -123.0014);
    private GoogleMap map;

    /*------------------------------------------------------------------------------------------------------------------
    -- METHOD:		    onCreate
    --
    -- DATE:			March 4, 2014
    --
    -- REVISIONS:		(Date and Description)
    --
    -- DESIGNER:		Mat Siwoski
    --
    -- PROGRAMMER:		Mat Siwoski
    --
    -- INTERFACE:		protected void onCreate(Bundle savedInstanceState)
    --                                  savedInstance: if the application was paused in the background.
    --
    -- RETURNS:			void.
    --
    -- NOTES:			This function is called when the activity is created. It initializes information.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        TextView ip_address = (TextView)findViewById(R.id.server_ip);

        WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        int ipaddress = wifiManager.getConnectionInfo().getIpAddress();
        String msg;
        if (ipaddress == 0) {
            msg = "Not connected to wireless internet.";
        }
        else {
            msg = "IP Address: " + String.format(
                    "%d.%d.%d.%d",
                    (ipaddress & 0xff),
                    (ipaddress >> 8 & 0xff),
                    (ipaddress >> 16 & 0xff),
                    (ipaddress >> 24 & 0xff));
        }

        ip_address.setText(msg);

        tts=new TextToSpeech(getApplicationContext(),new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR){
                    tts.setLanguage(Locale.CANADA);
                }
            }
        });
    }

    /*------------------------------------------------------------------------------------------------------------------
    -- METHOD:		    btn_server_begin_click
    --
    -- DATE:			March 4, 2014
    --
    -- REVISIONS:		(Date and Description)
    --
    -- DESIGNER:		Mat Siwoski
    --
    -- PROGRAMMER:		Mat Siwoski
    --
    -- INTERFACE:		public void btn_server_begin_click(View view)
    --                                  view: the view of the button clicked
    --
    -- RETURNS:			void.
    --
    -- NOTES:			This function is called when the start button is pressed.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    public void btn_server_begin_click(View view) {

        int port;
        EditText port_widget = (EditText)findViewById(R.id.server_port);
        try {
            port = Integer.parseInt(port_widget.getText().toString());
            CheckBox cb = (CheckBox)findViewById(R.id.server_speech);

            use_tts = cb.isChecked();

            server = new ServerThread(this, port);
            server.start();

            setContentView(R.layout.fragment_main);

            map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            Marker burnaby = map.addMarker(new MarkerOptions().position(BURNABY)
                    .title("Burnaby"));
            map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(BURNABY)            // Sets the center of the map to BURNABY
                    .zoom(15)                   // Sets the zoom
                    .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


            map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {

                    // Getting view from the layout file info_window_layout
                    View v = getLayoutInflater().inflate(R.layout.snippet_layout, null);

                    // Getting reference to the TextView to set title

                    TextView name = (TextView) v.findViewById(R.id.snippet_name);
                    TextView address = (TextView) v.findViewById(R.id.snippet_address);
                    TextView pressure = (TextView) v.findViewById(R.id.snippet_pressure);
                    TextView coords = (TextView) v.findViewById(R.id.snippet_coords);
                    TextView time = (TextView) v.findViewById(R.id.snippet_time);

                    String[] lines = marker.getSnippet().split("\n");

                    name.setText(marker.getTitle());

                    if (lines.length > 3) {
                        address.setText(lines[0]);
                        pressure.setText(lines[1]);
                        coords.setText(lines[2]);
                        time.setText(lines[3]);
                    }


                    return v;

                }
            });


        } catch(NumberFormatException nfe) {
        } catch(IOException ioe) {
        }


//        Button start = (Button)findViewById(R.id.btn_server_begin);
//        start.setEnabled(false);
//
//        Button stop = (Button)findViewById(R.id.btn_server_stop);
//        stop.setEnabled(true);

    }

    /*------------------------------------------------------------------------------------------------------------------
    -- METHOD:		    btn_server_stop_click
    --
    -- DATE:			March 4, 2014
    --
    -- REVISIONS:		(Date and Description)
    --
    -- DESIGNER:		Mat Siwoski
    --
    -- PROGRAMMER:		Mat Siwoski
    --
    -- INTERFACE:		public void btn_server_stop_click(View view)
    --                                  view: the view of the button clicked
    --
    -- RETURNS:			void.
    --
    -- NOTES:			This function is called when the stop button is pressed.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    public void btn_server_stop_click(View view) {
        if (server != null) {
            Button start = (Button)findViewById(R.id.btn_server_begin);
            start.setEnabled(true);

            Button stop = (Button)findViewById(R.id.btn_server_stop);
            stop.setEnabled(false);


            server.stop_server();
        }
    }


    /*------------------------------------------------------------------------------------------------------------------
        -- METHOD:		    HandlePacket
        --
        -- DATE:			March 4, 2014
        --
        -- REVISIONS:		(Date and Description)
        --
        -- DESIGNER:		Jordan Marling
        --
        -- PROGRAMMER:		Jordan Marling
        --
        -- INTERFACE:		private void HandlePacket(ClientThread client, IPacket packet)
        --                                  client: the client object that sent the packet
        --                                  packet: the packet sent
        --
        -- RETURNS:			void.
        --
        -- NOTES:			This function is called to handle a packet recieved from a client.
        --
        ----------------------------------------------------------------------------------------------------------------------*/
    private void HandlePacket(ClientThread client, IPacket packet) {

        //System.out.println("PACKET ID: " + packet.getID());
        //username
        if (packet.getID() == 0) {
            UsernamePacket p = (UsernamePacket)packet;
            System.out.println("USERNAME: " + p.username);

            client.username = p.username;

        }
        //gps
        else if (packet.getID() == 1) {
            GPSPacket p = (GPSPacket)packet;

            System.out.println("CLIENT LOCATION: " + p.longitude + ", " + p.latitude);

            client.time = p.time;

            if (client.latlngs.size() == 0) {

                client.latlngs.add(new LatLng(p.latitude, p.longitude));
                client.updated_latlng = true;
            }
            else {

                LatLng prev = client.latlngs.get(client.latlngs.size() - 1);


                if (prev.longitude != p.longitude ||
                    prev.latitude != p.latitude) {


                    client.latlngs.add(new LatLng(p.latitude, p.longitude));
                    client.updated_latlng = true;


                }
            }

//            client.longitude = p.longitude;
//            client.latitude = p.latitude;
        }
        //barometer
        else if (packet.getID() == 2) {
            BarometerPacket p = (BarometerPacket)packet;

            //System.out.println("CLIENT BAROMETER: " + p.pressure);
            client.pressure = p.pressure;
        }
        //image
        else if (packet.getID() == 3) {
            ImagePacket p = (ImagePacket)packet;



            client.icon = BitmapFactory.decodeByteArray(p.byteArray, 0, p.byteArray.length);
            if (client.icon == null) {
                System.out.println("ERROR DECODING!");
            }
        }

        Update_Client(client);
    }

    /*------------------------------------------------------------------------------------------------------------------
        -- METHOD:		    Update_Client
        --
        -- DATE:			March 4, 2014
        --
        -- REVISIONS:		(Date and Description)
        --
        -- DESIGNER:		Jordan Marling
        --
        -- PROGRAMMER:		Jordan Marling
        --
        -- INTERFACE:		private void Update_Client(ClientThread client)
        --                                  client: the client object that needs to be updated on
        --                                          google maps.
        --
        -- RETURNS:			void.
        --
        -- NOTES:			This function is called to update a client on google maps.
        --
        ----------------------------------------------------------------------------------------------------------------------*/
    private void Update_Client(ClientThread client) {

        if (client.marker == null) {
            if (client.latlngs.size() > 0) {

                LatLng loc = client.latlngs.get(client.latlngs.size() - 1);

                client.marker = map.addMarker(new MarkerOptions().position(loc)
                        .title(client.username));

                if (client.icon != null) {
                    client.marker.setIcon(BitmapDescriptorFactory.fromBitmap(client.icon));
                }

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(loc)            // Sets the center of the map to the client
                        .zoom(15)                   // Sets the zoom
                        .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                if (use_tts) {
                    boolean speakingEnd;
                    do{
                        speakingEnd = tts.isSpeaking();
                    } while (speakingEnd);
                    tts.speak(client.username + " Has joined the session.", TextToSpeech.QUEUE_FLUSH, null);
                }
                else {
                    Toast.makeText(this, client.username + " Has joined the session.", 1000).show();
                }

            }
        }
        else {

            String address = "";
            LatLng loc = client.latlngs.get(client.latlngs.size() - 1);

            address = new GetAddressTask(this.getApplicationContext()).doInBackground(loc);

            if (client.updated_latlng) {
                PolylineOptions polylines = new PolylineOptions();
                polylines.width(10);
                polylines.color(Color.BLUE);

                for (LatLng location : client.latlngs) {
                    polylines.add(location);
                }

                map.addPolyline(polylines);
                if (!client.prev_address.equals(address)) {
                    String text = client.username  + " has changed location to " + address;

                    if (use_tts) {
                        boolean speakingEnd;
                        do{
                            speakingEnd = tts.isSpeaking();
                        } while (speakingEnd);

                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                    }
                    else {
                        Toast.makeText(this, text, 1000).show();
                    }
                }
                client.updated_latlng = false;
                client.prev_address = address;
            }

            String coords = "";

            if (client.latlngs.size() > 0) {
                LatLng ll = client.latlngs.get(client.latlngs.size() - 1);

                coords = ll.latitude + ", " + ll.longitude;
            }

            String snippet = "";
            snippet += "Pressure: " + client.pressure + "kPa";
            snippet += "\n" + address;
            snippet += "\nLat/Lng: " + coords;
            snippet += "\nTime: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(client.time));

            client.marker.setPosition(loc);
            client.marker.setSnippet(snippet);

            client.marker.hideInfoWindow();
            client.marker.showInfoWindow();
        }
    }


    /*------------------------------------------------------------------------------------------------------------------
        -- METHOD:		    btn_normal_map
        --
        -- DATE:			March 4, 2014
        --
        -- REVISIONS:		(Date and Description)
        --
        -- DESIGNER:		Mat Siwoski
        --
        -- PROGRAMMER:		Mat Siwoski
        --
        -- INTERFACE:		public void btn_normal_map(View view)
        --                                  view: the view of the button clicked
        --
        -- RETURNS:			void.
        --
        -- NOTES:			This function is called when the normal button is pressed on google maps.
        --
        ----------------------------------------------------------------------------------------------------------------------*/
    public void btn_normal_map(View view){
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    /*------------------------------------------------------------------------------------------------------------------
        -- METHOD:		    btn_hybrid_map
        --
        -- DATE:			March 4, 2014
        --
        -- REVISIONS:		(Date and Description)
        --
        -- DESIGNER:		Mat Siwoski
        --
        -- PROGRAMMER:		Mat Siwoski
        --
        -- INTERFACE:		public void btn_hybrid_map(View view)
        --                                  view: the view of the button clicked
        --
        -- RETURNS:			void.
        --
        -- NOTES:			This function is called when the hybrid button is pressed on google maps.
        --
        ----------------------------------------------------------------------------------------------------------------------*/
    public void btn_hybrid_map(View view){
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }


    private class ServerThread extends Thread {

        private ServerActivity parent;
        private ServerSocket listen;
        private int port;
        private boolean running = true;

        /*------------------------------------------------------------------------------------------------------------------
        -- METHOD:		    ServerThread
        --
        -- DATE:			March 4, 2014
        --
        -- REVISIONS:		(Date and Description)
        --
        -- DESIGNER:		Mat Siwoski
        --
        -- PROGRAMMER:		Mat Siwoski
        --
        -- INTERFACE:		public ServerThread(ServerActivity parent, int port)
        --                                  parent: the parent object
        --                                  port: the port to run on.
        --
        -- RETURNS:			(nothing, constructor)
        --
        -- NOTES:			This function initializes the packet information and the ServerThread
        --                  data members.
        --
        ----------------------------------------------------------------------------------------------------------------------*/
        public ServerThread(ServerActivity parent, int port) throws IOException {
            this.parent = parent;
            this.port = port;
            PacketHandler.InitializePackets();
        }

        /*------------------------------------------------------------------------------------------------------------------
        -- METHOD:		    stop_server
        --
        -- DATE:			March 4, 2014
        --
        -- REVISIONS:		(Date and Description)
        --
        -- DESIGNER:		Mat Siwoski
        --
        -- PROGRAMMER:		Mat Siwoski
        --
        -- INTERFACE:		public void stop_server()
        --
        -- RETURNS:			void.
        --
        -- NOTES:			This function is called to stop the server.
        --
        ----------------------------------------------------------------------------------------------------------------------*/
        public void stop_server() {
            try {
                listen.close();
                running = false;
            } catch(IOException e) {
                System.out.println("Error closing listener.");
            }
        }

        /*------------------------------------------------------------------------------------------------------------------
        -- METHOD:		    run
        --
        -- DATE:			March 4, 2014
        --
        -- REVISIONS:		(Date and Description)
        --
        -- DESIGNER:		Mat Siwoski
        --
        -- PROGRAMMER:		Mat Siwoski
        --
        -- INTERFACE:		public void run()
        --
        -- RETURNS:			void.
        --
        -- NOTES:			This function is in a separate thread listening for incoming clients.
        --
        ----------------------------------------------------------------------------------------------------------------------*/
        public void run() {

            try {
                System.out.println("Running");
                listen = new ServerSocket(port);
                //listen.setSoTimeout(10000);

                while (running) {

                    try {

                        System.out.println("Listening");
                        Socket client = listen.accept();
                        System.out.println("Accepted Client");

                        new ClientThread(parent, client).start();

                    } catch(IOException ioe) {

                    }

                }
            } catch(IOException ioe) {

            }

        }

    }

    private class ClientThread extends Thread {

        private Socket client;
        private ServerActivity parent;
        public boolean running = false;


        public String username;
        public double pressure;
        public Marker marker;
        public long time = 0;

        public List<LatLng> latlngs = new ArrayList<LatLng>();
        public boolean updated_latlng = false;
        public String prev_address = "";

        public Bitmap icon;

        /*------------------------------------------------------------------------------------------------------------------
        -- METHOD:		    ClientThread
        --
        -- DATE:			March 4, 2014
        --
        -- REVISIONS:		(Date and Description)
        --
        -- DESIGNER:		Jordan Marling
        --
        -- PROGRAMMER:		Jordan Marling
        --
        -- INTERFACE:		public ClientThread(ServerActivity p, Socket c)
        --                                  p: the parent object
        --                                  c: the client socket.
        --
        -- RETURNS:			(nothing, constructor)
        --
        -- NOTES:			This function sets the username of the client to the IP address and
        --                  initializes data members.
        --
        ----------------------------------------------------------------------------------------------------------------------*/
        public ClientThread(ServerActivity p, Socket c) {
            client = c;
            this.parent = p;

            this.username = c.getInetAddress().getHostAddress();
        }

        /*------------------------------------------------------------------------------------------------------------------
        -- METHOD:		    run
        --
        -- DATE:			March 4, 2014
        --
        -- REVISIONS:		(Date and Description)
        --
        -- DESIGNER:		Jordan Marling
        --
        -- PROGRAMMER:		Jordan Marling
        --
        -- INTERFACE:		public void run()
        --
        -- RETURNS:			(nothing, constructor)
        --
        -- NOTES:			This function runs the client on its own thread and reads in packets.
        --                  the packets are then sent to the main UI thread and dealth with there
        --                  to be displayed.
        --
        ----------------------------------------------------------------------------------------------------------------------*/
        public void run() {

            try {
                DataInputStream istream = new DataInputStream(client.getInputStream());
                running = true;
                while (running) {

                    final IPacket packet = PacketHandler.ReadPacket(istream);

                    if (packet != null) {
                        parent.runOnUiThread(new Runnable() {
                            public void run() {
                                parent.HandlePacket(ClientThread.this, packet);
                            }
                        });
                    }
                    else {
                        System.out.println("Could not find packet.");
                        client.close();
                        running = false;
                    }
                }

            } catch(IOException e) {
                System.out.println("CLIENT IO Exception: " + e.getMessage());
            }

            parent.runOnUiThread(new Runnable() {
                public void run() {

                    String text = username  + " has left the session.";

                    if (use_tts) {
                        boolean speakingEnd;
                        do{
                            speakingEnd = tts.isSpeaking();
                        } while (speakingEnd);

                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                    }
                    else {
                        Toast.makeText(parent, text, 1000).show();
                    }

                    marker.remove();
                }
            });

        }

    }


    /*------------------------------------------------------------------------------------------------------------------
        -- CLASS:		    GetAddressTask
        --
        -- DATE:			March 4, 2014
        --
        -- REVISIONS:		(Date and Description)
        --                  March 4, 2014 - Jordan Marling - changed the class from taking a Location
        --                                                   to take a LatLng object instead.
        --
        -- DESIGNER:		tutorialpoints.com
        --
        -- PROGRAMMER:		tutorialpoints.com
        --
        -- NOTES:			This class converts a latitude and longitude into a street address.
        --
        ----------------------------------------------------------------------------------------------------------------------*/
    private class GetAddressTask extends AsyncTask<LatLng, Void, String> {
        Context mContext;
        public GetAddressTask(Context context) {
            super();
            mContext = context;
        }

        /*
         * When the task finishes, onPostExecute() displays the address.
         */
        @Override
        protected void onPostExecute(String address) {
            // Display the current address in the UI
            Toast.makeText(getApplicationContext(), address, Toast.LENGTH_SHORT).show();
        }
        @Override
        protected String doInBackground(LatLng... params) {
            Geocoder geocoder =
                    new Geocoder(mContext, Locale.getDefault());
            // Get the current location from the input parameter list
            LatLng loc = params[0];
            // Create a list to contain the result address
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(loc.latitude,
                        loc.longitude, 1);
            } catch (IOException e1) {
                Log.e("LocationSampleActivity",
                        "IO Exception in getFromLocation()");
                e1.printStackTrace();
                return ("IO Exception trying to get address");
            } catch (IllegalArgumentException e2) {
                // Error message to post in the log
                String errorString = "Illegal arguments " +
                        Double.toString(loc.latitude) +
                        " , " +
                        Double.toString(loc.longitude) +
                        " passed to address service";
                Log.e("LocationSampleActivity", errorString);
                e2.printStackTrace();
                return errorString;
            }
            // If the reverse geocode returned an address
            if (addresses != null && addresses.size() > 0) {
                // Get the first address
                Address address = addresses.get(0);
            /*
            * Format the first line of address (if available),
            * city, and country name.
            */
                String addressText = String.format(
                        "%s, %s, %s",
                        // If there's a street address, add it
                        address.getMaxAddressLineIndex() > 0 ?
                                address.getAddressLine(0) : "",
                        // Locality is usually a city
                        address.getLocality(),
                        // The country of the address
                        address.getCountryName());
                // Return the text
                return addressText;
            } else {
                return "No address found";
            }
        }
    }

}
