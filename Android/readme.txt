COMP 4985 Android Assignment 
Jordan Marling/Mateusz Siwoski

files included:
Assign3.apk
Comp 4985 Android Assignment.docx
Android State Diagram.vsdx
Android State Diagram.jpg
readme.txt
src/main/java/comp4985.matjordan.assign3/ClientActivity.java
src/main/java/comp4985.matjordan.assign3/MainActivity.java
src/main/java/comp4985.matjordan.assign3/ServerActivity.java
src/main/java/comp4985.matjordan.assign3/BarometerPacket.java
src/main/java/comp4985.matjordan.assign3/ImagePacket.java
src/main/java/comp4985.matjordan.assign3/GPSPacket.java
src/main/java/comp4985.matjordan.assign3/ IPacket.java
src/main/java/comp4985.matjordan.assign3/ UsernamePacket.java
src/main/java/comp4985.matjordan.assign3/ PacketHandler.java
src/main/java/res/drawable-hdpi/ic_launcher.png
src/main/java/res/drawable-mdpi/ic_launcher.png
src/main/java/res/drawable-xhdpi/ic_launcher.png
src/main/java/res/drawable-xxhdpi/background.png
src/main/java/res/drawable-xxhdpi/clientbtn.png
src/main/java/res/drawable-xxhdpi/clientbutton.png
src/main/java/res/drawable-xxhdpi/hybrid.png
src/main/java/res/drawable-xxhdpi/map.png
src/main/java/res/drawable-xxhdpi/normal.png
src/main/java/res/drawable-xxhdpi/serverbtn.png
src/main/java/res/drawable-xxhdpi/serverbutton.png
src/main/java/res/layout/activity_client.xml
src/main/java/res/layout/activity_main.xml
src/main/java/res/layout/activity_server.xml
src/main/java/res/layout/fragment_main.xml
src/main/java/res/menu/client.xml
src/main/java/res/menu/main.xml
src/main/java/res/values/strings.xml
src/main/java/res/values/dimens.xml
src/main/java/res/values/styles.xml
src/main/java/AndroidManifest.xml
src/main/java/ic_launcher-web.png
src/build.gradle

Features:
The following features are available in our application:
•	Integration with the Google Maps API
•	Barometric Readings
•	Text-to-speech (Client joins/leaves and changes position)
•	Enabled/disabled Text-To-Speech
•	Toast messages for updates
•	GPS Coordinates
•	Multiple Clients connecting to server
•	GPS Tracking on map (clients route is tracked)
•	GUI Design
•	Changing map type form Hybrid to Normal (vice versa)
•	Continuous Map Updates
•	Custom Usernames for clients (IP is set as default) 
•	Take pictures with the Camera and send the picture to the server and display it on the Google Maps API 


Installation:

The following is the guideline to install the application. A file managing app will be necessary on the phone (Suggestions include Astro File Manager and App Installer):
1.	Connect phone to computer
2.	Open the android phone on the PC
3.	Browse to a folder where you would like to install the .apk (suggested would be Download)
4.	Copy the .apk to the folder
5.	On the phone, open the file manager and browse to the location of the folder
6.	Click on the app name
7.	Click install
8.	Click Done to finish installation or Open to open the app right away

Use:

Open the application on two phones. One phone will be the client, the other will be the server. 

1.	On the server, set the Port Number and either enable or disable Text-to-Speech. Click start to begin tracking the client. Stop will stop the client
2.	On the client, set a username for the client. If the client does not set a username, default IP will be used. 
3.	Set the server’s IP address
4.	Set the server’s Port number
5.	Set the Location Provider option (Network or GPS)
6.	Click Start to begin sending data to the Server
