package threapy.physical.spieler.noa.smsdailyreminders;

import android.support.annotation.NonNull;

import com.evernote.android.job.DailyJob;
import com.evernote.android.job.JobRequest;

import java.util.concurrent.TimeUnit;

public class SmsReminderJob extends DailyJob {
    public static final String TAG = "SmsReminderDailyJob";


    public static int schedule() {
        long time2run = TimeUnit.HOURS.toMillis(20)+TimeUnit.MINUTES.toMillis(45);
        long time2MustStop = TimeUnit.HOURS.toMillis(20)+TimeUnit.MINUTES.toMillis(46);
//        long time2run = TimeUnit.HOURS.toMillis(10)+TimeUnit.MINUTES.toMillis(36);
//        long time2MustStop = TimeUnit.HOURS.toMillis(10)+TimeUnit.MINUTES.toMillis(37);
        JobRequest.Builder baseBuilder = new JobRequest.Builder(TAG);
        baseBuilder = baseBuilder.setUpdateCurrent(true).setRequiredNetworkType(JobRequest.NetworkType.ANY).setRequiresCharging(false);
        return DailyJob.schedule(baseBuilder, time2run, time2MustStop);
      }

    @NonNull
    @Override
    protected DailyJobResult onRunDailyJob(@NonNull Params params) {
        ReminderExecutor.getReminderExecutor().startSendingReminders(true, 1, false, null,null);
        return DailyJobResult.SUCCESS;
    }
}
