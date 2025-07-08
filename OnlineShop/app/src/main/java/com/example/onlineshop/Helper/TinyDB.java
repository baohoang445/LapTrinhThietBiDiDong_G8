package com.example.onlineshop.Helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;



import com.google.gson.Gson;
import com.example.onlineshop.Model.ItemsModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;


public class TinyDB {

    private SharedPreferences preferences;
    private String DEFAULT_APP_IMAGEDATA_DIRECTORY;
    private String lastImagePath = "";

    public TinyDB(Context appContext) {
        preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
    }

    /**
     * Kiểm tra bộ nhớ ngoài có ghi được không
     *
     * @return true nếu ghi được, false nếu không
     */
    public static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * Kiểm tra bộ nhớ ngoài có đọc được không
     *
     * @return true nếu đọc được, false nếu không
     */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();

        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    /**
     * Giải mã Bitmap từ 'path' và trả về nó
     *
     * @param path đường dẫn ảnh
     * @return Bitmap từ 'path'
     */
    public Bitmap getImage(String path) {
        Bitmap bitmapFromPath = null;
        try {
            bitmapFromPath = BitmapFactory.decodeFile(path);

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

        return bitmapFromPath;
    }

    /**
     * Trả về đường dẫn String của ảnh vừa lưu gần nhất
     *
     * @return đường dẫn string của ảnh vừa lưu
     */
    public String getSavedImagePath() {
        return lastImagePath;
    }

    /**
     * Lưu 'theBitmap' vào thư mục 'theFolder' với tên 'theImageName'
     *
     * @param theFolder    đường dẫn thư mục bạn muốn lưu, ví dụ "DropBox/WorkImages"
     * @param theImageName tên bạn muốn đặt cho file ảnh, ví dụ "MeAtLunch.png"
     * @param theBitmap    ảnh bạn muốn lưu dạng Bitmap
     * @return trả về đường dẫn đầy đủ (địa chỉ hệ thống tệp) của ảnh đã lưu
     */
    public String putImage(String theFolder, String theImageName, Bitmap theBitmap) {
        if (theFolder == null || theImageName == null || theBitmap == null)
            return null;

        this.DEFAULT_APP_IMAGEDATA_DIRECTORY = theFolder;
        String mFullPath = setupFullPath(theImageName);

        if (!mFullPath.equals("")) {
            lastImagePath = mFullPath;
            saveBitmap(mFullPath, theBitmap);
        }

        return mFullPath;
    }

    /**
     * Lưu 'theBitmap' vào 'fullPath'
     *
     * @param fullPath  đường dẫn đầy đủ của file ảnh, ví dụ "Images/MeAtLunch.png"
     * @param theBitmap ảnh bạn muốn lưu dạng Bitmap
     * @return true nếu lưu thành công, false nếu không
     */
    public boolean putImageWithFullPath(String fullPath, Bitmap theBitmap) {
        return !(fullPath == null || theBitmap == null) && saveBitmap(fullPath, theBitmap);
    }

    // Getters

    /**
     * Tạo đường dẫn cho ảnh với tên 'imageName' trong thư mục DEFAULT_APP..
     *
     * @param imageName tên của ảnh
     * @return đường dẫn đầy đủ của ảnh. Nếu tạo thư mục thất bại, trả về chuỗi rỗng
     */
    private String setupFullPath(String imageName) {
        File mFolder = new File(Environment.getExternalStorageDirectory(), DEFAULT_APP_IMAGEDATA_DIRECTORY);

        if (isExternalStorageReadable() && isExternalStorageWritable() && !mFolder.exists()) {
            if (!mFolder.mkdirs()) {
                Log.e("ERROR", "Failed to setup folder");
                return "";
            }
        }

        return mFolder.getPath() + '/' + imageName;
    }

    /**
     * Lưu Bitmap thành file PNG tại đường dẫn 'fullPath'
     *
     * @param fullPath đường dẫn file ảnh
     * @param bitmap   ảnh dạng Bitmap
     * @return true nếu lưu thành công, false nếu không
     */
    private boolean saveBitmap(String fullPath, Bitmap bitmap) {
        if (fullPath == null || bitmap == null)
            return false;

        boolean fileCreated = false;
        boolean bitmapCompressed = false;
        boolean streamClosed = false;

        File imageFile = new File(fullPath);

        if (imageFile.exists())
            if (!imageFile.delete())
                return false;

        try {
            fileCreated = imageFile.createNewFile();

        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(imageFile);
            bitmapCompressed = bitmap.compress(CompressFormat.PNG, 100, out);

        } catch (Exception e) {
            e.printStackTrace();
            bitmapCompressed = false;

        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                    streamClosed = true;

                } catch (IOException e) {
                    e.printStackTrace();
                    streamClosed = false;
                }
            }
        }

