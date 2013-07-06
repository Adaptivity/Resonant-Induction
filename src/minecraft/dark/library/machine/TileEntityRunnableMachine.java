package dark.library.machine;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;
import universalelectricity.core.UniversalElectricity;
import universalelectricity.core.block.IConnector;
import universalelectricity.core.block.IVoltage;
import universalelectricity.core.electricity.ElectricityNetworkHelper;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.electricity.IElectricityNetwork;
import universalelectricity.prefab.tile.TileEntityElectrical;
import universalelectricity.prefab.tile.TileEntityElectricityRunnable;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import dark.core.PowerSystems;
import dark.core.api.INetworkPart;

public abstract class TileEntityRunnableMachine extends TileEntityElectrical implements IPowerReceptor, IConnector, IVoltage
{
	/** Forge Ore Directory name of the item to toggle power */
	public static String powerToggleItemID = "battery";
	/** Should this machine run without power */
	protected boolean runPowerless = false;
	/** BuildCraft power provider? */
	private IPowerProvider powerProvider;

	public double prevWatts, wattsReceived = 0;

	private PowerSystems[] powerList = new PowerSystems[] { PowerSystems.BUILDCRAFT, PowerSystems.MEKANISM };

	@Override
	public void updateEntity()
	{
		super.updateEntity();
		this.prevWatts = this.wattsReceived;
		if (!this.worldObj.isRemote)
		{
			if ((this.runPowerless || PowerSystems.runPowerLess(powerList)) && this.wattsReceived < this.getBattery(ForgeDirection.UNKNOWN))
			{
				this.wattsReceived += Math.max(this.getBattery(ForgeDirection.UNKNOWN) - this.wattsReceived, 0);
			}
			else
			{
				this.doPowerUpdate();
			}
		}
	}

	public void doPowerUpdate()
	{
		// UNIVERSAL ELECTRICITY UPDATE
		if (!this.isDisabled())
		{
			ElectricityPack electricityPack = TileEntityRunnableMachine.consumeFromMultipleSides(this, this.getConsumingSides(), ElectricityPack.getFromWatts(this.getRequest(ForgeDirection.UNKNOWN), this.getVoltage()));
			this.onReceive(ForgeDirection.UNKNOWN, electricityPack.voltage, electricityPack.amperes);
		}
		else
		{
			ElectricityNetworkHelper.consumeFromMultipleSides(this, new ElectricityPack());
		}

		// BUILDCRAFT POWER UPDATE
		if (PowerFramework.currentFramework != null)
		{
			if (this.powerProvider == null)
			{
				this.powerProvider = PowerFramework.currentFramework.createPowerProvider();
				this.powerProvider.configure(0, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
			}
		}
		if (this.powerProvider != null)
		{
			float requiredEnergy = (float) (this.getRequest(ForgeDirection.UNKNOWN) * UniversalElectricity.TO_BC_RATIO);
			float energyReceived = this.powerProvider.useEnergy(requiredEnergy, requiredEnergy, true);
			this.onReceive(ForgeDirection.UNKNOWN, this.getVoltage(), (UniversalElectricity.BC3_RATIO * energyReceived) / this.getVoltage());
		}
		//TODO add other power systems
	}

	/** Buildcraft */
	@Override
	public void setPowerProvider(IPowerProvider provider)
	{
		this.powerProvider = provider;
	}

	@Override
	public IPowerProvider getPowerProvider()
	{
		return this.powerProvider;
	}

	@Override
	public void doWork()
	{
	}

	@Override
	public int powerRequest(ForgeDirection from)
	{
		if (this.canConnect(from))
		{
			return (int) Math.ceil(this.getRequest(from) * UniversalElectricity.TO_BC_RATIO);
		}

		return 0;
	}

	protected EnumSet<ForgeDirection> getConsumingSides()
	{
		return ElectricityNetworkHelper.getDirections(this);
	}

	/** Watts this tile want to receive each tick */
	public abstract double getRequest(ForgeDirection side);

	/** Called when this tile gets power. Should equal getRequest or power will be wasted
	 * 
	 * @param voltage - E pressure
	 * @param amperes - E flow rate */
	public void onReceive(ForgeDirection side, double voltage, double amperes)
	{
		if (voltage > this.getVoltage())
		{
			this.onDisable(2);
			return;
		}
		this.wattsReceived = Math.min(this.wattsReceived + (voltage * amperes), this.getBattery(side));
	}

	/** Amount of Watts the internal battery/cap can store */
	public double getBattery(ForgeDirection side)
	{
		return this.getRequest(side) * 2;
	}

	/** Sets this machine to run without power only if the given stack match an ore directory name */
	public void toggleInfPower(ItemStack item)
	{
		if (item != null)
		{
			for (ItemStack stack : OreDictionary.getOres(this.powerToggleItemID))
			{
				if (stack.isItemEqual(item))
				{
					this.runPowerless = !this.runPowerless;
					break;
				}
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.wattsReceived = nbt.getDouble("wattsReceived");
		this.runPowerless = nbt.getBoolean("shouldPower");
		this.disabledTicks = nbt.getInteger("disabledTicks");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setDouble("wattsReceived", this.wattsReceived);
		nbt.setBoolean("shouldPower", this.runPowerless);
		nbt.setInteger("disabledTicks", this.disabledTicks);
	}

	public static ElectricityPack consumeFromMultipleSides(TileEntity tileEntity, EnumSet<ForgeDirection> approachingDirection, ElectricityPack requestPack)
	{
		ElectricityPack consumedPack = new ElectricityPack();

		if (tileEntity != null && approachingDirection != null)
		{
			final List<IElectricityNetwork> connectedNetworks = ElectricityNetworkHelper.getNetworksFromMultipleSides(tileEntity, approachingDirection);

			if (connectedNetworks.size() > 0)
			{
				/** Requests an even amount of electricity from all sides. */
				double wattsPerSide = (requestPack.getWatts() / connectedNetworks.size());
				double voltage = requestPack.voltage;

				for (IElectricityNetwork network : connectedNetworks)
				{
					boolean flag = false;
					if (tileEntity instanceof INetworkPart && ((INetworkPart) tileEntity).getTileNetwork() instanceof IElectricityNetwork)
					{
						flag = network.equals(((IElectricityNetwork) ((INetworkPart) tileEntity).getTileNetwork()));
					}
					if (!flag && wattsPerSide > 0 && requestPack.getWatts() > 0)
					{
						network.startRequesting(tileEntity, wattsPerSide / voltage, voltage);
						ElectricityPack receivedPack = network.consumeElectricity(tileEntity);
						consumedPack.amperes += receivedPack.amperes;
						consumedPack.voltage = Math.max(consumedPack.voltage, receivedPack.voltage);
					}
					else
					{
						network.stopRequesting(tileEntity);
					}
				}
			}
		}

		return consumedPack;
	}
}
