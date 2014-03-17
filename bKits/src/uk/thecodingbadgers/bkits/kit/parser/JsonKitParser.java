package uk.thecodingbadgers.bkits.kit.parser;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import uk.thecodingbadgers.bkits.InventoryAliases;
import uk.thecodingbadgers.bkits.bKits;
import uk.thecodingbadgers.bkits.kit.Kit;

public class JsonKitParser extends KitParser {

	public JsonKitParser(File config) {
		super(config);
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<Kit> parseKits() throws KitParseException {
		JsonObject json = null;
		
		try {
			JsonParser parser = new JsonParser();
			json = parser.parse(new FileReader(config)).getAsJsonObject();
		} catch (Exception e) {
			throw new KitParseException("Error parsing file " + config.getName(), e);
		}
		
		try {
			JsonArray jsonKits = json.get("kits").getAsJsonArray();
			List<Kit> kits = new ArrayList<Kit>();
			
			for (JsonElement element : jsonKits) {
				if (!element.isJsonObject()) {
					throw new KitParseException("Kit element is not a json object");
				}
				
				JsonObject kit = element.getAsJsonObject();
				
				List<ItemStack> items = new ArrayList<ItemStack>();
				JsonArray jsonItems = kit.get("items").getAsJsonArray();	
				
				for (JsonElement itemElement : jsonItems) {
					if (!itemElement.isJsonObject()) {
						throw new KitParseException("Item in kit is not a json object");
					}
					
					JsonObject item = itemElement.getAsJsonObject();
					
					Material material;
					if (item.has("name")) {
						material = InventoryAliases.getFromAlias(item.get("name").getAsString());
						
						if (material == null) {
							bKits.getInstance().getLogger().warning(item.get("name").getAsString() + " is not a valid item name");
							continue;
						}
					} else if (item.has("id")){
						material = Material.getMaterial(item.get("id").getAsInt());

						if (material == null) {
							bKits.getInstance().getLogger().warning(item.get("id").getAsInt() + " is not a valid item id");
							continue;
						}
					} else {
						throw new KitParseException("Could not find item identifier");
					}
					
					short dv = 0;			
					if (item.has("data")) {
						dv = item.get("data").getAsShort();
					}
					
					int amount = 1;			
					if (item.has("amount")) {
						amount = item.get("amount").getAsInt();
					}
					
					// clamp to max stack size
					if (amount > material.getMaxStackSize()) {
						bKits.getInstance().getLogger().warning(amount + " is bigger than the maximum stack size for " + material.name() + " clamping to max stack size");
						amount = material.getMaxStackSize();
					}
					
					ItemStack stack = new ItemStack(material, amount, dv);
					
					if (item.has("enchantments")) {
						for (JsonElement enchElement : item.get("enchantments").getAsJsonArray()) {
							if (!enchElement.isJsonObject()) {
								throw new KitParseException("Enchantment for item is not a json object.");
							}
							
							JsonObject ench = enchElement.getAsJsonObject();
							Enchantment enchant = Enchantment.getById(ench.get("id").getAsInt());

							if (enchant == null) {
								bKits.getInstance().getLogger().warning(ench.get("id").getAsInt() + " is not a valid enchantment id");
								continue;
							}
							
							int level = 1;
							
							if (ench.has("level")) {
								level = ench.get("level").getAsInt();
							}
							
							// force adding the enchantment
							stack.addUnsafeEnchantment(enchant, level);
						}
					}
					items.add(stack);
				}
				kits.add(new Kit(kit.get("name").getAsString(), kit.get("timeout").getAsLong(), items));
			}
			
			return kits;		
		} catch (Exception ex) {
			throw new KitParseException("Malformed kit config file", ex);
		}
	}

}
