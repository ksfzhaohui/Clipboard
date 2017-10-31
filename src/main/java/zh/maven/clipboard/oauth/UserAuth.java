package zh.maven.clipboard.oauth;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.scribe.model.Token;

import com.evernote.auth.EvernoteService;

/**
 * 用户手动授权认证
 * 
 * @author hui.zhao.cfs
 *
 */
public class UserAuth extends AbstractAuth {

	private static String requestToken;
	private static String requestTokenSecret;

	/**
	 * 第一步：生成一个临时的Token
	 */
	private static void getRequestToken() {
		try {
			Token scribeRequestToken = service.getRequestToken();
			requestToken = scribeRequestToken.getToken();
			requestTokenSecret = scribeRequestToken.getSecret();
			writeToken(requestToken, requestTokenSecret);
			logger.info("requestToken = " + requestToken + ",requestTokenSecret = " + requestTokenSecret);
		} catch (Exception e) {
			logger.error("getRequestToken error", e);
		}
	}

	/**
	 * 第二步：请求用户认证
	 */
	private static void authorization() {
		String authorizationUrl = EvernoteService.YINXIANG.getAuthorizationUrl(requestToken);
		try {
			java.net.URI uri = java.net.URI.create(authorizationUrl);
			java.awt.Desktop dp = java.awt.Desktop.getDesktop();
			if (dp.isSupported(java.awt.Desktop.Action.BROWSE)) {
				dp.browse(uri);
			}
		} catch (Exception e) {
			logger.error("authorization error", e);
		}
	}

	private static void writeToken(String requestToken, String requestTokenSecret) {
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(new FileOutputStream("userAuth"));
			out.writeUTF(requestToken);
			out.writeUTF(requestTokenSecret);
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
		UserAuth.getRequestToken();
		UserAuth.authorization();
	}

}
