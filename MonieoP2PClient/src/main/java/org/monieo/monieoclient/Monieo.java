package org.monieo.monieoclient;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.SplashScreen;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.json.JSONObject;
import org.monieo.monieoclient.blockchain.AbstractTransaction;
import org.monieo.monieoclient.blockchain.Block;
import org.monieo.monieoclient.blockchain.BlockHeader;
import org.monieo.monieoclient.blockchain.CoinbaseTransaction;
import org.monieo.monieoclient.blockchain.Transaction;
import org.monieo.monieoclient.gui.MessageWithLink;
import org.monieo.monieoclient.gui.Settings;
import org.monieo.monieoclient.gui.UI;
import org.monieo.monieoclient.mining.AbstractMiner;
import org.monieo.monieoclient.mining.DefaultMinerImpl;
import org.monieo.monieoclient.mining.TxPool;
import org.monieo.monieoclient.networking.ConnectionHandler;
import org.monieo.monieoclient.networking.NetAddressManager;
import org.monieo.monieoclient.networking.NetworkPacket;
import org.monieo.monieoclient.networking.NetworkPacket.NetworkPacketType;
import org.monieo.monieoclient.networking.Node;
import org.monieo.monieoclient.randomx.RandomXManager;
import org.monieo.monieoclient.wallet.Wallet;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class Monieo {
	
	public static final int PORT = 21093;
	public static final String MAGIC_NUMBERS = "MEOPROTOCOL";
	public static final String PROTOCOL_VERSION = "1.0";
	
	public static String VERSION;
	public static String NEXT_AVAILABLE = null;
	public static boolean UPDATE = false;
	
	public static int MAX_OUTGOING_CONNECTIONS = 10;
	public static int MAX_CONNECTIONS = 110;
	
	public static int CONFIRMATIONS_IGNORE = 100;
	public static int CONFIRMATIONS_BLOCK_SENSITIVE = 60;
	
	public static Monieo INSTANCE;
	
	public static final BigInteger MAXIMUM_HASH_VALUE = BigInteger.ONE.shiftLeft(256).subtract(BigInteger.ONE);
	
	public static final int SYSTEM_LOGICAL_THREADS = Runtime.getRuntime().availableProcessors();
	
	public static String GENESIS_HASH;
	
	public static SplashScreen ss;
	public static Graphics2D ssg;
	
	public static void main(String[] args) {
		
		ss = SplashScreen.getSplashScreen();
		ssg = ss.createGraphics();
		
		final Properties properties = new Properties();
		try {
			properties.load(Monieo.class.getClassLoader().getResourceAsStream("project.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		VERSION = properties.getProperty("version");
		
		System.out.println("Version: " + VERSION);
		System.out.println("Please look towards the GUI application.");
		System.out.println("");
		System.out.println("If there are any issues with running the application, all errors will be logged in this window.");
		System.out.println("Please paste the full log of this window when submitting a bug report.");
		System.out.println("Charset: " + Charset.defaultCharset().name());
		
		ssg.setComposite(AlphaComposite.Clear);
		ssg.fillRect(30,310,210,20);
		ssg.setPaintMode();
		ssg.setColor(Color.BLACK);
		ssg.setFont(new Font("Dialog", Font.PLAIN, 15));
		ssg.drawString("v" + String.valueOf(VERSION), 30, 310);
		
		ss.update();

		String result = getHTML("https://api.github.com/repos/Symphonic3/MonieO/releases/latest");
	    
		if (result == null) throw new RuntimeException("Could not parse github API");

		JSONObject response = new JSONObject(result);
		
		NEXT_AVAILABLE = response.getString("tag_name");

		ComparableVersion releaseLatest = new ComparableVersion(NEXT_AVAILABLE);
		ComparableVersion versionActual = new ComparableVersion(VERSION);

		if (releaseLatest.compareTo(versionActual) == 1) {
			
			UPDATE = true;

			String link = "https://github.com/Symphonic3/MonieO/releases/tag/" + releaseLatest;

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
			
			if (newJar.exists()) {
				
				try {
					Runtime.getRuntime().exec("java -jar " + "\"" + newJar.getPath() + "\"" + " " + String.join(" ", args));
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			} else {

				int res = JOptionPane.showOptionDialog(null,
						new MessageWithLink(
								"New update available. Press 'OK' to automatically download, or download and use version "
										+ releaseLatest + " from github:" + "\n <a href=#\"" + link + "\"></a>"),
						"New update available!", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);

				if (res == 0) {

					try {
						Files.copy(new URL(downloadURL).openStream(), newJar.toPath());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					
					try {
						Runtime.getRuntime().exec("java -jar " + "\"" + newJar.getPath() + "\"" + " " + String.join(" ", args));
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
				
			}
			
			System.exit(0);

		}
		
		new Monieo(arrayContains(args, "--skipconnect"));

	}
	
	public static <T> boolean arrayContains(T[] array, T item) {
		
		for (int i = 0; i < array.length; i++) {
			
			if (array[i].equals(item)) return true;
			
		}
		
		return false;
		
	}
	
	public static String getHTML(String url) {
		
		try {
		
			StringBuilder result = new StringBuilder();
			URL uri = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
			conn.setRequestMethod("GET");
			    
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
				 
				for (String line; (line = reader.readLine()) != null;) {
			        	  
					result.append(line + "\n");
			              
			    }
				 
			}
			
			return result.toString();
			
		} catch (IOException e) {
			
			e.printStackTrace();
			return null;
			
		}
		
	}
	
	public UI ui;
	
	public File workingFolder;
	public File walletsFolder; 
	public File blocksFolder; 
	public File blocksExtraDataFolder;
	public File blockMetadataFolder; 
	
	public File feeEstimate;
	
	public File blkhighest;
	
	public File txlistFolder;
	public File trackTx;
	
	public File settingsFile;
	public Settings settings;
	Yaml yamlSettings;
	
	public void saveSettings() {
		
		try (FileWriter fw = new FileWriter(settingsFile)) {
			
			fw.write(yamlSettings.dump(settings));
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	List<Socket> connections = new ArrayList<Socket>();
	
	public List<Wallet> myWallets = new ArrayList<Wallet>();
	
	public Vector<Node> nodes = new Vector<Node>();
	
	public TxPool txp;
	
	public NetAddressManager nam;
	
	public ConnectionHandler ch;
	
	public AbstractMiner miner;
	
	public Monieo(boolean l) {
		
		INSTANCE = this;
		
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
		
		RandomXManager.setRandomX(RandomXManager.getFlags());
		
		System.out.println("Test    : " + randomx("tast"));
		System.out.println("Expected: " + "6b08ba542fe59ffad744d12866fde82ba2a1397cd5408e3531ec37cd5f1011c1");
		
		GENESIS_HASH = genesis().hash();
		
		settingsFile = new File(workingFolder.getPath() + "/settings.yml");
		try {
			settingsFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		trackTx = new File(workingFolder.getPath() + "/tracktx.dat");
		try {
			trackTx.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		yamlSettings = new Yaml(new Constructor(Settings.class));
		
		try (InputStream in = new FileInputStream(settingsFile)) {

			settings = yamlSettings.load(in);
			if (settings == null) settings = new Settings();

		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		nam = new NetAddressManager();
		
		walletsFolder = new File(workingFolder.getPath() + "/wallets");
		walletsFolder.mkdir();
		
		for (File f : walletsFolder.listFiles()) {
			
			if (f.isDirectory()) {
				
				File pub = new File(f.getPath() + "/public.key");
				File priv = new File(f.getPath() + "/private.key");
				
				if (priv.exists()) {
					
					PrivateKey privKey = deserializePrivateKey(readFileData(priv));
					
					if (!pub.exists()) {
						
						try {
							
							pub.createNewFile();
							
							try (FileWriter fw = new FileWriter(pub, false)) {
								
								fw.append(base64(generatePublic(privKey).getEncoded()));
								
							}
							
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
					
					KeyPair kp = new KeyPair(deserializePublicKey(readFileData(pub)), privKey);
					
					myWallets.add(new Wallet(f.getName(), kp));
					
				} else {

					myWallets.add(new Wallet(f.getName(), deserializePublicKey(readFileData(pub))));
					
				}
				
			}
			
		}
		
		miner = new DefaultMinerImpl();
		
		blocksFolder = new File(workingFolder.getPath() + "/blocks");
		blocksFolder.mkdir();
		
		blocksExtraDataFolder = new File(workingFolder.getPath() + "/blockextra");
		blocksExtraDataFolder.mkdir();
		
		blockMetadataFolder = new File(workingFolder.getPath() + "/blockmeta");
		blockMetadataFolder.mkdir();
		
		txlistFolder = new File(workingFolder.getPath() + "/txlist");
		txlistFolder.mkdir();
		
		feeEstimate = new File(workingFolder.getPath() + "/feeestim.dat");
		
		if (!feeEstimate.exists()) {
			
			try {
				feeEstimate.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		blkhighest = new File(workingFolder.getPath() + "/.blkhighest");

		if (!blkhighest.exists()) {
			
			try {
				blkhighest.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		handleBlock(genesis());
		
		Block b = getHighestBlock();
		
		if (b == null || !b.validate() || !b.isReady() || !b.hasMetadata()) {
			
			String warning = "Detected potential block data corruption or system clock desync.\n"
					+ "Please backup the ENTIRE MonieO/wallets folder as to not lose your MonieO due to further corruption.\n"
					+ "MonieO will not start until the corrupted blocks are removed or the issue is manually corrected.";
			System.out.println(warning);
			
			JOptionPane.showMessageDialog(null, warning, "Error", JOptionPane.ERROR_MESSAGE);
			
			System.exit(0);
			
		}
		
		txp = new TxPool(txlistFolder);
		
		ch = new ConnectionHandler();
		new Thread(ch).start();
		
		//main timer
		new Timer().schedule(new TimerTask() {
			
			int at = 0;
			
			@Override
			public void run() {
				
				//attempt to load blockchain before starting UI application
				if (ss != null) {
					
					if (desyncAmount() == -1 || at >= (l ? 0 : 1)) { //try once //TODO reinstate the 2nd number to 1
						
						ss.close();
						ss = null;

						ui = new UI();
						System.err.println("Initializing UI...");
						ui.initialize();
						System.err.println("...UI Initialized!");
						
					}
					
					at++;
					
				}

				Block b = getHighestBlock();
				Block g = genesis();
				
				for (int i = 0; i < (10-1); i++) { //hardcoded to 10 now because we removed CONFIRMATIONS
					
					if (b == null || b.equals(g)) {
						
						break;
						
					}
					
				}

				Block nb = b.getPrevious();
				
				String hash = (nb == null ? GENESIS_HASH : b.header.preHash);
				
				for (int i = 0; i < nodes.size(); i++) {

					Node n = nodes.get(i);
					
					if (System.currentTimeMillis()-n.lastValidPacketTime > Node.MIN_RESPONSE_TIME && !n.doNotDisconnectPeer) {
						
						System.out.println("Disconnected peacefully");
						n.disconnect(true); //disconnecting via refusing to respond is the only way to peacefully disconnect
						i--;
						continue;
						
					}
					
					if (!n.localAcknowledgedRemote || !n.remoteAcknowledgedLocal) continue;
					
					//functions as a keepalive, for now
					if (!n.doNotDisconnectPeer) n.queueNetworkPacket(new NetworkPacket(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkPacketType.KEEPALIVE, hash));
					
				}
				
				if (nodes.size() < MAX_OUTGOING_CONNECTIONS) {
					
					String s = nam.getPossibleAddressOutbound();
					
					for (Node n : nodes) {
						
						if (n.getAdress().equals(s)) return;
						
					}
					
					System.out.println("attempting to connect to a node!");
					if (ch.connect(s)) {
						
						//this is not put here because we should wait for proper handshaking before doing this
						//nam.successfullyConnectedOrDisconnected(s);
						
					} else {
						
						nam.couldNotConnectToNode(s);
						
					}
					
				}
				
			}
			
		}, 0, 2500);
		
	}
	
	public long desyncAmount() {
		
		//TODO work on this check
		
		if (getHighestBlock().header.timestamp + 7200000 > getNetAdjustedTime()) { //2h
			
			return -1;
			
		} else {
			
			return (getNetAdjustedTime()-getHighestBlock().header.timestamp)/60000;
			
		}
		
	}
	
	public void setHighestBlock(Block b) {
		
		FileWriter fw;
		
		try {
			
			fw = new FileWriter(blkhighest, false);
			fw.write(b.hash());
			fw.flush();
			fw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	//shortcut method to save having to get the block and hash it
	public String getHighestBlockHash() {
		
		return readFileData(blkhighest);
		
	}
	
	public Block getHighestBlock() {
		
		String d = readFileData(new File(blocksFolder.getPath() + "/" + readFileData(blkhighest) + ".blk"));
		
		if (d == null) return null;
		
		return Block.deserialize(d);
		
	}

	public long getNetAdjustedTime() {
		
		long o = 0;
		
		if (nodes.size() >= 5) {
			
			for (Node n : nodes) {
				
				o += n.getTimeOffset();
				
			}
			
			if (Math.abs(o) > 3600000) { //1 hour
				
				o = 3600000;
				
			}
			
			return System.currentTimeMillis()+o;
			
		} else return System.currentTimeMillis();
		
	}
	
	public void handleBlock(Block b) {

		if (b == null || !b.validate()) throw new IllegalStateException("Attempted to handle an invalid block!");
		
		//TODO block checkpoints
		//if (getHighestBlock() != null && b.header.height+(CONFIRMATIONS_BLOCK_SENSITIVE*4) < getHighestBlock().header.height) return;
		
		String blockname = b.hash();
		
		File blockfile = new File(blocksFolder.getPath() + "/" + blockname + ".blk");
		
		if (!blockfile.exists()) {
			
			try {
				blockfile.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			try (FileWriter fw = new FileWriter(blockfile)) {
				
				fw.write(b.serialize());
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (b.isReady()) {
				
				System.out.println("generating metadata " + b.hash());
				b.generateMetadata();
				
				if (getHighestBlock() == null || b.getChainWork().compareTo(getHighestBlock().getChainWork()) == 1) {
					
					setHighestBlock(b);
					
					Block curr = b;
					
					BigDecimal lowestT = new BigDecimal(Monieo.MAXIMUM_HASH_VALUE); //big number
					BigDecimal highestT = BigDecimal.ZERO;
					BigDecimal avT = BigDecimal.ZERO;
					int txCount = 0;
					
					for (int i = 0; i < 60; i++) {
						
						if (curr.transactions.length == 1) {
							
							txCount++;
							lowestT = BigDecimal.ZERO;
							avT = BigDecimal.ZERO;
							break;
							
						} else for (AbstractTransaction t : curr.transactions) {
							
							if (t instanceof Transaction) {
								
								Transaction tr = (Transaction)t;
								
								if (tr.d.fee.compareTo(lowestT) == -1) lowestT = tr.d.fee;
								if (tr.d.fee.compareTo(highestT) == 1) highestT = tr.d.fee;
								txCount++;
								avT = avT.add(tr.d.fee);
								
							}
							
						}
						
						curr = curr.getPrevious();
						
						if (curr == null) break;
						
					}
					
					avT = avT.divide(BigDecimal.valueOf(txCount), 8, RoundingMode.HALF_UP);
					
					try (FileWriter fw = new FileWriter(feeEstimate, false)) {
						
						fw.append(lowestT.toPlainString() + "\n" + avT.toPlainString() + "\n" + highestT.toPlainString());
						
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					if (ui != null && ui.fullInit) ui.refresh(false, false);
					
				}
				
				Node.propagateAll(new NetworkPacket(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkPacketType.SEND_BLOCK, b.serialize()));
				
			}
			
		}
		
	}
	
	public BigDecimal getEstimatedLowestFee() {
		
		return getFeeData(0);
		
	}
	
	public BigDecimal getEstimatedAverageFee() {
		
		return getFeeData(1);
		
	}
	
	public BigDecimal getEstimatedHighestFee() {
		
		return getFeeData(2);
		
	}
	
	private BigDecimal getFeeData(int i) {
		
		String d = readFileData(feeEstimate);
		if (d == null) return BigDecimal.ZERO;
		
		String[] fs = d.split("\n");
		
		if (fs.length < 3) return BigDecimal.ZERO;
		
		return new BigDecimal(fs[i]);
		
	}
	
	public static Block genesis() {
		
		CoinbaseTransaction ct = new CoinbaseTransaction(MAGIC_NUMBERS, PROTOCOL_VERSION, new String("faa69b10d8d1a8c427e71839a0141e4e83a99c443b5c16e7f7e1cdaca96054de"), new BigDecimal(0));
		
		String bh = sha256d(ct.serialize());
		
		return new Block(new BlockHeader(MAGIC_NUMBERS, PROTOCOL_VERSION, "0", sha256d(bh + bh), 0, BigInteger.ZERO, 1, 0, MAXIMUM_HASH_VALUE), ct);
		
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
			
			fw.append(base64(kp.getPublic().getEncoded()));
			
		} catch (IOException e) {
			
			e.printStackTrace();
			return "Wallet creation error!";
			
		}
		
		try (FileWriter fw = new FileWriter(priv)) {
			
			fw.append(base64(kp.getPrivate().getEncoded()));
			
		} catch (IOException e) {
			
			e.printStackTrace();
			return "Wallet creation error!";
			
		}

		myWallets.add(w);
		
		return "Wallet " + nick + " created!";
		
	}
	
	public boolean deleteWallet(Wallet wallet) {
		
		File f = new File(walletsFolder.getPath() + "/" + wallet.nickname);
		
		if (f.exists() && f.isDirectory()) {
			
			deleteRecursively(f);
			myWallets.remove(wallet);
			return true;
			
		}
		
		return false;
		
	}
	
	public static void deleteRecursively(File path) { //tool to delete a file tree
		
		if(path.exists()) {
			
			File files[] = path.listFiles();
			
			for(int i=0; i<files.length; i++) {
				
				if(files[i].isDirectory()) {
					
					deleteRecursively(files[i]);
					
				} else {
					
					files[i].delete();
					
				}
			}
			
		}
		
		path.delete();
		return;
		
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
	
	public static String base64(byte[] b) {
		
		return Base64.getEncoder().encodeToString(b);
		
	}
	
	public static PublicKey deserializePublicKey(String pub) {
		
		try {
			
			KeyFactory kf = KeyFactory.getInstance("RSA");
			
			PublicKey pka = kf.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(pub)));
			
			return pka;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	public static PrivateKey deserializePrivateKey(String priv) {
		
		try {
			
			KeyFactory kf = KeyFactory.getInstance("RSA");
			
			PrivateKey ska = kf.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(priv)));
			
			return ska;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	public static PublicKey generatePublic(PrivateKey priv)  {
		
		try {
			
			KeyFactory kf = KeyFactory.getInstance("RSA");
			RSAPrivateCrtKey privk = ((RSAPrivateCrtKey) priv);
			return kf.generatePublic(new RSAPublicKeySpec(privk.getModulus(), privk.getPublicExponent()));
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	   	
	   	return null;
		
	}
	
	public static boolean verifySignature(String plainText, String signature, PublicKey publicKey) throws Exception {
		Signature publicSignature = Signature.getInstance("SHA256withRSA");
		publicSignature.initVerify(publicKey);
		publicSignature.update(plainText.getBytes("UTF8"));

		byte[] signatureBytes = Base64.getDecoder().decode(signature);

		return publicSignature.verify(signatureBytes);
	}
	
	public static String readFileData(File f) {
		
		if (f == null || !f.exists()) return null;
		
		if (!Monieo.INSTANCE.isMonieoFile(f)) return null;
		
		//safe against injection of this pattern.
		try (Scanner c = new Scanner(f).useDelimiter(Pattern.compile("\\Z"))) {
			
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
	
	public static byte[] randomxRaw(String s) {
		
		return RandomXManager.getManager().getRandomX().hash(s);
		
	}
	
	public static String randomx(String s) {
		
		return bytesToHex(randomxRaw(s));
		
	}

	public static byte[] sha256dRaw(String s) {
		
		MessageDigest digest = null;
		
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return sha256dRaw(s, digest);
		
	}
	
	public static String sha256(String s) {
		
		return bytesToHex(sha256Raw(s));
		
	}
	
	public static byte[] sha256dRawInRawOut(byte[] in) {
		
		MessageDigest digest = null;
		
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		byte[] n = digest.digest(in);
		
		digest.reset();
		
		return digest.digest(n);
		
	}
	
	public static byte[] sha256Raw(String s) {
		
		MessageDigest digest = null;
		
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return sha256Raw(s, digest);
		
	}
	
	public static byte[] sha256Raw(String s, MessageDigest d) {
		
		d.reset();
		
		return d.digest(s.getBytes(StandardCharsets.UTF_8));
		
	}
	
	public static byte[] sha256dRaw(String s, MessageDigest d) {
		
		d.reset();
		
		byte[] n = d.digest(s.getBytes(StandardCharsets.UTF_8));
		
		d.reset();

		return d.digest(n);
		
	}
	
	public static String sha256d(String s) {
		
		return bytesToHex(sha256dRaw(s));
		
	}
	
	private static String bytesToHex(byte[] hash) {
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
	
	public static boolean assertSupportedProtocol(String[] s) {
		
		return s[0].equals(MAGIC_NUMBERS) && s[1].equals(PROTOCOL_VERSION);
		
	}
	
	public boolean isMonieoFile(File f) {
		
		while (f.getParentFile()!=null) {
		   f = f.getParentFile();
		   if (f.equals(workingFolder)) {
			   return true;
		   }
		}
		return false;
	}
	
}