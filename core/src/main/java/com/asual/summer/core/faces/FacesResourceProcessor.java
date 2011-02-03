/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.asual.summer.core.faces;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.asual.summer.core.util.StringUtils;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class FacesResourceProcessor {

	public static byte[] execute(URL url, InputStream input) throws IOException {
    	
		byte[] bytes;
		
        try {
        	StringBuilder sb = new StringBuilder();
            UnicodeReader reader = new UnicodeReader(input, StringUtils.getEncoding());
            try {
                char[] cbuf = new char[32];
                int r;
                while ((r = reader.read(cbuf, 0, 32)) != -1) {
                    sb.append(cbuf, 0, r);
                }
                String str = sb.toString();
                if (!str.contains("ui:component")) {
	                if (url.getFile().contains("META-INF/templates")) {
	                	str = "<ui:component xmlns:ui=\"http://java.sun.com/jsf/facelets\">" + 
	                		Pattern.compile("(<\\!DOCTYPE html>)|(</?html[^>]*>)|(<title>[^<]*</title>)", 
	                			Pattern.CASE_INSENSITIVE).matcher(str).replaceAll("").replaceAll(
	                					"\\$\\{template\\.body\\}", "<ui:insert />") + "</ui:component>";
	                }
                }
            	Matcher m = Pattern.compile("&(\\w*);").matcher(str);
        		StringBuffer b = new StringBuffer();
        		while (m.find()) {
        			m.appendReplacement(b, "&#" + entities.get(m.group(1)) + ";");
        		}
        		m.appendTail(b);
                bytes = b.toString().getBytes(reader.getEncoding());
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            throw e;
        }
        
        return bytes;
	}
    
	/**
	 * Generic unicode textreader, which will use BOM mark
	 * to identify the encoding to be used. If BOM is not found
	 * then use a given default or system encoding.
	 * 
	 * @author Thomas Weidenfeller
	 * @author Aki Nieminen
	 */
	private static class UnicodeReader extends Reader {
	    
	    PushbackInputStream internalIn;
	    InputStreamReader internalIn2 = null;
	    String defaultEnc;

	    private static final int BOM_SIZE = 4;

	    /**
	     * @param in inputstream to be read
	     * @param defaultEnc default encoding if stream does not have 
	     *                   BOM marker. Give NULL to use system-level default.
	     */
	    public UnicodeReader(InputStream in, String defaultEnc) {
	        internalIn = new PushbackInputStream(in, BOM_SIZE);
	        this.defaultEnc = defaultEnc;
	    }
	    
	    /**
	     * Get stream encoding or NULL if stream is uninitialized.
	     * Call init() or read() method to initialize it.
	     */
	    public String getEncoding() {
	        if (internalIn2 == null) return null;
	        return internalIn2.getEncoding();
	    }

	    /**
	     * Read-ahead four bytes and check for BOM marks. Extra bytes are
	     * unread back to the stream, only BOM bytes are skipped.
	     */
	    protected void init() throws IOException {
	        
	        if (internalIn2 != null) return;

	        String encoding;
	        byte bom[] = new byte[BOM_SIZE];
	        int n, unread;
	        n = internalIn.read(bom, 0, bom.length);

	        if ( (bom[0] == (byte)0x00) && (bom[1] == (byte)0x00) &&
	                (bom[2] == (byte)0xFE) && (bom[3] == (byte)0xFF) ) {
	            encoding = "UTF-32BE";
	            unread = n - 4;
	        } else if ( (bom[0] == (byte)0xFF) && (bom[1] == (byte)0xFE) &&
	                (bom[2] == (byte)0x00) && (bom[3] == (byte)0x00) ) {
	            encoding = "UTF-32LE";
	            unread = n - 4;
	        } else if (  (bom[0] == (byte)0xEF) && (bom[1] == (byte)0xBB) &&
	                (bom[2] == (byte)0xBF) ) {
	            encoding = "UTF-8";
	            unread = n - 3;
	        } else if ( (bom[0] == (byte)0xFE) && (bom[1] == (byte)0xFF) ) {
	            encoding = "UTF-16BE";
	            unread = n - 2;
	        } else if ( (bom[0] == (byte)0xFF) && (bom[1] == (byte)0xFE) ) {
	            encoding = "UTF-16LE";
	            unread = n - 2;
	        } else {
	            // Unicode BOM mark not found, unread all bytes
	            encoding = defaultEnc;
	            unread = n;
	        }

	        if (unread > 0) internalIn.unread(bom, (n - unread), unread);

	        // Use given encoding
	        if (encoding == null) {
	            internalIn2 = new InputStreamReader(internalIn);
	        } else {
	            internalIn2 = new InputStreamReader(internalIn, encoding);
	        }
	    }

	    public void close() throws IOException {
	        init();
	        internalIn2.close();
	    }

	    public int read(char[] cbuf, int off, int len) throws IOException {
	        init();
	        return internalIn2.read(cbuf, off, len);
	    }

	}
	
	private static Map<String, String> entities = new HashMap<String, String>();
	
	static {
		entities.put("quot", "34");
		entities.put("amp", "38");
		entities.put("apos", "39");
		entities.put("lt", "60");
		entities.put("gt", "62");
		entities.put("nbsp", "160");
		entities.put("copy", "169");
		entities.put("iexcl", "161");
		entities.put("cent", "162");
		entities.put("pound", "163");
		entities.put("curren", "164");
		entities.put("yen", "165");
		entities.put("brvbar", "166");
		entities.put("sect", "167");
		entities.put("uml", "168");
		entities.put("copy", "169");
		entities.put("ordf", "170");
		entities.put("laquo", "171");
		entities.put("not", "172");
		entities.put("shy", "173");
		entities.put("reg", "174");
		entities.put("macr", "175");
		entities.put("deg", "176");
		entities.put("plusmn", "177");
		entities.put("sup2", "178");
		entities.put("sup3", "179");
		entities.put("acute", "180");
		entities.put("micro", "181");
		entities.put("para", "182");
		entities.put("middot", "183");
		entities.put("cedil", "184");
		entities.put("sup1", "185");
		entities.put("ordm", "186");
		entities.put("raquo", "187");
		entities.put("frac14", "188");
		entities.put("frac12", "189");
		entities.put("frac34", "190");
		entities.put("iquest", "191");
		entities.put("Agrave", "192");
		entities.put("Aacute", "193");
		entities.put("Acirc", "194");
		entities.put("Atilde", "195");
		entities.put("Auml", "196");
		entities.put("Aring", "197");
		entities.put("AElig", "198");
		entities.put("Ccedil", "199");
		entities.put("Egrave", "200");
		entities.put("Eacute", "201");
		entities.put("Ecirc", "202");
		entities.put("Euml", "203");
		entities.put("Igrave", "204");
		entities.put("Iacute", "205");
		entities.put("Icirc", "206");
		entities.put("Iuml", "207");
		entities.put("ETH", "208");
		entities.put("Ntilde", "209");
		entities.put("Ograve", "210");
		entities.put("Oacute", "211");
		entities.put("Ocirc", "212");
		entities.put("Otilde", "213");
		entities.put("Ouml", "214");
		entities.put("times", "215");
		entities.put("Oslash", "216");
		entities.put("Ugrave", "217");
		entities.put("Uacute", "218");
		entities.put("Ucirc", "219");
		entities.put("Uuml", "220");
		entities.put("Yacute", "221");
		entities.put("THORN", "222");
		entities.put("szlig", "223");
		entities.put("agrave", "224");
		entities.put("aacute", "225");
		entities.put("acirc", "226");
		entities.put("atilde", "227");
		entities.put("auml", "228");
		entities.put("aring", "229");
		entities.put("aelig", "230");
		entities.put("ccedil", "231");
		entities.put("egrave", "232");
		entities.put("eacute", "233");
		entities.put("ecirc", "234");
		entities.put("euml", "235");
		entities.put("igrave", "236");
		entities.put("iacute", "237");
		entities.put("icirc", "238");
		entities.put("iuml", "239");
		entities.put("eth", "240");
		entities.put("ntilde", "241");
		entities.put("ograve", "242");
		entities.put("oacute", "243");
		entities.put("ocirc", "244");
		entities.put("otilde", "245");
		entities.put("ouml", "246");
		entities.put("divide", "247");
		entities.put("oslash", "248");
		entities.put("ugrave", "249");
		entities.put("uacute", "250");
		entities.put("ucirc", "251");
		entities.put("uuml", "252");
		entities.put("yacute", "253");
		entities.put("thorn", "254");
		entities.put("yuml", "255");
		entities.put("OElig", "338");
		entities.put("oelig", "339");
		entities.put("Scaron", "352");
		entities.put("scaron", "353");
		entities.put("Yuml", "376");
		entities.put("fnof", "402");
		entities.put("circ", "710");
		entities.put("tilde", "732");
		entities.put("Alpha", "913");
		entities.put("Beta", "914");
		entities.put("Gamma", "915");
		entities.put("Delta", "916");
		entities.put("Epsilon", "917");
		entities.put("Zeta", "918");
		entities.put("Eta", "919");
		entities.put("Theta", "920");
		entities.put("Iota", "921");
		entities.put("Kappa", "922");
		entities.put("Lambda", "923");
		entities.put("Mu", "924");
		entities.put("Nu", "925");
		entities.put("Xi", "926");
		entities.put("Omicron", "927");
		entities.put("Pi", "928");
		entities.put("Rho", "929");
		entities.put("Sigma", "931");
		entities.put("Tau", "932");
		entities.put("Upsilon", "933");
		entities.put("Phi", "934");
		entities.put("Chi", "935");
		entities.put("Psi", "936");
		entities.put("Omega", "937");
		entities.put("alpha", "945");
		entities.put("beta", "946");
		entities.put("gamma", "947");
		entities.put("delta", "948");
		entities.put("epsilon", "949");
		entities.put("zeta", "950");
		entities.put("eta", "951");
		entities.put("theta", "952");
		entities.put("iota", "953");
		entities.put("kappa", "954");
		entities.put("lambda", "955");
		entities.put("mu", "956");
		entities.put("nu", "957");
		entities.put("xi", "958");
		entities.put("omicron", "959");
		entities.put("pi", "960");
		entities.put("rho", "961");
		entities.put("sigmaf", "962");
		entities.put("sigma", "963");
		entities.put("tau", "964");
		entities.put("upsilon", "965");
		entities.put("phi", "966");
		entities.put("chi", "967");
		entities.put("psi", "968");
		entities.put("omega", "969");
		entities.put("thetasym", "977");
		entities.put("upsih", "978");
		entities.put("piv", "982");
		entities.put("ensp", "8194");
		entities.put("emsp", "8195");
		entities.put("thinsp", "8201");
		entities.put("zwnj", "8204");
		entities.put("zwj", "8205");
		entities.put("lrm", "8206");
		entities.put("rlm", "8207");
		entities.put("ndash", "8211");
		entities.put("mdash", "8212");
		entities.put("lsquo", "8216");
		entities.put("rsquo", "8217");
		entities.put("sbquo", "8218");
		entities.put("ldquo", "8220");
		entities.put("rdquo", "8221");
		entities.put("bdquo", "8222");
		entities.put("dagger", "8224");
		entities.put("Dagger", "8225");
		entities.put("bull", "8226");
		entities.put("hellip", "8230");
		entities.put("permil", "8240");
		entities.put("prime", "8242");
		entities.put("Prime", "8243");
		entities.put("lsaquo", "8249");
		entities.put("rsaquo", "8250");
		entities.put("oline", "8254");
		entities.put("frasl", "8260");
		entities.put("euro", "8364");
		entities.put("weierp", "8472");
		entities.put("image", "8465");
		entities.put("real", "8476");
		entities.put("trade", "8482");
		entities.put("alefsym", "8501");
		entities.put("larr", "8592");
		entities.put("uarr", "8593");
		entities.put("rarr", "8594");
		entities.put("darr", "8595");
		entities.put("harr", "8596");
		entities.put("crarr", "8629");
		entities.put("lArr", "8656");
		entities.put("uArr", "8657");
		entities.put("rArr", "8658");
		entities.put("dArr", "8659");
		entities.put("hArr", "8660");
		entities.put("forall", "8704");
		entities.put("part", "8706");
		entities.put("exist", "8707");
		entities.put("empty", "8709");
		entities.put("nabla", "8711");
		entities.put("isin", "8712");
		entities.put("notin", "8713");
		entities.put("ni", "8715");
		entities.put("prod", "8719");
		entities.put("sum", "8721");
		entities.put("minus", "8722");
		entities.put("lowast", "8727");
		entities.put("radic", "8730");
		entities.put("prop", "8733");
		entities.put("infin", "8734");
		entities.put("ang", "8736");
		entities.put("and", "8743");
		entities.put("or", "8744");
		entities.put("cap", "8745");
		entities.put("cup", "8746");
		entities.put("int", "8747");
		entities.put("there4", "8756");
		entities.put("sim", "8764");
		entities.put("cong", "8773");
		entities.put("asymp", "8776");
		entities.put("ne", "8800");
		entities.put("equiv", "8801");
		entities.put("le", "8804");
		entities.put("ge", "8805");
		entities.put("sub", "8834");
		entities.put("sup", "8835");
		entities.put("sube", "8838");
		entities.put("supe", "8839");
		entities.put("oplus", "8853");
		entities.put("otimes", "8855");
		entities.put("perp", "8869");
		entities.put("sdot", "8901");
		entities.put("lceil", "8968");
		entities.put("rceil", "8969");
		entities.put("lfloor", "8970");
		entities.put("rfloor", "8971");
		entities.put("lang", "9001");
		entities.put("rang", "9002");
		entities.put("loz", "9674");
		entities.put("spades", "9824");
		entities.put("clubs", "9827");
		entities.put("hearts", "9829");
		entities.put("diams", "9830");
	}
	
}