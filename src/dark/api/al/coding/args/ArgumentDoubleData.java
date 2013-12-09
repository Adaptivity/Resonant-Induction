package dark.api.al.coding.args;

import net.minecraft.util.MathHelper;
import universalelectricity.core.electricity.ElectricityDisplay;

/** Used to create argument data for the encoder. Should only be used if the value needs to be
 * clearly limited inside the encoder display.
 * 
 * @author DarkGuardsman */
public class ArgumentDoubleData extends ArgumentData
{
    protected double max, min;

    public ArgumentDoubleData(String name, double value, double max, double min)
    {
        super(name, value);
        this.max = max;
        this.min = min;
    }

    @Override
    public boolean isValid(Object object)
    {
        if (super.isValid())
        {
            double value = MathHelper.parseDoubleWithDefault("" + object, min - 100);
            return value != min - 100 && value >= min && value <= max;
        }
        return false;
    }

    @Override
    public String warning()
    {
        return "" + ElectricityDisplay.roundDecimals(min, 2) + " - " + ElectricityDisplay.roundDecimals(max, 2);
    }
}
