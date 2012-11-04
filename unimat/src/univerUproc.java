import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.JOptionPane;


public class univerUproc {

	private String[] param;
	private String outDir = ".";
	private FileOutputStream out;
	private boolean mailabort = true;
	public enum FuncPeriod { giornaliero, settimanale, quindicinale, mensile, decadale };


	public void setParam(String[] param) {
		this.param = param;
	}

	public String[] getParam() {
		return param;
	}
	public String getScriptUnivNew(){
		String comando=null;
		String fp = fperiod();
		String appl = "";
		try {
		  appl=param[17].substring(0, 2);
		}
		catch (java.lang.Exception e){
			//System.out.println(param[13] + " "+ param[17]);
			JOptionPane.showConfirmDialog((Component) null,
					"Attenzione alla riga"+param[0]+" mancano le informazioni sulla MU :"+param[13]+" - "+param[17], "Avviso",
					JOptionPane.WARNING_MESSAGE);
		}
		comando = "uxadd upr sim upr="+param[13]+"  GENINF APPL="+appl+"\n";
        comando +="uxupd upr sim upr="+param[13]+" incclass add XCLASS=\\(REORG\\)\n";
        comando +="uxupd upr sim upr="+param[13]+" GENINF APPL="+appl+" CLASS=PIANO LABEL=\\\""+param[1]+"\\\" FPERIOD="+fp+"\n";
		return comando;
	}
	
	public String getScriptUniv(){
		String comando=null;
		String fp = fperiod();
		String appl=param[17].substring(0, 2);
		comando ="uxupd upr sim upr="+param[13]+" incclass add XCLASS=\\(REORG\\)\n";
        comando +="uxupd upr sim upr="+param[13]+" GENINF APPL="+appl+" CLASS=PIANO LABEL=\\\""+param[1]+"\\\" FPERIOD="+fp+"\n";
		return comando;
	}
	
