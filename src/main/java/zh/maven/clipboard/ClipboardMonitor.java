package zh.maven.clipboard;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 剪贴板监听器
 * 
 * @author hui.zhao.cfs
 *
 */
public class ClipboardMonitor implements ClipboardOwner {

	private static Logger logger = LoggerFactory.getLogger(ClipboardMonitor.class);

	private static final String AUTH_TOKEN = "S=s1:U=941fa:E=166a8ec927b:C=15f513b6580:P=81:A=ksfzhaohui:V=2:H=208abbcc7d6cf656bbb3c7ea574dd140";

	public ClipboardMonitor() throws Exception {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(clipboard.getContents(null), this);
	}

	/**
	 * 剪贴板数据变动调用
	 */
	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		try {
			EvernoteApi evernoteApi = new EvernoteApi(AUTH_TOKEN);
			// 延迟一段时间，防止剪贴板正在使用
			Thread.sleep(200);
			if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
				String text = (String) clipboard.getData(DataFlavor.stringFlavor);
				evernoteApi.createNoteText(text);
				clipboard.setContents(new StringSelection(text), this);
			} else if (clipboard.isDataFlavorAvailable(DataFlavor.imageFlavor)) {
				final BufferedImage image = (BufferedImage) clipboard.getData(DataFlavor.imageFlavor);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ImageIO.write(image, "png", out);
				Transferable trans = new Transferable() {
					public DataFlavor[] getTransferDataFlavors() {
						return new DataFlavor[] { DataFlavor.imageFlavor };
					}

					public boolean isDataFlavorSupported(DataFlavor flavor) {
						return DataFlavor.imageFlavor.equals(flavor);
					}

					public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
						if (isDataFlavorSupported(flavor))
							return image;
						throw new UnsupportedFlavorException(flavor);
					}
				};
				evernoteApi.createNoteImage("IMAGE:" + new Date(), out.toByteArray());
				clipboard.setContents(trans, this);
			} else if (clipboard.isDataFlavorAvailable(DataFlavor.javaFileListFlavor)) {
				@SuppressWarnings("unchecked")
				List<File> array = (List<File>) clipboard.getData(DataFlavor.javaFileListFlavor);
				for (File file : array) {
					evernoteApi.createNoteText(file.getPath());
				}
				clipboard.setContents(contents, this);
			} else {
				logger.info("未知的类型");
			}
		} catch (Exception e) {
			logger.error("lostOwnership error", e);
		}
	}
}
