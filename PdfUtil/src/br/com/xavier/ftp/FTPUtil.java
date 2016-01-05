package br.com.xavier.ftp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

public class FTPUtil {
	
	//XXX CONNECTION PROPERTIES
	private String host;
	private int hostPort;
	private String user;
	private String password; //TODO SEE BETTER STORAGE MODE
		
	//XXX CONTROL PROPERTIES
	private boolean passiveMode;
	
	private boolean isConnected;
	private int timeOutInSeconds;
	
	//XXX APACHE FTP CLIENT
	private FTPClient ftpClient;
	
	/**
	 * Simple abstraction for the way more complete Apache FTPClient class to handle PDF files stored in FTP servers.
	 * 
	 * @param host is the host of FTP server
	 * @param hostPort is the port of the host we will connect
	 * @param ftpUser is the username for athentication
	 * @param ftpUserPassword is the password of that user
	 * @param timeOutInSeconds is the amount of time to wait before throw an exception or if connected, send a NOOP command to keep the connection alive
	 * @param passiveMode set if the connection will be on passive mode instead of active
	 */
	public FTPUtil(String host, int hostPort, String ftpUser, String ftpUserPassword, int timeOutInSeconds, boolean passiveMode) {
		this.host = host;
		this.hostPort = hostPort;
		
		this.user = ftpUser;
		this.password = ftpUserPassword;
		
		this.passiveMode = passiveMode;
		this.isConnected = false;
		this.timeOutInSeconds = timeOutInSeconds;
		
		this.ftpClient = new FTPClient();
	}
	
	private void connect() throws SocketException, IOException {
		//send the command..
		ftpClient.connect(host, hostPort);
		
		//check reply code...
		int replyCode = ftpClient.getReplyCode();
		boolean sucess = FTPReply.isPositiveCompletion(replyCode);
		
		//sucess?
		if(!sucess){
			ftpClient.disconnect();
			throw new IOException("FTP server refused connection.");
		}
		
		setConnected(sucess);
	}
	
	private void login() throws IOException {
		if(!isConnected()){
			connect();
		}
		
		ftpClient.login(user, password);
		
		if(passiveMode){
			ftpClient.enterLocalPassiveMode();
		}
		
		//PDF transmitted with ASCII file type break the file 
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		ftpClient.setBufferSize(0);
		ftpClient.setControlKeepAliveTimeout(timeOutInSeconds);
	}
	
	private void logout() throws IOException {
		if(isConnected()){
			ftpClient.logout();
			ftpClient.disconnect();
			setConnected(false);
		}
	}
	
	/**
	 * 
	 * Method that downloads a file from the FTP server.
	 * 
	 * @param filepath is the absolute path of the file in the FTP server
	 * @return the file content as byte[]
	 * @throws IOException if an i/o error occurs.
	 */
	public byte[] downloadFile(String filepath) throws IOException {
		login();
		
		FTPFile ftpFile = ftpClient.listFiles(filepath)[0];
		
		//file exists on FTP server?
		if(ftpFile == null){
			throw new IOException("NULL FILE POINTER IN THE FTP SERVER");
		}
		
		//file is a directory?
		if(ftpFile.isDirectory()){
			throw new IOException("FILE POINTER IS A DIRECTORY");
		}
		
		//its a file and exists. start download stream...
		InputStream is = ftpClient.retrieveFileStream(filepath);
		
		//how the server replied to the fetch command?
		int ftpReplyCode = ftpClient.getReplyCode();
		
		//denied?
		if(FTPReply.isNegativePermanent(ftpReplyCode)) {
			throw new IOException("SERVER FTP:REQUEST DENIED");
			
		//can we try again?
		} else if(FTPReply.isNegativeTransient(ftpReplyCode)) {
			//close the already open stream before try again...
			if(is != null){
				is.close();
			}
			return downloadFile(filepath);
		}
		
		//server accepted the command
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		//copying the file
		IOUtils.copy(is, baos);
		
		//closing open streams
		is.close();
		baos.flush();
		baos.close();
		
		//transaction is successful?
		boolean transactionCompleted = ftpClient.completePendingCommand();
		if(!transactionCompleted) {
			return downloadFile(filepath);
		}
		
		//we got the file
		logout();
		return baos.toByteArray();
	}

	public void uploadFile(String filePath, byte[] data) throws IOException {
		login();
		
		OutputStream os = ftpClient.storeFileStream(filePath);
		int ftpUploadReplyCode = ftpClient.getReplyCode();
		
		if(FTPReply.isNegativePermanent(ftpUploadReplyCode)) {
			throw new IOException("SERVER FTP:REQUEST DENIED");
		} else if(FTPReply.isNegativeTransient(ftpUploadReplyCode)){
			uploadFile(filePath, data);
		}
		
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		IOUtils.copy(bais, os);
		
		bais.close();
		os.flush();
		os.close();
		
		 if(!ftpClient.completePendingCommand()) {
			 uploadFile(filePath, data);
		 }
		 
		 logout();
	}
	
	//XXX GETTERS/SETTERS
	private boolean isConnected() {
		return isConnected;
	}
	
	private void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}
}
