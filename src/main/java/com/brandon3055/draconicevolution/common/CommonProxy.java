package com.brandon3055.draconicevolution.common;

import com.brandon3055.draconicevolution.DraconicEvolution;
import com.brandon3055.draconicevolution.client.creativetab.DETab;
import com.brandon3055.draconicevolution.client.interfaces.GuiHandler;
import com.brandon3055.draconicevolution.client.render.particle.ParticleEnergyBeam;
import com.brandon3055.draconicevolution.client.render.particle.ParticleEnergyField;
import com.brandon3055.draconicevolution.common.achievements.Achievements;
import com.brandon3055.draconicevolution.common.entity.*;
import com.brandon3055.draconicevolution.common.handler.ConfigHandler;
import com.brandon3055.draconicevolution.common.handler.CraftingHandler;
import com.brandon3055.draconicevolution.common.handler.FMLEventHandler;
import com.brandon3055.draconicevolution.common.handler.MinecraftForgeEventHandler;
import com.brandon3055.draconicevolution.common.lib.OreDoublingRegistry;
import com.brandon3055.draconicevolution.common.lib.References;
import com.brandon3055.draconicevolution.common.magic.EnchantmentReaper;
import com.brandon3055.draconicevolution.common.magic.PotionHandler;
import com.brandon3055.draconicevolution.common.network.*;
import com.brandon3055.draconicevolution.common.tileentities.*;
import com.brandon3055.draconicevolution.common.tileentities.energynet.TileEnergyRelay;
import com.brandon3055.draconicevolution.common.tileentities.energynet.TileEnergyTransceiver;
import com.brandon3055.draconicevolution.common.tileentities.multiblocktiles.TileEnderResurrection;
import com.brandon3055.draconicevolution.common.tileentities.multiblocktiles.TileEnergyPylon;
import com.brandon3055.draconicevolution.common.tileentities.multiblocktiles.TileEnergyStorageCore;
import com.brandon3055.draconicevolution.common.tileentities.multiblocktiles.TileInvisibleMultiblock;
import com.brandon3055.draconicevolution.common.utills.LogHelper;
import com.brandon3055.draconicevolution.common.world.DraconicWorldGenerator;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.potion.Potion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public abstract class CommonProxy {
	//private final static boolean debug = DraconicEvolution.debug;

	public void preInit(FMLPreInitializationEvent event) {
		ConfigHandler.init(event.getSuggestedConfigurationFile());
		registerEventListeners(event.getSide());
		ModBlocks.init();
		ModItems.init();
		GameRegistry.registerWorldGenerator(new DraconicWorldGenerator(), 1);
		registerTileEntities();
		initializeNetwork();

		DraconicEvolution.reaperEnchant = new EnchantmentReaper(ConfigHandler.reaperEnchantID);

		Potion[] potionTypes = null;
		LogHelper.info("Expanding Potion array size to 256");

		for (Field f : Potion.class.getDeclaredFields()) {
			f.setAccessible(true);

			try {
				if (f.getName().equals("potionTypes") || f.getName().equals("field_76425_a")) {
					Field modfield = Field.class.getDeclaredField("modifiers");
					modfield.setAccessible(true);
					modfield.setInt(f, f.getModifiers() & ~Modifier.FINAL);
					potionTypes = (Potion[]) f.get(null);
					final Potion[] newPotionTypes = new Potion[256];
					System.arraycopy(potionTypes, 0, newPotionTypes, 0, potionTypes.length);
					f.set(null, newPotionTypes);
				}
			}
			catch (Exception e) {
				LogHelper.error("Severe error, please report this to the mod author:");
				LogHelper.error(e);
			}
		}

		Achievements.addModAchievements();
		LogHelper.info("Finished PreInitialization");
	}

	public void init(FMLInitializationEvent event) {
		CraftingHandler.init();
		registerGuiHandeler();
		registerWorldGen();
		registerEntitys();
		DETab.initialize();
		PotionHandler.init();

		LogHelper.info("Finished Initialization");
	}

	public void postInit(FMLPostInitializationEvent event) {
		OreDoublingRegistry.init();
		Achievements.registerAchievementPane();
		LogHelper.info("Finished PostInitialization");
	}

	public void initializeNetwork() {
		DraconicEvolution.network = NetworkRegistry.INSTANCE.newSimpleChannel(DraconicEvolution.networkChannelName);
		DraconicEvolution.network.registerMessage(ButtonPacket.Handler.class, ButtonPacket.class, 0, Side.SERVER);
		DraconicEvolution.network.registerMessage(ParticleGenPacket.Handler.class, ParticleGenPacket.class, 1, Side.SERVER);
		DraconicEvolution.network.registerMessage(PlacedItemPacket.Handler.class, PlacedItemPacket.class, 2, Side.SERVER);
		DraconicEvolution.network.registerMessage(PlayerDetectorButtonPacket.Handler.class, PlayerDetectorButtonPacket.class, 3, Side.SERVER);
		DraconicEvolution.network.registerMessage(PlayerDetectorStringPacket.Handler.class, PlayerDetectorStringPacket.class, 4, Side.SERVER);
		DraconicEvolution.network.registerMessage(TeleporterPacket.Handler.class, TeleporterPacket.class, 5, Side.SERVER);
		DraconicEvolution.network.registerMessage(TileObjectPacket.Handler.class, TileObjectPacket.class, 6, Side.CLIENT);
		DraconicEvolution.network.registerMessage(MountUpdatePacket.Handler.class, MountUpdatePacket.class, 7, Side.CLIENT);
		DraconicEvolution.network.registerMessage(MountUpdatePacket.Handler.class, MountUpdatePacket.class, 8, Side.SERVER);
		DraconicEvolution.network.registerMessage(ItemConfigPacket.Handler.class, ItemConfigPacket.class, 9, Side.SERVER);
		DraconicEvolution.network.registerMessage(TileObjectPacket.Handler.class, TileObjectPacket.class, 10, Side.SERVER);
	}

	public void registerTileEntities() {
		GameRegistry.registerTileEntity(TileWeatherController.class, References.RESOURCESPREFIX + "TileWeatherController");
		GameRegistry.registerTileEntity(TileSunDial.class, References.RESOURCESPREFIX + "TileSunDial");
		GameRegistry.registerTileEntity(TileGrinder.class, References.RESOURCESPREFIX + "TileGrinder");
		GameRegistry.registerTileEntity(TilePotentiometer.class, References.RESOURCESPREFIX + "TilePotentiometer");
		GameRegistry.registerTileEntity(TileParticleGenerator.class, References.RESOURCESPREFIX + "TileParticleGenerator");
		GameRegistry.registerTileEntity(TilePlayerDetector.class, References.RESOURCESPREFIX + "TilePlayerDetector");
		GameRegistry.registerTileEntity(TilePlayerDetectorAdvanced.class, References.RESOURCESPREFIX + "TilePlayerDetectorAdvanced");
		GameRegistry.registerTileEntity(TileEnergyInfuser.class, References.RESOURCESPREFIX + "TileEnergyInfuser");
		GameRegistry.registerTileEntity(TileCustomSpawner.class, References.RESOURCESPREFIX + "TileCustomSpawner");
		GameRegistry.registerTileEntity(TileGenerator.class, References.RESOURCESPREFIX + "TileGenerator");
		GameRegistry.registerTileEntity(TileEnergyStorageCore.class, References.RESOURCESPREFIX + "TileEnergyStorageCore");
		GameRegistry.registerTileEntity(TileInvisibleMultiblock.class, References.RESOURCESPREFIX + "TileInvisibleMultiblock");
		GameRegistry.registerTileEntity(TileEnergyPylon.class, References.RESOURCESPREFIX + "TileEnergyPylon");
		GameRegistry.registerTileEntity(TileEnderResurrection.class, References.RESOURCESPREFIX + "TileEnderResurrection");
		GameRegistry.registerTileEntity(TilePlacedItem.class, References.RESOURCESPREFIX + "TilePlacedItem");
		GameRegistry.registerTileEntity(TileCKeyStone.class, References.RESOURCESPREFIX + "TileCKeyStone");
		GameRegistry.registerTileEntity(TileDissEnchanter.class, References.RESOURCESPREFIX + "TileDissEnchanter");
		GameRegistry.registerTileEntity(TileTeleporterStand.class, References.RESOURCESPREFIX + "TileTeleporterStand");
		GameRegistry.registerTileEntity(TileDraconiumChest.class, References.RESOURCESPREFIX + "TileDraconiumChest");
		GameRegistry.registerTileEntity(TileEnergyRelay.class, References.RESOURCESPREFIX + "TileEnergyRelay");
		GameRegistry.registerTileEntity(TileEnergyTransceiver.class, References.RESOURCESPREFIX + "TileEnergyTransceiver");
		if (DraconicEvolution.debug) {
			GameRegistry.registerTileEntity(TileTestBlock.class, References.RESOURCESPREFIX + "TileTestBlock");
			GameRegistry.registerTileEntity(TileContainerTemplate.class, References.RESOURCESPREFIX + "TileContainerTemplate");
		}
	}

	public void registerEventListeners(Side s) {
		MinecraftForge.EVENT_BUS.register(new MinecraftForgeEventHandler());
		MinecraftForge.EVENT_BUS.register(new Achievements());
		FMLCommonHandler.instance().bus().register(new Achievements());
		FMLCommonHandler.instance().bus().register(new FMLEventHandler());
	}

	public void registerGuiHandeler() {
		new GuiHandler();
	}

	public void registerWorldGen() {
		GameRegistry.registerWorldGenerator(new DraconicWorldGenerator(), 0);
	}

	public void registerEntitys() {
		EntityRegistry.registerModEntity(EntityCustomDragon.class, "EnderDragon", 0, DraconicEvolution.instance, 500, 3, true);
		EntityRegistry.registerModEntity(EntityPersistentItem.class, "Persistent Item", 1, DraconicEvolution.instance, 32, 5, true);
		EntityRegistry.registerModEntity(EntityDraconicArrow.class, "Arrow", 2, DraconicEvolution.instance, 32, 5, true);
		EntityRegistry.registerModEntity(EntityEnderArrow.class, "Ender Arrow", 3, DraconicEvolution.instance, 32, 5, true);
		EntityRegistry.registerModEntity(EntityChaosDrill.class, "Chaos Drill", 4, DraconicEvolution.instance, 10, 5, false);
		EntityRegistry.registerModEntity(EntityDragonHeart.class, "Dragon Heart Item", 5, DraconicEvolution.instance, 32, 5, true);
	}

	public abstract boolean isDedicatedServer();

	public MinecraftServer getMCServer(){
		return FMLCommonHandler.instance().getMinecraftServerInstance();
	}

	public ParticleEnergyBeam energyBeam(World worldObj, double x, double y, double z, double tx, double ty, double tz, int powerFlow, boolean advanced, ParticleEnergyBeam oldBeam, boolean render, int beamType)
	{
		return null;
	}

	public ParticleEnergyField energyField(World worldObj, double x, double y, double z, int type, boolean advanced, ParticleEnergyField oldBeam, boolean render) {
		return null;
	}
}
