package cx.mccormick.pddroidparty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.*;

public final class PdParser {
	private static final String line_re = "(#((.|\r|\n)*?)[^\\\\])\r{0,1}\n{0,1};\r{0,1}\n";
	private static final String token_re = " |\r\n?|\n";
	
	/**
	* The pattern is matched to the first argument.
	*/
	public static void main (String[] aArguments) {
		ArrayList<String[]> atomlines = getAtomLines(readPatch(aArguments[0]));
		printAtoms(atomlines);
	}
	
	public static void printAtoms(ArrayList<String[]> atomlines) {
		for (String[] line: atomlines) {
			for (int i=0; i<line.length; i++) {
				System.out.print(" [" + line[i] + "]");
			}
			System.out.println();
		}
	}
	
	public static ArrayList<String[]> parsePatch(String filename) {
		return getAtomLines(readPatch(filename));
	}
	
	private static String readPatch(String name)
	{
		File file = new File(name);
		StringBuffer contents = new StringBuffer();
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(file));
			String text = null;

			// repeat until all lines is read
			while ((text = reader.readLine()) != null) {
				contents.append(text)
					.append(System.getProperty(
						"line.separator"));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// show file contents here
		return contents.toString();
	}
	
	/**
	* The Matcher.find method attempts to match *parts* of the input
	* to the given pattern.
	*/
	public static ArrayList<String[]> getAtomLines(String patchtext) {
		Pattern pattern = Pattern.compile(line_re, Pattern.MULTILINE);
		Pattern token_pattern = Pattern.compile(token_re, Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(patchtext);
		ArrayList<String[]> atomlines = new ArrayList<String[]>();
		while (matcher.find()) {
			String[] s = token_pattern.split(matcher.group(1));
			atomlines.add(token_pattern.split(matcher.group(1)));
		}
		return atomlines;
	}
}
