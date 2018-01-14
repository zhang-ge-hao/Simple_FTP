/*
 * class FTPCmds
 * 
 * Store some constant about service creation.
 * 
 * Variable explanation:
 * CMD(String[]) : 	Store instruction.
 * COPC(int[]) : 	Limit format,there must be COPC[i]-1 Operands 
 * 					follow CMD[i].
 * serverPortM : 	The port used to exchange message.
 * serverPortM : 	The port used to exchange data. 
 * 
 */
public class FTPCmds {
	static String[] CMD = {
			"USER","PASS","QUIT","LIST","CWD","SIZE","PORT","PASV","RETR","STOR","REST",
			//2      2      1      1      2     2      2      1      2      2
			"MULTMESS","RCVF","MKDIR","TEND"
	};
	static int[] COPC = {
			2,2,1,1,2,2,2,1,2,2
	};
	static public int serverPortM = 21000,serverPortD = 20000;
}
