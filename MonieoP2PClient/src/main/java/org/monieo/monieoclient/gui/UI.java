package org.monieo.monieoclient.gui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import org.monieo.monieoclient.Monieo;

public class UI {
	private JFrame frame;
	private JTextField textField;
	private JTextField textField_1;

	public UI() {
	}

	public void initialize() {
		frame = new JFrame();
		try {
			frame.setIconImage(ImageIO.read(Monieo.class.getClassLoader().getResourceAsStream("icon.png")));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		frame.setTitle("Monieo Client Version " + Monieo.version);
		frame.setBounds(100, 100, 764, 485);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblNewLabel = new JLabel("v" + Monieo.version);
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblNewLabel.setBounds(10, 414, 119, 21);
		frame.getContentPane().add(lblNewLabel);
		
		JButton btnNewButton = new JButton("Advanced");
		btnNewButton.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnNewButton.setBounds(649, 414, 89, 24);
		frame.getContentPane().add(btnNewButton);
		
		JLabel lblNewLabel_1 = new JLabel("(address or something idk never watched it)");
		lblNewLabel_1.setBounds(10, 38, 461, 24);
		frame.getContentPane().add(lblNewLabel_1);
		
		JLabel label = new JLabel("Address:");
		label.setFont(new Font("Tahoma", Font.BOLD, 11));
		label.setBounds(10, 3, 484, 24);
		frame.getContentPane().add(label);
		
		JLabel label_1 = new JLabel("Balance:");
		label_1.setFont(new Font("Tahoma", Font.BOLD, 11));
		label_1.setBounds(10, 70, 216, 24);
		frame.getContentPane().add(label_1);
		
		JLabel label_2 = new JLabel("0.0");
		label_2.setBounds(10, 105, 216, 24);
		frame.getContentPane().add(label_2);
		
		JLabel label_3 = new JLabel("Transfer:");
		label_3.setFont(new Font("Tahoma", Font.BOLD, 11));
		label_3.setBounds(10, 128, 484, 24);
		frame.getContentPane().add(label_3);
		
		textField = new JTextField();
		textField.setBounds(10, 179, 461, 52);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		JLabel label_4 = new JLabel("Address to transfer to:");
		label_4.setFont(new Font("Tahoma", Font.PLAIN, 11));
		label_4.setBounds(10, 157, 484, 24);
		frame.getContentPane().add(label_4);
		
		JLabel label_5 = new JLabel("Amount to transfer:");
		label_5.setHorizontalAlignment(SwingConstants.CENTER);
		label_5.setFont(new Font("Tahoma", Font.PLAIN, 11));
		label_5.setBounds(180, 242, 137, 24);
		frame.getContentPane().add(label_5);
		
		textField_1 = new JTextField();
		textField_1.setColumns(10);
		textField_1.setBounds(180, 264, 137, 21);
		frame.getContentPane().add(textField_1);
		
		JButton btnTransfer = new JButton("Transfer");
		btnTransfer.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnTransfer.setBounds(205, 296, 89, 24);
		frame.getContentPane().add(btnTransfer);
		
		JLabel lblMining = new JLabel("Mining:");
		lblMining.setHorizontalAlignment(SwingConstants.CENTER);
		lblMining.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 11));
		lblMining.setBounds(529, 3, 185, 24);
		frame.getContentPane().add(lblMining);
		
		JToggleButton tglbtnNewToggleButton = new JToggleButton("Off");
		tglbtnNewToggleButton.setBounds(580, 71, 75, 23);
		tglbtnNewToggleButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				if (tglbtnNewToggleButton.isSelected()) {
					tglbtnNewToggleButton.setText("On");
				} else {
					tglbtnNewToggleButton.setText("Off");
				}
			}
		});
		frame.getContentPane().add(tglbtnNewToggleButton);
		
		JLabel lblMiningEnabled = new JLabel("Mining enabled:");
		lblMiningEnabled.setHorizontalAlignment(SwingConstants.CENTER);
		lblMiningEnabled.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblMiningEnabled.setBounds(529, 38, 185, 24);
		frame.getContentPane().add(lblMiningEnabled);
		
		JLabel lblMiningInformation = new JLabel("Mining Statistics:");
		lblMiningInformation.setHorizontalAlignment(SwingConstants.CENTER);
		lblMiningInformation.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 11));
		lblMiningInformation.setBounds(529, 105, 185, 24);
		frame.getContentPane().add(lblMiningInformation);
		
		JLabel label_8 = new JLabel("Mining Live View:");
		label_8.setHorizontalAlignment(SwingConstants.CENTER);
		label_8.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 11));
		label_8.setBounds(529, 267, 185, 24);
		frame.getContentPane().add(label_8);
		
		JLabel lblNewLabel_2 = new JLabel("(the hash or someone idk never watched it)");
		lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_2.setBounds(502, 296, 236, 24);
		frame.getContentPane().add(lblNewLabel_2);
		
		frame.setVisible(true);
		frame.setResizable(false);
		
	}
}