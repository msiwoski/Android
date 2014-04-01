/*---------------------------------------------------------------------------------------
--	Source File:		ClientActivity.java - A class that sends information to the server.
--
--	Classes:		ClientActivity - public class
--                  ClientThread - private class
--
--	Methods:
--				protected void onCreate(Bundle savedInstanceState)                  (ClientActivity)
--				public void btn_client_begin_click(View view)                       (ClientActivity)
--				public void btn_client_stop_click(View view)                        (ClientActivity)
--              public final void onAccuracyChanged(Sensor sensor, int accuracy)    (ClientActivity)
--              public final void onSensorChanged(SensorEvent event)                (ClientActivity)
--              public void onProviderEnabled(String s)                             (ClientActivity)
--              public void onProviderDisabled(String s)                            (ClientActivity)
--              public void onLocationChanged(final Location l)                     (ClientActivity)
--              public void onStatusChanged(String s, int i, Bundle b)              (ClientActivity)
--              private int findFrontFacingCamera()                                 (ClientActivity)
--              public void btnCapturePicture(View view)                            (ClientActivity)
--              private void captureImage()                                         (ClientActivity)
--              public Uri getOutputMediaFileUri(int type)                          (ClientActivity)
--              private static File getOutputMediaFile(int type)                    (ClientActivity)
--              protected void onActivityResult(int, int, Intent)                   (ClientActivity)
--              private void previewCapturedImage()                                 (ClientActivity)
--              Bitmap ShrinkBitmap(String file, int width, int height)             (ClientActivity)
--
--              public ClientThread(String server, int port, String username)       (ClientThread)
--              public void stop_client()                                           (ClientThread)
--              public void run()                                                   (ClientThread)
--              public synchronized void Write(IPacket packet)                      (ClientThread)
--
--
--	Date:			March 4, 2014
--
--	Revisions:		(Date and Description)
--
--	Designer:		Jordan Marling
--                  Mat Siwoski
--
--	Programmer:		Jordan Marling
--                  Mat Siwoski
--
--	Notes:
--	This class displays the client activity view. It prompts a user for a username,
--  IP Address of a server and port. It allows the client to click a button to change
--  the location provider. The user clicks "Start" to connect and start sending
--  their GPS location, barometer information, and a username if supplied.
--
--
---------------------------------------------------------------------------------------*/

