 package org.monieo.monieoclient.gui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import org.monieo.monieoclient.Monieo;
import javax.swing.JPanel;

public class UI {
	private JFrame frame;
	private final JPanel panel = new JPanel();

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
		frame.setTitle("Monieo Client Version " + Monieo.version);
		frame.setBounds(100, 100, 901, 485);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JToggleButton TgBtnTOGGLEMINING = new JToggleButton("Off");
		TgBtnTOGGLEMINING.setBounds(672, 417, 76, 23);
		TgBtnTOGGLEMINING.addActionListener(new ActionListener() {
			//push
			@Override
			public void actionPerformed(ActionEvent e) {
				
				if (TgBtnTOGGLEMINING.isSelected()) {
					TgBtnTOGGLEMINING.setText("On");
				} else {
					TgBtnTOGGLEMINING.setText("Off");
				}
			}
		});
		frame.getContentPane().add(TgBtnTOGGLEMINING);
		panel.setBounds(0, 0, 206, 456);
		frame.getContentPane().add(panel);
		
			
		JLabel lblNewLabel_1 = new JLabel("Total addresses:");
		lblNewLabel_1.setBounds(218, 411, 86, 34);
		frame.getContentPane().add(lblNewLabel_1);
		JPanel panel_1 = new JPanel();
		panel_1.setLayout(null);
		panel_1.setBounds(208, 0, 702, 400);
		frame.getContentPane().add(panel_1);
		
		JLabel label_1 = new JLabel("Current address:");
		label_1.setFont(new Font("Tahoma", Font.PLAIN, 13));
		label_1.setBounds(10, 11, 106, 29);
		panel_1.add(label_1);
		
		JLabel label_2 = new JLabel("(address)");
		label_2.setFont(new Font("Tahoma", Font.PLAIN, 13));
		label_2.setBounds(130, 11, 410, 29);
		panel_1.add(label_2);
		
		JLabel label_3 = new JLabel("Address nickname:");
		label_3.setFont(new Font("Tahoma", Font.PLAIN, 13));
		label_3.setBounds(10, 51, 133, 29);
		panel_1.add(label_3);
		
		JLabel label_4 = new JLabel("(address nickname)");
		label_4.setFont(new Font("Tahoma", Font.PLAIN, 13));
		label_4.setBounds(163, 51, 377, 29);
		panel_1.add(label_4);
		
		JLabel label_5 = new JLabel("Address balance:");
		label_5.setFont(new Font("Tahoma", Font.PLAIN, 13));
		label_5.setBounds(10, 91, 133, 29);
		panel_1.add(label_5);
		
		JLabel label_6 = new JLabel("(address balance)");
		label_6.setFont(new Font("Tahoma", Font.PLAIN, 13));
		label_6.setBounds(155, 91, 385, 29);
		panel_1.add(label_6);
		
		JButton button = new JButton("Delete address");
		button.setBounds(10, 347, 144, 42);
		panel_1.add(button);
		
		JLabel LblADDRESSES = new JLabel("(addresses #)");
		LblADDRESSES.setBounds(304, 411, 86, 34);
		frame.getContentPane().add(LblADDRESSES);
		
		JButton BtnNEWADDRESS = new JButton("New address");
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
}