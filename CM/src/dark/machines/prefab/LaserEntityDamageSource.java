package dark.machines.prefab;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntityDamageSource;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class LaserEntityDamageSource extends EntityDamageSource
{
    public LaserEntityDamageSource(Entity par2Entity)
    {
        super("Laser", par2Entity);
    }

    @ForgeSubscribe
    public void LivingDeathEvent(LivingDeathEvent event)
    {
        if (event.entity instanceof EntityCreeper)
        {
            if (!event.entity.worldObj.isRemote && event.source instanceof LaserEntityDamageSource)
            {
                boolean flag = event.entity.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing");

                if (((EntityCreeper) event.entity).getPowered())
                {
                    event.entity.worldObj.createExplosion(event.entity, event.entity.posX, event.entity.posY, event.entity.posZ, 3 * 2, flag);
                }
                else
                {
                    event.entity.worldObj.createExplosion(event.entity, event.entity.posX, event.entity.posY, event.entity.posZ, 3, flag);
                }
            }
        }
    }

    @ForgeSubscribe
    public void LivingAttackEvent(LivingAttackEvent event)
    {
        if (event.entity instanceof EntityPlayer)
        {
            if (((EntityPlayer) event.entity).inventory.armorItemInSlot(3) == new ItemStack(Item.plateDiamond, 1))
            {
                if (event.isCancelable())
                {
                    event.setCanceled(true);
                }
            }
        }
    }
}
