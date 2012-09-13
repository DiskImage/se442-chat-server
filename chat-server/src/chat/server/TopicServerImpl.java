/*
 * Copyright 1999 by dreamBean Software,
 * All rights reserved.
 */
package chat.server;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import chat.interfaces.*;

/**
 *   This is the implementation of the chat server. It is a singleton object, 
 *   i.e. there will only be one of this class in the server. It manages the queues
 *   and worker threads.
 *      
 *   @see Worker
 *   @see MessageQueue
 *   @author Rickard Öberg (rickard@dreambean.com)
 *   @version $Revision:$
 */
public class TopicServerImpl extends UnicastRemoteObject
   implements TopicServer
{
   // Attributes ----------------------------------------------------
   Map topics = new HashMap();
   Map listeners = new HashMap();
   
   String greeting = "Welcome to the 'Mastering RMI' chatserver!";
   
   LinkedList queueList = new LinkedList();
   
   int port = 0;
    
   // Constructors --------------------------------------------------
   public TopicServerImpl(int workers)throws RemoteException
   {
      System.out.println("Creating "+workers+" worker threads");
      for (int i = 0; i < workers; i++)
      {
         Thread worker = new Thread(new Worker(this));
         worker.setDaemon(true);
         worker.start();
      }
   }
   
   // Public --------------------------------------------------------
   public void setGreeting(String greeting) 
   { 
      this.greeting = greeting; 
   }
   
   public String getGreeting() 
   { 
      return this.greeting; 
   }
   
   public void setPort(int port)
   {
      this.port = port;
   }
   
   public int getPort()
   {
      return port;
   }

   public synchronized Topic createTopic(TopicInfo info)
      throws IOException
   {
      Topic newTopic = new TopicImpl(this, info);
      topics.put(info, newTopic);
      sendMessage(new Message(Message.SYSTEM,
                              Message.TOPIC_CREATED, 
                              info));
      return newTopic;
   }

   // TopicServer implementation -------------------------------------
   public synchronized Collection getTopicInfos()
   {
      return new ArrayList(topics.keySet());
   }
      
   public synchronized Topic subscribe(TopicInfo info, ListenerInfo clientInfo)
   {
      TopicImpl topic = (TopicImpl)topics.get(info);
      topic.addListener((Listener)listeners.get(clientInfo));
      
      return topic;
   }
   
   public synchronized void unsubscribe(TopicInfo info, ListenerInfo clientInfo)
   {
      TopicImpl topic = (TopicImpl)topics.get(info);
      topic.removeListener((Listener)listeners.get(clientInfo));
   }
   
   public synchronized void addListener(ListenerInfo info, MessageListener callBack)
   {
      Listener listener = new Listener(info, callBack);
      MessageQueue queue = listener.getMessageQueue();
      
      // Add listener to map without interfering
      // with possible concurrent message dispatches
      Map newListeners = (Map)((HashMap)listeners).clone();
      newListeners.put(info, listener);
      listeners = newListeners;
      
      sendMessage(queue, new Message(Message.SYSTEM, 
                                     Message.GREETING, 
                                     greeting));
   }

   public synchronized void removeListener(ListenerInfo info)
   {
      // Remove listener
      Map newListeners = (Map)((HashMap)listeners).clone();
      Listener listener = (Listener)listeners.remove(info);
      listeners = newListeners;
      
      // Remove all topic subscriptions
      Iterator tList = topics.values().iterator();
      while (tList.hasNext())
      {
         ((TopicImpl)tList.next()).removeListener(listener);
      }
      
      // Remove all posted messages to this listener
      synchronized (queueList)
      {
         // remove returns true while queue found in queueList
         while(queueList.remove(listener.getMessageQueue())); 
      }
   }
   // Package protected ---------------------------------------------
   void sendMessage(Message message)
   {
      sendMessage(listeners.values().iterator(), message);
   }
   
   void sendMessage(Iterator queues, Message message)
   {
      while (queues.hasNext())
      {
         MessageQueue queue = (MessageQueue)queues.next();
         sendMessage(queue, message);
      }
   }
   
   void sendMessage(MessageQueue queue, Message message)
   {
      queue.add(message);
      
      synchronized (queueList)
      {
         if (!queueList.contains(queue))
         {
            queueList.addFirst(queue);
            queueList.notify();
         }
      }
   }
   
   MessageQueue getNextQueue()
   {
      while(true)
      {
         MessageQueue queue;
         synchronized(queueList)
         {
            // Wait for message to arrive in queue
            while (queueList.isEmpty())
            {
               try
               {
                  // Wait for sendMessage to call notify()
                  queueList.wait();
               } catch (InterruptedException e)
               {
                  // Ignore
               }
            }
      
            // Get queue
            try
            {
               queue = (MessageQueue)queueList.removeLast();
            } catch (NoSuchElementException e)
            {
               continue;
            }
      
            // More queues waiting?
            if (!queueList.isEmpty())
            {
               // Wake up another worker
               queueList.notify();
            }
         }
      
         return queue;
      }
   }
}