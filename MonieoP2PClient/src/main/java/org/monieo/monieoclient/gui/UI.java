 package org.monieo.monieoclient.gui;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
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
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.monieo.monieoclient.Monieo;
import org.monieo.monieoclient.blockchain.BlockMetadata;
import org.monieo.monieoclient.blockchain.PendingFunds;
import org.monieo.monieoclient.blockchain.Transaction;
import org.monieo.monieoclient.gui.FeeEstimate.FeeEstimateType;
import org.monieo.monieoclient.mining.AbstractMiner.MiningStatistics;
import org.monieo.monieoclient.networking.Node;
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
	
	private String[] walletNicks;
	
	public JTable table;
	public JTable table2;
	
	public JList<String> list;
	public JButton btnChangeWalName;
	public JButton btnDelWal;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	
	JTextField lblNewLabel_1;
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
	
	JLabel totAvailableFundsDisplay;
	JLabel totPendingFundsDisplay;
	JLabel totFundsDisplay;
	public JLabel totConnectedNodesDisplay;
	
	JCheckBoxMenuItem toggleDarkMode;
	
	JPanel invalidWallet;
	JPanel desyncDetected;
	JLabel desyncLabel;
	
	public boolean mining;
	JTextArea miningstats;
	public boolean miningWindowOpen = false;
	
	public boolean modeToggleStatus;
	private JLabel lblNewLabel_4;
	
	JComboBox<FeeEstimate> comboBox;
	
    public static final String[] FUNDINFO_COLUMN_NAMES = {"Wallet name",
            "Amount",
            "Confirmations"};
    
    public static final String[] NETWORK_COLUMN_NAMES = {"Node",
            "Direction",
            "Connected since"};
    
    private JScrollPane scrollPane_1;
	
	public UI() {
	}

	
	 /**
	  * @wbp.parser.entryPoint
	  */
	public void initialize() {
		
		FlatLightLaf.setup();
		
		miningstats = new JTextArea("Mining statistics");
		miningstats.setEditable(false);
		miningstats.setOpaque(false); //this is the same as a JLabel

		miningstats.setBounds(10, 64, 205, 190);
		miningstats.setLineWrap(true);
		
		frame = new JFrame();
		frame.setIconImage(new FlatSVGIcon("icon.svg").getImage());
		frame.setTitle("MonieO Client v" + Monieo.VERSION);
		frame.setBounds(100, 100, 901, 485);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		TgBtnTOGGLEMINING = new JButton("Off");
		TgBtnTOGGLEMINING.setBounds(799, 417, 76, 23);
		TgBtnTOGGLEMINING.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				if (miningWindowOpen) return;
				
				JDialog jd = new JDialog(frame);
				
				jd.setTitle("Mining v" + Monieo.VERSION);
				jd.setBounds(150, 150, 242, 350);
				jd.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				jd.setResizable(false);
				
				jd.getContentPane().setLayout(null);

				String text;
				
				if (mining) {
					
					text = "On";
					
				} else {
					
					text = "Off";
					
				}
				
				JButton btnNewButton = new JButton(text);
				btnNewButton.setBounds(10, 30, 205, 23);
				jd.getContentPane().add(btnNewButton);
				
				JLabel lblNewLabel = new JLabel("Toggle mining:");
				lblNewLabel.setBounds(10, 11, 172, 14);
				jd.getContentPane().add(lblNewLabel);
				
				jd.getContentPane().add(miningstats);
				
				btnNewButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						
						mining = !mining;
						
						String text;
						
						if (mining) {
							
							Monieo.INSTANCE.miner.begin(new Consumer<MiningStatistics>() {
								
								@Override
								public void accept(MiningStatistics t) {
									
									String txt = "Began mining: " + t.beginTime
											+ "\nHashes: " + t.hashes.toString()
											+ "\nBlock target: " + t.blockTarget.toString()
											+ "\nBlocks mined: " + t.blocks
											+ "\nTotal earned: " + t.total.toPlainString();
									
									miningstats.setText(txt);
									
								}

							});
							text = "On";
							
						} else {
							
							Monieo.INSTANCE.miner.stop();
							
							MiningStatistics m = Monieo.INSTANCE.miner.getMiningStatistics();
							
							long dur = System.currentTimeMillis() - m.beginTime;
							
							BigDecimal hashrate = new BigDecimal(m.hashes).divide(new BigDecimal(dur/1000), 0, RoundingMode.HALF_UP);
							
							JOptionPane.showMessageDialog(frame, new JLabel("Session hashrate estimate: " + hashrate.toPlainString() + "h/s"), "Mining session over", JOptionPane.INFORMATION_MESSAGE);
							
							text = "Off";
							
						}
						
						btnNewButton.setText(text);
						TgBtnTOGGLEMINING.setText(text);
						
					}
					
				});
				
				miningWindowOpen = true;
				
				jd.addWindowListener(new WindowAdapter()
				{
					@Override
					public void windowClosing(WindowEvent e)
					{
						miningWindowOpen = false;
					}
					
				});
				
				jd.setVisible(true);
				
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
		panel_1.add(tabbedPane);
		
		scrollPane_1 = new JScrollPane();
		tabbedPane.add("Funds Summary", scrollPane_1);
		
		table = new JTable();
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
		table2.setAutoCreateRowSorter(true);
		table2.getTableHeader().setReorderingAllowed(false);
		scrollPane_7.setViewportView(table2);
		table2.setModel(new DefaultTableModel(null, NETWORK_COLUMN_NAMES) {
			
			@Override
		    public boolean isCellEditable(int row, int column) {
				return false;
		    }
		            
		});
		
		JLabel lblTotalAvailableFunds = new JLabel("Total spendable funds:");
		lblTotalAvailableFunds.setBounds(10, 10, 133, 29);
		panel_1.add(lblTotalAvailableFunds);
		
		totAvailableFundsDisplay = new JLabel("0");
		totAvailableFundsDisplay.setBounds(144, 10, 331, 29);
		panel_1.add(totAvailableFundsDisplay);
		
		JLabel lblNewLabel_2 = new JLabel("Total pending funds:");
		lblNewLabel_2.setBounds(10, 40, 133, 29);
		panel_1.add(lblNewLabel_2);
		
		totPendingFundsDisplay = new JLabel("0");
		totPendingFundsDisplay.setBounds(144, 40, 331, 29);
		panel_1.add(totPendingFundsDisplay);
		
		JLabel lblTotalFunds = new JLabel("Total funds:");
		lblTotalFunds.setBounds(10, 70, 133, 29);
		panel_1.add(lblTotalFunds);
		
		totFundsDisplay = new JLabel("0");
		totFundsDisplay.setBounds(144, 70, 331, 29);
		panel_1.add(totFundsDisplay);
		
		JLabel lblTotalConnectedNodes = new JLabel("Total connected nodes:");
		lblTotalConnectedNodes.setBounds(10, 100, 133, 29);
		panel_1.add(lblTotalConnectedNodes);
		
		totConnectedNodesDisplay = new JLabel("0");
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
				
				addressLabel.addMouseListener(new MouseAdapter() {
					
					 @Override
					 public void mouseClicked(MouseEvent e) {
						StringSelection ss = new StringSelection(addressLabel.getText());
						Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
						
						JDialog jd = new JDialog();
						jd.setSize(50, 15);
						jd.setLocation(e.getLocationOnScreen());
						jd.getContentPane().add(new JLabel("Copied!"));
						jd.setUndecorated(true);
						((JPanel)jd.getContentPane()).setBorder(BorderFactory.createLineBorder(Color.BLACK));
						jd.setFocusable(false);
						jd.setFocusableWindowState(false);
						
						jd.setModalityType(ModalityType.MODELESS);

						jd.setVisible(true);
						
						new Timer().schedule(new TimerTask() {
							
							@Override
							public void run() {
								
								jd.setVisible(false);
								jd.dispose();
								
							}
							
						}, 1000);
						
					 }
					
				});
				
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
								
								JOptionPane.showMessageDialog(frame, "Transaction sent!");
								
								refresh(false, false);
								
								return;
								
							}
							
							notifyInvalid();
							
						} catch (Exception e2) {
							e2.printStackTrace();
							notifyInvalid();
						}
					}
				});
				
				textField = new JTextField();
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
				textField_1.setColumns(10);
				textField_1.setBounds(84, 92, 491, 20);
				panelTransaction.add(textField_1);
				
				lblFee = new JLabel("Fee:");
				lblFee.setBounds(84, 120, 117, 14);
				panelTransaction.add(lblFee);
				
				textField_2 = new JTextField();
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
								
								btnDelWal = new JButton("Delete selected wallet");
								btnDelWal.setEnabled(false);
								btnDelWal.setBounds(480, 97, 160, 29);
								panel.add(btnDelWal);
								btnDelWal.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										/*(Wallet walletInQuestion = Monieo.INSTANCE.getWalletByNick(list.getSelectedValue());
										if (list.getSelectedValue() != null) {
											String confirmation = JOptionPane.showInputDialog(frame, "Enter wallet nickname name for \"" + list.getSelectedValue() + "\" to confirm deletion:");
											if (confirmation.equals(walletInQuestion.nickname)) {
												Monieo.INSTANCE.deleteWallet(Monieo.INSTANCE.getWalletByNick(list.getSelectedValue()));
												refresh(true);
											}
										}*/
									}
								});
								
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
		
		JButton btnNewButton = null;
		
		lblNewLabel_1 = new JTextField("Address count:");
		lblNewLabel_1.setEditable(false);

		lblNewLabel_1.setBounds(208, 411, 109, 34);
		frame.getContentPane().add(lblNewLabel_1);
		
		JButton overviewBTN = new JButton("Overview");
		overviewBTN.setBounds(10, 414, 176, 23);
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
		scrollPane.setBounds(10, 11, 176, 396);
		scrollPane.createVerticalScrollBar();
		scrollPane.setLayout(new ScrollPaneLayout());
		frame.getContentPane().add(scrollPane);
		
		lblToggleExperimentalMining = new JLabel("Mining:");
		lblToggleExperimentalMining.setBounds(756, 414, 76, 29);
		frame.getContentPane().add(lblToggleExperimentalMining);
		
		lblTotalBalancelabel = new JLabel("Total spendable balance:");
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
		toggleDarkMode.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				refresh(false, true);
				
			}
			
		});
		settings.add(toggleDarkMode);
		settings.add(new JSeparator());
		JMenuItem options = new JMenuItem("Options...");
		options.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		settings.add(options);
		menuBar.add(settings);
		
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

				String link = "https://github.com/Symphonic3/MonieO";
				
				JOptionPane.showMessageDialog(frame,
					new Object[] {
						title,
						(Monieo.UPDATE ? "v" + Monieo.VERSION + " (latest is v" + Monieo.NEXT_AVAILABLE + ")" : "v" + Monieo.VERSION),
						" ",
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

		//frame.requestFocus();
		
	}
	
	public void refresh(boolean updlist, boolean changeColor) {
		
		if (changeColor) {
			
			if (!toggleDarkMode.getState()) {
				
				FlatLightLaf.setup();
				SwingUtilities.updateComponentTreeUI(frame);
				miningstats.updateUI();
				
			} else {
				
				FlatDarkLaf.setup();
				SwingUtilities.updateComponentTreeUI(frame);
				miningstats.updateUI();
				
			}

			INDIVbalanceLabel.setBorder(null); //remove the border
			nickLabel.setBorder(null); //remove the border
			addressLabel.setBorder(null); //remove the border
			lblNewLabel_1.setBorder(null); //remove the border
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
			lblNewLabel_1.setText("Address count: " + walletNicks.length);
			
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
			INDIVbalanceLabel.setText(n.toPlainString());
			
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
		
		lblTotalBalancelabel.setText("Total spendable balance: " + spendable.toPlainString());
		totAvailableFundsDisplay.setText(spendable.toPlainString());
		totPendingFundsDisplay.setText(unspendable.toPlainString());
		totFundsDisplay.setText(total.toPlainString());
		
	}
	
	public void notifyInvalid() {
		
		JOptionPane.showMessageDialog(frame, "Invalid data entered!", "Error", JOptionPane.ERROR_MESSAGE);
		
	}
}