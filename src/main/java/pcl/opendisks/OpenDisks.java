package pcl.opendisks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import li.cil.oc.api.fs.FileSystem;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * @author Caitlyn
 *
 */

@Mod(modid=OpenDisks.MODID, name="OpenDisks", version=BuildInfo.versionNumber + "." + BuildInfo.buildNumber, dependencies = "after:OpenComputers")

public class OpenDisks {
	public static final String MODID = "opendisks";

	@Instance(value = MODID)
	public static OpenDisks instance;
	public static Config cfg = null;

	@SidedProxy(clientSide = "pcl.opendisks.ClientProxy", serverSide = "pcl.opendisks.CommonProxy")
	public static CommonProxy proxy;

	private static boolean debug = true;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		listFilesForFolder(new File(event.getModConfigurationDirectory()+"/../mods/opendisks/lua/"));
	}

	public void listFilesForFolder(final File folder) {
		File[] files = folder.listFiles();
		if (files != null) {
			int i = 1;
			for (final File fileEntry : files) {
				if (fileEntry.isDirectory()) {
					if (new File(fileEntry+"/disk.cfg").isFile()) {
						Callable<FileSystem> OpenDiskFactory = new Callable<FileSystem>() {
							@Override
							public FileSystem call() {
								try {
									Path sourceFile = Paths.get(DimensionManager.getCurrentSaveRootDirectory().getPath() + File.separator + "opencomputers");
									Path targetFile = Paths.get(OpenDisks.proxy.getBaseFolder().toString() + "\\mods\\opendisks\\lua\\"+fileEntry.getName()); 
									Path relativePath = sourceFile.relativize(targetFile);
									cfg = new Config(targetFile + "\\disk.cfg");
									Boolean isReadOnly = cfg.getBool("isReadOnly");
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
						cfg = new Config(fileEntry + "\\disk.cfg");
						int color = cfg.getInt("color");
						String name = cfg.getString("name");
						EnumDyeColor colorEnum = EnumDyeColor.byDyeDamage(color);
						ItemStack floppy = li.cil.oc.api.Items.registerFloppy(name, colorEnum, OpenDiskFactory, true);
						floppy.setStackDisplayName(name);
						System.out.println("Registering a floppy with name " + fileEntry.getName() + " Color: " + colorEnum.getName());
						i++;
					} else {
						System.out.println("Not a disk");
					}
				} else {

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

		public int getInt(String key)
		{
			String value = this.configFile.getProperty(key);
			return Integer.parseInt(value);
		}
		public String getString(String key) {
			String value = this.configFile.getProperty(key);
			return value;
		}
		public Boolean getBool(String key) {
			String value = this.configFile.getProperty(key);
			return Boolean.parseBoolean(value);
		}
	}
}