package com.zap.keyboard;

import android.inputmethodservice.InputMethodService;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

public class ZapKeyboardService extends InputMethodService {

    private View keyboardView;
    private LinearLayout contentContainer;

    @Override
    public View onCreateInputView() {
        this.keyboardView = getLayoutInflater().inflate(R.layout.keyboard, null);
        this.contentContainer = keyboardView.findViewById(R.id.contentContainer);
        showKeyboardRoot();
        return keyboardView;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        showKeyboardRoot();
    }

    // ==================== ЭКРАН 1: КОРЕНЬ (ZAP) ====================

    private void showKeyboardRoot() {
        if (contentContainer == null) return;
        contentContainer.removeAllViews();

        // Заголовок
        TextView title = new TextView(this);
        title.setText("⚡ Zap");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(18f);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dpToPx(14), 0, dpToPx(14));
        contentContainer.addView(title);

        // Кнопка "Открыть шаблоны"
        Button btnOpen = new Button(this);
        btnOpen.setText("📂 Открыть шаблоны");
        btnOpen.setAllCaps(false);
        btnOpen.setTextColor(0xFFFFFFFF);
        btnOpen.setTextSize(16f);
        btnOpen.setBackgroundResource(R.drawable.key_bg);
        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(-1, dpToPx(56));
        lp.setMargins(dpToPx(10), 0, dpToPx(10), dpToPx(10));
        btnOpen.setLayoutParams(lp);
        btnOpen.setOnClickListener(v -> showTemplatesList());
        contentContainer.addView(btnOpen);

