package vn.edu.tdtu.tdtbank.ui.customer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import vn.edu.tdtu.tdtbank.BuildConfig;
import vn.edu.tdtu.tdtbank.domain.VnPayUrlBuilder;
import vn.edu.tdtu.tdtbank.utils.Constants;

import java.util.HashMap;
import java.util.Map;

/** Mở trang thanh toán VNPay sandbox trong WebView và bắt URL trả về để xác minh chữ ký. */
public class VnPayActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "url";
    public static final String EXTRA_SUCCESS = "success";
    public static final String EXTRA_TXN_REF = "txnRef";

    private final VnPayUrlBuilder verifier = new VnPayUrlBuilder(BuildConfig.VNPAY_TMN_CODE,
            BuildConfig.VNPAY_HASH_SECRET, VnPayUrlBuilder.SANDBOX_URL, Constants.VNPAY_RETURN_URL);

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout root = new FrameLayout(this);
        root.setFitsSystemWindows(true);
        WebView web = new WebView(this);
        root.addView(web, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        setContentView(root);

        web.getSettings().setJavaScriptEnabled(true); // trang VNPay yêu cầu JavaScript
        web.getSettings().setAllowFileAccess(false);
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (uri.toString().startsWith(Constants.VNPAY_RETURN_URL)) {
                    finishWith(uri);
                    return true;
                }
                return false;
            }
        });
        String url = getIntent().getStringExtra(EXTRA_URL);
        if (url == null) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        web.loadUrl(url);
    }

    private void finishWith(Uri uri) {
        Map<String, String> params = new HashMap<>();
        for (String name : uri.getQueryParameterNames()) {
            params.put(name, uri.getQueryParameter(name));
        }
        boolean ok = verifier.verifyReturn(params) && VnPayUrlBuilder.isSuccess(params);
        Intent data = new Intent();
        data.putExtra(EXTRA_SUCCESS, ok);
        data.putExtra(EXTRA_TXN_REF, params.get("vnp_TxnRef"));
        setResult(RESULT_OK, data);
        finish();
    }
}
