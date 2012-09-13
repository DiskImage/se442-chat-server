/*
 * Copyright 1999 by dreamBean Software,
 * All rights reserved.
 */
package chat.server;

import java.io.IOException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import chat.interfaces.*;

/**
 *   This is the implementation of a worker. It will acquire
 *   messages from the server and process them.
 *      
 *   @author Rickard Öberg (rickard@dreambean.com)
 *   @version $Revision:$
 */
class Worker
   implements Runnable
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   TopicServerImpl server;
   
   // Static --------------------------------------------------------
   // Metrics for throughput
   // Count how many messages are being sent and calculate
   // throughput every 5 seconds.
   
   // When the last check was performed
   static long lastCheck;
   
   // Nr of sent messages since last metric check
   static int messageCount = 0;
   static int maxMessageSend = 0;
   
   // Create throughput metric output thread
   static
   {
      new Thread(new Runnable()
      {
         public void run()
         {
            // Initialize check tracker
            lastCheck = System.currentTimeMillis();
            
            while (true)
            {
               // Wait 5 seconds
               try
               {
                  Thread.sleep(5000);
               } catch (InterruptedException e)
               {
                  // Ignore
               }
               
               synchronized (Worker.class)
               {
                  // Calculate and pring throughput
                  long now = System.currentTimeMillis();
                  long throughput = (messageCount*1000)/(now-lastCheck);
                  System.out.println("Throughput (messages/second):"+throughput + ", Max message batch send:"+maxMessageSend);
               
                  // Reset counter and note current time
                  messageCount = 0;
                  maxMessageSend = 0;
                  lastCheck = System.currentTimeMillis();
               }
            }
         }
      }).start();
   }
   
   // Add nr of messages to counter
   static void increaseMessageCount(int sentMessages)
   {
      synchronized (Worker.class)
      {
         messageCount += sentMessages;
         
         // Keep track of how many messages was sent in one single batch transfer
         // If this is too high the client will experience the chat to be choppy
         // and we should increase the number of worker threads
         if (maxMessageSend < sentMessages)
         {
            maxMessageSend = sentMessages;
         }
      }
   }

   // Constructors --------------------------------------------------
   public Worker(TopicServerImpl server)
   {
      this.server = server;
   }
   
   // Public --------------------------------------------------------

   // Runnable implementation ---------------------------------------
   public void run()
   {
      MessageQueue queue;
      while(true)
      {
         // Get queue from server
         queue = server.getNextQueue();
            
         // Send messages to listener
         try
         {
            int count = queue.send();
            
            // Increase message count
            increaseMessageCount(count);
         } catch (IOException e)
         {
            // Client failed - remove it
            server.removeListener(queue.getListener().getInfo());
         }
      }
   }
   
   // Y overrides ---------------------------------------------------

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}