package com.builtbroken.assemblyline.machine;

import java.util.EnumSet;

import net.minecraft.inventory.Container;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.energy.IEnergyInterface;
import universalelectricity.api.item.ElectricItemHelper;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorHelper;

import com.builtbroken.minecraft.network.PacketHandler;
import com.builtbroken.minecraft.prefab.TileEntityEnergyMachine;
import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.LanguageRegistry;

/** Simple in out battery box
 * 
 * @author DarkGuardsman */
public class TileEntityBatteryBox extends TileEntityEnergyMachine
{
    public TileEntityBatteryBox()
    {
        super(0, 5000000);
        this.invSlots = 2;
        this.hasGUI = true;
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();

        if (!this.worldObj.isRemote && this.enabled && !this.isDisabled())
        {
            /** Recharges electric item. */
            this.setEnergy(ForgeDirection.UNKNOWN, this.getEnergy(ForgeDirection.UNKNOWN) - CompatibilityModule.chargeItem(this.getStackInSlot(0), Math.min(10000, this.getEnergyStored()), true));
            /** Decharge electric item. */
            this.setEnergy(ForgeDirection.UNKNOWN, this.getEnergy(ForgeDirection.UNKNOWN) + CompatibilityModule.disChargeItem(this.getStackInSlot(1), Math.min(10000, this.getEnergyCapacity(ForgeDirection.UNKNOWN) - this.getEnergyStored()), true));

            this.produce();
        }
    }

    @Override
    public boolean canConnect(ForgeDirection direction)
    {
        return true;
    }

    @Override
    public EnumSet<ForgeDirection> getOutputDirections()
    {
        return EnumSet.of(ForgeDirection.getOrientation(this.getBlockMetadata()).getOpposite());
    }

    /** The electrical input direction.
     * 
     * @return The direction that electricity is entered into the tile. Return null for no input. By
     * default you can accept power from all sides. */
    public EnumSet<ForgeDirection> getInputDirections()
    {
        EnumSet<ForgeDirection> et = EnumSet.allOf(ForgeDirection.class);
        et.remove(ForgeDirection.getOrientation(this.getBlockMetadata()).getOpposite());
        et.remove(ForgeDirection.UNKNOWN);
        return et;
    }

    @Override
    public boolean simplePacket(String id, ByteArrayDataInput dis, Player player)
    {
        boolean r = super.simplePacket(id, dis, player);
        try
        {

            if (this.worldObj.isRemote && !r)
            {
                if (id.equalsIgnoreCase("desc"))
                {
                    this.setEnergy(ForgeDirection.UNKNOWN, dis.readLong());
                    this.setMaxEnergyStored(dis.readLong());
                    return true;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return r;
    }

    @Override
    public Packet getDescriptionPacket()
    {
        return PacketHandler.instance().getTilePacket(this.getChannel(), "desc", this, this.getEnergyStored(), this.getMaxEnergyStored());
    }

    @Override
    public Packet getGUIPacket()
    {
        return this.getDescriptionPacket();
    }

    @Override
    public Class<? extends Container> getContainer()
    {
        return ContainerBatteryBox.class;
    }

    @Override
    public String getInvName()
    {
        return LanguageRegistry.instance().getStringLocalization("tile.batterybox.name");
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 1;
    }

    @Override
    public boolean isInvNameLocalized()
    {
        return true;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int slotID)
    {
        return new int[] { 0, 1 };
    }
}
