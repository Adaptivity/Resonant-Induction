/**
 * 
 */
package resonantinduction.battery;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.PacketHandler;
import resonantinduction.base.IPacketReceiver;
import resonantinduction.base.ListUtil;
import universalelectricity.compatibility.TileEntityUniversalElectrical;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.item.IItemElectric;
import universalelectricity.core.vector.Vector3;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

/**
 * A modular battery with no GUI.
 * 
 * @author AidanBrady
 */
public class TileEntityBattery extends TileEntityUniversalElectrical implements IPacketReceiver, IInventory
{
	public Set<EntityPlayer> playersUsing = new HashSet<EntityPlayer>();

	public SynchronizedBatteryData structure = SynchronizedBatteryData.getBase(this);

	public SynchronizedBatteryData prevStructure;

	public float clientEnergy;
	public int clientCells;
	public float clientMaxEnergy;

	private EnumSet inputSides = EnumSet.allOf(ForgeDirection.class);

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (!this.worldObj.isRemote)
		{
			if (this.ticks == 5 && !structure.isMultiblock)
			{
				this.update();
			}

			if (structure.visibleInventory[0] != null)
			{
				if (structure.inventory.size() < structure.getMaxCells())
				{
					structure.inventory.add(structure.visibleInventory[0]);
					structure.visibleInventory[0] = null;
					structure.sortInventory();
					updateAllClients();
				}
			}

			if (structure.visibleInventory[1] != null)
			{
				ItemStack itemStack = structure.visibleInventory[1];
				IItemElectric battery = (IItemElectric) itemStack.getItem();

				float energyStored = getMaxEnergyStored();
				float batteryNeeded = battery.getMaxElectricityStored(itemStack) - battery.getElectricityStored(itemStack);
				float toGive = Math.min(energyStored, Math.min(battery.getTransfer(itemStack), batteryNeeded));

				battery.setElectricity(itemStack, battery.getElectricityStored(itemStack) + provideElectricity(toGive, true).getWatts());
			}

			if (structure.visibleInventory[2] != null)
			{
				ItemStack itemStack = structure.visibleInventory[2];
				IItemElectric battery = (IItemElectric) itemStack.getItem();

				float energyNeeded = getMaxEnergyStored() - getEnergyStored();
				float batteryStored = battery.getElectricityStored(itemStack);
				float toReceive = Math.min(energyNeeded, Math.min(battery.getTransfer(itemStack), batteryStored));

				battery.setElectricity(itemStack, battery.getElectricityStored(itemStack) - receiveElectricity(toReceive, true));
			}

			if (prevStructure != structure)
			{
				for (EntityPlayer player : playersUsing)
				{
					player.closeScreen();
				}

				updateClient();
			}

			prevStructure = structure;

			structure.wroteInventory = false;
			structure.didTick = false;

			if (playersUsing.size() > 0)
			{
				updateClient();
			}

			for (EntityPlayer player : this.playersUsing)
			{
				PacketDispatcher.sendPacketToPlayer(PacketHandler.getTileEntityPacket(this, this.getNetworkedData(new ArrayList()).toArray()), (Player) player);
			}

			this.produce();
		}
	}

	public void updateClient()
	{
		PacketHandler.sendPacketToAllPlayers(this, getNetworkedData(new ArrayList()).toArray());
	}

	public void updateAllClients()
	{
		for (Vector3 vec : structure.locations)
		{
			TileEntityBattery battery = (TileEntityBattery) vec.getTileEntity(worldObj);
			PacketHandler.sendPacketToAllPlayers(battery, battery.getNetworkedData(new ArrayList()).toArray());
		}
	}

	@Override
	public void validate()
	{
		super.validate();

		if (worldObj.isRemote)
		{
			PacketHandler.sendDataRequest(this);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		// Main inventory
		if (nbtTags.hasKey("Items"))
		{
			NBTTagList tagList = nbtTags.getTagList("Items");
			structure.inventory = new ArrayList<ItemStack>();

			for (int tagCount = 0; tagCount < tagList.tagCount(); tagCount++)
			{
				NBTTagCompound tagCompound = (NBTTagCompound) tagList.tagAt(tagCount);
				int slotID = tagCompound.getInteger("Slot");
				structure.inventory.add(slotID, ItemStack.loadItemStackFromNBT(tagCompound));
			}
		}

		// Visible inventory
		if (nbtTags.hasKey("VisibleItems"))
		{
			NBTTagList tagList = nbtTags.getTagList("VisibleItems");
			structure.visibleInventory = new ItemStack[3];

			for (int tagCount = 0; tagCount < tagList.tagCount(); tagCount++)
			{
				NBTTagCompound tagCompound = (NBTTagCompound) tagList.tagAt(tagCount);
				byte slotID = tagCompound.getByte("Slot");

				if (slotID >= 0 && slotID < structure.visibleInventory.length)
				{
					if (slotID == 0)
					{
						setInventorySlotContents(slotID, ItemStack.loadItemStackFromNBT(tagCompound));
					}
					else
					{
						setInventorySlotContents(slotID + 1, ItemStack.loadItemStackFromNBT(tagCompound));
					}
				}
			}
		}

		if (nbtTags.hasKey("inputSides"))
		{
			this.inputSides = EnumSet.noneOf(ForgeDirection.class);

			NBTTagList tagList = nbtTags.getTagList("inputSides");

			for (int tagCount = 0; tagCount < tagList.tagCount(); tagCount++)
			{
				NBTTagCompound tagCompound = (NBTTagCompound) tagList.tagAt(tagCount);
				byte side = tagCompound.getByte("side");
				this.inputSides.add(ForgeDirection.getOrientation(side));
			}

			this.inputSides.remove(ForgeDirection.UNKNOWN);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		if (!structure.wroteInventory)
		{
			// Inventory
			if (structure.inventory != null)
			{
				NBTTagList tagList = new NBTTagList();

				for (int slotCount = 0; slotCount < structure.inventory.size(); slotCount++)
				{
					if (structure.inventory.get(slotCount) != null)
					{
						NBTTagCompound tagCompound = new NBTTagCompound();
						tagCompound.setInteger("Slot", slotCount);
						structure.inventory.get(slotCount).writeToNBT(tagCompound);
						tagList.appendTag(tagCompound);
					}
				}

				nbt.setTag("Items", tagList);
			}

			// Visible inventory
			if (structure.visibleInventory != null)
			{
				NBTTagList tagList = new NBTTagList();

				for (int slotCount = 0; slotCount < structure.visibleInventory.length; slotCount++)
				{
					if (slotCount > 0)
					{
						slotCount++;
					}

					if (getStackInSlot(slotCount) != null)
					{
						NBTTagCompound tagCompound = new NBTTagCompound();
						tagCompound.setByte("Slot", (byte) slotCount);
						getStackInSlot(slotCount).writeToNBT(tagCompound);
						tagList.appendTag(tagCompound);
					}
				}

				nbt.setTag("VisibleItems", tagList);
			}

			structure.wroteInventory = true;

			/**
			 * Save the input sides.
			 */
			NBTTagList tagList = new NBTTagList();
			Iterator<ForgeDirection> it = this.inputSides.iterator();

			while (it.hasNext())
			{
				ForgeDirection dir = it.next();
				if (this.inputSides.contains(dir) && dir != ForgeDirection.UNKNOWN)
				{
					NBTTagCompound tagCompound = new NBTTagCompound();
					tagCompound.setByte("side", (byte) dir.ordinal());
					tagList.appendTag(tagCompound);
				}
			}

			nbt.setTag("inputSides", tagList);
		}
	}

	public void update()
	{
		if (!worldObj.isRemote && (structure == null || !structure.didTick))
		{
			new BatteryUpdateProtocol(this).updateBatteries();

			if (structure != null)
			{
				structure.didTick = true;
			}
		}
	}

	@Override
	public float receiveElectricity(ElectricityPack receive, boolean doAdd)
	{
		float amount = receive.getWatts();
		float added = 0;

		for (ItemStack itemStack : structure.inventory)
		{
			if (itemStack.getItem() instanceof IItemElectric)
			{
				IItemElectric battery = (IItemElectric) itemStack.getItem();

				float needed = amount - added;
				float itemAdd = Math.min(battery.getMaxElectricityStored(itemStack) - battery.getElectricityStored(itemStack), needed);

				if (doAdd)
				{
					battery.setElectricity(itemStack, battery.getElectricityStored(itemStack) + itemAdd);
				}

				added += itemAdd;

				if (amount == added)
				{
					break;
				}
			}
		}

		return added;
	}

	@Override
	public ElectricityPack provideElectricity(ElectricityPack pack, boolean doRemove)
	{
		float amount = pack.getWatts();

		List<ItemStack> inverse = ListUtil.inverse(structure.inventory);

		float removed = 0;
		for (ItemStack itemStack : inverse)
		{
			if (itemStack.getItem() instanceof IItemElectric)
			{
				IItemElectric battery = (IItemElectric) itemStack.getItem();

				float needed = amount - removed;
				float itemRemove = Math.min(battery.getElectricityStored(itemStack), needed);

				if (doRemove)
				{
					battery.setElectricity(itemStack, battery.getElectricityStored(itemStack) - itemRemove);
				}

				removed += itemRemove;

				if (amount == removed)
				{
					break;
				}
			}
		}

		return ElectricityPack.getFromWatts(removed, this.getVoltage());
	}

	@Override
	public float getMaxEnergyStored()
	{
		if (!this.worldObj.isRemote)
		{
			float max = 0;

			for (ItemStack itemStack : this.structure.inventory)
			{
				if (itemStack != null)
				{
					if (itemStack.getItem() instanceof IItemElectric)
					{
						max += ((IItemElectric) itemStack.getItem()).getMaxElectricityStored(itemStack);
					}
				}
			}

			return max;
		}
		else
		{
			return this.clientMaxEnergy;
		}
	}

	@Override
	public float getEnergyStored()
	{
		if (!this.worldObj.isRemote)
		{
			float energy = 0;

			for (ItemStack itemStack : this.structure.inventory)
			{
				if (itemStack != null)
				{
					if (itemStack.getItem() instanceof IItemElectric)
					{
						energy += ((IItemElectric) itemStack.getItem()).getElectricityStored(itemStack);
					}
				}
			}

			return energy;
		}
		else
		{
			return clientEnergy;
		}
	}

	@Override
	public void handle(ByteArrayDataInput input)
	{
		try
		{
			structure.isMultiblock = input.readBoolean();

			clientEnergy = input.readFloat();
			clientCells = input.readInt();
			clientMaxEnergy = input.readFloat();

			structure.height = input.readInt();
			structure.length = input.readInt();
			structure.width = input.readInt();
		}
		catch (Exception e)
		{
		}
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		data.add(structure.isMultiblock);

		data.add(getEnergyStored());
		data.add(structure.inventory.size());
		data.add(getMaxEnergyStored());

		data.add(structure.height);
		data.add(structure.length);
		data.add(structure.width);

		return data;
	}

	@Override
	public int getSizeInventory()
	{
		return 4;
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		if (i == 0)
		{
			return structure.visibleInventory[0];
		}
		else if (i == 1)
		{
			if (!worldObj.isRemote)
			{
				return ListUtil.getTop(structure.inventory);
			}
			else
			{
				return structure.tempStack;
			}
		}
		else
		{
			return structure.visibleInventory[i - 1];
		}
	}

	@Override
	public ItemStack decrStackSize(int slotID, int amount)
	{
		if (getStackInSlot(slotID) != null)
		{
			ItemStack tempStack;

			if (getStackInSlot(slotID).stackSize <= amount)
			{
				tempStack = getStackInSlot(slotID);
				setInventorySlotContents(slotID, null);
				return tempStack;
			}
			else
			{
				tempStack = getStackInSlot(slotID).splitStack(amount);

				if (getStackInSlot(slotID).stackSize == 0)
				{
					setInventorySlotContents(slotID, null);
				}

				return tempStack;
			}
		}
		else
		{
			return null;
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i)
	{
		return getStackInSlot(i);
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		if (i == 0)
		{
			structure.visibleInventory[0] = itemstack;
		}
		else if (i == 1)
		{
			if (itemstack == null)
			{
				if (!worldObj.isRemote)
				{
					structure.inventory.remove(ListUtil.getTop(structure.inventory));
				}
				else
				{
					structure.tempStack = null;
				}
			}
			else
			{
				if (worldObj.isRemote)
				{
					structure.tempStack = itemstack;
				}
			}
		}
		else
		{
			structure.visibleInventory[i - 1] = itemstack;
		}
	}

	@Override
	public String getInvName()
	{
		return "Battery";
	}

	@Override
	public boolean isInvNameLocalized()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return true;
	}

	@Override
	public void openChest()
	{
	}

	@Override
	public void closeChest()
	{
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemsSack)
	{
		return itemsSack.getItem() instanceof IItemElectric;
	}

	@Override
	public float getRequest(ForgeDirection direction)
	{
		return this.getMaxEnergyStored() - this.getEnergyStored();
	}

	@Override
	public float getProvide(ForgeDirection direction)
	{
		return this.getEnergyStored();
	}

	@Override
	public EnumSet<ForgeDirection> getInputDirections()
	{
		return this.inputSides;
	}

	@Override
	public EnumSet<ForgeDirection> getOutputDirections()
	{
		return EnumSet.complementOf(this.inputSides);
	}

	/**
	 * Toggles the input/output sides of the battery.
	 */
	public boolean toggleSide(ForgeDirection orientation)
	{
		if (this.inputSides.contains(orientation))
		{
			this.inputSides.remove(orientation);
			return false;
		}
		else
		{
			this.inputSides.add(orientation);
			return true;
		}
	}
}
