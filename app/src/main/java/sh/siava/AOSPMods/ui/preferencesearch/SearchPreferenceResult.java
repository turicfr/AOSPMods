package sh.siava.AOSPMods.ui.preferencesearch;

/*
 * https://github.com/ByteHamster/SearchPreference
 */

import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;


public class SearchPreferenceResult {
	private final String key;
	private final int file;
	private final String screen;

	SearchPreferenceResult(String key, int file, String screen) {
		this.key = key;
		this.file = file;
		this.screen = screen;
	}

	/**
	 * Returns the key of the preference pressed
	 *
	 * @return The key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Returns the file in which the result was found
	 *
	 * @return The file in which the result was found
	 */
	public int getResourceFile() {
		return file;
	}

	/**
	 * Returns the screen in which the result was found
	 *
	 * @return The screen in which the result was found
	 */
	public String getScreen() {
		return screen;
	}

	/**
	 * Highlight the preference that was found
	 *
	 * @param prefsFragment Fragment that contains the preference
	 */
	@SuppressWarnings("unused")
	public void highlight(final PreferenceFragmentCompat prefsFragment) {
		new Handler(Looper.getMainLooper()).post(() -> doHighlight(prefsFragment, getKey()));
	}

	public static void highlight(final PreferenceFragmentCompat prefsFragment, final String key) {
		new Handler(Looper.getMainLooper()).post(() -> doHighlight(prefsFragment, key));
	}

	private static void doHighlight(final PreferenceFragmentCompat prefsFragment, final String key) {
		final Preference prefResult = prefsFragment.findPreference(key);

		if (prefResult == null) {
			Log.e("doHighlight", "Preference not found on given screen");
			return;
		}

		final RecyclerView recyclerView = prefsFragment.getListView();
		final RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();

		if (adapter instanceof PreferenceGroup.PreferencePositionCallback) {
			PreferenceGroup.PreferencePositionCallback callback = (PreferenceGroup.PreferencePositionCallback) adapter;
			final int position = callback.getPreferenceAdapterPosition(prefResult);

			if (position != RecyclerView.NO_POSITION) {
				recyclerView.scrollToPosition(position);
				recyclerView.postDelayed(() -> {
					RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
					if (holder != null) {
						Drawable background = holder.itemView.getBackground();
						if (background instanceof RippleDrawable) {
							forceRippleAnimation((RippleDrawable) background);
							return;
						}
					}
					highlightFallback(prefsFragment, prefResult);
				}, 200);
				return;
			}
		}

		highlightFallback(prefsFragment, prefResult);
	}

	/**
	 * Alternative (old) highlight method if ripple does not work
	 */
	private static void highlightFallback(PreferenceFragmentCompat prefsFragment, final Preference prefResult) {
		prefsFragment.scrollToPreference(prefResult);
		prefResult.setTitle(prefResult.getTitle() + " \uD83D\uDD0D");
		new Handler(Looper.getMainLooper()).postDelayed(() -> prefResult.setTitle(Objects.requireNonNull(prefResult.getTitle()).toString().replace(" \uD83D\uDD0D", "")), 3000);
	}

	protected static void forceRippleAnimation(RippleDrawable background) {
		final RippleDrawable rippleDrawable = background;
		rippleDrawable.setState(new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled});
		new Handler(Looper.getMainLooper()).postDelayed(() -> rippleDrawable.setState(new int[]{}), 300);
	}

	/**
	 * Closes the search results page
	 *
	 * @param activity The current activity
	 */
	@SuppressWarnings("unused")
	public void closeSearchPage(AppCompatActivity activity) {
		FragmentManager fm = activity.getSupportFragmentManager();
		fm.beginTransaction().remove(Objects.requireNonNull(fm.findFragmentByTag(SearchPreferenceFragment.TAG))).commit();
	}
}
