package org.monieo.monieoclient;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.security.*;
import java.security.spec.ECGenParameterSpec;

import org.monieo.monieoclient.gui.UI;

public class Monieo {
	
	public static double version;
	public UI ui;
	
	public static void main(String[] args) {
		
		final Properties properties = new Properties();
		try {
			properties.load(Monieo.class.getClassLoader().getResourceAsStream("project.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Version: " + properties.getProperty("version"));
		System.out.println("Please look towards the GUI application.");
		System.out.println("");
		System.out.println("If there are any issues with running the application, all errors will be logged in this window.");
		System.out.println("Please paste the full log of this window when submitting a bug report.");
		
		/*String url = "https://api.github.com/repos/Symphonic3/MonieO/releases/latest";

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
		request.addHeader("content-type", "application/json");
		HttpResponse result = null;
		try {
			result = httpClient.execute(request);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if (result.getStatusLine().getStatusCode() != 200)
			throw new RuntimeException("Could not parse github API");
		String json = null;
		try {
			json = EntityUtils.toString(result.getEntity(), "UTF-8");
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		JSONObject response = new JSONObject(json);
		
		try {
			httpClient.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		double releaseLatest = Double.valueOf(response.getString("tag_name"));
		version = Double.valueOf(properties.getProperty("version"));

		if (releaseLatest > version) {

			String link = "https://github.com/Symphonic3/MonieO/releases/tag/" + releaseLatest;

			int res = JOptionPane.showOptionDialog(null,
					new MessageWithLink(
							"New update available. Press 'OK' to automatically download, or download and use version "
									+ releaseLatest + " from github:" + "\n <a href=\"" + link + "\">Click here</a>"),
					"New update available!", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);

			if (res == 0) {

				JSONObject assetInfo = response.getJSONArray("assets").getJSONObject(0);
				String downloadURL = assetInfo.getString("browser_download_url");
				String fileName = assetInfo.getString("name");

				String jarFol = null;
				try {
					jarFol = new File(Monieo.class.getProtectionDomain().getCodeSource().getLocation().toURI())
							.getParent();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}

				File newJar = new File(jarFol + "/" + fileName);

				try {
					FileUtils.copyURLToFile(new URL(downloadURL), newJar);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

			System.exit(0);

		}
		*/
		
		new Monieo();

	}
	
	public Monieo() {
		
		//moieno
		
		ui = new UI();
		ui.initialize();
		
	}
	
	public static void generateKeyPair () {
		try {
			KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
			gen.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());
			KeyPair pair = gen.generateKeyPair();
			System.out.println(pair.getPublic());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public BigInteger getCurrentHashMinValue() {
		
		//TODO this
		return BigInteger.ONE;
		
	}

	public static byte[] sha256dRaw(String s) {
		
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		byte[] n = digest.digest(s.getBytes(StandardCharsets.UTF_8));
		digest.reset();
		
		return digest.digest(n);
		
	}
	
	public static String sha256d(String s) {
		
		return bytesToHex(sha256dRaw(s));
		
	}
	
	public static String bytesToHex(byte[] hash) {
	    StringBuilder hexString = new StringBuilder(2 * hash.length);
	    for (int i = 0; i < hash.length; i++) {
	        String hex = Integer.toHexString(0xff & hash[i]);
	        if(hex.length() == 1) {
	            hexString.append('0');
	        }
	        hexString.append(hex);
	    }
	    return hexString.toString();
	}
	
}