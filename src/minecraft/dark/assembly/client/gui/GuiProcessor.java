package dark.assembly.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import dark.assembly.common.AssemblyLine;
import dark.assembly.common.machine.processor.ContainerProcessor;
import dark.assembly.common.machine.processor.TileEntityProcessor;

@SideOnly(Side.CLIENT)
public class GuiProcessor extends GuiContainer
{
    private static final ResourceLocation gui_texture = new ResourceLocation(AssemblyLine.instance.PREFIX + AssemblyLine.GUI_DIRECTORY + "processor.png");
    private TileEntityProcessor tileEntity;

    public GuiProcessor(InventoryPlayer par1InventoryPlayer, TileEntityProcessor par2TileEntityFurnace)
    {
        super(new ContainerProcessor(par1InventoryPlayer, par2TileEntityFurnace));
        this.tileEntity = par2TileEntityFurnace;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
        String s = this.tileEntity.isInvNameLocalized() ? this.tileEntity.getInvName() : I18n.func_135053_a(this.tileEntity.getInvName());
        this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.func_135053_a("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.func_110434_K().func_110577_a(gui_texture);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
        int i1;

        i1 = (this.tileEntity.processingTicks / this.tileEntity.processingTime) * 24;
        this.drawTexturedModalRect(k + 79, l + 34, 176, 14, i1 + 1, 16);
    }
}
