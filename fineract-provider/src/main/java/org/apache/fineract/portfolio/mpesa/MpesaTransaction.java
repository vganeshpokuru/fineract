package org.apache.fineract.portfolio.mpesa;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.tomcat.util.codec.binary.Base64;

public class MpesaTransaction {
	public static void main(String args[]) throws NoSuchAlgorithmException, ClientProtocolException, IOException {
		// Timestamp
		TimeZone lagosTimeZone = TimeZone.getTimeZone("Africa/Lagos");
		Calendar calendar = Calendar.getInstance(lagosTimeZone);
		Long timestamp = calendar.getTimeInMillis() / 1000;
		String timeStampString = timestamp.toString();
		// Nonce
		UUID uuid = UUID.randomUUID();
		String nonce = uuid.toString().replaceAll("-", "");
		// Signature Method
		String signatureMethod = "SHA1";
		// Signature
		String httpMethod = "GET"; // HTTP Method of the resource that is being called
		String URL = "https://testids.interswitch.co.ke:19081/api/v1/wallet/wallets/transactions/ministatement/8880170000000017";
		String encodedResourceUrl = URLEncoder.encode(URL); // put the resource URL here
		System.out.println(encodedResourceUrl);
		String clientId = "IKIAA9E210AA86A425B00F931B6F25D21441F7874B67"; // put your client Id here
		String clientSecretKey = "zcMFMaVwzu9FQYT0NlzU1vZNOTmt3al8UyNhqFPiWDI="; // put your client secret here
		String signatureCipher = httpMethod + "&" + encodedResourceUrl + "&" + timestamp + "&" + nonce + "&" + clientId + "&" + clientSecretKey;
		//String signatureCipher = "GET&https%3A%2F%2Ftestids.interswitch.co.ke%3A19081%2Fapi%2Fv1%2Fwallet%2Fwallets%2Fbalance%2F8880170000000017&1539694245&570117c5413e456cbaf1de6f911cd447&IKIAA9E210AA86A425B00F931B6F25D21441F7874B67&zcMFMaVwzu9FQYT0NlzU1vZNOTmt3al8UyNhqFPiWDI=";
		MessageDigest messageDigest = MessageDigest.getInstance(signatureMethod);
		byte[] signatureBytes = messageDigest.digest(signatureCipher.getBytes());
		String signature = new String(Base64.encodeBase64(signatureBytes));
		String SignatureExpected = "GbwJDtLlzEuawvATwTWDUEWWIIY=";
		System.out.println(signature.equals(SignatureExpected));
		System.out.println(signatureCipher);
		System.out.println(signature);
		String autherisation = new String(Base64.encodeBase64(clientId.getBytes()));
		// Setting HTTP Header values
		HttpGet post = new HttpGet(URL);
		post.setHeader("Timestamp", timeStampString);
		post.setHeader("Nonce", nonce);
		post.setHeader("SignatureMethod", signatureMethod);
		post.setHeader("Signature", signature);
		post.setHeader("Content-Type", "application/json");
		post.setHeader("Authorization","InterswitchAuth "+autherisation);
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(post);
		HttpEntity respEntity = response.getEntity();
		if (respEntity != null) {
	        // EntityUtils to get the response content
	        String content =  EntityUtils.toString(respEntity);
	        System.out.println(content);
	    }
	}
}
