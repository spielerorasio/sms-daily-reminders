package threapy.physical.spieler.noa.smsdailyreminders;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CalendarUtil {
    private static final long TWENTY_FOUR_HOURS = TimeUnit.HOURS.toMillis(24);

    private PermissionUtil permissionUtil;
    private ContentResolver contentResolver;
    private static final String TOMORROW = " מחר בשעה ";
    private static final String TODAY = " היום בשעה ";

    public CalendarUtil(PermissionUtil permissionUtil, ContentResolver contentResolver) {
        this.permissionUtil = permissionUtil;
        this.contentResolver = contentResolver;
    }
    public static Calendar getTimeForHourAndMinutes(int hour, int minutes) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, hour);
        today.set(Calendar.MINUTE, minutes);
        today.set(Calendar.SECOND, 0);
        return today;
    }


    public Map<String, String> readCalendarEvents(int daysToAdd) {

        permissionUtil.getUserPermission(PermissionUtil.MY_PERMISSIONS_REQUEST_READ_CALENDAR, Manifest.permission.READ_CALENDAR);


        String[] projection = {CalendarContract.Events._ID, CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART};


        Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI.buildUpon();


        Calendar calendar = getTime(new Date().getTime()) ;
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE),0,0, 0);
        long startingTime = calendar.getTimeInMillis() + (daysToAdd*TWENTY_FOUR_HOURS) ;


        long endTime = startingTime + TWENTY_FOUR_HOURS;
        ContentUris.appendId(eventsUriBuilder, startingTime);
        ContentUris.appendId(eventsUriBuilder, endTime);
        Uri eventsUri = eventsUriBuilder.build();

        Cursor cursor = contentResolver.query( eventsUri, projection, null, null,null);
        // Get the index of the columns.
        int nameIdx = cursor.getColumnIndexOrThrow(CalendarContract.Events.TITLE);
        int idIdx = cursor.getColumnIndexOrThrow(CalendarContract.Events._ID);
        int start = cursor.getColumnIndexOrThrow(CalendarContract.Events.DTSTART);
        // Initialize the result set.
        Map<String, String> result = new HashMap<>(cursor.getCount());
        // Iterate over the result Cursor.
        while (cursor.moveToNext()) {
            // Extract the name.
            String name = cursor.getString(nameIdx);
            String startTime = cursor.getString(start);
            Long eventStartTime = Long.valueOf(startTime);
            // Extract the unique ID.
//            String id = cursor.getString(idIdx);
            if(startingTime<eventStartTime && endTime>eventStartTime){
                result.put(  name , startTime);
            }

        }
        // Close the Cursor.
        cursor.close();
        return result;
    }


    private Calendar getTime(long time){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return  calendar;
    }


    public String returnMeetingStartTime(String timeStart,int daysToAdd){
        //missing date

        Calendar calendar = getTime(Long.parseLong(timeStart));

        String date = daysToAdd==1 ? TOMORROW : TODAY;
//        String date = " "+time.monthDay + "/" + (time.month + 1) + "/" + time.year + " ";
        if(calendar.get(Calendar.MINUTE)==0){
            return date + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + "0";
        }
        return date + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
    }

}
