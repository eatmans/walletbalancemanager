package com.eatmans.walletbalancemanager;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.TextView;

import java.lang.reflect.Field;

public class TestDemo implements IXposedHookLoadPackage {
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {

        // Xposed模块自检测
        if (lpparam.packageName.equals("com.eatmans.walletbalancemanager")){
            XposedHelpers.findAndHookMethod("com.eatmans.walletbalancemanager.MainActivity", lpparam.classLoader, "isModuleActive", XC_MethodReplacement.returnConstant(true));
        }
        XposedBridge.log("Loaded app: " + lpparam.packageName);
        Log.d("YOUR_TAG", "Loaded app: " + lpparam.packageName );

        if (lpparam.packageName.equals("com.tencent.mm")) {
            String hookClass = "com.tencent.mm.plugin.wallet.balance.ui.WalletBalanceManagerUI";
            String hookMethodName = "onCreate";

            XposedHelpers.findAndHookMethod(hookClass, lpparam.classLoader, hookMethodName, Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    Object walletBalanceActivity = param.thisObject;
                    Field[] allField = walletBalanceActivity.getClass().getDeclaredFields();
                    for (Field field : allField) {
                        field.setAccessible(true);
                        Object fieldObject = field.get(walletBalanceActivity);
                        if (fieldObject != null && fieldObject instanceof TextView) {
                            TextView textView = (TextView) fieldObject;
                            textView.addTextChangedListener(new TextViewWatcher(textView));
                            XposedBridge.log(field.getName() + ", " + textView.getText().toString());
                        }
                    }
                }
            });
        }
    }



    public class TextViewWatcher implements TextWatcher {
        public static final String LAST_TEXT = "200000.00";
        private TextView textView;

        public TextViewWatcher(TextView textView) {
            this.textView = textView;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();
            XposedBridge.log("textView内容: " + text);
            if (text.contains("¥")) {
                textView.removeTextChangedListener(this);
                textView.setText(LAST_TEXT);
                textView.addTextChangedListener(this);
            }
        }
    }
}