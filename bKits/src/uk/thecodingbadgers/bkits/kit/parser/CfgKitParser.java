package uk.thecodingbadgers.bkits.kit.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import uk.thecodingbadgers.bkits.bKits;
import uk.thecodingbadgers.bkits.kit.Kit;

/**
 * The Class KitParser.
 */
public class CfgKitParser extends KitParser {

	/**
	 * Instantiates a new kit parser.
	 *
	 * @param config the config
	 */
	public CfgKitParser(File config) {
		super(config);
	}

	/**
	 * Parses the kits.
	 * @return 
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public List<Kit> parseKits() throws KitParseException {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(config));

			List<Kit> kits = new ArrayList<Kit>();

			String name = null;
			String timeoutString = null;
			ArrayList<ItemStack> newKit = null;

			String line = null;
			while ((line = reader.readLine()) != null) {

				line = line.trim();

				if (line.startsWith("#"))
					continue;

				if (line.length() == 0 && newKit != null) {
					int timeout = Integer.parseInt(timeoutString);
					Kit kit = new Kit(name, timeout, newKit);
					kits.add(kit);
					newKit = null;
				}

				if (line.startsWith("[")) {
					name = line.substring(line.indexOf('[') + 1, line.indexOf('='));
					timeoutString = line.substring(line.indexOf('=') + 1, line.indexOf(']'));
					newKit = new ArrayList<ItemStack>();
					continue;
				}

				int amount = 1;
				Material material = null;

				if (line.indexOf(",") != -1) {

					String ammountString = line.substring(line.indexOf(',') + 1);

					if (isInt(ammountString)) {
						amount = Integer.parseInt(ammountString);
						line = line.substring(0, line.indexOf(','));
					}
				}

				if (isInt(line)) {
					if (Material.getMaterial(Integer.parseInt(line)) == null) {
						bKits.getInstance().getLogger().warning(bKits.getInstance().getLanguageValue("no-item-exists").replace("%item%", line));
						continue;
					}

					material = Material.getMaterial(Integer.parseInt(line));
				}

				if (material == null)
					continue;

				ItemStack item = new ItemStack(material, amount);
				newKit.add(item);
			}

			reader.close();

			if (newKit != null) {
				int timeout = Integer.parseInt(timeoutString);
				Kit kit = new Kit(name, timeout, newKit);
				kits.add(kit);
			}

			bKits.getInstance().getLogger().info(bKits.getInstance().getLanguageValue("loaded").replace("%number%", "" + kits.size()));
			return kits;
		} catch (IOException e) {
			throw new KitParseException(e.getMessage(), e);
		}
	}

}
