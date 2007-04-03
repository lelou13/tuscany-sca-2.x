package org.apache.tuscany.sca.test;

import java.io.File;

import junit.framework.Assert;

import org.osoa.sca.annotations.Callback;
import org.osoa.sca.annotations.ConversationAttributes;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Scope;
import org.osoa.sca.annotations.Service;

@Service(ConversationsLifeCycleService.class)
@Scope("CONVERSATION")
@ConversationAttributes(maxAge="5 seconds")

// This is a short-lived conversational service. 

public class ConversationsLifeCycleServiceImpl implements ConversationsLifeCycleService {
	
	@Callback 	
	protected ConversationsCallback callback; 
	
    private String state = "NOT READY";
    private final static String markerFileName = "target/testConversations_test3_marker"; 

	
	public String knockKnock(String aString) { 		
		
	  // Prior to any business methods being invoked the state must have been set to READY using the @Init.
	  System.out.println("ConversationsLifeCycleServiceImpl.knockKnock - State = " + state);	
	  Assert.assertEquals("ConversationsLifeCycleServiceImpl.knockKnock - not in READY state ", "READY", state);	
	 
	  return "Who's There!";	  
	}
	
	@Init
	public void init()
	{
	 state = "READY";
	 System.out.println("ConversationsLifeCycleServiceImpl.init()");	
	}
	
	
	@Destroy
	public void destroy()
	{
	  state = "DESTROYED";
	  
	  File aFile = new File(markerFileName);
	    try
	    {
	     aFile.createNewFile();
	    }
	    catch (Exception ex) 
	    {
	     System.out.println("Error Creating " + markerFileName);
	     ex.printStackTrace();
	    }  
	  System.out.println("ConversationsLifeCycleServiceImpl.destroy()");	
	}

//	@EndsConversation
	public void endThisSession() {
    //This method will end the current session by annotation.	
    System.out.println("ConversationsLifeCycleServiceImpl.endThisSession()");		
		
	}
	
	public void endThisSessionUsingCallback() {
		
	//This method will end the current session by a callback method annotated with @EndSession.
	callback.callBackEndSession();	
	System.out.println("ConversationsLifeCycleServiceImpl.endThisSessionUsingCallback()");		
	}
	
}

