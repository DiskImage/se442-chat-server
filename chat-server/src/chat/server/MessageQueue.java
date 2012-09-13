/*
 * Copyright 1999 by dreamBean Software,
 * All rights reserved.
 */
package chat.server;

import java.rmi.RemoteException;
import java.util.ArrayList;

import chat.interfaces.*;

/**
 *   This is a message queue for the chat server.
 *      
 *   @see TopicServerImpl
 *   @author Rickard Öberg (rickard@dreambean.com)
 *   @version $Revision:$
 */
class MessageQueue
{
   // Attributes ----------------------------------------------------
   ArrayList messageQueue = new ArrayList();
   Listener listener;
   
   // Constructors --------------------------------------------------
   MessageQueue(Listener listener)
   {
      this.listener = listener;
   }
   
   // Package protected ---------------------------------------------
   synchronized void add(Message message)
   {
      messageQueue.add(message);
   }
   
   synchronized int send()
      throws RemoteException
   {
      int messageCount = messageQueue.size();
   
      // Check nr of messages to send
      if (messageCount == 1)
      {
         // Send the message
         listener.getListener().messagePublished((Message)messageQueue.get(0));
      } else
      {
         // Send all messages
         listener.getListener().messagePublished(messageQueue);
      }
   
      // Remove messages
      messageQueue.clear();
   
      return messageCount;
   }

   synchronized Listener getListener()
   {
      return listener;
   }
}
