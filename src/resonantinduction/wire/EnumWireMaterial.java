package resonantinduction.wire;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import universalelectricity.core.vector.Vector3;

/**
 * An enumerator for different wire materials. The metadata of the wire determines the type of the
 * wire.
 * 
 * @author Calclavia
 * 
 */

public enum EnumWireMaterial
{
	COPPER("Copper", 12.5F, 3, 2, new Vector3(184, 115, 51)),
	TIN("Tin", 13, 2, 0.5F, new Vector3(132, 132, 130)),
	IRON("Iron", 0.1F, 2, 4, new Vector3(97, 102, 105)),
	ALUMINUM("Aluminum", 0.025F, 6, 0.15F, new Vector3(215, 205, 181)),
	SILVER("Silver", 0.005F, 1, 2, new Vector3(192, 192, 192)),
	SUPERCONDUCTOR("Superconductor", 0, 1, 2, new Vector3(192, 192, 192));

	public final float resistance;
	public final float damage;
	public final float maxAmps;
	public final Vector3 color;
	private ItemStack wire;
	private final String name;

	private EnumWireMaterial(String s, float resist, float electrocution, float max, Vector3 vec)
	{
		name = s;
		resistance = resist;
		damage = electrocution;
		maxAmps = max;
		color = vec.scale(1D / 255D);
	}

	public String getName()
	{
		return name;
	}

	public ItemStack getWire()
	{
		return getWire(1);
	}

	public ItemStack getWire(int amount)
	{
		ItemStack returnStack = wire.copy();
		returnStack.stackSize = amount;

		return returnStack;
	}

	public void setWire(ItemStack item)
	{
		if (wire == null)
		{
			wire = item;
			OreDictionary.registerOre(getName().toLowerCase() + "Wire", wire);
		}
	}

	public void setWire(Item item)
	{
		setWire(new ItemStack(item, 1, ordinal()));
	}

	public void setWire(Block block)
	{
		setWire(new ItemStack(block, 1, ordinal()));
	}
}