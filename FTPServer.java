import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;
/*
 * class FTPServer
 * 
 * It is responsible for receiving the client's link instructions 
 * and opening a new client process.
 * 
 * Variable explanation:
 * F_DIR(String) : 	The default server workspace root directory.
 * MT(String[]) : 	Used to store the prompt returned by the server 
 * 					to the client.
 * U_P(Map) : 		Save the user name to password mapping, 
 * 					initialized within the main function.
 * 
 */
public class FTPServer {
	public static String F_DIR = "C:/Users/Administrator/indix";
	static public Map<String,String> U_P = new HashMap<String,String>();
	static String[] MT = {
			"Welcome!!!",
			"You have already logged in.",
			"Please enter PASS command next.",
			"Operands missing.",
			"Login success.",
			"Wrong user name or password,login failed.",
			"No such a command.",
			"You have already logged out.",
			"Bye.",
			"Please enter USER command before.",
			"Please log in first.",
			"Change the directory to:",
			"Directory does not exist.",
			" send request:  ",
			"]\r\nResponse to ",
			"File does not exist.",
			"Port link success.",
			"Pasv link success.",
			"File transfer is completed",
			"Please enter PASV or PORT command before."
	};
	
	@SuppressWarnings("resource")
	public static void main(String[] args) throws
	IOException{
		
		U_P.put("yum","yum");
		
		ServerSocket ss = new ServerSocket(FTPCmds.serverPortM);
		while(true){
			Socket client = ss.accept();
			new ClientThread(client,F_DIR).start();
		}
	}
}
/*
 * class ClientThread
 * Responsible for the interaction with a client.
 * 
 * Basic business logic : 
 * 1.Receive the client's instructions.
 * 2.Processing instructions.
 * 3.Return prompts.
 * 4.Back 1. unless the client sends a quit.
 * 
 */
