# limonad-java-driver

### Dependencies
com.squareup.okhttp3:okhttp:3.14.4  
com.squareup.okhttp3:okhttp-urlconnection:3.14.4  
com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:2.2.3  
com.fasterxml.jackson.core:jackson-databind:2.9.10  
log4j:log4j:1.2.16  
org.slf4j:slf4j-log4j12:1.6.1  
org.atmosphere:wasync:1.4.3  

### Example usage
```java
// Create a new LimooDriver instance by limoo server, workspace key, bot username and bot password
LimooDriver ld = new LimooDriver("https://beta.limonadapp.ir/Limonad", "test", "test_bot_username", "test_bot_password");

// Get a conversation by its id
Conversation c = ld.getConversationById("conversationExtuid");

// Get a list of new message in the conversation (messages which have not been viewed by the bot)
List<Message> unreadMessages = c.getUnreadMessages();

// Send a message in the conversation
c.send("Hi everyone!");

// Register a new MessageCreatedEventListener which notifies you whenever a new message is sent in the conversation
ld.registerEventListener(new MessageCreatedEventListener(c) {
	@Override
	public void onNewMessage(Message msg) {
		System.out.println(msg.getText());

	    // Send a message in the thread of the new message (msg can be root of a thread only if its threadRootId is null)
		if (msg.getThreadRootId() == null) {
			Message response = c.sendInThread("Message received", msg.getId());
		}
	}
});

// When you're done with the driver:
ld.close();
```
