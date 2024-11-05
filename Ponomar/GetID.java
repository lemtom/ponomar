package Ponomar;

/***********************************************************************
 * THIS MODULE CREATES THE TEXT FOR THE ORTHODOX SERVICE OF THE FIRST HOUR
 * (PRIME) THIS MODULE IS STILL IN THE DEVELOPMENT PHASE.
 * 
 * (C) 2024 YURI SHARDT. ALL RIGHTS RESERVED.
 * 
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
 ***********************************************************************/
public class GetID {
	// SOME QUICK NOTES FOR FURTHER IMPLEMENTATION:
	// Allows me to create the correct HTML line for a specific piece of information
	// required from a file.
	public final String Type = "M";
	public String Id = "";
	public String What = "";
	public String Who = "";
	public String RedFirst = "0";
	public String Times = "1";
	public String NewLine = "";
	public String Header = "";
	private String HTML = "";
	public String ToneA = "";

	public GetID(String cId, String path) {
		Id = cId;
		What = path;
		// System.out.println("The ID is "+Id+" and the desired item is "+What);
	}

	public String getHTML() {
		HTML = "<GETID Type=\"" + Type + "\" Id=\"" + Id + "\" What=\"" + What + "\" Who=\"" + Who + "\" RedFirst=\""
				+ RedFirst + "\" ";
		if (Integer.parseInt(Times) > 1) {
			HTML += "Times=\"" + Times + "\" ";
		}
		HTML += "NewLine=\"" + NewLine + "\" Header=\"" + Header + "\" ToneA=\"" + ToneA + "\"/>";
		return HTML;
	}

	public String GetFullID() {
		// Id=CId;
		// What=path;
		String[] splitPath = What.split("/");

		// System.out.println("The ID is "+Id+" and the desired item is "+What);
		// System.out.println("Testing: "+Id+"_"+splitPath[splitPath.length-1]);
		return Id + "_" + splitPath[splitPath.length - 1];
	}

	public static void main(String[] args) {
		GetID getIDx = new GetID("9863", "soemthing/somewhere/test");
		getIDx.NewLine = "1";
		getIDx.Who = "C";
		getIDx.RedFirst = "0";
		getIDx.Times = "1";
		getIDx.NewLine = "0";
		getIDx.Header = "Nothing to Show";
		System.out.println(getIDx.getHTML());
		// output.append(s + newline);
		// output.setCaretPosition(output.getDocument().getLength());
	}

}
