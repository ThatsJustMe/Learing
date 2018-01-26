package cz.unicorncollege.controller;

import java.util.ArrayList;
import java.util.List;

import cz.unicorncollege.bt.utils.Choices;
import cz.unicorncollege.bt.utils.FileParser;

/**
 * Main controller class.
 * Contains methods to communicate with user and methods to work with files.
 *
 * @author UCL
 */
public class MainController
{
	private MeetingController MeetingController;
	
	/**
	 * Constructor of main class.
	 */
	public MainController() {
		MeetingController = new MeetingController();
		MeetingController.init();
	}

	
    /**
	 * Main method, which runs the whole application.
	 *
	 * @param argv String[]
	 */
	public static void main(String[] argv) {
		MainController instance = new MainController();
		instance.run();
	}

	/**
	 * Method which shows the main menu and end after user chooses Exit.
	 */
	private void run() {
		List<String> choices = new ArrayList<String>();
		choices.add("List all Meeting Centres");
		choices.add("Add Meeting Centre");
		choices.add("Manage Reservations");
		choices.add("Import Data");
		choices.add("Exit");

		while( true )
		{
			switch( Choices.getChoice( "Continue with an option: ", choices ))
			{
			case 1:
				MeetingController.listAllMeetingCentres();
				break;

			case 2:
				MeetingController.addNewMeetingCentre();
				break;

			case 3:
				MeetingController.manageReservationsStepOne();
				break;

			case 4:
				MeetingController.setMeetingCentres( FileParser.importMeetingObjectsData() );
				MeetingController.listAllMeetingCentres();
				break;

			case 5:
				MeetingController.verifyWhetherToSaveChanges( false );
				return;
			}
		}
	}
}
