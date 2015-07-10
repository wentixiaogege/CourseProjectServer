/**
 * Copyright (c) 2008 Andrew Rapp. All rights reserved.
 *  
 * This file is part of XBee-API.
 *  
 * XBee-API is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * XBee-API is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with XBee-API.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.itu.course.xbee.Server;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/** 
 * To run this example you need to have at least two ZNet XBees powered up and configured to the same PAN ID (ATID) in API mode (2).
 * This software requires the XBee to be configured in API mode; if your ZNet radios are flashed with the transparent (AT) firmware, 
 * you will need to re-flash with API firmware to run this software.
 * 
 * I use the Digi X-CTU software to configure my XBee's, but if you don't have Windows (X-CTU only works on Windows), you can still use the configureCoordinator and
 * configureEndDevice methods in ZNetApiAtTest.java.
 * 
 * There are a few chicken and egg situations where you need to know some basic configuration before you can connect to the XBee.  This
 * includes the baud rate and the API mode.  The default baud rate is 9600 and if you ever change it, you will want to remember the setting.  
 * If you can't connect at 9600 and you don't know the baud rate, try all possibilities until it works.  Same with the API Mode: if you click
 * Test/Query in X-CTU, try changing the API mode until it succeeds, then write it down somewhere for next time.
 *  
 * Here's my setup configuration (assumes factory configuration):
 * 
 * COORDINATOR config:
 * 
 * - Reset to factory settings: 
 * ATRE
 * - Put in API mode 2
 * ATAP 2
 * - Set PAN id to arbitrary value
 * ATID 1AAA
 * - Set the Node Identifier (give it a meaningful name)
 * ATNI COORDINATOR
 * - Save config
 * ATWR
 * - reboot
 * ATFR
 *
 * The XBee network will assign the network 16-bit MY address.  The coordinator MY address is always 0
 * 
 * X-CTU tells me my SH Address is 00 13 a2 00 and SL is 40 0a 3e 02
 * 
 * END DEVICE config:
 * 
 * - Reset to factory settings: 
 * ATRE
 * - Put in API mode 2
 * ATAP 2
 * - Set PAN id to arbitrary value
 * ATID 1AAA
 * - Set the Node Identifier (give it a meaningful name)
 * ATNI END_DEVICE_1
 * - Save config
 * ATWR
 * - reboot
 * ATFR
 *   
 * Only one XBee needs to be connected to the computer (serial-usb); the other may be remote, but can also be connected to the computer.
 * I use the XBee Explorer from SparkFun to connect my XBees to my computer as it makes it incredibly easy -- just drop in the XBee.
 * 
 * For this example, I use my XBee COORDINATOR as my "sender" (runs this class) and the END DEVICE as my "receiver" XBee.
 * You could alternatively use your END DEVICE as the sender -- it doesn't matter because any XBee, either configured as a COORDINATOR
 * or END DEVICE, can both send and receive.
 * 
 * How to find the COM port:
 * 
 * Java is nice in that it runs on many platforms.  I use mac/windows and linux (server) and the com port is different on all three.
 * On the mac it appears as /dev/tty.usbserial-A6005v5M on my machine.  I just plug in each XBee one at a time and check the /dev dir
 * to match the XBee to the device name: ls -l /dev/tty.u (hit tab twice to see all entries)
 *
 * On Windows you can simply select Start->My Computer->Manage, select Device Manager and expand "Ports" 
 * 
 * For Linux I'm not exactly sure just yet although I found mine by trial and error to be /dev/ttyUSB0  I think it could easily be different
 * for other distros.
 * 
 * To run, simply right-click on the class, in the left pane, and select Run As->Java Application.  Eclipse will let you run multiple
 * processes in one IDE, but there is only one console and it will switch between the two processes as it is updated.  
 * 
 * If you are running the sender and receiver in the same eclipse, remember to hit the terminate button twice to kill both
 * or you won't be able to start it again.  If this situation occurs, simply restart eclipse.
 * 
 * @author andrew
 */
public class ZNetSenderExample {

