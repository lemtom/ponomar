package net.ponomar;

import net.ponomar.parsing.DocHandler;
import net.ponomar.parsing.QDParser;
import net.ponomar.utility.Constants;
import net.ponomar.utility.OrderedHashtable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Hashtable;

/***************************************************************
ConfigurationFiles.java is part of the Ponomar project.
Copyright 2008, 2013 Yuri Shardt
version 1.0: August 2008
version 2.0: January 2013
yuri.shardt (at) gmail.com

 PERMISSION IS HEREBY GRANTED TO USE, MODIFY, AND/OR REDISTRIBUTE THIS SOURCE CODE
 PROVIDED THAT THIS NOTICE REMAINS IN ALL VERSION AND / OR DERIVATIVES THEREOF.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 THE AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT
 OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
**************************************************************/

/**
 * 
 * Module that reads and updates the configuration files.
 * 
 * @author Yuri Shardt
 * @version 2.0: January 2013
 * 
 */
public class ConfigurationFiles implements DocHandler
{
	private static OrderedHashtable Defaults;		//STORES THE START-UP VALUES FOR PONOMAR
	
	//THIS ALLOWS THE PONOMAR CONFIGURAITON FILE TO BE MAINTAINED, UPDATED, AND READ.
	public ConfigurationFiles()
	{
	
	}
	
	public static void ReadFile()
	{
		
		try
		{
			//FileReader frf = new FileReader("Constants.CONFIG_FILE)");
			BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(Constants.CONFIG_FILE), StandardCharsets.UTF_8));
			//OutputStreamWriter out = new OutputStreamWriter(new ByteArrayOutputStream());
			//System.out.println(out.getEncoding());

			ConfigurationFiles a1 =new ConfigurationFiles();
			QDParser.parse(a1, frf);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		//return Defaults;
	}
	
	public static void WriteFile()
	{
		StringBuilder output;
				
		try
		{
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.CONFIG_FILE), StandardCharsets.UTF_8));
			//BufferedWriter out = new BufferedWriter(new FileWriter("Constants.CONFIG_FILE)"));//,"UT8");
			out.write("<CONFIGURATION>");
			out.newLine();
			output = new StringBuilder("<DEFAULT ");
			for(Enumeration e=getDefaults().keys(); e.hasMoreElements();)
			{
				String key = (String) e.nextElement();
				String value=(String) getDefaults().get(key);
				output.append(key).append(" = \"").append(value).append("\" ");
			}
			output.append(" />");
			out.write(output.toString());
			out.newLine();
			out.write("</CONFIGURATION>");
			out.close();
		}
		catch(IOException e)
		{
			//CANNOT BE MULTILINGUAL
			System.out.println("There was a problem:" + e);
		}
	}
	
	
	public void startDocument() { }

	public void endDocument() { }

	public void startElement(String elem, Hashtable table)
	{
		
		if (elem.equals("DEFAULT"))
		{
			for(Enumeration e=table.keys(); e.hasMoreElements();)
			{
				
				String entry=(String) e.nextElement();
				String value = (String) table.get(entry);
				
				if(value != null && entry != null)
				{
					getDefaults().put(entry,value);
				}
			}
			
		}		
		return;
	}
	
	public void endElement(String elem) { }

	public void text(String text) { }

	public static OrderedHashtable getDefaults() {
		return Defaults;
	}

	public static void setDefaults(OrderedHashtable defaults) {
		Defaults = defaults;
	}
}