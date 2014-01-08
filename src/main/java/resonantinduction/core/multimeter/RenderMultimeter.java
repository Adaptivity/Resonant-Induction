package resonantinduction.core.multimeter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.ResonantInduction;
import resonantinduction.energy.model.ModelMultimeter;
import universalelectricity.api.energy.UnitDisplay;
import universalelectricity.api.energy.UnitDisplay.Unit;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Class used to render text onto the multimeter block.
 * 
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class RenderMultimeter
{
	public static final ModelMultimeter MODEL = new ModelMultimeter();
	public static final ResourceLocation TEXTURE = new ResourceLocation(ResonantInduction.DOMAIN, ResonantInduction.MODEL_TEXTURE_DIRECTORY + "multimeter.png");

	@SuppressWarnings("incomplete-switch")
	public static void render(PartMultimeter tileEntity, double x, double y, double z)
	{
		ForgeDirection direction = tileEntity.getDirection();

		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
		GL11.glRotatef(90, 0, 0, 1);
		GL11.glTranslated(0, -1, 0);

		switch (direction)
		{
			case UP:
				GL11.glRotatef(90, 0, 1, 0);
				break;
			case DOWN:
				GL11.glRotatef(-90, 0, 1, 0);
				break;
			case NORTH:
				GL11.glRotatef(180, 0, 1, 0);
				GL11.glRotatef(180, 0, 0, 1);
				GL11.glTranslatef(0, -2, 0);
				break;
			case SOUTH:
				break;
			case WEST:
				GL11.glTranslatef(0, 1, 1);
				GL11.glRotatef(-90, 1, 0, 0);
				break;
			case EAST:
				GL11.glTranslatef(0, 1, -1);
				GL11.glRotatef(90, 1, 0, 0);
				break;
		}

		FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);
		MODEL.render(0.0625f);

		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glPolygonOffset(-10, -10);
		GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);

		float dx = 1F / 16;
		float dz = 1F / 16;
		float displayWidth = 1 - 2F / 16;
		float displayHeight = 1 - 2F / 16;
		GL11.glTranslatef((float) x, (float) y, (float) z);

		switch (direction)
		{
			case UP:
				GL11.glTranslatef(1, 1, 0);
				GL11.glRotatef(180, 1, 0, 0);
				GL11.glRotatef(180, 0, 1, 0);
				GL11.glTranslatef(0, -0.9f, -0.1f);
				break;
			case DOWN:
				GL11.glRotatef(180, 0, 1, 0);
				GL11.glTranslatef(-1, -0.9f, -1.1f);
				break;
			case SOUTH:
				GL11.glTranslatef(1, 1, 1);
				GL11.glRotatef(180, 0, 1, 0);
				GL11.glRotatef(90, 1, 0, 0);
				GL11.glTranslatef(0, -0.9f, -0.1f);
				break;
			case NORTH:
				GL11.glTranslatef(0, 1, 0);
				GL11.glRotatef(0, 0, 1, 0);
				GL11.glRotatef(90, 1, 0, 0);
				GL11.glTranslatef(0, -0.9f, -0.1f);
				break;
			case EAST:
				GL11.glTranslatef(1, 1, 0);
				GL11.glRotatef(-90, 0, 1, 0);
				GL11.glRotatef(90, 1, 0, 0);
				GL11.glTranslatef(0, -0.9f, -0.1f);
				break;
			case WEST:
				GL11.glTranslatef(0, 1, 1);
				GL11.glRotatef(90, 0, 1, 0);
				GL11.glRotatef(90, 1, 0, 0);
				GL11.glTranslatef(0, -0.9f, -0.1f);
				break;
		}

		GL11.glTranslatef(dx + displayWidth / 2, 1F, dz + displayHeight / 2);
		GL11.glRotatef(-90, 1, 0, 0);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

		String joules = UnitDisplay.getDisplayShort(tileEntity.getDetectedEnergy(), Unit.JOULES);

		int stringWidth = Math.max(fontRenderer.getStringWidth(joules), 0);
		// maxWidth += 8;
		int lineHeight = fontRenderer.FONT_HEIGHT + 2;
		int requiredHeight = lineHeight * 1;

		/**
		 * Create an average scale.
		 */
		float scaleX = displayWidth / stringWidth;
		float scaleY = displayHeight / requiredHeight;
		float scale = (float) (Math.min(scaleX, scaleY) * 0.8);
		GL11.glScalef(scale, -scale, scale);
		GL11.glDepthMask(false);

		int realHeight = (int) Math.floor(displayHeight / scale);
		int realWidth = (int) Math.floor(displayWidth / scale);

		int offsetY = (realHeight - requiredHeight) / 2;
		int offsetX = (realWidth - stringWidth) / 2;

		GL11.glDisable(GL11.GL_LIGHTING);
		fontRenderer.drawString(joules, offsetX - realWidth / 2, 1 + offsetY - realHeight / 2 + 0 * lineHeight, 1);

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDepthMask(true);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
		GL11.glPopMatrix();
	}
}