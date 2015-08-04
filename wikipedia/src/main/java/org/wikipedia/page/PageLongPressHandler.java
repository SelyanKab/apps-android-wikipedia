package org.wikipedia.page;

import org.wikipedia.R;
import org.wikipedia.Site;
import org.wikipedia.Utils;
import org.wikipedia.history.HistoryEntry;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ListView;

public class PageLongPressHandler implements View.OnCreateContextMenuListener,
        MenuItem.OnMenuItemClickListener {
    private final Context context;
    private final ContextMenuListener contextMenuListener;
    private final int historySource;

    private PageTitle title;
    private HistoryEntry entry;

    public PageLongPressHandler(@NonNull Context context,
                                @NonNull View view,
                                int historySource,
                                @NonNull ContextMenuListener listener) {
        this.context = context;
        this.historySource = historySource;
        this.contextMenuListener = listener;
        view.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        title = null;
        if (view instanceof WebView) {
            WebView.HitTestResult result = ((WebView) view).getHitTestResult();
            if (result.getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
                Uri uri = Uri.parse(result.getExtra());
                final String authority = uri.getAuthority();
                if ("wikipedia.org".equals(authority)) {
                    title = ((WebViewContextMenuListener) contextMenuListener).getSite()
                            .titleForInternalLink(uri.getPath());
                }
            }
        } else if (view instanceof ListView) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            title = ((ListViewContextMenuListener) contextMenuListener)
                    .getTitleForListPosition(info.position);
        }

        if (title != null) {
            Utils.hideSoftKeyboard(view);
            entry = new HistoryEntry(title, historySource);
            new MenuInflater(context).inflate(R.menu.menu_page_long_press, menu);
            menu.setHeaderTitle(title.getDisplayText());
            for (int i = 0; i < menu.size(); i++) {
                menu.getItem(i).setOnMenuItemClickListener(this);
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_open_link:
                contextMenuListener.onOpenLink(title, entry);
                return true;
            case R.id.menu_open_in_new_tab:
                contextMenuListener.onOpenInNewTab(title, entry);
                return true;
            default:
                break;
        }
        return false;
    }

    public interface ContextMenuListener {
        void onOpenLink(PageTitle title, HistoryEntry entry);
        void onOpenInNewTab(PageTitle title, HistoryEntry entry);
    }

    public interface ListViewContextMenuListener extends ContextMenuListener {
        PageTitle getTitleForListPosition(int position);
    }

    public interface WebViewContextMenuListener extends ContextMenuListener {
        Site getSite();
    }
}
