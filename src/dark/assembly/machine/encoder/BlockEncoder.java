package dark.assembly.machine.encoder;

import java.util.Set;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.builtbroken.common.Pair;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dark.assembly.AssemblyLine;
import dark.assembly.CommonProxy;
import dark.assembly.machine.BlockAssembly;
import dark.core.ModObjectRegistry.BlockBuildData;

public class BlockEncoder extends BlockAssembly
{
    Icon encoder_side;
    Icon encoder_top;
    Icon encoder_bottom;

    public BlockEncoder()
    {
        super(new BlockBuildData(BlockEncoder.class, "encoder", Material.wood));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IconRegister iconReg)
    {
        this.encoder_side = iconReg.registerIcon(AssemblyLine.instance.PREFIX + "encoder_side");
        this.encoder_top = iconReg.registerIcon(AssemblyLine.instance.PREFIX + "encoder_top");
        this.encoder_bottom = iconReg.registerIcon(AssemblyLine.instance.PREFIX + "encoder_bottom");
    }

    /** Returns the block texture based on the side being looked at. Args: side */
    @Override
    @SideOnly(Side.CLIENT)
    public Icon getBlockTexture(IBlockAccess world, int x, int y, int z, int side)
    {
        return getIcon(side, 0);
    }

    /** Returns the block texture based on the side being looked at. Args: side */
    @Override
    @SideOnly(Side.CLIENT)
    public Icon getIcon(int side, int meta)
    {
        if (side == 1)
        {
            return this.encoder_top;

        }
        else if (side == 0)
        {
            return this.encoder_bottom;

        }

        return this.encoder_side;
    }

    /** Called upon block activation (right click on the block.) */
    @Override
    public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9)
    {
        if (!world.isRemote)
        {
            entityPlayer.openGui(AssemblyLine.instance, CommonProxy.GUI_ENCODER, world, x, y, z);
        }

        return true;

    }
    
    @Override
    public void getTileEntities(int blockID, Set<Pair<String, Class<? extends TileEntity>>> list)
    {
        list.add(new Pair("ALEncoder", TileEntityEncoder.class));
    }

    @Override
    public TileEntity createNewTileEntity(World world)
    {
        return new TileEntityEncoder();
    }
}