	private final static Logger log = Logger.getLogger(ZNetSenderExample.class);
	/*
	private ZNetSenderExample() throws XBeeException {
		
		XBee xbee = new XBee();
		
		// replace with port and baud rate of your XBee. this is the com port of my coordinator
		//coord
		xbee.open("/dev/ttyUSB0", 9600);
		// coord (21A7)
		//XBeeAddress64 addr64 = new XBeeAddress64(0, 0x13, 0xa2, 0, 0x40, 0x8b, 0x98, 0xfe);
		
		// replace with end device's 64-bit address (SH + SL)
		// router (firmware 23A7)
//		XBegetResponseeAddress64 addr64 = new XBeeAddress64(0, 0x13, 0xa2, 0, 0x40, 0x8b, 0x98, 0xff);
		XBeeAddress16 address16 = new XBeeAddress16(0xFF,0xFF);
		 
		// create an array of arbitrary data to send
		int[] payload = new int[] { 'X', 'B', 'e', 'e' };
		
		// first request we just send 64-bit address.  we get 16-bit network address with status response
//		ZNetTxRequest request = new ZNetTxRequest(addr64, payload);
		
		TxRequest16 request = new TxRequest16(address16, payload);
		
		log.debug("zb request is " + request.getXBeePacket().getPacket());
		
		log.info("sending tx " + request);
		
		while (true) {
			log.info("request packet bytes (base 16) " + ByteUtils.toBase16(request.getXBeePacket().getPacket()));
			
			long start = System.currentTimeMillis();
			//log.info("sending tx packet: " + request.toString());
			
			try {
//				TxStatusResponse response = (TxStatusResponse)xbee.sendAsynchronous((XBeeRequest)request);
				TxStatusResponse response = (TxStatusResponse) xbee.sendSynchronous(request,10000);
				
//				ZNetTxStatusResponse response = (ZNetTxStatusResponse) xbee.sendSynchronous(request, 10000);
				// update frame id for next request
				request.setFrameId(xbee.getNextFrameId());
				
				log.info("received response " + response);
				
				//log.debug("status response bytes:" + ByteUtils.toBase16(response.getPacketBytes()));

				if (response.isSuccess()) {
			        // packet was delivered successfully
//			        message = RS.rbLabel(KEY.SERVICE_WIRELESS_ACK_SUCCESS, bytes, event.getSource().getAddress(), response.getStatus());
			        log.info(response.getStatus());
//			        UGateKeeper.DEFAULT.notifyListeners(event.clone(UGateEvent.Type.WIRELESS_DATA_TX_ACK, i, message));
			      } else {
			        // packet was not delivered
//			        failureCount++;
//			        message = RS.rbLabel(KEY.SERVICE_WIRELESS_ACK_FAILED, bytes, event.getSource().getAddress(), response.getStatus());
			        log.error(response.getStatus());
//			        UGateKeeper.DEFAULT.notifyListeners(event.clone(UGateEvent.Type.WIRELESS_DATA_TX_ACK_FAILED, i, message));
			      }
				
				// I get the following message: Response in 75, Delivery status is SUCCESS, 16-bit address is 0x08 0xe5, retry count is 0, discovery status is SUCCESS 
//				log.info("Response in " + (System.currentTimeMillis() - start) + ", Delivery status is " + response.getDeliveryStatus() + ", 16-bit address is " + ByteUtils.toBase16(response.getRemoteAddress16().getAddress()) + ", retry count is " +  response.getRetryCount() + ", discovery status is " + response.getDeliveryStatus());					
			} catch (XBeeTimeoutException e) {
				log.warn("request timed out");
			}
			try {
				// wait a bit then send another packet
				Thread.sleep(10000);
			} catch (InterruptedException e) {
			}
		}
	}*/
	/*
		public static void main(String[] args) throws Exception {
			// init log4j
			PropertyConfigurator.configure("log4j.properties");
			
//			long patience = 1000 * 60 * 60;
			threadMessage("Starting XbeeCommunication thread");
//		    long startTime = System.currentTimeMillis();
			Thread t = new Thread(new XbeeCommunication());
	        t.start();
	        // loop until MessageLoop
	        // thread exits
	        while (t.isAlive()) {
	        	threadMessage("Still waiting...");
	            // Wait maximum of 1 second
	            // for MessageLoop thread
	            // to finish.
	            t.join(1000);
	            
	            
	            if (((System.currentTimeMillis() - startTime) > patience)
	                  && t.isAlive()) {
	            	threadMessage("Tired of waiting!");
	                t.interrupt();
	                // Shouldn't be long now
	                // -- wait indefinitely
	                t.join();
	            }
	        }
		}
		*/
	static void threadMessage(String message) {
		        String threadName =
		            Thread.currentThread().getName();
		        log.info(String.format("%s: %s%n",
		                          threadName,
		                          message));
		    }
}
