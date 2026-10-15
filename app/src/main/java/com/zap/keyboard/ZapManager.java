package com.zap.keyboard;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Хранилище шаблонов Zap.
 * Формат: JSON-массив [{name, lines: [...]}, ...]
 */
public final class ZapManager {

    private static final String PREFS = "ZapData";
    private static final String KEY_TEMPLATES = "zap_templates";

    /** Максимум строк в одном шаблоне. */
    public static final int MAX_LINES_PER_TEMPLATE = 50;

    public static final class ZapTemplate {
        public final String name;
        public final List<String> lines;

        public ZapTemplate(String name, List<String> lines) {
            this.name = name;
            this.lines = lines != null ? lines : new ArrayList<>();
        }

        public JSONObject toJson() throws Exception {
            JSONObject o = new JSONObject();
            o.put("name", name);
            JSONArray arr = new JSONArray();
            for (String line : lines) arr.put(line);
            o.put("lines", arr);
            return o;
        }

        public static ZapTemplate fromJson(JSONObject o) {
            String name = o.optString("name", "");
            List<String> lines = new ArrayList<>();
            JSONArray arr = o.optJSONArray("lines");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    String line = arr.optString(i, "");
                    if (!line.isEmpty()) lines.add(line);
                }
            }
            return new ZapTemplate(name, lines);
        }
    }

    private ZapManager() { }

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // ==================== ПОЛУЧИТЬ ВСЕ ШАБЛОНЫ ====================

    public static synchronized List<ZapTemplate> getAll(Context c) {
        List<ZapTemplate> out = new ArrayList<>();
        try {
            String raw = prefs(c).getString(KEY_TEMPLATES, "[]");
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o != null) out.add(ZapTemplate.fromJson(o));
            }
        } catch (Exception ignored) { }
        return out;
    }

    /** Найти шаблон по имени. */
    public static synchronized ZapTemplate findByName(Context c, String name) {
        if (name == null) return null;
        for (ZapTemplate t : getAll(c)) {
            if (t.name.equals(name)) return t;
        }
        return null;
    }

    // ==================== УПРАВЛЕНИЕ ШАБЛОНАМИ ====================

    /** Создать новый шаблон. */
    public static synchronized boolean addTemplate(Context c, String name) {
        if (name == null || name.trim().isEmpty()) return false;
        name = name.trim();

        List<ZapTemplate> list = getAll(c);
        for (ZapTemplate t : list) {
            if (t.name.equalsIgnoreCase(name)) return false;
        }
        list.add(0, new ZapTemplate(name, new ArrayList<>()));
        save(c, list);
        return true;
    }

    /** Удалить шаблон по имени. */
    public static synchronized boolean removeTemplate(Context c, String name) {
        if (name == null) return false;
        List<ZapTemplate> list = getAll(c);
        boolean removed = false;
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).name.equals(name)) {
                list.remove(i);
                removed = true;
            }
        }
        if (removed) save(c, list);
        return removed;
    }

    /** Переименовать шаблон. */
    public static synchronized boolean renameTemplate(Context c,
                                                      String oldName,
                                                      String newName) {
        if (oldName == null || newName == null) return false;
        newName = newName.trim();
        if (newName.isEmpty()) return false;

        List<ZapTemplate> list = getAll(c);
        // Проверка на дубликат
        for (ZapTemplate t : list) {
            if (!t.name.equals(oldName) && t.name.equalsIgnoreCase(newName)) return false;
        }
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).name.equals(oldName)) {
                list.set(i, new ZapTemplate(newName, list.get(i).lines));
                found = true;
                break;
            }
        }
        if (found) save(c, list);
        return found;
    }

    // ==================== УПРАВЛЕНИЕ СТРОКАМИ ====================

    /** Добавить строку в шаблон. */
    public static synchronized boolean addLine(Context c, String templateName, String line) {
        if (templateName == null || line == null) return false;
        line = line.trim();
        if (line.isEmpty()) return false;

        List<ZapTemplate> list = getAll(c);
        for (int i = 0; i < list.size(); i++) {
            ZapTemplate t = list.get(i);
            if (t.name.equals(templateName)) {
                if (t.lines.size() >= MAX_LINES_PER_TEMPLATE) return false;
                List<String> newLines = new ArrayList<>(t.lines);
                newLines.add(line);
                list.set(i, new ZapTemplate(t.name, newLines));
                save(c, list);
                return true;
            }
        }
        return false;
    }

    /** Удалить строку из шаблона по индексу. */
    public static synchronized boolean removeLine(Context c,
                                                  String templateName,
                                                  int index) {
        if (templateName == null) return false;
        List<ZapTemplate> list = getAll(c);
        for (int i = 0; i < list.size(); i++) {
            ZapTemplate t = list.get(i);
            if (t.name.equals(templateName)) {
                if (index < 0 || index >= t.lines.size()) return false;
                List<String> newLines = new ArrayList<>(t.lines);
                newLines.remove(index);
                list.set(i, new ZapTemplate(t.name, newLines));
                save(c, list);
                return true;
            }
        }
        return false;
    }

    /** Заменить строку в шаблоне по индексу. */
    public static synchronized boolean updateLine(Context c,
                                                  String templateName,
                                                  int index,
                                                  String newText) {
        if (templateName == null || newText == null) return false;
        newText = newText.trim();
        if (newText.isEmpty()) return false;

        List<ZapTemplate> list = getAll(c);
        for (int i = 0; i < list.size(); i++) {
            ZapTemplate t = list.get(i);
            if (t.name.equals(templateName)) {
                if (index < 0 || index >= t.lines.size()) return false;
                List<String> newLines = new ArrayList<>(t.lines);
                newLines.set(index, newText);
                list.set(i, new ZapTemplate(t.name, newLines));
                save(c, list);
                return true;
            }
        }
        return false;
    }

    // ==================== SAVE ====================

    private static void save(Context c, List<ZapTemplate> list) {
        try {
            JSONArray arr = new JSONArray();
            for (ZapTemplate t : list) arr.put(t.toJson());
            prefs(c).edit().putString(KEY_TEMPLATES, arr.toString()).apply();
        } catch (Exception ignored) { }
    }
                                                            }
