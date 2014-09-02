package org.jwildfire.headless;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.jwildfire.base.Prefs;
import org.jwildfire.base.Tools;
import org.jwildfire.create.tina.base.Flame;
import org.jwildfire.create.tina.base.raster.RasterPointPrecision;
import org.jwildfire.create.tina.io.FlameReader;
import org.jwildfire.create.tina.render.FlameRenderer;
import org.jwildfire.create.tina.render.RenderInfo;
import org.jwildfire.create.tina.render.RenderMode;
import org.jwildfire.create.tina.render.RenderedFlame;
import org.jwildfire.io.ImageWriter;

public class JWildfireCli {
	private static final String OPT_WIDTH = "w";
	private static final String OPT_HEIGHT = "h";
	private static final String OPT_QUALITY = "q";
	private static final String OPT_HELP = "?";
	private static final String OPT_VERSION = "version";
	private static final String OPT_VERBOSE = "verbose";
	private static final String OPT_HDR = "hdr";
	private static final String OPT_HDRINTENSITYMAP = "intensity";
	private static final String OPT_USE_DIMENSION = "D";
	private static final String OPT_USE_QUALITY = "Q";
	private static final String OPT_OVERWRITE = "f";
	private static final String OPT_LOWMEM = "l";
	private static final String OPT_LOWMEM_KEEP = "k";

	private static final List<String> sBatchRenderList = new ArrayList<String>();

	private static boolean sUseQualityInFilename;
	private static boolean sUseDimensionsInFilename;
	private static boolean sHdrOutput;
	private static boolean sVerbose;
	private static boolean sHdrIntensityMapOutput;
	private static boolean sOverwriteExisting;
	private static boolean sLowMemoryMode;
	private static boolean sLowMemoryKeep;
	private static int sWidth, sHeight, sQuality;
	private static Prefs sPrefs = new Prefs();
	static {
		sPrefs.setTinaRasterPointPrecision(RasterPointPrecision.DOUBLE_PRECISION);
	}

	@SuppressWarnings("static-access")
	private static Options getOptions() {
		Options options = new Options();

		options.addOption(OPT_HELP, false, "print this message");
		options.addOption(OPT_VERSION, false, "print the version information and exit");
		options.addOption(OPT_VERBOSE, false, "be extra verbose");
		options.addOption(OPT_HDR, false, "save HDR");
		options.addOption(OPT_HDRINTENSITYMAP, false, "save HDR intensity map");
		options.addOption(OPT_USE_DIMENSION, false, "include dimensions in output filename");
		options.addOption(OPT_USE_QUALITY, false, "include quality in output filename");
		options.addOption(OPT_OVERWRITE, false, "overwrite existing file if it has same name");
		options.addOption(OPT_LOWMEM, false, "low-memory mode");
		options.addOption(OPT_LOWMEM_KEEP, false, "keep low-memory intermediate images");

		Option width = OptionBuilder.hasArg().withArgName("pixels").withType(Number.class).withDescription("output image width")
				.isRequired(true).create(OPT_WIDTH);
		Option height = OptionBuilder.hasArg().withArgName("pixels").withType(Number.class).withDescription("output image height")
				.isRequired(true).create(OPT_HEIGHT);
		Option quality = OptionBuilder.hasArg().withArgName("quality").withType(Number.class).withDescription("output quality")
				.isRequired(true).create(OPT_QUALITY);

		options.addOption(width);
		options.addOption(height);
		options.addOption(quality);

		return options;
	}

