package dark.fluid.client.render;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dark.core.render.RenderMachine;
import dark.fluid.client.model.ModelConstructionPump;
import dark.fluid.common.FluidMech;
import dark.fluid.common.pump.TileEntityConstructionPump;

@SideOnly(Side.CLIENT)
public class RenderConstructionPump extends RenderMachine
{
	int type = 0;
	private ModelConstructionPump model;

	public RenderConstructionPump()
	{
		model = new ModelConstructionPump();
	}

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double d, double d1, double d2, float d3)
	{
		bindTextureByName(this.getTexture(0, 0));
		GL11.glPushMatrix();
		GL11.glTranslatef((float) d + 0.5F, (float) d1 + 1.45F, (float) d2 + 0.5F);
		GL11.glScalef(1.0F, -1F, -1F);
		int meta = tileEntity.worldObj.getBlockMetadata(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
		switch (meta)
		{
			case 0:
				GL11.glRotatef(0f, 0f, 1f, 0f);
				break;
			case 1:
				GL11.glRotatef(90f, 0f, 1f, 0f);
				break;
			case 2:
				GL11.glRotatef(180f, 0f, 1f, 0f);
				break;
			case 3:
				GL11.glRotatef(270f, 0f, 1f, 0f);
				break;
		}
		model.render(0.0625F);
		if (tileEntity instanceof TileEntityConstructionPump)
		{
			//TODO animation life
		}
		model.renderMotor(0.0625F);
		GL11.glPopMatrix();
	}

	@Override
	public ResourceLocation getTexture(int block, int meta)
	{
		return new ResourceLocation(FluidMech.DOMAIN, FluidMech.MODEL_DIRECTORY + "ConstructionPump.png");
	}

}