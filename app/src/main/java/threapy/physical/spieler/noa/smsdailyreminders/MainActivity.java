package threapy.physical.spieler.noa.smsdailyreminders;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    public final static String TAG = "smsReminders";

    private LogFileHandler logFileHandler;
    private CalendarUtil calendarUtil;
    private ContactsUtil contactsUtil;
    private PermissionUtil permissionUtil;
    private SmsUtil smsUtil;
    private StorageMap storageMap;


    private Button scheduleBtn;
    private Button stopScheduleBtn;
    private ReminderExecutor reminderExecutor;
    private EmailUtil emailUtil;

    public void init(){

        //https://gist.github.com/mjohnsullivan/403149218ecb480e7759
        Context applicationContext = getApplicationContext();
        ContentResolver contentResolver = getContentResolver();

        storageMap = new StorageMap(applicationContext.getFilesDir());
        emailUtil = new EmailUtil();
        permissionUtil = new PermissionUtil(this, this);
        logFileHandler = new LogFileHandler(applicationContext.getFilesDir());
        calendarUtil = new CalendarUtil(permissionUtil, contentResolver);
        contactsUtil = new ContactsUtil(permissionUtil, contentResolver);
        smsUtil = new SmsUtil(permissionUtil, applicationContext);

        reminderExecutor = ReminderExecutor.getReminderExecutor();
        reminderExecutor.setEmailUtil(emailUtil);
        reminderExecutor.setLogFileHandler(logFileHandler);
        reminderExecutor.setContactsUtil(contactsUtil);
        reminderExecutor.setCalendarUtil(calendarUtil);
        reminderExecutor.setSmsUtil(smsUtil);
        reminderExecutor.setStorageMap(storageMap);
        reminderExecutor.setTimeToRun( CalendarUtil.getTimeForHourAndMinutes(20, 45));

        if (!isMyServiceRunning(ReminderService.class)) {
            startService(new Intent(this, ReminderService.class));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try{
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Starting and binding service");
            }
            Intent i = new Intent(this, ReminderService.class);
            startService(i);
        }catch (Exception exp){
            logFileHandler.printStackTraceToLog(exp);
            smsUtil.sendSms("0545900855", logFileHandler.printStackTrace(exp));
        }
    }



    @Override
    protected void onStop() {
        try{
            super.onStop();
        }catch (Exception exp){
            logFileHandler.printStackTraceToLog(exp);
            smsUtil.sendSms("0545900855", logFileHandler.printStackTrace(exp));
        }
    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("Service status", "Running");
                return true;
            }
        }
        Log.i ("Service status", "Not running");
        return false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try{

            setContentView(R.layout.activity_main);

            if(permissionUtil==null){
                init();
            }

            //reportOnly
            Button reportOnly = findViewById(R.id.reportOnly);
            reportOnly.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "reportOnly button was clicked");
                    setMessageAndNumber();
                    reminderExecutor.startSendingReminders(false, 1, true, getApplicationContext(), MainActivity.this);//send4tomorrow();
                }
            });

            //emailReport
            Button emailReport = findViewById(R.id.emailReport);
            emailReport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "emailReport button was clicked");
                    String readFromLogFile = logFileHandler.readFromLogFile();
                    emailUtil.sendEmail("spieler.orasio@gmail.com","Log File Sms Reminders", readFromLogFile, MainActivity.this);
                }
            });


            //schedule button
            scheduleBtn = findViewById(R.id.scheduleBtn);
            scheduleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "schedule button was clicked");
                    setMessageAndNumber();
                    startService(new Intent(MainActivity.this, ReminderService.class));
                    scheduleBtn.setEnabled(false);
                    stopScheduleBtn.setEnabled(true);
                    reminderExecutor.setScheduleEnabled(true);
                }
            });
            scheduleBtn.setEnabled(false);


            //stop schedule button
            stopScheduleBtn = findViewById(R.id.stopScheduleBtn);
            stopScheduleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "stop schedule button was clicked");
                    setMessageAndNumber();

                    stopService(new Intent(MainActivity.this, ReminderService.class));
                    scheduleBtn.setEnabled(true);
                    stopScheduleBtn.setEnabled(false);
                    reminderExecutor.setScheduleEnabled(false);
                }
            });
            stopScheduleBtn.setEnabled(true);




            //send 4 tomorrow
            Button send4tomorrow = findViewById(R.id.send4tomorrow);
            send4tomorrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "send 4 tomorrow button was clicked");
                    setMessageAndNumber();
                    reminderExecutor.startSendingReminders(false,1, false, getApplicationContext(), MainActivity.this);//send4tomorrow();
                }
            });




            //send 4 today
            Button send4today = findViewById(R.id.send4today);
            send4today.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "send 4 today button was click");
                    setMessageAndNumber();
                    reminderExecutor.startSendingReminders(false, 0, false, getApplicationContext(), MainActivity.this);//send4today();
                }
            });




        }catch (Exception exp){
            logFileHandler.printStackTraceToLog(exp);
            smsUtil.sendSms("0545900855", logFileHandler.printStackTrace(exp));
        }
    }

    private void setMessageAndNumber(){
        EditText contentMessage = findViewById(R.id.contentMessage);
        EditText contentReport = findViewById(R.id.contentReport);

        reminderExecutor.setMessage(contentMessage.getText().toString());
        reminderExecutor.setSelfNumber(contentReport.getText().toString());


        EditText timeText = findViewById(R.id.time);
        String timeToSendInSchedule = timeText.getText().toString();

        int hour = Integer.parseInt(timeToSendInSchedule.substring(0, timeToSendInSchedule.indexOf(":")));
        int minutes = Integer.parseInt(timeToSendInSchedule.substring(timeToSendInSchedule.indexOf(":")+1));
        reminderExecutor.setTimeToRun( CalendarUtil.getTimeForHourAndMinutes(hour, minutes));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        if(permissionUtil==null){
            init();
        }
        permissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
