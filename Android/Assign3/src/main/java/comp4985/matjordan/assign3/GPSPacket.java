/*---------------------------------------------------------------------------------------
--	Source File:		GPSPacket.java - A class that handles the GPS data.
--
--	Classes:		GPSPacket - public class
--
--	Methods:
--				public abstract void ReadData(DataInputStream istream) throws IOException
--				public abstract void WriteData(DataOutputStream ostream);
--
--
--	Date:			March 4, 2014
--
--	Revisions:		(Date and Description)
--
--	Designer:		Jordan Marling
--
--	Programmer:		Jordan Marling
--
--	Notes:
--	This class allows the client and server to communicate GPS information
--  between the clients and server.
--
--
---------------------------------------------------------------------------------------*/
package comp4985.matjordan.assign3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class GPSPacket extends IPacket {

    public double longitude;
    public double latitude;
    public long time;

    /*------------------------------------------------------------------------------------------------------------------
    -- METHOD:		    ReadData
    --
    -- DATE:			March 4, 2014
    --
    -- REVISIONS:		(Date and Description)
    --
    -- DESIGNER:		Jordan Marling
    --
    -- PROGRAMMER:		Jordan Marling
    --
    -- INTERFACE:		public void ReadData(DataInputStream istream)
    --                                  istream: The input stream from the client
    --
    -- RETURNS:			void.
    --
    -- NOTES:			This function will read a GPS Packet from the input stream.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    public void ReadData(DataInputStream istream) throws IOException {
        longitude = istream.readDouble();
        latitude = istream.readDouble();
        time = istream.readLong();
    }

    /*------------------------------------------------------------------------------------------------------------------
    -- METHOD:		    WriteData
    --
    -- DATE:			March 4, 2014
    --
    -- REVISIONS:		(Date and Description)
    --
    -- DESIGNER:		Jordan Marling
    --
    -- PROGRAMMER:		Jordan Marling
    --
    -- INTERFACE:		public void WriteData(DataOutputStream ostream)
    --                                  ostream: The output stream from the client
    --
    -- RETURNS:			void.
    --
    -- NOTES:			This function will write a GPS Packet to the output stream.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    public void WriteData(DataOutputStream ostream) {
        try {
            ostream.writeDouble(longitude);
            ostream.writeDouble(latitude);
            ostream.writeLong(time);
        } catch (Exception e) {

        }
    }

}
