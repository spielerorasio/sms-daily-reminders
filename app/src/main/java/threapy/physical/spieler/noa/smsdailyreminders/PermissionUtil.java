package threapy.physical.spieler.noa.smsdailyreminders;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class PermissionUtil {
    public static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    public static final int MY_PERMISSIONS_REQUEST_READ_CALENDAR = 1;
    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 2;

    private Context context;
    private Activity activity;

    public PermissionUtil(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public void getUserPermission(int permissionRequested, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, permissionRequested );
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            } case MY_PERMISSIONS_REQUEST_READ_CALENDAR:{
                if (grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            } case MY_PERMISSIONS_REQUEST_READ_CONTACTS:{
                if (grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
        }

    }

}