package comp4985.matjordan.assign3;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClientActivity extends ActionBarActivity implements SensorEventListener, LocationListener {

    private static final String IMAGE_DIRECTORY_NAME = "Assign3";
    private Uri fileUri; // file url to store image/video
    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private ImageView imgPreview;
    private Button btnCapturePicture;
    private final static String DEBUG_TAG = "MakePhotoActivity";
    private Camera camera;
    private int cameraId = -1;

    private byte[] imageArray = null;



    private ClientThread client;
    private LocationManager locationManager;

    private SensorManager sensorManager;
    private Sensor pressure;

    private long lastBarometer = 0;

    /*------------------------------------------------------------------------------------------------------------------
    -- METHOD:		    onCreate
    --
    -- DATE:			March 4, 2014
    --
    -- REVISIONS:		(Date and Description)
    --
    -- DESIGNER:		Jordan Marling
    --
    -- PROGRAMMER:		Jordan Marling
    --
    -- INTERFACE:		protected void onCreate(Bundle savedInstanceState)
    --
    -- RETURNS:			void.
    --
    -- NOTES:			This function is called when the activity is created. It initializes values.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

        EditText ip = (EditText)findViewById(R.id.client_ip_address);
        ip.setText("192.168.1.109");

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);

        Spinner spinner_providers = (Spinner)findViewById(R.id.client_providers);

        List<String> providers = locationManager.getProviders(criteria, true);

        if (providers.isEmpty()) {
            providers.add("None");
        }

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, providers.toArray());
        spinner_providers.setAdapter(adapter);


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);



        imgPreview = (ImageView) findViewById(R.id.imageView);

        cameraId = findFrontFacingCamera();
        if (cameraId < 0) {
            Toast.makeText(this, "No front facing camera found.",
                    Toast.LENGTH_LONG).show();
        } else {
            camera = Camera.open(cameraId);
        }
    }

    /*------------------------------------------------------------------------------------------------------------------
    -- METHOD:		    btn_client_begin_click
    --
    -- DATE:			March 4, 2014
    --
    -- REVISIONS:		(Date and Description)
    --
    -- DESIGNER:		Jordan Marling
    --
    -- PROGRAMMER:		Jordan Marling
    --
    -- INTERFACE:		public void btn_client_begin_click(View view)
    --                                              view: the view of the button pressed.
    --
    -- RETURNS:			void.
    --
    -- NOTES:			This function is called when the start button is pressed on the client.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    public void btn_client_begin_click(View view) {

        int port;
        EditText username = (EditText)findViewById(R.id.client_user_name);
        EditText port_widget = (EditText)findViewById(R.id.client_port);
        EditText ipAddress_widget = (EditText)findViewById(R.id.client_ip_address);
        Spinner spinner_providers = (Spinner)findViewById(R.id.client_providers);

        if (spinner_providers.getSelectedItem().toString().equals("None")) {
            return;
        }

        String provider = spinner_providers.getSelectedItem().toString();

        try {
            port = Integer.parseInt(port_widget.getText().toString());
            client = new ClientThread(ipAddress_widget.getText().toString(), port, username.getText().toString(), imageArray);
            client.start();

            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);

            List<String> providers = locationManager.getProviders(criteria, true);

            for (String p : providers) {
                locationManager.requestLocationUpdates(p, 1000, 0, this);
            }

            sensorManager.registerListener(this, pressure, SensorManager.SENSOR_DELAY_UI);


        } catch (Exception e) {
            ipAddress_widget.setText("Exception: " + e.getMessage());
        }

        Button start = (Button)findViewById(R.id.btn_client_begin);
        start.setEnabled(false);

        Button stop = (Button)findViewById(R.id.btn_client_stop);
        stop.setEnabled(true);
    }



    /*------------------------------------------------------------------------------------------------------------------
    -- METHOD:		    btn_client_stop_click
    --
    -- DATE:			March 4, 2014
    --
    -- REVISIONS:		(Date and Description)
    --
    -- DESIGNER:		Jordan Marling
    --
    -- PROGRAMMER:		Jordan Marling
    --
    -- INTERFACE:		public void btn_client_stop_click(View view)
    --                                           view: the view of the button pressed.
    --
    -- RETURNS:			the ID of the packet
    --
    -- NOTES:			This function is called when the stop button is pressed on the client.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    public void btn_client_stop_click(View view) {
        if (client != null) {
            Button start = (Button)findViewById(R.id.btn_client_begin);
            start.setEnabled(true);

            Button stop = (Button)findViewById(R.id.btn_client_stop);
            stop.setEnabled(false);

            locationManager.removeUpdates(this);
            sensorManager.unregisterListener(this);
            client.stop_client();
        }
    }

    /*------------------------------------------------------------------------------------------------------------------
    -- METHOD:		    btn_client_location_provider_click
    --
    -- DATE:			March 4, 2014
    --
    -- REVISIONS:		(Date and Description)
    --
    -- DESIGNER:		Jordan Marling
    --
    -- PROGRAMMER:		Jordan Marling
    --
    -- INTERFACE:		public void btn_client_location_provider_click(View view)
    --                                      view: the view of the button pressed.
    --
    -- RETURNS:			void.
    --
    -- NOTES:			This function pulls up a menu that allows the user to change the GPS settings
    --                  on the phone.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    public void btn_client_location_provider_click(View view) {
        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }


    /*------------------------------------------------------------------------------------------------------------------
        -- METHOD:		    findFrontFacingCamera
        --
        -- DATE:			March 4, 2014
        --
        -- REVISIONS:		(Date and Description)
        --
        -- DESIGNER:		Mat Siwoski
        --
        -- PROGRAMMER:		Mat Siwoski
        --
        -- INTERFACE:		private int findFrontFacingCamera()
        --
        -- RETURNS:			the id of the front camera.
        --
        -- NOTES:			This function finds the camera id of the front camera on the android device.
        --
        ----------------------------------------------------------------------------------------------------------------------*/
    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    /*------------------------------------------------------------------------------------------------------------------
        -- METHOD:		    btnCapturePicture
        --
        -- DATE:			March 4, 2014
        --
        -- REVISIONS:		(Date and Description)
        --
        -- DESIGNER:		Mat Siwoski
        --
        -- PROGRAMMER:		Mat Siwoski
        --
        -- INTERFACE:		public void btnCapturePicture(View view)
        --                                      view: the view of the button pressed
        --
        -- RETURNS:			void.
        --
        -- NOTES:			This function is called when the Take Picture button is pressed.
        --
        ----------------------------------------------------------------------------------------------------------------------*/
    public void btnCapturePicture(View view) {
        captureImage();
    }

    /*------------------------------------------------------------------------------------------------------------------
        -- METHOD:		    captureImage
        --
        -- DATE:			March 4, 2014
        --
        -- REVISIONS:		(Date and Description)
        --
        -- DESIGNER:		Mat Siwoski
        --
        -- PROGRAMMER:		Mat Siwoski
        --
        -- INTERFACE:		private void captureImage()
        --
        -- RETURNS:			void.
        --
        -- NOTES:			This function opens the android camera to take a picture.
        --
        ----------------------------------------------------------------------------------------------------------------------*/
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    /*------------------------------------------------------------------------------------------------------------------
        -- METHOD:		    getOutputMediaFileUri
        --
        -- DATE:			March 4, 2014
        --
        -- REVISIONS:		(Date and Description)
        --
        -- DESIGNER:		Mat Siwoski
        --
        -- PROGRAMMER:		Mat Siwoski
        --
        -- INTERFACE:		public Uri getOutputMediaFileUri(int type)
        --                              type: the type of the media file
        --
        -- RETURNS:			the file path.
        --
        -- NOTES:			This function returns a path for the specified media type.
        --
        ----------------------------------------------------------------------------------------------------------------------*/
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /*------------------------------------------------------------------------------------------------------------------
        -- METHOD:		    getOutputMediaFile
        --
        -- DATE:			March 4, 2014
        --
        -- REVISIONS:		(Date and Description)
        --
        -- DESIGNER:		Mat Siwoski
        --
        -- PROGRAMMER:		Mat Siwoski
        --
        -- INTERFACE:		private static File getOutputMediaFile(int type)
        --                              type: the type of the media file
        --
        -- RETURNS:			the file path for the media type.
        --
        -- NOTES:			This function returns the file location of a media type.
        --
        ----------------------------------------------------------------------------------------------------------------------*/
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }


    /*------------------------------------------------------------------------------------------------------------------
        -- METHOD:		    onActivityResult
        --
        -- DATE:			March 4, 2014
        --
        -- REVISIONS:		(Date and Description)
        --
        -- DESIGNER:		Mat Siwoski
        --
        -- PROGRAMMER:		Mat Siwoski
        --
        -- INTERFACE:		protected void onActivityResult(int requestCode, int resultCode, Intent data)
        --                              requestCode: the request type
        --                              resultCode: the result of the request
        --                              data: the data returned from the request.
        --
        -- RETURNS:			void.
        --
        -- NOTES:			This function handles when the camera has finished taking a picture.
        --
        ----------------------------------------------------------------------------------------------------------------------*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // successfully captured the image
                // display it in image view
                previewCapturedImage();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    /*------------------------------------------------------------------------------------------------------------------
        -- METHOD:		    onPause
        --
        -- DATE:			March 4, 2014
        --
        -- REVISIONS:		(Date and Description)
        --
        -- DESIGNER:		Mat Siwoski
        --
        -- PROGRAMMER:		Mat Siwoski
        --
        -- INTERFACE:		protected void onPause()
        --
        -- RETURNS:			void.
        --
        -- NOTES:			This function handles when the activity is paused and releases the camera.
        --
        ----------------------------------------------------------------------------------------------------------------------*/
    @Override
    protected void onPause() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
        super.onPause();
    }

    /*------------------------------------------------------------------------------------------------------------------
        -- METHOD:		    previewCapturedImage
        --
        -- DATE:			March 4, 2014
        --
        -- REVISIONS:		(Date and Description)
        --
        -- DESIGNER:		Mat Siwoski
        --
        -- PROGRAMMER:		Mat Siwoski
        --
        -- INTERFACE:		private void previewCapturedImage()
        --
        -- RETURNS:			void.
        --
        -- NOTES:			This function displays the image captured by the camera to the activity.
        --
        ----------------------------------------------------------------------------------------------------------------------*/
    private void previewCapturedImage() {
        try {
            imgPreview.setVisibility(View.VISIBLE);

            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;

            //final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(), options);
            final Bitmap bitmap = ShrinkBitmap(fileUri.getPath(), 128, 128);

            int bytes = bitmap.getByteCount();
            //Create a new buffer
            ByteBuffer buffer = ByteBuffer.allocate(bytes);
            //Move the byte data to the buffer
            //bitmap.copyPixelsToBuffer(buffer);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);

            imageArray = bos.toByteArray();//buffer.array();


            imgPreview.setImageBitmap(bitmap);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /*------------------------------------------------------------------------------------------------------------------
       -- METHOD:		    ShrinkBitmap
       --
       -- DATE:			    March 4, 2014
       --
       -- REVISIONS:		(Date and Description)
       --
       -- DESIGNER:		    Mat Siwoski
       --
       -- PROGRAMMER:		Mat Siwoski
       --
       -- INTERFACE:		Bitmap ShrinkBitmap(String file, int width, int height)
       --                                       file: the location of the bitmap
       --                                       width: the maximum width
       --                                       height the maximum height
       --
       -- RETURNS:			void.
       --
       -- NOTES:			This function resizes a bitmap to a maximum width/height.
       --
       ----------------------------------------------------------------------------------------------------------------------*/
    Bitmap ShrinkBitmap(String file, int width, int height) {

        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
        bmpFactoryOptions.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);

        int heightRatio = (int)Math.ceil(bmpFactoryOptions.outHeight/(float)height);
        int widthRatio = (int)Math.ceil(bmpFactoryOptions.outWidth/(float)width);

        if (heightRatio > 1 || widthRatio > 1)
        {
            if (heightRatio > widthRatio)
            {
                bmpFactoryOptions.inSampleSize = heightRatio;
            } else {
                bmpFactoryOptions.inSampleSize = widthRatio;
            }
        }

        bmpFactoryOptions.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);
        return bitmap;
    }


    /*------------------------------------------------------------------------------------------------------------------
    -- METHOD:		    onAccuracyChanged
    --
    -- DATE:			March 4, 2014
    --
    -- REVISIONS:		(Date and Description)
    --
    -- DESIGNER:		Jordan Marling
    --
    -- PROGRAMMER:		Jordan Marling
    --
    -- INTERFACE:		public final void onAccuracyChanged(Sensor sensor, int accuracy)
    --                                              sensor: the sensor thats accuracy changed.
    --                                              accuracy: the amount the accuracy changed.
    --
    -- RETURNS:			void
    --
    -- NOTES:			This function is not used.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /*------------------------------------------------------------------------------------------------------------------
    -- METHOD:		    onSensorChanged
    --
    -- DATE:			March 4, 2014
    --
    -- REVISIONS:		(Date and Description)
    --
    -- DESIGNER:		Jordan Marling
    --
    -- PROGRAMMER:		Jordan Marling
    --
    -- INTERFACE:		public final void onSensorChanged(SensorEvent event)
    --
    -- RETURNS:			void.
    --
    -- NOTES:			This function is called when barometer values have changed and sends it to the server.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    @Override
    public final void onSensorChanged(SensorEvent event) {
        float millibarsOfPressure = event.values[0];
        //Toast.makeText(this, "Pressure: " + millibarsOfPressure, Toast.LENGTH_LONG).show();

        if (System.currentTimeMillis() - lastBarometer > 1000) {
            BarometerPacket packet = (BarometerPacket)PacketHandler.GetPacket(BarometerPacket.class);
            packet.pressure = event.values[0];

            client.Write(packet);

            lastBarometer = System.currentTimeMillis();
        }

    }


    /*------------------------------------------------------------------------------------------------------------------
    -- METHOD:		    onProviderEnabled
    --
    -- DATE:			March 4, 2014
    --
    -- REVISIONS:		(Date and Description)
    --
    -- DESIGNER:		Jordan Marling
    --
    -- PROGRAMMER:		Jordan Marling
    --
    -- INTERFACE:		public void onProviderEnabled(String s)
    --
    -- RETURNS:			void.
    --
    -- NOTES:			This function is not used.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    @Override
    public void onProviderEnabled(String s) {

    }

    /*------------------------------------------------------------------------------------------------------------------
    -- METHOD:		    onProviderDisabled
    --
    -- DATE:			March 4, 2014
    --
    -- REVISIONS:		(Date and Description)
    --
    -- DESIGNER:		Jordan Marling
    --
    -- PROGRAMMER:		Jordan Marling
    --
    -- INTERFACE:		public void onProviderDisabled(String s)
    --
    -- RETURNS:			void.
    --
    -- NOTES:			This function is not used.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    @Override
    public void onProviderDisabled(String s) {

    }

    /*------------------------------------------------------------------------------------------------------------------
    -- METHOD:		    onLocationChanged
    --
    -- DATE:			March 4, 2014
    --
    -- REVISIONS:		(Date and Description)
    --
    -- DESIGNER:		Jordan Marling
    --
    -- PROGRAMMER:		Jordan Marling
    --
    -- INTERFACE:		public void onLocationChanged(final Location l)
    --
    -- RETURNS:			void.
    --
    -- NOTES:			This function is called when the GPS location has changed. It sends it to the server.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    @Override
    public void onLocationChanged(final Location l) {
        GPSPacket packet = (GPSPacket)PacketHandler.GetPacket(GPSPacket.class);
        packet.latitude = l.getLatitude();
        packet.longitude = l.getLongitude();
        packet.time = l.getTime();

        client.Write(packet);
    }

    /*------------------------------------------------------------------------------------------------------------------
    -- METHOD:		    getID
    --
    -- DATE:			March 4, 2014
    --
    -- REVISIONS:		(Date and Description)
    --
    -- DESIGNER:		Jordan Marling
    --
    -- PROGRAMMER:		Jordan Marling
    --
    -- INTERFACE:		public int getID()
    --
    -- RETURNS:			the ID of the packet
    --
    -- NOTES:			This function is useful for checking which type of packet it is without using
    --                  instanceof.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    @Override
    public void onStatusChanged(String s, int i, Bundle b) {
    }


    public class ClientThread extends Thread {

        private int port;
        private String server;
        private Socket client;
        private DataOutputStream ostream;
        private String username;
        private byte[] imageArray;

        /*------------------------------------------------------------------------------------------------------------------
        -- METHOD:		    ClientThread
        --
        -- DATE:			March 4, 2014
        --
        -- REVISIONS:		(Date and Description)
        --
        -- DESIGNER:		Mat Siwoski
        --
        -- PROGRAMMER:		Mat Siwoski
        --
        -- INTERFACE:		public ClientThread(String server, int port, String username)
        --                                  server: the IP address of the server.
        --                                  port: the port to send to.
        --                                  username: the username to be seen on the server by (can be null)
        --
        -- RETURNS:			(nothing, constructor)
        --
        -- NOTES:			This function initializes the packets, and member data.
        --
        ----------------------------------------------------------------------------------------------------------------------*/
        public ClientThread(String server, int port, String username, byte[] imageArray) {
            this.server = server;
            this.port = port;
            this.username = username;
            this.imageArray = imageArray;

            PacketHandler.InitializePackets();
        }

        /*------------------------------------------------------------------------------------------------------------------
        -- METHOD:		    stop_client
        --
        -- DATE:			March 4, 2014
        --
        -- REVISIONS:		(Date and Description)
        --
        -- DESIGNER:		Mat Siwoski
        --
        -- PROGRAMMER:		Mat Siwoski
        --
        -- INTERFACE:		public void stop_client()
        --
        -- RETURNS:			void.
        --
        -- NOTES:			This function closes the connection to the server.
        --
        ----------------------------------------------------------------------------------------------------------------------*/
        public void stop_client() {
            try {
                client.close();
            } catch(IOException e) {
                System.out.println("Error closing socket.");
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
        -- NOTES:			This function runs the client in a separate thread.
        --
        ----------------------------------------------------------------------------------------------------------------------*/
        public void run() {
            try {
                System.out.println("CREATING SOCKET");
                System.out.println(server + ", " + port);
                client = new Socket(InetAddress.getByName(server), port);
                ostream = new DataOutputStream(client.getOutputStream());
                System.out.println("SOCKET CREATED");
                System.out.println("Username: " + username);

                if (username.length() > 0) {
                    UsernamePacket usernamePacket = (UsernamePacket) PacketHandler.GetPacket(UsernamePacket.class);
                    usernamePacket.username = username;
                    Write(usernamePacket);
                }

                if (imageArray != null) {
                    ImagePacket imagePacket = (ImagePacket) PacketHandler.GetPacket(ImagePacket.class);
                    if (imagePacket == null) {
                        System.out.println("NULL");
                    }
                    imagePacket.byteArray = imageArray;
                    Write(imagePacket);
                }

            } catch(Exception e) {
                System.out.println("CLIENT RUN EXCEPTION: " + e.getMessage());
            }
            System.out.println("Run finished.");
        }

        /*------------------------------------------------------------------------------------------------------------------
        -- METHOD:		    Write
        --
        -- DATE:			March 4, 2014
        --
        -- REVISIONS:		(Date and Description)
        --
        -- DESIGNER:		Mat Siwoski
        --
        -- PROGRAMMER:		Mat Siwoski
        --
        -- INTERFACE:		public synchronized void Write(IPacket packet)
        --                                          packet: the packet to send to the server.
        --
        -- RETURNS:			void.
        --
        -- NOTES:			This function sends a packet to the server on the client thread.
        --
        ----------------------------------------------------------------------------------------------------------------------*/
        public synchronized void Write(IPacket packet) {
            if (client != null) {
                try {
                    //System.out.println("Writing: " + packet.getID());
                    ostream.writeInt(packet.getID());
                    packet.WriteData(ostream);
                    ostream.flush();
                } catch (Exception e) {
                    System.out.println("Exception: " + e.getMessage());
                }

            }
            else {
                System.out.println("No client yet");
            }
        }

    }

}
