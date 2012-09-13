/*
 * Copyright 1999 by dreamBean Software,
 * All rights reserved.
 */
package chat.server;

import java.io.IOException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import chat.interfaces.*;

/**
 *   This is the implementation of a topic in the server. It is a remote object.
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard@dreambean.com)
 *   @version $Revision:$
 */
public class TopicImpl
   extends UnicastRemoteObject
   implements Topic
{
   // Attributes ----------------------------------------------------
   TopicInfo info;
   Collection messageQueues = new ArrayList();
   Collection listenerInfos = new ArrayList();
   
   TopicServerImpl server;
   
   // Constructors --------------------------------------------------
   public TopicImpl(TopicServerImpl server, TopicInfo info)
      throws RemoteException
   {
      super(server.getPort());
      
      this.info = info;
      this.server = server;
   }
   
   // Topic implementation ------------------------------------------
   public synchronized void publishMessage(Message message)
   {
      server.sendMessage(messageQueues.iterator(), message);
   }
   
   public synchronized Collection getListenerInfos()
   {
      return listenerInfos;
   }

   // Package protected ---------------------------------------------
   synchronized void addListener(Listener listener)
   {
      publishMessage(new Message(Message.SYSTEM, Message.USER_JOINED, listener.getInfo()));
      
      messageQueues.add(listener.getMessageQueue());
      listenerInfos.add(listener.getInfo());
   }

   synchronized void removeListener(Listener listener)
   {
      if (listener != null && listenerInfos.remove(listener.getInfo()))
      {
         messageQueues.remove(listener.getMessageQueue());
         publishMessage(new Message(Message.SYSTEM, Message.USER_LEFT, listener.getInfo()));
      }
   }
}
