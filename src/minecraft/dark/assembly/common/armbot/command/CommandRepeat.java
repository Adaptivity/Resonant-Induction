package dark.assembly.common.armbot.command;

import net.minecraft.nbt.NBTTagCompound;

/** This task resets all previous tasks and does them again in a loop.
 * 
 * @author Calclavia */
public class CommandRepeat extends Command
{
    /** The amount of tasks above this task to repeat. */
    private int tasksToRepeat;
    private int numReps;
    private int curReps;
    private boolean initialized = false;

    public void onTaskStart()
    {
        this.tasksToRepeat = Math.max(this.getIntArg(0), 0);
        this.numReps = this.getIntArg(1);
        if (numReps == 0)
            numReps = -1; // infinite
        if (!this.initialized)
        {
            this.initialized = true;
            this.curReps = 0;
        }
    }

    @Override
    public void onTaskEnd()
    {
        if (this.curReps < this.numReps || this.numReps == -1)
        {
            this.curReps++;
            if (this.tasksToRepeat > 0)
            {
                this.commandManager.setCurrentTask(this.commandManager.getCurrentTask() - this.tasksToRepeat - 1);
                return;
            }
            else
            {
                this.commandManager.setCurrentTask(-2);
                return;
            }
        }
        this.initialized = false;
    }

    @Override
    public void readFromNBT(NBTTagCompound taskCompound)
    {
        super.readFromNBT(taskCompound);
        this.initialized = taskCompound.getBoolean("repInitialized");
        this.tasksToRepeat = taskCompound.getInteger("repTasks");
        this.curReps = taskCompound.getInteger("repCur");
        this.numReps = taskCompound.getInteger("repGoal");
    }

    @Override
    public void writeToNBT(NBTTagCompound taskCompound)
    {
        super.writeToNBT(taskCompound);
        taskCompound.setBoolean("repInitialized", this.initialized);
        taskCompound.setInteger("repTasks", this.tasksToRepeat);
        taskCompound.setInteger("repCur", this.curReps);
        taskCompound.setInteger("repGoal", this.numReps);
    }

    @Override
    public String toString()
    {
        int cmdToTest = 0;
        if (this.tasksToRepeat > 0)
        {
            cmdToTest = this.commandManager.getCurrentTask() - this.tasksToRepeat;
        }
        if (this.commandManager.hasTasks() && this.commandManager.getCurrentTask() >= 0 && this.commandManager.getCurrentTask() < this.commandManager.getCommands().size())
        {
            return this.commandManager.getCommands().get(cmdToTest).toString();
        }
        return "REPEAT " + Integer.toString(this.tasksToRepeat) + " " + ((this.numReps > 0) ? Integer.toString(this.numReps) : "");
    }
}
