 package org.monieo.monieoclient.gui;

import java.awt.Font;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Console;
import java.io.IOException;
import java.math.BigDecimal;
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
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.monieo.monieoclient.Monieo;
import org.monieo.monieoclient.wallet.Wallet;

public class UI {
	private JFrame frame;
	private final JPanel panel = new JPanel();
	
	private String[] walletNicks;
	
	public JList<String> list;
	
	public JLabel addressLabel;
	
	public JLabel LblADDRESSES;
	
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
	    
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 11, 186, 434);
		
		panel.add(scrollPane);			
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
		
		LblADDRESSES = new JLabel("(addresses #)");
		LblADDRESSES.setBounds(304, 411, 86, 34);
		frame.getContentPane().add(LblADDRESSES);
		
		list = new JList<String>();
		list.setBounds(10, 11, 188, 429);
		frame.getContentPane().add(list);
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				Wallet selectedWal = Monieo.INSTANCE.getWalletByNick(list.getSelectedValue());
				addressLabel.setText(selectedWal.address);
				nickLabel.setText(selectedWal.nickname);
				//TODO DO BALANCE TAKE TAKE AND HHAVE THE BALANCE
				//INDIVbalanceLabel.setText( selectedWal); stage changed
			}
		});
		
		Refresh();
		
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
		
		JButton BtnNEWADDRESS = new JButton("New address");
		BtnNEWADDRESS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    Object result = JOptionPane.showInputDialog(frame, "Enter new address nickname:");
			    if (result.toString() != null) {
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
		lblTotalBalance.setBounds(758, 406, 76, 44);
		frame.getContentPane().add(lblTotalBalance);
		
		JLabel label = new JLabel("Total balance:");
		label.setBounds(842, 406, 53, 44);
		frame.getContentPane().add(label);
		
		frame.setVisible(true);
		frame.setResizable(false);
		
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