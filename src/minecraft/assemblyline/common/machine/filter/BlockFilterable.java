package assemblyline.common.machine.filter;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import universalelectricity.prefab.BlockMachine;
import universalelectricity.prefab.implement.IRedstoneReceptor;
import assemblyline.api.IFilterable;

/**
 * Extend this block class if a filter is allowed to be placed inside of this block.
 * 
 * @author Calclavia
 */
public abstract class BlockFilterable extends BlockMachine
{
	public BlockFilterable(String name, int id, Material material, CreativeTabs creativeTab)
	{
		super(name, id, material, creativeTab);
	}

	/**
	 * Allows filters to be placed inside of this block.
	 */
	@Override
	public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity != null)
		{
			if (tileEntity instanceof IFilterable)
			{
				ItemStack containingStack = ((IFilterable) tileEntity).getFilter();

				if (containingStack != null)
				{
					if (!world.isRemote)
					{
						EntityItem dropStack = new EntityItem(world, player.posX, player.posY, player.posZ, containingStack);
						dropStack.delayBeforeCanPickup = 0;
						world.spawnEntityInWorld(dropStack);
					}

					((IFilterable) tileEntity).setFilter(null);
					return true;
				}
				else
				{
					if (player.getCurrentEquippedItem() != null)
					{
						if (player.getCurrentEquippedItem().getItem() instanceof ItemImprinter)
						{
							((IFilterable) tileEntity).setFilter(player.getCurrentEquippedItem());
							player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
							return true;
						}
					}
				}

			}
		}

		return false;
	}

	@Override
	public boolean onSneakMachineActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		return this.onMachineActivated(world, x, y, z, player, side, hitX, hitY, hitZ);
	}

	@Override
	public void onNeighborBlockChange(World par1World, int x, int y, int z, int side)
	{
		super.onNeighborBlockChange(par1World, x, y, z, side);

		TileEntity tileEntity = par1World.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof IRedstoneReceptor)
		{
			if (par1World.isBlockIndirectlyGettingPowered(x, y, z))
			{
				((IRedstoneReceptor) par1World.getBlockTileEntity(x, y, z)).onPowerOn();
			}
		}
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving par5EntityLiving)
	{
		int angle = MathHelper.floor_double((par5EntityLiving.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		int change = 2;

		switch (angle)
		{
			case 0:
				change = 2;
				break;
			case 1:
				change = 5;
				break;
			case 2:
				change = 3;
				break;
			case 3:
				change = 4;
				break;

		}
		world.setBlockMetadataWithNotify(x, y, z, change);
	}

	@Override
	public boolean onUseWrench(World world, int x, int y, int z, EntityPlayer par5EntityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		int original = world.getBlockMetadata(x, y, z);
		int change = 2;

		switch (original)
		{
			case 2:
				change = 4;
				break;
			case 3:
				change = 5;
				break;
			case 4:
				change = 3;
				break;
			case 5:
				change = 2;
				break;

		}

		world.setBlockMetadataWithNotify(x, y, z, change);

		return true;
	}
}