	public String getScriptLUniv(String nome, Boolean nuova){
		
		String retStr = "";
		String nomes = nome.replace("_", "S");
		String appl=nome.substring(0, 2);
		if(nuova){
		 retStr="uxadd upr sim upr="+nome+" GENINF APPL="+appl;
		}
	    retStr+="\nuxupd upr sim upr="+nome+" GENINF APPL="+appl+" CLASS=PIANO LABEL=\\\"Uproc lancio sessione "+nomes+"\\\" FPERIOD=D";
	 
      return retStr;
	}
	private String fperiod(){
     String periodo=null;
     
     try {
	 switch(FuncPeriod.valueOf(param[4])){
	 case giornaliero  : periodo="D"; break;
	 case settimanale  : periodo="W"; break;
	 case quindicinale : periodo="F"; break;
	 case mensile      : periodo="M"; break;
	 case decadale     : periodo="T"; break;
	 default:  
		   periodo= "N";
	 }
     } catch (java.lang.IllegalArgumentException e){
    	 periodo = "D";
     }
     return periodo;
	}
	private String pdateUniverse(){
		 String procDate = param[9];
		
		 String DTRAITP1="$($UXEXE/uxdat \"YYYYMMDD\" \"$S_DATRAIT\" \"YYYYMMDD\" \"+1d\")";
		 /*
		 String EXEDATE="$($UXEXE/uxdat \"YYYYMMDD\" \"$(date +%Y%m%d)\" \"YYYYMMDD\" )";
		 String EXEDATEP1="$($UXEXE/uxdat \"YYYYMMDD\" \"$(date +%Y%m%d)\" \"YYYYMMDD\" \"+1d\" )";
		 */
		 String DTRAITM="$($UXEXE/uxdat \"YYYYMMDD\" \"$S_DATRAIT\" \"YYYYMM\" )";
     
		 procDate = procDate.replace("!DTRAIT!", "${S_DATRAIT}");  
		 procDate = procDate.replace("!PROCDATE_YMD!","${S_DATRAIT}");
		   
		 procDate = procDate.replace("!DTRAITP1!",DTRAITP1);
		 procDate = procDate.replace("!DTRAITM!",DTRAITM);
		
       return procDate;
	}
	public void createFile(){
		String nomefile = param[13]+".000";
		try {
			File f = new File(outDir,nomefile);
			out = new FileOutputStream(f);
			PrintStream pos = new PrintStream(out);
			pos.print("#Shell generarata da $matic\n");
			pos.print("if [ \"$S_NUMJALON\" = \"99\" ]\n");
			pos.print("     then\n");
			pos.print("             exit 0\n");
			pos.print("     fi\n");
			pos.print("\n\n");

			pos.print("if [ \"$S_ESPEXE\" = \"X\" ]\n");
			pos.print("     then\n");
            if (param[16].compareTo("d_standardAppl.sh")==0){
            	String paraDate = pdateUniverse();
            	pos.print("#Rif Dollaro uproc: "+param[13]+" sessione: "+param[14]+" riga: "+param[0]);
                pos.print("\n#Shell "+param[7]+" "+paraDate);
                pos.print("\necho Lancio "+param[7]+" "+paraDate);
       	        pos.print("\ncd "+param[8]+"\n");
       	        pos.print("       "+param[8]+param[7]+" "+paraDate);
       	        pos.print("\n       rc=$?");
       	        pos.print("\nelse\n");
       	        pos.print("echo \"Lancio "+param[7]+" "+paraDate+"\"\n");
       	        pos.print("rc=0\n");
       	        pos.print("sleep 10\n");
            }
            if (param[16].compareTo("d_relTokensQuota1.sh")==0){
            	String[] paramRes;
            	String mu = null, quota = null, resource = null;
            	int index;
            	paramRes = param[12].split(",");
            	for (int i=0;i<paramRes.length;i++){
            	    index = paramRes[i].indexOf("RES_NAME=");
            	    if (index!=-1){
            	    	resource = paramRes[i].substring(9);
            	    }
            	    index = paramRes[i].indexOf("MU_NAME=");
            	    if (index!=-1){
            	    	mu = paramRes[i].substring(9);
            	    }
            	    index = paramRes[i].indexOf("TOKENS_NUM=");
            	    if (index!=-1){
            	    	quota = paramRes[i].substring(12);
            	    }
            	
            	}
            	
            	pos.print("#"+param[13]+" sessione "+param[14]+" Uproc Tecnica per rilascio risorse\n");
                pos.print("#"+param[12]+"\n");
                pos.print("$UXEXE/uxrls res RES="+resource+" ESP=$S_ESPEXE MU="+mu+" QT1="+quota);
                pos.print("\n       rc=$?\n");
                pos.print("else\n");
                pos.print("$UXEXE/uxrls res SIM RES="+resource+" ESP=$S_ESPEXE MU="+mu+" QT1="+quota);

                
            }
            if (mailabort){
       	      pos.print("\nfi\n#in caso di abort mando e-mail\n");
       	      pos.print("/orsyp/univ_adm/CedPadovaDwh/bin/mailabort $rc\n");
            } else {
            	pos.print("\nfi\n");
            }
       	      pos.print("exit $rc");

            
			pos.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void setOutDir(String outDir) {
		this.outDir = outDir;
	}

	public String getOutDir() {
		return outDir;
	}
   public void createLanch(String nome, Boolean standard){
	   String nomefile = nome+".000";
	   String uprstd = nome.substring(0, 2)+"UINIZIO";
	   String nomes = nome.replace('_', 'S');
		try {
			File f = new File(outDir,nomefile);
			out = new FileOutputStream(f);
			PrintStream pos = new PrintStream(out);
			pos.print("#Shell generarata da $matic\n");
			pos.print("case ${S_ESPEXE} in\n");
			pos.print("           A) area=APP;;\n");
			pos.print("           I) area=INT;;\n");
			pos.print("           S) area=SIM;;\n");
			pos.print("           X) area=EXP;;\n");
			pos.print("           *) print \"Esapce inconnu \"\n");
			pos.print("              exit 1;;\n");
			pos.print("esac\n");
			if (!standard){
				String[] paramL = param[12].split(",");
				Boolean modificheP=false;
				Boolean modificheA=false;
				String afterstr="";
				for (int i=0;i<paramL.length;i++){
					if(paramL[i].contains("PDATE_OFFSET=")){
						modificheP=true;
						int valOffset;
						try {
						valOffset = Integer.valueOf(paramL[i].substring(14));
						} catch (java.lang.NumberFormatException enumb) {
							valOffset = 0;
						}
						if (valOffset>0){
							pos.print("\nUDATARES=$($UXEXE/uxdat \"YYYYMMDD\" \"$S_DATRAIT\" \"YYYYMMDD\" \"+"+valOffset+"d\")\n");
						}
						else {
							pos.print("\nUDATARES=$($UXEXE/uxdat \"YYYYMMDD\" \"$S_DATRAIT\" \"YYYYMMDD\" \""+valOffset+"d\")\n");
						}
					}
					if(paramL[i].contains("AFTER=")){
						modificheA=true;
						afterstr = paramL[i].substring(6);
						if(paramL[i].contains("EXEDATEP1")){
							pos.print("\nUDATAFTER=$($UXEXE/uxdat \"YYYYMMDD\" \"$(date +%Y%m%d)\" \"YYYYMMDD\" \"+1d\" )\n");
				            afterstr = afterstr.replace("!EXEDATEP1!", "$UDATAFTER");
						}
						else {
							pos.print("\nUDATAFTER=$($UXEXE/uxdat \"YYYYMMDD\" \"$(date +%Y%m%d)\" \"YYYYMMDD\")\n");
							afterstr = afterstr.replace("!EXEDATE!", "$UDATAFTER");
						}
					}
				}
				pos.print("$UXEXE/uxordre $area SES="+nomes+" VSES=$S_NUMVERSESS UPR="+uprstd+" VUPR=$S_VEREXE MU=$S_CODUG ");
				if (modificheP){
					pos.print("DTRAIT=$UDATARES ");
				} else {
					pos.print("DTRAIT=$S_DATRAIT ");	
				}
				if(modificheA){
					pos.print("AFTER="+afterstr);
				}
			}else{
				pos.print("\n$UXEXE/uxordre $area SES="+nomes+" VSES=$S_NUMVERSESS UPR="+uprstd+" VUPR=$S_VEREXE MU=$S_CODUG DTRAIT=$S_DATRAIT\n");
			}
			pos.print("\n");
			pos.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   }

public boolean isMailabort() {
	return mailabort;
}

public void setMailabort(boolean mailabort) {
	this.mailabort = mailabort;
}
}
