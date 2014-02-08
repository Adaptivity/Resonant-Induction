package resonantinduction.mechanical.process;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.obj.WavefrontObject;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.Reference;
import calclavia.lib.render.RenderUtility;
import calclavia.lib.utility.WorldUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class RenderGrinderWheel extends TileEntitySpecialRenderer
{
	public static final WavefrontObject MODEL = (WavefrontObject) AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "grinder.obj");

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		if (t instanceof TileGrinderWheel)
		{
			TileGrinderWheel tile = (TileGrinderWheel) t;
			glPushMatrix();
			glTranslatef((float) x + 0.5F, (float) y + 0.5f, (float) z + 0.5F);
			glScalef(0.51f, 0.5f, 0.5f);
			ForgeDirection dir = tile.getDirection();
			dir = ForgeDirection.getOrientation(!(dir.ordinal() % 2 == 0) ? dir.ordinal() - 1 : dir.ordinal());
			RenderUtility.rotateBlockBasedOnDirection(dir);
			glRotatef((float) Math.toDegrees(tile.angle), 0, 0, 1);
			RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "planks_oak.png");
			MODEL.renderAllExcept("teeth");
			RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "cobblestone.png");
			MODEL.renderOnly("teeth");
			glPopMatrix();
		}
	}
}
