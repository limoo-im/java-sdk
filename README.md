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
void limooDriverTest() {
	LimooDriver ld = new LimooDriver("https://beta.limonadapp.ir/Limonad", "test", "test_bot_username", "test_bot_password");
	Conversation c = ld.getConversationById("conversationExtuid");
	List<Message> unreadMessages = c.getUnreadMessages();
	ld.registerEventListener(new MessageCreatedEventListener(c) {
		@Override
		public void onNewMessage(Message msg) {
			System.out.println(msg.getText());
			if (msg.getThreadRootId() == null) {
				try {
					Message response = c.sendInThread("Message received", msg.getId());
				} catch (LimooException e) {
					e.printStackTrace();
				}
			}
		}
	});
}
```
