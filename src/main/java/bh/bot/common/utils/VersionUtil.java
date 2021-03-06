package bh.bot.common.utils;

import bh.bot.Main;
import bh.bot.common.Configuration;
import bh.bot.common.OS;
import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.exceptions.NotSupportedException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static bh.bot.app.GenMiniClient.readFromInputStream;
import static bh.bot.common.Log.*;
import static bh.bot.common.utils.ColorizeUtil.Cu;
import static bh.bot.common.utils.RegistryUtil.*;

public class VersionUtil {
	private static SematicVersion appVer = null;

	public static SematicVersion setCurrentAppVersion(String ver) {
		return appVer = new SematicVersion(ver);
	}

	public static boolean checkForLatestVersion() {
		if (appVer == null)
			return false;

		try {
			URL url = new URL("https://api.github.com/repos/9-9-9-9/Bit-Heroes-bot/releases");
			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			httpURLConnection.setRequestMethod("GET");
			try {
				int responseCode = httpURLConnection.getResponseCode();

				if (responseCode != 200 && responseCode != 304) {
					dev("VersionUtil::checkForLatestVersion response code %d", responseCode);
					return false;
				}

				final StringBuilder response = new StringBuilder();
				try (InputStreamReader isr = new InputStreamReader(httpURLConnection.getInputStream());
						BufferedReader in = new BufferedReader(isr)) {
					String inputLine;

					while ((inputLine = in.readLine()) != null)
						response.append(inputLine);
				}

				String data = response.toString();

				JSONArray array = new JSONArray(data);

				if (array.length() < 1)
					return false;

				for (int i = 0; i < array.length(); i++) {
					JSONObject obj = array.getJSONObject(i);
					String tagName = obj.getString("tag_name");
					if (tagName == null || !tagName.startsWith("release-"))
						continue;

					SematicVersion sematicVersion = new SematicVersion(tagName.substring(8));
					int compare = appVer.compareTo(sematicVersion);

					if (compare < 0) {
						boolean autoUpdate = !Configuration.Features.disableAutoUpdate && !new File("src").exists();
						String msg =
								autoUpdate
										? String.format( //
										"** NEW UPDATE AVAILABLE ** %s v%s is now available", //
										Main.botName, //
										sematicVersion //
								)
										: String.format( //
										"** NEW UPDATE AVAILABLE ** %s v%s is now available at website: https://github.com/9-9-9-9/Bit-Heroes-bot/releases/latest", //
										Main.botName, //
										sematicVersion //
								);
						info(ColorizeUtil.formatAsk, msg);
						info(ColorizeUtil.formatError, msg);
						info(ColorizeUtil.formatWarning, msg);
						info(ColorizeUtil.formatInfo, msg);

						if (autoUpdate)
							try {
								if (!autoUpdate(sematicVersion))
									info(Cu.i().yellow("** ").red("UPDATE NOTICE").yellow(" ** Attempted to perform Auto-update but ").red("failure").yellow(" please update new version manually from our website  ").cyan("https://github.com/9-9-9-9/Bit-Heroes-bot/releases/latest").reset());
							} catch (Exception ex) {
								dev(ex);
								err("Error while trying to update to newest version of %s", Main.botName);
							}
						return true;
					}
				}

				debug("Your bot version is up to date");
				return true;
			} finally {
				httpURLConnection.disconnect();
			}
		} catch (Exception ex) {
			if (isOnDebugMode())
				ex.printStackTrace();
			return false;
		}
	}

