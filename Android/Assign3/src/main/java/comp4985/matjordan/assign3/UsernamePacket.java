/*---------------------------------------------------------------------------------------
--	Source File:		UsernamePacket.java - A class that lets a client send the server
--                                                      a username.
--
--	Classes:		UsernamePacket - public class
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
--	This class allows the client to send the server its username.
--
--
---------------------------------------------------------------------------------------*/
package comp4985.matjordan.assign3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;

public class UsernamePacket extends IPacket {

    public String username;

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
    -- NOTES:			This function will read a Username Packet from the input stream.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    public void ReadData(DataInputStream istream) {
        username = PacketHandler.ReadString(istream);
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
    -- NOTES:			This function will write a Username Packet to the output stream.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    public void WriteData(DataOutputStream ostream) {
        PacketHandler.WriteString(ostream, username);
    }

}
