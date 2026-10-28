package com.workcollaboration.support;

/**
 * Android resource ids used by the end-to-end tests.
 *
 * <p>Every constant ending in {@code _ID} must match an {@code @+id/...} declaration in
 * {@code app/src/main/res/layout}; {@code LocatorContractTest} enforces this offline so a
 * renamed view breaks the build instead of a device run.
 */
public final class Locators {

    private Locators() {
    }

    public static final String APP_PACKAGE = "com.sheikbro.onlinechat";
    public static final String LAUNCH_ACTIVITY = "com.WorkCollabration.App.MainActivity";

    private static final String P = APP_PACKAGE + ":id/";

    // Login screen (activity_loginpage.xml)
    public static final String EMAIL_ID = P + "editEmail";
    public static final String PASSWORD_ID = P + "editPassword";
    public static final String SIGN_IN_ID = P + "signin";

    // Home screen tab bar (activity_homepage.xml)
    public static final String CONTACTS_TAB_LABEL_ID = P + "contacctt";
    public static final String CHAT_TAB_ICON_ID = P + "chatt";
    public static final String MY_ACCOUNT_TAB_ID = P + "myacc";
    public static final String MY_ACCOUNT_TAB_ICON_ID = P + "myaccc";

    // Contacts fragment (activity_contactspage.xml, contact_list_view_row.xml)
    public static final String CONTACT_LIST_ID = P + "contactlist";
    public static final String CONTACT_ROW_ID = P + "unExtendedLayout";
    public static final String CONTACT_NAME_ID = P + "textUserName";
    public static final String CONTACT_DELETE_ID = P + "imageDelete";

    /**
     * The "add contact" ImageView has no android:id (it uses android:onClick="openAddContacts"),
     * so it is located relative to the contact list, its only id-bearing sibling.
     */
    public static final String ADD_CONTACT_BUTTON_XPATH =
            "//*[@resource-id='" + CONTACT_LIST_ID + "']"
                    + "/preceding-sibling::android.widget.LinearLayout//android.widget.ImageView";

    // Add contact screen (activity_add_contact.xml)
    public static final String NEW_CONTACT_EMAIL_ID = P + "editEmail";
    public static final String NEW_CONTACT_NAME_ID = P + "editName";
    public static final String ADD_CONTACT_SUBMIT_ID = P + "addButton";

    /** Positive button of the platform AlertDialog ("yes, send an invite"). Not an app id. */
    public static final String DIALOG_POSITIVE_BUTTON = "android:id/button1";

    // Conversations list (activity_chatpage.xml, conversations_list_row.xml)
    public static final String CONVERSATION_LIST_ID = P + "conversationList";
    public static final String CONVERSATION_TITLE_ID = P + "textTitle";

    // Chat window (activity_chatwindow.xml, message_list_row.xml)
    public static final String CHAT_TITLE_ID = P + "title";
    public static final String CHAT_INPUT_ID = P + "chatBox";
    public static final String CHAT_SEND_ID = P + "sendButton";
    public static final String CHAT_MESSAGES_ID = P + "chatWindow";
    public static final String MESSAGE_TEXT_ID = P + "textMessage";
    /** Opens the shared "Notes" dialog (despite its id). */
    public static final String NOTES_BUTTON_ID = P + "audio";
    /** Opens the "Reminder" dialog (despite its id). */
    public static final String REMINDER_BUTTON_ID = P + "camera";

    // Notes dialog (custom2.xml)
    public static final String NOTE_INPUT_ID = P + "note";

    // Reminder dialog (custom.xml)
    public static final String REMINDER_NAME_ID = P + "remindername";
    public static final String REMINDER_DAY_ID = P + "day";
    public static final String REMINDER_MONTH_ID = P + "month";
    public static final String REMINDER_YEAR_ID = P + "year";
    public static final String REMINDER_HOUR_ID = P + "hour";
    public static final String REMINDER_MINUTE_ID = P + "minute";

    // Shared by both dialogs (custom.xml, custom2.xml)
    public static final String DIALOG_OK_ID = P + "dialogButtonOK";

    // My account fragment (activity_myaccountpage.xml)
    public static final String ACCOUNT_EMAIL_ID = P + "emailname";
    public static final String LOGOUT_ID = P + "logout";
}
