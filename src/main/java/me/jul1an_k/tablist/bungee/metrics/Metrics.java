package me.jul1an_k.tablist.bungee.metrics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

/**
 * bStats collects some data for plugin authors.
 *
 * Check out https://bStats.org/ to learn more about bStats!
 */
public class Metrics {
	
	static {
		// Maven's Relocate is clever and changes strings, too. So we have to
		// use this little "trick" ... :D
		final String defaultPackage = new String(new byte[] { 'o', 'r', 'g', '.', 'b', 's', 't', 'a', 't', 's' });
		final String examplePackage = new String(new byte[] { 'y', 'o', 'u', 'r', '.', 'p', 'a', 'c', 'k', 'a', 'g', 'e' });
		// We want to make sure nobody just copy & pastes the example and use
		// the wrong package names
		if(Metrics.class.getPackage().getName().equals(defaultPackage) || Metrics.class.getPackage().getName().equals(examplePackage)) {
			throw new IllegalStateException("bStats Metrics class has not been relocated correctly!");
		}
	}
	
	// The version of this bStats class
	public static final int B_STATS_VERSION = 1;
	
	// The url to which the data is sent
	private static final String URL = "https://bStats.org/submitData/bungeecord";
	
	// The plugin
	private final Plugin plugin;
	
	// Is bStats enabled on this server?
	private boolean enabled;
	
	// The uuid of the server
	private String serverUUID;
	
	// Should failed requests be logged?
	private boolean logFailedRequests = false;
	
	// A list with all known metrics class objects including this one
	private static final List<Object> knownMetricsInstances = new ArrayList<>();
	
	// A list with all custom charts
	private final List<CustomChart> charts = new ArrayList<>();
	
