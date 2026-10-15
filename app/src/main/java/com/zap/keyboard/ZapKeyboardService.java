package com.zap.keyboard;

import android.inputmethodservice.InputMethodService;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

public class ZapKeyboardService extends InputMethodService {

    private View keyboardView;
    private LinearLayout templatesContainer;

    @Override
    public View onCreateInputView() {
        this.keyboardView = getLayoutInflater().inflate(R.layout.keyboard, null);
        this.templatesContainer = keyboardView.findViewById(R.id.templatesContainer);
        refreshTemplates();
        buildKeys();
        return keyboardView;
    }

    @Override
    public void onStartInputView(android.view.inputmethod.EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        refreshTemplates();
    }

    /** Перестроить список шаблонов Zap. */
    private void refreshTemplates() {
        if (templatesContainer == null) return;
        templatesContainer.removeAllViews();

        List<ZapManager.ZapItem> items = ZapManager.getAll(this);
        if (items.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Нет шаблонов.\nДобавьте в приложении ZapKeyboard");
            empty.setTextColor(0xFFA78BFA);
            empty.setTextSize(13f);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(20, 30, 20, 30);
            templatesContainer.addView(empty);
            return;
        }

        for (final ZapManager.ZapItem item : items) {
            Button btn = new Button(this);
            btn.setText(item.name);
            btn.setAllCaps(false);
            btn.setTextColor(0xFFFFFFFF);
            btn.setTextSize(14f);
            btn.setBackgroundResource(R.drawable.key_bg);
            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(-1, dpToPx(44));
            lp.bottomMargin = dpToPx(6);
            btn.setLayoutParams(lp);
            btn.setOnClickListener(v -> insertZap(item.text));
            templatesContainer.addView(btn);
        }
    }

    /** Вставить текст шаблона в поле ввода. */
    private void insertZap(String text) {
        if (text == null) text = "";
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ic.commitText(text, 1);
        }
    }

    /** Обычные клавиши клавиатуры: QWERTY + цифры + пробел + backspace + enter. */
    private void buildKeys() {
        LinearLayout keysContainer = keyboardView.findViewById(R.id.keysContainer);
        if (keysContainer == null) return;
        keysContainer.removeAllViews();

        String[][] rows = {
                {"q","w","e","r","t","y","u","i","o","p"},
                {"a","s","d","f","g","h","j","k","l"},
                {"⇧","z","x","c","v","b","n","m","⌫"},
                {"123"," ","↵"}
        };

        for (String[] row : rows) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowLp =
                    new LinearLayout.LayoutParams(-1, dpToPx(48));
            rowLayout.setLayoutParams(rowLp);

            for (String key : row) {
                Button btn = new Button(this);
                btn.setText(key);
                btn.setAllCaps(false);
                btn.setTextColor(0xFFFFFFFF);
                btn.setTextSize(16f);
                btn.setBackgroundResource(R.drawable.key_bg);
                LinearLayout.LayoutParams kLp;
                if (key.equals(" ")) kLp = new LinearLayout.LayoutParams(0, -1, 5f);
                else if (key.equals("⇧") || key.equals("⌫")) kLp = new LinearLayout.LayoutParams(0, -1, 1.5f);
                else kLp = new LinearLayout.LayoutParams(0, -1, 1f);
                kLp.setMargins(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2));
                btn.setLayoutParams(kLp);
                btn.setOnClickListener(v -> handleKey(key));
                rowLayout.addView(btn);
            }
            keysContainer.addView(rowLayout);
        }
    }

    private void handleKey(String key) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;

        switch (key) {
            case "⌫":
                ic.deleteSurroundingText(1, 0);
                break;
            case "↵":
                ic.sendKeyEvent(new android.view.KeyEvent(
                        android.view.KeyEvent.ACTION_DOWN,
                        android.view.KeyEvent.KEYCODE_ENTER));
                ic.sendKeyEvent(new android.view.KeyEvent(
                        android.view.KeyEvent.ACTION_UP,
                        android.view.KeyEvent.KEYCODE_ENTER));
                break;
            case " ":
                ic.commitText(" ", 1);
                break;
            case "⇧":
            case "123":
                // Пока не реализовано — оставим как заглушки
                break;
            default:
                ic.commitText(key, 1);
        }
    }

    private int dpToPx(float dp) {
        return (int) android.util.TypedValue.applyDimension(
                android.util.TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }
          }
