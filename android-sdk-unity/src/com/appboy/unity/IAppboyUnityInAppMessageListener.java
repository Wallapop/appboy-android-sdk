package com.appboy.unity;

import com.appboy.models.IInAppMessage;
import com.appboy.models.MessageButton;
import com.appboy.ui.inappmessage.InAppMessageOperation;

/**
 * The Unity version of {@link com.appboy.ui.inappmessage.listeners.IInAppMessageManagerListener}.
 *
 * If you are implementing Unity, you must use this class instead. In Unity, Braze uses Activities
 * instead of Views to display in-app messages. As a result, the available life-cycle methods and
 * available custom handling are slightly different.
 *
 * Once messages are placed on the internal in-app message stack, Braze fully controls the display
 * and closing of in-app messages. However, it is possible to control which in-app messages are
 * placed on the stack. It is also possible to control navigation once in-app messages are closed.
 *
 * To use, create a class that implements this interface and use
 * {@link AppboyUnityNativeInAppMessageManagerListener#setUnityInAppMessageListener(IAppboyUnityInAppMessageListener)}
 * to set your listener. This should be done in your application or activity onCreate method.
 */
public interface IAppboyUnityInAppMessageListener {

  /**
   * @param inAppMessage the received in-app message.
   *
   * @return boolean flag to indicate to Braze whether the display of this message
   * has been manually handled. If true, Braze will do nothing with the in-app message.
   * If false, Braze will add the message to its internal stack of in-app messages and request
   * display.
   */
  boolean onInAppMessageReceived(IInAppMessage inAppMessage);
  
  /**
   * @param inAppMessage the in-app message that is currently requested for display.
   *
   * @return InAppMessageOperation indicating how to handle the candidate in-app message.
   */
  InAppMessageOperation beforeInAppMessageDisplayed(IInAppMessage inAppMessage);

  /**
   * @param inAppMessage the in-app message that was clicked.
   *
   * @return true to custom handle handle navigation from the in-app message click, false
   * to let Braze handle navigation from the in-app message click (e.g. to deep links, news feed,
   * etc).
   */
  boolean onInAppMessageClicked(IInAppMessage inAppMessage);

  /**
   * @param messageButton the in-app message button that was clicked.
   *
   * @return true to custom handle handle navigation from the in-app message button click, false
   * to let Braze handle navigation from the in-app message button click (e.g. to deep links, news feed,
   * etc).
   */
  boolean onInAppMessageButtonClicked(MessageButton messageButton);
  
  /**
   * @param inAppMessage the in-app message that was closed.
   */
  void onInAppMessageDismissed(IInAppMessage inAppMessage);
}
