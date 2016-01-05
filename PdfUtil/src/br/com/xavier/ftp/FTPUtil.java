package br.com.xavier.ftp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

public class FTPUtil {
	
	private boolean isConnected;
	
	private String host;
	private int hostPort;
	private String user;
	private String password;
	
	private int timeOutSeconds;
	
	private FTPClient ftpClient;
	
	public FTPUtil(String host, int hostPort, String ftpUser, String ftpUserPassword, int timeOutSeconds) {
		this.host = host;
		this.hostPort = hostPort;
		
		this.user = ftpUser;
		this.password = ftpUserPassword;
		
		this.timeOutSeconds = timeOutSeconds;
		
		this.ftpClient = new FTPClient();
		this.isConnected = false;
	}
	
	public boolean isConnected() {
		return isConnected;
	}
	
	public void connect() throws SocketException, IOException{
		ftpClient.connect(host, hostPort);
		this.isConnected = true;
	}
	
	public void login() throws IOException {
		if(!isConnected){
			connect();
		}
		
		ftpClient.login(user, password);
		ftpClient.enterLocalPassiveMode();			
		ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
		ftpClient.setBufferSize(0);
		ftpClient.setControlKeepAliveTimeout(timeOutSeconds);
	}
	
	public void logout() throws IOException {
		ftpClient.logout();
	}
		
	public byte[] downloadFile(String filepath) throws IOException {
		
		FTPFile ftpFile = ftpClient.listFiles(filepath)[0];
		
		if(ftpFile == null){
			throw new IOException("NULL FILE POINTER IN THE FTP SERVER");
		}
		
		if(ftpFile.isDirectory()){
			throw new IOException("FILE POINTER IS A DIRECTORY");
		}
		
		InputStream is = ftpClient.retrieveFileStream(filepath);
		int ftpReplyCode = ftpClient.getReplyCode();
			
		if(FTPReply.isNegativePermanent(ftpReplyCode)) {
			throw new IOException("SERVER FTP:REQUEST DENIED");
		} else if(FTPReply.isNegativeTransient(ftpReplyCode)) {
			if(is != null){
				is.close();
			}
			return downloadFile(filepath);
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		IOUtils.copy(is, baos);
		is.close();
		baos.flush();
		baos.close();
		
		if(!ftpClient.completePendingCommand()) {
			return downloadFile(filepath);
		}
		
		return baos.toByteArray();
	}

	public void uploadFile(String filePath, byte[] data) throws IOException {
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
	}
}
