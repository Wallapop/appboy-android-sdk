package com.appboy.ui.widget;

import android.content.Context;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.appboy.models.cards.BannerImageCard;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.R;
import com.appboy.ui.actions.IAction;
import com.facebook.drawee.view.SimpleDraweeView;

public class BannerImageCardView extends BaseCardView<BannerImageCard> {
  private static final String TAG = AppboyLogger.getAppboyLogTag(BannerImageCardView.class);private ImageView mImage;
  private IAction mCardAction;
  private SimpleDraweeView mDrawee;


  // We set this card's aspect ratio here as a first guess. If the server doesn't send down an
  // aspect ratio, then this value will be the aspect ratio of the card on render.
  private float mAspectRatio = 6f;

  public BannerImageCardView(Context context) {
    super(context);
    init(null);
  }

  public BannerImageCardView(Context context, AttributeSet set) {
    super(context, set);
    init(null);
    this.setRoundingCorners(mDrawee, mContext, getRadius());
  }

    public BannerImageCardView(final Context context, BannerImageCard card) {
        super(context);
        init(card);
    }

    private void init(BannerImageCard card) {
        if (canUseFresco()) {
            mDrawee = (SimpleDraweeView) getProperViewFromInflatedStub(R.id.com_appboy_banner_image_card_drawee_stub);
        } else {
            mImage = (ImageView) getProperViewFromInflatedStub(R.id.com_appboy_banner_image_card_imageview_stub);
            mImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mImage.setAdjustViewBounds(true);
        }

        if (card != null) {
            setCard(card);
        }

        safeSetBackground(getResources().getDrawable(R.drawable.com_appboy_card_background));
        backgroundCorners((LayerDrawable) getResources().getDrawable(R.drawable.com_appboy_card_background));
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.com_appboy_banner_image_card;
    }

  @Override
  public void onSetCard(final BannerImageCard card) {
    boolean respectAspectRatio = false;
    if (card.getAspectRatio() != 0f) {
      mAspectRatio = card.getAspectRatio();
      respectAspectRatio = true;
    }

    if (canUseFresco()) {
        setSimpleDraweeToUrl(mDrawee, card.getImageUrl(), mAspectRatio, respectAspectRatio);
    } else {
        setImageViewToUrl(mImage, card.getImageUrl(), mAspectRatio, respectAspectRatio);
    }

    mCardAction = getUriActionForCard(card);

    setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        // We don't set isRead here (like we do in other card views)
        // because Banner Cards don't have read/unread indicators. They are all images, so there's
        // no free space to put the indicator.
        handleCardClick(mContext,card, mCardAction, TAG, false);
      }
    });
  }
}
