package dark.assembly.common.machine;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.prefab.network.IPacketReceiver;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import dark.core.common.DarkMain;
import dark.core.interfaces.IExternalInv;
import dark.core.network.PacketHandler;
import dark.core.prefab.TileEntityInv;

public class TileEntityCrate extends TileEntityInv implements IPacketReceiver, IExternalInv
{
    /** Collective total stack of all inv slots */
    private ItemStack sampleStack;

    /** delay from last click */
    public long prevClickTime = -1000;
    /** max meta size of the crate */
    public static final int maxSize = 2;

    @Override
    public InventoryCrate getInventory()
    {
        if (this.inventory == null)
        {
            inventory = new InventoryCrate(this);
        }
        return (InventoryCrate) this.inventory;
    }

    /** Gets the sample stack that represent the total inv */
    public ItemStack getSampleStack()
    {
        if (this.sampleStack == null)
        {
            this.buildSampleStack();
        }
        return this.sampleStack;
    }

    /** Turns the inventory array into a single stack of matching items. This assumes that all items
     * in the crate are the same TODO eject minority items and only keep the majority that are the
     * same to prevent duplication issues
     *
     * @param force - force a rebuild of the inventory from the single stack created */
    public void buildSampleStack()
    {
        ItemStack stack = null;

        boolean rebuildBase = false;

        /* Creates the sample stack that is used as a collective itemstack */
        for (int i = 0; i < this.getInventory().getContainedItems().length; i++)
        {
            ItemStack s = this.getInventory().getContainedItems()[i];
            if (s != null && s.itemID > 0 && s.stackSize > 0)
            {
                if (stack == null)
                {
                    stack = s.copy();
                }
                else
                {
                    stack.stackSize += this.getInventory().getContainedItems()[i].stackSize;
                }
                if (this.getInventory().getContainedItems()[i].stackSize > this.getInventory().getContainedItems()[i].getMaxStackSize())
                {
                    rebuildBase = true;
                }
            }
        }
        if (stack == null || stack.itemID == 0 || stack.stackSize == 0)
        {
            this.sampleStack = null;
        }
        else
        {
            this.sampleStack = stack.copy();
        }
        /* if one stack is over sized this rebuilds the inv to redistribute the items in the slots */
        if ((rebuildBase || this.getInventory().getContainedItems().length > this.getSlotCount()) && this.sampleStack != null)
        {
            this.getInventory().buildInventory(this.sampleStack);
        }
    }

    /** Adds an item to the stack */
    public void addToStack(ItemStack stack, int amount)
    {
        if (stack != null)
        {
            this.addToStack(new ItemStack(stack.stackSize, amount, stack.getItemDamage()));
        }
    }

    /** Adds the stack to the sample stack */
    public void addToStack(ItemStack stack)
    {
        if (stack != null)
        {
            this.buildSampleStack();
            boolean flag = false;
            if (this.sampleStack == null)
            {
                this.sampleStack = stack;
                flag = true;
            }
            else if (this.sampleStack.isItemEqual(stack))
            {
                this.sampleStack.stackSize += stack.stackSize;
                flag = true;
            }
            if (flag)
            {
                this.getInventory().buildInventory(this.sampleStack);
                this.onInventoryChanged();
            }
        }
    }

    @Override
    public void onInventoryChanged()
    {
        super.onInventoryChanged();

        if (this.worldObj != null)
        {
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
            {
                PacketHandler.instance().sendPacketToClients(this.getDescriptionPacket(), this.worldObj);
            }
        }
    }

    @Override
    public boolean canStore(ItemStack stack, int slot, ForgeDirection side)
    {
        return this.sampleStack == null || stack != null && stack.isItemEqual(sampleStack);
    }

    /** Gets the current slot count for the crate */
    public int getSlotCount()
    {
        if (this.worldObj == null)
        {
            return TileEntityCrate.getSlotCount(TileEntityCrate.maxSize);
        }
        return TileEntityCrate.getSlotCount(this.getBlockMetadata());
    }

    /** Gets the slot count for the crate meta */
    public static int getSlotCount(int metadata)
    {
        if (metadata >= 2)
        {
            return 256;
        }
        else if (metadata >= 1)
        {
            return 64;
        }
        return 32;
    }

    @Override
    public boolean canUpdate()
    {
        return false;
    }

    @Override
    public void handlePacketData(INetworkManager network, int packetType, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
    {
        if (this.worldObj.isRemote)
        {
            try
            {
                if (dataStream.readBoolean())
                {
                    this.sampleStack = ItemStack.loadItemStackFromNBT(PacketHandler.instance().readNBTTagCompound(dataStream));
                    this.sampleStack.stackSize = dataStream.readInt();
                }
                else
                {
                    this.sampleStack = null;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Packet getDescriptionPacket()
    {
        this.buildSampleStack();
        ItemStack stack = this.getSampleStack();
        if (stack != null)
        {
            return PacketHandler.instance().getPacket(DarkMain.CHANNEL, this, true, stack.writeToNBT(new NBTTagCompound()), stack.stackSize);
        }
        else
        {
            return PacketHandler.instance().getPacket(DarkMain.CHANNEL, this, false);
        }
    }

    /** NBT Data */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        /* Load current two inv methods */
        ItemStack stack = null;
        int count = nbt.getInteger("Count");
        if (nbt.hasKey("itemID"))
        {
            stack = new ItemStack(nbt.getInteger("itemID"), count, nbt.getInteger("itemMeta"));
        }
        else
        {
            stack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("stack"));
            if (stack != null)
            {
                stack.stackSize = count;
            }
        }

        /* Only load sample stack if the read stack is valid */
        if (stack != null && stack.itemID != 0 && stack.stackSize > 0)
        {
            this.sampleStack = stack;
            this.getInventory().buildInventory(this.sampleStack);
        }

    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        /* Re-Build sample stack for saving */
        this.buildSampleStack();
        ItemStack stack = this.getSampleStack();
        /* Save sample stack */
        if (stack != null)
        {
            nbt.setInteger("Count", stack.stackSize);
            nbt.setCompoundTag("stack", stack.writeToNBT(new NBTTagCompound()));
        }

    }

}
