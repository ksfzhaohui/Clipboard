package zh.maven.clipboard.oauth;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.EvernoteApi;
import org.scribe.oauth.OAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractAuth {

	static Logger logger = LoggerFactory.getLogger(AbstractAuth.class);

	/** 用户授权之后的回调地址，将verifier存储 **/
	static String CALL_BACK_URL = "http://codingo.xyz:8080/Clipboard-Web/writeVerifier?mac=";

	/** API Key相关信息 **/
	static final String CONSUMER_KEY = "ksfzhaohui";
	static final String CONSUMER_SECRET = "7c6b257f1fc99a85";

	static String mac;
	static OAuthService service;

	static {
		try {
			mac = getLocalMac();
			Class<? extends EvernoteApi> providerClass = EvernoteApi.Yinxiang.class;
			service = new ServiceBuilder().provider(providerClass).apiKey(CONSUMER_KEY).apiSecret(CONSUMER_SECRET)
					.callback(CALL_BACK_URL + mac).build();
		} catch (Exception e) {
			logger.error("get mac error", e);
		}
	}

	/**
	 * 获取本机mac地址
	 * 
	 * @return
	 * @throws SocketException
	 * @throws UnknownHostException
	 */
	private static String getLocalMac() throws SocketException, UnknownHostException {
		byte[] mac = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < mac.length; i++) {
			if (i != 0) {
				sb.append("-");
			}
			int temp = mac[i] & 0xff;
			String str = Integer.toHexString(temp);
			if (str.length() == 1) {
				sb.append("0" + str);
			} else {
				sb.append(str);
			}
		}
		return sb.toString().toUpperCase();
	}
}
