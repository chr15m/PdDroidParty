package cx.mccormick.pddroidparty;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

public class SVGManipulator {
	Document doc = null;
	
	ArrayList<String[]> start = null;
	ArrayList<String[]> end = null;
	
	Node interpolated = null;
	StringBuffer newpath = new StringBuffer();
	
	// test stub
	public static void main (String[] aArguments) {
		SVGManipulator x = new SVGManipulator(new File(aArguments[0]));
		System.out.println(x.getAttribute("x"));
		System.out.println(x.getAttribute("y"));
		System.out.println(x.getAttribute("width"));
		System.out.println(x.getAttribute("height"));
		System.out.println(x.getAttribute("yo"));
		System.out.println(x.toString());
	}
	
	// constructor loads the SVG from a file
	public SVGManipulator(File f) {
		// load our document
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			doc = docBuilder.parse(f);
		} catch (Exception e) {
			doc = null;
		}
	}
	
	// get an attribute on the SVG we have loaded
	public String getAttribute(String s) {
		if (doc != null) {
			Node n = doc.getElementsByTagName("svg").item(0).getAttributes().getNamedItem(s);
			if (n != null) {
				return n.getNodeValue();
			}
		}
		return null;
	}
	
	// turn the document and it's subnodes recursively into a string
	// hack from here: http://stackoverflow.com/questions/2290945/writing-xml-on-android
	public String getStringFromNode(Node root) throws IOException {
		StringBuilder result = new StringBuilder();
		
		if (root.getNodeType() == 3)
			result.append(root.getNodeValue());
		else {
			if (root.getNodeType() != 9) {
				StringBuffer attrs = new StringBuffer();
				for (int k = 0; k < root.getAttributes().getLength(); ++k) {
					attrs.append(" ").append(
							root.getAttributes().item(k).getNodeName()).append(
							"=\"").append(
							root.getAttributes().item(k).getNodeValue())
							.append("\" ");
				}
				result.append("<").append(root.getNodeName()).append(" ").append(attrs).append(">");
			} else {
				result.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			}
			
			NodeList nodes = root.getChildNodes();
			for (int i = 0, j = nodes.getLength(); i < j; i++) {
				Node node = nodes.item(i);
				result.append(getStringFromNode(node));
			}
			
			if (root.getNodeType() != 9) {
				result.append("</").append(root.getNodeName()).append(">");
			}
		}
		return result.toString();
	}
	
	// return the document as a string
	public String toString() {
		if (doc != null) {
			try {
				return getStringFromNode(doc.getDocumentElement());
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}
	
	// interpolates two paths, sets the first one to the interpolated version, and hides the second one
	public SVGManipulator interpolate(String startid, String endid, double amount) {
		// create it if not and add it to the 
		if (start == null) {
			// find the start end end nodes
			Node startnode = doc.getElementById(startid);
			Node endnode = doc.getElementById(endid);
			
			// get the values out that we care about
			String[] startsplit = startnode.getAttributes().getNamedItem("d").getNodeValue().split(",");
			String[] endsplit = endnode.getAttributes().getNamedItem("d").getNodeValue().split(",");
			
			// get the list of lists of string and space separated elements
			start = new ArrayList<String[]>();
			end = new ArrayList<String[]>();
			
			// loop through the comma delimited bits, splitting them up and adding them to the sub arrays			
			for (int startidx=0; startidx < startsplit.length; startidx++) {
				start.add(startsplit[startidx].split(" "));
				end.add(endsplit[startidx].split(" "));
			}
			
			// remove the end node from the document
			endnode.getParentNode().removeChild(endnode);
			
			// remember the startnode as a node we want to interpolated
			interpolated = startnode;
		}
		
		if (start != null && end != null) {
			// clear our new path string
			newpath.delete(0, newpath.length());
			// interpolate the arrays and set the attribute
			for (int s=0; s<start.size(); s++) {
				for (int c=0; c<start.get(s).length; c++) {
					// try to convert strings to integers and subtract them
					String result = start.get(s)[c];
					try {
						// interpolate between the amounts, checking for ints first then floats
						float startfloat = Float.parseFloat(result);
						float endfloat = Float.parseFloat(end.get(s)[c]);
						float interpfloat = (endfloat - startfloat) * (float)amount + startfloat;
						result = "" + interpfloat;
					} catch (Exception e) {
						// we don't care if we fail
					}
					newpath.append(result);
					if (c<start.get(s).length - 1) {
						newpath.append(" ");
					}
				}
				if (s<start.size() - 1) {
					newpath.append(",");
				}
			}
			
			Log.e("SVGManipulator", newpath.toString());
			// set the value of interpolated path to what we just created
			interpolated.getAttributes().getNamedItem("d").setNodeValue(newpath.toString());
		}
		
		return this;
	}
}
