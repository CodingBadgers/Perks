package uk.thecodingbadgers.bkits.kit.parser;

import java.io.File;
import java.io.IOException;
import java.util.List;

import uk.thecodingbadgers.bkits.kit.Kit;

public abstract class KitParser {
	
	protected File config;
	
	public KitParser(File config) {
		this.config = config;
	}
	public abstract List<Kit> parseKits() throws KitParseException;

	/**
	 * Checks if it is an integer.
	 *
	 * @param string the string
	 * @return true, if it is a integer
	 */
	protected boolean isInt(String string) {
		try {
			Integer.parseInt(string);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}
	
	public static class KitParseException extends IOException {

		private static final long serialVersionUID = 2051486501089066079L;
		
		public KitParseException(String string) {
			super(string);
		}
		
		public KitParseException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
