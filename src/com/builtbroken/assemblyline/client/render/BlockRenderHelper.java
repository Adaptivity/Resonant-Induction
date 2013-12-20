package com.builtbroken.assemblyline.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import com.builtbroken.assemblyline.ALRecipeLoader;
import com.builtbroken.assemblyline.AssemblyLine;
import com.builtbroken.assemblyline.client.model.ModelConstructionPump;
import com.builtbroken.assemblyline.client.model.ModelGearRod;
import com.builtbroken.assemblyline.client.model.ModelGenerator;
import com.builtbroken.assemblyline.client.model.ModelPump;
import com.builtbroken.assemblyline.client.model.ModelSink;
import com.dark.DarkCore;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockRenderHelper implements ISimpleBlockRenderingHandler
{
    public static BlockRenderHelper instance = new BlockRenderHelper();
    public static int renderID = RenderingRegistry.getNextAvailableRenderId();
    private ModelPump modelPump = new ModelPump();
    private ModelGearRod modelRod = new ModelGearRod();
    private ModelGenerator modelGen = new ModelGenerator();
    private ModelSink sink = new ModelSink();
    private ModelConstructionPump conPump = new ModelConstructionPump();

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
    {
        GL11.glPushMatrix();
        if (ALRecipeLoader.blockPumpMachine != null && block.blockID == ALRecipeLoader.blockPumpMachine.blockID && metadata < 4)
        {
            GL11.glTranslatef(0.0F, 1.1F, 0.0F);
            GL11.glRotatef(180f, 0f, 0f, 1f);

            FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation(AssemblyLine.DOMAIN, DarkCore.MODEL_DIRECTORY + "pumps/WaterPump.png"));
            modelPump.render(0.0725F);
            modelPump.renderMotion(0.0725F, 0);
        }
        else if (ALRecipeLoader.blockSink != null && block.blockID == ALRecipeLoader.blockSink.blockID)
        {
            GL11.glTranslatef(0.0F, .8F, 0.0F);
            GL11.glRotatef(180f, 0f, 0f, 1f);
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation(AssemblyLine.DOMAIN, DarkCore.MODEL_DIRECTORY + "Sink.png"));
            sink.render(0.0565F);
        }
        else if (ALRecipeLoader.blockRod != null && block.blockID == ALRecipeLoader.blockRod.blockID)
        {
            GL11.glTranslatef(0.0F, 1.5F, 0.0F);
            GL11.glRotatef(180f, 0f, 0f, 1f);
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation(AssemblyLine.DOMAIN, DarkCore.MODEL_DIRECTORY + "mechanical/GearRod.png"));
            modelRod.render(0.0825F, 0);
        }
        else if (ALRecipeLoader.blockConPump != null && block.blockID == ALRecipeLoader.blockConPump.blockID && metadata < 4)
        {
            GL11.glTranslatef(0.0F, 1.2F, 0.0F);
            GL11.glRotatef(180f, 0f, 0f, 1f);
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation(AssemblyLine.DOMAIN, DarkCore.MODEL_DIRECTORY + "ConstructionPump.png"));
            conPump.render(0.0725F);
            conPump.renderMotor(0.0725F);

        }
        GL11.glPopMatrix();
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
    {
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
        return renderID;
    }
}
