package com.appboy.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import com.appboy.Appboy;
import com.appboy.configuration.AppboyConfigurationProvider;
import com.appboy.enums.AppboyViewBounds;
import com.appboy.enums.Channel;
import com.appboy.models.cards.Card;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.AppboyNavigator;
import com.appboy.ui.R;
import com.appboy.ui.actions.ActionFactory;
import com.appboy.ui.actions.IAction;
import com.appboy.ui.actions.UriAction;
import com.appboy.ui.feed.AppboyFeedManager;
import com.appboy.ui.feed.AppboyImageSwitcher;
import com.appboy.ui.support.FrescoLibraryUtils;
import com.facebook.drawee.generic.RoundingParams;
import com.appboy.ui.support.ViewUtils;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.Observable;
import java.util.Observer;

/**
 * Base class for Braze feed card views
 */
public abstract class BaseCardView<T extends Card> extends RelativeLayout implements Observer {
    private static final String TAG = AppboyLogger.getAppboyLogTag(BaseCardView.class);
    private static final int TYPE_FACE_GENERIC_INDEX = 0;
    private static final int TYPE_FACE_TITLE_INDEX = 1;
    private static final int TYPE_FACE_MESSAGE_INDEX = 2;
    private static Boolean unreadCardVisualIndicatorOn;
    private static final float SQUARE_ASPECT_RATIO = 1f;
    private static final String COM_APPBOY_NEWSFEED_UNREAD_VISUAL_INDICATOR_ON = "com_appboy_newsfeed_unread_visual_indicator_on";

    protected final Context mContext;
    private Drawable iconUnreadDrawable;
    private Drawable iconReadDrawable;
    private String[] mTypeFaces;
    private String mTypeFaceReference;
    private float mRadius;
    protected T mCard;
    protected AppboyImageSwitcher mImageSwitcher;
    protected boolean mCanUseFresco;


    public String getTitleTypeFaceReference() {
        return (mTypeFaces[TYPE_FACE_TITLE_INDEX] != null && !"".equals(mTypeFaces[TYPE_FACE_TITLE_INDEX])) ?
                mTypeFaces[TYPE_FACE_TITLE_INDEX] :
                mTypeFaces[TYPE_FACE_GENERIC_INDEX];
    }

    public String getMessageTypeFaceReference() {
        return (mTypeFaces[TYPE_FACE_MESSAGE_INDEX] != null && !"".equals(mTypeFaces[TYPE_FACE_MESSAGE_INDEX])) ?
                mTypeFaces[TYPE_FACE_MESSAGE_INDEX] :
                mTypeFaces[TYPE_FACE_GENERIC_INDEX];
    }

