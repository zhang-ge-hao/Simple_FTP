import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.*;

/*
 * class FileTransTool
 * 
 * Include file send and receive functions.It becomes a class 
 * because it is used both at the client and at the server
 * 
 */

public class FileTransTool {

	static public int fileUp(PrintWriter writer,BufferedReader reader,Socket sd,File file,int mode,String ad)
														//File upload,do a BFS to upload a folder
			throws IOException{
		int dif = file.getAbsolutePath().length()-file.getName().length(),con = 1;
		Queue<File> que = new LinkedList<File>(); que.offer(file);
		if(file.exists())while(!que.isEmpty()){
			File frtf = que.poll();
			String frtp = frtf.getAbsolutePath().substring(dif);
			if(frtf.isDirectory()){
				 con += Arrays.asList(frtf.listFiles()).stream()
				 .map(f->que.offer(f)).collect(Collectors.toList()).size();
				writer.println(FTPCmds.CMD[13]+" "+frtp);writer.flush();
				con --;
			}else {
				writer.println(FTPCmds.CMD[12]+" "+frtp);writer.flush();
				FileInputStream fis = new FileInputStream(frtf);
				byte bb[]= new byte[1024];int len = 0;
				BufferedOutputStream os = new BufferedOutputStream(sd.getOutputStream());
				while((len=fis.read(bb))!=-1)os.write(bb,0,len);
				fis.close(); os.close(); sd.close();
				sd = fileTranRestate(writer,reader,mode,ad);
				System.out.println("<-"+frtp);
			}
		}
		writer.println(FTPCmds.CMD[14]); writer.flush();
		return con;
	}
	static public int fileDown(PrintWriter writer,BufferedReader reader,Socket sd,String basePath,int mode,String ad)
														//File download.
			throws IOException{
		String cmd; String[] ops; int con = 0;
		while((cmd=reader.readLine()) != null){
			ops = cmd.split(" ");
			File f = new File(basePath+"/"+(ops.length>1?ops[1]:""));
			if(ops[0].equals(FTPCmds.CMD[12])){
				FileOutputStream fos = new FileOutputStream(f);
				byte bb[] = new byte[1024]; int len = 0;
				BufferedInputStream is = new BufferedInputStream(sd.getInputStream());
				while((len=is.read(bb))!=-1)fos.write(bb,0,len);
				fos.close(); is.close(); sd.close();
				sd = fileTranRestate(writer,reader,mode,ad);
				System.out.println("->"+ops[1]);
				con ++;
			}else if(ops[0].equals(FTPCmds.CMD[13]))f.mkdir();
			else if(ops[0].equals(FTPCmds.CMD[14]))break;
		}
		return con;
	}
	static public Socket fileTranRestate(PrintWriter writer,BufferedReader reader,int mode,String ad){
		Socket clid = null;
		if(mode == 1){
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
			writer.println(ad+":"+(ph*256+pl));
			writer.flush();
			try {
				clid = ss.accept();
			} catch (IOException e) {System.out.println(e.getMessage());}
		}else if(mode == 3){
			try {
				String[] adap = reader.readLine().split(":");
				clid = new Socket(adap[0],Integer.valueOf(adap[1]));
			} catch (IOException e) {System.out.println(e.getMessage());} 
		}
		return clid;
	}
}
