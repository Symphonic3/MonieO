package org.monieo.monieoclient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.monieo.monieoclient.gui.UI;
import org.monieo.monieoclient.wallet.Wallet;

public class Monieo {
	
	public static final int PORT = 21093;
	public static final String MAGIC_NUMBERS = "MEOPROTOCOL1";
	public static double VERSION;
	
	public static int MAX_OUTGOING_CONNECTIONS = 10;
	public static int MAX_INCOMING_CONNECTIONS = 10;
	
	public static Monieo INSTANCE;
	
	public static void main(String[] args) {
		
		final Properties properties = new Properties();
		try {
			properties.load(Monieo.class.getClassLoader().getResourceAsStream("project.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		VERSION = Double.valueOf(properties.getProperty("version"));
		
		System.out.println("Version: " + VERSION);
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
	
	UI ui;
	
	File workingFolder;
	File nodesFile;
	File walletsFolder; 
	
	List<Socket> connections = new ArrayList<Socket>();
	List<String> knownNodes = new ArrayList<String>();
	
	public List<Wallet> myWallets = new ArrayList<Wallet>();
	
	public Monieo() {
		
		INSTANCE = this;
		
		ui = new UI();
		ui.initialize();
		
		String workingDirectory;
		String OS = (System.getProperty("os.name")).toUpperCase();

		if (OS.contains("WIN"))
		{
		    workingDirectory = System.getenv("AppData");
		}
		else
		{
		    workingDirectory = System.getProperty("user.home");
		    workingDirectory += "/Library/Application Support";
		}
		
		workingDirectory += "/MonieO";
		
		workingFolder = new File(workingDirectory);
		workingFolder.mkdirs();
		
		nodesFile = new File(workingFolder.getPath() + "/clients.dat");
		
		try {
			nodesFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try (Scanner c = new Scanner(nodesFile)) {
			
			while (c.hasNextLine()) {
				
				knownNodes.add(c.nextLine());
				
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		walletsFolder.mkdir();
		
		for (File f : walletsFolder.listFiles()) {
			
			if (f.isDirectory()) {
				
				String[] fn = f.getName().split(".");
				String name = fn[fn.length-2];
				
				File pub = new File(f.getPath() + "/public.key");
				File priv = new File(f.getPath() + "/private.key");
				
				KeyPair kp = deserializeKeyPair(readFileData(pub), readFileData(priv));
				
				myWallets.add(new Wallet(name, kp));
				
			}
			
		}
		
	}
	
	public Wallet getWalletByNick(String nick) {
		
		for (Wallet w : myWallets) {
			
			if (w.nickname.equalsIgnoreCase(nick)) {
				
				return w;
				
			}
			
		}
		
		return null;
		
	}
	
	public String createWallet(String nick) {
		
		if (getWalletByNick(nick) != null) return "Wallet " + nick + " already exists!";
		
		Wallet w = Wallet.newWallet(nick);
		
		File f = new File(walletsFolder.getPath() + "/" + nick);
		f.mkdir();
		
		File pub = new File(f.getPath() + "/public.key");
		File priv = new File(f.getPath() + "/private.key");
		
		KeyPair kp = w.getKeyPair();
		
		try (FileWriter fw = new FileWriter(pub)) {
			
			fw.append(serializeKeyPairPublic(kp));
			
		} catch (IOException e) {
			
			e.printStackTrace();
			return "Wallet creation error!";
			
		}
		
		try (FileWriter fw = new FileWriter(priv)) {
			
			fw.append(serializeKeyPairPrivate(kp));
			
		} catch (IOException e) {
			
			e.printStackTrace();
			return "Wallet creation error!";
			
		}
		
		return "Wallet " + nick + " created!";
		
	}
	
	public void deleteWallet(Wallet wallet) {
		
		//TODO this
		
	}
	
	public void attemptRememberNode(String node) {
		
		if (!knownNodes.contains(node)) {
			
			knownNodes.add(node);
			
			try (FileWriter fw = new FileWriter(nodesFile)) {
				
				fw.append(node);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            return gen.generateKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String serializeKeyPairPublic(KeyPair k) {
    	
    	return Base64.getEncoder().encodeToString(k.getPublic().getEncoded());
    	
    }
    
    public static String serializeKeyPairPrivate(KeyPair k) {
    	
    	return Base64.getEncoder().encodeToString(k.getPrivate().getEncoded());
    	
    }

    public static KeyPair deserializeKeyPair(String pub, String priv) {
    	
    	try {
    		
			KeyFactory kf = KeyFactory.getInstance("RSA");
			
			PublicKey pka = kf.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(pub)));
		    PrivateKey ska = kf.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(priv)));
		        
		    return new KeyPair(pka, ska);
            
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
    	
    	return null;
    	
    }
    
    public static String readFileData(File f) {
    	
    	try (Scanner c = new Scanner(f)) {
    		
    		String ret = "";
    		
    		while (c.hasNext()) {
    			
    			ret += c.next();
    			
    		}
    		
    		return ret;
    		
    	} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	
    	return null;
    	
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
	
	public static boolean assertMagicNumbers(String s) {
		
		return s.equals(MAGIC_NUMBERS);
		
	}
	
}