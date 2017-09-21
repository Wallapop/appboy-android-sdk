package com.appboy.ui.widget;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.appboy.models.cards.TextAnnouncementCard;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.R;
import com.appboy.ui.actions.IAction;

public class TextAnnouncementCardView extends BaseCardView<TextAnnouncementCard> {
  private static final String TAG = AppboyLogger.getAppboyLogTag(TextAnnouncementCardView.class);
  private final TextView mTitle;
  private final TextView mDescription;
  private final TextView mDomain;
  private IAction mCardAction;

  public TextAnnouncementCardView(Context context) {
    this(context, null);
  }

  public TextAnnouncementCardView(final Context context, TextAnnouncementCard card) {
    super(context);
    mTitle = (TextView) findViewById(R.id.com_appboy_text_announcement_card_title);
    mDescription = (TextView) findViewById(R.id.com_appboy_text_announcement_card_description);
    mDomain = (TextView) findViewById(R.id.com_appboy_text_announcement_card_domain);

    if (card != null) {
      setCard(card);
    }

    safeSetBackground(getResources().getDrawable(R.drawable.com_appboy_card_background));
  }

  @Override
  protected int getLayoutResource() {
    return R.layout.com_appboy_text_announcement_card;
  }

  @Override
  public void onSetCard(final TextAnnouncementCard card) {
    mTitle.setText(card.getTitle());
    mDescription.setText(card.getDescription());
    setOptionalTextView(mDomain, card.getDomain());
    mCardAction = getUriActionForCard(card);

    setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        handleCardClick(mContext, card, mCardAction, TAG);
      }
    });
  }
}
