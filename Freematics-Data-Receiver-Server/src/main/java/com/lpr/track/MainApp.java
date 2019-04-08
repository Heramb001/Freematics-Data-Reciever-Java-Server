package com.lpr.track;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


import com.lpr.track.entity.Clients;
import com.lpr.track.entity.Data;
import com.lpr.track.entity.ServerFeeds;
import com.lpr.track.entity.Servers;
import com.lpr.track.exception.BusinessException;
import com.lpr.track.service.DataService;
import com.lpr.track.service.ServerFeedService;
import com.lpr.track.service.ServersService;
import com.lpr.track.utility.Helper;
import com.lpr.track.utility.HelperConstants;
import com.lpr.track.utility.checkSumUtility;
import com.lpr.track.utility.generalUtilities;
import com.lpr.track.utility.loggers;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.lpr.track.config.AppConfig;
import com.lpr.track.service.ClientService;

/**
 * @author Girish
 *
 */
public class MainApp {

   private static final Logger logger = Logger.getLogger(MainApp.class);

   public static void main(String[] args) throws ParseException, BusinessException  {
      /*
	  AnnotationConfigApplicationContext context = 
            new AnnotationConfigApplicationContext(AppConfig.class);
      ClientService clientService = context.getBean(ClientService.class);
      ServersService serversService= context.getBean(ServersService.class);
      ServerFeedService serverFeedService= context.getBean(ServerFeedService.class);
      DataService dataService= context.getBean(DataService.class);
       */
  try {
      logger.info("Post-process begins.");
      /*
      List<Clients> clients = clientService.listClients();
      List<Servers> servers = serversService.listServerDetails();
      List<ServerFeeds> serverFeeds = serverFeedService.listServerFeedidDetails();
	  */
         int port = 8081;
         DatagramSocket dsocket = new DatagramSocket(port);
         boolean eventNotified = false;
         boolean displayed_columns = false;
         //byte[] buffer = new byte[2048];
         byte[] buffer = new byte[30720];
         DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
         while(true){
            dsocket.receive(packet);
            String msg = new String(buffer, 0, packet.getLength());
            //System.out.println("--> Hostname : "+packet.getAddress().getHostName() + ": "
            //        + msg);
            packet.setLength(buffer.length);
            
            if(!msg.isEmpty() && msg != null) {
                //System.out.println("--> MSG : " + msg);
            	String[] feedidIdnty = msg.trim().split("#");
            	InetAddress address = packet.getAddress();
                int portToSend = packet.getPort();
                List<String> elementsList = new ArrayList();
                boolean clientexist = false;
                //System.out.println("--> Message : "+feedidIdnty[0]);
                if(msg.contains("#")){
                	elementsList = Arrays.asList(feedidIdnty[1].split(","));
                	//logger.info("--> Request Recieved, Preparing response to send");
                    //System.out.println("--> Request Recieved, Preparing response to send");
                }else{
                	elementsList = Arrays.asList(msg.split(","));
                }
                /* Input Message Example
                 * ABCDEFG#EV=1,SK=TEST_SERVER_KEY,TS=39539,VIN=A1JC5444R7252367*XX
				 * ABCDEFG is device ID
				 * EV=1 indicates a login event
				 * SK=… specifies server key
				 * TS=… the current timestamp (normally the device timer counter)
				 * VIN=… specifies the VIN which should be unique and is used to allocate Feed ID
				 * *XX is the checksum (in hex) of all previous bytes
                 * */
                String feedId = "";
                String EV = "";
                String TS = "";
                String Vin = "";
                String XX_cs = "";
                //int i = 10;
                //System.out.println("--> Elements : " + elementsList.toString());
                if(elementsList.size() !=0) {
                	for (String element : elementsList) {
                        //System.out.println("--> EV : "+EV+", TS : "+TS+", VIN : "+Vin+", Check SUM XX : "+XX_cs);
                		//System.out.println("--> No of elements in List : "+elementsList.size());
                		String[] eachElement = element.trim().split("=");
                		if (eachElement[0].equalsIgnoreCase(HelperConstants.EVENT)) {
                            EV = eachElement[1];
                         } else if (eachElement[0].equalsIgnoreCase(HelperConstants.TIME_STAMP_COUNTER)) {
                            TS = eachElement[1];
                         } else if (eachElement[0].equalsIgnoreCase(HelperConstants.VIN)) {
                        	 String[] ServerElement = eachElement[1].trim().split("\\*");
                             Vin = ServerElement[0];
                             XX_cs = ServerElement[1];
                         }
                	}
                }
                /* sendString sample = 1#EV=1,RX=n,TS=39539*X
                 * First number (in Hex) is the feed ID (arbitrary)
				 * EV=n is the event ID from request
				 * TS=… is the timestamp from the request
				 * RX=… is the number of received requests/data transmissions && i>0
				 * XX is the checksum (in hex) of all previous bytes*/
                if(EV.equals("1")  && !clientexist){
                	//String sendString = feedId+"#EV="+EV+",RX=1000,TS="+TS+"*"+XX_cs;
                	String sendString = "1#EV="+EV+",RX=1000,TS="+TS+"*"+XX_cs;
                	//Get Updated Checksum
                	String updatedCheckSum = checkSumUtility.getCheckSum(sendString);
                	sendString = "1#EV="+EV+",RX=1000,TS="+TS+"*"+updatedCheckSum;
                    byte[] sendData = sendString.getBytes(StandardCharsets.US_ASCII);
                    System.out.println("--> Replying with response : "+sendString);
                    System.out.println(portToSend + " PORT: address " + address);
                    logger.info("--> Replying with repsonse : "+sendString);
                    logger.info(portToSend + " PORT: address " + address);
                    DatagramPacket packet1 = new DatagramPacket(sendData, sendData.length, address, portToSend);
                    dsocket.send(packet1);
                    clientexist = true;
                }else if(EV.equals("2")){
                	logger.info("--> Ride Interrupted.");
                	System.out.println("--> Ride Interrupted.");
                	clientexist = false;
                	displayed_columns = false;
                }else{
                	//Data transfer Started
                	//System.out.println("--> Device Replying Back With Repsonse");
                	if(!displayed_columns){
                		generalUtilities.displayColumnNames();
                		displayed_columns = true;
                	}
                	
                	//Vehicle_id Vehicle_Speed Engine_RPM Throttle_Position EngineLoad TimingAdvance Accelerometer BatteryVoltage CPUTemperature
                	String Vehichle_Speed="",
                    		Engine_RPM = "",
                    		ThrottlePosition = "",
                    		EngineLoad = "",
                    		TimingAdvance = "",
                    		Accelerometer = "",
                    		BatteryVoltage = "",
                    		CPUTemperature = "";
                	for (String element : elementsList) {
                        //System.out.println("--> EV : "+EV+", TS : "+TS+", VIN : "+Vin+", Check SUM XX : "+XX_cs);
                		//System.out.println("--> No of elements in List : "+elementsList.size());
                		String[] eachElement = element.trim().split(":");
                		if (eachElement[0].equalsIgnoreCase(HelperConstants.TEND)) {
                			Vehichle_Speed = eachElement[1];
                         } else if (eachElement[0].equalsIgnoreCase(HelperConstants.TENC)) {
                            Engine_RPM = eachElement[1];
                         } else if (eachElement[0].equalsIgnoreCase(HelperConstants.THRIBBLEONE)) {
                             ThrottlePosition = eachElement[1];
                          }else if (eachElement[0].equalsIgnoreCase(HelperConstants.ONEZEROFOUR)) {
                              EngineLoad = eachElement[1];
                          }else if (eachElement[0].equalsIgnoreCase(HelperConstants.TENE)) {
                              TimingAdvance = eachElement[1];
                          } else if (eachElement[0].equalsIgnoreCase(HelperConstants.TWENTY)) {
                              Accelerometer = eachElement[1];
                          }else if (eachElement[0].equalsIgnoreCase(HelperConstants.TEWENTYFOUR)) {
                        	  String[] ServerElement = eachElement[1].trim().split("\\*");
                        	  BatteryVoltage = ServerElement[0];
                              //BatteryVoltage = eachElement[1];
                          }else if (eachElement[0].equalsIgnoreCase(HelperConstants.EIGHTYTWO)) {
                        	  String[] ServerElement = eachElement[1].trim().split("\\*");
                              CPUTemperature = ServerElement[0];
                          }
                         else if (eachElement[0].equalsIgnoreCase(HelperConstants.VIN)) {
                        	 String[] ServerElement = eachElement[1].trim().split("\\*");
                             //Vin = ServerElement[0];
                             //XX_cs = ServerElement[1];
                         }
                	}
                	logger.info("--> Handshake Complete, Data Transfer Started");
                	System.out.println(feedidIdnty[0]+" ,   "+Vehichle_Speed+"   ,   "+Engine_RPM+"   ,   "+ThrottlePosition+"   ,   "+EngineLoad+"   ,   "+ThrottlePosition+"   ,   "+EngineLoad+"   ,   "+TimingAdvance+"   ,   "+Accelerometer+"   ,   "+BatteryVoltage+"   ,   "+CPUTemperature+".");
                	loggers.DataLogger.info(feedidIdnty[0]+"   ,   "+Vehichle_Speed+"   ,   "+Engine_RPM+"   ,   "+ThrottlePosition+"   ,   "+EngineLoad+"   ,   "+ThrottlePosition+"   ,   "+EngineLoad+"   ,   "+TimingAdvance+"   ,   "+Accelerometer+"   ,   "+BatteryVoltage+"   ,   "+CPUTemperature+"");
                }
            }
         }
         /*
            if (!msg.isEmpty() && msg != null) {
               String[] feedidIdnty = msg.trim().split("#");
               int feedId = Integer.parseInt(feedidIdnty[0]);
               int i = 10;
               InetAddress address = packet.getAddress();
               int portToSend = packet.getPort();

               if(feedId==0) {
//                  0#EV=1,TS=67538,VIN=1A1JC5444R7252367,SK=LPR01*E8
                  String event="";
                  String serverKey="";
                  String timeStampCounter="";
                  String vin="";
                  int minFeedid=0;
                  int maxFeedid=0;
                  int sendFeedid=0;
                  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                  Date date = new Date();
                  boolean clientExist= false;
                  List<String> elementsList = Arrays.asList(msg.split(","));

                  if(elementsList.size() !=0) {

                     for (String element : elementsList) {

                           String[] eachElement = element.trim().split("=");
                      //    System.out.println(eachElement[0]+" "+eachElement[1]);
                           if (eachElement[0].equalsIgnoreCase(HelperConstants.EVENT)) {
                              event = eachElement[1];
                           } else if (eachElement[0].equalsIgnoreCase(HelperConstants.TIME_STAMP_COUNTER)) {
                              timeStampCounter = eachElement[1];
                           } else if (eachElement[0].equalsIgnoreCase(HelperConstants.VIN)) {
                               vin= eachElement[1];
                           } else if (eachElement[0].equalsIgnoreCase(HelperConstants.SERVER_KEY)){
                              String[] ServerElement = eachElement[1].trim().split("\\*");
                              serverKey= ServerElement[0];
                           }

                     }
                   //  System.out.println("Server Key "+serverKey);
                     for (Servers server : servers) {
                        if(server.getSerkey().equalsIgnoreCase(serverKey)){
                             minFeedid=server.getSfeedidmin();
                             maxFeedid=server.getSfeedidmax();
                             clientExist= true;
                             break;

                        }
                     }
                     if(clientExist) {
                        String dateNow1 = sdf.format(date);
                        java.util.Date dn = sdf.parse(dateNow1);
                        ServerFeeds sf;
                        List<ServerFeeds> serverFeedsList= serverFeedService.getInfobyDateandKey(serverKey,vin,dn);
                        if(serverFeedsList.size() !=0){
                       //    System.out.println("Printing server feed details");
                           String fvin= vin;
                           ServerFeeds sf1 = serverFeedsList.stream()
                                   .filter(sf2 -> sf2.getSfvin().equals(fvin))
                                   .findAny()
                                   .orElse(null);
                           if(sf1 !=null){
                              sendFeedid = sf1.getSffeedid();
                           }else{
                              sf =  Collections.max(serverFeedsList, Comparator.comparing(s -> s.getSffeedid()));
                              sendFeedid = sf.getSffeedid()+1;
                              //later we need to throw exception if it exceeds max feed id
                              sf= new ServerFeeds();
                              sf.setSffeedid(sendFeedid);
                              sf.setSfdate(dn);
                              sf.setSfskey(serverKey);
                              sf.setSfvin(vin);
                              serverFeedService.add(sf);
                           }

                        }else{
                              //update
                         //  System.out.println("Adding todays feed info");
                           sf= new ServerFeeds();
                           sf.setSffeedid(minFeedid);
                           sf.setSfdate(dn);
                           sf.setSfskey(serverKey);
                           sf.setSfvin(vin);
                           serverFeedService.add(sf);
                           sendFeedid=minFeedid;
                        }
                      //feedId=sendFeedid;
                     }
                      //System.out.println("Trasmiting feed id:"+sendFeedid);
                  }

                  while (feedId == 0 && i > 0 && clientExist) {
                     String sendString = sendFeedid+"#EV=1,RX=1000,VIN="+vin;
                     byte[] sendData = sendString.getBytes(StandardCharsets.US_ASCII);
                  //   System.out.println(portToSend + " PORT: address " + address);
                     logger.info(portToSend + " PORT: address " + address);
                     DatagramPacket packet1 = new DatagramPacket(sendData, sendData.length, address, portToSend);
                     dsocket.send(packet1);
                     i--;
                  }
               } else {
                  List<String> dataList = Arrays.asList(msg.split(","));
                  Data data= Helper.splitData(dataList);
                  data.setDfeedId(feedId);
                  dataService.add(data);
               }

            } 
         } */

      } catch (IOException e){
            logger.error(e);
      }
  
      //context.close();
   }
}
