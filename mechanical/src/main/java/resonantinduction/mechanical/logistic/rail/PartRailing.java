package resonantinduction.mechanical.logistic.rail;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import resonantinduction.core.prefab.part.PartFramedConnection;
import resonantinduction.mechanical.fluid.pipe.EnumPipeMaterial;
import universalelectricity.api.net.INodeNetwork;
import codechicken.microblock.IHollowConnect;
import codechicken.multipart.JIconHitEffects;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.TSlottedPart;

public class PartRailing extends PartFramedConnection<EnumPipeMaterial, IRailing, INodeNetwork> implements IRailing, TSlottedPart, JNormalOcclusion, IHollowConnect, JIconHitEffects
{
	RedstoneControl control;

	@Override
	protected boolean canConnectTo(TileEntity tile)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected PartRailing getConnector(TileEntity tile)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMaterial(int i)
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected ItemStack getItem()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getType()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public INodeNetwork getNetwork()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
