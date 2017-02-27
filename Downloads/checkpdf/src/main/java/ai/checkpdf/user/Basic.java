package ai.checkpdf.user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;



public abstract class Basic {

	private String inputPath;
	private String outputPath;

	// </protected>

	// <internal>
	final void initialize(String inputPath, String outputPath) {
		this.inputPath = inputPath;
		this.outputPath = outputPath;
	}

	/**
	 * Executes the Basic.
	 * 
	 * @return Whether the Basic has been completed.
	 */
	public abstract void run();

	/**
	 * Prompts a message to the user.
	 * 
	 * @param message
	 *            Text to show.
	 */
	/**
	 * Gets the path used to serialize output files.
	 */
	protected String getOutputPath() {
		return getOutputPath(null);
	}

	/**
	 * Gets the path used to serialize output files.
	 * 
	 * @param fileName
	 *            Relative output file path.
	 */
	protected String getOutputPath(String fileName) {
		return outputPath + (fileName != null ? java.io.File.separator + fileName : "");
	}

	protected void prompt(String message) {
		System.out.println("\n" + message);
	    System.out.println("Press ENTER to continue");
	    try
	    {
	      Scanner in = new Scanner(System.in);
	      in.nextLine();
	    }
	    catch(Exception e)
	    {}
	}

	/**
	 * Gets the user's choice from the given request.
	 * 
	 * @param message
	 *            Description of the request to show to the user.
	 * @return User choice.
	 */
	protected String promptChoice(String message) {
		System.out.print(message);
		Scanner in = new Scanner(System.in);
		try {
			return in.nextLine();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Gets the user's choice from the given options.
	 * 
	 * @param options
	 *            Available options to show to the user.
	 * @param message
	 * 			  Description of the request to show to the user.
	 * @return Chosen option key.
	 */
	protected String promptChoice(Map<String, String> options,String message) {
		System.out.println();
		List<Map.Entry<String, String>> optionEntries = new ArrayList<Map.Entry<String, String>>(options.entrySet());
		Collections.sort(optionEntries, new Comparator<Map.Entry<String, String>>() {
			@Override
			public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
				return o1.getKey().compareTo(o2.getKey());
			};
		});
		while (true) {
			for (Map.Entry<String, String> option : optionEntries) {
				System.out.println(
						(option.getKey().equals("") ? "ENTER" : "[" + option.getKey() + "]") + " " + option.getValue());
			}
			System.out.print(message);
			Scanner in = new Scanner(System.in);
			try {
				String input = in.nextLine();
				if (options.containsKey(input)) {
					return input;
				}
			} catch (Exception e) {
			}
		}
	}


	protected String promptFileChoice(String inputDescription) {
		Scanner in = new Scanner(System.in);

		java.io.File resourceFolder = new java.io.File(inputPath);
		try {
			System.out.println("\nAvailable PDF files (" + resourceFolder.getCanonicalPath() + "):");
		} catch (IOException e1) {
			/* NOOP */}

		// Get the list of available PDF files!
		FileResources resources = new FileResources(resourceFolder);
		List<String> fileNames = Arrays.asList(resources.filter("pdf"));
		Collections.sort(fileNames);

		// Display files!
		resources.printList((String[]) fileNames.toArray());

		while (true) {
			// Get the user's choice!
			System.out.print(inputDescription + ": ");
			try {
				return resourceFolder.getPath() + java.io.File.separator
						+ fileNames.get(Integer.parseInt(in.nextLine()));
			} catch (Exception e) {
				/* NOOP */}
		}
	}
}
