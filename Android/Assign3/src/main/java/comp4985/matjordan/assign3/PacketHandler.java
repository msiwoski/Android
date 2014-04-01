/*---------------------------------------------------------------------------------------
--	Source File:		PacketHandler.java - A class manages all of the packet information.
--
--	Classes:		PacketHandler - public class
--
--	Methods:
--				public static void InitializePackets()
--				public static IPacket ReadPacket(DataInputStream istream)
--				public static IPacket GetPacket(Class p)
--				public static String ReadString(DataInputStream istream)
--				public static void WriteString(DataOutputStream ostream, String str)
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
--	This class helps manage packets. The ReadPacket class will construct a packet based on
--  information supplied by a DataInputStream which is used by a TCP connection. This class
--  also has methods for packets to use when reading/writing strings.
--
--  This provides a nice interface for both the client and the server to use to deal
--  with packets the same way, and any changed made only need to be made in one spot.
--
--
---------------------------------------------------------------------------------------*/
package comp4985.matjordan.assign3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PacketHandler {

    private static Map<Integer, Class> packets = new HashMap<Integer, Class>();

    /*------------------------------------------------------------------------------------------------------------------
    -- METHOD:		    InitializePackets
    --
    -- DATE:			March 4, 2014
    --
    -- REVISIONS:		(Date and Description)
    --
    -- DESIGNER:		Jordan Marling
    --
    -- PROGRAMMER:		Jordan Marling
    --
    -- INTERFACE:		public static void InitializePackets()
    --
    -- RETURNS:			void.
    --
    -- NOTES:			This function puts all of the packets into a hashmap for easy lookup.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    public static void InitializePackets() {

        packets.put(0, UsernamePacket.class);
        packets.put(1, GPSPacket.class);
        packets.put(2, BarometerPacket.class);
        packets.put(3, ImagePacket.class);

    }

    /*------------------------------------------------------------------------------------------------------------------
    -- METHOD:		    ReadPacket
    --
    -- DATE:			March 4, 2014
    --
    -- REVISIONS:		(Date and Description)
    --
    -- DESIGNER:		Jordan Marling
    --
    -- PROGRAMMER:		Jordan Marling
    --
    -- INTERFACE:		public static IPacket ReadPacket(DataInputStream istream)
    --                                             istream: the input stream to read the packet from.
    --
    -- RETURNS:			The packet that is read, if no packet is read it returns null.
    --
    -- NOTES:			This function blocks until it has read a packet, or failed to read
    --                  a packet form the input stream.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    public static IPacket ReadPacket(DataInputStream istream) {

        int num;
        IPacket packet = null;

        try {
            num = istream.readInt();

            //System.out.println("Searching for packet: " + num);

            if (packets.containsKey(num)) {
                packet = (IPacket)packets.get(num).newInstance();

                packet.ID = num;
                packet.ReadData(istream);
            }
            else {
                return null;
            }
        }
        catch(Exception ex) {
            return null;
        }
        return packet;
    }

    /*------------------------------------------------------------------------------------------------------------------
    -- METHOD:		    GetPacket
    --
    -- DATE:			March 4, 2014
    --
    -- REVISIONS:		(Date and Description)
    --
    -- DESIGNER:		Jordan Marling
    --
    -- PROGRAMMER:		Jordan Marling
    --
    -- INTERFACE:		public static IPacket GetPacket(Class p)
    --                                              p: The class of packet wanted.
    --
    -- RETURNS:			The packet that is requested or null if the packet type doesn't exist.
    --
    -- NOTES:			Pass in a class such as GPSPacket.class as the parameter. This will be
    --                  lookup up with the initialized hashmap and the ID will automatically
    --                  be set.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    public static IPacket GetPacket(Class p) {
        IPacket packet = null;
        try {
            Iterator it = packets.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry)it.next();

                //System.out.println(((Class)pairs.getValue()).getName() + " == " + p.getName());

                if (((Class)pairs.getValue()).getName().equals(p.getName())) {
                    packet = (IPacket)p.newInstance();
                    packet.ID = (Integer)pairs.getKey();
                }

            }
        } catch(Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
            return null;
        }

        return packet;
    }

    /*------------------------------------------------------------------------------------------------------------------
    -- METHOD:		    ReadString
    --
    -- DATE:			March 4, 2014
    --
    -- REVISIONS:		(Date and Description)
    --
    -- DESIGNER:		Mat Siwoski
    --
    -- PROGRAMMER:		Mat Siwoski
    --
    -- INTERFACE:		public static String ReadString(DataInputStream istream)
    --                                       istream; The input stream to read the string from.
    --
    -- RETURNS:			the string read.
    --
    -- NOTES:			This function reads a string from the input stream.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    public static String ReadString(DataInputStream istream) {
        String str = "";
        int length;
        try {

            length = istream.readInt();
            //System.out.println("String length: " + length);

            for(int i = 0; i < length; i++) {
                str += istream.readChar();
            }

            //System.out.println("String: " + str);
        } catch(IOException e) {
            return null;
        }

        return str;
    }

    /*------------------------------------------------------------------------------------------------------------------
    -- METHOD:		    WriteString
    --
    -- DATE:			March 4, 2014
    --
    -- REVISIONS:		(Date and Description)
    --
    -- DESIGNER:		Mat Siwoski
    --
    -- PROGRAMMER:		Mat Siwoski
    --
    -- INTERFACE:		public static void WriteString(DataOutputStream ostream, String str)
    --                                              ostream: the stream to write the string to.
    --                                              str: the string to write.
    --
    -- RETURNS:			void.
    --
    -- NOTES:			This function is useful for checking which type of packet it is without using
    --                  instanceof.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    public static void WriteString(DataOutputStream ostream, String str) {
        try {
            ostream.writeInt(str.length());
            System.out.println("Write length: " + str.length());
            for(int i = 0; i < str.length(); i++) {
                ostream.writeChar(str.charAt(i));
            }
            System.out.println("Write string: " + str);
        } catch(IOException e) {
            System.out.println("Error writing string to output stream.");
        }
    }

}
