/* ===============================================================================
 *
 * Part of the InfoglueIDE Project 
 *
 * ===============================================================================
 *
 * Copyright (C) Stefan Sik 2007
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *
 * ===============================================================================
 */
package org.infoglue.igide.helper;

/**
 * @author Stefan Sik 
 */
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;

public class HTTPClientHelper {
	HttpClient client;

	
	public HTTPClientHelper(URL baseurl, String username, String password) 
	{
		HttpClientParams params = new HttpClientParams(HttpClientParams.getDefaultParams());
		
		client = new HttpClient(params);
		
		Credentials defaultcreds = new UsernamePasswordCredentials(username, password);
		client.getState().setCredentials(new AuthScope(baseurl.getHost(), baseurl.getPort(), AuthScope.ANY_REALM), defaultcreds);		
		
		List<String> authPrefs = new ArrayList<String>(2);
		authPrefs.add(AuthPolicy.BASIC);
		authPrefs.add(AuthPolicy.DIGEST);
		authPrefs.add(AuthPolicy.NTLM);
		client.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);
	}
	
	public String getContent(URL url) {
		GetMethod method = new GetMethod(url.toExternalForm());
		method.setDoAuthentication(true);
		method.setFollowRedirects(true);
		
		String response = null;
		
		try {
			// Execute the method.
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {
				System.err.println("Method failed: " + method.getStatusLine() + "code;" + method.getStatusCode());
				if(method.getStatusCode()==302)
				{
					// Infoglue special!!
					
				}
				
			}

			// Read the response body.
			byte[] responseBody = method.getResponseBody();

			// Deal with the response.
			// Use caution: ensure correct character encoding and is not binary data
			response = new String(responseBody);

		} catch (HttpException e) {
			System.err.println("Fatal protocol violation: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Fatal transport error: " + e.getMessage());
			e.printStackTrace();
		} finally {
			// Release the connection.
			method.releaseConnection();
		}
		return response;
	}
	
	public static String getUrlContent(URL url)
	{
		return null;
	}

	public static void main(String... s) throws MalformedURLException
	{
		URL base = new URL("http://localhost:8080/infoglueCMS/");
		HTTPClientHelper h = new HTTPClientHelper(base,"ss","idioter98" );
		System.out.println(
		h.getContent(new URL(base, "SimpleContentXml!ContentTypeDefinitions.action"))
		);
	}
	
	
}
