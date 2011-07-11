package edu.albany.ils.dsarmd0200.util;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class FileSelector 
{
	public static String FileSelection()
	{
		String filepath = "";
		String filename = "";
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int result = chooser.showOpenDialog(null);
		
		if(result == JFileChooser.CANCEL_OPTION)
		{
			return null;
		}
		
		File fileChoose = chooser.getSelectedFile();
		if(fileChoose == null)
		{
			JOptionPane.showMessageDialog(null, "Invalid file");
			return null;
		}
		
		filepath = fileChoose.getAbsolutePath();
		filename = fileChoose.getName();
		
		//Ensure File type
		//int last = filepath.lastIndexOf(".");
		//String lastStr = filepath.substring(last+1, last+4);
		//if(!lastStr.equals("xml"))
		//{
		//	JOptionPane.showMessageDialog(null, "Invalid file type, must be xml file");
		//	return null;
		//}
		return filepath;
	}
}
