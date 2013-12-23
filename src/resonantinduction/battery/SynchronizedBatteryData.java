package resonantinduction.battery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.item.ItemStack;
import resonantinduction.base.ListUtil;
import universalelectricity.api.item.IElectricalItem;
import universalelectricity.api.vector.Vector3;

public class SynchronizedBatteryData
{
	public Set<Vector3> locations = new HashSet<Vector3>();

	public List<ItemStack> inventory = new ArrayList<ItemStack>();

	/**
	 * Slot 0: Cell input slot Slot 1: Battery charge slot Slot 2: Battery discharge slot
	 */
	public ItemStack[] visibleInventory = new ItemStack[3];

	public int length;

	public int width;

	public int height;

	public ItemStack tempStack;

	public boolean isMultiblock;

	public boolean didTick;

	public boolean wroteInventory;

	public int getVolume()
	{
		return length * width * height;
	}

	public int getMaxCells()
	{
		return getVolume() * BatteryManager.CELLS_PER_BATTERY;
	}

	public boolean addCell(ItemStack cell)
	{
		if (this.inventory.size() < this.getMaxCells())
		{
			this.inventory.add(cell);
			this.sortInventory();
			return true;
		}

		return false;
	}

	public void sortInventory()
	{
		Object[] array = ListUtil.copy(inventory).toArray();

		ItemStack[] toSort = new ItemStack[array.length];

		for (int i = 0; i < array.length; i++)
		{
			toSort[i] = (ItemStack) array[i];
		}

		boolean cont = true;
		ItemStack temp;

		while (cont)
		{
			cont = false;

			for (int i = 0; i < toSort.length - 1; i++)
			{
				if (((IElectricalItem) toSort[i].getItem()).getElectricityStored(toSort[i]) < ((IElectricalItem) toSort[i + 1].getItem()).getElectricityStored(toSort[i + 1]))
				{
					temp = toSort[i];
					toSort[i] = toSort[i + 1];
					toSort[i + 1] = temp;
					cont = true;
				}
			}
		}

		inventory = new ArrayList<ItemStack>();

		for (ItemStack itemStack : toSort)
		{
			inventory.add(itemStack);
		}
	}

	public boolean hasVisibleInventory()
	{
		for (ItemStack itemStack : visibleInventory)
		{
			if (itemStack != null)
			{
				return true;
			}
		}

		return false;
	}

	public static SynchronizedBatteryData getBase(TileEntityBattery tileEntity, List<ItemStack> inventory)
	{
		SynchronizedBatteryData structure = getBase(tileEntity);
		structure.inventory = inventory;

		return structure;
	}

	public static SynchronizedBatteryData getBase(TileEntityBattery tileEntity)
	{
		SynchronizedBatteryData structure = new SynchronizedBatteryData();
		structure.length = 1;
		structure.width = 1;
		structure.height = 1;
		structure.locations.add(new Vector3(tileEntity));

		return structure;
	}

	@Override
	public int hashCode()
	{
		int code = 1;
		code = 31 * locations.hashCode();
		code = 31 * length;
		code = 31 * width;
		code = 31 * height;
		return code;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof SynchronizedBatteryData))
		{
			return false;
		}

		SynchronizedBatteryData data = (SynchronizedBatteryData) obj;

		if (!data.locations.equals(locations))
		{
			return false;
		}

		if (data.length != length || data.width != width || data.height != height)
		{
			return false;
		}

		return true;
	}
}
