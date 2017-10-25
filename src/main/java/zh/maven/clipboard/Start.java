package zh.maven.clipboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Start {

	private static Logger logger = LoggerFactory.getLogger(Start.class);

	public static void main(String[] args) {
		try {
			new ClipboardMonitor();
			logger.info("Listener Clipboard start success!");
			System.in.read();
		} catch (Exception e) {
			logger.error("Listener Clipboard start error!", e);
			System.exit(1);
		}
	}

}
