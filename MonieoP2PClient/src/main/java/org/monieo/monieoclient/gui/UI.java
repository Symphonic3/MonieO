 package org.monieo.monieoclient.gui;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.Dialog.ModalityType;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer.Form;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneLayout;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.monieo.monieoclient.Monieo;
import org.monieo.monieoclient.blockchain.BlockMetadata;
import org.monieo.monieoclient.blockchain.PendingFunds;
import org.monieo.monieoclient.blockchain.Transaction;
import org.monieo.monieoclient.mining.AbstractMiner.MiningStatistics;
import org.monieo.monieoclient.wallet.Wallet;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.JTextField;

public class UI {
	private JFrame frame;
	
	private String[] walletNicks;
	
	public JList<String> list;
	public JButton btnChangeWalName;
	public JButton btnDelWal;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	
	JTextField lblNewLabel_1;
	JPanel panel;
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
	
	public boolean mining;
	JTextArea miningstats;
	
	public boolean modeToggleStatus;
	
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
		miningstats.setBorder(null); //remove the border
		miningstats.setBounds(10, 64, 205, 190);
		miningstats.setLineWrap(true);
		
		frame = new JFrame();
		try {
			frame.setIconImage(ImageIO.read(Monieo.class.getClassLoader().getResourceAsStream("icon.png")));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		frame.setTitle("MonieO Client Version " + Monieo.VERSION);
		frame.setBounds(100, 100, 901, 485);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		TgBtnTOGGLEMINING = new JButton("Off");
		TgBtnTOGGLEMINING.setBounds(799, 417, 76, 23);
		TgBtnTOGGLEMINING.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				JDialog jd = new JDialog(frame);
				
				jd.setTitle("Mining " + Monieo.VERSION);
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
							text = "Off";
							
						}
						