	public static void main(String[] args) {
		CommandLineParser parser = new PosixParser();
		HelpFormatter help = new HelpFormatter();
		CommandLine cmd;
		Options options = getOptions();
		try {
			cmd = parser.parse(options, args);
			if (cmd.hasOption(OPT_HELP)) {
				help.printHelp("j-wildfire-cli -w <pixels> -h <pixels> -q <quality> file.flame", options);
				System.exit(0);
			}
			if (cmd.hasOption(OPT_VERSION)) {
				System.out.println(Tools.APP_TITLE);
				System.out.println(Tools.APP_VERSION);
			}

			// required values
			sWidth = ((Long) cmd.getParsedOptionValue(OPT_WIDTH)).intValue();
			if (sWidth <= 0) {
				System.err.println("Width must be positive.");
			}
			sHeight = ((Long) cmd.getParsedOptionValue(OPT_HEIGHT)).intValue();
			if (sHeight <= 0) {
				System.err.println("Height must be positive.");
			}
			sQuality = ((Long) cmd.getParsedOptionValue(OPT_QUALITY)).intValue();
			if (sQuality <= 0) {
				System.err.println("Quality must be positive.");
			}

			// optional values
			sUseDimensionsInFilename = cmd.hasOption(OPT_USE_DIMENSION);
			sUseQualityInFilename = cmd.hasOption(OPT_USE_QUALITY);
			sHdrOutput = cmd.hasOption(OPT_HDR);
			sHdrIntensityMapOutput = cmd.hasOption(OPT_HDRINTENSITYMAP);
			sOverwriteExisting = cmd.hasOption(OPT_OVERWRITE);
			sLowMemoryMode = cmd.hasOption(OPT_LOWMEM);
			if (sLowMemoryMode) {
				sQuality *= 8;
			}
			sLowMemoryKeep = cmd.hasOption(OPT_LOWMEM_KEEP);
			sVerbose = cmd.hasOption(OPT_VERBOSE);

			for (String filename : cmd.getArgs()) {
				if (!filename.endsWith(Tools.FILEEXT_FLAME)) {
					System.out.println("File does not appear to be a flame.");
				}
				sBatchRenderList.add(filename);
			}

			run();
		} catch (ParseException e) {
			System.err.println("Wrong parameters: " + e.getMessage());
			help.printHelp("j-wildfire-cli -w <pixels> -h <pixels> -q <quality> file.flame", options);
			System.exit(1);
		}
	}
	
	private static String getImageFilename(String filename, int x, int y) {
		if (!filename.endsWith(Tools.FILEEXT_FLAME)) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(filename.substring(0, filename.lastIndexOf('.')));
		if (sUseDimensionsInFilename) {
			sb.append('-');
			sb.append(sWidth);
			sb.append('x');
			sb.append(sHeight);
		}
		if (sUseQualityInFilename) {
			sb.append("-q");
			if (sLowMemoryMode) {
				sb.append(sQuality / 8);
			} else {
				sb.append(sQuality);
			}
		}
		if (x != 8 && y != 8) {
			sb.append('.');
			sb.append(x);
			sb.append(y);
		}
		sb.append('.');
		sb.append(Tools.FILEEXT_PNG);
		return sb.toString();
	}

