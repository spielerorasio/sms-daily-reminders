package threapy.physical.spieler.noa.smsdailyreminders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class LogFileHandler {
    private  static String LOG_FILE = "/smsRemindersLogFile.log";
    private  static String LINE_DOWN = "\r\n ";

    private  static File logFile;
    private  File filesDir;
    private  StringBuilder report;

    public LogFileHandler(File filesDir) {
        this.report = new StringBuilder();
        this.filesDir = filesDir;
    }

    public boolean isReportEmpty(){
        return report.toString().isEmpty();
    }

    public String printStackTrace(Exception exp){
        StringWriter errors = new StringWriter();
        exp.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }

    public void printStackTraceToLog(Exception exp){
        append(printStackTrace(exp));
        writeToLogFile();
    }

    public void createLogFile(){
        try {
            logFile = new File(filesDir.getAbsolutePath() + LOG_FILE);
            if(logFile.exists()){
                logFile.delete();
            }
            logFile.createNewFile();
            this.report = new StringBuilder();
        } catch (IOException exp) {
            exp.printStackTrace();
        }
    }

    public String  readFromLogFile(){
        if(logFile==null){
            return "";
        }
        try{
            StringBuffer fileData = new StringBuffer();
            BufferedReader reader = new BufferedReader(new FileReader(logFile));
            char[] buf = new char[1024];
            int numRead=0;
            while((numRead=reader.read(buf)) != -1){
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
            }
            reader.close();
            return fileData.toString();

        } catch (Exception exp) {
            exp.printStackTrace();
            return null;
        }
    }


    public void writeToLogFile(){
        if(logFile==null){
            createLogFile();
        }
        try{
            PrintWriter printWriter = new PrintWriter(logFile);
            printWriter.print(report.toString());
            printWriter.close();
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }


    public void append(String str) {
        report.append(str).append(LINE_DOWN);
    }
}
