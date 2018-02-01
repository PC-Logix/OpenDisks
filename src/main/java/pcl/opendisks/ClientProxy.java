package pcl.opendisks;

import java.io.IOException;
import java.nio.file.Path;

import net.minecraft.client.Minecraft;

public class ClientProxy extends CommonProxy {

	@Override
	public Path getBaseFolder() throws IOException {
		return Minecraft.getMinecraft().mcDataDir.toPath();
	}
}
