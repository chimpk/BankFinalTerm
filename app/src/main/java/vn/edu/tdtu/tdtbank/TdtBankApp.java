package vn.edu.tdtu.tdtbank;

import android.app.Application;
import android.content.Context;

import org.osmdroid.config.Configuration;

import java.io.File;

public class TdtBankApp extends Application {

    private static TdtBankApp instance;

    public static Context get() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        // osmdroid: user-agent bắt buộc theo chính sách OSM; cache trong thư mục riêng của app (không cần quyền lưu trữ).
        Configuration cfg = Configuration.getInstance();
        cfg.setUserAgentValue(getPackageName());
        cfg.setOsmdroidBasePath(new File(getCacheDir(), "osmdroid"));
        cfg.setOsmdroidTileCache(new File(getCacheDir(), "osmdroid/tiles"));
    }
}
