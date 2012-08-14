package gr.valor.mediafire.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class LoginTokenParser extends MediafireXmlParser{
	
	public static String token ;
	private InputStream in;
	
	public LoginTokenParser(InputStream in) {
		this.in = in;
	}

	public String getToken(){
		try {
			return getStringElementValue(in, LOGIN_TOKEN).trim();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected List readXml(XmlPullParser parser) throws XmlPullParserException, IOException  {
		// TODO Auto-generated method stub
		return null;
	}

}
