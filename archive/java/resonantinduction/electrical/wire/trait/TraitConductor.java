package resonantinduction.electrical.wire.trait;

import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.energy.IConductor;
import universalelectricity.api.energy.IEnergyNetwork;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

public class TraitConductor extends TileMultipart implements IConductor
{
	public Set<IConductor> ueInterfaces = new HashSet<IConductor>();

	@Override
	public void copyFrom(TileMultipart that)
	{
		super.copyFrom(that);

		if (that instanceof TraitConductor)
		{
			this.ueInterfaces = ((TraitConductor) that).ueInterfaces;
		}
	}

	@Override
	public void bindPart(TMultiPart part)
	{
		super.bindPart(part);

		if (part instanceof IConductor)
		{
			this.ueInterfaces.add((IConductor) part);
		}
	}

	@Override
	public void partRemoved(TMultiPart part, int p)
	{
		super.partRemoved(part, p);

		if (part instanceof IConductor)
		{
			this.ueInterfaces.remove(part);
		}
	}

	@Override
	public void clearParts()
	{
		super.clearParts();
		this.ueInterfaces.clear();
	}

	@Override
	public Object[] getConnections()
	{
		for (IConductor conductor : this.ueInterfaces)
		{
			return conductor.getConnections();
		}

		return null;
	}

	@Override
	public IEnergyNetwork getNetwork()
	{
		for (IConductor conductor : this.ueInterfaces)
		{
			return conductor.getNetwork();
		}

		return null;
	}

	@Override
	public void setNetwork(IEnergyNetwork network)
	{
		for (IConductor conductor : this.ueInterfaces)
		{
			conductor.setNetwork(network);
		}
	}

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		for (IConductor conductor : this.ueInterfaces)
		{
			if (conductor.canConnect(direction.getOpposite()))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public long onReceiveEnergy(ForgeDirection from, long receive, boolean doReceive)
	{
		/**
		 * Try out different sides to try to inject energy into.
		 */
		if (this.partMap(from.ordinal()) == null)
		{
			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				if (dir != from.getOpposite())
				{
					TMultiPart part = this.partMap(dir.ordinal());

					if (this.ueInterfaces.contains(part))
					{
						return ((IConductor) part).onReceiveEnergy(from, receive, doReceive);
					}
				}
			}
		}

		return 0;
	}

	@Override
	public long onExtractEnergy(ForgeDirection from, long extract, boolean doExtract)
	{
		return 0;
	}

	@Override
	public float getResistance()
	{
		long energyLoss = 0;

		if (this.ueInterfaces.size() > 0)
		{
			for (IConductor conductor : this.ueInterfaces)
			{
				energyLoss += conductor.getResistance();
			}

			energyLoss /= this.ueInterfaces.size();
		}

		return energyLoss;
	}

	@Override
	public long getCurrentCapacity()
	{
		long capacitance = 0;

		if (this.ueInterfaces.size() > 0)
		{
			for (IConductor conductor : this.ueInterfaces)
			{
				capacitance += conductor.getCurrentCapacity();
			}

			capacitance /= this.ueInterfaces.size();
		}

		return capacitance;
	}
}
