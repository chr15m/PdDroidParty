package cx.mccormick.pddroidparty;

import java.io.File;
import java.io.StringWriter;
import java.io.IOException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.OutputKeys;

public class SVGManipulator {
	Document doc = null;
	
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
	
	public String getAttribute(String s) {
		if (doc != null) {
			Node n = doc.getElementsByTagName("svg").item(0).getAttributes().getNamedItem(s);
			if (n != null) {
				return n.getNodeValue();
			}
		}
		return null;
	}
	
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
	
	// interpolates two paths, sets the first one to the interpolations, and removes the second one
	public String interpolatePaths(String startid, String endid, float amount) {
		return "";
	}
}
