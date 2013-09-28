package uk.thecodingbadgers.bkits.kit.parser;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import uk.thecodingbadgers.bkits.bKits;
import uk.thecodingbadgers.bkits.kit.Kit;

public class JsonKitParser extends KitParser {

	public JsonKitParser(File config) {
		super(config);
	}

	@Override
	public List<Kit> parseKits() throws KitParseException {
		JSONObject json = null;
		
		try {
			JSONParser parser = new JSONParser();
			json = (JSONObject) parser.parse(new FileReader(config));
		} catch (Exception e) {
			throw new KitParseException("Error parsing file " + config.getName(), e);
		}
		
		try {
			JSONArray jsonKits = (JSONArray) json.get("kits");
			List<Kit> kits = new ArrayList<Kit>();
			
			for (Object object : jsonKits) {
				JSONObject kit = (JSONObject) object;
				
				List<ItemStack> items = new ArrayList<ItemStack>();
				JSONArray jsonItems = (JSONArray) kit.get("items");		
				for (Object itemObject : jsonItems) {
					JSONObject item = (JSONObject) itemObject;
					
					Material material;
					if (item.containsKey("name")) {
						material = Material.getMaterial((String) item.get("name"));
						
						if (material == null) {
							bKits.getInstance().getLogger().warning((String) item.get("name") + " is not a valid item name");
							continue;
						}
					} else if (item.containsKey("id")){
						material = Material.getMaterial(((Number) item.get("id")).intValue());

						if (material == null) {
							bKits.getInstance().getLogger().warning(((Number) item.get("id")).intValue() + " is not a valid item id");
							continue;
						}
					} else {
						throw new KitParseException("Could not find item identifier");
					}
					
					short dv = 0;			
					if (item.containsKey("dv")) {
						dv = ((Number) item.get("dv")).shortValue();
					}
					
					int amount = 1;			
					if (item.containsKey("amount")) {
						amount = ((Number) item.get("amount")).intValue();
					}
					
					// clamp to max stack size
					if (amount > material.getMaxStackSize()) {
						bKits.getInstance().getLogger().warning(amount + " is bigger than the maximum stack size for " + material.name() + " clamping to max stack size");
						amount = material.getMaxStackSize();
					}
					
					ItemStack stack = new ItemStack(material, amount, dv);
					
					if (item.containsKey("enchantments")) {
						for (Object enchObject : (JSONArray) item.get("enchants")) {
							JSONObject ench = (JSONObject) enchObject;
							Enchantment enchant = Enchantment.getById(((Number) ench.get("id")).intValue());

							if (enchant == null) {
								bKits.getInstance().getLogger().warning(((Number) ench.get("id")).intValue() + " is not a valid enchantment id");
								continue;
							}
							
							int level = 1;
							
							if (ench.containsKey("level")) {
								level = ((Number) ench.get("level")).intValue();
							}
							
							// force adding the enchantment
							stack.addUnsafeEnchantment(enchant, level);
						}
					}
					items.add(stack);
				}
				kits.add(new Kit((String) kit.get("name"), ((Number) kit.get("timeout")).longValue(), items));
			}
			
			return kits;		
		} catch (Exception ex) {
			throw new KitParseException("Malformed kit config file", ex);
		}
	}

}
