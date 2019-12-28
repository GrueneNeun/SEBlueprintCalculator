import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class BluePrintCalc {
	
	public static void getCubeBlocksFromZip(BlockMap blocks, File tocheck) {
		ZipFile zipfile;
		try {
			zipfile = new ZipFile(tocheck);
		    Enumeration<? extends ZipEntry> entries = zipfile.entries();
		    
		    while(entries.hasMoreElements()){
		        ZipEntry entry = entries.nextElement();
		        if (entry.getName().endsWith("sbc")) {
		        	InputStream stream = zipfile.getInputStream(entry);
		        	getCubeBlocksFromInputStream(blocks, stream);
		        }
		    }
		
		    zipfile.close();
		    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void getCubeBlocksFromInputStream(BlockMap blocks, InputStream input) {
		try {
	         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	         Document doc = dBuilder.parse(input);
	         
	         doc.getDocumentElement().normalize();
	         NodeList dList = doc.getElementsByTagName("Definition");
	         for (int i = 0; i < dList.getLength(); i++) {
	            Node nNode = dList.item(i);
	            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	               Element eElement = (Element) nNode;
	               CubeBlock block = new CubeBlock();
	               NodeList idList = eElement.getElementsByTagName("SubtypeId");
	               if (idList.getLength()==0) continue;
	               block.setSubtypeId(idList.item(0).getTextContent());
	               idList = eElement.getElementsByTagName("TypeId");
	               block.setTypeId(idList.item(0).getTextContent());
	               
	               NodeList coList = eElement.getElementsByTagName("Component");
	               for (int j = 0; j < coList.getLength(); j++) {
	            	   //System.out.println("Component: ");
	            	   Node cNode = coList.item(j);
	            	   NamedNodeMap attrib = cNode.getAttributes();
	            	   String name = attrib.getNamedItem("Subtype").getNodeValue();
	            	   int count = Integer.valueOf(attrib.getNamedItem("Count").getNodeValue());
	            	   block.setComponent(name, count);
	               }
	               blocks.put(block.getTypeId()+block.getSubtypeId(), block);
	            }
	         }
	      } catch (Exception e) {
	         e.printStackTrace();
	      }
		return;
	}
	
	public static void getCubeBlocksFromDirectory(BlockMap blocks, File inputFolder) {
		for (File inputFile :inputFolder.listFiles()) {
			try {
				if (inputFile.getName().endsWith("sbc")) getCubeBlocksFromInputStream(blocks, new FileInputStream(inputFile));
				if (inputFile.isDirectory()) getCubeBlocksFromDirectory(blocks, inputFile);
				if (inputFile.getName().endsWith("bin")) getCubeBlocksFromZip(blocks, inputFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return;
	}
	
	public static void generateBlocklist(BlockMap blocks, File baseFolder) {
        File gameFolder = new File(baseFolder.getAbsolutePath() + "\\common\\SpaceEngineers\\Content\\Data\\CubeBlocks");
        File workshopFolder = new File(baseFolder.getAbsolutePath() + "\\workshop\\content\\244850");
       
        getCubeBlocksFromDirectory(blocks, gameFolder);
        getCubeBlocksFromDirectory(blocks, workshopFolder);

        return;
	}
	
	public static HashMap<String, Integer> readBlueprint (File inputFile) {
		HashMap<String, Integer> BlockCount = new HashMap<>();
		
		try {
	         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	         Document doc = dBuilder.parse(inputFile);
	         
	         doc.getDocumentElement().normalize();
	         NodeList dList = doc.getElementsByTagName("MyObjectBuilder_CubeBlock");
	         for (int i = 0; i < dList.getLength(); i++) {
	            Node nNode = dList.item(i);
	            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	               Element eElement = (Element) nNode;
	               String name = eElement.getAttribute("xsi:type");
	               name = name.replaceAll("MyObjectBuilder_", "");
	               NodeList idList = eElement.getElementsByTagName("SubtypeName");
	               name += idList.item(0).getTextContent();
	               
	               if (BlockCount.containsKey(name)) BlockCount.put(name, BlockCount.get(name)+1); else BlockCount.put(name, 1);
	            }
	         }
	      } catch (Exception e) {
	         e.printStackTrace();
	      }
		
		return BlockCount;
	}
	
	public static void printLine() {
		for (int i=0;i<80;i++) System.out.print("-");
		System.out.println();
	}
	
	
	public static void printComponents(HashMap<String, Integer> GridCount) {
		System.out.println("Components:");
		
		for (Map.Entry<String, Integer> entry:GridCount.entrySet()) {
			System.out.print(entry.getKey());
			for (int i=0;i<30-entry.getKey().length();i++) System.out.print(" ");
			System.out.println(entry.getValue());
		}
	}
	
	public static void printComponentsSpecial(HashMap<String, Integer> GridCount) {
		for (Map.Entry<String, Integer> entry:GridCount.entrySet()) {
			System.out.print("Components/"+entry.getKey()+"=");
			System.out.println(entry.getValue());
		}
	}
	
	public static HashMap<String, Integer> generateComponentCount(HashMap<String, CubeBlock> BlockMap, HashMap<String, Integer> BlockCount) {
		HashMap<String, Integer> GridCount = new HashMap<>();
		
		for (Map.Entry<String, Integer> entry:BlockCount.entrySet()) {
			if (!BlockMap.containsKey(entry.getKey())) continue;
			
			for (Map.Entry<String, Integer> com:BlockMap.get(entry.getKey()).getComponents().entrySet()) {
				if (GridCount.containsKey(com.getKey())) GridCount.put(com.getKey(), GridCount.get(com.getKey())+(com.getValue()*entry.getValue()));
				else GridCount.put(com.getKey(), com.getValue()*entry.getValue());
			}
		}
		return GridCount;
	}
	
	public static void printBlocks(HashMap<String, Integer> BlockCount) {
		System.out.println("Used Blocks:");
		for (Map.Entry<String, Integer> entry:BlockCount.entrySet()) {
			System.out.print(entry.getKey());
			for (int i=0;i<60-entry.getKey().length();i++) System.out.print(" ");
			System.out.println(entry.getValue());
		}
	}
	
	public static void printMissingBlocks(HashMap<String, CubeBlock> BlockMap, HashMap<String, Integer> BlockCount) {
		LinkedList<String> missing = new LinkedList<>();
		for (Map.Entry<String, Integer> entry:BlockCount.entrySet()) {
			if (!BlockMap.containsKey(entry.getKey())) {
				missing.add(entry.getKey());
			}
		}
		if (missing.size()==0) {
			System.out.println("All Blocks found.");
		} else {
			System.out.println("Missing Blocks:");
			for (String s:missing) {
				System.out.println(s);
			}
		}
	}
	
	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		System.out.println("Start:");
		
		File baseFolder = new File("D:\\Games\\steamapps\\");
		BlockMap blocks = new BlockMap();
		generateBlocklist(blocks, baseFolder);
		System.out.println("Generated BlockMap");
		System.out.println(System.currentTimeMillis()-start);
		
		
		File blueprint = new File("D:\\Games\\steamapps\\workshop\\content\\244850\\1606630169\\bp.sbc");
		
		HashMap<String, Integer> BlockCount = readBlueprint(blueprint);
		
		System.out.println("Generated BlockCount");
		System.out.println(System.currentTimeMillis()-start);
		HashMap<String, Integer> ComponentCount = generateComponentCount(blocks, BlockCount);
		
		System.out.println("Generated ComponentCount");
		System.out.println(System.currentTimeMillis()-start);
		printMissingBlocks(blocks, BlockCount);
		printLine();
		printBlocks(BlockCount);
		printLine();
		printComponents(ComponentCount);
		printLine();
		printComponentsSpecial(ComponentCount);
		
	}
}
