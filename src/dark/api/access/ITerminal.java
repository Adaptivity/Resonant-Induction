package dark.api.access;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import dark.core.interfaces.IScroll;

/** Basic methods to make it easier to construct or interact with a terminal based tile. Recommend to
 * be used by tiles that want to mimic computer command line like interfaces. As well to restrict
 * access to the tile in the same way a computer would
 * 
 * @author DarkGuardsmsan */
public interface ITerminal extends ISpecialAccess, IScroll
{
    /** Gets an output of the string stored in the console. */
    public List<String> getTerminalOuput();

    /** Adds a string to the console. Server side only. */
    public boolean addToConsole(String msg);

    public boolean canUse(String node, EntityPlayer player);
}
