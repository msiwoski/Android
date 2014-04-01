/*---------------------------------------------------------------------------------------
--	Source File:		MainActivity.java - A class that prompts a user to run the client
--                                                     or server.
--
--	Classes:		MainActivity - public class
--
--	Methods:
--				protected void onCreate(Bundle savedInstanceState)
--				public void btn_client_click(View view)
--              public void btn_server_click(View view)
--
--
--	Date:			March 4, 2014
--
--	Revisions:		(Date and Description)
--
--	Designer:		Mat Siwoski
--
--	Programmer:		Mat Siwoski
--
--	Notes:
--	This class prompts a user to either run the server or client part of the application.
--
--
---------------------------------------------------------------------------------------*/
package comp4985.matjordan.assign3;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends ActionBarActivity {

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
    --                                  saveInstance: the data of the current instance of the application.
    --
    -- RETURNS:			void.
    --
    -- NOTES:			This function sets the view to be the main activity view.
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /*------------------------------------------------------------------------------------------------------------------
    -- METHOD:		    btn_client_click
    --
    -- DATE:			March 4, 2014
    --
    -- REVISIONS:		(Date and Description)
    --
    -- DESIGNER:		Mat Siwoski
    --
    -- PROGRAMMER:		Mat Siwoski
    --
    -- INTERFACE:		public void btn_client_click(View view)
    --                                  view: the view that is clicked.
    --
    -- RETURNS:			void.
    --
    -- NOTES:			This function handles when the user presses the client button
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    public void btn_client_click(View view) {
        Intent intent = new Intent(this, ClientActivity.class);
        startActivity(intent);
    }

    /*------------------------------------------------------------------------------------------------------------------
    -- METHOD:		    btn_server_click
    --
    -- DATE:			March 4, 2014
    --
    -- REVISIONS:		(Date and Description)
    --
    -- DESIGNER:		Mat Siwoski
    --
    -- PROGRAMMER:		Mat Siwoski
    --
    -- INTERFACE:		public void btn_server_click(View view)
    --                                  view: the view that was clicked
    --
    -- RETURNS:			void.
    --
    -- NOTES:			This function handles when the user presses the server button
    --
    ----------------------------------------------------------------------------------------------------------------------*/
    public void btn_server_click(View view) {
        Intent intent = new Intent(this, ServerActivity.class);
        startActivity(intent);
    }

}
