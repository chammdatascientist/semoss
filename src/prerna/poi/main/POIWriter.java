package prerna.poi.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class POIWriter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		POIWriter writer = new POIWriter();
		Hashtable blankHash = new Hashtable();
		Vector<String[]> blankVect = new Vector<String[]>();
		String[] blankStr = new String[2];
		blankStr[0] = "Relation";
		blankStr[1] = "bill2";
		blankVect.add(blankStr);
		String[] blankStr2 = new String[2];
		blankStr2[0] = "2";
		blankStr2[1] = "3";
		blankVect.add(blankStr2);
		String[] blankStr3 = new String[2];
		blankStr3[0] = "4";
		blankStr3[1] = "5";
		blankVect.add(blankStr3);
		blankHash.put("TEST SHEET", blankVect);
		writer.runExport(blankHash);
	}
	
	public void runExport(Hashtable hash){
		//this function will write a hashtable to an xlsx sheet
		//keys from the hastable become sheet names
		//objects must be in format Vector<String[]>
		//writeFileName is the name of the file this function will create
		//readFileName is the file that this function will add to
		//if readFileName is null, it will create a new workbook
		String workingDir = System.getProperty("user.dir");
		String writeFileName = "LoadingSheets1.xlsx";
		String readFileName = "BaseGILoadingSheets.xlsx";
		String folder = "/export/";
		String fileLoc = workingDir + folder + writeFileName;
		String readFileLoc = workingDir + folder + readFileName;

		ExportLoadingSheets(fileLoc, hash, readFileLoc);
	}
	
	public void ExportLoadingSheets(String fileLoc, Hashtable<String, Vector<String[]>> hash, String readFileLoc){
		//create file
		//OPCPackage pack = new OPCPackage();
		XSSFWorkbook wb = getWorkbook(fileLoc, readFileLoc);
		if(wb == null) return;
		Hashtable<String, Vector<String[]>> preparedHash = prepareLoadingSheetExport(hash);
		
		Set<String> keySet = preparedHash.keySet();
		for(String key: keySet){
			Vector<String[]> sheetVector = preparedHash.get(key);
			writeSheet(key, sheetVector, wb);
		}

		writeFile(wb, fileLoc);
	}
	
	public void writeSheet(String key, Vector<String[]> sheetVector, XSSFWorkbook workbook){
		XSSFSheet worksheet = workbook.createSheet(key);
		int count=0;//keeps track of rows; one below the row int because of header row
		//for each row, create the row in excel
		for (int row=0; row<sheetVector.size();row++){
			XSSFRow row1 = worksheet.createRow( count);
			count++;
			//for each col, write it to that row.
			for (int col=0; col<sheetVector.get(0).length;col++){
				XSSFCell cell = row1.createCell(col);
				cell.setCellValue(sheetVector.get(row)[col]);
			}
		}
	}
	
	public void writeFile(XSSFWorkbook wb, String fileLoc){
        
        try {
        	
	        //Create a fileStream to write into a file
	        FileOutputStream newExcelFile = new FileOutputStream(fileLoc);
	        
	        //Write Stream
			wb.write(newExcelFile);
			
	        //Close New Excel File
	        newExcelFile.close();  
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public XSSFWorkbook getWorkbook(String fileLoc, String readFileLoc){
		XSSFWorkbook wb = null;
		try {
			File inFile = new File(readFileLoc);
			if(readFileLoc!=null && inFile.exists()){
				FileInputStream stream = new FileInputStream(inFile);
				wb = new XSSFWorkbook(stream);
	        	stream.close();
			}
			else{
				wb = new XSSFWorkbook();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return wb;
	}
	
	
	//pass in a hashtable that has the first or of every sheet as {"Relation", "*the relation*"}
	//turns it to look like a loading sheet
	public Hashtable<String, Vector<String[]>> prepareLoadingSheetExport(Hashtable oldHash){
		Hashtable newHash = new Hashtable();
		Iterator<String> keyIt = oldHash.keySet().iterator();
		while(keyIt.hasNext()){
			String key = keyIt.next();
			Vector<String[]> sheetV = (Vector<String[]>) oldHash.get(key);
			Vector<String[]> newSheetV = new Vector<String[]>();
			String[] oldTopRow = sheetV.get(0);//this should be {Relation, *the relation, "", "" ...}
			String[] oldHeaderRow = sheetV.get(1);//this should be {*header1, *header2....}
			String[] oldSecondRow = new String[oldHeaderRow.length];//this is in case the sheet is null (other than the headers)
			if(sheetV.size()>2) oldSecondRow = sheetV.get(2);//this should be {*value1, *value2....}
			String[] newTopRow = new String[oldTopRow.length+1];

			newTopRow[0] = oldTopRow[0];
			for(int i = 0; i<oldTopRow.length;i++){
				newTopRow[i+1] = oldHeaderRow[i];
			}//newTopRow is now set as {"Relation", "Header1", "Header2", ...}
			newSheetV.add(newTopRow);
			
			String[] newSecondRow = new String[oldTopRow.length+1];
			newSecondRow[0] = oldTopRow[1];
			for(int i = 0; i<oldTopRow.length;i++){
				newSecondRow[i+1] = oldSecondRow[i];
			}//newSecondRow should now be {*the relation, *value1....}
			newSheetV.add(newSecondRow);
			
			//now to run through the rest of the sheet
			for(int i = 3; i<sheetV.size(); i++){
				String[] row = sheetV.get(i);
				String[] newRow = new String[row.length+1];
				for(int colIndx = 0; colIndx < row.length; colIndx++){
					newRow[colIndx+1] = row[colIndx];
				}
				newSheetV.add(newRow);
			}
			
			//now add the completed sheet to the new hash
			newHash.put(key, newSheetV);
		}
		return newHash;
	}

}
