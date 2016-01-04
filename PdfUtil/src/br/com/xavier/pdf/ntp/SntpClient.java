package br.com.xavier.pdf.ntp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class SntpClient { 
	private final int  timetOutMilliseconds = 2000;
	private final String[] ntpServersHosts = { 
		"a.st1.ntp.br", "c.st1.ntp.br", "d.st1.ntp.br", "a.ntp.br", "b.ntp.br", "c.ntp.br", "gps.ntp.br" 
	};
	
	private ArrayList<InetAddress> ntpServersAdresses;
	private int ntpServerListPointer;
	
	public SntpClient(){
		this.ntpServerListPointer = -1;
		this.ntpServersAdresses = new ArrayList<InetAddress>();
		registerNtpServers();
	}
	
	private void registerNtpServers(){
		for (int i = 0; i < ntpServersHosts.length; i++) {
			InetAddress ntpServerAddress = resolveNtpServerAddress(ntpServersHosts[i]);
			if(ntpServerAddress != null){
				ntpServersAdresses.add(ntpServerAddress);
			}
		}
	}
	
	public String getCurrentNtpServerAdress(){
		return ntpServersHosts[ntpServerListPointer];
	}
	
	private InetAddress resolveNtpServerAddress(String ntpServerHost){
		try {
			return InetAddress.getByName(ntpServerHost);
		} catch (UnknownHostException e) {
			return null;
		}
	}
	
	private void updateListPointer(){
		ntpServerListPointer++;
		if(ntpServerListPointer >= ntpServersAdresses.size()){
			ntpServerListPointer = 0;
		}
	}
	
	public Date getTime() {
		updateListPointer();
		try {
			// Send request
			DatagramSocket socket = new DatagramSocket();		
			byte[] buf = new NtpMessage().toByteArray();
			DatagramPacket packet = new DatagramPacket(buf, buf.length, ntpServersAdresses.get(ntpServerListPointer), 123);
			
			
			// Set the transmit timestamp *just* before sending the packet
			// ToDo: Does this actually improve performance or not?
			NtpMessage.encodeTimestamp(packet.getData(), 40, (System.currentTimeMillis() / 1000.0) + 2208988800.0);
			socket.setSoTimeout(timetOutMilliseconds);
			socket.send(packet);
			
			// Get response
			packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);
			socket.close();
			// Process response
			NtpMessage msg = new NtpMessage(packet.getData());
			Calendar msgCalendar = NtpMessage.timestampToCalendar(msg.referenceTimestamp);
			Date msgDate = msgCalendar.getTime();
			return msgDate;
		} catch(Exception e){
			return getTime();
		}
	}
}