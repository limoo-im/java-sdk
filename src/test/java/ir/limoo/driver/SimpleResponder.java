package ir.limoo.driver;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Scanner;

import ir.limoo.driver.LimooDriver;
import ir.limoo.driver.entity.Conversation;
import ir.limoo.driver.entity.Message;
import ir.limoo.driver.entity.MessageFile;
import ir.limoo.driver.event_listener.MessageCreatedEventListener;
import ir.limoo.driver.exception.LimooException;

public class SimpleResponder {

	public static void main(String[] args) {
		try {
			new SimpleResponder().limooTest();
		} catch (LimooException e) {
			e.printStackTrace();
		}
	}

	private void limooTest() throws LimooException {
		LimooDriver ld = new LimooDriver("https://web.limoo.im/Limonad", "myWorkspace", "botUsername", "botPassword");

		List<Conversation> conversations = ld.getConversations();
		System.out.println("Number of bot conversations: " + conversations.size());

		Conversation c = ld.getConversationById("conversationId");
		List<Message> unreads = c.getUnreadMessages();
		System.out.println("Number of unread messages in specified conversation: " + unreads.size());
		c.send("Hey there");

		ld.registerEventListener(new MessageCreatedEventListener(c) {

			@Override
			public void onNewMessage(Message msg) {
				System.out.println(msg.getText());

				// Print the contents of the text attachments
				if (msg.getFiles() != null) {
					for (MessageFile messageFile : msg.getFiles()) {
						try (InputStream fileStream = messageFile.download()) {
							try (Scanner sc = new Scanner(fileStream)) {
								while (sc.hasNext())
									System.out.println(sc.nextLine());
							}
						} catch (IOException | LimooException e) {
							e.printStackTrace();
						}
					}
				}

				// Send a file in the response of the message
				if (msg.getThreadRootId() == null) {
					try {
						File file = new File(getClass().getResource("test.txt").toURI());
						c.send(new Message.Builder().text("Message received").threadRootId(msg.getId()).file(file));
					} catch (LimooException | URISyntaxException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
}
