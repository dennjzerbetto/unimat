import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import java.io.*;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

import com.csvreader.*;
import net.sf.mpxj.*;
import net.sf.mpxj.mpp.*;
import net.sf.mpxj.reader.*;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class unimatForm extends javax.swing.JFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static JTable uprocGrid;
	private static DefaultTableModel modelUGrid;
	private static ArrayList<String> sesProj;
	private static String outDir = ".";
	private static String projFile = "none.csv";
	private static String fileConf = "unimat.ini";
	private static DbUtils dbUnimat;
	private static ScriptFile filescr;
	private static JMenu menu;
	private static JMenu menuImp ;
	private static JMenuItem itemMail;
	private static boolean mailAbortWrite = true;
	private static Logger logger = Logger.getLogger("unimatForm.class");

	private static JLabel statusBar;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new unimatForm().setVisible(true);
			}
		});

	}

	public unimatForm() {
		initComponents();
		loadConf();
		 PropertyConfigurator.configure("unimat.properties");
		logger.info("Carico le configurazioni");

		dbUnimat = new DbUtils();
		dbUnimat.Connect();
		filescr = new ScriptFile();
		filescr.setOutDir(outDir);
		logger.info("Configurazioni caricate");
		
	}

	public void loadConf() {
		try {
			String mailAbortConf = "1";
			Properties p = new Properties();
			p.load(new FileInputStream(fileConf));

			String outConfDir = p.getProperty("output");
			outDir = outConfDir;
			statusBar.setText(outDir);
			try {
			    mailAbortConf = p.getProperty("MailAbort");
			} catch (java.lang.NullPointerException enull) {
				//
			}
			if ((mailAbortConf !=null) && ( mailAbortConf.compareTo("0")==0)){
				mailAbortWrite = true;
			} else {
				mailAbortWrite = false;
			}
			itemMail.setSelected(mailAbortWrite);
		
		} catch (IOException e) {
			logger.error("File di configurazione non trovato");
			logger.error(e.getStackTrace());
		}
	}

	public void setConf() {
		try {

			Properties p = new Properties();

			FileOutputStream out = new FileOutputStream(fileConf);
			p.put("output", outDir);
			if (mailAbortWrite){
				p.put("MailAbort", "0");
			} else { 
				p.put("MailAbort", "1");
			}
			p.store(out, null);
			out.close();
		} catch (IOException e) {
			logger.error("Scrittura file di configurazione");
			logger.error(e.getStackTrace());
		}

	}

	private void initComponents() {
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setTitle("$matic");
		setLayout(new BorderLayout());
		JMenuBar menuBar = new JMenuBar();
		JMenu menuArch = new JMenu("Archivio");
		JMenuItem itemOpen = new JMenuItem("Apri project.csv");
		itemOpen.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				openProj(evt);
			}
		});
		JMenuItem itemOpenMPP = new JMenuItem("Apri project.mpp");
		JMenuItem itemSetProj = new JMenuItem("Imposta Progetto");
		JMenuItem itemExit = new JMenuItem("Uscita");
		itemExit.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				setVisible(false);
				dispose();
			}
		});
		itemSetProj.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				setProj();
			}});
		itemOpenMPP.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				setMPPProj();
			}
		});
		menuArch.add(itemOpen);
		menuArch.add(itemSetProj);
		menuArch.addSeparator();
		menuArch.add(itemOpenMPP);
		menuArch.addSeparator();
		menuArch.add(itemExit);
		/*
		 * fine menu Archivio genero menu Azioni
		 */
		menu = new JMenu("Azioni");
		JMenuItem item = new JMenuItem("Genera Tutte le Uproc nuove");
		JMenuItem itemSes = new JMenuItem("Genera le Uproc di una sessione");
		JMenuItem itemCons = new JMenuItem("Consolida il project");
		JMenuItem itemNewSes = new JMenuItem("Genera le nuove sessioni");
		JMenuItem itemCsv = new JMenuItem("Genera project.csv");
		item.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				generaAllNew(evt);
			}
		});
		itemSes.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				generaSessione(evt);
			}
		});
		itemCons.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if (dbUnimat.isSet()){
				dbUnimat.consolida();
				//Eliminazione delle righe della griglia
				cleanGrid();
				}
			}
		});
		itemNewSes.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				creaSessioni();
			}	
		});
		itemCsv.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt){
				writeCSV();
			}
		
		});

		menu.add(item);
		menu.add(itemSes);
		menu.addSeparator();
		menu.add(itemNewSes);
		menu.addSeparator();
		menu.add(itemCons);
		menu.addSeparator();
		menu.add(itemCsv);
		menu.setEnabled(false);

		/*
		 * fine menu Azioni Genero menu Impostazioni
		 */
		menuImp = new JMenu("Impostazioni");
		JMenuItem itemOut = new JMenuItem("Directory di output");
	    itemMail = new JCheckBoxMenuItem("Aggiungi mailabort");
		
		itemMail.setSelected(mailAbortWrite);

		itemOut.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				setOutDir(evt);
			}
		});
		
		itemMail.addActionListener(new java.awt.event.ActionListener(){
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				setFlagMail(evt);
			}
		});
		menuImp.add(itemOut);
		menuImp.add(itemMail);
		/*
		 * fine menu Impostazioni
		 */
		menuBar.add(menuArch);
		menuBar.add(menu);
		menuBar.add(menuImp);
		setJMenuBar(menuBar);
		setSize(700, 400);
		/*
		 * JLabel emptyLabel = new JLabel("Provone");
		 * emptyLabel.setPreferredSize(new Dimension(175, 100));
		 * getContentPane().add(emptyLabel, BorderLayout.CENTER);
		 */
		modelUGrid = new DefaultTableModel();

		// Creo le colonne
		modelUGrid.addColumn("Crea");
		modelUGrid.addColumn("Riga");
		modelUGrid.addColumn("Uproc");
		modelUGrid.addColumn("Sessione");
		modelUGrid.addColumn("shell");
		modelUGrid.addColumn("Parametri");
		uprocGrid = new JTable(modelUGrid);
		uprocGrid.setPreferredScrollableViewportSize(new Dimension(640, 70));
		uprocGrid.setFillsViewportHeight(true);

		// uprocGrid.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		// uprocGrid.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		uprocGrid.getColumnModel().getColumn(0).setPreferredWidth(40);
		uprocGrid.getColumnModel().getColumn(1).setPreferredWidth(40);
		uprocGrid.getColumnModel().getColumn(2).setPreferredWidth(100);
		uprocGrid.getColumnModel().getColumn(3).setPreferredWidth(100);
		uprocGrid.getColumnModel().getColumn(4).setPreferredWidth(280);
		uprocGrid.getColumnModel().getColumn(0).setCellRenderer(
				new TableCellRenderer() {
					// the method gives the component like whome the cell must
					// be rendered
					public Component getTableCellRendererComponent(
							JTable table, Object value, boolean isSelected,
							boolean isFocused, int row, int col) {
						boolean marked = (Boolean) value;
						JCheckBox rendererComponent = new JCheckBox();
						if (marked) {
							rendererComponent.setSelected(true);
						}
						return rendererComponent;
					}
				});
		uprocGrid.getColumnModel().getColumn(0).setCellEditor(
				new CheckBoxEditor());
		JScrollPane scrollPane = new JScrollPane(uprocGrid);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		statusBar = new JLabel();
		statusBar.setPreferredSize(new Dimension(100, 16));
		statusBar.setText("Output: " + outDir);
		getContentPane().add(statusBar, BorderLayout.SOUTH);
		setDefaultLookAndFeelDecorated(true);

		setLocationRelativeTo(null);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Al momento non è usata
		// JMenuItem source = (JMenuItem)(e.getSource());

		// generaAllNew();
	}

	public void setOutDir(ActionEvent e) {
		JFileChooser chooser;
		chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle("Directory di Output");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		//
		// disable the "All files" option.
		//
		chooser.setAcceptAllFileFilterUsed(false);
		//    
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			outDir = chooser.getSelectedFile().toString();
			setConf();
		}
		statusBar.setText("Output: " + outDir);
	}

	public void openProj(ActionEvent evt) {

		JFileChooser chooser;
		chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle("Project .csv");

		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			projFile = chooser.getSelectedFile().toString();
			readProj();
		}

	}

	public void generaSessione(ActionEvent evt) {
		// Genera la sessione prescelta
		String s = (String) JOptionPane.showInputDialog(this,
				"Selezionare la sessione:", "Genera le Uproc di una sessione",
				JOptionPane.PLAIN_MESSAGE, null, dbUnimat.getSessioni(), "ham");
		if ((s != null) && (s.length() > 0)) {
			// Creo le uproc
			// System.out.println(s);
			int n = JOptionPane.showConfirmDialog(
		            null,
		            "Ricreare la sessione?",
		            "Unimat",
		            JOptionPane.YES_NO_OPTION);
			univerUproc out = new univerUproc();
			    String[] uprocGen = dbUnimat.getUprocSessione(s);

				for (int i=0;i<uprocGen.length;i++){
					out.setParam(dbUnimat.getUprocNewParam(uprocGen[i]));
					out.createFile();
					if(dbUnimat.isNew(uprocGen[i])){
						filescr.write(out.getScriptUnivNew() + "\n");
					}else{
						filescr.write(out.getScriptUniv() + "\n");
					}
				}
			if (n == JOptionPane.YES_OPTION){
				
				
				if (dbUnimat.isSet()){
						
						//System.out.println(label+"<");
						String [] uprocs = dbUnimat.getUprocSessione(s);
						String label = dbUnimat.getLabelSessione(s);
						filescr.write("uxadd ses sim ses="+s+" VSES=001 LABEL=\\\""+label+"\\\" ");
						String apl = s.substring(0,2);
						//filescr.write("HEADER="+apl+"UINIZIO FATHER="+apl+"UINIZIO \\\n");
						filescr.write("HEADER="+apl+"UINIZIO \\\n");
						for (int j=0;j<uprocs.length;j++){
							//filescr.write(" SONOK=\\(\\("+uprocs[j]+"\\)\\) \\\n");
							//filescr.write(" FATHER="+uprocs[j]);
							String depUproc = dbUnimat.getFirstDep(uprocs[j]);
							if (depUproc.length()>0){
							//filescr.write(" SONOK=\\(\\("+depUproc+"\\)\\) \\\n");
							filescr.write(" FATHER="+depUproc);
							filescr.write(" SONOK=\\(\\("+uprocs[j]+"\\)\\) \\\n");
							
							} else {
								
								//filescr.write(" SONOK=\\(\\("+uprocs[j]+"\\)\\) \\\n");
								//filescr.write(" FATHER="+apl+"uprocs[j] ");
								filescr.write(" FATHER="+apl+"UINIZIO ");
								filescr.write(" SONOK=\\(\\("+uprocs[j]+"\\)\\) \\\n");
							}
						}
						//filescr.write(" SONOK=\\(\\("+apl+"UFINE\\)\\)\n");
						filescr.write("FATHER="+uprocs[uprocs.length-1]+" SONOK=\\(\\("+apl+"UFINE\\)\\)\n");
					}
				JOptionPane.showConfirmDialog((Component) null,
						"Generazione completata", "Avviso",
						JOptionPane.DEFAULT_OPTION);
				}
				
				
			}
	}


	

	public void generaAllNew(java.awt.event.ActionEvent evt) {
		// Genera tutte le uproc selezionate
//		CsvReader proj;

		String oldSessione = "";
		
		ArrayList<String> sesCrea = new ArrayList<String>();
		if (outDir.compareTo("none") != 0) {
			univerUproc out = new univerUproc();
			out.setOutDir(outDir);
			
					for (int i = 0; i < uprocGrid.getRowCount(); i++) {
						Boolean creo = (Boolean) uprocGrid.getValueAt(i, 0);
						
						if (creo) {
							String[] params = dbUnimat.getUprocNewParam(uprocGrid
									.getValueAt(i, 2).toString());
							out.setParam(params);
							out.setMailabort(mailAbortWrite);
							out.createFile();
							filescr.write(out.getScriptUnivNew() + "\n");
							if (oldSessione.compareTo(params[14]) != 0) {
								oldSessione = params[14];
								// creo lancio
								sesCrea.add(oldSessione);
							}
						}
					}
				
				generaLaunchSession(sesCrea.toArray());
				JOptionPane.showConfirmDialog((Component) null,
						"Generazione completata", "Avviso",
						JOptionPane.DEFAULT_OPTION);
		}
	}

	private void generaLaunchSession(Object[] sessioni) {
		
		String params;
		String sespl;
		univerUproc uprocLaunch = new univerUproc();
		uprocLaunch.setOutDir(outDir);
					for (int i = 0; i < sessioni.length; i++) {
						logger.info("Generero lancio "+sessioni[i].toString());
						sespl = sessioni[i].toString().substring(0,2)+"_"+sessioni[i].toString().substring(3);
						    params = dbUnimat.getStringLaunch(sessioni[i].toString());
						    if (params.compareTo("Standard")!=0){
						    	uprocLaunch.setParam(dbUnimat.getUprocNewParam(sespl));
						    	uprocLaunch.createLanch(sespl, false);
						    }else{
						    uprocLaunch.createLanch(sespl, true);
						    }
							filescr.write(uprocLaunch.getScriptLUniv(sespl, true)+"\n");
						    
					}
			
	}

	public void readProj() {
		try {
			CsvReader proj;
			new ArrayList<String>();
			sesProj = new ArrayList<String>();
            String uprocBad = "";

			for (int i=0;i<modelUGrid.getRowCount();i++){
				modelUGrid.removeRow(0);
			}
			proj = new CsvReader(projFile);
			proj.setDelimiter(';');
			proj.setTextQualifier('"');
						proj.readRecord(); // header
			
			String input = JOptionPane.showInputDialog(null, "Nome Progetto:", "Project",
			        JOptionPane.QUESTION_MESSAGE);
			
			dbUnimat.setProj(input);
			filescr.setNewScriptFile();
			
            while (proj.readRecord()) {
				dbUnimat.storeRow(proj.getValues());
				//System.out.println("Letta riga n. "+j);
				String Uproc = proj.get(13);

				if (proj.get(1).contains("SESSIONE")) {
					int pos = proj.get(1).indexOf(':');
					String nomes = proj.get(1).substring(9, pos);
					sesProj.add(nomes);
					if (nomes.length()>10){
						uprocBad+=nomes+" nome sessione maggiore di 10 caratteri\n";
					}
					
				}
				if (Uproc.length()>10){
					uprocBad+=Uproc+" nome uproc maggiore di 10 caratteri riga: "+proj.get(0)+"\n";
				}
			}
		
			proj.close();
			ResultSet rsNew = dbUnimat.getNewUprocs();
			try {
				while(rsNew.next()){
					try{
					if (rsNew.getString("Uproc").charAt(2) != '_') {
						String sessione = rsNew.getString("Sessione");
						String shell = rsNew.getString("Nome_script");
						String param = rsNew.getString("Parametri");
						String riga = " ";
						modelUGrid.addRow(new Object[] { new Boolean(true),
								riga, rsNew.getString("Uproc"), sessione, shell, param });
		
					}
					} catch (java.lang.StringIndexOutOfBoundsException e){
						//System.out.println(rsNew.getString(3)+"  "+rsNew.getString("Uproc"));
					}
					
				}
			} catch (SQLException e) {
				
				logger.error(e.getStackTrace());
			}
			if (uprocBad.length()>0){
				logger.error("Il file mpp contiene degli errori\n"+uprocBad);
				JOptionPane.showConfirmDialog((Component) null,
						"Attenzione il project contiene degli errori:\n"+uprocBad, "Avviso",
						JOptionPane.WARNING_MESSAGE);
			}else{
				JOptionPane.showConfirmDialog((Component) null,
						"Import completato", "Avviso",
						JOptionPane.DEFAULT_OPTION);
			}
          menu.setEnabled(true);
          this.setTitle("$matic "+input);
		} catch (FileNotFoundException e) {

			logger.error(e.getStackTrace());
		} catch (java.io.IOException ioerr) {
			logger.error(ioerr.getStackTrace());;
		}
	}
	private void creaSessioni(){
		String sessioneNew = "";
		if (dbUnimat.isSet()){
			String[] sessioni=dbUnimat.getNewSession();
			for (int i=0;i<sessioni.length;i++){
				sessioneNew = sessioni[i];
				int pos = sessioneNew.indexOf(':');
				String sessioneNewDollaro = sessioneNew.substring(9, pos);
				String label = sessioneNew.substring(pos+2);
				logger.info("Genero sessione: "+label);
				String [] uprocs = dbUnimat.getUprocSessione(sessioneNewDollaro);
				filescr.write("uxadd ses sim ses="+sessioneNewDollaro+" VSES=001 LABEL=\\\""+label+"\\\" ");
				String apl = sessioneNew.substring(9,11);
				//filescr.write("HEADER="+apl+"UINIZIO FATHER="+apl+"UINIZIO \\\n");
				filescr.write("HEADER="+apl+"UINIZIO  \\\n");
				for (int j=0;j<uprocs.length;j++){
					String depUproc = dbUnimat.getFirstDep(uprocs[j]);
					if (depUproc.length()>0){
					//filescr.write(" SONOK=\\(\\("+depUproc+"\\)\\) \\\n");
					filescr.write(" FATHER="+depUproc);
					filescr.write(" SONOK=\\(\\("+uprocs[j]+"\\)\\) \\\n");
					
					} else {
						
						//filescr.write(" SONOK=\\(\\("+uprocs[j]+"\\)\\) \\\n");
						//filescr.write(" FATHER="+apl+"uprocs[j] ");
						filescr.write(" FATHER="+apl+"UINIZIO ");
						filescr.write(" SONOK=\\(\\("+uprocs[j]+"\\)\\) \\\n");
					}
				}
				//filescr.write(" SONOK=\\(\\("+apl+"UFINE\\)\\)\n");
				filescr.write("FATHER="+uprocs[uprocs.length-1]+" SONOK=\\(\\("+apl+"UFINE\\)\\)\n");
			}
			logger.info("Sessione generata");
			JOptionPane.showConfirmDialog((Component) null,
					"Generazione completata", "Avviso",
					JOptionPane.DEFAULT_OPTION);
		}
	}
	
	private void cleanGrid(){
		int righe=modelUGrid.getRowCount();
		for (int i=0;i<righe;i++){
			modelUGrid.removeRow(0);
		}
	}
	private void setProj(){
		
		String input = (String) JOptionPane.showInputDialog(this,
				"Selezionare il progetto", "Imposta Project",
				JOptionPane.PLAIN_MESSAGE, null, dbUnimat.getProjects(), "ham");
		if ((input != null) && (input.length() > 0)){
		dbUnimat.setProjStd(input);
		filescr.setNewScriptFile();
		cleanGrid();
		ResultSet rsNew = dbUnimat.getNewUprocs();
		try {
			while(rsNew.next()){
				try{
				if (rsNew.getString("Uproc").charAt(2) != '_') {
					String sessione = rsNew.getString("Sessione");
					String shell = rsNew.getString("Nome_script");
					String param = rsNew.getString("Parametri");
					String riga = " ";
					modelUGrid.addRow(new Object[] { new Boolean(true),
							riga, rsNew.getString("Uproc"), sessione, shell, param });
	
				}
				} catch (java.lang.StringIndexOutOfBoundsException e){
					/*
					for (int i=1;i<=7;i++){
					System.out.print("["+i+"]"+rsNew.getString(i)+" | ");
					}
					System.out.println();*/
					}
				
			}
			menu.setEnabled(true);
			this.setTitle("$matic "+input);
			JOptionPane.showConfirmDialog((Component) null,
					"Impostato il Project "+input, "Avviso",
					JOptionPane.DEFAULT_OPTION);

		} catch (SQLException e) {
			logger.error("Errore SQL");
			logger.error(e.getStackTrace());
		}
		}
	}


