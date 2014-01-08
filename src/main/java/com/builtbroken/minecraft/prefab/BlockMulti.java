package com.builtbroken.minecraft.prefab;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import universalelectricity.api.UniversalElectricity;
import universalelectricity.api.vector.Vector3;

import com.builtbroken.minecraft.DarkCore;
import com.builtbroken.minecraft.interfaces.IMultiBlock;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockMulti extends BlockContainer
{
	public String textureName = null;

	public BlockMulti()
	{
		super(DarkCore.CONFIGURATION.getBlock("multiBlock", DarkCore.getNextID()).getInt(), UniversalElectricity.machine);
		this.setHardness(0.8F);
		this.setUnlocalizedName("multiBlock");
	}

	@Override
	public BlockMulti setTextureName(String name)
	{
		this.textureName = name;
		return this;
	}

	public void createMultiBlockStructure(IMultiBlock tile)
	{
		TileEntity tileEntity = (TileEntity) tile;
		Vector3[] positions = tile.getMultiBlockVectors();

		for (Vector3 position : positions)
		{
			makeFakeBlock(tileEntity.worldObj, new Vector3(tileEntity).translate(position), new Vector3(tileEntity));
		}
	}

	public void destroyMultiBlockStructure(IMultiBlock tile)
	{
		TileEntity tileEntity = (TileEntity) tile;
		Vector3[] positions = tile.getMultiBlockVectors();

		for (Vector3 position : positions)
		{
			new Vector3(tileEntity).translate(position).setBlock(tileEntity.worldObj, 0);
		}

		new Vector3(tileEntity).setBlock(tileEntity.worldObj, 0);
	}

	public void makeFakeBlock(World worldObj, Vector3 position, Vector3 mainBlock)
	{
		// Creates a fake block, then sets the relative main block position.
		worldObj.setBlock(position.intX(), position.intY(), position.intZ(), this.blockID);
		((TileEntityMulti) worldObj.getBlockTileEntity(position.intX(), position.intY(), position.intZ())).setMainBlock(mainBlock);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IconRegister iconRegister)
	{
		if (this.textureName != null)
		{
			this.blockIcon = iconRegister.registerIcon(this.textureName);
		}
		else
		{
			super.registerIcons(iconRegister);
		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int par5, int par6)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileEntityMulti)
		{
			((TileEntityMulti) tileEntity).onBlockRemoval(this);
		}

		super.breakBlock(world, x, y, z, par5, par6);
	}

	/**
	 * Called when the block is right clicked by the player. This modified version detects electric
	 * items and wrench actions on your machine block. Do not override this function. Use
	 * machineActivated instead! (It does the same thing)
	 */
	@Override
	public boolean onBlockActivated(World par1World, int x, int y, int z, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)
	{
		TileEntityMulti tileEntity = (TileEntityMulti) par1World.getBlockTileEntity(x, y, z);
		return tileEntity.onBlockActivated(par1World, x, y, z, par5EntityPlayer);
	}

	/**
	 * Returns the quantity of items to drop on block destruction.
	 */
	@Override
	public int quantityDropped(Random par1Random)
	{
		return 0;
	}

	@Override
	public int getRenderType()
	{
		return -1;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return new TileEntityMulti();
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World par1World, int x, int y, int z)
	{
		TileEntity tileEntity = par1World.getBlockTileEntity(x, y, z);
		Vector3 mainBlockPosition = ((TileEntityMulti) tileEntity).getMainBlock();

		if (mainBlockPosition != null)
		{
			int mainBlockID = par1World.getBlockId(mainBlockPosition.intX(), mainBlockPosition.intY(), mainBlockPosition.intZ());

			if (mainBlockID > 0)
			{
				return Block.blocksList[mainBlockID].getPickBlock(target, par1World, mainBlockPosition.intX(), mainBlockPosition.intY(), mainBlockPosition.intZ());
			}
		}

		return null;
	}
}