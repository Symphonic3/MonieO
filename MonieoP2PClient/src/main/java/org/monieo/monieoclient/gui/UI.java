 package org.monieo.monieoclient.gui;

import java.awt.Color;
import java.awt.Font;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.monieo.monieoclient.Monieo;
import org.monieo.monieoclient.blockchain.BlockMetadata;
import org.monieo.monieoclient.blockchain.Transaction;
import org.monieo.monieoclient.blockchain.WalletAdress;
import org.monieo.monieoclient.mining.AbstractMiner.MiningStatistics;
import org.monieo.monieoclient.wallet.Wallet;
import javax.swing.JTextField;

public class UI {
	private JFrame frame;
	
	private String[] walletNicks;
	
	public JList<String> list;
	public JLabel LblADDRESSES;
	public JButton btnChangeWalName;
	public JButton btnDelWal;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	
	JTextField lblNewLabel_1;
	JPanel panel_1;
	JPanel panel;
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
	JLabel lbltotalBalance;
	JButton TgBtnTOGGLEMINING;
	
	public boolean mining;
	JTextArea miningstats;
	
	public boolean modeToggleStatus;
	
	public UI() {
	}

	
	 /**
	  * @wbp.parser.entryPoint
	  */
	public void initialize() {
		
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
		frame.setTitle("Monieo Client Version " + Monieo.VERSION);
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
			
		lblNewLabel_1 = new JTextField("Total addresses:");
		lblNewLabel_1.setEditable(false);
		lblNewLabel_1.setOpaque(false); //this is the same as a JLabel
		lblNewLabel_1.setBorder(null); //remove the border
		lblNewLabel_1.setBounds(208, 411, 96, 34);
		frame.getContentPane().add(lblNewLabel_1);
		
		panel_1 = new JPanel();
		panel_1.setLayout(null);
		panel_1.setBounds(208, 0, 702, 400);
		frame.getContentPane().add(panel_1);
		
		addressLabel = new JTextField("(address)");
		addressLabel.setEditable(false);
		addressLabel.setOpaque(false); //this is the same as a JLabel
		addressLabel.setBorder(null); //remove the border
		addressLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
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
		
		panel_1.add(addressLabel);
		
		nickLabel = new JTextField("(address nickname)");
		nickLabel.setEditable(false);
		nickLabel.setOpaque(false); //this is the same as a JLabel
		nickLabel.setBorder(null); //remove the border
		nickLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
		nickLabel.setBounds(144, 40, 331, 29);
		panel_1.add(nickLabel);
		
		INDIVbalanceLabel = new JTextField("(address balance)");
		INDIVbalanceLabel.setEditable(false);
		INDIVbalanceLabel.setOpaque(false); //this is the same as a JLabel
		INDIVbalanceLabel.setBorder(null); //remove the border
		INDIVbalanceLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
		INDIVbalanceLabel.setBounds(144, 70, 331, 29);
		panel_1.add(INDIVbalanceLabel);
		
		panel = new JPanel();
		panel.setBounds(10, 152, 663, 225);
		panel_1.add(panel);
		panel.setLayout(null);
		
		btnChangeWalName = new JButton("Change wallet name");
		btnChangeWalName.setBounds(480, 60, 160, 29);
		panel_1.add(btnChangeWalName);
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
		btnChangeWalName.setVisible(false);
		
		btnDelWal = new JButton("Delete selected wallet");
		btnDelWal.setBounds(480, 97, 160, 29);
		panel_1.add(btnDelWal);
		btnDelWal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Wallet walletInQuestion = Monieo.INSTANCE.getWalletByNick(list.getSelectedValue());
				if (list.getSelectedValue() != null) {
					String confirmation = JOptionPane.showInputDialog(frame, "Enter wallet nickname name for \"" + list.getSelectedValue() + "\" to confirm deletion:");
					if (confirmation.equals(walletInQuestion.nickname)) {
						Monieo.INSTANCE.deleteWallet(Monieo.INSTANCE.getWalletByNick(list.getSelectedValue()));
						refresh(true);
					}
				}
			}
		});
		btnDelWal.setVisible(false);
		
		LblADDRESSES = new JLabel("(addresses #)");
		LblADDRESSES.setBounds(304, 411, 86, 34);
		frame.getContentPane().add(LblADDRESSES);
		
		list = new JList<String>();
		list.setBounds(10, 11, 188, 429);
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) refresh(false);
			}
			
		});
		
		scrollPane = new JScrollPane(list);
		scrollPane.setBounds(10, 11, 186, 424);
		scrollPane.createVerticalScrollBar();
        scrollPane.setLayout(new ScrollPaneLayout());
		frame.getContentPane().add(scrollPane);
		
		label_1 = new JLabel("Selected address:");
		label_1.setFont(new Font("Tahoma", Font.PLAIN, 13));
		label_1.setBounds(10, 10, 106, 29);
		panel_1.add(label_1);
		
		label_3 = new JLabel("Address nickname:");
		label_3.setFont(new Font("Tahoma", Font.PLAIN, 13));
		label_3.setBounds(10, 40, 133, 29);
		panel_1.add(label_3);
		
		label_5 = new JLabel("Address balance:");
		label_5.setFont(new Font("Tahoma", Font.PLAIN, 13));
		label_5.setBounds(10, 70, 133, 29);
		panel_1.add(label_5);
		
		//what is a trnt and why is it already sent
		sentTrnt = new JButton("Send transaction");
		sentTrnt.setBounds(84, 191, 144, 23);
		panel.add(sentTrnt);
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

					Transaction newTransaction = Transaction.createNewTransaction(selectedWal, new WalletAdress(textField.getText()), new BigDecimal(textField_1.getText()), new BigDecimal(textField_2.getText()));
					
					if (newTransaction == null || !newTransaction.validate()) {

						notifyInvalid();
						return;
						
					}

					System.out.println("r");
					
					String res = JOptionPane.showInputDialog(frame, 
							"WARNING: YOU ARE ABOUT TO SEND A MONIEO TRANSACTION.\nTHIS ACTION IS IRREVERSIBLE AND CANNOT BE UNDONE.\n\n"
							+ "To: " + newTransaction.d.to.adress
							+ "\nAmount: " + newTransaction.d.amount
							+ "\nFee: " + newTransaction.d.fee
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
		panel.add(textField);
		textField.setColumns(10);
		
		lblNewLabel = new JLabel("Recipient address:");
		lblNewLabel.setBounds(84, 11, 117, 14);
		panel.add(lblNewLabel);
		
		lblTransactionAmount = new JLabel("Transaction amount:");
		lblTransactionAmount.setBounds(84, 67, 117, 14);
		panel.add(lblTransactionAmount);
		
		textField_1 = new JTextField();
		textField_1.setColumns(10);
		textField_1.setBounds(84, 92, 491, 20);
		panel.add(textField_1);
		
		lblFee = new JLabel("Fee:");
		lblFee.setBounds(84, 120, 117, 14);
		panel.add(lblFee);
		
		textField_2 = new JTextField();
		textField_2.setColumns(10);
		textField_2.setBounds(84, 145, 491, 20);
		panel.add(textField_2);
		
		JButton btnNewButton = new JButton("New button");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				toggleDarkMode();
			}
		});
		
		btnNewButton.setBounds(650, 4, 23, 23);
		panel_1.add(btnNewButton);
		
		JLabel label_5_1 = new JLabel("Pending funds:");
		label_5_1.setForeground(Color.BLACK);
		label_5_1.setFont(new Font("Tahoma", Font.PLAIN, 13));
		label_5_1.setBounds(10, 100, 133, 29);
		panel_1.add(label_5_1);
		
		JButton btnNewButton_1 = new JButton("Click to view");
		btnNewButton_1.setBounds(144, 102, 116, 23);
		panel_1.add(btnNewButton_1);
		
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
		
		lblTotalBalancelabel.setBounds(440, 406, 86, 44);
		frame.getContentPane().add(lblTotalBalancelabel);
		
		lbltotalBalance = new JLabel("(total balance)");
		lbltotalBalance.setBounds(526, 406, 109, 44);
		frame.getContentPane().add(lbltotalBalance);
				
		frame.setResizable(false);
		
		panel.setVisible(false);
		
		setColors(new Color(222, 222, 222), new Color(255, 255, 255), new Color(0, 0, 0));
		
		refresh(true);
		
		frame.setVisible(true);
		
	}
	
	public void refresh(boolean updlist) {
		
    	if (updlist) {
    		
    		walletNicks = new String[Monieo.INSTANCE.myWallets.size()];
        	for (int i = 0; i < walletNicks.length; i++) {
        		
        		walletNicks[i] = Monieo.INSTANCE.myWallets.get(i).nickname;
        		
        	}
        	list.setListData(walletNicks);
        	LblADDRESSES.setText(Integer.toString(walletNicks.length));
    		
    	}
    	
    	if (list.getSelectedIndex() == -1) {
    		
    		btnDelWal.setVisible(false);
    		btnChangeWalName.setVisible(false);
    		panel.setVisible(false);
    		
    	} else {
    		
    		BlockMetadata m = Monieo.INSTANCE.getHighestBlock().getMetadata();
    		
    		BigDecimal tot = BigDecimal.ZERO;
    		
    		for (String s : walletNicks) {
    			
    			Wallet w = Monieo.INSTANCE.getWalletByNick(s);
    			
    			BigDecimal n = BlockMetadata.getSpendableBalance(m.getFullTransactions(w.getAsWalletAdress()));
    			
    			tot = tot.add(n);
    			
    			if (s.equals(list.getSelectedValue())) {
    				
    	    		addressLabel.setText(w.getAsWalletAdress().adress);
    	    		nickLabel.setText(w.nickname);
    				INDIVbalanceLabel.setText(n.toPlainString());
    				
    			}
    			
    		}
    		
    		lbltotalBalance.setText(tot.toPlainString());
    		
    		btnChangeWalName.setVisible(true);
    		btnDelWal.setVisible(true);
    		panel.setVisible(true);
    		
    	}


	}
	
	public void notifyInvalid() {
		
		JOptionPane.showMessageDialog(frame, "Invalid data entered!", "Error", 0);
		
	}
	
	public void toggleDarkMode() {
		if (!modeToggleStatus) {
			setColors(new Color(54,57,63), new Color(64,68,75), new Color(255, 255, 255));
		} else {
            setColors(new Color(222, 222, 222), new Color(255, 255, 255), new Color(0, 0, 0));
		}
		modeToggleStatus = !modeToggleStatus;
	}
	
	void setColors(Color main, Color highlight, Color text) {
		
		/*//main components
		frame.getContentPane().setBackground(main);
		panel.setBackground(main);
		panel_1.setBackground(main);
		scrollPane.setBackground(highlight);
		list.setBackground(highlight);
		list.setForeground(text);
		
		//text labels
		lblNewLabel.setForeground(text);
		LblADDRESSES.setForeground(text);
		lblFee.setForeground(text);
		lblNewLabel_1.setForeground(text);
		lblTotalBalancelabel.setForeground(text);
		lblToggleExperimentalMining.setForeground(text);
		lblTransactionAmount.setForeground(text);
		label_1.setForeground(text);
		label_3.setForeground(text);
		label_5.setForeground(text);
		addressLabel.setForeground(text);
		INDIVbalanceLabel.setForeground(text);
		nickLabel.setForeground(text);
		lbltotalBalance.setForeground(text);

		//things
		textField.setBackground(highlight);
		textField_1.setBackground(highlight);
		textField_2.setBackground(highlight);
		textField.setForeground(text);
		textField_1.setForeground(text);
		textField_2.setForeground(text);
		
		//buttoibns
		sentTrnt.setBackground(highlight);
		sentTrnt.setForeground(text);
		
		btnChangeWalName.setBackground(highlight);
		btnChangeWalName.setForeground(text);
		
		btnDelWal.setBackground(highlight);
		btnDelWal.setForeground(text);
		
		BtnNEWADDRESS.setBackground(highlight);
		BtnNEWADDRESS.setForeground(text);
		
		TgBtnTOGGLEMINING.setBackground(highlight);
		TgBtnTOGGLEMINING.setForeground(text);
		
		scrollPane.getVerticalScrollBar().setBackground(highlight);
		scrollPane.getVerticalScrollBar().setForeground(text);
		
		/*scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
			
			@Override
			protected void configureScrollBarColors() {
				this.trackColor = main;
				this.thumbDarkShadowColor = highlight;
				this.thumbHighlightColor = main;
			}
		});
		
		scrollPane.getHorizontalScrollBar().setUI(new BasicScrollBarUI() {
			
			@Override
			protected void configureScrollBarColors() {
				this.trackColor = main;
				this.thumbDarkShadowColor = highlight;
				this.thumbHighlightColor = main;
			}
		});*/
		
		
		
		
		
		frame.repaint();
	}
}