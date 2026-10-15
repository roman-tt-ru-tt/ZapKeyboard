package com.zap.keyboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity {

    private LinearLayout listContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.listContainer = findViewById(R.id.listContainer);

        Button btnAdd = findViewById(R.id.btnAdd);
        Button btnEnable = findViewById(R.id.btnEnableKeyboard);
        Button btnSwitch = findViewById(R.id.btnSwitchKeyboard);

        btnAdd.setOnClickListener(v -> showAddDialog());

        btnEnable.setOnClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
            Toast.makeText(this,
                    "Выберите ZapKeyboard в списке",
                    Toast.LENGTH_LONG).show();
        });

        btnSwitch.setOnClickListener(v -> {
            try {
                InputMethodManager imm = (InputMethodManager)
                        getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showInputMethodPicker();
                    Toast.makeText(this,
                            "Выберите ZapKeyboard",
                            Toast.LENGTH_LONG).show();
                } else {
                    startActivity(new Intent(
                            Settings.ACTION_INPUT_METHOD_SETTINGS));
                }
            } catch (Exception e) {
                Toast.makeText(this, "Ошибка: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    private void refreshList() {
        listContainer.removeAllViews();
        List<ZapManager.ZapItem> items = ZapManager.getAll(this);

        if (items.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Пока нет шаблонов.\nНажмите «+ Добавить шаблон»");
            empty.setTextColor(0xFFA78BFA);
            empty.setTextSize(14f);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(20, 60, 20, 60);
            listContainer.addView(empty);
            return;
        }

        for (final ZapManager.ZapItem item : items) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14));
            LinearLayout.LayoutParams cardLp =
                    new LinearLayout.LayoutParams(-1, -2);
            cardLp.bottomMargin = dpToPx(8);
            card.setLayoutParams(cardLp);

            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setColor(0xFF2D1B4E);
            bg.setCornerRadius(dpToPx(14));
            bg.setStroke(dpToPx(1), 0xFF7C3AED);
            card.setBackground(bg);

            TextView name = new TextView(this);
            name.setText(item.name);
            name.setTextColor(0xFFFFFFFF);
            name.setTextSize(16f);
            name.setTypeface(null, Typeface.BOLD);
            card.addView(name);

            TextView text = new TextView(this);
            String preview = item.text.length() > 120
                    ? item.text.substring(0, 120) + "..."
                    : item.text;
            text.setText(preview);
            text.setTextColor(0xFFC4B5FD);
            text.setTextSize(13f);
            text.setPadding(0, dpToPx(6), 0, dpToPx(8));
            card.addView(text);

            LinearLayout btns = new LinearLayout(this);
            btns.setOrientation(LinearLayout.HORIZONTAL);

            Button btnCopy = new Button(this);
            btnCopy.setText("📋 Копировать");
            btnCopy.setAllCaps(false);
            btnCopy.setTextColor(0xFFFFFFFF);
            btnCopy.setTextSize(12f);
            btnCopy.setBackground(accentButton(0xFF7C3AED));
            btnCopy.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
            btnCopy.setOnClickListener(v -> {
                android.content.ClipboardManager cm =
                        (android.content.ClipboardManager)
                                getSystemService(CLIPBOARD_SERVICE);
                if (cm != null) {
                    cm.setPrimaryClip(android.content.ClipData.newPlainText(
                            "Zap", item.text));
                    Toast.makeText(MainActivity.this,
                            "Скопировано в буфер", Toast.LENGTH_SHORT).show();
                }
            });
            btns.addView(btnCopy);

            View sp = new View(this);
            sp.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(6), -1));
            btns.addView(sp);

            Button btnDelete = new Button(this);
            btnDelete.setText("🗑 Удалить");
            btnDelete.setAllCaps(false);
            btnDelete.setTextColor(0xFFFFFFFF);
            btnDelete.setTextSize(12f);
            btnDelete.setBackground(accentButton(0xFFDC2626));
            btnDelete.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
            btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(MainActivity.this,
                        R.style.ZapDialog)
                        .setTitle("Удалить?")
                        .setMessage("Убрать шаблон «" + item.name + "»?")
                        .setPositiveButton("Удалить", (d, w) -> {
                            ZapManager.remove(MainActivity.this, item.name);
                            refreshList();
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            });
            btns.addView(btnDelete);

            card.addView(btns);
            listContainer.addView(card);
        }
    }

    private void showAddDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dpToPx(30), dpToPx(20), dpToPx(30), dpToPx(20));

        TextView nameLabel = new TextView(this);
        nameLabel.setText("Название");
        nameLabel.setTextColor(0xFFFFFFFF);
        nameLabel.setTextSize(14f);
        nameLabel.setPadding(0, 0, 0, dpToPx(6));
        layout.addView(nameLabel);

        final EditText etName = new EditText(this);
        etName.setHint("Например: Привет");
        etName.setHintTextColor(0xFFA78BFA);
        etName.setTextColor(0xFFFFFFFF);
        etName.setInputType(InputType.TYPE_CLASS_TEXT);
        etName.setBackground(accentButton(0xFF2D1B4E));
        etName.setPadding(dpToPx(20), dpToPx(14), dpToPx(20), dpToPx(14));
        layout.addView(etName);

        View sp1 = new View(this);
        sp1.setLayoutParams(new LinearLayout.LayoutParams(-1, dpToPx(20)));
        layout.addView(sp1);

        TextView textLabel = new TextView(this);
        textLabel.setText("Текст");
        textLabel.setTextColor(0xFFFFFFFF);
        textLabel.setTextSize(14f);
        textLabel.setPadding(0, 0, 0, dpToPx(6));
        layout.addView(textLabel);

        final EditText etText = new EditText(this);
        etText.setHint("Текст шаблона");
        etText.setHintTextColor(0xFFA78BFA);
        etText.setTextColor(0xFFFFFFFF);
        etText.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        etText.setLines(5);
        etText.setGravity(Gravity.TOP | Gravity.START);
        etText.setBackground(accentButton(0xFF2D1B4E));
        etText.setPadding(dpToPx(20), dpToPx(14), dpToPx(20), dpToPx(14));
        layout.addView(etText);

        new AlertDialog.Builder(this, R.style.ZapDialog)
                .setTitle("Новый шаблон")
                .setView(layout)
                .setPositiveButton("Сохранить", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String text = etText.getText().toString();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Введите название",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (ZapManager.add(this, name, text)) {
                        Toast.makeText(this, "✅ Добавлено",
                                Toast.LENGTH_SHORT).show();
                        refreshList();
                    } else {
                        Toast.makeText(this,
                                "Шаблон с таким именем уже есть",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private android.graphics.drawable.StateListDrawable accentButton(int color) {
        android.graphics.drawable.StateListDrawable sld =
                new android.graphics.drawable.StateListDrawable();

        GradientDrawable normal = new GradientDrawable();
        normal.setShape(GradientDrawable.RECTANGLE);
        normal.setColor(color);
        normal.setCornerRadius(dpToPx(10));

        GradientDrawable pressed = new GradientDrawable();
        pressed.setShape(GradientDrawable.RECTANGLE);
        pressed.setColor(darken(color));
        pressed.setCornerRadius(dpToPx(10));

        sld.addState(new int[]{android.R.attr.state_pressed}, pressed);
        sld.addState(new int[0], normal);
        return sld;
    }

    private int darken(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return 0xFF000000
                | (((int) (r * 0.7f)) << 16)
                | (((int) (g * 0.7f)) << 8)
                | ((int) (b * 0.7f));
    }

    private int dpToPx(float dp) {
        return (int) android.util.TypedValue.applyDimension(
                android.util.TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }
        }
