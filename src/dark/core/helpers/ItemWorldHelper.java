package dark.core.helpers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;

public class ItemWorldHelper
{

    /** gets all EntityItems in a location using a start and end point */
    public static List<EntityItem> findAllItemIn(World world, Vector3 start, Vector3 end)
    {
        return world.getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.getBoundingBox(start.x, start.y, start.z, end.x, end.y, end.z));
    }

    /** Gets all EntityItems in an area and sorts them by a list of itemStacks
     * 
     * @param world - world being worked in
     * @param start - start point
     * @param end - end point
     * @param disiredItems - list of item that are being looked for
     * @return a list of EntityItem that match the itemStacks desired */
    public static List<EntityItem> findSelectItems(World world, Vector3 start, Vector3 end, List<ItemStack> disiredItems)
    {
        List<EntityItem> entityItems = ItemWorldHelper.findAllItemIn(world, start, end);
        return filterEntityItemsList(entityItems, disiredItems);
    }

    /** filters an EntityItem List to a List of Items */
    public static List<EntityItem> filterEntityItemsList(List<EntityItem> entityItems, List<ItemStack> disiredItems)
    {
        List<EntityItem> newItemList = new ArrayList<EntityItem>();
        for (ItemStack itemStack : disiredItems)
        {
            for (EntityItem entityItem : entityItems)
            {
                if (entityItem.getEntityItem().isItemEqual(itemStack) && !newItemList.contains(entityItem))
                {
                    newItemList.add(entityItem);
                    break;
                }
            }
        }
        return newItemList;
    }

    /** filters out EnittyItems from an Entity list */
    public static List<EntityItem> filterOutEntityItems(List<Entity> entities)
    {
        List<EntityItem> newEntityList = new ArrayList<EntityItem>();

        for (Entity entity : entities)
        {
            if (entity instanceof EntityItem)
            {
                newEntityList.add((EntityItem) entity);
            }

        }
        return newEntityList;
    }

    /** filter a list of itemStack to another list of itemStacks
     * 
     * @param totalItems - full list of items being filtered
     * @param desiredItems - list the of item that are being filtered too
     * @return a list of item from the original that are wanted */
    public static List<ItemStack> filterItems(List<ItemStack> totalItems, List<ItemStack> desiredItems)
    {
        List<ItemStack> newItemList = new ArrayList<ItemStack>();

        for (ItemStack entityItem : totalItems)
        {
            for (ItemStack itemStack : desiredItems)
            {
                if (entityItem.itemID == itemStack.itemID && entityItem.getItemDamage() == itemStack.getItemDamage() && !newItemList.contains(entityItem))
                {
                    newItemList.add(entityItem);
                    break;
                }
            }
        }
        return newItemList;
    }

    /** grabs all the items that the block can drop then pass them onto dropBlockAsItem_do
     * 
     * @param world
     * @param x
     * @param y
     * @param z */
    public static void dropBlockAsItem(World world, Vector3 loc)
    {
        if (!world.isRemote)
        {
            int meta = loc.getBlockMetadata(world);
            int id = loc.getBlockID(world);
            ArrayList<ItemStack> items = Block.blocksList[id].getBlockDropped(world, loc.intX(), loc.intY(), loc.intZ(), meta, 0);

            for (ItemStack item : items)
            {
                dropItemStack(world, loc, item, false);
            }
        }
    }

    public static ItemStack dropItemStack(World world, Vector3 location, ItemStack itemStack, boolean random)
    {
        if (!world.isRemote && world.getGameRules().getGameRuleBooleanValue("doTileDrops"))
        {
            float f = 0.7F;
            double xx = 0;
            double yy = 0;
            double zz = 0;
            if (random)
            {
                xx = (world.rand.nextFloat() * f) + (1.0F - f) * 0.5D;
                yy = (world.rand.nextFloat() * f) + (1.0F - f) * 0.5D;
                zz = (world.rand.nextFloat() * f) + (1.0F - f) * 0.5D;
            }
            EntityItem entityitem = new EntityItem(world, location.x + xx, location.y + yy, location.z + zz, itemStack);
            entityitem.delayBeforeCanPickup = 10;
            world.spawnEntityInWorld(entityitem);
            return null;
        }
        return itemStack;
    }
}