    public BaseCardView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mTypeFaces = new String[3];
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.com_appboy_ui_widget_CardView, 0, 0);
            try {
                mRadius = a.getDimension(R.styleable.com_appboy_ui_widget_CardView_appboyCardViewRoundedCorners, 0);
                mTypeFaces[TYPE_FACE_GENERIC_INDEX] = a.getString(R.styleable.com_appboy_ui_widget_CardView_appboyCardViewCustomFont);
                mTypeFaces[TYPE_FACE_TITLE_INDEX] = a.getString(R.styleable.com_appboy_ui_widget_CardView_appboyCardViewCustomFontTitle);
                mTypeFaces[TYPE_FACE_MESSAGE_INDEX] = a.getString(R.styleable.com_appboy_ui_widget_CardView_appboyCardViewCustomFontMessage);

                iconReadDrawable = a.getDrawable(R.styleable.com_appboy_ui_widget_CardView_appboyCardViewIconReadDrawable);
                iconUnreadDrawable = a.getDrawable(R.styleable.com_appboy_ui_widget_CardView_appboyCardViewIconUnreadDrawable);
            } finally {
                a.recycle();
            }
        }
        init(context);
    }

    public BaseCardView(Context context) {
        super(context);
        mContext = context;
        init(context);
    }

    private void init(Context context) {
        // Note: this must be called before we inflate any views.
        mCanUseFresco = FrescoLibraryUtils.canUseFresco(mContext);


        inflate(mContext, getLayoutResource(), this);

        // All implementing views of BaseCardView must include this switcher view in order to have the
        // read/unread functionality. Views that don't have the indicator (like banner views) won't have the image switcher
        // in them and thus we do the null-check below.
        mImageSwitcher = (AppboyImageSwitcher) findViewById(R.id.com_appboy_newsfeed_item_read_indicator_image_switcher);
        if (mImageSwitcher != null) {
          mImageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
              return new ImageView(mContext.getApplicationContext());
            }
          });
        }

        // If the visual indicator on cards shouldn't be on, due to the xml setting in appboy.xml, then set the
        // imageSwitcher to GONE to hide the indicator UI.
        // Read the setting from the appboy.xml if we don't already have a value.
        if (unreadCardVisualIndicatorOn == null) {
          AppboyConfigurationProvider configurationProvider = new AppboyConfigurationProvider(context);
          unreadCardVisualIndicatorOn = configurationProvider.getIsNewsfeedVisualIndicatorOn();
        }

        // If the setting is false, then hide the indicator.
        if (!unreadCardVisualIndicatorOn) {
            if (mImageSwitcher != null) {
                mImageSwitcher.setVisibility(GONE);
            }
        }
    }


    /**
     * This method is called when the setRead() method is called on the internal Card object.
     */
    @Override
    public void update(Observable observable, Object data) {
        setCardViewedIndicator();
    }

  /**
   * Checks to see if the card object is viewed and if so, sets the read/unread status
   * indicator image. If the card is null, does nothing.
   */
  private void setCardViewedIndicator() {
    if (getCard() != null) {
      if (mImageSwitcher != null) {
        AppboyLogger.v(TAG, "Setting the read/unread indicator for the card.");
        if (getCard().isRead()) {
          if (mImageSwitcher.getReadIcon() != null) {
            mImageSwitcher.setImageDrawable(mImageSwitcher.getReadIcon());
          } else {
            mImageSwitcher.setImageResource(R.drawable.icon_read);
          }
          mImageSwitcher.setTag("icon_read");
        } else {
          if (mImageSwitcher.getUnReadIcon() != null) {
            mImageSwitcher.setImageDrawable(mImageSwitcher.getUnReadIcon());
            return;
          } else {
            mImageSwitcher.setImageResource(R.drawable.icon_unread);
          }
          mImageSwitcher.setTag("icon_unread");
        }
      }
    } else {
      AppboyLogger.d(TAG, "The card is null.");
    }
  }

    protected abstract int getLayoutResource();

    public void setCard(final T card) {
        mCard = card;
        onSetCard(card);
        // Register as an observer to the card class
        card.addObserver(this);
        setCardViewedIndicator();
    }

    protected abstract void onSetCard(T card);

    public Card getCard() {
        return mCard;
    }

  void setOptionalTextView(TextView view, String value) {
    if (value != null && !value.trim().equals("")) {
      view.setText(value);
      view.setVisibility(VISIBLE);
    } else {
      view.setText("");
      view.setVisibility(GONE);
    }
  }

    void safeSetBackground(Drawable background) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            setBackgroundDrawable(background);
        } else {
            setBackgroundNew(background);
        }
    }

    @TargetApi(16)
    private void setBackgroundNew(Drawable background) {
        setBackground(background);
    }

    /**
     * Calls setImageViewToUrl with aspect ratio set to 1f and respectAspectRatio set to false.
     *
     * @see com.appboy.ui.widget.BaseCardView#setImageViewToUrl(android.widget.ImageView, String, float, boolean)
     */
    void setImageViewToUrl(final ImageView imageView, final String imageUrl) {
        setImageViewToUrl(imageView, imageUrl, 1f, false);
    }

  /**
   * Calls setImageViewToUrl with respectAspectRatio set to true.
   * @see com.appboy.ui.widget.BaseCardView#setImageViewToUrl(android.widget.ImageView, String, float, boolean)
   */
  void setImageViewToUrl(final ImageView imageView, final String imageUrl, final float aspectRatio) {
    setImageViewToUrl(imageView, imageUrl, aspectRatio, true);
  }

    /**
     * Asynchronously fetches the image at the given imageUrl and displays the image in the ImageView. No image will be
     * displayed if the image cannot be downloaded or fetched from the cache.
     *
     * @param imageView          the ImageView in which to display the image
     * @param imageUrl           the URL of the image resource
     * @param aspectRatio        the desired aspect ratio of the image. This should match what's being sent down from the dashboard.
     * @param respectAspectRatio whether to use aspectRatio as the final aspect ratio of the imageView. When set to false,
     *                           the aspect ratio of the imageView will match that of the downloaded image. When set to true,
     *                           the provided aspect ratio will match aspectRatio, regardless of the actual dimensions of the
     *                           downloaded image.
     */
    void setImageViewToUrl(final ImageView imageView, final String imageUrl, final float aspectRatio, final boolean respectAspectRatio) {
        if (imageUrl == null) {
            AppboyLogger.w(TAG, "The image url to render is null. Not setting the card image.");
            return;
        }

    if (aspectRatio == 0) {
      AppboyLogger.w(TAG, "The image aspect ratio is 0. Not setting the card image.");
      return;
    }

    if (!imageUrl.equals(imageView.getTag(R.string.com_appboy_image_resize_tag_key))) {
      if (aspectRatio != SQUARE_ASPECT_RATIO) {
        // We need to set layout params on the imageView once its layout state is visible. To do this,
        // we obtain the imageView's observer and attach a listener on it for when the view's layout
        // occurs. At layout time, we set the imageView's size params based on the aspect ratio
        // for our card. Note that after the card's first layout, we don't want redundant resizing
        // so we remove our listener after the resizing.
        ViewTreeObserver viewTreeObserver = imageView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
          viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
              int width = imageView.getWidth();
              imageView.setLayoutParams(new LayoutParams(width, (int) (width / aspectRatio)));
              ViewUtils.removeOnGlobalLayoutListenerSafe(imageView.getViewTreeObserver(), this);
            }
          });
        }
      }

            imageView.setImageResource(android.R.color.transparent);
            Appboy.getInstance(getContext()).getAppboyImageLoader().renderUrlIntoView(getContext(), imageUrl, imageView, AppboyViewBounds.BASE_CARD_VIEW);
            imageView.setTag(R.string.com_appboy_image_resize_tag_key, imageUrl);
        }
    }

  /**
   * Loads an image via url for display in a SimpleDraweeView using the Facebook Fresco library.
   * By default, gif urls are set to autoplay and tap to retry is on for all images.
   * @param simpleDraweeView the fresco SimpleDraweeView in which to display the image
   * @param imageUrl the URL of the image resource
   */
  void setSimpleDraweeToUrl(final SimpleDraweeView simpleDraweeView, final String imageUrl, final float aspectRatio, final boolean respectAspectRatio) {
    if (imageUrl == null) {
      AppboyLogger.w(TAG, "The image url to render is null. Not setting the card image.");
      return;
    }

        FrescoLibraryUtils.setDraweeControllerHelper(simpleDraweeView, imageUrl, aspectRatio, respectAspectRatio);
    }

    /**
     * Returns whether we can use the Fresco Library for newsfeed cards.
     */
    boolean canUseFresco() {
        return mCanUseFresco;
    }

  protected static void handleCardClick(Context context, Card card, IAction cardAction, String tag) {
    handleCardClick(context, card, cardAction, tag, true);
  }

  /**
   * All card views should handle new feed card clicks through this method
   */
  protected static void handleCardClick(Context context, Card card, IAction cardAction, String tag, boolean markAsRead) {
    if (markAsRead) {
      card.setIsRead(true);
    }
    if (cardAction != null) {
      if (card.logClick()) {
        AppboyLogger.d(tag, "Logged click for card " + card.getId());
      } else {
        AppboyLogger.d(tag, "Logging click failed for card " + card.getId());
      }
      if (!AppboyFeedManager.getInstance().getFeedCardClickActionListener().onFeedCardClicked(context, card, cardAction)) {
        if (cardAction instanceof UriAction) {
          AppboyNavigator.getAppboyNavigator().gotoUri(context, (UriAction) cardAction);
        } else {
          // Some other action received, execute directly.
          cardAction.execute(context);
        }
      }
    }
  }

  protected static UriAction getUriActionForCard(Card card) {
    Bundle extras = new Bundle();
    for (String key : card.getExtras().keySet()) {
      extras.putString(key, card.getExtras().get(key));
    }
    return ActionFactory.createUriActionFromUrlString(card.getUrl(), extras, card.getOpenUriInWebView(), Channel.NEWS_FEED);
  }

    /**
     * Gets the view to display the correct card image after checking if it can use Fresco.
     *
     * @param stubLayoutId The resource Id of the stub for inflation as returned by findViewById.
     * @return the view to display the image. This will either be an ImageView or DraweeView
     */
    View getProperViewFromInflatedStub(int stubLayoutId) {
        ViewStub imageStub = (ViewStub) findViewById(stubLayoutId);
        imageStub.inflate();

        if (mCanUseFresco) {
            return findViewById(R.id.com_appboy_stubbed_feed_drawee_view);
        } else {
            return findViewById(R.id.com_appboy_stubbed_feed_image_view);
        }
    }

    /**
     *
     * @return float radius of the background corners of the card
     */
    protected float getRadius() {
        return mRadius;
    }

    /**
     * Round corners of the background, if it is a {@link LayerDrawable}.
     *
     * @param layers The LayerDrawable of the background
     */
    protected void backgroundCorners(LayerDrawable layers) {
        for (int i = 0; i < layers.getNumberOfLayers(); i++) {
            Drawable item = layers.getDrawable(i);
            if (item instanceof GradientDrawable) {
                ((GradientDrawable) item).setCornerRadius(getRadius());
            }
        }
    }

    /**
     * have file extension .ttf
     *
     * @param typeFace key representing a font stored in assets
     * @return The typeFace key with the suffix .ttf
     */
    protected String ensureTypeFaceSuffix(String typeFace) {
        if (!typeFace.endsWith(".ttf"))
            typeFace += ".ttf";

        return typeFace;
    }

    public void setRoundingCorners(final SimpleDraweeView mDrawee, final Context context, final float radius) {
        if (mDrawee == null) return;
        setRoundingCorners(mDrawee, context, radius, radius, radius, radius);
    }

    public void setRoundingCorners(final SimpleDraweeView mDrawee, final Context context, final float topLeftRadius,
                                          final float topRightRadius, final float bottomLeftRadius, final float bootmRightRadius) {
        if (mDrawee == null) return;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        RoundingParams r = RoundingParams.fromCornersRadii(topLeftRadius, topRightRadius, bottomLeftRadius, bootmRightRadius);
        mDrawee.getHierarchy().setRoundingParams(r);
    }
}