class ClientThread extends Thread{
	public Socket clim,clid = null;
	public String dir,user = null,pass = null,ipa = null;
	public boolean isLogin = false;
	public int linkMode = -1;
	public ClientThread(Socket client,String F_DIR){
		clim = client;
		dir = F_DIR;
	}
	public void run(){
		InputStream ins = null;
		OutputStream outs = null;
		String cmd = null;
		try {
			ins = clim.getInputStream();
			outs = clim.getOutputStream();
		} catch (IOException e) {
			System.out.println(e.getMessage()+"_001");
		}
		BufferedReader reader = new BufferedReader
				(new InputStreamReader(ins,Charset.forName("UTF-8")));  
        PrintWriter writer = new PrintWriter(outs);
        writer.println(FTPServer.MT[0]); writer.flush();
        while( true ){
        	writer.println(dir+"> ");writer.flush(); 	//Let the client output the path information
        	try {
        		cmd = reader.readLine();        		//Received instructions
        		if(cmd == null)break;
        		else if(cmd.equals(""))continue;
        		System.out.println("["+new Date()+"]\r\n"+clim.getInetAddress()+":"+clim.getPort()+FTPServer.MT[13]+cmd);
        												//Print the Prompt information
        	} catch (IOException e){
        		System.out.println(e.getMessage()+"_002");
        		break;
        	}
        	String op0 = cmd.split(" ")[0],rpl;			//Decomposition instructions and operands
        	String[] ops = cmd.split(" ");
        	if(cmd.equals("")){							//This if block used to call the appropriate function
        		rpl = "";
        	}else if(op0.equals(FTPCmds.CMD[0])){
        		if(ops.length != FTPCmds.COPC[0])
        			writer.println(rpl = FTPServer.MT[3]);
        		else writer.println(rpl = user(cmd));writer.flush();
        	}else if(op0.equals(FTPCmds.CMD[1])){
        		if(ops.length != FTPCmds.COPC[1])
        			writer.println(rpl = FTPServer.MT[3]);
        		else writer.println(rpl = pass(cmd));writer.flush();
        	}else if(op0.equals(FTPCmds.CMD[2])){
        		if(ops.length != FTPCmds.COPC[2])
        			writer.println(rpl = FTPServer.MT[3]);
        		else writer.println(rpl = quit());writer.flush();
        	}else if(op0.equals(FTPCmds.CMD[3])){
        		if(ops.length != FTPCmds.COPC[3])
        			writer.println(rpl = FTPServer.MT[3]);
        		else writer.println(rpl = list());writer.flush();
        	}else if(op0.equals(FTPCmds.CMD[4])){
        		if(ops.length != FTPCmds.COPC[4])
        			writer.println(rpl = FTPServer.MT[3]);
        		else writer.println(rpl = cwd(cmd));writer.flush();
        	}else if(op0.equals(FTPCmds.CMD[5])){
        		if(ops.length != FTPCmds.COPC[5])
        			writer.println(rpl = FTPServer.MT[3]);
        		else writer.println(rpl = size(cmd));writer.flush();
        	}else if(op0.equals(FTPCmds.CMD[6])){
        		if(ops.length != FTPCmds.COPC[6])
        			writer.println(rpl = FTPServer.MT[3]);
        		else writer.println(rpl = port(cmd));writer.flush();
        	}else if(op0.equals(FTPCmds.CMD[7])){
        		if(ops.length != FTPCmds.COPC[7])
        			writer.println(rpl = FTPServer.MT[3]);
        		else writer.println(rpl = pasv(writer));writer.flush();
        	}else if(op0.equals(FTPCmds.CMD[8])){
        		if(ops.length != FTPCmds.COPC[8])
        			writer.println(rpl = FTPServer.MT[3]);
        		else writer.println(rpl = retr(writer,reader,cmd));writer.flush();
        	}else if(op0.equals(FTPCmds.CMD[9])){
        		if(ops.length != FTPCmds.COPC[9])
        			writer.println(rpl = FTPServer.MT[3]);
        		else writer.println(rpl = stor(writer,reader,cmd));writer.flush();
        	}else{
        		writer.println(rpl = FTPServer.MT[6]);writer.flush();
        	}
        	System.out.println("["+new Date()+FTPServer.MT[14]+clim.getInetAddress()+":"+clim.getPort()+":  "+rpl);
        }
        try {  
            reader.close();  
            clim.close();
            writer.close();
            if(clid != null)clid.close();
        } catch (IOException e) {  
        	System.out.println(e.getMessage()+"_003");
        }  
	}
	private String user(String cmd){					//Deal with the "USER"
		String op1 = (cmd.split(" ").length>1)?cmd.split(" ")[1]:null;
		if(isLogin){
			return FTPServer.MT[1];
		}else{
			user = op1; ipa = clim.getInetAddress().toString();
			return FTPServer.MT[2];
		}
	}
	private String pass(String cmd){					//Deal with the "PASS"
		String op1 = (cmd.split(" ").length>1)?cmd.split(" ")[1]:null;
		//System.out.println(user+" "+op1+" "+FTPServer.U_P.get(user));
		if(isLogin){
			return FTPServer.MT[1];
		}else if(user == null){
			return FTPServer.MT[9];
		}else if(FTPServer.U_P.containsKey(user) && 
				FTPServer.U_P.get(user).equals(op1)){
			isLogin = true;
			return FTPServer.MT[4];
		}else{
			return FTPServer.MT[5];
		}
	}
	private String quit(){								//Deal with the "QUIT"
		int rank = isLogin?8:7;
		isLogin = false;
		return FTPServer.MT[rank];
	}
	private String list(){								//Deal with the "LIST"
		if(!isLogin)return FTPServer.MT[10];
		File[] files = new File(dir).listFiles();
		String[] names = new String[files.length];
		for(int i=0;i<files.length;i++)names[i] = files[i].getName();
		Arrays.sort(names);
		String res = CmdLineTools.TypesettingAtAFixedWidth(names,60,2);
		return FTPCmds.CMD[11]+" "+res.split("\r\n").length+"\r\n"+res;
	}
	private String cwd(String cmd){						//Deal with the "CWD"
		if(!isLogin)return FTPServer.MT[10];
		String op1 = (cmd.split(" ").length>1)?cmd.split(" ")[1]:null;
		File dirFile = new File(op1);
		if(dirFile.exists() && dirFile.isDirectory()){
			dir = op1;
			return FTPServer.MT[11]+dir;
		}else return FTPServer.MT[12];
	}
	private String size(String cmd){					//Deal with the "SIZE"
		if(!isLogin)return FTPServer.MT[10];
		String op1 = (cmd.split(" ").length>1)?cmd.split(" ")[1]:null;
		File selFile = new File(dir+"/"+op1);
		String[] MD = {"B","KB","MB","GB"};
		int mdid = 0;
		if(selFile.exists() && selFile.isFile()){
			double r = selFile.length();
			while(true){
				if(r > 1024){
					r /= 1024;
					mdid ++;
				}else break;
			}
			return ((long)(r*100+0.5))/100.0+MD[mdid];
		}else return FTPServer.MT[15];
	}
	private String port(String cmd){					//Deal with the "PORT"
		if(!isLogin)return FTPServer.MT[10];
		String op1 = (cmd.split(" ").length>1)?cmd.split(" ")[1]:null;
		try {
			if(clid != null)clid.close();
			clid = new Socket(ipa.substring(1),Integer.valueOf(op1)
					,clim.getLocalAddress(),FTPCmds.serverPortD);
			linkMode = 0;
			return FTPServer.MT[16];
		} catch (NumberFormatException e) {
			return e.getMessage();
		} catch (UnknownHostException e) {
			return e.getMessage();
		} catch (IOException e) {
			return e.getMessage();
		}
	}
	private String pasv(PrintWriter writer){			//Deal with the "PASV"
		if(!isLogin)return FTPServer.MT[10];
		if(clid != null) try {
			clid.close();
		} catch (IOException e) {
			return e.getMessage();
		}
		int ph,pl; ServerSocket ss;
		Random rm = new Random();
		while(true){
			ph = 1 + rm.nextInt(20);
			pl = 100 + rm.nextInt(1000);
			try {
                ss = new ServerSocket(ph * 256 + pl);
                break;
            } catch (IOException e) {continue;}
		}
		String resMess = clim.getLocalAddress().getHostAddress().replaceAll("\\.",",");
		resMess += ","+ph+","+pl;
		writer.println("227 entering passive mode ("+resMess+")");
		writer.flush();
		try {
			clid = ss.accept();
			linkMode = 1;
			return FTPServer.MT[17];
		} catch (IOException e) {
			return e.getMessage();
		}
	}
	private String retr(PrintWriter writer,BufferedReader reader,String cmd){
														//Deal with the "RETR"
		if(!isLogin)return FTPServer.MT[10];
		if(clid == null || clid.isClosed())return FTPServer.MT[19];
		String op1 = (cmd.split(" ").length>1)?cmd.split(" ")[1]:null;
		File opf = new File(dir+"/"+op1);
		if(!opf.exists() || (linkMode==0&&opf.isDirectory())){
			writer.println(FTPCmds.CMD[14]);writer.flush();
			return FTPServer.MT[15];
		}
		try {
			if(0 < FileTransTool.fileUp(writer,reader,clid,opf
					,linkMode,clim.getLocalAddress().getHostAddress())){
				clid.close(); clid = null; linkMode = -1;
			}
			return FTPServer.MT[18];
		} catch (IOException e) {
			return e.getMessage();
		}
	}
	private String stor(PrintWriter writer,BufferedReader reader,String cmd){
														//Deal with the "USER"
		if(!isLogin)return FTPServer.MT[10];
		if(clid == null || clid.isClosed())return FTPServer.MT[19];
		try {
			if(0 < FileTransTool.fileDown(writer,reader,clid,dir
					,linkMode,clim.getLocalAddress().getHostAddress())){
				clid.close(); clid = null; linkMode = -1;
			}
			return FTPServer.MT[18];
		} catch (IOException e) {
			return e.getMessage();
		}
	}
}