package resonantinduction.archaic.fluid.tank;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import resonant.lib.utility.LanguageUtility;
import resonantinduction.archaic.Archaic;
import resonantinduction.core.fluid.TileFluidDistribution;
import universalelectricity.api.energy.UnitDisplay;
import universalelectricity.api.energy.UnitDisplay.Unit;
import universalelectricity.api.energy.UnitDisplay.UnitPrefix;
import universalelectricity.api.vector.Vector3;

/** @author Darkguardsman */
public class ItemBlockTank extends ItemBlock implements IFluidContainerItem
{
    public ItemBlockTank(int id)
    {
        super(id);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int damage)
    {
        return damage;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
    {
        if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("fluid"))
        {
            FluidStack fluid = getFluid(stack);

            if (fluid != null)
            {
                list.add("Fluid: " + fluid.getFluid().getLocalizedName());
                list.add("Volume: " + UnitDisplay.getDisplay(fluid.amount, Unit.LITER, UnitPrefix.MILLI));
            }
        }
    }

    public static ItemStack getWrenchedItem(World world, Vector3 vec)
    {
        TileEntity entity = vec.getTileEntity(world);

        if (entity instanceof TileTank && ((TileTank) entity).getInternalTank() != null && ((TileTank) entity).getInternalTank().getFluid() != null)
        {
            ItemStack itemStack = new ItemStack(Archaic.blockTank);

            FluidStack stack = ((TileTank) entity).getInternalTank().getFluid();

            if (stack != null)
            {
                if (itemStack.getTagCompound() == null)
                {
                    itemStack.setTagCompound(new NBTTagCompound());
                }
                ((TileTank) entity).drain(ForgeDirection.UNKNOWN, stack.amount, true);
                itemStack.getTagCompound().setCompoundTag("fluid", stack.writeToNBT(new NBTTagCompound()));
            }
            return itemStack;
        }

        return null;
    }

    @Override
    public int getItemStackLimit(ItemStack stack)
    {
        if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("fluid"))
        {
            return 1;
        }
        return this.maxStackSize;
    }

    @Override
    public String getUnlocalizedName(ItemStack itemStack)
    {
        String translation = LanguageUtility.getLocal(Block.blocksList[this.getBlockID()].getUnlocalizedName() + "." + itemStack.getItemDamage());

        if (translation == null || translation.isEmpty())
        {
            return Block.blocksList[this.getBlockID()].getUnlocalizedName();
        }

        return Block.blocksList[this.getBlockID()].getUnlocalizedName() + "." + itemStack.getItemDamage();
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
    {
        if (super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata))
        {
            TileEntity tile = world.getBlockTileEntity(x, y, z);
            if (tile instanceof TileFluidDistribution)
            {
                ((TileFluidDistribution) tile).setSubID(stack.getItemDamage());
                ((TileFluidDistribution) tile).getInternalTank().fill(getFluid(stack), true);

            }
            return true;
        }

        return false;
    }

    @Override
    public FluidStack getFluid(ItemStack container)
    {
        if (container.stackTagCompound == null || !container.stackTagCompound.hasKey("fluid"))
        {
            return null;
        }
        return FluidStack.loadFluidStackFromNBT(container.stackTagCompound.getCompoundTag("fluid"));
    }

    @Override
    public int getCapacity(ItemStack container)
    {
        return TileTank.VOLUME;
    }

    @Override
    public int fill(ItemStack container, FluidStack resource, boolean doFill)
    {
        if (resource == null)
        {
            return 0;
        }

        if (!doFill)
        {
            if (container.stackTagCompound == null || !container.stackTagCompound.hasKey("fluid"))
            {
                return Math.min(getCapacity(container), resource.amount);
            }

            FluidStack stack = FluidStack.loadFluidStackFromNBT(container.stackTagCompound.getCompoundTag("fluid"));

            if (stack == null)
            {
                return Math.min(getCapacity(container), resource.amount);
            }

            if (!stack.isFluidEqual(resource))
            {
                return 0;
            }

            return Math.min(getCapacity(container) - stack.amount, resource.amount);
        }

        if (container.stackTagCompound == null)
        {
            container.stackTagCompound = new NBTTagCompound();
        }

        if (!container.stackTagCompound.hasKey("fluid"))
        {
            NBTTagCompound fluidTag = resource.writeToNBT(new NBTTagCompound());

            if (getCapacity(container) < resource.amount)
            {
                fluidTag.setInteger("Amount", getCapacity(container));
                container.stackTagCompound.setTag("fluid", fluidTag);
                return getCapacity(container);
            }

            container.stackTagCompound.setTag("fluid", fluidTag);
            return resource.amount;
        }

        NBTTagCompound fluidTag = container.stackTagCompound.getCompoundTag("fluid");
        FluidStack stack = FluidStack.loadFluidStackFromNBT(fluidTag);

        if (!stack.isFluidEqual(resource))
        {
            return 0;
        }

        int filled = getCapacity(container) - stack.amount;
        if (resource.amount < filled)
        {
            stack.amount += resource.amount;
            filled = resource.amount;
        }
        else
        {
            stack.amount = getCapacity(container);
        }

        container.stackTagCompound.setTag("fluid", stack.writeToNBT(fluidTag));
        return filled;
    }

    @Override
    public FluidStack drain(ItemStack container, int maxDrain, boolean doDrain)
    {
        if (container.stackTagCompound == null || !container.stackTagCompound.hasKey("fluid") || maxDrain == 0)
        {
            return null;
        }

        FluidStack stack = FluidStack.loadFluidStackFromNBT(container.stackTagCompound.getCompoundTag("fluid"));
        if (stack == null)
        {
            return null;
        }

        int drained = Math.min(stack.amount, maxDrain);
        if (doDrain)
        {
            if (maxDrain >= stack.amount)
            {
                container.stackTagCompound.removeTag("fluid");

                if (container.stackTagCompound.hasNoTags())
                {
                    container.stackTagCompound = null;
                }
                return stack;
            }

            NBTTagCompound fluidTag = container.stackTagCompound.getCompoundTag("fluid");
            fluidTag.setInteger("Amount", fluidTag.getInteger("Amount") - maxDrain);
            container.stackTagCompound.setTag("fluid", fluidTag);
        }
        stack.amount = drained;
        return stack;
    }
}
