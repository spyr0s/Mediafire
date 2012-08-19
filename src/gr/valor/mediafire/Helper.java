package gr.valor.mediafire;

import gr.valor.mediafire.api.ApiUrls;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;

import android.util.Log;

public class Helper {
	
	public static final String TAG = "Helper";
		
	
	public static String implode(String[] array, String glue){
		String str = "";
		for (int i = 0; i < array.length; i++) {
			if(i<array.length-1){
				str += array[i]+glue;
			} else {
				str += array[i];
			}
		}
		return str;
		
	}
	
	public static String implode(List list, String glue){
		String str = "";
		int i =0;
		for (Iterator it = list.iterator(); it.hasNext();) {
			Object object = (Object) it.next();
			if(i<list.size()-1){
				str += object.toString() + glue;
			} else {
				str += object.toString();
			}
			i++;
		}
		return str;
		
	}
	
	public static String sha1(String data) {
		try {
			byte[] b = data.getBytes();
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.reset();
			md.update(b);
			byte messageDigest[] = md.digest();
			StringBuilder result = new StringBuilder();
			for (int i = 0; i < messageDigest.length; i++) {
				result.append(Integer.toString(
						(messageDigest[i] & 0xff) + 0x100, 16).substring(1));
			}
			return result.toString();
		} catch (NoSuchAlgorithmException ex) {
			Log.e(TAG, "SHA-1 is not supported");
		}
		return null;
	}
	
	public static String getSignature(String email, String password) {
		String sign = Helper.sha1(email + password + ApiUrls.APP_ID + ApiUrls.API_KEY);
		Log.d(TAG, "SHA-1 calculated:" + sign);
		return sign;
	}

}
