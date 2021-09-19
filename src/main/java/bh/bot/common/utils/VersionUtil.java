package bh.bot.common.utils;

import static bh.bot.common.Log.debug;
import static bh.bot.common.Log.info;
import static bh.bot.common.Log.isOnDebugMode;
import static bh.bot.common.Log.warn;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

import bh.bot.Main;
import bh.bot.common.exceptions.InvalidDataException;

public class VersionUtil {
	private static SematicVersion appVer = null;

	public static void setCurrentAppVersion(String ver) {
		appVer = new SematicVersion(ver);
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
					debug("VersionUtil::checkForLatestVersion response code %d", responseCode);
					return false;
				}

				StringBuffer response = new StringBuffer();
				try (InputStreamReader isr = new InputStreamReader(httpURLConnection.getInputStream());
						BufferedReader in = new BufferedReader(isr)) {
					String inputLine;

					while ((inputLine = in.readLine()) != null)
						response.append(inputLine);
				}

				String data = response.toString();

				JSONArray array = new JSONArray(data);

				if (array == null || array.length() < 1)
					return false;

				for (int i = 0; i < array.length(); i++) {
					JSONObject obj = array.getJSONObject(i);
					String tagName = obj.getString("tag_name");
					if (tagName == null || !tagName.startsWith("release-"))
						continue;
					
					SematicVersion sematicVersion = new SematicVersion(tagName.substring(8));
					int compare = appVer.compareTo(sematicVersion);
					
					if (compare < 0) {
						info( //
							ColorizeUtil.formatError, // 
							"** NEW UPDATE AVAILABLE ** %s v%s is now available at https://github.com/9-9-9-9/Bit-Heroes-bot/releases", //
							Main.botName, //
							sematicVersion.toString() //
						);
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

	private static class SematicVersion {
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