	private static boolean autoUpdate(SematicVersion newerVersion) throws Exception {
		if (appVer == null)
			return false;

		cleanUpAbortedDownloadFiles();

		ThreadUtil.sleep(10_000);
		if (Configuration.Features.isFunctionDisabled("auto-update")) {
			err("Auto-update for this version had been disabled remotely");
			return false;
		}

		final String newBinaryFileName = String.format("download-this-file-%s.zip", newerVersion);
		File fileZip = new File(newBinaryFileName);

		boolean checkGeneratedUpdateScript = true;
		if (!fileZip.exists()) {
			checkGeneratedUpdateScript = false;
			dev("%s is not exists, attempting to download from github repo", newBinaryFileName);

			final String tmpFileName = tmpDownloadFilePrefix + System.currentTimeMillis() + tmpDownloadFileSuffix;
			final File tmpFile = new File(tmpFileName);

			if (tmpFile.exists()) {
				dev("Cancel autoUpdate due to tmp file exists: %s", tmpFileName);
				return false;
			}

			final String urlDownloadBinary = "https://github.com/9-9-9-9/Bit-Heroes-bot/releases/latest/download/download-this-file.zip";
			try (BufferedInputStream in = new BufferedInputStream(new URL(urlDownloadBinary).openStream());
				 FileOutputStream fileOutputStream = new FileOutputStream(tmpFileName)) {
				info("Downloading binary of the new version %s of %s", newerVersion, Main.botName);
				byte[] dataBuffer = new byte[1024];
				int bytesRead;
				while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
					fileOutputStream.write(dataBuffer, 0, bytesRead);
				}

				boolean success = true;
				if (!tmpFile.renameTo(fileZip)) {
					try {
						Files.move(tmpFile.toPath(), fileZip.toPath(), StandardCopyOption.REPLACE_EXISTING);
					} catch (Exception forDevPurposeOnly) {
						dev(forDevPurposeOnly);
						try {
							Files.copy(tmpFile.toPath(), fileZip.toPath(), StandardCopyOption.REPLACE_EXISTING);
						} catch (Exception forDevPurposeOnly2) {
							dev(forDevPurposeOnly2);
							success = false;
						}
					}
				}

				if (!success) {
					err("Failed to rename new downloaded version from %s to %s", tmpFileName, newBinaryFileName);
					return false;
				}

				updateNotice("Downloaded binary for the new version %s", newerVersion);
			} catch (Exception e) {
				dev(e);
				err("Failed to download binary for new version %s", newerVersion);
				return false;
			}
		}

		updateNotice("%s is the compressed zip file of the new version %s of %s", newBinaryFileName, newerVersion, Main.botName);

		final String dstFolder = "auto-update-" + newerVersion;
		// extract
		List<String> extractedFiles = extractZip(fileZip, dstFolder);
		if (extractedFiles == null || extractedFiles.size() < 1)
			return false;

		boolean needGenerateUpdateScript = true;
		if (checkGeneratedUpdateScript) {
			try {
				final File fUpdateScript = new File(autoUpdateScriptFileName);
				if (fUpdateScript.exists()) {
					String marker = Files.readAllLines(fUpdateScript.toPath()).get(1).trim();
					if (marker.equals("rem " + newerVersion) || marker.equals("echo '" + newerVersion + "'"))
						needGenerateUpdateScript = false;
				}
			} catch (Exception forDevPurposeOnly) {
				dev(forDevPurposeOnly);
				dev("Error occurs while checking %s => will continue generate %s", autoUpdateScriptFileName, autoUpdateScriptFileName);
			}
		}

		if (needGenerateUpdateScript) {
			// generate update script
			if (OS.isWin) {
				return generateAutoUpdateScriptOnWindows(fileZip, newerVersion.toString(), extractedFiles);
			} else if (OS.isLinux) {
				return generateAutoUpdateScriptOnLinux(fileZip, newerVersion.toString(), extractedFiles);
			} else if (OS.isMac) {
				return generateAutoUpdateScriptOnMacOS(fileZip, newerVersion.toString(), extractedFiles);
			} else {
				throw new NotSupportedException(String.format("Currently not supported auto update for %s", OS.name));
			}
		} else {
			for (int i = 0; i < RandomUtil.nextInt(2, 10); i++) {
				info(Cu.i().yellow("** ").red("UPDATE NOTICE").yellow(" ** Please run file ").red(autoUpdateScriptFileName).yellow(" to update ").a(Main.botName).a(" to the latest released version ").red(newerVersion.toString()).reset());
			}
		}

