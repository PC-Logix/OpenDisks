package pcl.opendisks;

import java.io.IOException;
import java.nio.file.Path;

import cpw.mods.fml.server.FMLServerHandler;

public class CommonProxy {
	public Path getDisksFolder() throws IOException {
		Path universeFolder = FMLServerHandler.instance().getSavesDirectory().toPath();
		Path backupsFolder = universeFolder.resolve("../../mods/opendisks/");
		return backupsFolder;
	}
	
	public Path getBaseFolder() throws IOException {
		Path universeFolder = FMLServerHandler.instance().getSavesDirectory().toPath();
		return universeFolder;
	}
}
