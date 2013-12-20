package mffs.api.modules;

import java.util.Set;

import mffs.api.IFieldInteraction;
import mffs.api.IProjector;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;

public interface IModule
{

	/**
	 * The amount of Fortron this module consumes per tick.
	 * 
	 * @return
	 */
	public float getFortronCost(float amplifier);

	/**
	 * Called before the projector projects a field.
	 * 
	 * @param projector
	 * @return True to stop projecting.
	 */
	boolean onProject(IProjector projector, Set<Vector3> field);

	boolean onDestroy(IProjector projector, Set<Vector3> field);

	/**
	 * Called right after the projector creates a force field block.
	 * 
	 * @param projector
	 * @param position
	 * @return 0 - Do nothing; 1 - Skip this block and continue; 2 - Cancel rest of projection;
	 */

	public int onProject(IProjector projector, Vector3 position);

	/**
	 * Called when an entity collides with a force field block.
	 * 
	 * @return True to stop the default process of entity collision.
	 */
	public boolean onCollideWithForceField(World world, int x, int y, int z, Entity entity, ItemStack moduleStack);

	/**
	 * Called in this module when it is being calculated by the projector.
	 * 
	 * @return False if to prevent this position from being added to the projection que.
	 */
	public void onCalculate(IFieldInteraction projector, Set<Vector3> fieldDefinition);

}
