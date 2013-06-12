package dark.library.machine.terminal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;

public abstract class ContainerTerminal extends Container
{
	private TileEntityTerminal tileEntity;

	public ContainerTerminal(InventoryPlayer inventoryPlayer, TileEntityTerminal tileEntity)
	{
		this.tileEntity = tileEntity;
		this.tileEntity.playersUsing.add(inventoryPlayer.player);
	}

	@Override
	public void onCraftGuiClosed(EntityPlayer par1EntityPlayer)
	{
		this.tileEntity.playersUsing.remove(par1EntityPlayer);
		super.onCraftGuiClosed(par1EntityPlayer);
	}

	@Override
	public boolean canInteractWith(EntityPlayer par1EntityPlayer)
	{
		if (tileEntity instanceof IInventory)
		{
			return ((IInventory) this.tileEntity).isUseableByPlayer(par1EntityPlayer);
		}
		return true;
	}
}
