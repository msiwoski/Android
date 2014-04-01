/*---------------------------------------------------------------------------------------
--	Source File:		BarometerPacket.java - A class that handles the barometer data.
--
--	Classes:		BarometerPacket - public class
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
--	This class allows the client and server to communicate barometric information
--  between the clients and server.
--
--
---------------------------------------------------------------------------------------*/
package comp4985.matjordan.assign3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class BarometerPacket extends IPacket {

    public double pressure;

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
    -- NOTES:			This function will read a Barometer Packet from the input stream.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    public void ReadData(DataInputStream istream) throws IOException {
        pressure = istream.readDouble();
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
    -- NOTES:			This function will write a Barometer Packet to the output stream.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    public void WriteData(DataOutputStream ostream) {
        try {
            ostream.writeDouble(pressure);
        } catch (Exception e) {

        }
    }

}
