import java.io.*;
import java.net.*;
import java.util.*;

/*
 * class FTPClient
 * 
 * A class which is a client.
 * 
 * Variable explanation:
 * cmds(String[]) : 	Stored a series of instructions for 
 * 						testing.Just for test.
 * 
 */
public class FTPClient {
	
	public static String[] cmds = {
			"USER yum",
			"PASS yum",
			"CWD C:/Users/Administrator/indix/fileNameFemo",
			"LIST",
			"SIZE",
			"SIZE myjk.zip",
			"SIZE myjk.zi",
			"PORT 65532",
			"",
			"",
			"STOR pics",
			"STOR README.md",
			"PASV",
			"STOR pics",
			"STOR README.md",
			"QUIT"
	};
	
	static public Socket clim,clid = null;
	static public String serverIP = "127.0.0.1";
	static public int linkMode = -1;
	static public String B_DIR = "C:/Users/Administrator/indix/fileClient";
	static public BufferedReader reader;
	static public PrintWriter writer;
	public static void main(String[] args) throws 
	UnknownHostException, IOException{
		clim = new Socket(serverIP,FTPCmds.serverPortM);
		reader = new BufferedReader(
				new InputStreamReader(clim.getInputStream())); 
		writer = new PrintWriter(clim.getOutputStream());
		System.out.println(reader.readLine());
		
		for(int i=0;i<cmds.length;i++){					//this for loop,just for test.
			String cmd = cmds[i];
			exe(cmd);									//Deal with the commend
		}
		
		reader.close();
		writer.close();
		clim.close();
	}
	public static void exe(String cmd) throws IOException{
		System.out.print(reader.readLine());
		System.out.println(cmd);
		writer.println(cmd);
		writer.flush();
		if(cmd.equals(""))return;
		String[] ops = cmd.split(" ");
		
		exeBeforeRet(ops);
		
		StringBuffer sb = new StringBuffer();
		getMessage(reader,sb,1);
		String resMess = sb.toString();
		System.out.print(resMess);
		
		exeAfterRet(resMess,ops);
	}
	public static void exeBeforeRet(String[] ops) throws IOException{
														//Things to do before receiving the return value
														//The commend about this function : "PORT","RETR","STOR"
		for(int j=0;j<FTPCmds.COPC.length;j++){
			if( ops[0].equals(FTPCmds.CMD[j]) && ops.length==FTPCmds.COPC[j]){
				if(ops[0].equals(FTPCmds.CMD[6])){
					if(clid != null)clid.close();
					int lp = Integer.valueOf(ops[1]);
					ServerSocket ss;
					if(isLocalPortInUse(lp)&&(ss=new ServerSocket(lp))!=null)
						clid = ss.accept();
				}else if(ops[0].equals(FTPCmds.CMD[8])){
					if(clid == null||clid.isClosed())break;
					if(0 < FileTransTool.fileDown(writer,reader,clid,B_DIR
							,linkMode,clim.getLocalAddress().getHostAddress())){
						clid.close(); clid = null; linkMode = -1;
					}
				}else if(ops[0].equals(FTPCmds.CMD[9])){
					if(clid == null||clid.isClosed())break;
					if(0 < FileTransTool.fileUp(writer,reader,clid,new File(B_DIR+"/"+ops[1])
							,linkMode,clim.getLocalAddress().getHostAddress())){
						clid.close(); clid = null; linkMode = -1;
					}
				}
				break;
			}
		}
	}
	public static void exeAfterRet(String resMess,String[] ops) throws
														//Things to do before receiving the return value
														//The commend about this function : "PASV","PORT"
	UnknownHostException, IOException{
		if(resMess.startsWith("227 entering passive mode ")){
			resMess = resMess.replaceAll("227 entering passive mode \\(","");
			resMess = resMess.replaceAll("\\)\r\n","");
			String ads = Arrays.asList(resMess.split(",")).stream()
					.limit(4).reduce((a,b)->(a+"."+b)).get();
			int pot = Arrays.asList(resMess.split(",")).stream()
					.skip(4).map(s->(Integer.valueOf(s)))
					.reduce((a,b)->(a*256+b)).get();
			if(clid != null)clid.close();
			clid = new Socket(ads,pot);
			StringBuffer sb = new StringBuffer();
			getMessage(reader,sb,1);
			System.out.print(sb.toString());
			linkMode = 3;
		}
		if(ops[0].equals(FTPCmds.CMD[6]))linkMode = 2;
	}
	public static void getMessage(BufferedReader reader,StringBuffer sb,int row) throws 
														//Get the return message recursively
	IOException{
		String mess;
		for(int i=0;i<row;i++){
			mess = reader.readLine();
			if(mess.startsWith(FTPCmds.CMD[11])){
				String op1 = (mess.split(" ").length>1)?mess.split(" ")[1]:null;
				getMessage(reader,sb,Integer.valueOf(op1).intValue());
			}else sb.append(mess+"\r\n");
		}
	}
	public static boolean isLocalPortInUse(int pt){		//Tool function to test if a port is occupied
		try {
			Socket ts = new Socket("127.0.0.1",pt);
			ts.close();
			return true;
		} catch (UnknownHostException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}
}
