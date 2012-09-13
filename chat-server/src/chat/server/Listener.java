/*
 * Copyright 1999 by dreamBean Software,
 * All rights reserved.
 */
package chat.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import chat.interfaces.*;

/**
 *   Listeners encapsulate info about a client, and the callback to it.
 *      
 *   @see MessageListener
 *   @see ListenerInfo
 *   @author Rickard Öberg (rickard@dreambean.com)
 *   @version $Revision:$
 */
class Listener
{
   // Attributes ----------------------------------------------------
   ListenerInfo info;
   MessageListener listener;
   MessageQueue queue;
   
   // Constructors --------------------------------------------------
   public Listener(ListenerInfo info, MessageListener listener)
   {
      this.info = info;
      this.listener = listener;
      this.queue = new MessageQueue(this);
   }
   
   // Public --------------------------------------------------------
   public ListenerInfo getInfo() 
   { 
      return info; 
   }
   
   public MessageListener getListener() 
   { 
      return listener; 
   }
   
   public MessageQueue getMessageQueue()
   {
      return queue;
   }
}
