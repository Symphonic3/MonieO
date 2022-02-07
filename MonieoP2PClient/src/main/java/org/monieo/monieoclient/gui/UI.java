 package org.monieo.monieoclient.gui;

import java.awt.Font;
import java.awt.Panel;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.io.comparator.DirectoryFileComparator;
import org.monieo.monieoclient.Monieo;
import org.monieo.monieoclient.blockchain.Transaction;
import org.monieo.monieoclient.blockchain.WalletAdress;
import org.monieo.monieoclient.networking.NetworkCommand;
import org.monieo.monieoclient.networking.Node;
import org.monieo.monieoclient.networking.ServerConnectionHandler;
import org.monieo.monieoclient.networking.NetworkCommand.NetworkCommandType;
import org.monieo.monieoclient.networking.Node.PacketCommitment;
import org.monieo.monieoclient.wallet.Wallet;
import javax.swing.JTextField;

public class UI {
	private JFrame frame;
	
	private String[] walletNicks;
	
	public JList<String> list;
	public JLabel addressLabel;
	public JLabel LblADDRESSES;
	public JButton btnChangeWalName;
	public JButton btnDelWal;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	
	public UI() {
	}

	
	 /**
	  * @wbp.parser.entryPoint
	  */
	public void initialize() {
		
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
		
		JToggleButton TgBtnTOGGLEMINING = new JToggleButton("Off");
		TgBtnTOGGLEMINING.setBounds(672, 417, 76, 23);
		//TgBtnTOGGLEMINING.addActionListener(new ActionListener() {

		frame.getContentPane().add(TgBtnTOGGLEMINING);
			
		JLabel lblNewLabel_1 = new JLabel("Total addresses:");
		lblNewLabel_1.setBounds(208, 411, 96, 34);
		frame.getContentPane().add(lblNewLabel_1);
		
		JPanel panel_1 = new JPanel();
		panel_1.setLayout(null);
		panel_1.setBounds(208, 0, 702, 400);
		frame.getContentPane().add(panel_1);
		
		JLabel addressLabel = new JLabel("(address)");
		addressLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
		addressLabel.setBounds(144, 11, 529, 29);
		panel_1.add(addressLabel);
		
		JLabel nickLabel = new JLabel("(address nickname)");
		nickLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
		nickLabel.setBounds(144, 51, 377, 29);
		panel_1.add(nickLabel);
		
		JLabel INDIVbalanceLabel = new JLabel("(address balance)");
		INDIVbalanceLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
		INDIVbalanceLabel.setBounds(144, 91, 385, 29);
		panel_1.add(INDIVbalanceLabel);
		
		JPanel panel = new JPanel();
		panel.setBounds(10, 152, 663, 225);
		panel_1.add(panel);
		panel.setLayout(null);
		
		btnChangeWalName = new JButton("Change wallet name");
		btnChangeWalName.setBounds(480, 39, 160, 29);
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
						Refresh();
						btnDelWal.setVisible(false);
						btnChangeWalName.setVisible(false);
						panel.setVisible(false);
					}
				}
			}
		});
		btnChangeWalName.setVisible(false);
		
		btnDelWal = new JButton("Delete selected wallet");
		btnDelWal.setBounds(480, 76, 160, 29);
		panel_1.add(btnDelWal);
		btnDelWal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Wallet walletInQuestion = Monieo.INSTANCE.getWalletByNick(list.getSelectedValue());
				if (list.getSelectedValue() != null) {
					String confirmation = JOptionPane.showInputDialog(frame, "Enter wallet nickname name for \"" + list.getSelectedValue() + "\" to confirm deletion:");
					if (confirmation.equals(walletInQuestion.nickname)) {
						Monieo.INSTANCE.deleteWallet(Monieo.INSTANCE.getWalletByNick(list.getSelectedValue()));
						Refresh();
						btnDelWal.setVisible(false);
						btnChangeWalName.setVisible(false);
						panel.setVisible(false);
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
				try {
				Wallet selectedWal = Monieo.INSTANCE.getWalletByNick(list.getSelectedValue());
				addressLabel.setText(selectedWal.getAsWalletAdress().adress);
				nickLabel.setText(selectedWal.nickname);
				//TODO DO BALANCE TAKE TAKE AND HHAVE THE BALANCE
				//INDIVbalanceLabel.setText( selectedWal); stage changed
				} catch (Exception e2) {
					
				}
				btnChangeWalName.setVisible(true);
				btnDelWal.setVisible(true);
				panel.setVisible(true);
			}
		});
		
		Refresh();
		
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setBounds(10, 11, 186, 434);
		scrollPane.createVerticalScrollBar();
        scrollPane.setLayout(new ScrollPaneLayout());
		frame.getContentPane().add(scrollPane);
		
		JLabel label_1 = new JLabel("Selected address:");
		label_1.setFont(new Font("Tahoma", Font.PLAIN, 13));
		label_1.setBounds(10, 11, 106, 29);
		panel_1.add(label_1);
		
		JLabel label_3 = new JLabel("Address nickname:");
		label_3.setFont(new Font("Tahoma", Font.PLAIN, 13));
		label_3.setBounds(10, 51, 133, 29);
		panel_1.add(label_3);
		
		JLabel label_5 = new JLabel("Address balance:");
		label_5.setFont(new Font("Tahoma", Font.PLAIN, 13));
		label_5.setBounds(10, 91, 133, 29);
		panel_1.add(label_5);
		
		JButton sentTrnt = new JButton("Send transaction");
		sentTrnt.setBounds(84, 191, 123, 23);
		panel.add(sentTrnt);
		sentTrnt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Wallet selectedWal = Monieo.INSTANCE.getWalletByNick(list.getSelectedValue());
					Transaction newTransaction = Transaction.createNewTransaction(selectedWal, new WalletAdress(textField.getText()), new BigDecimal(textField_1.getText()), new BigDecimal(textField_2.getText()));
					NetworkCommand netCommand = new NetworkCommand(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkCommandType.SEND_TRANSACTION, newTransaction.serialize());
					for (Node node : Monieo.INSTANCE.nodes) {
						node.sendNetworkCommand(netCommand, /*what the frikp iis a packet commitment*/null);
					}
					
				} catch (Exception e2) {
					System.out.println("invalid data entered fatty");
					e2.printStackTrace();
				}
			}
		});
		
		textField = new JTextField();
		textField.setBounds(84, 36, 491, 20);
		panel.add(textField);
		textField.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("Recipient address:");
		lblNewLabel.setBounds(84, 11, 117, 14);
		panel.add(lblNewLabel);
		
		JLabel lblTransactionAmount = new JLabel("Transaction amount:");
		lblTransactionAmount.setBounds(84, 67, 117, 14);
		panel.add(lblTransactionAmount);
		
		textField_1 = new JTextField();
		textField_1.setColumns(10);
		textField_1.setBounds(84, 92, 491, 20);
		panel.add(textField_1);
		
		JLabel lblFee = new JLabel("Fee:");
		lblFee.setBounds(84, 120, 117, 14);
		panel.add(lblFee);
		
		textField_2 = new JTextField();
		textField_2.setColumns(10);
		textField_2.setBounds(84, 145, 491, 20);
		panel.add(textField_2);
		
		JButton BtnNEWADDRESS = new JButton("New address");
		BtnNEWADDRESS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    Object result = JOptionPane.showInputDialog(frame, "Enter new address nickname:");
			    if (result!= null) {
			    	String resp = Monieo.INSTANCE.createWallet(result.toString());
			    	JOptionPane.showMessageDialog(frame, resp, "Info", 1);
			    	
			    	Refresh();
			    }
			}
		});
		BtnNEWADDRESS.setBounds(341, 417, 109, 23);
		frame.getContentPane().add(BtnNEWADDRESS);
		
		JLabel lblToggleExperimentalMining = new JLabel("Toggle experimental mining (CPU only):");
		lblToggleExperimentalMining.setBounds(474, 406, 188, 445);
		frame.getContentPane().add(lblToggleExperimentalMining);
		
		JLabel lblTotalBalance = new JLabel("Total balance:");
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(10, 11, 188, 429);
		frame.getContentPane().add(scrollPane_1);
		
		lblTotalBalance.setBounds(758, 406, 76, 44);
		frame.getContentPane().add(lblTotalBalance);
		
		JLabel label = new JLabel("Total balance:");
		label.setBounds(842, 406, 53, 44);
		frame.getContentPane().add(label);
				
		frame.setVisible(true);
		frame.setResizable(false);
		
		panel.setVisible(false);
	}
	void Refresh() {
		
    	walletNicks = new String[Monieo.INSTANCE.myWallets.size()];
    	
    	for (int i = 0; i < walletNicks.length; i++) {
    		
    		walletNicks[i] = Monieo.INSTANCE.myWallets.get(i).nickname;
    		
    	}
    	list.setListData(walletNicks);
    	LblADDRESSES.setText(Integer.toString(walletNicks.length));
	}
}