package me.filoghost.chestcommands.parsing;

import me.filoghost.chestcommands.logging.ErrorMessages;
import me.filoghost.commons.MaterialsHelper;
import org.bukkit.Material;

public class MaterialParser {

	public static Material parseMaterial(String materialName) throws ParseException {
		return MaterialsHelper.matchMaterial(materialName)
				.orElseThrow(() -> new ParseException(ErrorMessages.Parsing.unknownMaterial(materialName)));
	}

}
