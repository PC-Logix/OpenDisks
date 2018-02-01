package pcl.opendisks;

import java.io.IOException;
import java.nio.file.Path;

import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.server.FMLServerHandler;

public class CommonProxy {

	public Path getBaseFolder() throws IOException {
		Path universeFolder = FMLServerHandler.instance().getSavesDirectory().toPath();
		return universeFolder;
	}
}
