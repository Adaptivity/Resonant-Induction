/**
 * 
 */
package resonantinduction.tesla;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.ResonantInduction;
import resonantinduction.base.BlockBase;
import resonantinduction.entangler.ItemCoordLink;
import resonantinduction.render.BlockRenderingHandler;
import universalelectricity.core.vector.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
public class BlockTesla extends BlockBase implements ITileEntityProvider
{
	public BlockTesla(int id)
	{
		super("tesla", id);
		this.setTextureName(ResonantInduction.PREFIX + "machine");
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		super.onBlockAdded(world, x, y, z);
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		((TileEntityTesla) tileEntity).updatePositionStatus();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9)
	{
		TileEntity t = world.getBlockTileEntity(x, y, z);
		TileEntityTesla tileEntity = ((TileEntityTesla) t).getControllingTelsa();

		if (entityPlayer.getCurrentEquippedItem() != null)
		{
			if (entityPlayer.getCurrentEquippedItem().itemID == Item.dyePowder.itemID)
			{
				tileEntity.setDye(entityPlayer.getCurrentEquippedItem().getItemDamage());

				if (!entityPlayer.capabilities.isCreativeMode)
				{
					entityPlayer.inventory.decrStackSize(entityPlayer.inventory.currentItem, 1);
				}

				return true;
			}
			else if (entityPlayer.getCurrentEquippedItem().itemID == Item.redstone.itemID)
			{
				boolean status = tileEntity.toggleEntityAttack();

				if (!entityPlayer.capabilities.isCreativeMode)
				{
					entityPlayer.inventory.decrStackSize(entityPlayer.inventory.currentItem, 1);
				}

				if (!world.isRemote)
				{
					entityPlayer.addChatMessage("Toggled entity attack to: " + status);
				}

				return true;
			}
			else if (entityPlayer.getCurrentEquippedItem().getItem() instanceof ItemCoordLink)
			{
				if (tileEntity.linked == null)
				{
					ItemCoordLink link = ((ItemCoordLink) entityPlayer.getCurrentEquippedItem().getItem());
					Vector3 linkVec = link.getLink(entityPlayer.getCurrentEquippedItem());

					if (linkVec != null)
					{
						if (!world.isRemote)
						{
							int dimID = link.getLinkDim(entityPlayer.getCurrentEquippedItem());
							World otherWorld = MinecraftServer.getServer().worldServerForDimension(dimID);

							if (linkVec.getTileEntity(otherWorld) instanceof TileEntityTesla)
							{
								tileEntity.setLink(new Vector3(((TileEntityTesla) linkVec.getTileEntity(otherWorld)).getTopTelsa()), dimID, true);

								entityPlayer.addChatMessage("Linked " + this.getLocalizedName() + " with " + " [" + (int) linkVec.x + ", " + (int) linkVec.y + ", " + (int) linkVec.z + "]");

								link.clearLink(entityPlayer.getCurrentEquippedItem());
								world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "ambient.weather.thunder", 5, 1);

								return true;
							}
						}
					}
				}
				else
				{
					tileEntity.setLink(null, world.provider.dimensionId, true);

					if (!world.isRemote)
					{
						entityPlayer.addChatMessage("Unlinked Tesla.");
					}

					return true;
				}
			}
		}
		else
		{
			boolean receiveMode = tileEntity.toggleReceive();

			if (world.isRemote)
			{
				entityPlayer.addChatMessage("Tesla receive mode is now " + receiveMode);
			}
			return true;

		}

		return false;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int id)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		((TileEntityTesla) tileEntity).updatePositionStatus();
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityTesla();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderType()
	{
		return BlockRenderingHandler.INSTANCE.getRenderId();
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
