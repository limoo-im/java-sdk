import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
		LimooDriver ld = new LimooDriver("https://alpha.limonadapp.ir/Limonad", "limoo", "limoo_bot", "limoo_bot");
		Conversation c = ld.getConversationById("95d21dd1-be99-4611-ad3f-b49decc3d3e5");
		List<Message> unreads = c.getUnreadMessages();
		System.out.println(unreads.size());
		ld.registerEventListener(new MessageCreatedEventListener(c) {

			@Override
			public void onNewMessage(Message msg) {
				System.out.println(msg.getText());
				if (msg.getThreadRootId() == null) {
					for (MessageFile messageFile : msg.getFiles()) {
						try (InputStream fileStream = messageFile.download()) {
							try (Scanner sc = new Scanner(fileStream)) {
								while (sc.hasNext())
									System.out.println(sc.nextLine());
							}
						} catch (IOException e) {
							e.printStackTrace();
						} catch (LimooException e) {
							e.printStackTrace();
						}
					}
					try {
						File file = new File("/home/meprime/bull.txt");
						c.send(new Message.Builder().text("Message received").threadRootId(msg.getId()).file(file));
					} catch (LimooException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
}