	public static void run() {
		int elapsed;
		CliUpdater updater = new CliUpdater();
		if (sVerbose) {
			updater.setVolume(2);
		}

		while (sBatchRenderList.size() > 0) {
			String flameFilename = sBatchRenderList.remove(0);
			System.out.println(flameFilename);
			int width, height;
			int iX = 8;
			int iY = 8;
			float zoomScale = 1f;
			if (sLowMemoryMode) {
				// check dimensions
				if (sWidth % 8 != 0 || sHeight % 8 != 0) {
					System.err.println("For low-mem, width and height must each be a multiple of 8.");
					System.exit(1);
				}
				if (flameFilename.endsWith(Tools.FILEEXT_FLAME)) {
					// put entries back in the render list which correspond to each subsection
					sBatchRenderList.remove(flameFilename);
					for (int x = 0; x < 8; x++) {
						for (int y = 0; y < 8; y++) {
							sBatchRenderList.add(flameFilename + String.valueOf(x) + String.valueOf(y));
						}
					}
					sBatchRenderList.add(flameFilename + "88");
					continue;
				} else if (flameFilename.endsWith("88")) {
					concatenateImage(flameFilename.substring(0, flameFilename.length()-2));
					continue;
				} else {
					// otherwise set up all the scaling and names appropriately for an 8x8 subsection render
					width = sWidth / 8;
					height = sHeight / 8;
					zoomScale = 8f;
					iX = flameFilename.charAt(flameFilename.length()-2) - '0';
					iY = flameFilename.charAt(flameFilename.length()-1) - '0';
					flameFilename = flameFilename.substring(0, flameFilename.length()-2);
				}
			} else {
				width = sWidth;
				height = sHeight;
			}
			elapsed = -1;
			RenderInfo info = new RenderInfo(width, height, RenderMode.PRODUCTION);
			info.setRenderHDR(sHdrOutput);
			info.setRenderHDRIntensityMap(sHdrIntensityMapOutput);
			List<Flame> flames = new FlameReader(sPrefs).readFlames(flameFilename);
			Flame flame = flames.get(0);
			if (sLowMemoryMode) {
				flame.setCamZoom(flame.getCamZoom() * zoomScale);
//				flame.setPixelsPerUnit(flame.getPixelsPerUnit() * zoomScale);
				System.out.println("Old centre: " + flame.getCentreX() + ':' + flame.getCentreY());
				System.out.println("WH: " + flame.getWidth() + ':' + flame.getHeight());
				System.out.println("PPU: " + flame.getPixelsPerUnit());
				System.out.println("Zoom: " + flame.getCamZoom());
				double xOffset = flame.getWidth() / (flame.getPixelsPerUnit() * flame.getCamZoom());
				double yOffset = flame.getHeight() / (flame.getPixelsPerUnit() * flame.getCamZoom());
				System.out.println("Offsets: " + xOffset + ':' + yOffset);
				xOffset = flame.getCentreX() + xOffset * (iX - 3.5);
				yOffset = flame.getCentreY() + yOffset * (iY - 3.5);
				System.out.println(iX + ":" + iY + ": " + xOffset + ':' + yOffset);
				flame.setCentreX(xOffset);
				flame.setCentreY(yOffset);
			}
			String primaryFilename = getImageFilename(flameFilename, iX, iY);
			try {
				if (!sOverwriteExisting && new File(primaryFilename).exists()) {
					System.out.println("File exists, skipping: " + primaryFilename);
					elapsed = -1;
				} else {
					double wScl = (double) info.getImageWidth() / (double) flame.getWidth();
					double hScl = (double) info.getImageHeight() / (double) flame.getHeight();
					flame.setPixelsPerUnit((wScl + hScl) * 0.5 * flame.getPixelsPerUnit());
					flame.setWidth(info.getImageWidth());
					flame.setHeight(info.getImageHeight());
					flame.setSampleDensity(sQuality);
					FlameRenderer renderer = new FlameRenderer(flame, sPrefs, flame.isBGTransparency(), false);
					if (sLowMemoryMode) {
						updater.setName(flameFilename + "[" + iX + ":" + iY + "]");
					} else {
						updater.setName(flameFilename);
					}
					renderer.setProgressUpdater(updater);
					long t0 = Calendar.getInstance().getTimeInMillis();
					RenderedFlame res = renderer.renderFlame(info);
					long t1 = Calendar.getInstance().getTimeInMillis();
					elapsed = (int) ((t1 - t0) / 1000);
					new ImageWriter().saveImage(res.getImage(), primaryFilename, true);
					if (res.getHDRImage() != null) {
						new ImageWriter().saveImage(res.getHDRImage(), getImageFilename(flameFilename, iX, iY) + '.' + Tools.FILEEXT_HDR);
					}
					if (res.getHDRIntensityMap() != null) {
						new ImageWriter().saveImage(res.getHDRIntensityMap(), getImageFilename(flameFilename, iX, iY) + ".intensity."
								+ Tools.FILEEXT_HDR);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				System.out.printf("\r[%d]: %s in %dm%ds", sBatchRenderList.size(), primaryFilename, elapsed / 60, elapsed % 60);
			}
		}
	}
	
	private static final void concatenateImage(final String flameFilename) {
		final BufferedImage image = new BufferedImage(sWidth, sHeight, BufferedImage.TYPE_INT_ARGB);
		final File file = new File(getImageFilename(flameFilename, 8, 8));
		File subFile;
		BufferedImage subImage;
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				subFile = new File(getImageFilename(flameFilename, x, y));
				try {
					subImage = ImageIO.read(subFile);
					image.createGraphics().drawImage(subImage, x * (sWidth/8), y * (sHeight/8), null);
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
		try {
			boolean drawn = ImageIO.write(image, "png", file);
			if (!drawn) {
				System.out.println("Problem drawing final image.");
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
}
