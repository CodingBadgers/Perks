package uk.thecodingbadgers.bkits.kit.parser;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.thecodingbadgers.bkits.kit.Kit;

public enum ParserType {

	CFG("cfg", CfgKitParser.class),
	JSON("json", JsonKitParser.class),
	;
	
	private static final Map<String, ParserType> BY_EXTENTION = new HashMap<String, ParserType>();
	
	static {
		for (ParserType value : values()) {
			BY_EXTENTION.put(value.getExtention(), value);
		}
	}
	
	private String extention;
	private Class<? extends KitParser> clazz;

	private ParserType(String end, Class<? extends KitParser> clazz) {
		this.extention = end;
		this.clazz = clazz;
	}
	
	public String getExtention() {
		return extention;
	}
	
	public List<Kit> parseConfig(File config) throws IOException {
		try {
			Constructor<? extends KitParser> ctor = clazz.getConstructor(File.class);
			KitParser parser = ctor.newInstance(config);
			return parser.parseKits();
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new KitParser.KitParseException("An error has occured whilst creating the kit parser", e);
		}
	}
	
	public static List<Kit> parse(File config) throws IOException {
		ParserType type = findByExtention(config.getName().substring(config.getName().lastIndexOf('.')));
		
		if (type == null) {
			throw new IOException("Cannot find parser for file " + config.getName());
		}
		
		return type.parseConfig(config);
	}
	
	public static ParserType findByExtention(String extention) {
		return BY_EXTENTION.get(extention);
	}
}
