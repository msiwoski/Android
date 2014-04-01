/*---------------------------------------------------------------------------------------
--	Source File:		IPacket.java - An interface that applies to all packets.
--
--	Classes:		IPacket - public abstract class
--
--	Methods:
--				public int getID()
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
--	This class makes it easy to create new packets because all packets have a base
--  class with common reading/writing methods.
--
--
---------------------------------------------------------------------------------------*/
package comp4985.matjordan.assign3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class IPacket {

    protected int ID;

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
    public int getID() {
        return ID;
    }


    public abstract void ReadData(DataInputStream istream) throws IOException;
    public abstract void WriteData(DataOutputStream ostream);

}
