package dark.assembly.common.machine.processor;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.Player;
import dark.api.ProcessorRecipes;
import dark.api.ProcessorRecipes.ProcessorType;
import dark.assembly.common.machine.processor.BlockProcessor.ProcessorData;
import dark.core.interfaces.IInvBox;
import dark.core.network.PacketHandler;
import dark.core.prefab.invgui.InvChest;
import dark.core.prefab.machine.TileEntityEnergyMachine;

/** Basic A -> B recipe processor machine designed mainly to handle ore blocks
 * 
 * @author DarkGuardsman */
public class TileEntityProcessor extends TileEntityEnergyMachine
{
    public int slotInput = 0, slotOutput = 1, slotBatteryCharge = 2, slotBatteryDrain = 3;

    public int processingTicks = 0;
    public int processingTime = 100;
    public int renderStage = 1;

    public ProcessorData processorData;

    public boolean invertPiston = false;
    protected ItemStack[] outputBuffer;

    public ProcessorData getProcessorData()
    {
        if (this.processorData == null)
        {
            this.processorData = ProcessorData.values()[this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord) / 4];
            this.WATTS_PER_TICK = processorData.wattPerTick;
            this.MAX_WATTS = this.WATTS_PER_TICK * 20;

            this.processingTime = processorData.processingTicks;
        }
        return processorData;
    }

    @Override
    public void updateEntity()
    {
        this.getProcessorData();
        super.updateEntity();
        if (!this.worldObj.isRemote)
        {
            if (this.outputBuffer != null)
            {
                int nullCount = 0;
                for (int i = 0; i < outputBuffer.length; i++)
                {
                    ItemStack outputStack = this.getInventory().getStackInSlot(this.slotOutput);

                    if (outputBuffer[i] == null)
                    {
                        nullCount += 1;
                    }
                    else if (outputStack == null)
                    {
                        this.getInventory().setInventorySlotContents(this.slotOutput, outputBuffer[i].copy());
                        outputBuffer[i] = null;
                        this.onInventoryChanged();
                    }
                    else if (outputBuffer[i] != null && outputBuffer[i].isItemEqual(outputStack))
                    {
                        ItemStack outStack = outputStack.copy();
                        int room = Math.min(outputStack.getMaxStackSize() - outputStack.stackSize, this.getInventoryStackLimit());
                        if (room >= outputBuffer[i].stackSize)
                        {

                            outStack.stackSize += outputBuffer[i].stackSize;
                            outputBuffer[i] = null;
                        }
                        else
                        {
                            int extract = outputBuffer[i].stackSize - (outputBuffer[i].stackSize - room);
                            outputBuffer[i].stackSize -= extract;
                            outStack.stackSize += extract;

                        }

                        this.getInventory().setInventorySlotContents(this.slotOutput, outStack);
                        this.onInventoryChanged();
                    }

                }
                if (nullCount >= outputBuffer.length)
                {
                    outputBuffer = null;
                }
            }
        }
        if (this.isFunctioning())
        {

            if (!this.worldObj.isRemote)
            {
                if (outputBuffer == null && this.processingTicks++ >= this.processingTime)
                {
                    this.process();
                    this.processingTicks = 0;
                }
            }
            else
            {
                if (this.processorData.doAnimation)
                {
                    this.updateAnimation();
                }
            }
        }
    }

    /** Updates the animation calculation for the renderer to use */
    public void updateAnimation()
    {
        if (this.getProcessorData().type == ProcessorType.CRUSHER || this.getProcessorData().type == ProcessorType.PRESS)
        {
            if (invertPiston)
            {
                if (renderStage-- <= 0)
                {
                    invertPiston = false;
                }
            }
            else
            {
                if (renderStage++ >= 8)
                {
                    invertPiston = true;
                }
            }
        }
        else
        {
            if (renderStage++ >= 8)
            {
                renderStage = 1;
            }
        }
    }

    @Override
    public boolean canFunction()
    {
        return super.canFunction() && this.canProcess();
    }

    /** Can the machine process the itemStack */
    public boolean canProcess()
    {
        ItemStack inputStack = this.getInventory().getStackInSlot(this.slotInput);
        ItemStack outputStack = this.getInventory().getStackInSlot(this.slotOutput);
        if (inputStack != null)
        {
            inputStack = inputStack.copy();
            inputStack.stackSize = 1;
            ItemStack[] outputResult = ProcessorRecipes.getOuput(this.getProcessorData().type, inputStack, true);
            if (outputResult != null)
            {
                if (outputStack == null)
                {
                    return true;
                }
                else
                {
                    for (int i = 0; i < outputResult.length; i++)
                    {
                        if (outputResult[i] != null && outputResult[i].isItemEqual(outputStack))
                        {
                            if (Math.min(outputStack.getMaxStackSize() - outputStack.stackSize, this.getInventoryStackLimit()) >= outputResult[i].stackSize)
                            {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /** Processes the itemStack */
    public void process()
    {
        ItemStack inputSlotStack = this.getInventory().getStackInSlot(this.slotInput);
        if (inputSlotStack != null)
        {

            inputSlotStack = inputSlotStack.copy();
            inputSlotStack.stackSize = 1;
            ItemStack[] receipeResult = ProcessorRecipes.getOuput(this.getProcessorData().type, inputSlotStack, true);
            if (receipeResult != null && this.outputBuffer == null)
            {
                this.getInventory().decrStackSize(this.slotInput, 1);
                this.outputBuffer = receipeResult;
            }
        }
    }

    @Override
    public IInvBox getInventory()
    {
        if (inventory == null)
        {
            inventory = new InvChest(this, 4);
        }
        return inventory;
    }

    @Override
    public boolean canStore(ItemStack stack, int slot, ForgeDirection side)
    {
        if (slotInput == slot && ProcessorRecipes.getOuput(this.getProcessorData().type, stack, true) != null)
        {
            return true;
        }
        if (slotBatteryDrain == slot && this.isBatteryItem(stack))
        {
            return true;
        }
        return false;
    }

    @Override
    public boolean canRemove(ItemStack stack, int slot, ForgeDirection side)
    {
        return slot != slotInput;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side)
    {
        if (side == ForgeDirection.DOWN.ordinal())
        {
            return new int[] { slotOutput };
        }
        if (side == ForgeDirection.UP.ordinal())
        {
            return new int[] { slotInput };
        }
        return new int[] { slotBatteryDrain, slotInput, slotOutput };
    }

    @Override
    public ForgeDirection getDirection()
    {
        if (this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord) == 0)
        {
            return ForgeDirection.NORTH;
        }
        return ForgeDirection.EAST;
    }

    @Override
    public String getInvName()
    {
        if (this.getProcessorData() != null)
        {
            return "gui." + getProcessorData().unlocalizedName + ".name";
        }
        return "gui.processor.name";
    }

    @Override
    public void sendGUIPacket(EntityPlayer entity)
    {
        if (!this.worldObj.isRemote && entity instanceof EntityPlayerMP)
        {
            ((EntityPlayerMP) entity).playerNetServerHandler.sendPacketToPlayer(PacketHandler.instance().getPacket(this.getChannel(), this, SimplePacketTypes.GUI.name, this.processingTicks, this.processingTime, this.energyStored));
        }
    }

    @Override
    public boolean simplePacket(String id, ByteArrayDataInput dis, Player player)
    {
        if (!super.simplePacket(id, dis, player))
        {
            try
            {
                if (this.worldObj.isRemote)
                {
                    if (id.equalsIgnoreCase(SimplePacketTypes.GUI.name))
                    {
                        this.processingTicks = dis.readInt();
                        this.processingTime = dis.readInt();
                        this.setEnergyStored(dis.readFloat());
                        return true;
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return false;
    }

    /** NBT Data */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this.processingTicks = nbt.getInteger("processingTicks");
        this.renderStage = nbt.getInteger("renderStage");
        this.getInventory().loadInv(nbt);

    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setInteger("processingTicks", this.processingTicks);
        nbt.setInteger("renderStage", this.renderStage);
        this.getInventory().saveInv(nbt);

    }

}
