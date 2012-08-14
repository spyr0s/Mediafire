package gr.valor.mediafire.parser;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public abstract class MediafireXmlParser implements  Elements{
	protected static final String ns = null;
	
	
	public String getStringElementValue(InputStream in, String el) throws XmlPullParserException,
	IOException {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			return getValue(parser, el);
		} finally {
			//in.close();
		}
	}

	public Object parse(InputStream in) throws XmlPullParserException,
			IOException {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			return readXml(parser);
		} finally {
			//in.close();
		}
	}
	
	protected abstract Object readXml(XmlPullParser parser) throws XmlPullParserException, IOException ; 
	
	protected String getValue(XmlPullParser parser, String el) throws XmlPullParserException, IOException{
		parser.require(XmlPullParser.START_TAG, ns, RESPONSE);
	    while (parser.next() != XmlPullParser.END_TAG) {
	        if (parser.getEventType() != XmlPullParser.START_TAG) {
	            continue;
	        }
	        String name = parser.getName();
	        // Starts by looking for the  tag
	        if (name.equals(el)) {
	        	parser.require(XmlPullParser.START_TAG, ns, el);
	            String title = readText(parser);
	            parser.require(XmlPullParser.END_TAG, ns, el);
	            return title;
	        } else {
	            skip(parser);
	        }
	    }  
	    return null;
	}
	
	protected String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
	    String result = "";
	    if (parser.next() == XmlPullParser.TEXT) {
	        result = parser.getText();
	        parser.nextTag();
	    }
	    return result;
	}
	
	protected void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
	    if (parser.getEventType() != XmlPullParser.START_TAG) {
	        throw new IllegalStateException();
	    }
	    int depth = 1;
	    while (depth != 0) {
	        switch (parser.next()) {
	        case XmlPullParser.END_TAG:
	            depth--;
	            break;
	        case XmlPullParser.START_TAG:
	            depth++;
	            break;
	        }
	    }
	 }
	
}