private void setMPPProj(){
	JFileChooser chooser;
	chooser = new JFileChooser();
	chooser.setCurrentDirectory(new java.io.File("."));
	chooser.setDialogTitle("Project .mpp");

	if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
		projFile = chooser.getSelectedFile().toString();
		readMPP(projFile);
		
	}

}

private void readMPP(String nomefile){
	ProjectReader reader = new MPPReader();
	sesProj = new ArrayList<String>();
	String rigaTask[];
	String uprocBad = "";
	String errEstraz = "";
	rigaTask = new String[19];
	String input = JOptionPane.showInputDialog(null, "Nome Progetto:", "Project",
	        JOptionPane.QUESTION_MESSAGE);
	try {
		ProjectFile project = reader.read(nomefile);
		dbUnimat.setProj(input);
		filescr.setNewScriptFile();
		
		for ( Task task : project.getAllTasks()){
			rigaTask[0] = task.getID().toString();
			rigaTask[1] = task.getName();
			rigaTask[2] = task.getText15();
			// Estraggoi le rowid dei predecessori
			java.util.List<Relation> predecessors = task.getPredecessors();
			String predecessori = "\"";
			   if (predecessors != null && predecessors.isEmpty() == false)
			   {
			      logger.info(task.getName() + " predecessors:");
			      for (Relation relation : predecessors)
			      {
			         predecessori += relation.getTargetTask().getID().toString();
			         predecessori += ";";
			       
			      }
			   }
			   predecessori += "\"";
			rigaTask[3] = predecessori;
			rigaTask[4] = task.getText12();
			rigaTask[5] = task.getText4();
			rigaTask[6] = task.getText3();
			rigaTask[7] = task.getText1();
			rigaTask[8] = task.getText5();
			rigaTask[9] = task.getText6();
			rigaTask[10] = task.getText7();
			rigaTask[11] = task.getText8();
			rigaTask[12] = task.getText2();
			rigaTask[13] = task.getText27();
			rigaTask[14] = task.getText26();
			rigaTask[15] = task.getText9();
			rigaTask[16] = task.getText10();
			rigaTask[17] = task.getText11();
			rigaTask[18] = task.getText22();
			
			// Sistemo i null sulle strings
			for (int i=0;i<rigaTask.length;i++){
				if (rigaTask[i] == null){
					rigaTask[i] = "";
				}
			}
			dbUnimat.storeRow(rigaTask);
			String Uproc = rigaTask[13];
			try{
			if (task.getName().contains("SESSIONE")) {
				int pos = task.getName().indexOf(':');
				String nomes = "";
				try {
				nomes = task.getName().substring(9, pos);
				nomes = nomes.replace(" ", "");
				} catch (java.lang.StringIndexOutOfBoundsException e) 
				{
					errEstraz += task.getName()+"\n";

				}
				
				sesProj.add(nomes);
				if (nomes.length()>10){
					uprocBad+=nomes+" nome sessione maggiore di 10 caratteri\n";
				}
				
				
			}
			if (Uproc.length()>10){
				uprocBad+=Uproc+" nome uproc maggiore di 10 caratteri riga: "+task.getID()+"\n";
			}
			} catch (java.lang.NullPointerException e) {
				logger.error("Errore getTaskName riga: "+task.getID().toString());
				logger.error(e.getStackTrace());
			}
		
		}
		menu.setEnabled(true);
		this.setTitle("$matic "+input);
	} catch (MPXJException e) {
		JOptionPane.showConfirmDialog((Component) null,
				"Errore Import ", "Avviso",
				JOptionPane.DEFAULT_OPTION);
		e.printStackTrace();
		logger.error("Errore import mpp");
		logger.error(e.getStackTrace());
	}
	
	ResultSet rsNew = dbUnimat.getNewUprocs();
	try {
		while(rsNew.next()){
			try{
			if (rsNew.getString("Uproc").charAt(2) != '_') {
				String sessione = rsNew.getString("Sessione");
				String shell = rsNew.getString("Nome_script");
				String param = rsNew.getString("Parametri");
				String riga = " ";
				modelUGrid.addRow(new Object[] { new Boolean(true),
						riga, rsNew.getString("Uproc"), sessione, shell, param });

			}
			} catch (java.lang.StringIndexOutOfBoundsException e){
				//System.out.println(rsNew.getString(3)+"  "+rsNew.getString("Uproc"));
				logger.warn("Mancana val Uproc "+rsNew.getString(3));
			}

		}
	} catch (SQLException e) {
		logger.error("Errore SQ GetNew");
		logger.error(e.getStackTrace());
	}
	
	if (errEstraz.length()>0){
		JOptionPane.showConfirmDialog((Component) null,
				"Attenzione ci sono sessioni non definite:\n"+errEstraz,"Avviso",
				JOptionPane.WARNING_MESSAGE);
	}
	if (uprocBad.length()>0){
		JOptionPane.showConfirmDialog((Component) null,
				"Attenzione il project contiene degli errori:\n"+uprocBad, "Avviso",
				JOptionPane.WARNING_MESSAGE);
	}else{
		JOptionPane.showConfirmDialog((Component) null,
				"Import completato", "Avviso",
				JOptionPane.DEFAULT_OPTION);
	}

	 
}

