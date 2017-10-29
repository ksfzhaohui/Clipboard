package zh.maven.clipboard;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.evernote.auth.EvernoteAuth;
import com.evernote.auth.EvernoteService;
import com.evernote.clients.ClientFactory;
import com.evernote.clients.NoteStoreClient;
import com.evernote.clients.UserStoreClient;
import com.evernote.edam.type.Data;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Resource;
import com.evernote.edam.type.ResourceAttributes;

/**
 * 印象笔记调用api
 * 
 * @author hui.zhao.cfs
 *
 */
public class EvernoteAction {

	private static Logger logger = LoggerFactory.getLogger(EvernoteAction.class);

	private UserStoreClient userStore;
	private NoteStoreClient noteStore;
	private String newNoteGuid;

	public EvernoteAction(String token) throws Exception {
		EvernoteAuth evernoteAuth = new EvernoteAuth(EvernoteService.YINXIANG, token);
		ClientFactory factory = new ClientFactory(evernoteAuth);
		userStore = factory.createUserStoreClient();

		boolean versionOk = userStore.checkVersion("Evernote EDAMDemo (Java)",
				com.evernote.edam.userstore.Constants.EDAM_VERSION_MAJOR,
				com.evernote.edam.userstore.Constants.EDAM_VERSION_MINOR);
		if (!versionOk) {
			logger.error("Incompatible Evernote client protocol version");
			System.exit(1);
		}

		noteStore = factory.createNoteStoreClient();
	}

	public void createNoteText(String body) throws Exception {
		Note note = new Note();
		Notebook notebook = getCurrentNotebook();
		note.setNotebookGuid(notebook.getGuid());
		note.setTitle(getDateStr());
		body = body.replaceAll("&", "&amp;");
		body = body.replaceAll("<", "&lt;");
		body = body.replaceAll(">", "&gt;");
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">" + "<en-note>" + body
				+ "</en-note>";
		note.setContent(content);
		Note createdNote = noteStore.createNote(note);
		newNoteGuid = createdNote.getGuid();

		logger.info("Successfully created a new note with GUID: " + newNoteGuid);
	}

	public void createNoteImage(String fileName, byte[] body) throws Exception {
		Note note = new Note();
		note.setNotebookGuid(getCurrentNotebook().getGuid());
		note.setTitle(getDateStr());

		String mimeType = "image/png";
		Resource resource = new Resource();
		resource.setData(readFileAsData(body));
		resource.setMime(mimeType);
		ResourceAttributes attributes = new ResourceAttributes();
		attributes.setFileName(fileName);
		resource.setAttributes(attributes);

		note.addToResources(resource);

		String hashHex = bytesToHex(resource.getData().getBodyHash());

		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">" + "<en-note>"
				+ "<en-media type=\"image/png\" hash=\"" + hashHex + "\"/>" + "</en-note>";
		note.setContent(content);

		Note createdNote = noteStore.createNote(note);
		newNoteGuid = createdNote.getGuid();

		logger.info("Successfully created a new note with GUID: " + newNoteGuid);
	}

	/**
	 * 获取notebook的guid
	 * 
	 * @return
	 * @throws Exception
	 */
	private Notebook getCurrentNotebook() throws Exception {
		List<Notebook> bookList = getAllNotebooks();
		String hostName = "[剪贴板]"+getHostName();
		for (Notebook notebook : bookList) {
			if (hostName.equals(notebook.getName())) {
				return notebook;
			}
		}
		Notebook notebook = new Notebook();
		notebook.setName(hostName);
		return noteStore.createNotebook(notebook);
	}

	/**
	 * 获取当前主机hostname
	 * 
	 * @return
	 * @throws UnknownHostException
	 */
	private String getHostName() throws UnknownHostException {
		InetAddress inetAddress = InetAddress.getLocalHost();
		return inetAddress.getHostName();
	}

	/**
	 * 获取所有的笔记本
	 * 
	 * @return
	 * @throws Exception
	 */
	private List<Notebook> getAllNotebooks() throws Exception {
		return noteStore.listNotebooks();
	}

	/**
	 * 获取当前日志字符串
	 * 
	 * @return
	 */
	private String getDateStr() {
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		return df.format(new Date());
	}

	private static Data readFileAsData(byte[] body) throws Exception {
		Data data = new Data();
		data.setSize(body.length);
		data.setBodyHash(MessageDigest.getInstance("MD5").digest(body));
		data.setBody(body);
		return data;
	}

	private static String bytesToHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte hashByte : bytes) {
			int intVal = 0xff & hashByte;
			if (intVal < 0x10) {
				sb.append('0');
			}
			sb.append(Integer.toHexString(intVal));
		}
		return sb.toString();
	}
}
