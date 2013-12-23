package com.builtbroken.assemblyline.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.vector.Vector3;

import com.builtbroken.minecraft.prefab.TileEntityEnergyMachine;
import com.builtbroken.minecraft.tilenetwork.INetworkEnergyPart;
import com.builtbroken.minecraft.tilenetwork.ITileNetwork;
import com.builtbroken.minecraft.tilenetwork.prefab.NetworkSharedPower;
import com.builtbroken.minecraft.tilenetwork.prefab.NetworkTileEntities;

/** A class to be inherited by all machines on the assembly line. This class acts as a single peace
 * in a network of similar tiles allowing all to share power from one or more sources
 *
 * @author DarkGuardsman */
public abstract class TileEntityAssembly extends TileEntityEnergyMachine implements INetworkEnergyPart
{
    /** lowest value the network can update at */
    public static int refresh_min_rate = 20;
    /** range by which the network can update at */
    public static int refresh_diff = 9;
    /** Network used to link assembly machines together */
    private NetworkAssembly assemblyNetwork;
    /** Tiles that are connected to this */
    public List<TileEntity> connectedTiles = new ArrayList<TileEntity>();
    /** Random instance */
    public Random random = new Random();
    /** Random rate by which this tile updates its network connections */
    private int updateTick = 1;

    public TileEntityAssembly(long wattsPerTick)
    {
        super(wattsPerTick);
    }

    public TileEntityAssembly(long wattsPerTick, long maxEnergy)
    {
        super(wattsPerTick, maxEnergy);
    }

    @Override
    public void invalidate()
    {
        NetworkTileEntities.invalidate(this);
        if (this.getTileNetwork() != null)
        {
            this.getTileNetwork().splitNetwork(this);
        }
        super.invalidate();
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();
        if (!this.worldObj.isRemote)
        {
            if (ticks % updateTick == 0)
            {
                this.updateTick = (random.nextInt(1 + refresh_diff) + refresh_min_rate);
                this.refresh();
            }
        }
    }

    @Override
    public boolean canTileConnect(Connection type, ForgeDirection dir)
    {
        return true;
    }

    @Override
    public void refresh()
    {
        if (this.worldObj != null && !this.worldObj.isRemote)
        {
            this.connectedTiles.clear();

            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
            {
                TileEntity tileEntity = new Vector3(this).modifyPositionFromSide(dir).getTileEntity(this.worldObj);
                if (tileEntity instanceof TileEntityAssembly && ((TileEntityAssembly) tileEntity).canTileConnect(Connection.NETWORK, dir.getOpposite()))
                {
                    this.getTileNetwork().mergeNetwork(((TileEntityAssembly) tileEntity).getTileNetwork(), this);
                    connectedTiles.add(tileEntity);
                }
            }
        }
    }

    @Override
    public List<TileEntity> getNetworkConnections()
    {
        return this.connectedTiles;
    }

    @Override
    public NetworkAssembly getTileNetwork()
    {
        if (this.assemblyNetwork == null)
        {
            this.assemblyNetwork = new NetworkAssembly(this);
        }
        return this.assemblyNetwork;
    }

    @Override
    public void setTileNetwork(ITileNetwork network)
    {
        if (network instanceof NetworkAssembly)
        {
            this.assemblyNetwork = (NetworkAssembly) network;
        }
    }

    @Override
    public boolean consumePower(long request, boolean doExtract)
    {
        return ((NetworkSharedPower) this.getTileNetwork()).removePower(this, request, doExtract) >= request;
    }

    @Override
    public long onReceiveEnergy(ForgeDirection from, long receive, boolean doReceive)
    {
        if (this.canConnect(from))
        {
            return this.getTileNetwork().addPower(this, receive, doReceive);
        }
        return 0;
    }

    /** Amount of energy this tile runs on per tick */
    public int getWattLoad()
    {
        return 1;//1J/t or 20J/t
    }

    public int getExtraLoad()
    {
        return 1;//1J/t or 20J/t
    }

    @Override
    public void togglePowerMode()
    {
        super.togglePowerMode();
        ((NetworkSharedPower) this.getTileNetwork()).setPowerLess(!this.runPowerLess());
    }

    @Override
    public long getEnergyStored()
    {
        return ((NetworkSharedPower) this.getTileNetwork()).getEnergy();
    }

    @Override
    public long getMaxEnergyStored()
    {
        return ((NetworkSharedPower) this.getTileNetwork()).getEnergyCapacity();
    }

    @Override
    public long getPartEnergy()
    {
        return this.energyStored;
    }

    @Override
    public long getPartMaxEnergy()
    {
        return this.MAX_JOULES_STORED;
    }

    @Override
    public void setPartEnergy(long energy)
    {
        this.energyStored = energy;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return INFINITE_EXTENT_AABB;
    }
}
