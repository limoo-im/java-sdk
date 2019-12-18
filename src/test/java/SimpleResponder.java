import java.util.List;

import ir.limoo.driver.LimooDriver;
import ir.limoo.driver.entity.Conversation;
import ir.limoo.driver.entity.Message;
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
		System.out.println("Number of unread messages: " + unreads.size());
		ld.registerEventListener(new MessageCreatedEventListener(c) {
			
			@Override
			public void onNewMessage(Message msg) {
				System.out.println(msg.getText());
				if (msg.getThreadRootId() == null) {
					try {
						c.sendInThread("Message received", msg.getId());
					} catch (LimooException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
}
