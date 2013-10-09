package resonantinduction;

import ic2.api.item.Items;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import org.modstats.ModstatInfo;
import org.modstats.Modstats;

import resonantinduction.battery.BlockBattery;
import resonantinduction.battery.ItemCapacitor;
import resonantinduction.battery.ItemInfiniteCapacitor;
import resonantinduction.battery.TileEntityBattery;
import resonantinduction.contractor.BlockEMContractor;
import resonantinduction.contractor.ItemBlockContractor;
import resonantinduction.contractor.TileEntityEMContractor;
import resonantinduction.entangler.ItemLinker;
import resonantinduction.entangler.ItemQuantumEntangler;
import resonantinduction.multimeter.BlockMultimeter;
import resonantinduction.multimeter.ItemBlockMultimeter;
import resonantinduction.multimeter.MultimeterEventHandler;
import resonantinduction.multimeter.TileEntityMultimeter;
import resonantinduction.tesla.BlockTesla;
import resonantinduction.tesla.TileEntityTesla;
import resonantinduction.wire.BlockWire;
import resonantinduction.wire.EnumWireMaterial;
import resonantinduction.wire.ItemBlockWire;
import resonantinduction.wire.TileEntityTickWire;
import resonantinduction.wire.TileEntityWire;
import scala.collection.generic.BitOperations.Int;
import universalelectricity.compatibility.Compatibility;
import universalelectricity.core.item.IItemElectric;
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.TranslationHelper;
import basiccomponents.api.BasicRegistry;
import calclavia.lib.UniversalRecipes;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * @author Calclavia
 * 
 */
@Mod(modid = ResonantInduction.ID, name = ResonantInduction.NAME, version = ResonantInduction.VERSION)
@NetworkMod(channels = ResonantInduction.CHANNEL, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class)
@ModstatInfo(prefix = "resonantin")
public class ResonantInduction
{
	/**
	 * Mod Information
	 */
	public static final String ID = "resonantinduction";
	public static final String NAME = "Resonant Induction";
	public static final String CHANNEL = "RESIND";

	public static final String MAJOR_VERSION = "@MAJOR@";
	public static final String MINOR_VERSION = "@MINOR@";
	public static final String REVISION_VERSION = "@REVIS@";
	public static final String BUILD_VERSION = "@BUILD@";
	public static final String VERSION = MAJOR_VERSION + "." + MINOR_VERSION + "." + REVISION_VERSION;

	@Instance(ID)
	public static ResonantInduction INSTANCE;

	@SidedProxy(clientSide = ID + ".ClientProxy", serverSide = ID + ".CommonProxy")
	public static CommonProxy proxy;

	@Mod.Metadata(ID)
	public static ModMetadata metadata;

	public static final Logger LOGGER = Logger.getLogger(NAME);

	/**
	 * Directory Information
	 */
	public static final String DOMAIN = "resonantinduction";
	public static final String PREFIX = DOMAIN + ":";
	public static final String DIRECTORY = "/assets/" + DOMAIN + "/";
	public static final String TEXTURE_DIRECTORY = "textures/";
	public static final String GUI_DIRECTORY = TEXTURE_DIRECTORY + "gui/";
	public static final String BLOCK_TEXTURE_DIRECTORY = TEXTURE_DIRECTORY + "blocks/";
	public static final String ITEM_TEXTURE_DIRECTORY = TEXTURE_DIRECTORY + "items/";
	public static final String MODEL_TEXTURE_DIRECTORY = TEXTURE_DIRECTORY + "models/";
	public static final String MODEL_DIRECTORY = DIRECTORY + "models/";

	public static final String LANGUAGE_DIRECTORY = DIRECTORY + "languages/";
	public static final String[] LANGUAGES = new String[] { "en_US", "de_DE" };

	/**
	 * Settings
	 */
	public static final Configuration CONFIGURATION = new Configuration(new File(Loader.instance().getConfigDir(), NAME + ".cfg"));
	public static float FURNACE_WATTAGE = 1;
	public static boolean SOUND_FXS = true;
	public static boolean LO_FI_INSULATION = false;
	public static boolean SHINY_SILVER = true;

	/** Block ID by Jyzarc */
	private static final int BLOCK_ID_PREFIX = 3200;
	/** Item ID by Horfius */
	private static final int ITEM_ID_PREFIX = 20150;
	public static int MAX_CONTRACTOR_DISTANCE = 200;

	private static int NEXT_BLOCK_ID = BLOCK_ID_PREFIX;
	private static int NEXT_ITEM_ID = ITEM_ID_PREFIX;

