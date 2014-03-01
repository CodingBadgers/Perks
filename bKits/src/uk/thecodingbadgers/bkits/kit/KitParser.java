package uk.thecodingbadgers.bkits.kit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import uk.thecodingbadgers.bkits.bKits;

/**
 * The Class KitParser.
 */
public class KitParser {

	/** The config. */
	private File config;
	
	/**
	 * Instantiates a new kit parser.
	 *
	 * @param config the config
	 */
	public KitParser(File config) {
		this.config = config;
	}

	/**
	 * Parses the kits.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings("deprecation")
	public void parseKits() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(config));

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
                KitHandler.getInstance().addKit(kit);
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

                if (isNumeric(ammountString)) {
                    amount = Integer.parseInt(ammountString);
                    line = line.substring(0, line.indexOf(','));
                }
            }

            if (isNumeric(line)) {
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
            KitHandler.getInstance().addKit(kit);
        }

        bKits.getInstance().getLogger().info(bKits.getInstance().getLanguageValue("loaded").replace("%number%", "" + KitHandler.size()));
	}

	/**
	 * Checks if is numeric.
	 *
	 * @param string the string
	 * @return true, if is numeric
	 */
	private boolean isNumeric(String string) {
		try {
			Integer.parseInt(string);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}
}
