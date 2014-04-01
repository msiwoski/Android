package comp4985.matjordan.assign3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by jordan on 05/03/14.
 */
public class ImagePacket extends IPacket {

    public byte[] byteArray;

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
    -- NOTES:			This function will read an Image Packet from the input stream.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    public void ReadData(DataInputStream istream) throws IOException {

        int length = istream.readInt();

        byteArray = new byte[length];

//        istream.read(byteArray, 0, length);

        for(int i = 0; i < length; i++) {
            byteArray[i] = istream.readByte();
        }
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
    -- NOTES:			This function will write an Image Packet to the output stream.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    public void WriteData(DataOutputStream ostream) {
        try {
            ostream.writeInt(byteArray.length);

            //ostream.write(byteArray, 0, byteArray.length);

            for(int i = 0; i < byteArray.length; i++) {
                ostream.writeByte(byteArray[i]);
            }

        } catch (Exception e) {

        }
    }

}
