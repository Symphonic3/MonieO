package org.monieo.monieoclient;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.SplashScreen;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.monieo.monieoclient.blockchain.Block;
import org.monieo.monieoclient.blockchain.BlockHeader;
import org.monieo.monieoclient.blockchain.CoinbaseTransaction;
import org.monieo.monieoclient.gui.UI;
import org.monieo.monieoclient.mining.AbstractMiner;
import org.monieo.monieoclient.mining.DefaultMinerImpl;
import org.monieo.monieoclient.mining.TxPool;
import org.monieo.monieoclient.networking.ConnectionHandler;
import org.monieo.monieoclient.networking.NetAdressHolder;
import org.monieo.monieoclient.networking.NetworkPacket;
import org.monieo.monieoclient.networking.NetworkPacket.NetworkPacketType;
import org.monieo.monieoclient.networking.Node;
import org.monieo.monieoclient.wallet.Wallet;

public class Monieo {
	
	public static final int PORT = 21093;
	public static final String MAGIC_NUMBERS = "MEOPROTOCOL";
	public static final String PROTOCOL_VERSION = "1.0";
	public static double VERSION;
	
	public static int MAX_OUTGOING_CONNECTIONS = 10;
	public static int MAX_CONNECTIONS = 110;
	
	public static int CONFIRMATIONS = 5;
	public static int CONFIRMATIONS_BLOCK_SENSITIVE = 60;
	
	public static Monieo INSTANCE;
	
	public static final BigInteger MAXIMUM_HASH_VALUE = BigInteger.ONE.shiftLeft(256).subtract(BigInteger.ONE);
	
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

		VERSION = Double.valueOf(properties.getProperty("version"));
		
		System.out.println("Version: " + VERSION);
		System.out.println("Please look towards the GUI application.");
		System.out.println("");
		System.out.println("If there are any issues with running the application, all errors will be logged in this window.");
		System.out.println("Please paste the full log of this window when submitting a bug report.");
		
		ssg.setComposite(AlphaComposite.Clear);
		ssg.fillRect(30,310,210,20);
        ssg.setPaintMode();
        ssg.setColor(Color.BLACK);
        ssg.setFont(new Font("Dialog", Font.PLAIN, 15));
		ssg.drawString("v" + String.valueOf(VERSION), 30, 310);
		
		ss.update();
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ss.close();
		
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
	
	public UI ui;
	
	File workingFolder;
	File nodesFile;
	public File walletsFolder; 
	public File blocksFolder; 
	public File blocksExtraDataFolder;
	public File blockMetadataFolder; 
	
	public File blkhighest;
	
	public File txlistFolder;
	
	List<Socket> connections = new ArrayList<Socket>();
	List<NetAdressHolder> knownNodes = new ArrayList<NetAdressHolder>();
	
	public List<Wallet> myWallets = new ArrayList<Wallet>();
	
	public Vector<Node> nodes = new Vector<Node>();
	
	public TxPool txp;
	
	public ConnectionHandler ch;
	
	public AbstractMiner miner;
	
	public Monieo() {
		
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
		
		nodesFile = new File(workingFolder.getPath() + "/nodes.dat");
		
		try {
			nodesFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try (Scanner c = new Scanner(nodesFile)) {
			
			while (c.hasNextLine()) {
				
				knownNodes.add(NetAdressHolder.deserialize(c.nextLine()));
				
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
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
		
		blkhighest = new File(workingFolder.getPath() + "/.blkhighest");

		if (!blkhighest.exists()) {
			
			try {
				blkhighest.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		handleBlock(genesis());

        ui = new UI();
        ui.initialize();
        
		txp = new TxPool(txlistFolder);
		
		ch = new ConnectionHandler();
		new Thread(ch).start();
		
		//main timer
		new Timer().schedule(new TimerTask() {
			
			@Override
			public void run() {
				
				int amntns = 0;

				Block b = getHighestBlock();
				Block g = genesis();
				
				for (int i = 0; i < CONFIRMATIONS*2; i++) {
					
					if (b.equals(g)) {
						
						break;
						
					}
					
					b = b.getPrevious();
					
				}
				
				for (int i = 0; i < nodes.size(); i++) {

					Node n = nodes.get(i);
					
					if (System.currentTimeMillis()-n.lastValidPacketTime > Node.MIN_RESPONSE_TIME) {
						
						n.disconnect();
						i--;
						continue;
						
					}
					
					if (!n.isServer()) amntns++;
					
					if (!n.localAcknowledgedRemote || !n.remoteAcknowledgedLocal) continue;
					
					//functions as a keepalive, for now
					n.sendNetworkPacket(new NetworkPacket(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkPacketType.REQUEST_BLOCKS_AFTER, b.hash()));
					
				}
				
				List<String> rn = getValidNodesRightNow();
				
				int ind = 0;

				for (int i = 0; i < MAX_OUTGOING_CONNECTIONS-amntns; i++) {
					
					whilea: while (rn.size() < ind) {
						
						for (int k = 0; k < nodes.size(); k++) {
							
							Node n = nodes.get(k);
							
							if (n.getAdress().equalsIgnoreCase(rn.get(ind))) {
								
								ind++;
								continue whilea;
								
							}
							
						}
						
						System.out.println("attempting to connect to a node!");
						ch.connect(rn.get(ind));
						ind++;
						break whilea;
						
					}
					
				}
				
			}
			
		}, 0, 2500);
		
	}
	
	public boolean isSynchronized() {
		
		//TODO work on this check
		
		return getHighestBlock().header.timestamp + 7200000 > getNetAdjustedTime();
		
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
			
			System.out.println("generating metadata " + b.hash());
			b.generateMetadata();
			
			if (getHighestBlock() == null || b.getChainWork().compareTo(getHighestBlock().getChainWork()) == 1) {
				
				setHighestBlock(b);
				if (ui != null) ui.refresh(false); //ui not initialized yet!
				
			}
			
			Node.propagateAll(new NetworkPacket(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkPacketType.SEND_BLOCK, b.serialize()));
			
		}
		
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
	
	public List<String> getValidNodesRightNow() {
		
		pruneKnownNodes();
		
		List<String> ad = new ArrayList<String>();
		
		for (NetAdressHolder nah : knownNodes) {
			
			if (!nah.isBanned()) ad.add(nah.adress);
			
		}
		
		Collections.shuffle(ad);
		
		if (ad.size() > 1000) ad.subList(1000, ad.size()).clear();
		
		return ad;
		
	}
	
	public NetAdressHolder fetchByAdress(String address) {
		
		for (NetAdressHolder nah : knownNodes) {
			
			if (nah.adress.equalsIgnoreCase(address)) {
				
				return nah;
				
			}
			
		}
		
		return null;
		
	}
	
	public void attemptRememberNode(String address) {
		
		pruneKnownNodes();
		
		if (fetchByAdress(address) != null) return;
		
		NetAdressHolder n = new NetAdressHolder(address, Long.MAX_VALUE, 0);
		
		knownNodes.add(n);
		
		saveKnownNodes();
		
	}
	
	public void saveKnownNodes() {

		try (FileWriter c = new FileWriter(nodesFile, false)) {
			
			for (NetAdressHolder nah : knownNodes) {
				
				c.append(nah.serialize() + "\n");
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void pruneKnownNodes() {
		
		knownNodes.removeIf(t -> t.isExpired());
		
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