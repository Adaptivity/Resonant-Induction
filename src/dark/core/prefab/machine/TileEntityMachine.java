package dark.core.prefab.machine;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.tile.IRotatable;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.Player;
import dark.api.IDisableable;
import dark.core.common.DarkMain;
import dark.core.interfaces.IExternalInv;
import dark.core.interfaces.IExtraInfo.IExtraTileEntityInfo;
import dark.core.interfaces.IInvBox;
import dark.core.network.ISimplePacketReceiver;
import dark.core.network.PacketHandler;

public abstract class TileEntityMachine extends TileEntityInv implements ISidedInventory, IExternalInv, IDisableable, ISimplePacketReceiver, IRotatable, IExtraTileEntityInfo
{
    protected int disabledTicks = 0, playersUsingMachine = 0;
    protected boolean functioning = false, prevFunctioning = false, hasGUI = false, rotateByMetaGroup = false, canBeDisabled = false;

    /** Inventory manager used by this machine */
    protected IInvBox inventory;

    /** Default generic packet types used by all machines */
    public static enum SimplePacketTypes
    {
        /** Normal packet data of any kind */
        GENERIC("generic"),
        /** Power updates */
        RUNNING("isRunning"),
        /** GUI display data update */
        GUI("guiGeneral"),
        /** Full tile read/write data from tile NBT */
        NBT("nbtAll"),
        GUI_EVENT("clientGui"),
        GUI_COMMAND("clientCommand"),
        TERMINAL_OUTPUT("serverTerminal");

        public String name;

        private SimplePacketTypes(String name)
        {
            this.name = name;
        }
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();
        if (!this.worldObj.isRemote)
        {
            this.prevFunctioning = this.functioning;
            this.functioning = this.isFunctioning();

            if (prevFunctioning != this.functioning)
            {
                this.sendPowerUpdate();
            }
            if (this.ticks % 5 == 0)
            {
                this.sendGUIPacket();
            }
        }

        if (this.disabledTicks > 0)
        {
            this.disabledTicks--;
            this.whileDisable();
        }
    }

    /** Can this tile function, or run threw normal processes */
    public boolean canFunction()
    {
        return !this.isDisabled();
    }

    public boolean isFunctioning()
    {
        if (this.worldObj.isRemote)
        {
            return this.functioning;
        }
        else
        {
            return this.canFunction();
        }
    }

    public void doRunningDebug()
    {
        System.out.println("\n  CanRun: " + this.canFunction());
        System.out.println("  RedPower: " + this.worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord));
        System.out.println("  IsDisabled: " + this.isDisabled());//TODO i'm going to kick myself if this is it, yep disabled
        System.out.println("  IsRunning: " + this.functioning);
    }

    /** Called every tick while this tile entity is disabled. */
    protected void whileDisable()
    {

    }

    @Override
    public ForgeDirection getDirection()
    {
        if (this.rotateByMetaGroup)
        {
            switch (this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord) % 4)
            {
                case 0:
                    return ForgeDirection.NORTH;
                case 1:
                    return ForgeDirection.SOUTH;
                case 2:
                    return ForgeDirection.SOUTH;
                default:
                    return ForgeDirection.WEST;
            }
        }
        return ForgeDirection.UNKNOWN;
    }

    @Override
    public void setDirection(ForgeDirection direction)
    {
        if (this.rotateByMetaGroup)
        {
            switch (direction)
            {
                case NORTH:
                    this.worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord) / 4, 3);
                case WEST:
                    this.worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, (this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord) / 4) + 1, 3);
                case SOUTH:
                    this.worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, (this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord) / 4) + 2, 3);
                default:
                    this.worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, (this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord) / 4) + 3, 3);
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this.disabledTicks = nbt.getInteger("disabledTicks");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setInteger("disabledTicks", this.disabledTicks);
    }

    @Override
    public boolean simplePacket(String id, ByteArrayDataInput dis, Player player)
    {
        try
        {
            if (this.worldObj.isRemote)
            {
                if (id.equalsIgnoreCase(SimplePacketTypes.RUNNING.name))
                {
                    this.functioning = dis.readBoolean();
                    return true;
                }
                if (id.equalsIgnoreCase(SimplePacketTypes.NBT.name))
                {
                    this.readFromNBT(Packet.readNBTTagCompound(dis));
                    return true;
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    /** Sends the tileEntity save data to the client */
    public void sendNBTPacket()
    {
        if (!this.worldObj.isRemote)
        {
            NBTTagCompound tag = new NBTTagCompound();
            this.writeToNBT(tag);
            PacketHandler.instance().sendPacketToClients(PacketHandler.instance().getPacket(this.getChannel(), this, SimplePacketTypes.NBT.name, tag), worldObj, new Vector3(this), 64);
        }
    }

    /** Sends a simple true/false am running power update */
    public void sendPowerUpdate()
    {
        if (!this.worldObj.isRemote)
        {
            PacketHandler.instance().sendPacketToClients(PacketHandler.instance().getPacket(this.getChannel(), this, SimplePacketTypes.RUNNING.name, this.functioning), worldObj, new Vector3(this), 64);
        }
    }

    /** Sends a gui packet only to the given player */
    public void sendGUIPacket(EntityPlayer entity)
    {

    }

    public void sendGUIPacket()
    {
        if (this.hasGUI && this.getContainer() != null && this.ticks % 5 == 0)
        {
            this.playersUsingMachine = 0;
            for (Object entity : this.worldObj.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(xCoord - 10, yCoord - 10, zCoord - 10, xCoord + 10, yCoord + 10, zCoord + 10)))
            {
                if (entity instanceof EntityPlayer && ((EntityPlayer) entity).openContainer.getClass().equals(this.getContainer()))
                {
                    this.playersUsingMachine += 1;
                    this.sendGUIPacket(((EntityPlayer) entity));
                }
            }
        }
    }

    @Override
    public Packet getDescriptionPacket()
    {
        return PacketHandler.instance().getPacket(this.getChannel(), this, SimplePacketTypes.RUNNING.name, this.functioning);
    }

    /** NetworkMod channel name */
    public String getChannel()
    {
        return DarkMain.CHANNEL;
    }

    @Override
    public void onDisable(int duration)
    {
        if (this.canBeDisabled)
        {
            this.disabledTicks = duration;
        }
    }

    @Override
    public boolean isDisabled()
    {
        return !this.canBeDisabled && this.disabledTicks > 0;
    }

    @Override
    public boolean hasExtraConfigs()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void loadExtraConfigs(Configuration config)
    {
        // TODO Auto-generated method stub

    }
}
