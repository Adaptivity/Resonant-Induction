package com.builtbroken.assemblyline.armbot.command;

import com.builtbroken.assemblyline.api.IArmbot;
import com.builtbroken.assemblyline.api.IBelt;
import com.builtbroken.assemblyline.armbot.TaskBaseArmbot;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.vector.Vector2;
import universalelectricity.core.vector.Vector3;

/** Prefab for grab based commands
 * 
 * @author DarkGuardsman */
public abstract class TaskGrabPrefab extends TaskBaseArmbot
{
    public static final float radius = 0.5f;
    protected Vector3 armPos;
    protected IBelt belt;

    public TaskGrabPrefab(String name)
    {
        super(name);
        this.UV = new Vector2(0, 80);
    }

    @Override
    public ProcessReturn onMethodCalled()
    {
        ProcessReturn re = super.onMethodCalled();
        if (re == ProcessReturn.CONTINUE)
        {
            this.armPos = ((IArmbot) this.program.getMachine()).getHandPos();
            TileEntity entity = this.armPos.getTileEntity(this.program.getMachine().getLocation().left());
            if (entity == null)
            {
                entity = this.armPos.clone().translate(new Vector3(ForgeDirection.DOWN)).getTileEntity(this.program.getMachine().getLocation().left());
            }
            if (entity instanceof IBelt)
            {
                this.belt = (IBelt) entity;
            }
            return ProcessReturn.CONTINUE;
        }
        return re;
    }

    @Override
    public ProcessReturn onUpdate()
    {
        super.onUpdate();

        if (((IArmbot) this.program.getMachine()).getGrabbedObject() != null)
        {
            return ProcessReturn.DONE;
        }

        return ProcessReturn.CONTINUE;
    }
}
