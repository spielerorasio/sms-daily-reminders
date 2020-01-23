package threapy.physical.spieler.noa.smsdailyreminders;

import android.Manifest;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

public class ContactsUtil {
    private static final String GOOGLE_ACCOUNT = "com.google";
    private PermissionUtil permissionUtil;
    private  ContentResolver contentResolver;


    public ContactsUtil(PermissionUtil permissionUtil, ContentResolver contentResolver) {
        this.permissionUtil = permissionUtil;
        this.contentResolver = contentResolver;

    }


    public List<ContactPerson> getContactsByName(String containsStr, String containsStr2) {

        permissionUtil.getUserPermission(PermissionUtil.MY_PERMISSIONS_REQUEST_READ_CONTACTS, Manifest.permission.READ_CONTACTS);

        List<ContactPerson> contacts = new ArrayList<>();
        String[] columnsAccount = {ContactsContract.RawContacts.CONTACT_ID, ContactsContract.RawContacts.ACCOUNT_TYPE};

        String[] columns = {ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER};
        String selection = "DISPLAY_NAME LIKE  '%" + containsStr.replace("'","") ;
        if(selection!=null){
            selection += "%' AND DISPLAY_NAME LIKE  '%" + containsStr2 + "%'";
        }

        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, columns, selection, null, null);

        int indexId = cursor.getColumnIndex(ContactsContract.Contacts._ID);
        int displayName = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
        int hasPhoneNumber = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);

        while(cursor.moveToNext()) {
            String id = cursor.getString(indexId);
            String name = cursor.getString(displayName);
            String has_phone = cursor.getString(hasPhoneNumber);
            boolean isGoogle = false;
            Cursor rawContactsCursor = contentResolver.query(ContactsContract.RawContacts.CONTENT_URI, columnsAccount, "_id="+id, null, null);
            while(rawContactsCursor.moveToNext()) {
                int accountType = rawContactsCursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE);
                String account_type = rawContactsCursor.getString(accountType);
                isGoogle = GOOGLE_ACCOUNT.equals(account_type);
            }


//            System.out.println(name);
            if(! has_phone.endsWith("0")  && isGoogle)  {
                ContactPerson contact = getContactDetails(id);
                if(contact!=null){
                    contact.setName(name);
                    contacts.add(contact) ;
                }
            }
        }

        cursor.close();
        return contacts;
    }

    private ContactPerson getContactDetails(String id)  {
        Cursor phones = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
        ContactPerson contactPerson = new ContactPerson();
        try{
            if(phones.getCount() > 0) {
                while(phones.moveToNext()) {
                    String nextNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    if(nextNumber.length()<8 && (nextNumber.startsWith("+972") || nextNumber.startsWith("972"))){
                        for (int i = 1; i < 10; i++) {
                            String phonesString = phones.getString(phones.getColumnIndex("data" + i));
                            if(phonesString!=null && phonesString.length()>6 && phonesString.startsWith("5")){
                                nextNumber +=  phonesString.trim();
                                break;
                            }
                        }
                    }
                    nextNumber = nextNumber.replace(" ","");
                    if(nextNumber.length()==9 && nextNumber.startsWith("5")){
                        nextNumber = "0"+nextNumber;
                    }
                    if(isCellPhone(nextNumber)){
                        contactPerson.setPhone(nextNumber);
                    }
                    String email = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
                    if(email.indexOf('@')!=-1){
                        contactPerson.setEmail(email);
                    }
                }
            }
        }finally {
            phones.close();
        }
        return contactPerson;
    }


    public boolean isCellPhone(String numberStr){
        return numberStr!=null && (numberStr.startsWith("05") ||  numberStr.startsWith("+9725"));
    }
}