		return true;
	}

	private static boolean generateAutoUpdateScriptOnWindows(File fileZip, String version, List<String> extractedFiles) {
		try (
				InputStream p1 = Configuration.class.getResourceAsStream("/templates/auto-update.1.bat");
				InputStream p2 = Configuration.class.getResourceAsStream("/templates/auto-update.2.bat")
		) {
			final StringBuilder sb = new StringBuilder();
			String templateMaterial = readFromInputStream(p2);
			extractedFiles.forEach(f ->
				sb.append("\r\n")
				.append(
					templateMaterial
						.replaceAll("%SRC%", f.replaceAll("\\\\", "\\\\\\\\"))
						.replaceAll("%DST%", new File(f).getName())
				)
			);
			String script = readFromInputStream(p1)
					.replaceAll("%VERSION%", version)
					.replace("%COPY_SCRIPT%", sb.toString())
					.replaceAll("%ZIP_FILE%", fileZip.getName());

			Files.write(Paths.get(autoUpdateScriptFileName), script.getBytes());

			info(Cu.i().yellow("** ").red("UPDATE NOTICE").yellow(" ** Please run file ").cyan(autoUpdateScriptFileName).yellow(" to update ").a(Main.botName).a(" to the latest released version ").cyan(version).reset());
			return true;
		} catch (IOException e) {
			dev(e);
			err("Unable to generate auto update script, please update manually by yourself by going to https://github.com/9-9-9-9/Bit-Heroes-bot/releases/latest");
			return false;
		}
	}

	private static boolean generateAutoUpdateScriptOnLinux(File fileZip, String version, List<String> extractedFiles) {
		try (
				InputStream p1 = Configuration.class.getResourceAsStream("/templates/auto-update.1.sh");
				InputStream p2 = Configuration.class.getResourceAsStream("/templates/auto-update.2.sh")
		) {
			final StringBuilder sb = new StringBuilder();
			String templateMaterial = readFromInputStream(p2);
			extractedFiles.forEach(f -> sb.append('\n').append(templateMaterial.replaceAll("%SRC%", f.replaceAll(" ", "\\ ")).replaceAll("%DST%", new File(f).getName().replaceAll(" ", "\\ "))));
			String script = readFromInputStream(p1)
					.replaceAll("%VERSION%", version)
					.replace("%COPY_SCRIPT%", sb.toString())
					.replaceAll("%ZIP_FILE%", fileZip.getName());

			Files.write(Paths.get(autoUpdateScriptFileName), script.getBytes());

			info(Cu.i().yellow("** ").red("UPDATE NOTICE").yellow(" ** Please run file ").cyan(autoUpdateScriptFileName).yellow(" to update ").a(Main.botName).a(" to the latest released version ").cyan(version).reset());
			return true;
		} catch (IOException e) {
			dev(e);
			err("Unable to generate auto update script, please update manually by yourself by going to https://github.com/9-9-9-9/Bit-Heroes-bot/releases/latest");
			return false;
		}
	}

	private static boolean generateAutoUpdateScriptOnMacOS(File fileZip, String version, List<String> extractedFiles) {
		return generateAutoUpdateScriptOnLinux(fileZip, version, extractedFiles);
	}

	private static List<String> extractZip(File zipFile, String dstFolder) throws Exception {
		if (!zipFile.exists()) {
			err("Zip file could not be found: %s", zipFile.getName());
			return null;
		}

		ZipInputStream zis = null;
		try {
			if (!mkdir(zipFile, "out"))
				return null;
			if (!mkdir(zipFile, "out", dstFolder))
				return null;

			final File fileMarkedAsSuccess = Paths.get("out", dstFolder, ".success").toFile();
			if (fileMarkedAsSuccess.exists()) {
				List<String> extractedFiles = streamOf(Paths.get("out", dstFolder).toFile().listFiles())
						.filter(x -> !x.getName().endsWith(fileMarkedAsSuccess.getName()))
						.map(File::getAbsolutePath)
						.collect(Collectors.toList());
				if (extractedFiles.size() > 0) {
					dev("Successfully extracted before, no need to re-extract");
					extractedFiles.forEach(fn -> dev("File: %s", fn));
					return extractedFiles;
				}
			}

			byte[] buffer = new byte[1024];
			List<String> extractedFiles = new ArrayList<>();
			zis = new ZipInputStream(new FileInputStream(zipFile.getName()));
			ZipEntry zipEntry;
			while ((zipEntry = zis.getNextEntry()) != null) {
				final String fileName = new File(zipEntry.getName()).getName();
				if (!zipEntry.isDirectory()) {
					if (fileName.endsWith(".jar") || fileName.endsWith(".sh") || fileName.endsWith(".bat") || fileName.equals("prepare-mini-chrome-client.txt")) {
						final File targetFile = Paths.get("out", dstFolder, fileName).toFile();
						if (targetFile.exists())
							//noinspection ResultOfMethodCallIgnored
							targetFile.delete();
						try (FileOutputStream fos = new FileOutputStream(targetFile)) {
							int len;
							while ((len = zis.read(buffer)) > 0) {
								fos.write(buffer, 0, len);
							}
							String absolutePath = targetFile.getAbsolutePath();
							dev("Success: %s => %s", fileName, absolutePath);
							extractedFiles.add(absolutePath);
						} catch (Exception ex2) {
							err("Failure while attempting to extract file %s from zip file of the new update", targetFile.getName());
							throw ex2;
						}
					} else {
						dev("Ignore zip entry %s (file extension)", fileName);
					}
				} else {
					dev("Ignore zip entry %s (directory)", fileName);
				}
			}

			if (extractedFiles.size() < 1)
				throw new InvalidDataException("No entry was extracted from %s", zipFile.getName());

			info("Successfully extracted content of %s into folder %s/%s", zipFile.getName(), "out", dstFolder);
			if (!fileMarkedAsSuccess.createNewFile())
				dev("Unable to mark successfully extracted by creating file %s", fileMarkedAsSuccess.getName());
			return extractedFiles;
		} finally {
			try {
				if (zis != null) {
					zis.closeEntry();
					zis.close();
				}
			} catch (Exception forDevPurposeOnly) {
				dev(forDevPurposeOnly);
				dev("Failed to close zip stream");
			}
		}
	}

	private static <T> Stream<T> streamOf(T[] arr) {
		if (arr == null) {
			return Stream.empty();
		}
		return Arrays.stream(arr);
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private static boolean mkdir(File zipFile, @SuppressWarnings("SameParameterValue") String path, String...paths) {
		final File dir = Paths.get(path, paths).toFile();
		if (!dir.exists() || !dir.isDirectory()) {
			if (!dir.mkdir()) {
				err("Unable to create directory to extract %s (%s)", zipFile.getName(), dir.getAbsolutePath());
				return false;
			}
		}
		return true;
	}

	private static final String tmpDownloadFilePrefix = ".download-new-ver.";
	private static final String tmpDownloadFileSuffix = ".zip";
	private static final String autoUpdateScriptFileName = Extensions.scriptFileName("update-bot");

	private static void cleanUpAbortedDownloadFiles() {
		try {
			File curDir = new File(".");
			if (!curDir.exists() || !curDir.isDirectory()) {
				dev("Un-expected! Attempting to get current dir but failure");
				return;
			}

			final long validAfter = System.currentTimeMillis() - 300_000; // valid after 5m ago
				streamOf(curDir.listFiles())
					.filter(x -> x.isFile() && x.getName().endsWith(tmpDownloadFileSuffix) && x.getName().startsWith(tmpDownloadFilePrefix))
					.filter(x -> {
						String[] spl = x.getName().split("\\.");
						if (spl.length != 4)
							return false;
						try {
							return Long.parseLong(spl[2]) >= validAfter;
						} catch (NumberFormatException ignored) {
							return false;
						}
					}).collect(Collectors.toList()).forEach(f -> {
						try {
							//noinspection ResultOfMethodCallIgnored
							f.delete();
						} catch (Exception forDevPurposeOnly) {
							dev(forDevPurposeOnly);
							dev("Error occurs while attempting to remove file %s", f.getName());
						}
			});
		} catch (Exception ex) {
			dev("Failed to cleanUpAbortedDownloadFiles");
			dev(ex);
		}
	}

	private static void updateNotice(String format, Object...args) {
		info(Cu.i().yellow("** ").red("UPDATE NOTICE").yellow(" ** ").ra(String.format(format, args)));
	}

	public static void fetchDisabledFunctions() {
		if (appVer == null) {
			dev("VersionUtil::fetchDisabledFunctions appVer has not been set");
			return;
		}

		try {
			URL url = new URL("https://bh99bot.com/json/reject-versions-2.json");
			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			httpURLConnection.setRequestMethod("GET");
			try {
				int responseCode = httpURLConnection.getResponseCode();

				if (responseCode != 200 && responseCode != 304) {
					dev("VersionUtil::fetchDisabledFunctions response code %d", responseCode);
					return;
				}

				StringBuilder response = new StringBuilder();
				try (InputStreamReader isr = new InputStreamReader(httpURLConnection.getInputStream());
					 BufferedReader in = new BufferedReader(isr)) {
					String inputLine;

					while ((inputLine = in.readLine()) != null)
						response.append(inputLine);
				}

				String data = response.toString();

				JSONObject json = new JSONObject(data);

				JSONArray rejectedVersions = json.getJSONArray("bv"); // by versions

				if (rejectedVersions.length() > 0) {
					for (int i = 0; i < rejectedVersions.length(); i++) {
						try {
							String v = rejectedVersions.getString(i);
							debug("rejectedVersions[%d]=%s", i, v);
							SematicVersion sematicVersion = new SematicVersion(v);

							if (sematicVersion.compareTo(appVer) == 0) {
								try {
									String msg = String.format("You're using %s v%s which is an old & suspended version due to one of the following reasons:", Main.botName, appVer.toString());
									for (int j = 0; j < 20; j++) {
										err(msg);
										info(ColorizeUtil.formatError, "  - Game-itself might have changed some textures and this old version bot didn't get updated");
										info(ColorizeUtil.formatError, "  - This old version might contain critical issues");
										info(ColorizeUtil.formatError, "  - Other reasons");
										info(ColorizeUtil.formatWarning, "Please download latest version on our website 'https://github.com/9-9-9-9/Bit-Heroes-bot/releases/latest'");
									}
								} finally {
									Main.exit(Main.EXIT_CODE_VERSION_IS_REJECTED);
								}
								break;
							}

						} catch (Exception ex2) {
							dev("Can not parse version at index %d", i);
						}
					}
				}

				JSONObject rejectedFunctionsByVersion = json.getJSONObject("bf");
				if (rejectedFunctionsByVersion != null && rejectedFunctionsByVersion.has(appVer.toString())) {
					JSONArray rejectedFunctionsOfThisVersion = rejectedFunctionsByVersion.getJSONArray(appVer.toString());
					for (int i = 0; i < rejectedFunctionsOfThisVersion.length(); i++)
						Configuration.Features.disableFunction(rejectedFunctionsOfThisVersion.getString(i));
				}
			} finally {
				httpURLConnection.disconnect();
			}
		} catch (Throwable t) {
			dev(t);
		}
	}

	public static void quitIfCurrentVersionIsRejected(String appCode) {
		if (appVer == null) {
			dev("VersionUtil::quitIfCurrentVersionIsRejected appVer has not been set");
			return;
		}

		if (Configuration.Features.isFunctionDisabled(appCode)) {
			for (int j = 0; j < 20; j++) {
				err(String.format("Function `%s` was suspended in this version %s due to one of the following reasons:", appCode, appVer.toString()));
				info(ColorizeUtil.formatError, "  - Game-itself might have changed some textures and this old version bot didn't get updated");
				info(ColorizeUtil.formatError, "  - `%s` in old version might contain critical issues", appCode);
				info(ColorizeUtil.formatError, "  - Other reasons");
				info(ColorizeUtil.formatWarning, "Please download latest version on our website 'https://github.com/9-9-9-9/Bit-Heroes-bot/releases/latest'");
			}
			Main.exit(Main.EXIT_CODE_VERSION_IS_REJECTED);
		}
	}

	public static void saveBotInfo(SematicVersion currentAppVersion) {
		try {
			if (OS.isWin)
				saveBotInfoOnWindows(currentAppVersion);
			if (OS.isLinux)
				saveBotInfoOnLinux(currentAppVersion);
			if (OS.isMac)
				saveBotInfoOnMacOS(currentAppVersion);
		} catch (Throwable t) {
			dev(t);
		}
	}

	private static void saveBotInfoOnLinux(SematicVersion currentAppVersion) throws Exception {
		String homeDir = System.getProperty("user.home");
		if (StringUtil.isBlank(homeDir))
			homeDir = new File("~").getAbsolutePath();
		File fHomeDir = new File(homeDir);
		if (!fHomeDir.exists() || !fHomeDir.isDirectory()) {
			debug("Not a home dir");
			return;
		}

		String wrkDir = System.getProperty("user.dir");

		File f99bot = Paths.get(fHomeDir.getAbsolutePath(), ".99bot").toFile();

		boolean writeDefault = false;

		final String keyVer = "curVer";
		final String keyDir = "curDir";
		try {
			if (f99bot.exists()) {
				JSONObject json = new JSONObject(new String(Files.readAllBytes(f99bot.toPath())));
				String curVer, curDir;
				boolean updateVer;
				try {
					updateVer = !json.has(keyVer) || StringUtil.isBlank(curVer = json.getString(keyVer)) || new SematicVersion(curVer).compareTo(currentAppVersion) < 0;
				} catch (Exception e) {
					dev(e);
					updateVer = true;
				}

				boolean updateDir;
				try {
					updateDir = !json.has(keyDir) || StringUtil.isBlank(curDir = json.getString(keyDir)) || !curDir.equals(wrkDir) || !new File(curDir).exists() || !new File(curDir).isDirectory();
				} catch (Exception e) {
					dev(e);
					updateDir = true;
				}

				writeDefault = updateVer || updateDir;
				debug("writeDefault %b", writeDefault);
			} else {
				writeDefault = true;
			}
		} catch (Exception ex) {
			writeDefault = true;
			throw ex;
		} finally {
			if (writeDefault) {
				JSONObject json = new JSONObject();
				json.put(keyVer, currentAppVersion.toString());
				json.put(keyDir, wrkDir);
				Files.write(f99bot.toPath(), json.toString().getBytes());
			}
		}
	}

	private static void saveBotInfoOnMacOS(SematicVersion currentAppVersion) throws Exception {
		saveBotInfoOnLinux(currentAppVersion);
	}
	
	private static void saveBotInfoOnWindows(SematicVersion currentAppVersion) {
		String curVer = readRegistryString(regValueVer);
		String curDir = readRegistryString(regValueDir);
		String wrkDir = System.getProperty("user.dir");
		debug("VersionUtil::saveBotInfoOnWindows.curVer %s", curVer);
		debug("VersionUtil::saveBotInfoOnWindows.curDir %s", curDir);
		debug("VersionUtil::saveBotInfoOnWindows.wrkDir %s", wrkDir);
		
		boolean updateVer;
		try {
			updateVer = StringUtil.isBlank(curVer) || new SematicVersion(curVer).compareTo(currentAppVersion) < 0;
		} catch (Exception e) {
			dev(e);
			updateVer = true;
		}
		
		boolean updateDir;
		try {
			updateDir = StringUtil.isBlank(curDir) || !curDir.equals(wrkDir) || !new File(curDir).exists() || !new File(curDir).isDirectory();
			debug("VersionUtil::saveBotInfoOnWindows.updateDir %b (%b-%b-%b-%b)",
					updateDir,
					StringUtil.isBlank(curDir),
					!Objects.equals(curDir, wrkDir),
					curDir == null || !new File(curDir).exists(),
					curDir == null || !new File(curDir).isDirectory()
			);
		} catch (Exception e) {
			dev(e);
			updateDir = true;
		}
		
		if (updateVer || updateDir) {
			debug("VersionUtil::saveBotInfoOnWindows -> Rewrite");
			prepareRegKey();
			createValue(regValueVer, currentAppVersion.toString());
			createValue(regValueDir, wrkDir);
		} else {
			debug("VersionUtil::saveBotInfoOnWindows -> No need to update version and dir of bot");
		}
	}

	public static class SematicVersion {
		public final byte major;
		public final byte minor;
		public final byte patch;

		public SematicVersion(String version) {
			String[] spl = version.split("\\.");
			if (spl.length != 3)
				throw new InvalidDataException("%s it not a valid version format", version);
			this.major = Byte.parseByte(spl[0]);
			this.minor = Byte.parseByte(spl[1]);
			this.patch = Byte.parseByte(spl[2]);
		}

		public int compareTo(SematicVersion that) {
			return this.hashCode() - that.hashCode();
		}

		@Override
		public int hashCode() {
			return this.major * 255 * 255 + this.minor * 255 + this.patch;
		}

		@Override
		public String toString() {
			return String.format("%d.%d.%d", this.major, this.minor, this.patch);
		}
	}
}
