# Limoo java SDK
A Java SDK for Limoo.

This SDK is assumed to be used with Java version 8. It might be incompatible with newer versions.
  
*Wondering what Limoo is? Visit https://limoo.im*  
  
*Give Limoo a try: https://web.limoo.im*

### Dependencies
com.squareup.okhttp3:okhttp:4.9.1  
com.squareup.okhttp3:okhttp-urlconnection:4.9.1  
com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:2.12.4  
com.fasterxml.jackson.core:jackson-databind:2.12.4  
log4j:log4j:1.2.17  
org.slf4j:slf4j-log4j12:1.7.31  
org.atmosphere:wasync:2.1.7  

### Example usage
```java
// Create a new LimooDriver instance by limoo server, workspace key, bot username and bot password
LimooDriver ld = new LimooDriver("https://web.limoo.im/Limonad", "test_bot_username", "test_bot_password");

// Get a workspace by its key
Workspace w = ld.getWorkspaceByKey("test");

// Get a conversation by its id
Conversation c = w.getConversationById("conversationExtuid");

// Get a list of new messages in the conversation (messages which have not been viewed by the bot)
List<Message> unreadMessages = c.getUnreadMessages();

// Send a simple message in the conversation
c.send("Hi everyone!");

// Send a message containing a file
c.send(new Message.Builder().text("Here's a file!").file(new File("test.txt")));

// Get all bot conversations
List<Conversation> conversations = w.getConversations();

// Register a new MessageCreatedEventListener which notifies you whenever a new message is sent in the conversation
ld.addEventListener(new MessageCreatedEventListener() {
	@Override
	public void onNewMessage(Message msg, Conversation c) {
		System.out.println(msg.getText());
		
		// Download attachments of the message
		if (msg.getFileInfos() != null) {
			for (MessageFile messageFile : msg.getFileInfos()) {
				try (InputStream inputStream = messageFile.download()) {
					System.out.println(inputStream.available());
				} catch (IOException e) {
					e.printStackTrace();
				} catch (LimooException e) {
					e.printStackTrace();
				}
			}
		}

		// Send a message in the thread of the new message (msg can be root of a thread only if its threadRootId is null)
		if (msg.getThreadRootId() == null) {
			try {
				c.send(new Message.Builder().text("Message received").threadRootId(msg.getId()));
			} catch (LimooException e) {
				e.printStackTrace();
			}
		}
	}
});

// When you're done with the driver:
ld.close();
```

### Bot creation
In order to create a bot, send the following command in your direct conversation with Limoo Bot:

<div dir="rtl">

```
/ساخت-بات my_bot bot_nickname
```

</div>

Note that only admins of the workspace can create bots.