						btnNewButton.setText(text);
						TgBtnTOGGLEMINING.setText(text);
						
					}
					
				});
				
				jd.setVisible(true);
				
			}
			
		});

		frame.getContentPane().add(TgBtnTOGGLEMINING);
			
		lblNewLabel_1 = new JTextField("Address count:");
		lblNewLabel_1.setEditable(false);
		lblNewLabel_1.setBorder(null); //remove the border
		lblNewLabel_1.setBounds(208, 411, 109, 34);
		frame.getContentPane().add(lblNewLabel_1);
		
		panel = new JPanel();
		panel.setLayout(null);
		panel.setBounds(196, 0, 689, 400);
		frame.getContentPane().add(panel);
		
		addressLabel = new JTextField("(address)");
		addressLabel.setEditable(false);
		addressLabel.setOpaque(false); //this is the same as a JLabel
		addressLabel.setBorder(null); //remove the border
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
		
		panel.add(addressLabel);
		
		nickLabel = new JTextField("(address nickname)");
		nickLabel.setEditable(false);
		nickLabel.setOpaque(false); //this is the same as a JLabel
		nickLabel.setBorder(null); //remove the border
		nickLabel.setBounds(144, 40, 331, 29);
		panel.add(nickLabel);
		
		INDIVbalanceLabel = new JTextField("(address balance)");
		INDIVbalanceLabel.setEditable(false);
		INDIVbalanceLabel.setOpaque(false); //this is the same as a JLabel
		INDIVbalanceLabel.setBorder(null); //remove the border
		INDIVbalanceLabel.setBounds(144, 70, 331, 29);
		panel.add(INDIVbalanceLabel);
		
		panelTransaction = new JPanel();
		panelTransaction.setBounds(10, 152, 663, 225);
		panel.add(panelTransaction);
		panelTransaction.setLayout(null);
		
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
						refresh(true);
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
		
		JPanel panel_1 = new JPanel();
		panel_1.setBounds(190, 0, 663, 225);
		frame.getContentPane().add(panel_1);
		panel_1.setLayout(null);
		panel_1.setVisible(false);
		
		JButton overviewBTN = new JButton("Overview");
		overviewBTN.setBounds(10, 414, 176, 23);
		overviewBTN.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel_1.setVisible(true);
                panel.setVisible(false);
			    }
		});
		frame.getContentPane().add(overviewBTN);
		
		list = new JList<String>();
		list.setBounds(10, 11, 188, 429);
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
	                panel.setVisible(true);
	                panel_1.setVisible(false);

					refresh(false);
				}
			}
			
		});
		
		scrollPane = new JScrollPane(list);
		scrollPane.setBounds(10, 11, 176, 396);
		scrollPane.createVerticalScrollBar();
        scrollPane.setLayout(new ScrollPaneLayout());
		frame.getContentPane().add(scrollPane);
		
		label_1 = new JLabel("Selected address:");
		label_1.setBounds(10, 10, 106, 29);
		panel.add(label_1);
		
		label_3 = new JLabel("Address nickname:");
		label_3.setBounds(10, 40, 133, 29);
		panel.add(label_3);
		
		label_5 = new JLabel("Address balance:");
		label_5.setBounds(10, 70, 133, 29);
		panel.add(label_5);
		
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
						
						refresh(false);
						
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
		textField_2.setBounds(84, 145, 491, 20);
		panelTransaction.add(textField_2);
		
		JLabel lblNewLabel_2 = new JLabel("Total pending funds:");
		lblNewLabel_2.setBounds(83, 11, 111, 14);
		panel_1.add(lblNewLabel_2);
		
		JLabel totPendingFundsDisplay = new JLabel("0");
		totPendingFundsDisplay.setBounds(209, 11, 74, 14);
		panel_1.add(totPendingFundsDisplay);
		
		JLabel lblTotalAvailableFunds = new JLabel("Total available funds:");
		lblTotalAvailableFunds.setBounds(83, 36, 111, 14);
		panel_1.add(lblTotalAvailableFunds);
		
		totAvailableFundsDisplay = new JLabel("0");
		totAvailableFundsDisplay.setBounds(203, 36, 80, 14);
		panel_1.add(totAvailableFundsDisplay);
		
		JLabel lblTotalConnectedNodes = new JLabel("Total connected nodes:");
		lblTotalConnectedNodes.setBounds(83, 63, 137, 14);
		panel_1.add(lblTotalConnectedNodes);
		
		JLabel totConnectedNodesDisplay = new JLabel("0");
		totConnectedNodesDisplay.setBounds(236, 63, 92, 14);
		panel_1.add(totConnectedNodesDisplay);
		
		panel_1.setVisible(false);

		JButton btnNewButton = new JButton("...");
		btnNewButton.putClientProperty("JButton.buttonType", "roundRect");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

			}
		});
		
		btnNewButton.setBounds(663, 3, 23, 23);
		panel.add(btnNewButton);
		
		JLabel label_5_1 = new JLabel("Pending funds:");
		label_5_1.setBounds(10, 100, 133, 29);
		panel.add(label_5_1);
		
		JButton btnNewButton_1 = new JButton("Click to view");
		btnNewButton_1.setBounds(144, 102, 116, 23);
		panel.add(btnNewButton_1);
		
		BtnNEWADDRESS = new JButton("New address");
		BtnNEWADDRESS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    Object result = JOptionPane.showInputDialog(frame, "Enter new address nickname:");
			    if (result!= null) {
			    	
			    	String resp = Monieo.INSTANCE.createWallet(result.toString());
			    	
			    	JOptionPane.showMessageDialog(frame, resp, "Info", 1);
			    	
			    	refresh(true);
			    }
			}
		});
		BtnNEWADDRESS.setBounds(321, 417, 109, 23);
		frame.getContentPane().add(BtnNEWADDRESS);
		
		lblToggleExperimentalMining = new JLabel("Mining:");
		lblToggleExperimentalMining.setBounds(756, 414, 76, 29);
		frame.getContentPane().add(lblToggleExperimentalMining);
		
		lblTotalBalancelabel = new JLabel("Total balance:");
		lblTotalBalancelabel.setBounds(440, 406, 231, 44);
		frame.getContentPane().add(lblTotalBalancelabel);
		
		frame.setResizable(false);
		refresh(true);
		frame.setVisible(true);
	}
	
	public void refresh(boolean updlist) {
		lblTotalBalancelabel.setText("fat");

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
    		
    	} else {
    		
    		for (String s : walletNicks) {
    			
    			Wallet w = Monieo.INSTANCE.getWalletByNick(s);
    			
    			BigDecimal n = BlockMetadata.getSpendableBalance(m.getWalletData(w.getAsString()).pf).setScale(8);
    			
    			totAvailableFundsDisplay.setText(n.toPlainString());
    			
    			
    			if (s.equals(list.getSelectedValue())) {
    				//pushy
    	    		addressLabel.setText(w.getAsString());
    	    		nickLabel.setText(w.nickname);
    				INDIVbalanceLabel.setText(n.toPlainString());
    				
    			}
    			
    		}

    		panel.setVisible(true);
    		
    	}
    	
		BigDecimal tot = BigDecimal.ZERO.setScale(8);
		
		for (String s : walletNicks) {
			
			Wallet w = Monieo.INSTANCE.getWalletByNick(s);
			
			BigDecimal n = BlockMetadata.getSpendableBalance(m.getWalletData(w.getAsString()).pf).setScale(8);
			
			tot = tot.add(n);
			
		}
		
		lblTotalBalancelabel.setText("Total balance: " + tot.toPlainString());
		
	}
	
	public void notifyInvalid() {
		
		JOptionPane.showMessageDialog(frame, "Invalid data entered!", "Error", 0);
		
	}
}