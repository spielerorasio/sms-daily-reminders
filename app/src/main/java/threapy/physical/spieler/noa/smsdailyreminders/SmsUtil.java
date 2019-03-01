package threapy.physical.spieler.noa.smsdailyreminders;

import android.Manifest;
import android.content.Context;
import android.telephony.SmsManager;
import android.widget.Toast;

public class SmsUtil {

    private PermissionUtil permissionUtil;
    private Context applicationContext;

    public SmsUtil(PermissionUtil permissionUtil, Context applicationContext) {
        this.permissionUtil = permissionUtil;
        this.applicationContext = applicationContext;
    }

    public void sendSms(String phoneNo, String message) {
        permissionUtil.getUserPermission(PermissionUtil.MY_PERMISSIONS_REQUEST_SEND_SMS, Manifest.permission.SEND_SMS);

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, message, null, null);
        } catch (Exception e) {
            Toast.makeText(applicationContext,
                    "SMS failed, please try again later!",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
