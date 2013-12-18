package dark.assembly.machine;

import java.util.List;
import java.util.Set;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import universalelectricity.core.UniversalElectricity;

import com.builtbroken.common.Pair;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dark.assembly.AssemblyLine;
import dark.assembly.client.render.BlockRenderingHandler;
import dark.assembly.client.render.RenderRejector;
import dark.assembly.imprinter.prefab.BlockImprintable;

public class BlockRejector extends BlockImprintable
{
    @SideOnly(Side.CLIENT)
    protected Icon front;

    public BlockRejector()
    {
        super("rejector", UniversalElectricity.machine);
    }

    @Override
    public void getTileEntities(int blockID, Set<Pair<String, Class<? extends TileEntity>>> list)
    {
        list.add(new Pair("ALRejector", TileEntityRejector.class));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getClientTileEntityRenderers(List<Pair<Class<? extends TileEntity>, TileEntitySpecialRenderer>> list)
    {
        list.add(new Pair<Class<? extends TileEntity>, TileEntitySpecialRenderer>(TileEntityRejector.class, new RenderRejector()));
    }

    @Override
    public TileEntity createNewTileEntity(World var1)
    {
        return new TileEntityRejector();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getRenderType()
    {
        return this.zeroRendering ? 0 : BlockRenderingHandler.BLOCK_RENDER_ID;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IconRegister par1IconRegister)
    {
        this.blockIcon = par1IconRegister.registerIcon(AssemblyLine.instance.PREFIX + "imprinter_bottom");
        this.front = par1IconRegister.registerIcon(AssemblyLine.instance.PREFIX + "disk_tray");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Icon getIcon(int side, int metadata)
    {
        if (side == metadata)
        {
            return this.front;
        }
        return this.blockIcon;
    }

}
