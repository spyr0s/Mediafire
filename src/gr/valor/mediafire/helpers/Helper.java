package gr.valor.mediafire.helpers;

import gr.valor.mediafire.api.ApiUrls;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Helper {

	public static final String TAG = "Helper";

	public static String implode(String[] array, String glue) {
		String str = "";
		for (int i = 0; i < array.length; i++) {
			if (i < array.length - 1) {
				str += array[i] + glue;
			} else {
				str += array[i];
			}
		}
		return str;

	}

	public static String implode(List list, String glue) {
		String str = "";
		int i = 0;
		for (Iterator it = list.iterator(); it.hasNext();) {
			Object object = (Object) it.next();
			if (i < list.size() - 1) {
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
				result.append(Integer.toString((messageDigest[i] & 0xff) + 0x100, 16).substring(1));
			}
			return result.toString();
		} catch (NoSuchAlgorithmException ex) {
			MyLog.e(TAG, "SHA-1 is not supported");
		}
		return null;
	}

	public static String getSignature(String email, String password) {
		String sign = Helper.sha1(email + password + ApiUrls.APP_ID + ApiUrls.API_KEY);
		MyLog.d(TAG, "SHA-1 calculated:" + sign);
		return sign;
	}

	public static boolean isValidEmail(CharSequence target) {
		try {
			return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
		} catch (NullPointerException exception) {
			return false;
		}
	}

	public static ArrayList<String> encodeParams(ArrayList<String> params) throws UnsupportedEncodingException {
		ArrayList<String> encoded = new ArrayList<String>();
		for (String string : params) {
			String[] parts = string.split("=");
			if (parts.length == 2) {
				encoded.add(parts[0] + "=" + URLEncoder.encode(parts[1], "UTF-8"));
			} else {
				encoded.add(string);
			}
		}
		return encoded;

	}

}