	public static int getNextBlockID()
	{
		return NEXT_BLOCK_ID++;
	}

	public static int getNextItemID()
	{
		return NEXT_ITEM_ID++;
	}

	// Items
	public static Item itemQuantumEntangler;
	public static Item itemCapacitor;
	public static Item itemInfiniteCapacitor;
	public static Item itemLinker;
	/** With Forge Multipart; Use EnumWireMaterial reference. **/
	private static Item itemPartWire;

	// Blocks
	public static Block blockTesla;
	public static Block blockMultimeter;
	public static Block blockEMContractor;
	public static Block blockBattery;
	/** Without Forge Multipart **/
	private static Block blockWire;

	public static Block blockAdvancedFurnaceIdle, blockAdvancedFurnaceBurning;

	public static final Vector3[] DYE_COLORS = new Vector3[] { new Vector3(), new Vector3(1, 0, 0), new Vector3(0, 0.608, 0.232), new Vector3(0.588, 0.294, 0), new Vector3(0, 0, 1), new Vector3(0.5, 0, 05), new Vector3(0, 1, 1), new Vector3(0.8, 0.8, 0.8), new Vector3(0.3, 0.3, 0.3), new Vector3(1, 0.412, 0.706), new Vector3(0.616, 1, 0), new Vector3(1, 1, 0), new Vector3(0.46f, 0.932, 1), new Vector3(0.5, 0.2, 0.5), new Vector3(0.7, 0.5, 0.1), new Vector3(1, 1, 1) };

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt)
	{
		LOGGER.setParent(FMLLog.getLogger());
		NetworkRegistry.instance().registerGuiHandler(this, ResonantInduction.proxy);
		Modstats.instance().getReporter().registerMod(this);
		MinecraftForge.EVENT_BUS.register(new MultimeterEventHandler());
		CONFIGURATION.load();

		// Config
		FURNACE_WATTAGE = (float) CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Furnace Wattage Per Tick", FURNACE_WATTAGE).getDouble(FURNACE_WATTAGE);
		SOUND_FXS = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Tesla Sound FXs", SOUND_FXS).getBoolean(SOUND_FXS);
		LO_FI_INSULATION = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Use lo-fi insulation texture", LO_FI_INSULATION).getBoolean(LO_FI_INSULATION);
		SHINY_SILVER = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Shiny silver wires", SHINY_SILVER).getBoolean(SHINY_SILVER);
		MAX_CONTRACTOR_DISTANCE = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Max EM Contractor Path", MAX_CONTRACTOR_DISTANCE).getInt(MAX_CONTRACTOR_DISTANCE);

		TileEntityEMContractor.ACCELERATION = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Contractor Item Acceleration", TileEntityEMContractor.ACCELERATION).getDouble(TileEntityEMContractor.ACCELERATION);
		TileEntityEMContractor.MAX_REACH = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Contractor Max Item Reach", TileEntityEMContractor.MAX_REACH).getInt(TileEntityEMContractor.MAX_REACH);
		TileEntityEMContractor.MAX_SPEED = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Contractor Max Item Speed", TileEntityEMContractor.MAX_SPEED).getDouble(TileEntityEMContractor.MAX_SPEED);
		TileEntityEMContractor.PUSH_DELAY = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Contractor Item Push Delay", TileEntityEMContractor.PUSH_DELAY).getInt(TileEntityEMContractor.PUSH_DELAY);

		// Items
		itemQuantumEntangler = new ItemQuantumEntangler(getNextItemID());
		itemCapacitor = new ItemCapacitor(getNextItemID());
		itemLinker = new ItemLinker(getNextItemID());
		itemInfiniteCapacitor = new ItemInfiniteCapacitor(getNextItemID());

		if (Loader.isModLoaded("ForgeMultipart"))
		{
			try
			{
				itemPartWire = (Item) Class.forName("resonantinduction.wire.multipart.ItemPartWire").getConstructor(Int.class).newInstance(getNextItemID());
			}
			catch (Exception e)
			{
				LOGGER.severe("Failed to load multipart wire.");
			}
		}

		// Blocks
		blockTesla = new BlockTesla(getNextBlockID());
		blockMultimeter = new BlockMultimeter(getNextBlockID());
		blockEMContractor = new BlockEMContractor(getNextBlockID());
		blockBattery = new BlockBattery(getNextBlockID());

		if (itemPartWire == null)
		{
			blockWire = new BlockWire(getNextBlockID());
		}

		blockAdvancedFurnaceIdle = new BlockAdvancedFurnace(getNextBlockID(), false);
		blockAdvancedFurnaceBurning = new BlockAdvancedFurnace(getNextBlockID(), true);

		CONFIGURATION.save();

		GameRegistry.registerItem(itemQuantumEntangler, itemQuantumEntangler.getUnlocalizedName());
		GameRegistry.registerItem(itemCapacitor, itemCapacitor.getUnlocalizedName());
		GameRegistry.registerItem(itemInfiniteCapacitor, itemInfiniteCapacitor.getUnlocalizedName());
		GameRegistry.registerItem(itemLinker, itemLinker.getUnlocalizedName());

		GameRegistry.registerBlock(blockTesla, blockTesla.getUnlocalizedName());
		GameRegistry.registerBlock(blockMultimeter, ItemBlockMultimeter.class, blockMultimeter.getUnlocalizedName());
		GameRegistry.registerBlock(blockEMContractor, ItemBlockContractor.class, blockEMContractor.getUnlocalizedName());
		GameRegistry.registerBlock(blockBattery, blockBattery.getUnlocalizedName());

		if (blockWire != null)
		{
			GameRegistry.registerBlock(blockWire, ItemBlockWire.class, blockWire.getUnlocalizedName());
		}

		// Tiles
		GameRegistry.registerTileEntity(TileEntityTesla.class, blockTesla.getUnlocalizedName());
		GameRegistry.registerTileEntity(TileEntityMultimeter.class, blockMultimeter.getUnlocalizedName());
		GameRegistry.registerTileEntity(TileEntityEMContractor.class, blockEMContractor.getUnlocalizedName());
		GameRegistry.registerTileEntity(TileEntityBattery.class, blockBattery.getUnlocalizedName());

		if (blockWire != null)
		{
			GameRegistry.registerTileEntity(TileEntityWire.class, blockWire.getUnlocalizedName());
			GameRegistry.registerTileEntity(TileEntityTickWire.class, blockWire.getUnlocalizedName() + "2");
		}

		ResonantInduction.proxy.registerRenderers();

		/**
		 * Set reference itemstacks
		 */
		TabRI.ITEMSTACK = new ItemStack(blockBattery);

		if (itemPartWire != null)
		{
			for (EnumWireMaterial material : EnumWireMaterial.values())
			{
				material.setWire(itemPartWire);
			}
		}
		else
		{
			for (EnumWireMaterial material : EnumWireMaterial.values())
			{
				material.setWire(blockWire);
			}
		}

		// Basic Components
		BasicRegistry.register("itemIngotCopper");
		BasicRegistry.register("itemIngotSteel");
		BasicRegistry.register("itemPlateSteel");
		BasicRegistry.register("itemIngotBronze");
	}

	@EventHandler
	public void init(FMLInitializationEvent evt)
	{
		LOGGER.fine("Languages Loaded:" + TranslationHelper.loadLanguages(LANGUAGE_DIRECTORY, LANGUAGES));

		metadata.modId = ID;
		metadata.name = NAME;
		metadata.description = "Resonant Induction is a Minecraft mod focusing on the manipulation of electricity and wireless technology. Ever wanted blazing electrical shocks flying off your evil lairs? You've came to the right place!";
		metadata.url = "http://universalelectricity.com/resonant-induction";
		metadata.logoFile = "/ri_logo.png";
		metadata.version = VERSION + BUILD_VERSION;
		metadata.authorList = Arrays.asList(new String[] { "Calclavia", "Aidancbrady" });
		metadata.credits = "Thanks to Archadia for the awesome assets!";
		metadata.autogenerated = false;

		if (itemPartWire != null)
		{
			try
			{
				Class.forName("resonantinduction.MultipartRI").newInstance();
			}
			catch (InstantiationException | IllegalAccessException | ClassNotFoundException e)
			{
				e.printStackTrace();
				LOGGER.severe("Failed to initiate Resonant Induction multipart module.");
			}
		}

		Compatibility.initiate();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt)
	{
		/**
		 * Recipes
		 */
		ItemStack emptyCapacitor = new ItemStack(itemCapacitor);
		((IItemElectric) itemCapacitor).setElectricity(emptyCapacitor, 0);

		final ItemStack defaultWire = EnumWireMaterial.IRON.getWire();

		/** Capacitor **/
		GameRegistry.addRecipe(new ShapedOreRecipe(emptyCapacitor, "RRR", "RIR", "RRR", 'R', Item.redstone, 'I', UniversalRecipes.PRIMARY_METAL));

		/** Linker **/
		GameRegistry.addRecipe(new ShapedOreRecipe(itemLinker, " E ", "GCG", " E ", 'E', Item.eyeOfEnder, 'C', emptyCapacitor, 'G', UniversalRecipes.SECONDARY_METAL));

		/** Quantum Entangler **/
		GameRegistry.addRecipe(new ShapedOreRecipe(itemQuantumEntangler, "EEE", "ILI", "EEE", 'E', Item.eyeOfEnder, 'L', itemLinker, 'I', UniversalRecipes.PRIMARY_METAL));

		/** Tesla - by Jyzarc */
		GameRegistry.addRecipe(new ShapedOreRecipe(blockTesla, "WEW", " C ", " I ", 'W', defaultWire, 'E', Item.eyeOfEnder, 'C', emptyCapacitor, 'I', UniversalRecipes.PRIMARY_PLATE));

		/** Multimeter */
		GameRegistry.addRecipe(new ShapedOreRecipe(blockMultimeter, "WWW", "ICI", 'W', defaultWire, 'C', emptyCapacitor, 'I', UniversalRecipes.PRIMARY_METAL));

		/** Multimeter */
		GameRegistry.addRecipe(new ShapedOreRecipe(blockBattery, "III", "IRI", "III", 'R', Block.blockRedstone, 'I', UniversalRecipes.PRIMARY_METAL));

		/** EM Contractor */
		GameRegistry.addRecipe(new ShapedOreRecipe(blockEMContractor, " I ", "GCG", "WWW", 'W', UniversalRecipes.PRIMARY_METAL, 'C', emptyCapacitor, 'G', UniversalRecipes.SECONDARY_METAL, 'I', UniversalRecipes.PRIMARY_METAL));

		/** Wires **/
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.COPPER.getWire(3), "MMM", 'M', "ingotCopper"));
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.TIN.getWire(3), "MMM", 'M', "ingotTin"));
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.IRON.getWire(3), "MMM", 'M', Item.ingotIron));
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.ALUMINUM.getWire(3), "MMM", 'M', "ingotAluminum"));
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.SILVER.getWire(), "MMM", 'M', "ingotSilver"));
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.SUPERCONDUCTOR.getWire(3), "MMM", 'M', "ingotSuperconductor"));
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.SUPERCONDUCTOR.getWire(3), "MMM", "MEM", "MMM", 'M', Item.ingotGold, "E", Item.eyeOfEnder));

		/** Wire Compatiblity **/
		if (Loader.isModLoaded("IC2"))
		{
			GameRegistry.addRecipe(new ShapelessOreRecipe(EnumWireMaterial.COPPER.getWire(), Items.getItem("copperCableItem")));
			GameRegistry.addRecipe(new ShapelessOreRecipe(EnumWireMaterial.TIN.getWire(), Items.getItem("tinCableItem")));
			GameRegistry.addRecipe(new ShapelessOreRecipe(EnumWireMaterial.IRON.getWire(), Items.getItem("ironCableItem")));
			GameRegistry.addRecipe(new ShapelessOreRecipe(EnumWireMaterial.SUPERCONDUCTOR.getWire(), Items.getItem("glassFiberCableItem")));
		}

		if (Loader.isModLoaded("Mekanism"))
		{
			GameRegistry.addRecipe(new ShapelessOreRecipe(EnumWireMaterial.COPPER.getWire(), "universalCable"));
		}

		/** Inject new furnace tile class */
		replaceTileEntity(TileEntityFurnace.class, TileEntityAdvancedFurnace.class);
	}

	public static void replaceTileEntity(Class<? extends TileEntity> findTile, Class<? extends TileEntity> replaceTile)
	{
		try
		{
			Map<String, Class> nameToClassMap = ObfuscationReflectionHelper.getPrivateValue(TileEntity.class, null, "field_" + "70326_a", "nameToClassMap", "a");
			Map<Class, String> classToNameMap = ObfuscationReflectionHelper.getPrivateValue(TileEntity.class, null, "field_" + "70326_b", "classToNameMap", "b");

			String findTileID = classToNameMap.get(findTile);

			if (findTileID != null)
			{
				nameToClassMap.put(findTileID, replaceTile);
				classToNameMap.put(replaceTile, findTileID);
				classToNameMap.remove(findTile);
				LOGGER.fine("Replaced TileEntity: " + findTile);
			}
			else
			{
				LOGGER.severe("Failed to replace TileEntity: " + findTile);
			}
		}
		catch (Exception e)
		{
			LOGGER.severe("Failed to replace TileEntity: " + findTile);
			e.printStackTrace();
		}
	}
}
