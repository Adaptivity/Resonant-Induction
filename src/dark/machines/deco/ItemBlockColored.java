package dark.machines.deco;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import dark.api.ColorCode;
import dark.machines.CoreMachine;

public class ItemBlockColored extends ItemBlock
{
    public ItemBlockColored(int par1)
    {
        super(par1);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int par1)
    {
        return par1;
    }

    @Override
    public String getUnlocalizedName(ItemStack par1ItemStack)
    {
        return Block.blocksList[this.getBlockID()].getUnlocalizedName() + "." + ColorCode.get(par1ItemStack.getItemDamage()).name;
    }

    @Override
    public String getUnlocalizedName()
    {
        return Block.blocksList[this.getBlockID()].getUnlocalizedName();
    }
}
