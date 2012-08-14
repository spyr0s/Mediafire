package gr.valor.mediafire.parser;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class SessionTokenParser extends MediafireXmlParser {

	public static String token;
	private InputStream in;

	public SessionTokenParser(InputStream in) {
		this.in = in;
	}

	public String getToken() {
		try {
			return getStringElementValue(in, SESSION_TOKEN).trim();
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
	protected Object readXml(XmlPullParser parser) throws XmlPullParserException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
