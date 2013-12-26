package com.builtbroken.assemblyline.armbot;

import java.util.List;
import java.util.Random;
import java.util.Set;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import universalelectricity.api.UniversalElectricity;
import universalelectricity.api.vector.Vector3;

import com.builtbroken.assemblyline.AssemblyLine;
import com.builtbroken.assemblyline.blocks.BlockAssembly;
import com.builtbroken.assemblyline.client.render.BlockRenderingHandler;
import com.builtbroken.assemblyline.client.render.RenderArmbot;
import com.builtbroken.common.Pair;
import com.builtbroken.minecraft.DarkCore;
import com.builtbroken.minecraft.interfaces.IMultiBlock;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockArmbot extends BlockAssembly
{
    public BlockArmbot()
    {
        super("armbot", UniversalElectricity.machine);
        DarkCore.requestMultiBlock(AssemblyLine.MOD_ID);
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z)
    {
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

        if (tileEntity != null && tileEntity instanceof IMultiBlock)
        {
            ((IMultiBlock) tileEntity).onCreate(new Vector3(x, y, z));
        }
    }

    @Override
    public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9)
    {
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

        if (tileEntity != null && tileEntity instanceof IMultiBlock)
        {
            return ((IMultiBlock) tileEntity).onActivated(player);
        }

        return false;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, int par5, int par6)
    {
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

        if (tileEntity != null && tileEntity instanceof IMultiBlock)
        {
            ((IMultiBlock) tileEntity).onDestroy(tileEntity);
        }

        this.dropBlockAsItem_do(world, x, y, z, new ItemStack(this));

        super.breakBlock(world, x, y, z, par5, par6);
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
    {
        return new ItemStack(this);
    }

    @Override
    public int quantityDropped(Random par1Random)
    {
        return 0;
    }

    @Override
    public void getTileEntities(int blockID, Set<Pair<String, Class<? extends TileEntity>>> list)
    {
        list.add(new Pair("ALArmbot", TileEntityArmbot.class));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getClientTileEntityRenderers(List<Pair<Class<? extends TileEntity>, TileEntitySpecialRenderer>> list)
    {
        list.add(new Pair<Class<? extends TileEntity>, TileEntitySpecialRenderer>(TileEntityArmbot.class, new RenderArmbot()));
    }

    @Override
    public TileEntity createNewTileEntity(World var1)
    {
        return new TileEntityArmbot();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getRenderType()
    {
        return BlockRenderingHandler.BLOCK_RENDER_ID;
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean canProvidePower()
    {
        return true;
    }

}
