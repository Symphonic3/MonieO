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
import java.awt.Component;
import javax.swing.Box;
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
		frame.setBounds(100, 100, 764, 485);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JToggleButton tglbtnNewToggleButton = new JToggleButton("Off");
		tglbtnNewToggleButton.setBounds(638, 11, 75, 23);
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
		panel.setBounds(0, 0, 206, 456);
		frame.getContentPane().add(panel);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBounds(208, 0, 550, 352);
		frame.getContentPane().add(panel_1);
		
		d it)");
		lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_2.setBounds(502, 296, 236, 24);
		frame.getContentPane().add(lblNewLabel_2);
		
		frame.setVisible(true);
		frame.setResizable(false);
		
	}
}