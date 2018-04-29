package pcl.opendisks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import li.cil.oc.api.fs.FileSystem;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import scala.Option;
import scala.Tuple2;
import scala.collection.JavaConverters;
import scala.collection.Seq;

/**
 * @author Caitlyn
 *
 */

@Mod(modid=OpenDisks.MODID, name="OpenDisks", version=BuildInfo.versionNumber + "." + BuildInfo.buildNumber, dependencies = "after:OpenComputers", acceptableRemoteVersions = "*")

public class OpenDisks {
	public static final String MODID = "opendisks";

	@Instance(value = MODID)
	public static OpenDisks instance;
	public static Config cfg = null;

	@SidedProxy(clientSide = "pcl.opendisks.ClientProxy", serverSide = "pcl.opendisks.CommonProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load e) {
		if (!e.getWorld().isRemote && e.getWorld().provider.getDimension() == 0) {
			try {
				listFilesForFolder(new File(OpenDisks.proxy.getBaseFolder().toString() + "\\mods\\opendisks\\lua\\"));
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		}
	}

	public void listFilesForFolder(final File folder) {
		File[] files = folder.listFiles();
		if (files != null) {
			for (final File fileEntry : files) {
				if (fileEntry.isDirectory()) {
					if (new File(fileEntry+"/.disk.cfg").isFile()) {
						cfg = new Config(fileEntry + "/.disk.cfg");
						String name = cfg.getString("name", fileEntry.getName());
						int color = cfg.getInt("color", "0");
						EnumDyeColor colorEnum = EnumDyeColor.byDyeDamage(color);
						
						Callable<FileSystem> OpenDiskFactory = new Callable<FileSystem>() {
							@Override
							public FileSystem call() {
								try {
									Path sourceFile = Paths.get(DimensionManager.getCurrentSaveRootDirectory().getPath() + File.separator + "opencomputers");
									Path targetFile = Paths.get(OpenDisks.proxy.getBaseFolder().toString() + "\\mods\\opendisks\\lua\\"+fileEntry.getName()); 
									Path relativePath = sourceFile.relativize(targetFile);
									cfg = new Config(targetFile + "\\.disk.cfg");
									Boolean isReadOnly = cfg.getBool("isReadOnly", "true");
									if (isReadOnly) {
										return li.cil.oc.api.FileSystem.asReadOnly(li.cil.oc.api.FileSystem.fromSaveDirectory(File.separator + relativePath + File.separator, 1024, false));
									} else {
										return li.cil.oc.api.FileSystem.fromSaveDirectory(File.separator + relativePath + File.separator, 1024, false);
									}
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								return null;
							}
						};
						Boolean isCraftable = cfg.getBool("isCraftable", "true");
						ItemStack loot = li.cil.oc.common.Loot.registerLootDisk(name, colorEnum, OpenDiskFactory, isCraftable);
						loot.setStackDisplayName(name);
						List<Tuple2<ItemStack, Object>> elems = new ArrayList<Tuple2<ItemStack, Object>>();
						elems.add(new Tuple2<ItemStack, Object>(loot, 0));
						li.cil.oc.common.Loot.globalDisks().append(JavaConverters.asScalaBufferConverter(elems).asScala());
					} else {
						System.out.println("Not a disk");
					}
				}
			}
		}
	}

	class Config
	{
		Properties configFile;
		public Config(String path)
		{
			configFile = new java.util.Properties();
			try {
				configFile.load(new FileInputStream(path));
			}catch(Exception eta){
				eta.printStackTrace();
			}
		}

		public int getInt(String key, String defultVal)
		{
			String value = this.configFile.getProperty(key, defultVal);
			return Integer.parseInt(value);
		}
		public String getString(String key, String defultVal) {
			String value = this.configFile.getProperty(key, defultVal);
			return value;
		}
		public Boolean getBool(String key, String defultVal) {
			String value = this.configFile.getProperty(key, defultVal);
			return Boolean.parseBoolean(value);
		}
	}
}