	public Metrics(Plugin plugin) {
		this.plugin = plugin;
		
		try {
			loadConfig();
		} catch(IOException e) {
			// Failed to load configuration
			plugin.getLogger().log(Level.WARNING, "Failed to load bStats config!", e);
			return;
		}
		
		// We are not allowed to send data about this server :(
		if(!enabled) {
			return;
		}
		
		Class<?> usedMetricsClass = getFirstBStatsClass();
		if(usedMetricsClass == null) {
			// Failed to get first metrics class
			return;
		}
		if(usedMetricsClass == getClass()) {
			// We are the first! :)
			linkMetrics(this);
			startSubmitting();
		} else {
			// We aren't the first so we link to the first metrics class
			try {
				usedMetricsClass.getMethod("linkMetrics", Object.class).invoke(null, this);
			} catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				if(logFailedRequests) {
					plugin.getLogger().log(Level.WARNING, "Failed to link to first metrics class " + usedMetricsClass.getName() + "!", e);
				}
			}
		}
	}
	
	/**
	 * Adds a custom chart.
	 *
	 * @param chart
	 *            The chart to add.
	 */
	public void addCustomChart(CustomChart chart) {
		if(chart == null) {
			plugin.getLogger().log(Level.WARNING, "Chart cannot be null");
		}
		charts.add(chart);
	}
	
	/**
	 * Links an other metrics class with this class. This method is called using
	 * Reflection.
	 *
	 * @param metrics
	 *            An object of the metrics class to link.
	 */
	public static void linkMetrics(Object metrics) {
		knownMetricsInstances.add(metrics);
	}
	
	/**
	 * Gets the plugin specific data. This method is called using Reflection.
	 *
	 * @return The plugin specific data.
	 */
	public JsonObject getPluginData() {
		JsonObject data = new JsonObject();
		
		String pluginName = plugin.getDescription().getName();
		String pluginVersion = plugin.getDescription().getVersion();
		
		data.addProperty("pluginName", pluginName);
		data.addProperty("pluginVersion", pluginVersion);
		
		JsonArray customCharts = new JsonArray();
		for(CustomChart customChart : charts) {
			// Add the data of the custom charts
			JsonObject chart = customChart.getRequestJsonObject(plugin.getLogger(), logFailedRequests);
			if(chart == null) { // If the chart is null, we skip it
				continue;
			}
			customCharts.add(chart);
		}
		data.add("customCharts", customCharts);
		
		return data;
	}
	
	private void startSubmitting() {
		// We use a timer cause want to be independent from the server tps
		final Timer timer = new Timer(true);
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				// The data collection (e.g. for custom graphs) is done sync
				// Don't be afraid! The connection to the bStats server is still
				// async, only the stats collection is sync ;)
				TaskScheduler scheduler = plugin.getProxy().getScheduler();
				scheduler.schedule(plugin, () -> submitData(), 0L, TimeUnit.SECONDS);
			}
		}, 1000 * 60 * 2, 1000 * 60 * 30);
		// Submit the data every 30 minutes, first time after 2 minutes to give
		// other plugins enough time to start
		// WARNING: Changing the frequency has no effect but your plugin WILL be
		// blocked/deleted!
		// WARNING: Just don't do it!
	}
	
	/**
	 * Gets the server specific data.
	 *
	 * @return The server specific data.
	 */
	@SuppressWarnings("deprecation")
	private JsonObject getServerData() {
		// Minecraft specific data
		int playerAmount = plugin.getProxy().getOnlineCount();
		playerAmount = playerAmount > 500 ? 500 : playerAmount;
		int onlineMode = plugin.getProxy().getConfig().isOnlineMode() ? 1 : 0;
		String bungeecordVersion = plugin.getProxy().getVersion();
		int managedServers = plugin.getProxy().getServers().size();
		
		// OS/Java specific data
		String javaVersion = System.getProperty("java.version");
		String osName = System.getProperty("os.name");
		String osArch = System.getProperty("os.arch");
		String osVersion = System.getProperty("os.version");
		int coreCount = Runtime.getRuntime().availableProcessors();
		
		JsonObject data = new JsonObject();
		
		data.addProperty("serverUUID", serverUUID);
		
		data.addProperty("playerAmount", playerAmount);
		data.addProperty("managedServers", managedServers);
		data.addProperty("onlineMode", onlineMode);
		data.addProperty("bungeecordVersion", bungeecordVersion);
		
		data.addProperty("javaVersion", javaVersion);
		data.addProperty("osName", osName);
		data.addProperty("osArch", osArch);
		data.addProperty("osVersion", osVersion);
		data.addProperty("coreCount", coreCount);
		
		return data;
	}
	
	/**
	 * Collects the data and sends it afterwards.
	 */
	private void submitData() {
		final JsonObject data = getServerData();
		
		final JsonArray pluginData = new JsonArray();
		// Search for all other bStats Metrics classes to get their plugin data
		for(Object metrics : knownMetricsInstances) {
			try {
				Object plugin = metrics.getClass().getMethod("getPluginData").invoke(metrics);
				if(plugin instanceof JsonObject) {
					pluginData.add((JsonObject) plugin);
				}
			} catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
			}
		}
		
		data.add("plugins", pluginData);
		
		// Create a new thread for the connection to the bStats server
		new Thread(() -> {
			try {
				// Send the data
				sendData(data);
			} catch(Exception e) {
				// Something went wrong! :(
				if(logFailedRequests) {
					plugin.getLogger().log(Level.WARNING, "Could not submit plugin stats!", e);
				}
			}
		}).start();
	}
	
	/**
	 * Loads the bStats configuration.
	 *
	 * @throws IOException
	 *             If something did not work :(
	 */
	private void loadConfig() throws IOException {
		Path configPath = plugin.getDataFolder().toPath().getParent().resolve("bStats");
		configPath.toFile().mkdirs();
		File configFile = new File(configPath.toFile(), "config.yml");
		if(!configFile.exists()) {
			writeFile(configFile, "#bStats collects some data for plugin authors like how many servers are using their plugins.", "#To honor their work, you should not disable it.", "#This has nearly no effect on the server performance!", "#Check out https://bStats.org/ to learn more :)", "enabled: true", "serverUuid: \"" + UUID.randomUUID().toString() + "\"", "logFailedRequests: false");
		}
		
		Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
		
		// Load configuration
		enabled = configuration.getBoolean("enabled", true);
		serverUUID = configuration.getString("serverUuid");
		logFailedRequests = configuration.getBoolean("logFailedRequests", false);
	}
	
	/**
	 * Gets the first bStat Metrics class.
	 *
	 * @return The first bStats metrics class.
	 */
	private Class<?> getFirstBStatsClass() {
		Path configPath = plugin.getDataFolder().toPath().getParent().resolve("bStats");
		configPath.toFile().mkdirs();
		File tempFile = new File(configPath.toFile(), "temp.txt");
		
		try {
			String className = readFile(tempFile);
			if(className != null) {
				try {
					// Let's check if a class with the given name exists.
					return Class.forName(className);
				} catch(ClassNotFoundException ignored) {
				}
			}
			writeFile(tempFile, getClass().getName());
			return getClass();
		} catch(IOException e) {
			if(logFailedRequests) {
				plugin.getLogger().log(Level.WARNING, "Failed to get first bStats class!", e);
			}
			return null;
		}
	}
	
	/**
	 * Reads the first line of the file.
	 *
	 * @param file
	 *            The file to read. Cannot be null.
	 * @return The first line of the file or <code>null</code> if the file does
	 *         not exist or is empty.
	 * @throws IOException
	 *             If something did not work :(
	 */
	private String readFile(File file) throws IOException {
		if(!file.exists()) {
			return null;
		}
		try(FileReader fileReader = new FileReader(file); BufferedReader bufferedReader = new BufferedReader(fileReader)) {
			return bufferedReader.readLine();
		}
	}
	
	/**
	 * Writes a String to a file. It also adds a note for the user,
	 *
	 * @param file
	 *            The file to write to. Cannot be null.
	 * @param lines
	 *            The lines to write.
	 * @throws IOException
	 *             If something did not work :(
	 */
	private void writeFile(File file, String... lines) throws IOException {
		if(!file.exists()) {
			file.createNewFile();
		}
		try(FileWriter fileWriter = new FileWriter(file); BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
			for(String line : lines) {
				bufferedWriter.write(line);
				bufferedWriter.newLine();
			}
		}
	}
	
	/**
	 * Sends the data to the bStats server.
	 *
	 * @param data
	 *            The data to send.
	 * @throws Exception
	 *             If the request failed.
	 */
	private static void sendData(JsonObject data) throws Exception {
		if(data == null) {
			throw new IllegalArgumentException("Data cannot be null");
		}
		
		HttpsURLConnection connection = (HttpsURLConnection) new URL(URL).openConnection();
		
		// Compress the data to save bandwidth
		byte[] compressedData = compress(data.toString());
		
		// Add headers
		connection.setRequestMethod("POST");
		connection.addRequestProperty("Accept", "application/json");
		connection.addRequestProperty("Connection", "close");
		connection.addRequestProperty("Content-Encoding", "gzip"); // We gzip
		                                                           // our
		                                                           // request
		connection.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
		connection.setRequestProperty("Content-Type", "application/json"); // We
		                                                                   // send
		                                                                   // our
		                                                                   // data
		                                                                   // in
		                                                                   // JSON
		                                                                   // format
		connection.setRequestProperty("User-Agent", "MC-Server/" + B_STATS_VERSION);
		
		// Send data
		connection.setDoOutput(true);
		DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
		outputStream.write(compressedData);
		outputStream.flush();
		outputStream.close();
		
		connection.getInputStream().close(); // We don't care about the response
		                                     // - Just send our data :)
	}
	
	/**
	 * Gzips the given String.
	 *
	 * @param str
	 *            The string to gzip.
	 * @return The gzipped String.
	 * @throws IOException
	 *             If the compression failed.
	 */
	private static byte[] compress(final String str) throws IOException {
		if(str == null) {
			return null;
		}
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(outputStream);
		gzip.write(str.getBytes("UTF-8"));
		gzip.close();
		return outputStream.toByteArray();
	}
	
	/**
	 * Represents a custom chart.
	 */
	public static abstract class CustomChart {
		
		// The id of the chart
		protected final String chartId;
		
		/**
		 * Class constructor.
		 *
		 * @param chartId
		 *            The id of the chart.
		 */
		public CustomChart(String chartId) {
			if(chartId == null || chartId.isEmpty()) {
				throw new IllegalArgumentException("ChartId cannot be null or empty!");
			}
			this.chartId = chartId;
		}
		
		protected JsonObject getRequestJsonObject(Logger logger, boolean logFailedRequests) {
			JsonObject chart = new JsonObject();
			chart.addProperty("chartId", chartId);
			try {
				JsonObject data = getChartData();
				if(data == null) {
					// If the data is null we don't send the chart.
					return null;
				}
				chart.add("data", data);
			} catch(Throwable t) {
				if(logFailedRequests) {
					logger.log(Level.WARNING, "Failed to get data for custom chart with id " + chartId, t);
				}
				return null;
			}
			return chart;
		}
		
		protected abstract JsonObject getChartData();
		
	}
	
	/**
	 * Represents a custom simple pie.
	 */
	public static abstract class SimplePie extends CustomChart {
		
		/**
		 * Class constructor.
		 *
		 * @param chartId
		 *            The id of the chart.
		 */
		public SimplePie(String chartId) {
			super(chartId);
		}
		
		/**
		 * Gets the value of the pie.
		 *
		 * @return The value of the pie.
		 */
		public abstract String getValue();
		
		@Override
		protected JsonObject getChartData() {
			JsonObject data = new JsonObject();
			String value = getValue();
			if(value == null || value.isEmpty()) {
				// Null = skip the chart
				return null;
			}
			data.addProperty("value", value);
			return data;
		}
	}
	
	/**
	 * Represents a custom advanced pie.
	 */
	public static abstract class AdvancedPie extends CustomChart {
		
		/**
		 * Class constructor.
		 *
		 * @param chartId
		 *            The id of the chart.
		 */
		public AdvancedPie(String chartId) {
			super(chartId);
		}
		
		/**
		 * Gets the values of the pie.
		 *
		 * @param valueMap
		 *            Just an empty map. The only reason it exists is to make
		 *            your life easier. You don't have to create a map yourself!
		 * @return The values of the pie.
		 */
		public abstract HashMap<String, Integer> getValues(HashMap<String, Integer> valueMap);
		
		@Override
		protected JsonObject getChartData() {
			JsonObject data = new JsonObject();
			JsonObject values = new JsonObject();
			HashMap<String, Integer> map = getValues(new HashMap<>());
			if(map == null || map.isEmpty()) {
				// Null = skip the chart
				return null;
			}
			boolean allSkipped = true;
			for(Map.Entry<String, Integer> entry : map.entrySet()) {
				if(entry.getValue() == 0) {
					continue; // Skip this invalid
				}
				allSkipped = false;
				values.addProperty(entry.getKey(), entry.getValue());
			}
			if(allSkipped) {
				// Null = skip the chart
				return null;
			}
			data.add("values", values);
			return data;
		}
	}
	
	/**
	 * Represents a custom single line chart.
	 */
	public static abstract class SingleLineChart extends CustomChart {
		
		/**
		 * Class constructor.
		 *
		 * @param chartId
		 *            The id of the chart.
		 */
		public SingleLineChart(String chartId) {
			super(chartId);
		}
		
		/**
		 * Gets the value of the chart.
		 *
		 * @return The value of the chart.
		 */
		public abstract int getValue();
		
		@Override
		protected JsonObject getChartData() {
			JsonObject data = new JsonObject();
			int value = getValue();
			if(value == 0) {
				// Null = skip the chart
				return null;
			}
			data.addProperty("value", value);
			return data;
		}
		
	}
	
	/**
	 * Represents a custom multi line chart.
	 */
	public static abstract class MultiLineChart extends CustomChart {
		
		/**
		 * Class constructor.
		 *
		 * @param chartId
		 *            The id of the chart.
		 */
		public MultiLineChart(String chartId) {
			super(chartId);
		}
		
		/**
		 * Gets the values of the chart.
		 *
		 * @param valueMap
		 *            Just an empty map. The only reason it exists is to make
		 *            your life easier. You don't have to create a map yourself!
		 * @return The values of the chart.
		 */
		public abstract HashMap<String, Integer> getValues(HashMap<String, Integer> valueMap);
		
		@Override
		protected JsonObject getChartData() {
			JsonObject data = new JsonObject();
			JsonObject values = new JsonObject();
			HashMap<String, Integer> map = getValues(new HashMap<>());
			if(map == null || map.isEmpty()) {
				// Null = skip the chart
				return null;
			}
			boolean allSkipped = true;
			for(Map.Entry<String, Integer> entry : map.entrySet()) {
				if(entry.getValue() == 0) {
					continue; // Skip this invalid
				}
				allSkipped = false;
				values.addProperty(entry.getKey(), entry.getValue());
			}
			if(allSkipped) {
				// Null = skip the chart
				return null;
			}
			data.add("values", values);
			return data;
		}
		
	}
	
	/**
	 * Represents a custom simple bar chart.
	 */
	public static abstract class SimpleBarChart extends CustomChart {
		
		/**
		 * Class constructor.
		 *
		 * @param chartId
		 *            The id of the chart.
		 */
		public SimpleBarChart(String chartId) {
			super(chartId);
		}
		
		/**
		 * Gets the value of the chart.
		 *
		 * @param valueMap
		 *            Just an empty map. The only reason it exists is to make
		 *            your life easier. You don't have to create a map yourself!
		 * @return The value of the chart.
		 */
		public abstract HashMap<String, Integer> getValues(HashMap<String, Integer> valueMap);
		
		@Override
		protected JsonObject getChartData() {
			JsonObject data = new JsonObject();
			JsonObject values = new JsonObject();
			HashMap<String, Integer> map = getValues(new HashMap<>());
			if(map == null || map.isEmpty()) {
				// Null = skip the chart
				return null;
			}
			for(Map.Entry<String, Integer> entry : map.entrySet()) {
				JsonArray categoryValues = new JsonArray();
				categoryValues.add(new JsonPrimitive(entry.getValue()));
				values.add(entry.getKey(), categoryValues);
			}
			data.add("values", values);
			return data;
		}
		
	}
	
	/**
	 * Represents a custom advanced bar chart.
	 */
	public static abstract class AdvancedBarChart extends CustomChart {
		
		/**
		 * Class constructor.
		 *
		 * @param chartId
		 *            The id of the chart.
		 */
		public AdvancedBarChart(String chartId) {
			super(chartId);
		}
		
		/**
		 * Gets the value of the chart.
		 *
		 * @param valueMap
		 *            Just an empty map. The only reason it exists is to make
		 *            your life easier. You don't have to create a map yourself!
		 * @return The value of the chart.
		 */
		public abstract HashMap<String, int[]> getValues(HashMap<String, int[]> valueMap);
		
		@Override
		protected JsonObject getChartData() {
			JsonObject data = new JsonObject();
			JsonObject values = new JsonObject();
			HashMap<String, int[]> map = getValues(new HashMap<>());
			if(map == null || map.isEmpty()) {
				// Null = skip the chart
				return null;
			}
			boolean allSkipped = true;
			for(Map.Entry<String, int[]> entry : map.entrySet()) {
				if(entry.getValue().length == 0) {
					continue; // Skip this invalid
				}
				allSkipped = false;
				JsonArray categoryValues = new JsonArray();
				for(int categoryValue : entry.getValue()) {
					categoryValues.add(new JsonPrimitive(categoryValue));
				}
				values.add(entry.getKey(), categoryValues);
			}
			if(allSkipped) {
				// Null = skip the chart
				return null;
			}
			data.add("values", values);
			return data;
		}
		
	}
	
	/**
	 * Represents a custom simple map chart.
	 */
	public static abstract class SimpleMapChart extends CustomChart {
		
		/**
		 * Class constructor.
		 *
		 * @param chartId
		 *            The id of the chart.
		 */
		public SimpleMapChart(String chartId) {
			super(chartId);
		}
		
		/**
		 * Gets the value of the chart.
		 *
		 * @return The value of the chart.
		 */
		public abstract Country getValue();
		
		@Override
		protected JsonObject getChartData() {
			JsonObject data = new JsonObject();
			Country value = getValue();
			
			if(value == null) {
				// Null = skip the chart
				return null;
			}
			data.addProperty("value", value.getCountryIsoTag());
			return data;
		}
		
	}
	
	/**
	 * Represents a custom advanced map chart.
	 */
	public static abstract class AdvancedMapChart extends CustomChart {
		
		/**
		 * Class constructor.
		 *
		 * @param chartId
		 *            The id of the chart.
		 */
		public AdvancedMapChart(String chartId) {
			super(chartId);
		}
		
		/**
		 * Gets the value of the chart.
		 *
		 * @param valueMap
		 *            Just an empty map. The only reason it exists is to make
		 *            your life easier. You don't have to create a map yourself!
		 * @return The value of the chart.
		 */
		public abstract HashMap<Country, Integer> getValues(HashMap<Country, Integer> valueMap);
		
		@Override
		protected JsonObject getChartData() {
			JsonObject data = new JsonObject();
			JsonObject values = new JsonObject();
			HashMap<Country, Integer> map = getValues(new HashMap<>());
			if(map == null || map.isEmpty()) {
				// Null = skip the chart
				return null;
			}
			boolean allSkipped = true;
			for(Map.Entry<Country, Integer> entry : map.entrySet()) {
				if(entry.getValue() == 0) {
					continue; // Skip this invalid
				}
				allSkipped = false;
				values.addProperty(entry.getKey().getCountryIsoTag(), entry.getValue());
			}
			if(allSkipped) {
				// Null = skip the chart
				return null;
			}
			data.add("values", values);
			return data;
		}
		
	}
	
	/**
	 * A enum which is used for custom maps.
	 */
	public enum Country {
		
		/**
		 * bStats will use the country of the server.
		 */
		AUTO_DETECT("AUTO", "Auto Detected"),
		
		ANDORRA("AD", "Andorra"), UNITED_ARAB_EMIRATES("AE", "United Arab Emirates"), AFGHANISTAN("AF", "Afghanistan"), ANTIGUA_AND_BARBUDA("AG", "Antigua and Barbuda"), ANGUILLA("AI", "Anguilla"), ALBANIA("AL", "Albania"), ARMENIA("AM", "Armenia"), NETHERLANDS_ANTILLES("AN", "Netherlands Antilles"), ANGOLA("AO", "Angola"), ANTARCTICA("AQ", "Antarctica"), ARGENTINA("AR", "Argentina"), AMERICAN_SAMOA("AS", "American Samoa"), AUSTRIA("AT", "Austria"), AUSTRALIA("AU", "Australia"), ARUBA("AW", "Aruba"), ALAND_ISLANDS("AX", "Aland Islands"), AZERBAIJAN("AZ", "Azerbaijan"), BOSNIA_AND_HERZEGOVINA("BA", "Bosnia and Herzegovina"), BARBADOS("BB", "Barbados"), BANGLADESH("BD", "Bangladesh"), BELGIUM("BE", "Belgium"), BURKINA_FASO("BF", "Burkina Faso"), BULGARIA("BG", "Bulgaria"), BAHRAIN("BH", "Bahrain"), BURUNDI("BI", "Burundi"), BENIN("BJ", "Benin"), SAINT_BARTHELEMY("BL", "Saint Barthelemy"), BERMUDA("BM", "Bermuda"), BRUNEI("BN", "Brunei"), BOLIVIA("BO", "Bolivia"), BONAIRE_SINT_EUSTATIUS_AND_SABA("BQ", "Bonaire, Sint Eustatius and Saba"), BRAZIL("BR", "Brazil"), BAHAMAS("BS", "Bahamas"), BHUTAN("BT", "Bhutan"), BOUVET_ISLAND("BV", "Bouvet Island"), BOTSWANA("BW", "Botswana"), BELARUS("BY", "Belarus"), BELIZE("BZ", "Belize"), CANADA("CA", "Canada"), COCOS_ISLANDS("CC", "Cocos Islands"), THE_DEMOCRATIC_REPUBLIC_OF_CONGO("CD", "The Democratic Republic Of Congo"), CENTRAL_AFRICAN_REPUBLIC("CF", "Central African Republic"), CONGO("CG", "Congo"), SWITZERLAND("CH", "Switzerland"), COTE_D_IVOIRE("CI", "Cote d'Ivoire"), COOK_ISLANDS("CK", "Cook Islands"), CHILE("CL", "Chile"), CAMEROON("CM", "Cameroon"), CHINA("CN", "China"), COLOMBIA("CO", "Colombia"), COSTA_RICA("CR", "Costa Rica"), CUBA("CU", "Cuba"), CAPE_VERDE("CV", "Cape Verde"), CURACAO("CW", "Curacao"), CHRISTMAS_ISLAND("CX", "Christmas Island"), CYPRUS("CY", "Cyprus"), CZECH_REPUBLIC("CZ", "Czech Republic"), GERMANY("DE", "Germany"), DJIBOUTI("DJ", "Djibouti"), DENMARK("DK", "Denmark"), DOMINICA("DM", "Dominica"), DOMINICAN_REPUBLIC("DO", "Dominican Republic"), ALGERIA("DZ", "Algeria"), ECUADOR("EC", "Ecuador"), ESTONIA("EE", "Estonia"), EGYPT("EG", "Egypt"), WESTERN_SAHARA("EH", "Western Sahara"), ERITREA("ER", "Eritrea"), SPAIN("ES", "Spain"), ETHIOPIA("ET", "Ethiopia"), FINLAND("FI", "Finland"), FIJI("FJ", "Fiji"), FALKLAND_ISLANDS("FK", "Falkland Islands"), MICRONESIA("FM", "Micronesia"), FAROE_ISLANDS("FO", "Faroe Islands"), FRANCE("FR", "France"), GABON("GA", "Gabon"), UNITED_KINGDOM("GB", "United Kingdom"), GRENADA("GD", "Grenada"), GEORGIA("GE", "Georgia"), FRENCH_GUIANA("GF", "French Guiana"), GUERNSEY("GG", "Guernsey"), GHANA("GH", "Ghana"), GIBRALTAR("GI", "Gibraltar"), GREENLAND("GL", "Greenland"), GAMBIA("GM", "Gambia"), GUINEA("GN", "Guinea"), GUADELOUPE("GP", "Guadeloupe"), EQUATORIAL_GUINEA("GQ", "Equatorial Guinea"), GREECE("GR", "Greece"), SOUTH_GEORGIA_AND_THE_SOUTH_SANDWICH_ISLANDS("GS", "South Georgia And The South Sandwich Islands"), GUATEMALA("GT", "Guatemala"), GUAM("GU", "Guam"), GUINEA_BISSAU("GW", "Guinea-Bissau"), GUYANA("GY", "Guyana"), HONG_KONG("HK", "Hong Kong"), HEARD_ISLAND_AND_MCDONALD_ISLANDS("HM", "Heard Island And McDonald Islands"), HONDURAS("HN", "Honduras"), CROATIA("HR", "Croatia"), HAITI("HT", "Haiti"), HUNGARY("HU", "Hungary"), INDONESIA("ID", "Indonesia"), IRELAND("IE", "Ireland"), ISRAEL("IL", "Israel"), ISLE_OF_MAN("IM", "Isle Of Man"), INDIA("IN", "India"), BRITISH_INDIAN_OCEAN_TERRITORY("IO", "British Indian Ocean Territory"), IRAQ("IQ", "Iraq"), IRAN("IR", "Iran"), ICELAND("IS", "Iceland"), ITALY("IT", "Italy"), JERSEY("JE", "Jersey"), JAMAICA("JM", "Jamaica"), JORDAN("JO", "Jordan"), JAPAN("JP", "Japan"), KENYA("KE", "Kenya"), KYRGYZSTAN("KG", "Kyrgyzstan"), CAMBODIA("KH", "Cambodia"), KIRIBATI("KI", "Kiribati"), COMOROS("KM", "Comoros"), SAINT_KITTS_AND_NEVIS("KN", "Saint Kitts And Nevis"), NORTH_KOREA("KP", "North Korea"), SOUTH_KOREA("KR", "South Korea"), KUWAIT("KW", "Kuwait"), CAYMAN_ISLANDS("KY", "Cayman Islands"), KAZAKHSTAN("KZ", "Kazakhstan"), LAOS("LA", "Laos"), LEBANON("LB", "Lebanon"), SAINT_LUCIA("LC", "Saint Lucia"), LIECHTENSTEIN("LI", "Liechtenstein"), SRI_LANKA("LK", "Sri Lanka"), LIBERIA("LR", "Liberia"), LESOTHO("LS", "Lesotho"), LITHUANIA("LT", "Lithuania"), LUXEMBOURG("LU", "Luxembourg"), LATVIA("LV", "Latvia"), LIBYA("LY", "Libya"), MOROCCO("MA", "Morocco"), MONACO("MC", "Monaco"), MOLDOVA("MD", "Moldova"), MONTENEGRO("ME", "Montenegro"), SAINT_MARTIN("MF", "Saint Martin"), MADAGASCAR("MG", "Madagascar"), MARSHALL_ISLANDS("MH", "Marshall Islands"), MACEDONIA("MK", "Macedonia"), MALI("ML", "Mali"), MYANMAR("MM", "Myanmar"), MONGOLIA("MN", "Mongolia"), MACAO("MO", "Macao"), NORTHERN_MARIANA_ISLANDS("MP", "Northern Mariana Islands"), MARTINIQUE("MQ", "Martinique"), MAURITANIA("MR", "Mauritania"), MONTSERRAT("MS", "Montserrat"), MALTA("MT", "Malta"), MAURITIUS("MU", "Mauritius"), MALDIVES("MV", "Maldives"), MALAWI("MW", "Malawi"), MEXICO("MX", "Mexico"), MALAYSIA("MY", "Malaysia"), MOZAMBIQUE("MZ", "Mozambique"), NAMIBIA("NA", "Namibia"), NEW_CALEDONIA("NC", "New Caledonia"), NIGER("NE", "Niger"), NORFOLK_ISLAND("NF", "Norfolk Island"), NIGERIA("NG", "Nigeria"), NICARAGUA("NI", "Nicaragua"), NETHERLANDS("NL", "Netherlands"), NORWAY("NO", "Norway"), NEPAL("NP", "Nepal"), NAURU("NR", "Nauru"), NIUE("NU", "Niue"), NEW_ZEALAND("NZ", "New Zealand"), OMAN("OM", "Oman"), PANAMA("PA", "Panama"), PERU("PE", "Peru"), FRENCH_POLYNESIA("PF", "French Polynesia"), PAPUA_NEW_GUINEA("PG", "Papua New Guinea"), PHILIPPINES("PH", "Philippines"), PAKISTAN("PK", "Pakistan"), POLAND("PL", "Poland"), SAINT_PIERRE_AND_MIQUELON("PM", "Saint Pierre And Miquelon"), PITCAIRN("PN", "Pitcairn"), PUERTO_RICO("PR", "Puerto Rico"), PALESTINE("PS", "Palestine"), PORTUGAL("PT", "Portugal"), PALAU("PW", "Palau"), PARAGUAY("PY", "Paraguay"), QATAR("QA", "Qatar"), REUNION("RE", "Reunion"), ROMANIA("RO", "Romania"), SERBIA("RS", "Serbia"), RUSSIA("RU", "Russia"), RWANDA("RW", "Rwanda"), SAUDI_ARABIA("SA", "Saudi Arabia"), SOLOMON_ISLANDS("SB", "Solomon Islands"), SEYCHELLES("SC", "Seychelles"), SUDAN("SD", "Sudan"), SWEDEN("SE", "Sweden"), SINGAPORE("SG", "Singapore"), SAINT_HELENA("SH", "Saint Helena"), SLOVENIA("SI", "Slovenia"), SVALBARD_AND_JAN_MAYEN("SJ", "Svalbard And Jan Mayen"), SLOVAKIA("SK", "Slovakia"), SIERRA_LEONE("SL", "Sierra Leone"), SAN_MARINO("SM", "San Marino"), SENEGAL("SN", "Senegal"), SOMALIA("SO", "Somalia"), SURINAME("SR", "Suriname"), SOUTH_SUDAN("SS", "South Sudan"), SAO_TOME_AND_PRINCIPE("ST", "Sao Tome And Principe"), EL_SALVADOR("SV", "El Salvador"), SINT_MAARTEN_DUTCH_PART("SX", "Sint Maarten (Dutch part)"), SYRIA("SY", "Syria"), SWAZILAND("SZ", "Swaziland"), TURKS_AND_CAICOS_ISLANDS("TC", "Turks And Caicos Islands"), CHAD("TD", "Chad"), FRENCH_SOUTHERN_TERRITORIES("TF", "French Southern Territories"), TOGO("TG", "Togo"), THAILAND("TH", "Thailand"), TAJIKISTAN("TJ", "Tajikistan"), TOKELAU("TK", "Tokelau"), TIMOR_LESTE("TL", "Timor-Leste"), TURKMENISTAN("TM", "Turkmenistan"), TUNISIA("TN", "Tunisia"), TONGA("TO", "Tonga"), TURKEY("TR", "Turkey"), TRINIDAD_AND_TOBAGO("TT", "Trinidad and Tobago"), TUVALU("TV", "Tuvalu"), TAIWAN("TW", "Taiwan"), TANZANIA("TZ", "Tanzania"), UKRAINE("UA", "Ukraine"), UGANDA("UG", "Uganda"), UNITED_STATES_MINOR_OUTLYING_ISLANDS("UM", "United States Minor Outlying Islands"), UNITED_STATES("US", "United States"), URUGUAY("UY", "Uruguay"), UZBEKISTAN("UZ", "Uzbekistan"), VATICAN("VA", "Vatican"), SAINT_VINCENT_AND_THE_GRENADINES("VC", "Saint Vincent And The Grenadines"), VENEZUELA("VE", "Venezuela"), BRITISH_VIRGIN_ISLANDS("VG", "British Virgin Islands"), U_S__VIRGIN_ISLANDS("VI", "U.S. Virgin Islands"), VIETNAM("VN", "Vietnam"), VANUATU("VU", "Vanuatu"), WALLIS_AND_FUTUNA("WF", "Wallis And Futuna"), SAMOA("WS", "Samoa"), YEMEN("YE", "Yemen"), MAYOTTE("YT", "Mayotte"), SOUTH_AFRICA("ZA", "South Africa"), ZAMBIA("ZM", "Zambia"), ZIMBABWE("ZW", "Zimbabwe");
		
		private String isoTag;
		private String name;
		
		Country(String isoTag, String name) {
			this.isoTag = isoTag;
			this.name = name;
		}
		
		/**
		 * Gets the name of the country.
		 *
		 * @return The name of the country.
		 */
		public String getCountryName() {
			return name;
		}
		
		/**
		 * Gets the iso tag of the country.
		 *
		 * @return The iso tag of the country.
		 */
		public String getCountryIsoTag() {
			return isoTag;
		}
		
		/**
		 * Gets a country by it's iso tag.
		 *
		 * @param isoTag
		 *            The iso tag of the county.
		 * @return The country with the given iso tag or <code>null</code> if
		 *         unknown.
		 */
		public static Country byIsoTag(String isoTag) {
			for(Country country : Country.values()) {
				if(country.getCountryIsoTag().equals(isoTag)) {
					return country;
				}
			}
			return null;
		}
		
		/**
		 * Gets a country by a locale.
		 *
		 * @param locale
		 *            The locale.
		 * @return The country from the giben locale or <code>null</code> if
		 *         unknown country or if the locale does not contain a country.
		 */
		public static Country byLocale(Locale locale) {
			return byIsoTag(locale.getCountry());
		}
		
	}
	
}
