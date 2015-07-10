package edu.itu.course.xbee.Server;


import java.util.Properties;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import se.hirt.w1.Sensor;
import se.hirt.w1.Sensors;

import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.XBeeTimeoutException;
import com.rapplogic.xbee.api.wpan.RxResponse16;
import com.rapplogic.xbee.api.wpan.TxRequest16;
import com.rapplogic.xbee.api.wpan.TxStatusResponse;
import com.rapplogic.xbee.util.ByteUtils;

import edu.itu.course.PropertyReading;
import edu.itu.course.XbeeEnum;

public class XbeeCommunication  {

	private final static Logger log = Logger
			.getLogger(XbeeCommunication.class);

	// using future

	//
	public static void main(String[] args) throws Exception {

		Properties props = new Properties();
		props.load(XbeeCommunication.class.getResourceAsStream("/log4j.properties"));
		
		PropertyConfigurator.configure(props);		
		final XbeeCommunication xbeeCommunication = new XbeeCommunication();
		
		final XBee xbee = new XBee();
		PropertyReading propertyReading = new PropertyReading();
		
		int i=1;
		try {

			    log.info("xbee opening---------");
			
			    xbee.open(propertyReading.getXbeeDevice(),Integer.parseInt(propertyReading.getXbeeBaud()));
//			    xbee.open("/dev/ttyUSB0",9600);
			    
			    log.info("xbee opened---------");
			    
			    
				while (true) {
					
					try {
						//sending command reading temp data
						xbeeCommunication.sendXbeeData(xbee,XbeeEnum.READING.getValue());
						
						//waiting for answer
						String receivedString ;
						if ((receivedString = xbeeCommunication.receiveXbeeData(xbee)) != null ) {
							
							if (receivedString.equals(XbeeEnum.ERROR_RESPONSE.getValue())) {
								log.error("error xbeeCommunication.sendXbeeData(xbee,XbeeEnum.READING.getValue());");
								break;
							}
							log.info("---> received Data is :"+receivedString);
							
							
							if (receivedString.equals(XbeeEnum.READING_DONE.getValue())) {
								log.info("command reading data succuess!!");
							}
							
						}
						else if (i%2 == 0) {
							xbeeCommunication.sendXbeeData(xbee, XbeeEnum.RELAY_ON.getValue());
							if ((receivedString = xbeeCommunication.receiveXbeeData(xbee)) != null ) {
								
								log.info("---> received Data is :"+receivedString);
								if (receivedString.equals(XbeeEnum.ERROR_RESPONSE.getValue())) {
									log.error("error xbeeCommunication.sendXbeeData(xbee, XbeeEnum.RELAY_ON.getValue());");
									break;
								}
								if (receivedString.equals(XbeeEnum.RELAY_ON_DONE.getValue())) {
									log.info("command relayon succuess!!");
								}
								
								
							}
						}else{
							xbeeCommunication.sendXbeeData(xbee, XbeeEnum.RELAY_OFF.getValue());
							if ((receivedString = xbeeCommunication.receiveXbeeData(xbee)) != null ) {
								
								log.info("---> received Data is :"+receivedString);
								if (receivedString.equals(XbeeEnum.ERROR_RESPONSE.getValue())) {
									log.error("error xbeeCommunication.sendXbeeData(xbee, XbeeEnum.RELAY_OFF.getValue()); here");
									break;
								}
								if (receivedString.equals(XbeeEnum.RELAY_OFF_DONE.getValue())) {
									log.info("command relayoff succuess!!");
								}
								
							}
						}
						
						i++;
						Thread.sleep(3000);
						
					} catch (Exception e) {
						log.error(e);
					}
					
				}
			} catch (XBeeException e1) {
				// TODO Auto-generated catch block
				System.out.println("coming XBeeException=========================");

				e1.printStackTrace();
				log.error(e1);
			   } finally {
				   System.out.println("coming XBeeException= finally========================");
				  if (xbee != null && xbee.isConnected()) {
					xbee.close();
				}
			  }
	}
	
