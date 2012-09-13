/*
 * Copyright 1999 by dreamBean Software,
 * All rights reserved.
 */
package chat.server;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;

import javax.naming.InitialContext;

import chat.interfaces.TopicInfo;

//import com.dreambean.dynaserver.DynaServer;

/**
 *   This is the admin class that manages the chat server.
 *     
 *   @see TopicServerImpl
 *   @author Rickard Öberg (rickard@dreambean.com)
 *   @version $Revision:$
 */
public class Main
{
   // Attributes ----------------------------------------------------
   TopicServerImpl server; // To prevent GC
   
   // Static --------------------------------------------------------
   public static void main(String[] args)
      throws Exception
   {
      // Load system properties
      // System.getProperties().load(
      //    Thread.currentThread().
      //   getContextClassLoader().
      //   getResourceAsStream("system.properties"));
      
      new Main();
   }
   
   // Constructors --------------------------------------------------
   public Main()
   {
      // Start server
      try
      {
      // startWebServer();        // Note: omitted for re-engineering project - TJR
         
      // startNamingServer();	  // Note: omitted for re-engineering project - TJR
         
         startTopicServer();
         
         System.out.println("Chat server has been started");
      
      } catch (Exception e)
      {
         System.out.println("The Chat server could not be started");
         e.printStackTrace(System.err);
      }
   }

   // Public --------------------------------------------------------
   //public void startWebServer()
  //    throws IOException
   //{
  // Start webserver for dynamic class loading -  Note: omitted for re-engineering project - TJR
  //    DynaServer srv = new DynaServer();
  //    srv.addClassLoader(Thread.currentThread().getContextClassLoader());
  //    srv.start();
   //}
   
   public void startNamingServer()
      throws Exception
   {
      // Create registry if not already running
      try 
      {
    	  LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
      } catch (java.rmi.server.ExportException ee) 
      {
         // Registry already exists
      } catch (RemoteException e) 
      {
         throw new ServerException("Could not create registry", e);
      }
   }
   
   public void startTopicServer()
      throws Exception
   {
      // Create remote object
	  // server = new TopicServerImpl(Integer.getInteger("chat.server.threads", 5).intValue());
/**/   server = new TopicServerImpl(2);

      
      // Load configuration
      // server.setPort(Integer.getInteger("chat.server.port", 0).intValue());
	  // server.setPort(0);
      
      // Export server
      // UnicastRemoteObject.exportObject(server);
/**/   Naming.rebind("chat-server", server);     
	   
      // Create a few topics
      server.createTopic(new TopicInfo("Beginner RMI", "Welcome to the RMI beginner forum"));
      server.createTopic(new TopicInfo("Advanced Java", "Welcome to the RMI advanced forum"));
      server.createTopic(new TopicInfo("Cool tricks in RMI", "Welcome to the forum for cool RMI tricks"));
   
      // Register server with naming service
      // new InitialContext().bind("topics",server);
   }
}