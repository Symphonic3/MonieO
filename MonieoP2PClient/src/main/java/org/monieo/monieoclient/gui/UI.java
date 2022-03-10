 package org.monieo.monieoclient.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneLayout;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.monieo.monieoclient.Monieo;
import org.monieo.monieoclient.blockchain.BlockMetadata;
import org.monieo.monieoclient.blockchain.PendingFunds;
import org.monieo.monieoclient.blockchain.Transaction;
import org.monieo.monieoclient.gui.FeeEstimate.FeeEstimateType;
import org.monieo.monieoclient.mining.AbstractMiner.MiningSettings;
import org.monieo.monieoclient.mining.AbstractMiner.MiningStatistics;
import org.monieo.monieoclient.mining.DefaultMinerImpl;
import org.monieo.monieoclient.networking.Node;
import org.monieo.monieoclient.randomx.RandomXManager;
import org.monieo.monieoclient.randomx.RandomXManager.RandomXFlags;
import org.monieo.monieoclient.wallet.Wallet;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.JTextField;
import javax.swing.BoxLayout;
import javax.swing.JTabbedPane;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class UI {
	private JFrame frame;
	
	public volatile boolean fullInit = false;
	
	private String[] walletNicks;
	
	public JTable table;
	public JTable table2;
	public JTable txTable;
	public JScrollPane ads;
	
	public JList<String> list;
	public JButton btnChangeWalName;
	public JButton btnDelWal;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	
	JLabel lblNewLabel_1;
	JPanel panel;
	JPanel panel_1;
	JPanel panelTransaction;
	public JTextField addressLabel;
	JTextField nickLabel;
	JTextField INDIVbalanceLabel;
	JScrollPane scrollPane;
	JLabel label_1;
	JLabel label_3;
	JLabel label_5;
	JButton sentTrnt;
	JLabel lblNewLabel;
	JLabel lblTransactionAmount;
	JLabel lblFee;
	JButton BtnNEWADDRESS;
	JLabel lblToggleExperimentalMining;
	JLabel lblTotalBalancelabel;
	JButton TgBtnTOGGLEMINING;
	
	JTextField lblTotalBalancelabel_1;
	
	JTextField totAvailableFundsDisplay;
	JTextField totPendingFundsDisplay;
	JTextField totFundsDisplay;
	public JLabel totConnectedNodesDisplay;
	
	JCheckBoxMenuItem toggleDarkMode;
	
	JPanel invalidWallet;
	JPanel desyncDetected;
	JLabel desyncLabel;
	
	public boolean mining;
	JTextArea miningstats;
	public boolean miningWindowOpen = false;
	
	public boolean settingsWindowOpen = false;
	
	public boolean modeToggleStatus;
	private JLabel lblNewLabel_4;
	
	JComboBox<FeeEstimate> comboBox;
	
    public static final String[] FUNDINFO_COLUMN_NAMES = {"Wallet name",
            "Amount",
            "Confirmations"};
    
    public static final String[] NETWORK_COLUMN_NAMES = {"Node",
            "Direction",
            "Connected since"};
    
    public static final String[] TX_COLUMN_NAMES = {"Source",
            "Destination",
            "Amount", "Fee"};
    
    private JScrollPane scrollPane_1;
    private JTextField textField_3;
	
	public UI() {
	}
	
	public void finalizeTransaction(Transaction newTransaction) {
		
		String res = JOptionPane.showInputDialog(frame, 
				"WARNING: YOU ARE ABOUT TO SEND A MONIEO TRANSACTION.\nTHIS ACTION IS IRREVERSIBLE AND CANNOT BE UNDONE.\n\n"
				+ "To: " + newTransaction.d.to
				+ "\nAmount: " + newTransaction.d.amount.toPlainString() + " MNO"
				+ "\nFee: " + newTransaction.d.fee.toPlainString() + " MNO"
				+ "\n\nPlease type \"confirm\" below to confirm.", "Confirmation", 2);
		
		if (res == null) return;
		
		if (res.equals("confirm")) {
			
			Monieo.INSTANCE.txp.add(newTransaction);
			
			textField.setText(null);
			textField_1.setText(null);
			textField_2.setText(null);
			
			JOptionPane.showMessageDialog(frame, "Transaction submitted to the network! Depending on network traffic and the fee the transaction might be processed in any amount of time or not at all."
					+ "\n\nNote:\n\n- If your transaction is not confirmed, it will remain valid until you make another transaction from the same address."
					+ "\n- You should not make another transaction from this address until this transaction is confirmed unless you are attempting to cancel this transaction."
					+ "\n- Your balance will not be updated until the transaction is accepted by the network."
					+ "\n- You can see any recent outstanding unconfirmed transactions in Overview>Outgoing Transactions.");
			
			refresh(false, false);
			
			return;
			
		}
		
		notifyInvalid();
		
	}

	MiningSettings m;
	
	void updateSettings() {
		
        m = new MiningSettings(chckbxAllocateMemoryIn.isSelected(),
                chckbxNewCheckBox.isSelected(),
                chckbxNewCheckBox_1.isSelected(),
                chckbxNewCheckBox_1_1.isSelected(),
                (int)spinner.getValue());
		
	}
	
	public JComboBox<String> mCBox = new JComboBox<String>();
	public JButton mineButtonDo;
	JCheckBox chckbxNewCheckBox;
	JCheckBox chckbxNewCheckBox_1;
	JCheckBox chckbxNewCheckBox_1_1;
	JCheckBox chckbxAllocateMemoryIn;
	JSpinner spinner;
	JButton btnNewButton_1;
	
	JLabel lblNewLabelMStatus;
	
	Vector<JLabel> boxes = new Vector<JLabel>();
	
	 /**
	  * @wbp.parser.entryPoint
	  */
	@SuppressWarnings("serial")
	public void initialize() {
		
		m = RandomXManager.msettings;
		
		FlatLightLaf.setup();

		mineButtonDo = new JButton("Off");
		mineButtonDo.setBounds(10, 122, 279, 23);
		
		btnNewButton_1 = new JButton("Restart RandomX");
		btnNewButton_1.setBounds(115, 110, 125, 23);
		btnNewButton_1.setEnabled(false);
		btnNewButton_1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				btnNewButton_1.setText("Restarting RandomX...");
				RandomXManager.msettings = m;
				RandomXManager.applySettings();
				btnNewButton_1.setText("Restart RandomX");
				btnNewButton_1.setEnabled(false);
				
			}
		});
		
		mCBox = new JComboBox<String>();
		mCBox.setBounds(52, 89, 235, 22);

		mineButtonDo.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				mining = !mining;
				
				String text;
				String texta;
				
				if (mining) {
					
					chckbxAllocateMemoryIn.setEnabled(false);
					chckbxNewCheckBox.setEnabled(false);
					chckbxNewCheckBox_1.setEnabled(false);
					chckbxNewCheckBox_1_1.setEnabled(false);
					spinner.setEnabled(false);
					btnNewButton_1.setEnabled(false);
					
					mineButtonDo.setText("Restarting RandomX...");
					
					Monieo.INSTANCE.miner.begin(new Consumer<MiningStatistics>() {
						
						@Override
						public void accept(MiningStatistics t) {
							
							int th = 0;
							
							for (int i = 0; i < boxes.size(); i++) {
								
								if (t.hashrates.size() <= i) break;
								
								int v = t.hashrates.get(i);
								
								boxes.get(i).setText(v + "h/s");
								
								th += v;
								
							}
							
							BigInteger i = Monieo.INSTANCE.getHighestBlock().calculateNextDifficulty();
							
							BigDecimal hs = new BigDecimal(Monieo.MAXIMUM_HASH_VALUE).divide(new BigDecimal(i), 8, RoundingMode.HALF_UP);
							
							BigDecimal earn = new BigDecimal(th*60).divide(hs, 8, RoundingMode.HALF_UP).multiply(new BigDecimal(5));
							
							String txt = "Hashrate: " + th + "h/s"
									+ "\nEstim. rate of earning: " + earn.toPlainString() + " MNO/min"
									+ "\nBegan mining: " + TimeAgo.toDuration(System.currentTimeMillis()-t.beginTime)
									+ "\nBlocks mined: " + t.blocks
									+ "\nEstimated earnings: " + t.total.toPlainString() + " MNO";
							
							miningstats.setText(txt);
							
						}

					}, m);
					
					text = "On";
					texta = "Mining...";
					
				} else {
					
					Monieo.INSTANCE.miner.stop();
					
					chckbxAllocateMemoryIn.setEnabled(true);
					chckbxNewCheckBox.setEnabled(true);
					chckbxNewCheckBox_1.setEnabled(true);
					chckbxNewCheckBox_1_1.setEnabled(true);
					spinner.setEnabled(true);
					
					text = "Off";
					texta = "Stopped";
					
				}
				
				lblNewLabelMStatus.setText(texta);
				mineButtonDo.setText(text);
				TgBtnTOGGLEMINING.setText(text);
				
			}
			
		});
		
		miningstats = new JTextArea("Mining statistics");
		miningstats.setEditable(false);
		miningstats.setOpaque(false); //this is the same as a JLabel

		miningstats.setBounds(297, 6, 248, 139);
		miningstats.setLineWrap(true);
		
		frame = new JFrame();
		frame.setIconImage(new FlatSVGIcon("icon.svg").getImage());
		frame.setTitle("MonieO Client v" + Monieo.VERSION);
		frame.setBounds(100, 100, 895, 485);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		TgBtnTOGGLEMINING = new JButton("Off");
		TgBtnTOGGLEMINING.setBounds(798, 417, 76, 23);
		TgBtnTOGGLEMINING.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				if (miningWindowOpen) return;
				
				JDialog f = new JDialog(frame);
				f.setTitle("Mining v" + Monieo.VERSION);
				f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				f.setResizable(false);
				f.getContentPane().setLayout(null);
				f.setBounds(frame.getX(), frame.getY(), 853, 478);
				
				String texta;
				
				if (mining) {
					
					texta = "Mining...";
					
				} else {
					
					texta = "Stopped";
					
				}
				
				lblNewLabelMStatus = new JLabel(texta);
				Font font = lblNewLabelMStatus.getFont();
				Font fontset = new Font(font.getName(), Font.BOLD, 44);
				lblNewLabelMStatus.setFont(fontset);
				//lblNewLabel.putClientProperty(FlatClientProperties.STYLE_CLASS, "h1");
				lblNewLabelMStatus.setBounds(10, 0, 240, 60);
				f.getContentPane().add(lblNewLabelMStatus);
				
				JLabel lblNewLabel_1 = new JLabel("Wallet:");
				lblNewLabel_1.setBounds(10, 93, 46, 14);
				f.getContentPane().add(lblNewLabel_1);

				f.getContentPane().add(mCBox);

				f.getContentPane().add(mineButtonDo);
				
				f.getContentPane().add(miningstats);
				
				JPanel panel = new JPanel();
				//panel.setBorder(new LineBorder(new Color(0, 0, 0)));
				panel.setBounds(555, 0, 271, 139);
				f.getContentPane().add(panel);
				panel.setLayout(null);
				
				JLabel lblNewLabel_2 = new JLabel("Settings");
				lblNewLabel_2.setBounds(5, 0, 46, 18);
				panel.add(lblNewLabel_2);
				
				panel.add(btnNewButton_1);
				
				chckbxNewCheckBox = new JCheckBox("Optimized Argon2");
				chckbxNewCheckBox.setSelected(m.oArg2);
				chckbxNewCheckBox.setBounds(5, 42, 130, 23);
				panel.add(chckbxNewCheckBox);
				chckbxNewCheckBox.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						
						updateSettings();
						
						if (m.sameSettings(RandomXManager.msettings)) {
							
							btnNewButton_1.setEnabled(false);
							
						} else btnNewButton_1.setEnabled(true);
						
					}
					
				});
				
				chckbxAllocateMemoryIn = new JCheckBox("Allocate memory in large pages");
				chckbxAllocateMemoryIn.setSelected(m.mLargePages);
				chckbxAllocateMemoryIn.setBounds(5, 20, 242, 23);
				panel.add(chckbxAllocateMemoryIn);
				chckbxAllocateMemoryIn.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						
						updateSettings();
						
						if (m.sameSettings(RandomXManager.msettings)) {
							
							btnNewButton_1.setEnabled(false);
							
						} else btnNewButton_1.setEnabled(true);
						
					}
					
				});
				
				chckbxNewCheckBox_1 = new JCheckBox("Use full dataset");
				chckbxNewCheckBox_1.setSelected(m.fullRam);
				chckbxNewCheckBox_1.setBounds(5, 64, 193, 23);
				panel.add(chckbxNewCheckBox_1);
				chckbxNewCheckBox_1.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						
						updateSettings();
						
						if (m.sameSettings(RandomXManager.msettings)) {
							
							btnNewButton_1.setEnabled(false);
							
						} else btnNewButton_1.setEnabled(true);
						
					}
					
				});
				
				chckbxNewCheckBox_1_1 = new JCheckBox("Harware AES");
				chckbxNewCheckBox_1_1.setSelected(m.hAES);
				chckbxNewCheckBox_1_1.setBounds(5, 86, 193, 23);
				panel.add(chckbxNewCheckBox_1_1);
				chckbxNewCheckBox_1_1.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						
						updateSettings();
						
						if (m.sameSettings(RandomXManager.msettings)) {
							
							btnNewButton_1.setEnabled(false);
							
						} else btnNewButton_1.setEnabled(true);
						
					}
					
				});
				
				JLabel lblNewLabel_3 = new JLabel("Threads");
				lblNewLabel_3.setBounds(5, 111, 89, 14);
				panel.add(lblNewLabel_3);
				
				JPanel panel_1 = new JPanel();
				panel_1.setLayout(new WrapLayout());
				panel_1.setBounds(10, 156, 816, 271);
				f.getContentPane().add(panel_1);
				
				for (int i = 0; i < m.getThreads(); i++) {
					
					JLabel j = new JLabel("0h/s");
					j.setHorizontalAlignment(SwingConstants.CENTER);
					j.setVerticalAlignment(SwingConstants.CENTER);
					
					JPanel jp = new JPanel();
					jp.setLayout(new GridBagLayout());
			        jp.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
					jp.add(j);
					jp.setBackground(new Color(66, 132, 255, 127));
					jp.setOpaque(true);
					
					panel_1.add(jp);
					boxes.add(j);
					
				}
				
				spinner = new JSpinner(new SpinnerNumberModel(m.getThreads(), 1, m.getThreads()*2, 1));
				spinner.setBounds(52, 112, 52, 20);
				spinner.addChangeListener(new ChangeListener() {
					
					@Override
					public void stateChanged(ChangeEvent e) {
						
						updateSettings();
						
						boxes.clear();
						panel_1.removeAll();
						
						for (int i = 0; i < m.getThreads(); i++) {
							
							JLabel j = new JLabel("0h/s");
							j.setHorizontalAlignment(SwingConstants.CENTER);
							j.setVerticalAlignment(SwingConstants.CENTER);
							
							JPanel jp = new JPanel();
							jp.setLayout(new GridBagLayout());
					        jp.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
							jp.add(j);
							jp.setBackground(new Color(66, 132, 255, 127));
							jp.setOpaque(true);
							
							panel_1.add(jp);
							boxes.add(j);
							
						}
						
						SwingUtilities.updateComponentTreeUI(panel_1);
						
					}
					
				});
				panel.add(spinner);
				
				miningWindowOpen = true;
				
				f.addWindowListener(new WindowAdapter()
				{
					@Override
					public void windowClosing(WindowEvent e)
					{
						miningWindowOpen = false;
					}
					
				});
				
				SwingUtilities.updateComponentTreeUI(f);
				
				f.setVisible(true);
				
			}
			
		});
		
		BtnNEWADDRESS = new JButton("New address");
		BtnNEWADDRESS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object result = JOptionPane.showInputDialog(frame, "Enter new address nickname:");
				if (result!= null) {
					
					String resp = Monieo.INSTANCE.createWallet(result.toString());
					
					JOptionPane.showMessageDialog(frame, resp, "Info", JOptionPane.INFORMATION_MESSAGE);
					
					refresh(true, false);
				}
			}
		});
		BtnNEWADDRESS.setBounds(304, 417, 100, 23);
		frame.getContentPane().add(BtnNEWADDRESS);
		
		panel_1 = new JPanel();
		panel_1.setOpaque(false);
		panel_1.setBounds(196, 0, 689, 380);
		panel_1.setLayout(null);
		frame.getContentPane().add(panel_1);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(10, 130, 663, 225);
		tabbedPane.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				
				if (tabbedPane.getSelectedComponent().equals(ads)) {

					DefaultTableModel txmodel = (DefaultTableModel) txTable.getModel();
					txmodel.setRowCount(0);
					
					for (Transaction t : Monieo.INSTANCE.txp.getTrackedTx()) {
						
						txmodel.addRow(new Object[] {t.d.from, t.d.to, t.d.amount.toPlainString(), t.d.fee.toPlainString()});
						
					}
					
				}
				
			}
			
		});
		panel_1.add(tabbedPane);
		
		scrollPane_1 = new JScrollPane();
		tabbedPane.add("Funds Summary", scrollPane_1);
		
		txTable = new JTable();
		txTable.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		txTable.setAutoCreateRowSorter(true);
		txTable.getTableHeader().setReorderingAllowed(false);
		ads = new JScrollPane();
		tabbedPane.add("Outgoing Transactions", ads);
		ads.setViewportView(txTable);
		txTable.setModel(new DefaultTableModel(null, TX_COLUMN_NAMES) {
			
			@Override
		    public boolean isCellEditable(int row, int column) {
				return false;
		    }
		            
		});
		
		table = new JTable();
		table.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		table.setAutoCreateRowSorter(true);
		table.getTableHeader().setReorderingAllowed(false);
		scrollPane_1.setViewportView(table);
		table.setModel(new DefaultTableModel(null, FUNDINFO_COLUMN_NAMES) {
			
			@Override
		    public boolean isCellEditable(int row, int column) {
				return false;
		    }
		            
		});
		
		JScrollPane scrollPane_7 = new JScrollPane();
		tabbedPane.add("Network", scrollPane_7);
		
		table2 = new JTable();
		table2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		table2.setAutoCreateRowSorter(true);
		table2.getTableHeader().setReorderingAllowed(false);
		scrollPane_7.setViewportView(table2);
		table2.setModel(new DefaultTableModel(null, NETWORK_COLUMN_NAMES) {
			
			@Override
		    public boolean isCellEditable(int row, int column) {
				return false;
		    }
		            
		});
		
		JLabel lblTotalAvailableFunds = new JLabel("Available funds:");
		lblTotalAvailableFunds.setBounds(10, 10, 133, 29);
		panel_1.add(lblTotalAvailableFunds);
		
		JLabel plus = new JLabel("+");
		//plus.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		plus.setBounds(182, 34, 10, 10);
		panel_1.add(plus);
		
		totAvailableFundsDisplay = new JTextField("0");
		totAvailableFundsDisplay.setEditable(false);
		totAvailableFundsDisplay.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		totAvailableFundsDisplay.setBounds(144, 10, 331, 29);
		panel_1.add(totAvailableFundsDisplay);
		
		JLabel lblNewLabel_2 = new JLabel("Pending funds:");
		lblNewLabel_2.setBounds(10, 40, 133, 29);
		panel_1.add(lblNewLabel_2);
		
		totPendingFundsDisplay = new JTextField("0");
		totPendingFundsDisplay.setEditable(false);
		totPendingFundsDisplay.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		totPendingFundsDisplay.setBounds(144, 40, 331, 29);
		panel_1.add(totPendingFundsDisplay);
		
		JLabel eq = new JLabel("=");
		//eq.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		eq.setBounds(182, 64, 10, 10);
		panel_1.add(eq);
		
		JLabel lblTotalFunds = new JLabel("Total balance:");
		lblTotalFunds.setBounds(10, 70, 133, 29);
		panel_1.add(lblTotalFunds);
		
		totFundsDisplay = new JTextField("0");
		totFundsDisplay.setEditable(false);
		totFundsDisplay.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		totFundsDisplay.setBounds(144, 70, 331, 29);
		panel_1.add(totFundsDisplay);
		
		JLabel lblTotalConnectedNodes = new JLabel("Total connected nodes:");
		lblTotalConnectedNodes.setBounds(10, 100, 133, 29);
		panel_1.add(lblTotalConnectedNodes);
		
		totConnectedNodesDisplay = new JLabel("0");
		totConnectedNodesDisplay.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		totConnectedNodesDisplay.setBounds(144, 100, 331, 29);
		panel_1.add(totConnectedNodesDisplay);
		
		panel = new JPanel();
		panel.setVisible(false);
		panel.setOpaque(false);
		panel.setLayout(null);
		panel.setBounds(196, 0, 689, 380);
		frame.getContentPane().add(panel);
		
		addressLabel = new JTextField("0");
		addressLabel.setEditable(false);
		addressLabel.setOpaque(false); //this is the same as a JLabel
		
				addressLabel.setBounds(144, 10, 496, 29);

				panelTransaction = new JPanel();
				panelTransaction.setOpaque(false);
				panelTransaction.setBounds(10, 130, 663, 225);
				panel.add(panelTransaction);
				panelTransaction.setLayout(null);
				
				//what is a trnt and why is it already sent
				sentTrnt = new JButton("Send transaction");
				sentTrnt.setBounds(84, 191, 144, 23);
				panelTransaction.add(sentTrnt);
				sentTrnt.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							//TODO sanitize data and do pending in/out money. Client should allow you to create transactions spending funds that are already sent in other transactions,
							//TODO but not without warning first. Client should also track the expiry of generated transactions and un-pend funds that do not make it into blocks
							
							Wallet selectedWal = Monieo.INSTANCE.getWalletByNick(list.getSelectedValue());
							
							if (selectedWal == null || 
									textField.getText() == null || textField_1.getText() == null || textField_2.getText() == null) {
								
								notifyInvalid();
								return;
								
							}

							Transaction newTransaction = Transaction.createNewTransaction(selectedWal, new String(textField.getText()), new BigDecimal(textField_1.getText()), new BigDecimal(textField_2.getText()));
							
							if (newTransaction == null || !newTransaction.validate()) {

								notifyInvalid();
								return;
								
							}
							
							boolean fullIssue = false;
							
							BigDecimal am = BlockMetadata.getSpendableBalance(Monieo.INSTANCE.getHighestBlock().getMetadata().getWalletData(newTransaction.getSource()).pf).subtract(newTransaction.getAmount());
							
							for (Transaction t : Monieo.INSTANCE.txp.getTrackedTx()) {
								
								if (t.getSource().equals(newTransaction.getSource())) {
									
									am = am.subtract(t.getAmount().add(t.d.fee));
									fullIssue = true;
									
								}
								
							}
							
							if (am.compareTo(BigDecimal.ZERO) == -1) {
								
								String s = fullIssue ? "You have outstanding transaction(s) that depend on MonieO being spent by this transaction. "
										+ "One or more of the transaction(s) will not be accepted by the network! "
										: "You do not have enough MonieO to send this transaction! "
										+ "It will not be accepted by the network. ";
								
								s = s + "\n\nPlease confirm that you wish to send this tranaction! This can be a potentially dangerous action.";
								
								int r = JOptionPane.showConfirmDialog(frame, s, "Unsafe Transaction!", 0, JOptionPane.WARNING_MESSAGE);
								
								if (r == 0) finalizeTransaction(newTransaction);
								
							} else finalizeTransaction(newTransaction);
							
						} catch (Exception e2) {
							e2.printStackTrace();
							notifyInvalid();
						}
					}
				});
				
				textField = new JTextField();
				textField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
				textField.setBounds(84, 36, 491, 20);
				panelTransaction.add(textField);
				textField.setColumns(10);
				
				lblNewLabel = new JLabel("Recipient address:");
				lblNewLabel.setBounds(84, 11, 117, 14);
				panelTransaction.add(lblNewLabel);
				
				lblTransactionAmount = new JLabel("Transaction amount:");
				lblTransactionAmount.setBounds(84, 67, 117, 14);
				panelTransaction.add(lblTransactionAmount);
				
				textField_1 = new JTextField();
				textField_1.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
				textField_1.setColumns(10);
				textField_1.setBounds(84, 92, 491, 20);
				panelTransaction.add(textField_1);
				
				lblFee = new JLabel("Fee:");
				lblFee.setBounds(84, 120, 117, 14);
				panelTransaction.add(lblFee);
				
				textField_2 = new JTextField();
				textField_2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
				textField_2.setColumns(10);
				textField_2.setBounds(84, 145, 94, 20);
				textField_2.setEnabled(false);
				panelTransaction.add(textField_2);
				
				JCheckBox chckbxNewCheckBox = new JCheckBox("Select fee manually");
				chckbxNewCheckBox.setBounds(456, 144, 125, 23);
				panelTransaction.add(chckbxNewCheckBox);
				chckbxNewCheckBox.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						
						boolean s = chckbxNewCheckBox.isSelected();
						
						if (s) {
							
							comboBox.setEnabled(false);
							textField_2.setEnabled(true);
							
						} else {
							
							textField_2.setEnabled(false);
							comboBox.setEnabled(true);
							FeeEstimate f = (FeeEstimate)comboBox.getSelectedItem();
							textField_2.setText(f == null ? null : f.fee.toPlainString());
							
						}
						
					}
					
				});
				
				comboBox = new JComboBox<FeeEstimate>();
				comboBox.setBounds(188, 145, 262, 20);
				panelTransaction.add(comboBox);
				comboBox.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						
						if (!chckbxNewCheckBox.isSelected()) {
							
							FeeEstimate f = (FeeEstimate)comboBox.getSelectedItem();
							textField_2.setText(f == null ? null : f.fee.toPlainString());
							
						}
						
					}
					
				});
				
				panel.add(addressLabel);
				
				nickLabel = new JTextField("0");
				nickLabel.setEditable(false);
				nickLabel.setOpaque(false); //this is the same as a JLabel
				
						nickLabel.setBounds(144, 40, 331, 29);
						panel.add(nickLabel);
						
						INDIVbalanceLabel = new JTextField("0");
						INDIVbalanceLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
						INDIVbalanceLabel.setEditable(false);
						INDIVbalanceLabel.setOpaque(false); //this is the same as a JLabel
						
								INDIVbalanceLabel.setBounds(144, 70, 331, 29);
								panel.add(INDIVbalanceLabel);
								
								btnChangeWalName = new JButton("Change wallet name");
								btnChangeWalName.setBounds(480, 60, 160, 29);
								panel.add(btnChangeWalName);
								btnChangeWalName.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										if (list.getSelectedValue() != null) {
											Wallet selectedWal = Monieo.INSTANCE.getWalletByNick(list.getSelectedValue());
											String newNick = JOptionPane.showInputDialog(frame, "Enter new address nickname:");
											if (newNick != null) {
												File oldWalletFolder = new File((Monieo.INSTANCE.walletsFolder.toString() + "/" + selectedWal.nickname));
												File newWalletFolder = new File((oldWalletFolder.getParentFile().getPath() + "/" + newNick));
												oldWalletFolder.renameTo(newWalletFolder);
												Monieo.INSTANCE.getWalletByNick(selectedWal.nickname).nickname = newNick;
												refresh(true, false);
											}
										}
									}
								});
								
								/*btnDelWal = new JButton("Delete selected wallet");
								btnDelWal.setEnabled(false);
								btnDelWal.setBounds(480, 97, 160, 29);
								panel.add(btnDelWal);
								btnDelWal.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										(Wallet walletInQuestion = Monieo.INSTANCE.getWalletByNick(list.getSelectedValue());
										if (list.getSelectedValue() != null) {
											String confirmation = JOptionPane.showInputDialog(frame, "Enter wallet nickname name for \"" + list.getSelectedValue() + "\" to confirm deletion:");
											if (confirmation.equals(walletInQuestion.nickname)) {
												Monieo.INSTANCE.deleteWallet(Monieo.INSTANCE.getWalletByNick(list.getSelectedValue()));
												refresh(true);
											}
										}
									}
								});*/
								
								label_1 = new JLabel("Selected address:");
								label_1.setBounds(10, 10, 106, 29);
								panel.add(label_1);
								
								label_3 = new JLabel("Address nickname:");
								label_3.setBounds(10, 40, 133, 29);
								panel.add(label_3);
								
								label_5 = new JLabel("Address balance:");
								label_5.setBounds(10, 70, 133, 29);
								panel.add(label_5);
								
								JLabel label_5_1 = new JLabel("Pending funds:");
								label_5_1.setBounds(10, 100, 133, 29);
								panel.add(label_5_1);
								
								JButton btnNewButton_1 = new JButton("Click to view");
								btnNewButton_1.addActionListener(new ActionListener() {
									
									@Override
									public void actionPerformed(ActionEvent e) {
										
										list.setSelectedValue(null, false);
										tabbedPane.setSelectedIndex(0);
										refresh(false, false);
										
									 }
									
								});
								btnNewButton_1.setBounds(144, 102, 116, 23);
								panel.add(btnNewButton_1);
								
								//warning invalid wallet
								invalidWallet = new JPanel();
								invalidWallet.setBounds(10, 140, 663, 66);
								invalidWallet.setBorder(BorderFactory.createLineBorder(new Color(237, 162, 0, 180), 4, true));
								panel.add(invalidWallet);
								invalidWallet.setLayout(new BoxLayout(invalidWallet, BoxLayout.X_AXIS));
								
								invalidWallet.add(Box.createRigidArea(new Dimension(20, 0)));
								
								lblNewLabel_4 = new JLabel(UIManager.getIcon("OptionPane.warningIcon"));
								invalidWallet.add(lblNewLabel_4);
								
								invalidWallet.add(Box.createRigidArea(new Dimension(20, 0)));
								
								JLabel lblNewLabel_3 = new JLabel("Warning! This wallet has no associated private key, therefore the funds in it are not spendable!", SwingConstants.CENTER);
								invalidWallet.add(lblNewLabel_3);
								
								invalidWallet.setVisible(false);

		frame.getContentPane().add(TgBtnTOGGLEMINING);
		
		lblNewLabel_1 = new JLabel("Address count:");
		lblNewLabel_1.setBounds(208, 411, 109, 34);
		frame.getContentPane().add(lblNewLabel_1);
		
		JButton overviewBTN = new JButton("Overview");
		overviewBTN.setBounds(9, 414, 188, 23);
		overviewBTN.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				list.setSelectedValue(null, false);
				
				refresh(false, false);
				
			 }
			
		});
		frame.getContentPane().add(overviewBTN);
		
		list = new JList<String>();
		list.setBounds(10, 11, 188, 429);
		list.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				
				if (!e.getValueIsAdjusting()) {

					textField.setText(null);
					textField_1.setText(null);
					textField_2.setText(null);
					
					refresh(false, false);
					
				}
				
			}
			
		});
		
		scrollPane = new JScrollPane(list);
		scrollPane.setBounds(10, 7, 186, 396);
		scrollPane.createVerticalScrollBar();
		scrollPane.setLayout(new ScrollPaneLayout());
		frame.getContentPane().add(scrollPane);
		
		lblToggleExperimentalMining = new JLabel("Mining:");
		lblToggleExperimentalMining.setBounds(751, 414, 76, 29);
		frame.getContentPane().add(lblToggleExperimentalMining);
		
		lblTotalBalancelabel = new JLabel("Total available balance:");
		lblTotalBalancelabel.setBounds(409, 406, 231, 44);
		frame.getContentPane().add(lblTotalBalancelabel);
		
		//warning desync
		desyncDetected = new JPanel();
		desyncDetected.setBounds(206, 370, 663, 40);
		desyncDetected.setBorder(BorderFactory.createLineBorder(new Color(237, 162, 0, 180), 4, true));
		frame.getContentPane().add(desyncDetected);
		desyncDetected.setLayout(new BoxLayout(desyncDetected, BoxLayout.X_AXIS));
		
		desyncDetected.add(Box.createRigidArea(new Dimension(20, 0)));
		
		JLabel lblNewLabel_210 = new JLabel(UIManager.getIcon("OptionPane.warningIcon"));
		desyncDetected.add(lblNewLabel_210);
		
		desyncDetected.add(Box.createRigidArea(new Dimension(100, 0)));
		
		desyncLabel = new JLabel("Warning! Detected blockchain desync! (approx. " + 0 + " blocks)", SwingConstants.CENTER);
		desyncDetected.add(desyncLabel);
		
		textField_3 = new JTextField("0");
		textField_3.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		textField_3.setEditable(false);
		textField_3.setBounds(290, 411, 109, 34);
		frame.getContentPane().add(textField_3);
		
		lblTotalBalancelabel_1 = new JTextField("0");
		lblTotalBalancelabel_1.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		lblTotalBalancelabel_1.setEditable(false);
		lblTotalBalancelabel_1.setBounds(545, 406, 231, 44);
		frame.getContentPane().add(lblTotalBalancelabel_1);
		
		desyncDetected.setVisible(false);
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu file = new JMenu("File");
		JMenuItem newAddress = new JMenuItem("New Address...");
		newAddress.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				for (ActionListener n : BtnNEWADDRESS.getActionListeners()) n.actionPerformed(null);
				
			}
			
		});
		file.add(newAddress);
		menuBar.add(file);
		
		JMenu settings = new JMenu("Settings");
		toggleDarkMode = new JCheckBoxMenuItem("Dark Mode");
		toggleDarkMode.setState(Monieo.INSTANCE.settings.darkMode);
		toggleDarkMode.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				Monieo.INSTANCE.settings.darkMode = toggleDarkMode.getState();
				Monieo.INSTANCE.saveSettings();
				
				refresh(false, true);
				
			}
			
		});
		settings.add(toggleDarkMode);
		settings.add(new JSeparator());
		JMenuItem options = new JMenuItem("Options...");
		options.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				if (settingsWindowOpen) return;
				
				JDialog jd = new JDialog(frame);
				
				jd.setTitle("Settings v" + Monieo.VERSION);
				jd.setBounds(150, 150, 250+17, 85+45);
				jd.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				jd.setResizable(false);
				
				jd.getContentPane().setLayout(null);
				
				JCheckBox mOC = new JCheckBox("Minimize on close");
				mOC.setEnabled(false);
				mOC.setBounds(10, 10, 230, 15);
				mOC.setSelected(Monieo.INSTANCE.settings.minimizeOnClose);
				mOC.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						
						Monieo.INSTANCE.settings.minimizeOnClose = mOC.isSelected();
						Monieo.INSTANCE.saveSettings();
						
					}
					
				});
				jd.getContentPane().add(mOC);
				
				JCheckBox hOC = new JCheckBox("Hide hints");
				hOC.setEnabled(false);
				hOC.setBounds(10, 35, 230, 15);
				hOC.setSelected(Monieo.INSTANCE.settings.disableHints);
				hOC.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						
						Monieo.INSTANCE.settings.disableHints = hOC.isSelected();
						Monieo.INSTANCE.saveSettings();
						
					}
					
				});
				jd.getContentPane().add(hOC);
				
				JButton close = new JButton("Done");
				close.setBounds(10, 60, 230, 20);
				close.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						
						settingsWindowOpen = false;
						jd.dispose();
						
					}
					
				});
				jd.getContentPane().add(close);
				
				settingsWindowOpen = true;
				
				jd.addWindowListener(new WindowAdapter()
				{
					@Override
					public void windowClosing(WindowEvent e)
					{
						settingsWindowOpen = false;
					}
					
				});
				
				jd.setVisible(true);
				
				
			}
		});
		settings.add(options);
		menuBar.add(settings);
		
		JMenu tools = new JMenu("Tools");
		JMenuItem hm = new JMenuItem("Hash (RandomX)");
		hm.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				JDialog in = new JDialog(frame, "Enter data to hash (RandomX)");
				in.setSize(355, 199);
				in.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				in.setResizable(false);
				
				JTextArea inT = new JTextArea();
				inT.setLineWrap(true);
				inT.setBounds(10, 10, 320, 110);
				in.getContentPane().add(inT);
				
				JButton b = new JButton("Hash (RandomX)");
				b.setBounds(10, 130, 320, 20);
				b.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						
						inT.setText(Monieo.randomx(inT.getText()));
						
					}
					
				});
				in.getContentPane().add(b);
				
				in.getContentPane().setLayout(null);
				
				in.setVisible(true);
				
			}
			
		});
		JMenuItem hs = new JMenuItem("Hash (SHA256 single)");
		hs.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				JDialog in = new JDialog(frame, "Enter data to hash (SHA256 single)");
				in.setSize(355, 199);
				in.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				in.setResizable(false);
				
				JTextArea inT = new JTextArea();
				inT.setLineWrap(true);
				inT.setBounds(10, 10, 320, 110);
				in.getContentPane().add(inT);
				
				JButton b = new JButton("Hash (SHA256 single)");
				b.setBounds(10, 130, 320, 20);
				b.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						
						inT.setText(Monieo.sha256(inT.getText()));
						
					}
					
				});
				in.getContentPane().add(b);
				
				in.getContentPane().setLayout(null);
				
				in.setVisible(true);
				
			}
			
		});
		JMenuItem hd = new JMenuItem("Hash (SHA256d)");
		hd.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				JDialog in = new JDialog(frame, "Enter data to hash (SHA256d)");
				in.setSize(355, 199);
				in.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				in.setResizable(false);
				
				JTextArea inT = new JTextArea();
				inT.setLineWrap(true);
				inT.setBounds(10, 10, 320, 110);
				in.getContentPane().add(inT);
				
				JButton b = new JButton("Hash (SHA256d)");
				b.setBounds(10, 130, 320, 20);
				b.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						
						inT.setText(Monieo.sha256d(inT.getText()));
						
					}
					
				});
				in.getContentPane().add(b);
				
				in.getContentPane().setLayout(null);
				
				in.setVisible(true);
				
			}
			
		});
		tools.add(hm);
		tools.add(hs);
		tools.add(hd);
		menuBar.add(tools);
		
		JMenu window = new JMenu("Window");
		JMenuItem showOverview = new JMenuItem("Show Overview");
		showOverview.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				for (ActionListener n : overviewBTN.getActionListeners()) n.actionPerformed(null);
				
			}
			
		});
		window.add(showOverview);
		JMenuItem showMining = new JMenuItem("Show Mining");
		showMining.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				for (ActionListener n : TgBtnTOGGLEMINING.getActionListeners()) n.actionPerformed(null);
				
			}
			
		});
		window.add(showMining);
		menuBar.add(window);
		
		JMenu help = new JMenu("Help");		
		JMenuItem about = new JMenuItem("About");
		about.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				JLabel title = new JLabel("MonieO Client");
				title.putClientProperty(FlatClientProperties.STYLE_CLASS, "h1");
				JLabel copyR = new JLabel("© 2022 ‘The MonieO developers’");

				String link = "https://github.com/Symphonic3/MonieO";
				
				JOptionPane.showMessageDialog(frame,
					new Object[] {
						title,
						(Monieo.UPDATE ? "v" + Monieo.VERSION + " (latest is v" + Monieo.NEXT_AVAILABLE + ")" : "v" + Monieo.VERSION),
						" ",
						copyR,
						new MessageWithLink("<a href=\"" + link + "\">" + link + "</a>"),
					}, "About", JOptionPane.PLAIN_MESSAGE);
				
			}
			
		});
		help.add(about);
		help.add(new JSeparator());
		JMenuItem discord = new JMenuItem("Chat");
		discord.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					Desktop.getDesktop().browse(URI.create("https://discord.gg/y2Hx9Ewn2V"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
			}
			
		});
		help.add(discord);
		JMenuItem github = new JMenuItem("Github");
		github.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					Desktop.getDesktop().browse(URI.create("https://github.com/Symphonic3/MonieO"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
			}
			
		});
		help.add(github);
		menuBar.add(help);
		
		frame.setResizable(false);
		refresh(true, true);
		frame.setVisible(true);
		frame.setAlwaysOnTop(true);
		frame.setAlwaysOnTop(false);
		
		for (Component c : getAllComponents(frame)) {
			
			if (c instanceof JTextField) {
				
				JTextField cr = (JTextField)c;
				
				cr.addMouseListener(new MouseListener() {
					
					@Override
					public void mouseClicked(MouseEvent e) {
						
						JPopupMenu p = new JPopupMenu();
						
						if (e.getButton() != 3) return;
						
						if (!cr.isEnabled()) return;
						
						if (cr.isEditable()) {
							
							JMenuItem pst = new JMenuItem("Paste");
							pst.addActionListener(new ActionListener() {
								
								@Override
								public void actionPerformed(ActionEvent e) {
									
									cr.paste();
									
								}
								
							});
							p.add(pst);				
							
							
						} else {
							
							JMenuItem cp = new JMenuItem("Copy");
							cp.addActionListener(new ActionListener() {
								
								@Override
								public void actionPerformed(ActionEvent e) {
									
									cr.selectAll();
									cr.copy();
									
								}
								
							});
							p.add(cp);
							
						}
						
						p.show(cr, cr.getMousePosition().x, cr.getMousePosition().y);
						
					}

					@Override
					public void mousePressed(MouseEvent e) {}
					@Override
					public void mouseReleased(MouseEvent e) {}
					@Override
					public void mouseEntered(MouseEvent e) {}
					@Override
					public void mouseExited(MouseEvent e) {}
					
				});
				
			} else if (c instanceof JTable) {
				
				JTable cr = (JTable)c;
				
				cr.addMouseListener(new MouseListener() {
					
					@Override
					public void mouseClicked(MouseEvent e) {
						
						JPopupMenu p = new JPopupMenu();
						
						if (e.getButton() != 3) return;
						
						if (!cr.isEnabled()) return;
						
						JMenuItem cp = new JMenuItem("Copy");
						cp.addActionListener(new ActionListener() {
							
							@Override
							public void actionPerformed(ActionEvent e) {
								
								String s = "";
								int clc = table.getColumnCount();
								for (int i = 0; i < clc; i++) {
									
									s = s + table.getColumnName(i) + " | ";
									
								}
								
								s = s.substring(0, s.length()-3);
								
								int[] rw = table.getSelectedRows();
								
								for (int i = 0; i < rw.length; i++) {
									
									s = s + "\n";
									
									for (int k = 0; k < table.getColumnCount(); k++) {
										
										s = s + table.getValueAt(rw[i], k) + " | ";
										
									}
									
									s = s.substring(0, s.length()-3);
									
								}
								
								StringSelection ss = new StringSelection(s);
								Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
								
							}
							
						});
						p.add(cp);
						
						p.show(cr, cr.getMousePosition().x, cr.getMousePosition().y);
						
					}

					@Override
					public void mousePressed(MouseEvent e) {}
					@Override
					public void mouseReleased(MouseEvent e) {}
					@Override
					public void mouseEntered(MouseEvent e) {}
					@Override
					public void mouseExited(MouseEvent e) {}
					
				});
				
			}
			
		}
		
		fullInit = true;
		
		//frame.requestFocus();
		
	}
	
	public static List<Component> getAllComponents(final Container c) {
	    Component[] comps = c.getComponents();
	    List<Component> compList = new ArrayList<Component>();
	    for (Component comp : comps) {
	        compList.add(comp);
	        if (comp instanceof Container)
	            compList.addAll(getAllComponents((Container) comp));
	    }
	    return compList;
	}
	
	public void refresh(boolean updlist, boolean changeColor) {
		
		if (changeColor) {
			
			if (!Monieo.INSTANCE.settings.darkMode) {
				
				FlatLightLaf.setup();
				SwingUtilities.updateComponentTreeUI(frame);
				miningstats.updateUI();
				
			} else {
				
				FlatDarkLaf.setup();
				SwingUtilities.updateComponentTreeUI(frame);
				miningstats.updateUI();
				
			}

			totAvailableFundsDisplay.setBorder(null);
			totPendingFundsDisplay.setBorder(null);
			totFundsDisplay.setBorder(null);
			lblTotalBalancelabel_1.setBorder(null);
			INDIVbalanceLabel.setBorder(null); //remove the border
			nickLabel.setBorder(null); //remove the border
			addressLabel.setBorder(null); //remove the border
			textField_3.setBorder(null); //remove the border
			miningstats.setBorder(null); //remove the border
			
		}
		
		comboBox.removeAllItems();
		
		comboBox.addItem(new FeeEstimate(Monieo.INSTANCE.getEstimatedAverageFee(), FeeEstimateType.AVERAGE));
		comboBox.addItem(new FeeEstimate(Monieo.INSTANCE.getEstimatedLowestFee(), FeeEstimateType.SMALLEST));
		comboBox.addItem(new FeeEstimate(Monieo.INSTANCE.getEstimatedHighestFee(), FeeEstimateType.LARGEST));
		
		totConnectedNodesDisplay.setText(String.valueOf(Monieo.INSTANCE.nodes.size()));
		
		DefaultTableModel model2 = (DefaultTableModel) table2.getModel();
		model2.setRowCount(0);
		
		for (Node n : Monieo.INSTANCE.nodes) {
			
			model2.addRow(new Object[] {n.getAdress(), n.isServer() ? "Inbound" : "Outbound", n.timeConnected});
			
		}

		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setRowCount(0);
		
		long ds = Monieo.INSTANCE.desyncAmount();
		
		if (ds != -1) {
			
			desyncLabel.setText("Warning! Detected blockchain desync! (approx. " + ds + " blocks)");
			
			desyncDetected.setVisible(true);
			
		} else {
			
			desyncDetected.setVisible(false);
			
		}
		
		if (updlist) {
			
			walletNicks = new String[Monieo.INSTANCE.myWallets.size()];
			for (int i = 0; i < walletNicks.length; i++) {
				
				walletNicks[i] = Monieo.INSTANCE.myWallets.get(i).nickname;
				
			}
			list.setListData(walletNicks);
			mCBox.removeAllItems();
			
			for (String s : walletNicks) mCBox.addItem(s);
			
		}
		
		BlockMetadata m = Monieo.INSTANCE.getHighestBlock().getMetadata();
		
		if (list.getSelectedIndex() == -1) {

			panel.setVisible(false);
			panel_1.setVisible(true);
			
		} else {
			
			panel_1.setVisible(false);
			panel.setVisible(true);
			
			Wallet w = Monieo.INSTANCE.getWalletByNick(list.getSelectedValue());
			BigDecimal n = BlockMetadata.getSpendableBalance(m.getWalletData(w.getAsString()).pf).setScale(8);
			
			addressLabel.setText(w.getAsString());
			nickLabel.setText(w.nickname);
			INDIVbalanceLabel.setText(n.toPlainString() + " MNO");
			
			if (w.hasSK) {
				
				invalidWallet.setVisible(false);
				panelTransaction.setVisible(true);
				
			} else {

				panelTransaction.setVisible(false);
				invalidWallet.setVisible(true);
				
			}

		}
		
		BigDecimal unspendable = BigDecimal.ZERO.setScale(8);
		BigDecimal spendable = BigDecimal.ZERO.setScale(8);
		BigDecimal total = BigDecimal.ZERO.setScale(8);
		
		for (String s : walletNicks) {
			
			Wallet w = Monieo.INSTANCE.getWalletByNick(s);
			
			List<PendingFunds> pf = m.getWalletData(w.getAsString()).pf;
			
			for (PendingFunds p : pf) {
				
				if (p.isSpendable()) {

					spendable = spendable.add(p.amount);
					
				} else {
					
					unspendable = unspendable.add(p.amount);
					
				}
			
				model.addRow(new Object[] {s, p.amount.toPlainString(), p.isOverConfirmed() ? Monieo.CONFIRMATIONS_IGNORE + "+" : p.conf});
				
			}
			
		}
		
		total = spendable.add(unspendable);
		
		lblTotalBalancelabel_1.setText(spendable.toPlainString() + " MNO");
		totAvailableFundsDisplay.setText(spendable.toPlainString() + " MNO");
		totPendingFundsDisplay.setText(unspendable.toPlainString() + " MNO");
		totFundsDisplay.setText(total.toPlainString() + " MNO");
		
	}
	
	public void notifyInvalid() {
		
		JOptionPane.showMessageDialog(frame, "Invalid data entered!", "Error", JOptionPane.ERROR_MESSAGE);
		
	}
}