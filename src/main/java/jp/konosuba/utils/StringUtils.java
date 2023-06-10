package jp.konosuba.utils;

import jp.konosuba.data.message.MessageObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class StringUtils {


    public static String generateCodeStatic(int length){
        String [] keys = {"Q","W","E","R","T","Y","U","I","O","P","A"
                ,"A","S","D","F","G","H","J","q","w","e","r","t","y","u","i","$","_",".","#","@"};
        String string = "";
        for (var i =0;i<length;i++){
            string+=keys[new Random().nextInt(keys.length)];
        }
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return string;

    }
    private static final char[] hex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    public static String byteArray2Hex(byte[] bytes) {
        StringBuffer sb = new StringBuffer(bytes.length * 2);
        for(final byte b : bytes) {
            sb.append(hex[(b & 0xF0) >> 4]);
            sb.append(hex[b & 0x0F]);
        }
        return sb.toString();
    }

    public static String getStringFromSHA256(String stringToEncrypt)  {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        messageDigest.update(stringToEncrypt.getBytes());
        return byteArray2Hex(messageDigest.digest());
    }
    /*
    public static MessageAction generateMessageAction(){
        MessageAction messageAction = new MessageAction();
        messageAction.setEnd(false);
        messageAction.setHash(getStringFromSHA256(generateCodeStatic(16)));
        return messageAction;
    }

     */


    public static MessageObject fromStringToMessageObject(String str){
        String[] spl = str.split(";");
        String type = spl[0];
        String typeMessage = spl[1];
        String message = str.replace(type+";"+typeMessage+";","");
        return new MessageObject(type,typeMessage,message);
    }
}