private void writeCSV(){
	JFileChooser chooser;
	String outDirCsv;
	FileOutputStream outCsv;
	PrintStream posStreamCsv;
	chooser = new JFileChooser();
	chooser.setCurrentDirectory(new java.io.File("."));
	chooser.setDialogTitle("Directory di Output");
	chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	int numCols = 19;
	//
	// disable the "All files" option.
	//
	chooser.setAcceptAllFileFilterUsed(false);
	//    
	if (dbUnimat.isSet()){
	if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
		outDirCsv = chooser.getSelectedFile().toString();
		ResultSet rs = dbUnimat.getAllForCsv();
	    ResultSetMetaData rsmd;
		try {
			rsmd = rs.getMetaData();
			numCols = rsmd.getColumnCount(); 
		} catch (SQLException e1) {
			logger.error("Errore SQL");
			logger.error(e1.getStackTrace());
		} 
	     
		try {
			File f = new File(outDirCsv, "project.csv");
			try {
				outCsv = new FileOutputStream(f);
				posStreamCsv = new PrintStream(outCsv);
				while(rs.next()){
					// Output
					for (int campo=1; campo<=numCols;campo++){
						posStreamCsv.print(rs.getString(campo));
						posStreamCsv.print(";");
					}
					posStreamCsv.print("\n");
					}
				JOptionPane.showConfirmDialog((Component) null,
						"File project.csv generato", "Avviso",
						JOptionPane.DEFAULT_OPTION);
			} catch (FileNotFoundException e) {
				JOptionPane.showConfirmDialog((Component) null,
						"Errore scrittura file csv:\n"+e.getMessage(), "Errore",
						JOptionPane.ERROR_MESSAGE);
				logger.error("Errore scrittura per il file project.csv");
				logger.error(e.getStackTrace());
			}
			
			
		} catch (SQLException e) {
			logger.error("Errore SQL");
			logger.error(e.getStackTrace());
		}
	}

}

}
private void setFlagMail(ActionEvent evt){
 if (mailAbortWrite){
	 mailAbortWrite = false;
 } else {
	 mailAbortWrite = true;
 }
 setConf();
}
}
