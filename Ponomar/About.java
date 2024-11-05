package Ponomar;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/********************************************************************
 * THIS CLASS CREATES THE ABOUT BOX THAT APPEARS IN THE WINDOW.
 * 
 * Copyright 2008 Yuri Shardt version 1.0: August 2008 yuri (dot) shardt (at)
 * gmail.com
 * 
 * PERMISSION IS HEREBY GRANTED TO USE, MODIFY, AND/OR REDISTRIBUTE THIS SOURCE
 * CODE PROVIDED THAT THIS NOTICE REMAINS IN ALL VERSION AND / OR DERIVATIVES
 * THEREOF.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 **********************************************************************/
class About extends JFrame {
	private final LanguagePack ponomar;// =new LanguagePack();
	private final String value;// =(String)Ponomar.Phrases.get("0");
	private final StringOp analyse = new StringOp();

	protected About(Map<Object, Object> dayInfo) {
		analyse.dayInfo = dayInfo;
		ponomar = new LanguagePack(dayInfo);

		value = ponomar.Phrases.get("0");
		// ALLOWS A DIFFERENT TITLE TO BE SPECIFIED BY THE USER (CYRILLIC FOR THE
		// CYRILLIC VERSIONS)
		setTitle(value);
		LanguagePack Text = new LanguagePack(dayInfo);
		String[] aboutNames = Text.obtainValues(Text.Phrases.get("About"));
		Helpers about1 = new Helpers(analyse.dayInfo);
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setOpaque(true);
		JTextPane output = new JTextPane();
		output.setEditable(false);
		output.setContentType("text/html");
		String displayFont = analyse.dayInfo.get("FontFaceM").toString();
		String displaySize = analyse.dayInfo.get("FontSizeM").toString();
		output.setText("<body style=\"font-family:" + displayFont + ";font-size:" + displaySize
				+ "\"><B><h1 style=\"text-align: center;\">" + ponomar.Phrases.get("0")
				+ "</h1></B><p style=\"text-align: center;\">" + aboutNames[0] + " "
				+ ConfigurationFiles.Defaults.get("Version") + "</p><p>" + aboutNames[1] + "</p><p>" + aboutNames[2]
				+ "</p><p>" + about1.getCopyright() + "<BR>" + aboutNames[5] + "<BR>");
		output.setCaretPosition(0);
		JScrollPane scrollPane = new JScrollPane(output);
		contentPane.add(scrollPane, BorderLayout.CENTER);
		setContentPane(contentPane);
		pack();
		setSize(500, 400);
		// CENTRES THE FRAME
		setLocationRelativeTo(null);
		// ALLOWS US TO ADD OUR OWN IMAGES IN THE TOP OF THE WINDOW
		// setIconImage(new ImageIcon(imgURL).getImage());
		setVisible(true);
	}
}