        return (fileCreated && bitmapCompressed && streamClosed);
    }

    /**
     * Lấy giá trị int từ SharedPreferences tại 'key'. Nếu không tìm thấy trả về 0
     *
     * @param key khóa SharedPreferences
     * @return giá trị int tại 'key' hoặc 0 nếu không tìm thấy
     */
    public int getInt(String key) {
        return preferences.getInt(key, 0);
    }

    /**
     * Lấy ArrayList các số nguyên từ SharedPreferences tại 'key'
     *
     * @param key khóa SharedPreferences
     * @return ArrayList các số nguyên
     */
    public ArrayList<Integer> getListInt(String key) {
        String[] myList = TextUtils.split(preferences.getString(key, ""), "‚‗‚");
        ArrayList<String> arrayToList = new ArrayList<String>(Arrays.asList(myList));
        ArrayList<Integer> newList = new ArrayList<Integer>();

        for (String item : arrayToList)
            newList.add(Integer.parseInt(item));

        return newList;
    }

    /**
     * Lấy giá trị long từ SharedPreferences tại 'key'. Nếu không tìm thấy trả về 0
     *
     * @param key khóa SharedPreferences
     * @return giá trị long tại 'key' hoặc 0 nếu không tìm thấy
     */
    public long getLong(String key) {
        return preferences.getLong(key, 0);
    }

    /**
     * Lấy giá trị float từ SharedPreferences tại 'key'. Nếu không tìm thấy trả về 0
     *
     * @param key khóa SharedPreferences
     * @return giá trị float tại 'key' hoặc 0 nếu không tìm thấy
     */
    public float getFloat(String key) {
        return preferences.getFloat(key, 0);
    }

    /**
     * Lấy giá trị double từ SharedPreferences tại 'key'. Nếu có lỗi trả về 0
     *
     * @param key khóa SharedPreferences
     * @return giá trị double tại 'key' hoặc 0 nếu có lỗi
     */
    public double getDouble(String key) {
        String number = getString(key);

        try {
            return Double.parseDouble(number);

        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Lấy ArrayList các số thực từ SharedPreferences tại 'key'
     *
     * @param key khóa SharedPreferences
     * @return ArrayList các số thực
     */
    public ArrayList<Double> getListDouble(String key) {
        String[] myList = TextUtils.split(preferences.getString(key, ""), "‚‗‚");
        ArrayList<String> arrayToList = new ArrayList<String>(Arrays.asList(myList));
        ArrayList<Double> newList = new ArrayList<Double>();

        for (String item : arrayToList)
            newList.add(Double.parseDouble(item));

        return newList;
    }

    /**
     * Lấy ArrayList các số nguyên dài từ SharedPreferences tại 'key'
     *
     * @param key khóa SharedPreferences
     * @return ArrayList các số nguyên dài
     */
    public ArrayList<Long> getListLong(String key) {
        String[] myList = TextUtils.split(preferences.getString(key, ""), "‚‗‚");
        ArrayList<String> arrayToList = new ArrayList<String>(Arrays.asList(myList));
        ArrayList<Long> newList = new ArrayList<Long>();

        for (String item : arrayToList)
            newList.add(Long.parseLong(item));

        return newList;
    }

    /**
     * Lấy giá trị String từ SharedPreferences tại 'key'. Nếu không tìm thấy trả về ""
     *
     * @param key khóa SharedPreferences
     * @return giá trị String tại 'key' hoặc "" nếu không tìm thấy
     */
    public String getString(String key) {
        return preferences.getString(key, "");
    }

    /**
     * Lấy ArrayList các chuỗi từ SharedPreferences tại 'key'
     *
     * @param key khóa SharedPreferences
     * @return ArrayList các chuỗi
     */
    public ArrayList<String> getListString(String key) {
        return new ArrayList<String>(Arrays.asList(TextUtils.split(preferences.getString(key, ""), "‚‗‚")));
    }

    /**
     * Lấy giá trị boolean từ SharedPreferences tại 'key'. Nếu không tìm thấy trả về false
     *
     * @param key khóa SharedPreferences
     * @return giá trị boolean tại 'key' hoặc false nếu không tìm thấy
     */
    public boolean getBoolean(String key) {
        return preferences.getBoolean(key, false);
    }

    /**
     * Lấy ArrayList các giá trị boolean từ SharedPreferences tại 'key'
     *
     * @param key khóa SharedPreferences
     * @return ArrayList các giá trị boolean
     */
    public ArrayList<Boolean> getListBoolean(String key) {
        ArrayList<String> myList = getListString(key);
        ArrayList<Boolean> newList = new ArrayList<Boolean>();

        for (String item : myList) {
            if (item.equals("true")) {
                newList.add(true);
            } else {
                newList.add(false);
            }
        }

        return newList;
    }


    // Put methods

    public ArrayList<ItemsModel> getListObject(String key) {
        Gson gson = new Gson();

        ArrayList<String> objStrings = getListString(key);
        ArrayList<ItemsModel> playerList = new ArrayList<ItemsModel>();

        for (String jObjString : objStrings) {
            ItemsModel player = gson.fromJson(jObjString, ItemsModel.class);
            playerList.add(player);
        }
        return playerList;
    }

    public <T> T getObject(String key, Class<T> classOfT) {

        String json = getString(key);
        Object value = new Gson().fromJson(json, classOfT);
        if (value == null)
            throw new NullPointerException();
        return (T) value;
    }

    /**
     * Ghi giá trị int vào SharedPreferences với 'key' và lưu lại
     *
     * @param key   khóa SharedPreferences
     * @param value giá trị int cần thêm
     */
    public void putInt(String key, int value) {
        checkForNullKey(key);
        preferences.edit().putInt(key, value).apply();
    }

    /**
     * Ghi ArrayList các số nguyên vào SharedPreferences với 'key' và lưu lại
     *
     * @param key     khóa SharedPreferences
     * @param intList ArrayList các số nguyên cần thêm
     */
    public void putListInt(String key, ArrayList<Integer> intList) {
        checkForNullKey(key);
        Integer[] myIntList = intList.toArray(new Integer[intList.size()]);
        preferences.edit().putString(key, TextUtils.join("‚‗‚", myIntList)).apply();
    }

    /**
     * Ghi giá trị long vào SharedPreferences với 'key' và lưu lại
     *
     * @param key   khóa SharedPreferences
     * @param value giá trị long cần thêm
     */
    public void putLong(String key, long value) {
        checkForNullKey(key);
        preferences.edit().putLong(key, value).apply();
    }

    /**
     * Ghi ArrayList các số nguyên dài vào SharedPreferences với 'key' và lưu lại
     *
     * @param key      khóa SharedPreferences
     * @param longList ArrayList các số nguyên dài cần thêm
     */
    public void putListLong(String key, ArrayList<Long> longList) {
        checkForNullKey(key);
        Long[] myLongList = longList.toArray(new Long[longList.size()]);
        preferences.edit().putString(key, TextUtils.join("‚‗‚", myLongList)).apply();
    }

    /**
     * Ghi giá trị float vào SharedPreferences với 'key' và lưu lại
     *
     * @param key   khóa SharedPreferences
     * @param value giá trị float cần thêm
     */
    public void putFloat(String key, float value) {
        checkForNullKey(key);
        preferences.edit().putFloat(key, value).apply();
    }

    /**
     * Ghi giá trị double vào SharedPreferences với 'key' và lưu lại
     *
     * @param key   khóa SharedPreferences
     * @param value giá trị double cần thêm
     */
    public void putDouble(String key, double value) {
        checkForNullKey(key);
        putString(key, String.valueOf(value));
    }

    /**
     * Ghi ArrayList các số thực vào SharedPreferences với 'key' và lưu lại
     *
     * @param key        khóa SharedPreferences
     * @param doubleList ArrayList các số thực cần thêm
     */
    public void putListDouble(String key, ArrayList<Double> doubleList) {
        checkForNullKey(key);
        Double[] myDoubleList = doubleList.toArray(new Double[doubleList.size()]);
        preferences.edit().putString(key, TextUtils.join("‚‗‚", myDoubleList)).apply();
    }

    /**
     * Ghi giá trị String vào SharedPreferences với 'key' và lưu lại
     *
     * @param key   khóa SharedPreferences
     * @param value giá trị String cần thêm
     */
    public void putString(String key, String value) {
        checkForNullKey(key);
        checkForNullValue(value);
        preferences.edit().putString(key, value).apply();
    }

    /**
     * Ghi ArrayList các chuỗi vào SharedPreferences với 'key' và lưu lại
     *
     * @param key        khóa SharedPreferences
     * @param stringList ArrayList các chuỗi cần thêm
     */
    public void putListString(String key, ArrayList<String> stringList) {
        checkForNullKey(key);
        String[] myStringList = stringList.toArray(new String[stringList.size()]);
        preferences.edit().putString(key, TextUtils.join("‚‗‚", myStringList)).apply();
    }

    /**
     * Ghi giá trị boolean vào SharedPreferences với 'key' và lưu lại
     *
     * @param key   khóa SharedPreferences
     * @param value giá trị boolean cần thêm
     */
    public void putBoolean(String key, boolean value) {
        checkForNullKey(key);
        preferences.edit().putBoolean(key, value).apply();
    }

    /**
     * Ghi ArrayList các giá trị boolean vào SharedPreferences với 'key' và lưu lại
     *
     * @param key      khóa SharedPreferences
     * @param boolList ArrayList các giá trị boolean cần thêm
     */
    public void putListBoolean(String key, ArrayList<Boolean> boolList) {
        checkForNullKey(key);
        ArrayList<String> newList = new ArrayList<String>();

        for (Boolean item : boolList) {
            if (item) {
                newList.add("true");
            } else {
                newList.add("false");
            }
        }

        putListString(key, newList);
    }

    /**
     * Ghi đối tượng bất kỳ vào SharedPreferences với 'key' và lưu lại
     *
     * @param key khóa SharedPreferences
     * @param obj đối tượng cần lưu
     */
    public void putObject(String key, Object obj) {
        checkForNullKey(key);
        Gson gson = new Gson();
        putString(key, gson.toJson(obj));
    }

    public void putListObject(String key, ArrayList<ItemsModel> playerList) {
        checkForNullKey(key);
        Gson gson = new Gson();
        ArrayList<String> objStrings = new ArrayList<String>();
        for (ItemsModel player : playerList) {
            objStrings.add(gson.toJson(player));
        }
        putListString(key, objStrings);
    }

    /**
     * Xóa phần tử SharedPreferences với 'key'
     *
     * @param key khóa SharedPreferences
     */
    public void remove(String key) {
        preferences.edit().remove(key).apply();
    }

    /**
     * Xóa file ảnh tại 'path'
     *
     * @param path đường dẫn file ảnh
     * @return true nếu xóa thành công, false nếu không
     */
    public boolean deleteImage(String path) {
        return new File(path).delete();
    }

    /**
     * Xóa toàn bộ SharedPreferences (xóa tất cả)
     */
    public void clear() {
        preferences.edit().clear().apply();
    }

    /**
     * Lấy tất cả giá trị từ SharedPreferences. Không chỉnh sửa collection trả về bởi hàm này
     *
     * @return Map đại diện cho danh sách các cặp key/value từ SharedPreferences
     */
    public Map<String, ?> getAll() {
        return preferences.getAll();
    }

    /**
     * Đăng ký lắng nghe thay đổi SharedPreferences
     *
     * @param listener đối tượng lắng nghe OnSharedPreferenceChangeListener
     */
    public void registerOnSharedPreferenceChangeListener(
            SharedPreferences.OnSharedPreferenceChangeListener listener) {

        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Hủy đăng ký lắng nghe thay đổi SharedPreferences
     *
     * @param listener đối tượng lắng nghe OnSharedPreferenceChangeListener cần hủy
     */
    public void unregisterOnSharedPreferenceChangeListener(
            SharedPreferences.OnSharedPreferenceChangeListener listener) {

        preferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Khóa null sẽ làm hỏng file shared pref và khiến nó không đọc được, đây là biện pháp phòng ngừa
     *
     * @param key khóa cần kiểm tra
     */
    private void checkForNullKey(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
    }

    /**
     * Giá trị null sẽ làm hỏng file shared pref và khiến nó không đọc được, đây là biện pháp phòng ngừa
     *
     * @param value giá trị cần kiểm tra
     */
    private void checkForNullValue(String value) {
        if (value == null) {
            throw new NullPointerException();
        }
    }
}