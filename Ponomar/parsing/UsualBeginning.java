package Ponomar.parsing;

import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import javax.swing.event.*;

import Ponomar.utility.OrderedHashtable;
import Ponomar.utility.StringOp;

import java.awt.event.*;
import java.beans.*;


/***********************************************************************
THIS MODULE CREATES THE TEXT FOR THE ORTHODOX USUAL BEGINNING OF A SERVICE
THIS MODULE IS STILL IN THE DEVELOPMENT PHASE.

(C) 2008 YURI SHARDT. ALL RIGHTS RESERVED.
Updated some parts to make it compatible with the changes in Ponomar, especially the language issues!

 PERMISSION IS HEREBY GRANTED TO USE, MODIFY, AND/OR REDISTRIBUTE THIS SOURCE CODE
 PROVIDED THAT THIS NOTICE REMAINS IN ALL VERSION AND / OR DERIVATIVES THEREOF.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 THE AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT
 OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
***********************************************************************/
public class UsualBeginning
{
	//SOME QUICK NOTES FOR FURTHER IMPLEMENTATION:
	//THE DATE OR THE RELEVANT INFORMATION WILL HAVE TO BE GIVEN
	//TO THE PROGRAMME. AT PRESENT IT WILL BE ASSUMED THAT IT IS TONE 1
	//DURING THE COURSE OF A SINGLE WEEK.
	public static String usualBeginning1;
        private static StringOp analyse=new StringOp();
	public UsualBeginning(int weekday)
	{
		//Analyse.getDayInfo() = new OrderedHashtable();
		analyse.getDayInfo().put("dow", weekday);		//DETERMINE THE DAY OF THE WEEK.
		analyse.getDayInfo().put("PS",1);
		analyse.getDayInfo().put("nday",250);
		analyse.getDayInfo().put("LS",0);
		final String UsualFileName = "Ponomar/xml/Services/UsualBeginning/"; // THE LOCATION FOR ANY EXTRA INFORMATION
		Service test2=new Service(analyse.getDayInfo().clone());
		 test2.readService(UsualFileName+"UsualBeginning.xml");
		usualBeginning1=test2.service1;
	}	
	public UsualBeginning(OrderedHashtable dayInfo)
	{
		final String UsualFileName = "Ponomar/xml/Services/UsualBeginning/";
		Service test2=new Service(dayInfo);
		usualBeginning1=test2.readService(UsualFileName+"UsualBeginning.xml");
		 	
		 	
	}
	public String getUsualBeginning()
	{
		return usualBeginning1;
	}
}
