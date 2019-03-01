package threapy.physical.spieler.noa.smsdailyreminders;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;
import com.evernote.android.job.JobManager;

/**
 * Created by TutorialsPoint7 on 8/23/2016.
 */

public class ReminderService extends Service {

    private boolean isScheduled = false;

    private void cancelScheduleDailyJob(){
        if(isScheduled){
            JobManager.instance().cancelAll();
            isScheduled = false;
        }
    }

    private void scheduleDailyJob(){

        JobManager.create(this).addJobCreator(new JobCreator() {
            @Nullable
            @Override
            public Job create(@NonNull String tag) {
                switch (tag) {
                    case SmsReminderJob.TAG:
                        return new SmsReminderJob();
                    default:
                        return null;
                }
            }
        });

        SmsReminderJob.schedule();
         isScheduled = true;
    }

    private void startSchedulingSmsReminders(){
        if(isScheduled) {
            return;
        }
        cancelScheduleDailyJob();
        scheduleDailyJob();

    }


    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground(); //it appears that you in 8.1 must create your own notification channel.
        else
            startForeground(2, new Notification());


    }

    private void startMyOwnForeground(){
        String name = ReminderService.class.getPackage().getName();
        String NOTIFICATION_CHANNEL_ID = name;
        String channelName = name;
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_light)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    /** The service is starting, due to a call to startService() */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startSchedulingSmsReminders();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /** Called when The service is no longer used and is being destroyed */
    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelScheduleDailyJob();
        Intent broadcastIntent = new Intent("threapy.physical.spieler.noa.smsdailyreminders.ReminderService.RestartSensor");
        sendBroadcast(broadcastIntent);
    }


}