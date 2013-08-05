package resonantinduction.contractor;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.ResonantInduction;
import resonantinduction.base.BlockBase;
import resonantinduction.base.Vector3;
import resonantinduction.entangler.ItemCoordLink;
import resonantinduction.render.BlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockEMContractor extends BlockBase implements ITileEntityProvider
{
	public BlockEMContractor(int id)
	{
		super("contractor", id);
		this.func_111022_d(ResonantInduction.PREFIX + "machine");
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderType()
	{
		return BlockRenderingHandler.INSTANCE.getRenderId();
	}

	@Override
	public boolean onBlockActivated(World world, int par2, int par3, int par4, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9)
	{
		TileEntityEMContractor contractor = (TileEntityEMContractor) world.getBlockTileEntity(par2, par3, par4);

		if (entityPlayer.getCurrentEquippedItem() != null)
		{
			if (entityPlayer.getCurrentEquippedItem().itemID == Item.dyePowder.itemID)
			{
				contractor.setDye(entityPlayer.getCurrentEquippedItem().getItemDamage());

				if (!entityPlayer.capabilities.isCreativeMode)
				{
					entityPlayer.inventory.decrStackSize(entityPlayer.inventory.currentItem, 1);
				}
				return true;
			}
			else if (entityPlayer.getCurrentEquippedItem().getItem() instanceof ItemCoordLink)
			{
				ItemCoordLink link = ((ItemCoordLink) entityPlayer.getCurrentEquippedItem().getItem());
				Vector3 linkVec = link.getLink(entityPlayer.getCurrentEquippedItem());

				if (linkVec != null)
				{
					if (linkVec.getTileEntity(world) instanceof TileEntityEMContractor)
					{
						contractor.setLink((TileEntityEMContractor) linkVec.getTileEntity(world), true);

						if (!world.isRemote)
						{
							entityPlayer.addChatMessage("Linked " + this.getLocalizedName() + " with " + " [" + (int) linkVec.x + ", " + (int) linkVec.y + ", " + (int) linkVec.z + "]");
						}

						link.clearLink(entityPlayer.getCurrentEquippedItem());

						return true;
					}
				}

				return false;
			}
		}

		if (!entityPlayer.isSneaking())
		{
			contractor.incrementFacing();
		}
		else
		{
			contractor.suck = !contractor.suck;
		}

		return true;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int blockID)
	{
		TileEntityEMContractor tileContractor = (TileEntityEMContractor) world.getBlockTileEntity(x, y, z);

		if (!world.isRemote && !tileContractor.isLatched())
		{
			for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tileEntity = world.getBlockTileEntity(x + side.offsetX, y + side.offsetY, z + side.offsetZ);

				if (tileEntity instanceof IInventory)
				{
					tileContractor.setFacing(side.getOpposite());
					return;
				}
			}
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityEMContractor();
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}
}
