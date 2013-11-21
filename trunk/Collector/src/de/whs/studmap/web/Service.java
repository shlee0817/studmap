package de.whs.studmap.web;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

import de.whs.studmap.data.Constants;
import de.whs.studmap.data.Fingerprint;

public class Service implements Constants {

	public static boolean SaveFingerprintForNode(int nodeId,
			Fingerprint fingerprint) throws WebServiceException,
			ConnectException {

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(REQUEST_PARAM_NODEID, String
				.valueOf(nodeId)));

		httpPost(URL_FINGERPRINT, METHOD_SAVEFINGERPRINTFORNODE, params,
				new Gson().toJson(fingerprint));

		return true;
	}

	public static boolean GetNodeForFingerprint(Fingerprint fingerprint,
			double factor) throws WebServiceException, ConnectException {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(REQUEST_PARAM_FACTOR, String
				.valueOf(factor)));

		httpPost(URL_FINGERPRINT, METHOD_GETNODEFORFINGERPRINT, params,
				new Gson().toJson(fingerprint));

		return true;
	}

	private static JSONObject httpPost(String url, String methodName,
			List<NameValuePair> params, String jsonData)
			throws WebServiceException, ConnectException {

		// Create a new HttpClient and Post Header
		HttpClient httpClient = new DefaultHttpClient();

		try {
			// Add data
			String entityString = URLEncodedUtils.format(params, "utf-8");
			HttpPost httpPost = new HttpPost(url + methodName + "?"
					+ entityString);
			
			StringEntity se = new StringEntity(jsonData.toString());  
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			httpPost.setEntity(se);

			// Execute HTTP Post Request
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				String responseString = EntityUtils.toString(entity);
				JSONObject jObject = new JSONObject(responseString);

				int status = jObject.getInt(RESPONSE_STATUS);
				if (status == ResponseStatus.Ok)
					return jObject;
				else
					throw new WebServiceException(jObject);
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new ConnectException();
	}

	private static JSONObject httpGet(String url, String methodName)
			throws WebServiceException, ConnectException {
		return httpGet(url, methodName, null);
	}

	private static JSONObject httpGet(String url, String methodName,
			List<NameValuePair> params) throws WebServiceException,
			ConnectException {

		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url + methodName);

		try {

			if (params != null) {
				String paramString = URLEncodedUtils.format(params, "utf-8");
				httpGet = new HttpGet(url + methodName + "?" + paramString);
			}

			HttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				String responseString = EntityUtils.toString(entity);
				JSONObject jObject = new JSONObject(responseString);

				int status = jObject.getInt(RESPONSE_STATUS);
				if (status == ResponseStatus.Ok)
					return jObject;
				else
					throw new WebServiceException(jObject);
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		throw new ConnectException();
	}
}
