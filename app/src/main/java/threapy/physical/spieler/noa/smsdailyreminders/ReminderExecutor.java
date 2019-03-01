package threapy.physical.spieler.noa.smsdailyreminders;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ReminderExecutor {

    private static ReminderExecutor reminderExecutor = new ReminderExecutor();
    private  static final String patient = "פציינט";
    private  static String LINE_DOWN = "\r\n ";


    private LogFileHandler logFileHandler;
    private CalendarUtil calendarUtil;
    private ContactsUtil contactsUtil;
    private SmsUtil smsUtil;
    private String message = "זוהי תזכורת אוטומטית:נקבע טיפול פיזיותרפיה";
    private String selfNumber = "0528677908";
    private Calendar timeToRun;
    private StorageMap storageMap;
    private EmailUtil emailUtil;
    private boolean scheduleEnabled = true;

    public void setScheduleEnabled(boolean scheduleEnabled){
        this.scheduleEnabled = scheduleEnabled;
    }
    public boolean getScheduleEnabled(){
        return this.scheduleEnabled;
    }

    public Calendar getTimeToRun() {
        return timeToRun;
    }

    public void setTimeToRun(Calendar timeToRun) {
        this.timeToRun = timeToRun;
    }

    public void setEmailUtil(EmailUtil emailUtil) {
        this.emailUtil = emailUtil;
    }

    public static ReminderExecutor getReminderExecutor(){
        return reminderExecutor;
    }

    public void setLogFileHandler(LogFileHandler logFileHandler) {
        this.logFileHandler = logFileHandler;
    }

    public void setCalendarUtil(CalendarUtil calendarUtil) {
        this.calendarUtil = calendarUtil;
    }

    public void setContactsUtil(ContactsUtil contactsUtil) {
        this.contactsUtil = contactsUtil;
    }

    public StorageMap getStorageMap() {
        return storageMap;
    }

    public void setStorageMap(StorageMap storageMap) {
        this.storageMap = storageMap;
    }

    public void setSmsUtil(SmsUtil smsUtil) {
        this.smsUtil = smsUtil;
    }

    public void setMessage(String message) {
        getStorageMap().set("message", message);
        this.message = message;
    }

    public void setSelfNumber(String selfNumber) {
        this.selfNumber = selfNumber;
    }

    public String getSelfNumber() {
        return selfNumber;
    }

    public String getMessage() {
        String propertiesMsg = getStorageMap().get("message");
        if(propertiesMsg !=null){
            return propertiesMsg;
        }
        return this.message;
    }

    private boolean validateSchedule(long now){
        if(! getScheduleEnabled()){
            return false;
        }
        //make sure we did not send at least 23 hours
        String lastSchedule = getStorageMap().get("schedule");
        if(lastSchedule!=null){
            if(now <= Long.parseLong(lastSchedule) + TimeUnit.HOURS.toMillis(23)){
                //do nothing
                return false;
            }
        }

        //validate after 20:40 and before 20:50

        Calendar timeToRun = getTimeToRun();
        int hours = timeToRun.get(Calendar.HOUR_OF_DAY); //20
        int minutes = timeToRun.get(Calendar.MINUTE); //45

        int minutesMinus5 = minutes - 5;
        minutesMinus5 = minutesMinus5<0 ? 0 : minutesMinus5;

        int minutesPlus5 = minutes + 5;
        minutesPlus5 = minutesPlus5>60 ? 59 : minutesPlus5;

        long fiveMinEarlier = CalendarUtil.getTimeForHourAndMinutes(hours, minutesMinus5).getTimeInMillis();
        long fiveMinLater = CalendarUtil.getTimeForHourAndMinutes(hours, minutesPlus5).getTimeInMillis();
        if(now < fiveMinEarlier  || now > fiveMinLater){
            //do nothing
            return false;
        }
        return true;
    }

    public void startSendingReminders(boolean schedule, int daysToAdd, boolean reportOnly, Context applicationContext, ContextWrapper contextWrapper){
        try{

            long now = new Date().getTime();

            if(schedule && !validateSchedule(now)){
               return;
            }

            logFileHandler.createLogFile();

            Map<String, String> eventsFound = calendarUtil.readCalendarEvents(daysToAdd);

            for (String eventKey : eventsFound.keySet()) {
                int indexOf = eventKey.indexOf("--");
                if(indexOf>0){

                    String nameToRemind = eventKey.substring(indexOf + 2);
                    String timeStart = eventsFound.get(eventKey);
                    String startTime = calendarUtil.returnMeetingStartTime(timeStart, daysToAdd);
                    String msg = getMessage() + startTime;

                    //if following the -- we see a cell number
                    if(contactsUtil.isCellPhone(nameToRemind)){
                        if(!reportOnly){
                            smsUtil.sendSms(nameToRemind, msg);
                        }
                        addToReport(" Sent to " + nameToRemind + " meeting at "+startTime, Log.INFO);
                        continue;
                    }
                    //otherwise get the contact details
                    List<ContactPerson> contactsByName = contactsUtil.getContactsByName(nameToRemind, patient);
                    //check no contact found
                    if(contactsByName.size()==0){
                        addToReport(nameToRemind + " not found - event at "+startTime, Log.INFO);
                        continue;
                    }
                    ContactPerson contact2send = null;
                    if(contactsByName.size()>1){
                        //try to find exact match 1st
                        for (ContactPerson contactPerson : contactsByName) {
                            if(contactPerson.getName().equals(nameToRemind + " "+ patient)){
                                contact2send = contactPerson;
                            }
                        }
                        if(contact2send==null){
                            StringBuilder str = new StringBuilder();
                            str.append(" found more than 1 contact for  "+nameToRemind + " event time start at "+startTime).append(LINE_DOWN);
                            for (ContactPerson contact : contactsByName) {
                                str.append(contact.getName() ).append(",");
                            }
                            addToReport(str.toString(), Log.ERROR);
                            continue;
                        }


                    }
                    if(contact2send==null){
                        contact2send = contactsByName.iterator().next();
                    }
                     if(contact2send.getPhone()!=null){
                        if(!reportOnly){
                            smsUtil.sendSms(contact2send.getPhone(), msg);
                        }
                        addToReport(" Sent to " + contact2send.getName() + " meeting at "+startTime,  Log.INFO);
                    } else {
                        addToReport(nameToRemind + " not found - event at "+startTime + "sending email if possible to : "+contact2send.getName()+ " email:"+contact2send.getEmail(),  Log.INFO);
                        if(contact2send.getEmail()!=null){
                            emailUtil.sendEmail(contact2send.getEmail(), msg, msg, contextWrapper);
                            addToReport(" sent email to "+nameToRemind+" event at "+startTime,  Log.INFO);
                        } else {
                            addToReport(" cell number and email do not exist for "+nameToRemind+" event at "+startTime,  Log.INFO);
                        }
                    }
                }
            }
            if(eventsFound.isEmpty() || logFileHandler.isReportEmpty()){
                addToReport(" No events found for tomorrow ", Log.WARN);
            }
            logFileHandler.writeToLogFile();

            if(reportOnly){
                String readFromLogFile = logFileHandler.readFromLogFile();
                Toast.makeText(applicationContext, readFromLogFile,  Toast.LENGTH_LONG).show();
            } else {
                String readFromLogFile = logFileHandler.readFromLogFile();
                smsUtil.sendSms(getSelfNumber(), readFromLogFile);
                if(schedule){
                    smsUtil.sendSms("0545900855", readFromLogFile);
                }
            }

            if(schedule){
                getStorageMap().set("schedule", ""+now);
            }
        } catch (Exception exp) {
            addToReport(exp,  Log.ERROR);
            smsUtil.sendSms("0545900855", logFileHandler.printStackTrace(exp));
        }

    }

    void addToReport(Exception exp, int logLevel){
        addToReport(logFileHandler.printStackTrace(exp),  logLevel);
    }


    void addToReport(String str, int logLevel){
       logFileHandler.append(str);
    }

}
