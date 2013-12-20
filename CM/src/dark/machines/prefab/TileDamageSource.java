package dark.machines.prefab;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import universalelectricity.prefab.CustomDamageSource;

public class TileDamageSource extends CustomDamageSource
{
    protected Object damageSource;

    public TileDamageSource(String damageName, Object attacker)
    {
        super(damageName);
        this.damageSource = attacker;
    }

    @Override
    public Entity getEntity()
    {
        return damageSource instanceof Entity ? ((Entity) damageSource) : null;
    }

    @Override
    public boolean isDifficultyScaled()
    {
        return this.damageSource != null && this.damageSource instanceof EntityLiving && !(this.damageSource instanceof EntityPlayer);
    }

    public static TileDamageSource doBulletDamage(Object object)
    {
        return (TileDamageSource) ((CustomDamageSource) new TileDamageSource("Bullets", object).setProjectile()).setDeathMessage("%1$s was filled with holes!");
    }

    public static TileDamageSource doLaserDamage(Object object)
    {
        return (TileDamageSource) ((CustomDamageSource) new TileDamageSource("Laser", object).setProjectile()).setDeathMessage("%1$s was vaporized!");
    }
}
