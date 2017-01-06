package com.wm.remusic.proxy.utils;

import java.net.HttpURLConnection;
import java.net.URLConnection;

public class HttpUtils {

    private static final String LOG_TAG = HttpUtils.class.getSimpleName();

    /**
     * 生成返回MediaPlayer的Response Header
     *
     * @param rangeStart
     * @param rangeEnd
     * @param fileLength
     * @return
     */
    public static String genResponseHeader(int rangeStart, int rangeEnd, int fileLength) {
        StringBuffer sb = new StringBuffer();
        sb.append("HTTP/1.1 206 Partial Content").append("\n");
        sb.append("Content-Type: audio/mpeg").append("\n");
        sb.append("Content-Length: ").append(rangeEnd - rangeStart + 1).append("\n");
        sb.append("Connection: keep-alive").append("\n");
        sb.append("Accept-Ranges: bytes").append("\n");
        String contentRangeValue = String.format(Constants.CONTENT_RANGE_PARAMS + "%d-%d/%d", rangeStart, rangeEnd,
                fileLength);
        sb.append("Content-Range: ").append(contentRangeValue).append("\n");
        sb.append("\n");
        return sb.toString();
    }


    /**
     * 发送请求,得到Response
     *
     * @param url
     * @return
     */
    public static HttpURLConnection send(URLConnection request) {
        /*
         * 添加需要的Header
		 */
//		request.setRequestProperty(Constants.USER_AGENT, "TrafficRadio_BedPotato_Exclusive_UA");
        // TODO Others Header
        /*
         * 发送请求
		 */
        HttpURLConnection httpURLConnection = (HttpURLConnection) request;
        httpURLConnection.setConnectTimeout(20000);
        httpURLConnection.setReadTimeout(60000);
//		request.set
//		DefaultHttpClient seed = new DefaultHttpClient();
//		SchemeRegistry registry = new SchemeRegistry();
//		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
//		SingleClientConnManager mgr = new MyClientConnManager(seed.getParams(), registry);
//		DefaultHttpClient http = new DefaultHttpClient(mgr, seed.getParams());
//		HttpResponse response = null;
//		try {
//			Log.d(LOG_TAG, "sending request");
//		//	response = http.execute(request);
//			Log.d(LOG_TAG, "response receive");
        //	} catch (ClientProtocolException e) {
        //		Log.e(LOG_TAG, "Error downloading", e);
        //	} catch (IOException e) {
        //		Log.e(LOG_TAG, "Error downloading", e);
        //	}
        //	StatusLine line = response.getStatusLine();
        //	if (line.getStatusCode() != 200 && line.getStatusCode() != 206) {
        //		Log.i(LOG_TAG, "ERROR Response Status:" + line.toString());
        //		return null;
        //	}else {
        //		return response;
        //	}
        return httpURLConnection;
    }

//	private static class IcyLineParser extends BasicLineParser {
//		private static final String ICY_PROTOCOL_NAME = "ICY";
//
//		private IcyLineParser() {
//			super();
//		}
//
//		@Override
//		public boolean hasProtocolVersion(CharArrayBuffer buffer, ParserCursor cursor) {
//			boolean superFound = super.hasProtocolVersion(buffer, cursor);
//			if (superFound) {
//				return true;
//			}
//			int index = cursor.getPos();
//
//			final int protolength = ICY_PROTOCOL_NAME.length();
//
//			if (buffer.length() < protolength)
//				return false; // not long enough for "HTTP/1.1"
//
//			if (index < 0) {
//				// end of line, no tolerance for trailing whitespace
//				// this works only for single-digit major and minor version
//				index = buffer.length() - protolength;
//			} else if (index == 0) {
//				// beginning of line, tolerate leading whitespace
//				while ((index < buffer.length()) && HTTP.isWhitespace(buffer.charAt(index))) {
//					index++;
//				}
//			} // else within line, don't tolerate whitespace
//
//			return index + protolength <= buffer.length()
//					&& buffer.substring(index, index + protolength).equals(ICY_PROTOCOL_NAME);
//
//		}
//
//		@Override
//		public ProtocolVersion parseProtocolVersion(CharArrayBuffer buffer, ParserCursor cursor) throws ParseException {
//
//			if (buffer == null) {
//				throw new IllegalArgumentException("Char array buffer may not be null");
//			}
//			if (cursor == null) {
//				throw new IllegalArgumentException("Parser cursor may not be null");
//			}
//
//			final int protolength = ICY_PROTOCOL_NAME.length();
//
//			int indexFrom = cursor.getPos();
//			int indexTo = cursor.getUpperBound();
//
//			skipWhitespace(buffer, cursor);
//
//			int i = cursor.getPos();
//
//			// long enough for "HTTP/1.1"?
//			if (i + protolength + 4 > indexTo) {
//				throw new ParseException("Not a valid protocol version: " + buffer.substring(indexFrom, indexTo));
//			}
//
//			// check the protocol name and slash
//			if (!buffer.substring(i, i + protolength).equals(ICY_PROTOCOL_NAME)) {
//				return super.parseProtocolVersion(buffer, cursor);
//			}
//
//			cursor.updatePos(i + protolength);
//
//			return createProtocolVersion(1, 0);
//		}
//
//		@Override
//		public StatusLine parseStatusLine(CharArrayBuffer buffer, ParserCursor cursor) throws ParseException {
//			return super.parseStatusLine(buffer, cursor);
//		}
//	}
//
//	private static class MyClientConnection extends DefaultClientConnection {
//		@Override
//		protected HttpMessageParser createResponseParser(final SessionInputBuffer buffer,
//				final HttpResponseFactory responseFactory, final HttpParams params) {
//			return new DefaultResponseParser(buffer, new IcyLineParser(), responseFactory, params);
//		}
//	}
//
//	private static class MyClientConnectionOperator extends DefaultClientConnectionOperator {
//		public MyClientConnectionOperator(final SchemeRegistry sr) {
//			super(sr);
//		}
//
//		@Override
//		public OperatedClientConnection createConnection() {
//			return new MyClientConnection();
//		}
//	}
//
//	private static class MyClientConnManager extends SingleClientConnManager {
//		private MyClientConnManager(HttpParams params, SchemeRegistry schreg) {
//			super(params, schreg);
//		}
//
//		@Override
//		protected ClientConnectionOperator createConnectionOperator(final SchemeRegistry sr) {
//			return new MyClientConnectionOperator(sr);
//		}
//	}
}
