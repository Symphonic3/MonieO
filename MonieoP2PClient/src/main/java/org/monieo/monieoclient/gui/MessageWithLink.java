package org.monieo.monieoclient.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class MessageWithLink extends JEditorPane {
	private static final long serialVersionUID = 1L;

	public MessageWithLink(String htmlBody) {
		super("text/html", "<html><body style=\"" + getStyle() + "\">" + htmlBody + "</body></html>");
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
					try {
						java.awt.Desktop.getDesktop().browse(e.getURL().toURI());
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (URISyntaxException e1) {
						e1.printStackTrace();
					}
					
					JDialog topFrame = (JDialog) SwingUtilities.getWindowAncestor(MessageWithLink.this.getRootPane());
					topFrame.dispatchEvent(new WindowEvent(topFrame, WindowEvent.WINDOW_CLOSING));

				}
			}
		});
		setEditable(false);
		setBorder(null);
	}

	static StringBuffer getStyle() {
		// for copying style
		JLabel label = new JLabel();
		Font font = label.getFont();
		Color color = label.getBackground();

		// create some css from the label's font
		StringBuffer style = new StringBuffer("font-family:" + font.getFamily() + ";");
		style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
		style.append("font-size:" + font.getSize() + "pt;");
		style.append("background-color: rgb("+color.getRed()+","+color.getGreen()+","+color.getBlue()+");");
		return style;
	}
}