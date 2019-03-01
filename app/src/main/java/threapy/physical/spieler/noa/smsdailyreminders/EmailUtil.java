package threapy.physical.spieler.noa.smsdailyreminders;

import android.content.ContextWrapper;
import android.content.Intent;

public class EmailUtil {

    public void sendEmail(String emailRecipient, String subject, String body, ContextWrapper contextWrapper){
        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{emailRecipient});
//        email.putExtra(android.content.Intent.EXTRA_CC, aEmailCCList);
        email.putExtra(Intent.EXTRA_SUBJECT, subject);
        email.putExtra(Intent.EXTRA_TEXT, body);
        email.setType("message/rfc822");
//        startActivity(email);
        contextWrapper.startActivity(Intent.createChooser(email, "Choose email sender..."));
    }

}
