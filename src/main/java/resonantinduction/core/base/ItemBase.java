package resonantinduction.core.base;

import net.minecraft.item.Item;
import net.minecraftforge.common.Configuration;
import resonantinduction.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.ResonantInductionTabs;
import resonantinduction.core.Settings;

/**
 * 
 * @author AidanBrady
 * 
 */
public class ItemBase extends Item
{
	public ItemBase(String name, int id)
	{
		super(Settings.CONFIGURATION.get(Configuration.CATEGORY_ITEM, name, id).getInt(id));
		this.setCreativeTab(ResonantInductionTabs.CORE);
		this.setUnlocalizedName(Reference.PREFIX + name);
		this.setTextureName(Reference.PREFIX + name);
	}
}
