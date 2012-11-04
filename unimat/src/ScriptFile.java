import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class ScriptFile {
	private String outDir = ".";
	private FileOutputStream out;
	private PrintStream posStream;
	private static String nomefileOut = "script_dollaro.ksh";

	public void setOutDir(String outDir) {
		this.outDir = outDir;
	}

	public String getOutDir() {
		return outDir;
	}

	public void setNewScriptFile() {
		File f = new File(outDir, nomefileOut);
		try {
			out = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.err.println("Errore apertura file di output");
		}
		posStream = new PrintStream(out);
	}

	public void write(String s) {
		posStream.print(s);
	}

	public void close() {
		posStream.flush();
		try {
			if (out != null) {
				out.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
