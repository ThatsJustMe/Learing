package cz.unicorncollege.bt.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import cz.unicorncollege.bt.model.MeetingCentre;
import cz.unicorncollege.bt.model.MeetingRoom;
import cz.unicorncollege.bt.model.Reservation;
import cz.unicorncollege.controller.MeetingController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class FileParser {
	
	/**
	 * Method to import data from the chosen file.
	 */
	public static List<MeetingCentre> importMeetingObjectsData() {
		
		String inputedPathOfFileToImport = Choices.getInput("Enter path of imported file: ");
		List<MeetingCentre> allMeetingCentres = new ArrayList<>();

		//TODO: Nacist data z importovaneho souboru

		Path pathOfFileToImport = Paths.get( inputedPathOfFileToImport );

		if( Files.exists( pathOfFileToImport ))
		{
			allMeetingCentres = loadMeetingObjectsDataFromFile( inputedPathOfFileToImport, null );

			if( allMeetingCentres != null )
			{
				MeetingController.showDialogMessage( "Data was imported. " + allMeetingCentres.size() + " objects of Meeting Centres were loaded." );
			}
			else
			{
				MeetingController.showDialogMessage( "Unable to read the file correctly. Please, try it again." );
				importMeetingObjectsData();
			}
		}
		else
		{
			MeetingController.showDialogMessage( "Path of imported file you entered does not exist. Please, try it again." );
			importMeetingObjectsData();
		}

		return allMeetingCentres;
	}
	
	/**
	 * Method to save the data to file.
	 */
	public static void writeDataToFile( String dataToSave, String filePath, boolean omitSuccessDialog )
	{
		//TODO: ulozeni dat do souboru

		File storage_fiile = new File( filePath.toString() );

		if ( !Files.exists( Paths.get( filePath )))
		{
			try
			{
				storage_fiile.createNewFile();
			}
			catch( IOException cachedException )
			{
				cachedException.printStackTrace();
			}
		}

		try( BufferedWriter bufferedWriter = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( storage_fiile ))))
		{
			bufferedWriter.write( dataToSave );
			if( !omitSuccessDialog ) MeetingController.showDialogMessage( "Data was saved correctly." );
		}
		catch( IOException cachedException )
		{
			MeetingController.showDialogMessage( "Something went wrong when saving a data. Here are details: \n");
			cachedException.printStackTrace();
		}
	}

	
	/**
	 * Method to load the data from file.
	 * @return
	 */
	public static List<MeetingCentre> loadMeetingObjectsDataFromFile( String pathOfFileToImportCSV, String pathOfFileToImportXML )
	{
		//TODO: nacist data ze souboru
		Map<String, MeetingCentre> allMeetingCentres = new HashMap<>();
		Map<String, Map<String, ArrayList<Reservation>>> loadedReservationsMap = pathOfFileToImportXML == null ? null : FileParser.loadStoredReservations( pathOfFileToImportXML );

		if( Files.exists( Paths.get( pathOfFileToImportCSV )))
		{
			try( BufferedReader bufferedReader = new BufferedReader( new FileReader( pathOfFileToImportCSV )))
			{
				String oneInputLine;
				String currentlyLoadingObjectType = "";

				while(( oneInputLine = bufferedReader.readLine() ) != null )
				{
					String[] oneLineSplitted = oneInputLine.split( "," );
					oneLineSplitted[0] = oneLineSplitted[0].replace( "\uFEFF", "" );

					if( oneLineSplitted[0].equals( "MEETING_CENTRES" ) || oneLineSplitted[0].equals( "MEETING_ROOMS" ))
					{
						currentlyLoadingObjectType = oneLineSplitted[0];
						continue;
					}

					if( currentlyLoadingObjectType.equals( "MEETING_CENTRES" ))
					{
						MeetingCentre newMeetingCentre = new MeetingCentre( oneLineSplitted[0], oneLineSplitted[1], oneLineSplitted[2] );
						allMeetingCentres.put( oneLineSplitted[1], newMeetingCentre );
					}
					else
					{
						MeetingRoom newMeetingRoom = new MeetingRoom( oneLineSplitted[0],
																		 oneLineSplitted[1],
																		 oneLineSplitted[2],
																		 Integer.parseInt( oneLineSplitted[3] ),
																		 oneLineSplitted[4].equals( "YES" ) );

						MeetingCentre meetingCentre = allMeetingCentres.get( oneLineSplitted[5] );
						meetingCentre.setMeetingRoom( newMeetingRoom );
						newMeetingRoom.setMeetingCentre( meetingCentre );

						if( loadedReservationsMap != null &&
							!loadedReservationsMap.isEmpty() &&
							loadedReservationsMap.containsKey( meetingCentre.getCode() ) &&
							loadedReservationsMap.get( meetingCentre.getCode() ).containsKey( newMeetingRoom.getCode() ))
						{
							List<Reservation> reservationsForNewMeetingRoom = loadedReservationsMap.get( meetingCentre.getCode() ).get( newMeetingRoom.getCode() );
							newMeetingRoom.setReservations( reservationsForNewMeetingRoom );

							for( Reservation reservation : reservationsForNewMeetingRoom )
								reservation.setMeetingRoom( newMeetingRoom );
						}
					}
				}

				List<MeetingCentre> allMeetingCentresList = new ArrayList<>();
				allMeetingCentresList.addAll( allMeetingCentres.values() );

				MeetingController.showDialogMessage( "Data was succesfully loaded. " + allMeetingCentresList.size() + " objects of MEETING CENTRES were loaded." );

				return allMeetingCentresList;
			}
			catch( IOException cachedException )
			{
				MeetingController.showDialogMessage( "Something went wrong when reading file. Here are some details: \n" );
				cachedException.printStackTrace();
			}
		}
		else MeetingController.showDialogMessage( "No initial data to load. Nothing was loaded." );

		return null;
	}


	private static Map loadStoredReservations(String pathOfFileToImport )
	{
		if( Files.exists( Paths.get( pathOfFileToImport )))
		{
			try
			{
				Map<String, Map<String, ArrayList<Reservation>>> meetingCentresMap = new HashMap<>();

				DateFormat timeFormat = new SimpleDateFormat( "HH:MM" );
				DateFormat dateFormat = new SimpleDateFormat( "yyyy/MM/dd" );

				File fXmlFile = new File( pathOfFileToImport );
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document XMLDocumnent = dBuilder.parse(fXmlFile);

				NodeList meetingCentresNodeList = XMLDocumnent.getElementsByTagName( "meetingCentres" );

				for( int i = 0; i < meetingCentresNodeList.getLength(); i++ )
				{
					Map<String, ArrayList<Reservation>> meetingRoomsMap = new HashMap<>();

					Node meetingCentreNode = meetingCentresNodeList.item( i );
					Element meetingCentreElement = (Element) meetingCentreNode;

					String meetingCentreCode = meetingCentreElement.getElementsByTagName( "code" ).item( 0 ).getTextContent();
					NodeList meetingRoomsNodeList = meetingCentreElement.getElementsByTagName( "meetingRoom" );

					for( int j = 0; j < meetingRoomsNodeList.getLength(); j++ )
					{
						ArrayList<Reservation> reservationsList = new ArrayList<>();

						Node meetingRoomNode = meetingRoomsNodeList.item( i );
						Element meetingRoomElement = (Element) meetingRoomNode;

						String meetingRoomCode = meetingRoomElement.getElementsByTagName( "code" ).item( 0 ).getTextContent();
						NodeList ReservationsNodeList = meetingRoomElement.getElementsByTagName( "reservation" );

						for( int k = 0; k < ReservationsNodeList.getLength(); k++ )
						{
							Node reservationNode = ReservationsNodeList.item( i );
							Element reservationElement = (Element) reservationNode;

							String customer = reservationElement.getElementsByTagName( "customer" ).item( 0 ).getTextContent();
							int numberOfExpectedPersons = Integer.parseInt( reservationElement.getElementsByTagName( "numberOfExpectedPersons" ).item( 0 ).getTextContent() );
							boolean videoConferenceNeed = reservationElement.getElementsByTagName("videoConferenceNeed").item(0).getTextContent().equals( "true" );
							Date date = dateFormat.parse( reservationElement.getElementsByTagName( "date" ).item( 0 ).getTextContent() );
							Date timeFrom = timeFormat.parse( reservationElement.getElementsByTagName( "timeFrom" ).item( 0 ).getTextContent() );
							Date timeTo = timeFormat.parse( reservationElement.getElementsByTagName( "timeTo" ).item( 0 ).getTextContent() );
							String note = reservationElement.getElementsByTagName( "note" ).item( 0 ).getTextContent();

							Reservation newReservation = new Reservation();
							newReservation.setCustomer( customer );
							newReservation.setExpectedPersonsCount( numberOfExpectedPersons );
							newReservation.setVideoConference( videoConferenceNeed );
							newReservation.setDate( date );
							newReservation.setTimeFrom( timeFrom );
							newReservation.setTimeTo( timeTo );
							newReservation.setNote( note == null || note.isEmpty() ? "" : note );

							reservationsList.add( newReservation );
						}

						meetingRoomsMap.put( meetingRoomCode, reservationsList );
					}

					meetingCentresMap.put( meetingCentreCode, meetingRoomsMap );
				}

				return meetingCentresMap;
			}
			catch (ParserConfigurationException | IOException | SAXException | ParseException e)
			{
				MeetingController.showDialogMessage( "Can't load saved reservations. File with saved reservations is probably corrupted. Here are som more details" );
				e.printStackTrace();
				return null;
			}
		}

		return null;
	}
}
