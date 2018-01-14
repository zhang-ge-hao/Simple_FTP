/*
 * class CmdLineTools
 * 
 * Used to assist the command line display.
 * 
 */
public class CmdLineTools {
	
	public static String TypesettingAtAFixedWidth(String[] tArr,int w,int s){
														//Make typesetting,beautify the return value 
														//of the "LIST" instruction.
		int lo = 1,hi = 1+tArr.length,mi,sum,max,li = 0;
		int[] ws = new int[tArr.length];
		while(lo <= hi){
			mi = (lo+hi)/2;
			sum = 0; max = 0; li = 0;
			for(int i=0;i<tArr.length;i++){
				max = Math.max(max,tArr[i].length());
				if((i+1) % mi == 0 || i==tArr.length-1){
					ws[li++] = max;
					sum += max; max = 0;
				}
			}
			if(hi == lo)break;
			sum += (li-1)*s;
			if(sum > w)lo = mi+1;
			else hi = mi;
		}
		StringBuffer sb = new StringBuffer();
		if(lo > tArr.length){
			for(int i=0;i<tArr.length;i++)
				sb.append(tArr[i]+"\r\n");
		}else{
			for(int i=0;i<lo;i++){
				for(int j=0;j<li;j++){
					int blo = 0;
					if(j*lo+i<tArr.length){
						sb.append(tArr[j*lo+i]);
						blo = ws[j]-tArr[j*lo+i].length();
					}
					if(j<li-1)for(int k=0;k<blo+s;k++)sb.append(" ");
					else if(i<lo-1)sb.append("\r\n");
				}
			}
		}
		//System.out.println(lo+" "+li);
		//for(int i=0;i<li;i++)System.out.print(ws[i]+(i==li-1?"\n":" "));
		return sb.toString();
	}
}
