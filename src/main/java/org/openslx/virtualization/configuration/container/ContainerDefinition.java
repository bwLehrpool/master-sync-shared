package org.openslx.virtualization.configuration.container;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import org.apache.log4j.Logger;
import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarInputStream;
import org.kamranzafar.jtar.TarOutputStream;
import org.openslx.util.TarArchiveUtil;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ContainerDefinition {

	//	TODO database needs a refactoring to store container details
	//  TODO refatoring: tar.gz of this object is not useful, for smaller dockerfiles it makes the package lager.
	//		 remove the containerRecipe, ContainerMeta holds in build_context the dockerfile.

	protected static final Logger LOGGER = Logger.getLogger(ContainerDefinition.class);

	protected static final String CONTAINER_FILE = "dockerfile";
	protected static final String CONTAINER_META_FILE = "container_meta.json";

	/**
	 * The file to construct a real container image, could be an dockerfile or a singularity recipe.
	 */
	public String containerRecipe = "";

	/**
	 * Further container information, see {@link ContainerMeta}.
	 */
	public ContainerMeta containerMeta;

	public ContainerDefinition() {
		containerMeta = new ContainerMeta();
	}

	/**
	 * Copy Constructor
	 *
	 * @param containerDef {@link ContainerDefinition} from which to make a deep copy.
	 */
	public ContainerDefinition(ContainerDefinition containerDef) {
		containerRecipe = String.valueOf(containerDef.getContainerRecipe());
		containerMeta = new ContainerMeta(containerDef.getContainerMeta());
	}

	/**
	 * Utility function to create a {@link ContainerDefinition} object for a byte array downloaded from the server.
	 *
	 * @param rawTarData Downloaded tar.gz file from the server as a byte array.
	 * @return New object of ContainerDefinition.
	 */
	public static ContainerDefinition fromByteArray(byte[] rawTarData) {

		ContainerDefinition containerDef = new ContainerDefinition();

		try {
			TarInputStream tis = new TarInputStream(
					new GZIPInputStream(new BufferedInputStream(new ByteArrayInputStream(rawTarData))));

			TarEntry entry;

			while ((entry = tis.getNextEntry()) != null) {
				byte[] rawData = new byte[1024];
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				int count;

				// read everything from the TarInputStream for the current Entry
				while ((count = tis.read(rawData)) != -1) {
					output.write(rawData, 0, count);
				}

				if (entry.getName().equals(CONTAINER_FILE))
					containerDef.setContainerRecipe(output.toByteArray());
				if (entry.getName().equals(CONTAINER_META_FILE))
					containerDef.setContainerMeta(output.toByteArray());
			}

		} catch (IOException e) {
			LOGGER.error("Could not create a ContainerDefinition Object for rawTarData", e);
		}

		return containerDef;
	}

	public String getContainerRecipe() {
		return containerRecipe;
	}

	public void setContainerRecipe(String containerRecipe) {
		this.containerRecipe = containerRecipe;
	}

	public void setContainerRecipe(File containerRecipeFile) {
		this.containerRecipe = readContainerRecipe(containerRecipeFile);
	}

	public void setContainerRecipe(byte[] rawContainerRecipe) {
		this.containerRecipe = new String(rawContainerRecipe, StandardCharsets.UTF_8);
	}

	public ContainerMeta getContainerMeta() {
		return containerMeta;
	}

	public void setContainerMeta(byte[] containerMeta) {
		Gson gson = new GsonBuilder().create();
		this.containerMeta = gson.fromJson(new JsonReader(
						new InputStreamReader(new ByteArrayInputStream(containerMeta), StandardCharsets.UTF_8)),
				ContainerMeta.class);
	}

	/**
	 * Serializes the ContainerMeta and Container Description (e.g. dockerfile) into an tar.gz archive.
	 *
	 * @return A ByteBuffer object of the container definition. Can be uploaded so satellite server.
	 */
	public ByteBuffer toByteBuffer() {

		ByteBuffer containerDef = null;

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			TarOutputStream output = new TarOutputStream(new GZIPOutputStream(baos));

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			TarArchiveUtil.tarPutFile(output, CONTAINER_META_FILE, gson.toJson(containerMeta));
			TarArchiveUtil.tarPutFile(output, CONTAINER_FILE, containerRecipe);
			output.close();

			containerDef = ByteBuffer.wrap(baos.toByteArray());

		} catch (IOException e) {
			LOGGER.warn("Could not create a tar file", e);
		}

		return containerDef;
	}

	private String readContainerRecipe(File file) {
		String recipe = null;
		try {

			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			ByteArrayOutputStream rawFile = new ByteArrayOutputStream();
			int count;
			byte[] data = new byte[1024];
			while ((count = bis.read(data)) != -1) {
				rawFile.write(data, 0, count);
			}

			String rawRecipe = new String(rawFile.toByteArray(), StandardCharsets.UTF_8);

			// replace windows by unix EOL
			recipe = rawRecipe.replaceAll("\\r\\n", "\n");

			bis.close();

		} catch (IOException e) {
			LOGGER.error("Could not read Container Recipe", e);
		}
		return recipe;
	}

	/**
	 * Saves containerRecipe and containerMeta at the provided location.
	 *
	 * @param destDir destination directory for containerRecipe and containerMeta.
	 */
	public void saveLocal(File destDir) {
		writeFile(destDir, containerRecipe, CONTAINER_FILE);
	}

	private void writeFile(File destDir, String fileContent, String filename) {
		File output = new File(destDir, filename);
		try {
			FileWriter fw = new FileWriter(output);
			fw.write(fileContent);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.error("Could not write File", e);
		}
	}

	public ContainerImageContext getContainerImageContext() {
		return ContainerImageContext.fromInt(containerMeta.getContainerImageContext());
	}

	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ContainerDefinition that = (ContainerDefinition) o;
		return containerRecipe.equals(that.containerRecipe) && containerMeta.equals(that.containerMeta);
	}

	@Override public int hashCode() {
		return Objects.hash(containerRecipe, containerMeta);
	}
}
