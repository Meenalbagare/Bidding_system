package readServerTxtFiles;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PersonSubItem {
	
	// this method use to add users subscribed items or read user subscribed items or read user names with given symbols
	// if uName is EMPTY - it say to get user names which subscribed given symbol
	// if sym is NOSYMBOL- it say to get symbol list what subscribed under given user name
	// else this used to subscribed symbol under given user name
	// if have result return it and else return null	
	// synchronized use to avoid clashes
	public synchronized List<String> addOrRes(String uName, String sym){
		
		List<String> res = null;
		
		if(sym.equals("NOSYMBOL")) {
			res = getSubSymWithData(uName);
		}
		else if(uName.equals("EMPTY")) {
			res = getSubUnameList(sym);
		}
		else {
			try {
				res = addNewSubs(uName, sym);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return res;
	}
	
	// read the given file
    private BufferedReader FileRead(String fileName)
    { 
    	try {
    		File f = new File(fileName);
    		String path = f.getAbsolutePath();
			FileReader fr = new FileReader(path);
			BufferedReader br = new BufferedReader(fr);
	        return br;
	        
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}	
    }
    
    // write on the given file
    private PrintWriter FileWrite(String fileName)
    {
		try {
			File f1 = new File(fileName);
			String path = f1.getAbsolutePath();
			FileWriter fw1 = new FileWriter(path,true);
			BufferedWriter bw1 = new BufferedWriter(fw1);
			PrintWriter pw1 = new PrintWriter(bw1);
			return pw1;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
    }
	
	// make a string of ID + lineNo
    //return 0 if add else 1 already exist item 
    private List<String> addNewSubs(String uname, String symbol) throws Exception
    {
    	int flag = 0;
    	List<String> set= new ArrayList<>();
    	BufferedReader br = FileRead("txtFiles/subscribedList.csv");
    	PrintWriter rw = FileWrite("txtFiles/List2.csv");
    	String line;
    	String re = "";
    	
    	while((line = br.readLine()) != null) {
    		String[] nums = line.split(",");
    		
    		if(nums[0].equals(uname)) {
    			String existing = line;
    			
    			for(int x=1; x<nums.length; x++) {
    				String s1 = nums[x];
    				if(symbol.equals(s1)) {
    					re = String.format("%s", existing);
    					System.out.printf("%s : %s : [%s] already subscribed\n", time(),uname, symbol);
    					set.add("1");
    				}
    				else
    					re = String.format("%s,%s", existing, symbol);
    			}
    			flag = 1;
    		}else {
    			rw.println(line);
    		}
    	}
    	
    	if(flag == 0) re = String.format("%s,%s", uname, symbol);
    	rw.println(re);
    	rw.close();
    	br.close();
    	fileUpdate();
    	return set;
    }
    
    // delete the subscribedList.csv
    // rename List2.csv into subscribedList.csv
    
    public void fileUpdate() throws Exception
    {
    	int flag1 = 0, flag2 = 0, flag3 = 0;
    	
    	File f1 = new File("txtFiles/subscribedList.csv");
    	File f2 = new File("txtFiles/List2.csv");
    	if(f1.delete()) {
    		flag1 = 1;
    	}
	    
    	if(flag1 == 1) {
    		if(f2.renameTo(f1)) {
    			flag2 = 1;
    			File f3 = new File("List2.csv");
    			if(f3.createNewFile()) flag3 = 1;
    		}
    	}
    	if(flag1 == 1 && flag2 == 1 && flag3 == 1) {
    		System.out.printf("%s : successfully updated the files [subscribedList.csv,List2.csv]\n", time());
    	}
    	
    }
    
    // read csv and get the subscribed symbols and return as a list
    private List<String> getSubSymWithData(String uname) {
    	
    	BufferedReader br = FileRead("txtFiles/subscribedList.csv");
    	String line;
    	List<String> userSubLst = new ArrayList();
    	
    	
    	try {
			while((line = br.readLine()) != null) {
				String[] nums = line.split(",");
				
				if(nums[0].equals(uname)) {
					for(int i = 1; i < nums.length; i++) {
						userSubLst.add(nums[i]);
					}
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	return userSubLst;
    }
    
    //this method return user Name list related to the given Symbol
    private List<String> getSubUnameList(String sym){
    	
    	BufferedReader br = FileRead("txtFiles/subscribedList.csv");
    	String line;
    	List<String> userLst = new ArrayList();
    	
    	try {
			while((line = br.readLine()) != null) {
				String[] nums = line.split(",");
				
				for(int i = 1; i < nums.length; i++) {
					if(nums[i].equals(sym)) {
						userLst.add(nums[0]);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	
    	return userLst;
    }


	// get current time
	public String time() {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}
}