	/*// using threads
	@Override
	public void run() {

//		String transferData = "error";
		XBee xbee = new XBee();
		int i =1;
		PropertyReading propertyReading = new PropertyReading();
		try {
			
			    xbee.open(propertyReading.getXbeeDevice(),Integer.parseInt(propertyReading.getXbeeBaud()));
			    
				while (true) {
					
					try {
						//sending command reading temp data
						sendXbeeData(xbee,XbeeEnum.READING.toString());
						
						//waiting for answer
						String receivedString ;
						if ((receivedString = receiveXbeeData(xbee)) != null ) {
							
							if (receivedString.equals(XbeeEnum.ERROR_RESPONSE)) {
								log.error("error happens here");
								break;
							}
							log.info("---> received Data is :"+receivedString);
							
							
							if (receivedString.equals(XbeeEnum.READING_DONE)) {
								log.info("command reading data succuess!!");
							}
							
						}else{
							log.info("get temp data error!!!!!!!!!!!!!!!!!!");
						}
						
						//controlling the device
						if (i%2 == 0) {
							sendXbeeData(xbee, XbeeEnum.RELAY_ON_DONE.toString());
							if ((receivedString = receiveXbeeData(xbee)) != null ) {
								
								log.info("---> received Data is :"+receivedString);
								if (receivedString.equals(XbeeEnum.READING_DONE)) {
									log.info("command relayon succuess!!");
								}
								if (receivedString.equals(XbeeEnum.ERROR_RESPONSE)) {
									log.error("error happens here");
									break;
								}
								
							}else{
								log.info("get temp data error!!!!!!!!!!!!!!!!!!");
							}
						}else{
							sendXbeeData(xbee, XbeeEnum.RELAY_OFF_DONE.toString());
							if ((receivedString = receiveXbeeData(xbee)) != null ) {
								
								log.info("---> received Data is :"+receivedString);
								if (receivedString.equals(XbeeEnum.READING_DONE)) {
									log.info("command relayoff succuess!!");
								}
								if (receivedString.equals(XbeeEnum.ERROR_RESPONSE)) {
									log.error("error happens here");
									break;
								}
								
							}else{
								log.info("get temp data error!!!!!!!!!!!!!!!!!!");
							}
						}
						
						
						Thread.sleep(100);
						
					} catch (Exception e) {
						log.error(e);
					}
					
				}
			} catch (XBeeException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				log.error(e1);
			   } finally {
				  if (xbee != null && xbee.isConnected()) {
					xbee.close();
				}
			  }
	}*/
	public String receiveXbeeData(XBee xbee) throws XBeeException {

		try {
			//forever waiting here
			XBeeResponse response = xbee.getResponse();

			log.debug("received response " + response.toString());

			if (response.getApiId() == ApiId.RX_16_RESPONSE) {
				// we received a packet from ZNetSenderTest.java
				RxResponse16 rx = (RxResponse16) response;

				log.debug("Received RX packet, options is" + rx.getOptions()
						+ ", sender address is " + rx.getRemoteAddress()
						+ ", data is " + ByteUtils.toString(rx.getData()));
				

				return ByteUtils.toString(rx.getData());
			}
		}catch(XBeeTimeoutException timeout){
			
			log.info("server timeout"+timeout.getMessage());
			throw new XBeeTimeoutException();
		}
		catch (XBeeException e1)
		{
				// TODO Auto-generated catch block
				e1.printStackTrace();
				log.error(e1);
				throw new XBeeException(e1);
		}
		return null;

	}

	public void sendXbeeData(XBee xbee,String data) {
		//should add into the properties file
		PropertyReading propertyReading = new PropertyReading();
		
		int msb= DatatypeConverter.parseHexBinary(propertyReading.getServerXbeeAddress())[0];
		int lsb= DatatypeConverter.parseHexBinary(propertyReading.getServerXbeeAddress())[1];
		
		XBeeAddress16 address16 = new XBeeAddress16(msb,lsb);
		
		final int[] payload = data.chars().toArray();
//		final int[] payload = data.toCharArray();
		
		TxRequest16 request = new TxRequest16(address16, payload);
		
		log.debug("sending tx packet: " + request.toString());
		
		try {
			TxStatusResponse response = (TxStatusResponse) xbee.sendSynchronous(request,10000);
			
			request.setFrameId(xbee.getNextFrameId());
			
			log.debug("received response " + response);

			if (response.isSuccess()) {
		        log.info("response is Success"+response.getStatus());
		      } else {
		        log.error("response is Error"+response.getStatus());
		      }
		} catch (XBeeTimeoutException e) {
			log.warn("request timed out");
		} catch (XBeeException e) {
			e.printStackTrace();
		}
		
	}

	// using constructor
	/*
	 * private XbeeCommunication() throws Exception {
	 * 
	 * XBee xbee = new XBee(); // create gpio controller final GpioController
	 * gpio = GpioFactory.getInstance();
	 * 
	 * // provision gpio pin #12 as an output pin and turn on final
	 * GpioPinDigitalOutput pin =
	 * gpio.provisionDigitalOutputPin(RaspiPin.GPIO_12, "MyLED", PinState.LOW);
	 * 
	 * try {
	 * 
	 * // replace with the com port of your receiving XBee (typically your end
	 * device) xbee.open("/dev/ttyUSB0", 9600);
	 * 
	 * while (true) { try { // we wait here until a packet is received.
	 * XBeeResponse response = xbee.getResponse();
	 * 
	 * log.info("received response " + response.toString());
	 * 
	 * if (response.getApiId() == ApiId.RX_16_RESPONSE) { // we received a
	 * packet from ZNetSenderTest.java RxResponse16 rx = (RxResponse16)
	 * response;
	 * 
	 * log.info("Received RX packet, options is" + rx.getOptions()+
	 * ", sender address is "+rx.getRemoteAddress() + ", data is "+
	 * ByteUtils.toString(rx.getData())); // optionally we may want to get the
	 * signal strength (RSSI) of the last hop. // keep in mind if you have
	 * routers in your network, this will be the signal of the last hop.
	 * AtCommand at = new AtCommand("DB"); xbee.sendAsynchronous(at);
	 * XBeeResponse atResponse = xbee.getResponse();
	 * 
	 * if (atResponse.getApiId() == ApiId.AT_RESPONSE) { // remember rssi is a
	 * negative db value log.info("RSSI of last response is " +
	 * -((AtCommandResponse)atResponse).getValue()[0]); } else { // we didn't
	 * get an AT response log.info("expected RSSI, but received " +
	 * atResponse.toString()); }
	 * log.info("<--Pi4J--> GPIO Control Example ... started.");
	 * 
	 * 
	 * // ToggleLED(pin,state); pin.toggle(); Thread.sleep(5000);
	 * 
	 * } else { log.debug("received unexpected packet " + response.toString());
	 * } } catch (Exception e) { log.error(e); } } } finally { if (xbee != null
	 * && xbee.isConnected()) { xbee.close(); } } }
	 */

}