        // Подсказка
        TextView hint = new TextView(this);
        hint.setText("Нажмите на шаблон, потом на строку — она вставится в чат");
        hint.setTextColor(0xFFA78BFA);
        hint.setTextSize(12f);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20));
        contentContainer.addView(hint);

        // Обычные клавиши (опционально, простые: пробел + backspace + enter)
        LinearLayout keysRow = new LinearLayout(this);
        keysRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams krLp =
                new LinearLayout.LayoutParams(-1, dpToPx(48));
        krLp.setMargins(dpToPx(6), dpToPx(10), dpToPx(6), dpToPx(6));
        keysRow.setLayoutParams(krLp);

        Button btnSpace = new Button(this);
        btnSpace.setText("Пробел");
        btnSpace.setAllCaps(false);
        btnSpace.setTextColor(0xFFFFFFFF);
        btnSpace.setTextSize(14f);
        btnSpace.setBackgroundResource(R.drawable.key_bg);
        btnSpace.setLayoutParams(new LinearLayout.LayoutParams(0, -1, 4f));
        btnSpace.setOnClickListener(v -> {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) ic.commitText(" ", 1);
        });
        keysRow.addView(btnSpace);

        Button btnBack = new Button(this);
        btnBack.setText("⌫");
        btnBack.setAllCaps(false);
        btnBack.setTextColor(0xFFFFFFFF);
        btnBack.setTextSize(18f);
        btnBack.setBackgroundResource(R.drawable.key_bg);
        LinearLayout.LayoutParams bLp =
                new LinearLayout.LayoutParams(0, -1, 1f);
        bLp.leftMargin = dpToPx(4);
        btnBack.setLayoutParams(bLp);
        btnBack.setOnClickListener(v -> {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) ic.deleteSurroundingText(1, 0);
        });
        keysRow.addView(btnBack);

        Button btnEnter = new Button(this);
        btnEnter.setText("↵");
        btnEnter.setAllCaps(false);
        btnEnter.setTextColor(0xFFFFFFFF);
        btnEnter.setTextSize(18f);
        btnEnter.setBackgroundResource(R.drawable.key_bg);
        LinearLayout.LayoutParams eLp =
                new LinearLayout.LayoutParams(0, -1, 1.5f);
        eLp.leftMargin = dpToPx(4);
        btnEnter.setLayoutParams(eLp);
        btnEnter.setOnClickListener(v -> {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.sendKeyEvent(new android.view.KeyEvent(
                        android.view.KeyEvent.ACTION_DOWN,
                        android.view.KeyEvent.KEYCODE_ENTER));
                ic.sendKeyEvent(new android.view.KeyEvent(
                        android.view.KeyEvent.ACTION_UP,
                        android.view.KeyEvent.KEYCODE_ENTER));
            }
        });
        keysRow.addView(btnEnter);

        contentContainer.addView(keysRow);
    }

    // ==================== ЭКРАН 2: СПИСОК ШАБЛОНОВ ====================

    private void showTemplatesList() {
        if (contentContainer == null) return;
        contentContainer.removeAllViews();

        // Заголовок
        TextView title = new TextView(this);
        title.setText("📂 Шаблоны");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(16f);
        title.setTypeface(null, android.util.TypedValue.COMPLEX_UNIT_DIP == 0 ?
                null : android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dpToPx(10), 0, dpToPx(10));
        contentContainer.addView(title);

        List<ZapManager.ZapTemplate> templates = ZapManager.getAll(this);

        if (templates.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Нет шаблонов.\nДобавьте их в приложении ZapKeyboard");
            empty.setTextColor(0xFFA78BFA);
            empty.setTextSize(13f);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(dpToPx(20), dpToPx(40), dpToPx(20), dpToPx(40));
            contentContainer.addView(empty);
        } else {
            ScrollView scroll = new ScrollView(this);
            scroll.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1f));
            LinearLayout list = new LinearLayout(this);
            list.setOrientation(LinearLayout.VERTICAL);
            list.setPadding(dpToPx(8), 0, dpToPx(8), dpToPx(8));
            scroll.addView(list);

            for (final ZapManager.ZapTemplate tpl : templates) {
                Button btn = new Button(this);
                btn.setText("📄 " + tpl.name
                        + "  (" + tpl.lines.size() + ")");
                btn.setAllCaps(false);
                btn.setTextColor(0xFFFFFFFF);
                btn.setTextSize(14f);
                btn.setBackgroundResource(R.drawable.key_bg);
                LinearLayout.LayoutParams lp =
                        new LinearLayout.LayoutParams(-1, dpToPx(48));
                lp.bottomMargin = dpToPx(6);
                btn.setLayoutParams(lp);
                btn.setOnClickListener(v -> showLinesList(tpl.name));
                list.addView(btn);
            }

            contentContainer.addView(scroll);
        }

        // Назад
        Button btnBack = new Button(this);
        btnBack.setText("← Назад");
        btnBack.setAllCaps(false);
        btnBack.setTextColor(0xFFFFFFFF);
        btnBack.setTextSize(14f);
        btnBack.setBackgroundResource(R.drawable.key_bg);
        LinearLayout.LayoutParams bLp =
                new LinearLayout.LayoutParams(-1, dpToPx(44));
        bLp.setMargins(dpToPx(8), dpToPx(6), dpToPx(8), dpToPx(6));
        btnBack.setLayoutParams(bLp);
        btnBack.setOnClickListener(v -> showKeyboardRoot());
        contentContainer.addView(btnBack);
    }

    // ==================== ЭКРАН 3: СПИСОК СТРОК ====================

    private void showLinesList(final String templateName) {
        if (contentContainer == null) return;
        contentContainer.removeAllViews();

        // Заголовок
        TextView title = new TextView(this);
        title.setText("📄 " + templateName);
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(16f);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dpToPx(10), 0, dpToPx(10));
        contentContainer.addView(title);

        ZapManager.ZapTemplate tpl = ZapManager.findByName(this, templateName);

        if (tpl == null || tpl.lines.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Нет строк в этом шаблоне");
            empty.setTextColor(0xFFA78BFA);
            empty.setTextSize(13f);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(dpToPx(20), dpToPx(40), dpToPx(20), dpToPx(40));
            contentContainer.addView(empty);
        } else {
            ScrollView scroll = new ScrollView(this);
            scroll.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1f));
            LinearLayout list = new LinearLayout(this);
            list.setOrientation(LinearLayout.VERTICAL);
            list.setPadding(dpToPx(8), 0, dpToPx(8), dpToPx(8));
            scroll.addView(list);

            for (int i = 0; i < tpl.lines.size(); i++) {
                final String line = tpl.lines.get(i);

                Button btn = new Button(this);
                String preview = line.length() > 60
                        ? line.substring(0, 60) + "..."
                        : line;
                btn.setText((i + 1) + ". " + preview);
                btn.setAllCaps(false);
                btn.setTextColor(0xFFFFFFFF);
                btn.setTextSize(13f);
                btn.setBackgroundResource(R.drawable.key_bg);
                LinearLayout.LayoutParams lp =
                        new LinearLayout.LayoutParams(-1, dpToPx(50));
                lp.bottomMargin = dpToPx(6);
                btn.setLayoutParams(lp);
                btn.setOnClickListener(v -> {
                    InputConnection ic = getCurrentInputConnection();
                    if (ic != null) {
                        ic.commitText(line, 1);
                    }
                });
                list.addView(btn);
            }

            contentContainer.addView(scroll);
        }

        // Назад к шаблонам
        Button btnBack = new Button(this);
        btnBack.setText("← Назад к шаблонам");
        btnBack.setAllCaps(false);
        btnBack.setTextColor(0xFFFFFFFF);
        btnBack.setTextSize(14f);
        btnBack.setBackgroundResource(R.drawable.key_bg);
        LinearLayout.LayoutParams bLp =
                new LinearLayout.LayoutParams(-1, dpToPx(44));
        bLp.setMargins(dpToPx(8), dpToPx(6), dpToPx(8), dpToPx(6));
        btnBack.setLayoutParams(bLp);
        btnBack.setOnClickListener(v -> showTemplatesList());
        contentContainer.addView(btnBack);
    }

    // ==================== ВСПОМОГАТЕЛЬНОЕ ====================

    private int dpToPx(float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }
                }
