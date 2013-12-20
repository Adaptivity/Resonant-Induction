package dark.machines.client.renders;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.dark.DarkCore;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dark.machines.CoreMachine;
import dark.machines.client.models.ModelSolarPanel;

@SideOnly(Side.CLIENT)
public class RenderBlockSolarPanel extends RenderTileMachine
{
    private static final ResourceLocation solarPanelTexture = new ResourceLocation(CoreMachine.getInstance().DOMAIN, DarkCore.MODEL_DIRECTORY + "SolarPanel.png");

    public static final ModelSolarPanel model = new ModelSolarPanel();

    @Override
    public void renderModel(TileEntity tileEntity, double d, double d1, double d2, float f)
    {
        // Texture file
        this.bindTexture(this.getTexture(tileEntity.getBlockType().blockID, tileEntity.getBlockMetadata()));
        GL11.glPushMatrix();
        GL11.glTranslatef((float) d + 0.5F, (float) d1 + 1.5F, (float) d2 + 0.5F);
        GL11.glScalef(1.0F, -1F, -1F);
        model.render(0.0625F);
        GL11.glPopMatrix();
    }

    @Override
    public ResourceLocation getTexture(int block, int meta)
    {
        return solarPanelTexture;
    }
}