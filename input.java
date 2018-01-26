package cz.unicorncollege.bt.utils;

import cz.unicorncollege.controller.MeetingController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class Choices {

	/**
	 * Method to get the user choice from some list of options.
	 *
	 * @param choiceText String - Information text about options.
	 * @param choices List - list of options given to the user.
	 * @return int - choosen option.
	 */
	public static int getChoice( String choiceText, List<String> choices )
	{
		System.out.println();
		System.out.println( choiceText );
		System.out.println( "---" );

		for (int i = 0; i < choices.size(); i++)
		{
			System.out.println("  " + (i + 1) + " - " + choices.get(i));
		}

		System.out.println( "---" );
		System.out.print("> ");

		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		
		try {
			return Integer.parseInt(r.readLine().trim());
		} catch (Exception e) {
			return -1;
		}
	}
	
	/**
	 * Method to get the response from the user, typicaly some text or another data to fill in some object
	 *
	 * @param choiceText String - Info about what to enter.
	 * @return String - user's answer.
	 */
	public static String getInput(String choiceText)
	{
		String result = null;

		System.out.println();
		System.out.print(choiceText);
		System.out.println();
		System.out.println( "---" );
		System.out.print( "> " );

		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		try {
			result = r.readLine().trim();
		} catch (Exception e) {
		}

		return result;
	}

	public static String getInput( String choiceText, List<String> choices )
	{
		String result = null;

		System.out.println(choiceText);
		System.out.println( "---" );

		for (int i = 0; i < choices.size(); i++) {
			System.out.println("  " + (i + 1) + " - " + choices.get(i));
		}

		System.out.println( "---" );
		System.out.print("> ");

		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		try {
			result = r.readLine().trim();
		} catch (Exception e) {
		}

		return result;
	}


	public static String[] getVerifiedUserOption( String choicesText, List<String> choices )
	{
		String[] verifiedUserOptions = new String[2];

		while( true )
		{
			String userInput = getInput( choicesText, choices );

			verifiedUserOptions[0] = userInput.substring( 0, 1 );
			verifiedUserOptions[1] = userInput.contains( " " ) ? userInput.substring( 2, userInput.length() ) : userInput;

			if( Character.isDigit( verifiedUserOptions[0].charAt( 0 ))) break;
			else MeetingController.showDialogMessage( "Invalid input. Try it again." );
		}

		return verifiedUserOptions;
	}
}
