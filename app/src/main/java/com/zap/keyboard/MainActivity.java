package com.zap.keyboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

        btnAdd.setOnClickListener(v -> showAddTemplateDialog());
        btnEnable.setOnClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
            Toast.makeText(this, "Выберите ZapKeyboard в списке",
                    Toast.LENGTH_LONG).show();
        });
        btnSwitch.setOnClickListener(v -> {
            try {
                InputMethodManager imm = (InputMethodManager)
                        getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showInputMethodPicker();
                    Toast.makeText(this, "Выберите ZapKeyboard",
                            Toast.LENGTH_LONG).show();
                } else {
                    startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
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

    // ==================== СПИСОК ШАБЛОНОВ ====================

    private void refreshList() {
        listContainer.removeAllViews();
        List<ZapManager.ZapTemplate> templates = ZapManager.getAll(this);

        if (templates.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Пока нет шаблонов.\nНажмите «+ Добавить шаблон»");
            empty.setTextColor(0xFFA78BFA);
            empty.setTextSize(14f);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(20, 60, 20, 60);
            listContainer.addView(empty);
            return;
        }

        for (final ZapManager.ZapTemplate tpl : templates) {
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
            name.setText("📄 " + tpl.name);
            name.setTextColor(0xFFFFFFFF);
            name.setTextSize(17f);
            name.setTypeface(null, Typeface.BOLD);
            card.addView(name);

            TextView count = new TextView(this);
            count.setText("Строк: " + tpl.lines.size()
                    + " / " + ZapManager.MAX_LINES_PER_TEMPLATE);
            count.setTextColor(0xFFA78BFA);
            count.setTextSize(12f);
            count.setPadding(0, dpToPx(6), 0, dpToPx(8));
            card.addView(count);

            LinearLayout btns = new LinearLayout(this);
            btns.setOrientation(LinearLayout.HORIZONTAL);

            Button btnEdit = new Button(this);
            btnEdit.setText("✏️ Редактор строк");
            btnEdit.setAllCaps(false);
            btnEdit.setTextColor(0xFFFFFFFF);
            btnEdit.setTextSize(12f);
            btnEdit.setBackground(accentButton(0xFF7C3AED));
            btnEdit.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
            btnEdit.setOnClickListener(v -> showLinesEditor(tpl.name));
            btns.addView(btnEdit);

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
                new AlertDialog.Builder(MainActivity.this, R.style.ZapDialog)
                        .setTitle("Удалить шаблон?")
                        .setMessage("Убрать «" + tpl.name + "» со всеми строками?")
                        .setPositiveButton("Удалить", (d, w) -> {
                            ZapManager.removeTemplate(MainActivity.this, tpl.name);
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

    // ==================== ДОБАВИТЬ ШАБЛОН ====================

    private void showAddTemplateDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dpToPx(30), dpToPx(20), dpToPx(30), dpToPx(20));

        TextView label = new TextView(this);
        label.setText("Название шаблона");
        label.setTextColor(0xFFFFFFFF);
        label.setTextSize(14f);
        label.setPadding(0, 0, 0, dpToPx(6));
        layout.addView(label);

        final EditText etName = new EditText(this);
        etName.setHint("Например: Привет");
        etName.setHintTextColor(0xFFA78BFA);
        etName.setTextColor(0xFFFFFFFF);
        etName.setInputType(InputType.TYPE_CLASS_TEXT);
        etName.setBackground(accentButton(0xFF2D1B4E));
        etName.setPadding(dpToPx(20), dpToPx(14), dpToPx(20), dpToPx(14));
        layout.addView(etName);

        new AlertDialog.Builder(this, R.style.ZapDialog)
                .setTitle("Новый шаблон")
                .setView(layout)
                .setPositiveButton("Создать", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Введите название",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (ZapManager.addTemplate(this, name)) {
                        Toast.makeText(this, "✅ Шаблон создан",
                                Toast.LENGTH_SHORT).show();
                        refreshList();
                        // Сразу открываем редактор строк
                        showLinesEditor(name);
                    } else {
                        Toast.makeText(this, "Шаблон с таким именем уже есть",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    // ==================== РЕДАКТОР СТРОК ====================

    private void showLinesEditor(final String templateName) {
        ScrollView scroll = new ScrollView(this);
        final LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));
        scroll.addView(container);

        // Диалог создадим заранее, чтобы обновлять содержимое
        final AlertDialog dialog = new AlertDialog.Builder(this, R.style.ZapDialog)
                .setTitle("Шаблон: " + templateName)
                .setView(scroll)
                .setNegativeButton("Закрыть", null)
                .create();

        Runnable refresh = new Runnable() {
            @Override public void run() {
                container.removeAllViews();

                ZapManager.ZapTemplate tpl = ZapManager.findByName(
                        MainActivity.this, templateName);
                if (tpl == null) {
                    dialog.dismiss();
                    return;
                }

                // Счётчик
                TextView counter = new TextView(MainActivity.this);
                counter.setText("Строк: " + tpl.lines.size()
                        + " / " + ZapManager.MAX_LINES_PER_TEMPLATE);
                counter.setTextColor(0xFFA78BFA);
                counter.setTextSize(13f);
                counter.setPadding(0, 0, 0, dpToPx(10));
                container.addView(counter);

                // Кнопка "+ Добавить строку"
                Button btnAddLine = new Button(MainActivity.this);
                btnAddLine.setText("+ Добавить строку");
                btnAddLine.setAllCaps(false);
                btnAddLine.setTextColor(0xFFFFFFFF);
                btnAddLine.setTextSize(14f);
                btnAddLine.setBackground(accentButton(0xFF10B981));
                LinearLayout.LayoutParams alLp =
                        new LinearLayout.LayoutParams(-1, -2);
                alLp.bottomMargin = dpToPx(12);
                btnAddLine.setLayoutParams(alLp);
                btnAddLine.setEnabled(
                        tpl.lines.size() < ZapManager.MAX_LINES_PER_TEMPLATE);
                btnAddLine.setOnClickListener(v -> {
                    dialog.dismiss();
                    showAddLineDialog(templateName);
                });
                container.addView(btnAddLine);

                // Список строк
                if (tpl.lines.isEmpty()) {
                    TextView empty = new TextView(MainActivity.this);
                    empty.setText("Пока нет строк.\nДобавьте первую строку.");
                    empty.setTextColor(0xFFA78BFA);
                    empty.setTextSize(13f);
                    empty.setGravity(Gravity.CENTER);
                    empty.setPadding(20, 40, 20, 40);
                    container.addView(empty);
                } else {
                    for (int i = 0; i < tpl.lines.size(); i++) {
                        final int index = i;
                        final String line = tpl.lines.get(i);

                        LinearLayout row = new LinearLayout(MainActivity.this);
                        row.setOrientation(LinearLayout.HORIZONTAL);
                        row.setBackground(accentButton(0xFF1A0B2E));
                        row.setPadding(dpToPx(12), dpToPx(10),
                                dpToPx(12), dpToPx(10));
                        LinearLayout.LayoutParams rowLp =
                                new LinearLayout.LayoutParams(-1, -2);
                        rowLp.bottomMargin = dpToPx(6);
                        row.setLayoutParams(rowLp);

                        TextView num = new TextView(MainActivity.this);
                        num.setText((i + 1) + ".");
                        num.setTextColor(0xFFA78BFA);
                        num.setTextSize(13f);
                        num.setPadding(0, 0, dpToPx(10), 0);
                        row.addView(num);

                        TextView text = new TextView(MainActivity.this);
                        text.setText(line);
                        text.setTextColor(0xFFFFFFFF);
                        text.setTextSize(13f);
                        text.setLayoutParams(new LinearLayout.LayoutParams(
                                0, -2, 1f));
                        row.addView(text);

                        Button btnEdit = new Button(MainActivity.this);
                        btnEdit.setText("✏️");
                        btnEdit.setAllCaps(false);
                        btnEdit.setTextColor(0xFFFFFFFF);
                        btnEdit.setBackground(accentButton(0xFF7C3AED));
                        btnEdit.setLayoutParams(new LinearLayout.LayoutParams(
                                dpToPx(44), dpToPx(44)));
                        btnEdit.setOnClickListener(v -> {
                            dialog.dismiss();
                            showEditLineDialog(templateName, index, line);
                        });
                        row.addView(btnEdit);

                        View sp = new View(MainActivity.this);
                        sp.setLayoutParams(new LinearLayout.LayoutParams(
                                dpToPx(4), -1));
                        row.addView(sp);

                        Button btnDel = new Button(MainActivity.this);
                        btnDel.setText("🗑");
                        btnDel.setAllCaps(false);
                        btnDel.setTextColor(0xFFFFFFFF);
                        btnDel.setBackground(accentButton(0xFFDC2626));
                        btnDel.setLayoutParams(new LinearLayout.LayoutParams(
                                dpToPx(44), dpToPx(44)));
                        btnDel.setOnClickListener(v -> {
                            ZapManager.removeLine(
                                    MainActivity.this, templateName, index);
                            refreshList();
                            refresh.run();
                        });
                        row.addView(btnDel);

                        container.addView(row);
                    }
                }
            }
        };
        refresh.run();
        dialog.setOnDismissListener(d -> refreshList());
        dialog.show();
    }

    // ==================== ДОБАВИТЬ СТРОКУ ====================

    private void showAddLineDialog(final String templateName) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dpToPx(30), dpToPx(20), dpToPx(30), dpToPx(20));

        final EditText et = new EditText(this);
        et.setHint("Текст строки");
        et.setHintTextColor(0xFFA78BFA);
        et.setTextColor(0xFFFFFFFF);
        et.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        et.setLines(3);
        et.setGravity(Gravity.TOP | Gravity.START);
        et.setBackground(accentButton(0xFF2D1B4E));
        et.setPadding(dpToPx(20), dpToPx(14), dpToPx(20), dpToPx(14));
        layout.addView(et);

        new AlertDialog.Builder(this, R.style.ZapDialog)
                .setTitle("Новая строка")
                .setView(layout)
                .setPositiveButton("Сохранить", (d, w) -> {
                    String text = et.getText().toString().trim();
                    if (text.isEmpty()) {
                        Toast.makeText(this, "Введите текст",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (ZapManager.addLine(this, templateName, text)) {
                        refreshList();
                        showLinesEditor(templateName);
                    } else {
                        Toast.makeText(this,
                                "Не удалось добавить (лимит "
                                        + ZapManager.MAX_LINES_PER_TEMPLATE + ")?",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", (d, w) -> showLinesEditor(templateName))
                .show();
    }

    // ==================== РЕДАКТИРОВАТЬ СТРОКУ ====================

    private void showEditLineDialog(final String templateName,
                                    final int index,
                                    String currentText) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dpToPx(30), dpToPx(20), dpToPx(30), dpToPx(20));

        final EditText et = new EditText(this);
        et.setText(currentText);
        et.setHintTextColor(0xFFA78BFA);
        et.setTextColor(0xFFFFFFFF);
        et.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        et.setLines(3);
        et.setGravity(Gravity.TOP | Gravity.START);
        et.setBackground(accentButton(0xFF2D1B4E));
        et.setPadding(dpToPx(20), dpToPx(14), dpToPx(20), dpToPx(14));
        layout.addView(et);

        new AlertDialog.Builder(this, R.style.ZapDialog)
                .setTitle("Редактировать строку " + (index + 1))
                .setView(layout)
                .setPositiveButton("Сохранить", (d, w) -> {
                    String text = et.getText().toString().trim();
                    if (text.isEmpty()) {
                        Toast.makeText(this, "Введите текст",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (ZapManager.updateLine(this, templateName, index, text)) {
                        refreshList();
                        showLinesEditor(templateName);
                    }
                })
                .setNegativeButton("Отмена", (d, w) -> showLinesEditor(templateName))
                .show();
    }

    // ==================== ВСПОМОГАТЕЛЬНОЕ ====================

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
