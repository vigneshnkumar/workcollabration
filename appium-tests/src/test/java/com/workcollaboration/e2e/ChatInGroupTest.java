package com.workcollaboration.e2e;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.workcollaboration.support.Locators;
import com.workcollaboration.support.TestConfig;

/** Sends a message in a group conversation (set -Dchat.group to pick one). */
class ChatInGroupTest extends BaseAppiumTest {

    @Test
    void sentMessageAppearsInConversation() {
        login();
        openConversation(TestConfig.groupConversation());

        String message = TestConfig.chatMessage();
        type(Locators.CHAT_INPUT_ID, message);
        tapId(Locators.CHAT_SEND_ID);

        assertTrue(waitForText(Locators.MESSAGE_TEXT_ID, message),
                "Sent message not shown in the chat window");
    }
}
