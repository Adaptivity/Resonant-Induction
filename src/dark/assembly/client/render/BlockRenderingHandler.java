package dark.assembly.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dark.assembly.ALRecipeLoader;
import dark.assembly.AssemblyLine;
import dark.assembly.client.model.ModelConveyorBelt;
import dark.assembly.client.model.ModelCrusher;
import dark.assembly.client.model.ModelGrinder;
import dark.assembly.client.model.ModelManipulator;
import dark.assembly.client.model.ModelRejectorPiston;
import dark.assembly.machine.red.BlockAdvancedHopper;
import dark.core.prefab.ModPrefab;

@SideOnly(Side.CLIENT)
public class BlockRenderingHandler implements ISimpleBlockRenderingHandler
{
    public static BlockRenderingHandler instance = new BlockRenderingHandler();
    public static final int BLOCK_RENDER_ID = RenderingRegistry.getNextAvailableRenderId();
    private ModelConveyorBelt modelConveyorBelt = new ModelConveyorBelt();
    private ModelRejectorPiston modelEjector = new ModelRejectorPiston();
    private ModelManipulator modelInjector = new ModelManipulator();
    private ModelCrusher modelCrushor = new ModelCrusher();
    private ModelGrinder grinderModel = new ModelGrinder();

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
    {
        if (ALRecipeLoader.blockConveyorBelt != null && block.blockID == ALRecipeLoader.blockConveyorBelt.blockID)
        {
            GL11.glPushMatrix();
            GL11.glTranslatef(0.0F, 1.5F, 0.0F);
            GL11.glRotatef(180f, 0f, 0f, 1f);
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation(AssemblyLine.instance.DOMAIN, ModPrefab.MODEL_DIRECTORY + "belt/frame0.png"));
            modelConveyorBelt.render(0.0625F, 0, false, false, false, false);
            GL11.glPopMatrix();
        }
        else if (ALRecipeLoader.blockRejector != null && block.blockID == ALRecipeLoader.blockRejector.blockID)
        {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation(AssemblyLine.instance.DOMAIN, ModPrefab.MODEL_DIRECTORY + "rejector.png"));
            GL11.glPushMatrix();
            GL11.glTranslatef(0.6F, 1.5F, 0.6F);
            GL11.glRotatef(180f, 0f, 0f, 1f);
            GL11.glRotatef(-90f, 0f, 1f, 0f);
            modelEjector.render(0.0625F);
            modelEjector.renderPiston(0.0625F, 1);
            GL11.glPopMatrix();
        }
        else if (ALRecipeLoader.blockManipulator != null && block.blockID == ALRecipeLoader.blockManipulator.blockID)
        {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation(AssemblyLine.instance.DOMAIN, ModPrefab.MODEL_DIRECTORY + "manipulator1.png"));
            GL11.glPushMatrix();
            GL11.glTranslatef(0.6F, 1.5F, 0.6F);
            GL11.glRotatef(180f, 0f, 0f, 1f);
            GL11.glRotatef(-90f, 0f, 1f, 0f);
            modelInjector.render(0.0625F, true, 0);
            GL11.glPopMatrix();
        }
        else if (ALRecipeLoader.blockArmbot != null && block.blockID == ALRecipeLoader.blockArmbot.blockID)
        {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation(AssemblyLine.instance.DOMAIN, ModPrefab.MODEL_DIRECTORY + RenderArmbot.TEXTURE));
            GL11.glPushMatrix();
            GL11.glTranslatef(0.4f, 0.8f, 0f);
            GL11.glScalef(0.7f, 0.7f, 0.7f);
            GL11.glRotatef(180f, 0f, 0f, 1f);
            GL11.glRotatef(-90f, 0f, 1f, 0f);
            RenderArmbot.MODEL.render(0.0625F, 0, 0);
            GL11.glPopMatrix();
        }
        else if (ALRecipeLoader.processorMachine != null && block.blockID == ALRecipeLoader.processorMachine.blockID && metadata == 0)
        {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation(AssemblyLine.instance.DOMAIN, ModPrefab.MODEL_DIRECTORY + "CrusherBlock.png"));
            GL11.glPushMatrix();
            GL11.glTranslatef(0f, 1f, 0f);
            GL11.glRotatef(180f, 0f, 0f, 1f);
            GL11.glRotatef(-90f, 0f, 1f, 0f);
            this.modelCrushor.renderBody(0.0625f);
            this.modelCrushor.renderPiston(0.0625f, 4);
            GL11.glPopMatrix();
        }
        else if (ALRecipeLoader.processorMachine != null && block.blockID == ALRecipeLoader.processorMachine.blockID && metadata == 4)
        {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation(AssemblyLine.instance.DOMAIN, ModPrefab.MODEL_DIRECTORY + "GrinderBlock.png"));
            GL11.glPushMatrix();
            GL11.glTranslatef(0f, 1f, 0f);
            GL11.glRotatef(180f, 0f, 0f, 1f);
            GL11.glRotatef(-90f, 0f, 1f, 0f);
            this.grinderModel.renderBody(0.0625f);
            this.grinderModel.renderRotation(0.0625f, 0);
            GL11.glPopMatrix();
        }
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
    {
        if (block instanceof BlockAdvancedHopper)
        {
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldRender3DInInventory()
    {
        return true;
    }

    @Override
    public int getRenderId()
    {
        return BLOCK_RENDER_ID;
    }
}
