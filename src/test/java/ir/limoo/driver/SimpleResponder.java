package ir.limoo.driver;

import ir.limoo.driver.entity.Conversation;
import ir.limoo.driver.entity.Message;
import ir.limoo.driver.entity.MessageFile;
import ir.limoo.driver.entity.Workspace;
import ir.limoo.driver.event.MessageCreatedEventListener;
import ir.limoo.driver.exception.LimooException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Scanner;

public class SimpleResponder {

    public static void main(String[] args) {
        try {
            new SimpleResponder().limooTest();
        } catch (LimooException e) {
            e.printStackTrace();
        }
    }

    private void limooTest() throws LimooException {
        LimooDriver ld = new LimooDriver("https://web.limoo.im/Limonad", "botUsername", "botPassword");

        Workspace workspace = ld.getWorkspaceByKey("myWorkspace");
        List<Conversation> conversations = workspace.getConversations();
        System.out.println("Number of bot conversations: " + conversations.size());

        Conversation c = workspace.getConversationById("conversationId");
        List<Message> unreads = c.getUnreadMessages();
        System.out.println("Number of unread messages in specified conversation: " + unreads.size());
        c.send("Hey there");

        ld.addEventListener(new MessageCreatedEventListener() {
            @Override
            public void onNewMessage(Message message, Conversation conversation) {
                System.out.println(message.getText());

                // Print the contents of the text attachments
                if (message.getFileInfos() != null) {
                    for (MessageFile messageFile : message.getFileInfos()) {
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
                if (message.getThreadRootId() == null) {
                    try {
                        File file = new File(getClass().getResource("test.txt").toURI());
                        message.sendInThread(new Message.Builder().text("Message received").file(file));
                    } catch (LimooException | URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
