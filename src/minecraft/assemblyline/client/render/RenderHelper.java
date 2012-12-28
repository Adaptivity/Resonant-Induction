package assemblyline.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;

import org.lwjgl.opengl.GL11;

public class RenderHelper
{
	public static void renderFloatingText(String text, float x, float y, float z)
	{
		RenderManager renderManager = RenderManager.instance;
		FontRenderer fontRenderer = renderManager.getFontRenderer();
		float scale = 0.027f;
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.0F, (float) y + 2.3F, (float) z);
		GL11.glNormal3f(0.0F, 1.0F, 0.0F);
		GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
		GL11.glScalef(-scale, -scale, scale);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDepthMask(false);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		Tessellator tessellator = Tessellator.instance;
		int yOffset = 0;

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		tessellator.startDrawingQuads();
		int stringMiddle = fontRenderer.getStringWidth(text) / 2;
		tessellator.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
		tessellator.addVertex((double) (-stringMiddle - 1), (double) (-1 + yOffset), 0.0D);
		tessellator.addVertex((double) (-stringMiddle - 1), (double) (8 + yOffset), 0.0D);
		tessellator.addVertex((double) (stringMiddle + 1), (double) (8 + yOffset), 0.0D);
		tessellator.addVertex((double) (stringMiddle + 1), (double) (-1 + yOffset), 0.0D);
		tessellator.draw();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		fontRenderer.drawString(text, -fontRenderer.getStringWidth(text) / 2, yOffset, 553648127);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(true);
		fontRenderer.drawString(text, -fontRenderer.getStringWidth(text) / 2, yOffset, -1);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glPopMatrix();
	}
}
