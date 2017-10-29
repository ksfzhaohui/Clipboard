package zh.maven.clipboard.oauth;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.scribe.model.Token;
import org.scribe.model.Verifier;

import com.evernote.auth.EvernoteAuth;
import com.evernote.auth.EvernoteService;

public class GetAccessToken extends AbstractAuth {

	private static String GET_VERIFIER_URL = "http://codingo.xyz:8080/Clipboard-Web/getVerifier?mac=";

	/**
	 * 第三步：取回 Access Token
	 */
	private static void getAccessToken() {
		try {
			CloseableHttpClient httpClient = HttpClients.custom().build();
			HttpGet get = new HttpGet();
			get.setURI(new URI(GET_VERIFIER_URL + mac));
			CloseableHttpResponse response = httpClient.execute(get);
			String verifier = EntityUtils.toString(response.getEntity());

			Map<String, String> map = readToken();
			Token scribeRequestToken = new Token(map.get("requestToken"), map.get("requestTokenSecret"));
			Verifier scribeVerifier = new Verifier(verifier);
			Token scribeAccessToken = service.getAccessToken(scribeRequestToken, scribeVerifier);
			EvernoteAuth evernoteAuth = EvernoteAuth.parseOAuthResponse(EvernoteService.YINXIANG,
					scribeAccessToken.getRawResponse());
			System.out.println(evernoteAuth.getToken());
			writeAccessToken(evernoteAuth.getToken());
		} catch (Exception e) {
			logger.error("getAccessToken error", e);
		}
	}

	private static Map<String, String> readToken() {
		DataInputStream input = null;
		Map<String, String> map = new HashMap<String, String>();
		try {
			input = new DataInputStream(new FileInputStream("userAuth"));
			map.put("requestToken", input.readUTF());
			map.put("requestTokenSecret", input.readUTF());
		} catch (IOException e) {
			logger.error("read error", e);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		}
		return map;
	}

	private static void writeAccessToken(String accessToken) {
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(new FileOutputStream("accessToken"));
			out.writeUTF(accessToken);
		} catch (IOException e) {
			logger.error("write error", e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public static void main(String[] args) {
		GetAccessToken.getAccessToken();
	}

}
