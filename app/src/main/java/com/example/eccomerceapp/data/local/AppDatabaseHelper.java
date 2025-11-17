package com.example.eccomerceapp.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.eccomerceapp.model.CartItem;
import com.example.eccomerceapp.model.Category;
import com.example.eccomerceapp.model.Order;
import com.example.eccomerceapp.model.Product;

import java.util.ArrayList;
import java.util.List;

public class AppDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "ecommerce.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_CATEGORY = "categories";
    public static final String TABLE_PRODUCT = "products";
    public static final String TABLE_CART = "cart_items";
    public static final String TABLE_ORDERS = "orders";

    public AppDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + TABLE_CATEGORY + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "title TEXT NOT NULL," +
                        "imageUrl TEXT" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE " + TABLE_PRODUCT + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL," +
                        "description TEXT," +
                        "price REAL NOT NULL," +
                        "imageUrl TEXT," +
                        "categoryId INTEGER," +
                        "sizes TEXT," +
                        "colors TEXT," +
                        "FOREIGN KEY(categoryId) REFERENCES " + TABLE_CATEGORY + "(id)" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE " + TABLE_CART + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "productId INTEGER NOT NULL," +
                        "quantity INTEGER NOT NULL," +
                        "size TEXT," +
                        "color TEXT," +
                        "FOREIGN KEY(productId) REFERENCES " + TABLE_PRODUCT + "(id)" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE " + TABLE_ORDERS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "customerName TEXT NOT NULL," +
                        "phone TEXT NOT NULL," +
                        "addressLine TEXT NOT NULL," +
                        "city TEXT NOT NULL," +
                        "total REAL NOT NULL," +
                        "status TEXT NOT NULL," +
                        "createdAt INTEGER NOT NULL" +
                        ")"
        );

        insertInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        onCreate(db);
    }

    private void insertInitialData(SQLiteDatabase db) {
        long furnitureId = insertCategory(db, "Furniture", "https://images.unsplash.com/photo-1493666438817-866a91353ca9");
        long fashionId = insertCategory(db, "Fashion", "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab");
        long gadgetsId = insertCategory(db, "Gadgets", "https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5");
        long shoesId = insertCategory(db, "Sneakers", "https://images.unsplash.com/photo-1514996937319-344454492b37");

        insertProduct(db, "Nike Jordan 1 Retro Yellow",
                "Timeless high-top sneakers with a bold yellow accent.",
                608,
                "https://images.unsplash.com/photo-1514986888952-8cd320577b68",
                shoesId,
                "S,M,L",
                "Yellow,White,Black");

        insertProduct(db, "Jacket Pullover Sweat Hoodie",
                "Soft and warm hoodie perfect for chilly evenings.",
                28,
                "https://images.unsplash.com/photo-1484519332611-516457305ff6",
                fashionId,
                "S,M,L,XL,XXL",
                "Black,Gray,White");

        insertProduct(db, "Minimal Sofa Bliss",
                "Modern grey sofa that elevates any living room.",
                389,
                "https://images.unsplash.com/photo-1493666438817-866a91353ca9",
                furnitureId,
                "One Size",
                "Gray,Beige");

        insertProduct(db, "Smart Speaker Mini",
                "Compact assistant with room-filling sound.",
                79,
                "https://images.unsplash.com/photo-1517059224940-d4af9eec41e5",
                gadgetsId,
                "One Size",
                "Charcoal,White");
    }

    private long insertCategory(SQLiteDatabase db, String title, String imageUrl) {
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("imageUrl", imageUrl);
        return db.insert(TABLE_CATEGORY, null, values);
    }

    private long insertProduct(SQLiteDatabase db,
                               String name,
                               String description,
                               double price,
                               String imageUrl,
                               long categoryId,
                               String sizes,
                               String colors) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("description", description);
        values.put("price", price);
        values.put("imageUrl", imageUrl);
        values.put("categoryId", categoryId);
        values.put("sizes", sizes);
        values.put("colors", colors);
        return db.insert(TABLE_PRODUCT, null, values);
    }

    public List<Category> getCategories() {
        List<Category> categories = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_CATEGORY, null, null, null, null, null, "title ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow("imageUrl"));
                categories.add(new Category(id, title, imageUrl));
            }
            cursor.close();
        }
        return categories;
    }

    public List<Product> getProducts(Long categoryId) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String selection = categoryId == null ? null : "categoryId = ?";
        String[] selectionArgs = categoryId == null ? null : new String[]{String.valueOf(categoryId)};
        Cursor cursor = db.query(TABLE_PRODUCT, null, selection, selectionArgs, null, null, "id DESC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                products.add(parseProductCursor(cursor));
            }
            cursor.close();
        }
        return products;
    }

    public Product getProductById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCT, null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Product product = parseProductCursor(cursor);
            cursor.close();
            return product;
        }
        return null;
    }

    public List<Product> searchProducts(String keyword) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCT, null, "name LIKE ?", new String[]{"%" + keyword + "%"}, null, null, "name ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                products.add(parseProductCursor(cursor));
            }
            cursor.close();
        }
        return products;
    }

    public long addToCart(long productId, int quantity, String size, String color) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("productId", productId);
        values.put("quantity", quantity);
        values.put("size", size);
        values.put("color", color);
        return db.insert(TABLE_CART, null, values);
    }

    public boolean updateCartQuantity(long cartId, int quantity) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("quantity", quantity);
        int rows = db.update(TABLE_CART, values, "id = ?", new String[]{String.valueOf(cartId)});
        return rows > 0;
    }

    public boolean removeCartItem(long cartId) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE_CART, "id = ?", new String[]{String.valueOf(cartId)});
        return rows > 0;
    }

    public void clearCart() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_CART, null, null);
    }

    public int getCartItemCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(quantity) AS total FROM " + TABLE_CART, null);
        int total = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow("total");
                if (!cursor.isNull(index)) {
                    total = cursor.getInt(index);
                }
            }
            cursor.close();
        }
        return total;
    }

    public long insertOrder(String customerName,
                            String phone,
                            String addressLine,
                            String city,
                            double total,
                            String status,
                            long createdAt) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("customerName", customerName);
        values.put("phone", phone);
        values.put("addressLine", addressLine);
        values.put("city", city);
        values.put("total", total);
        values.put("status", status);
        values.put("createdAt", createdAt);
        return db.insert(TABLE_ORDERS, null, values);
    }

    public List<Order> getOrders() {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_ORDERS, null, null, null, null, null, "createdAt DESC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                String customerName = cursor.getString(cursor.getColumnIndexOrThrow("customerName"));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
                String addressLine = cursor.getString(cursor.getColumnIndexOrThrow("addressLine"));
                String city = cursor.getString(cursor.getColumnIndexOrThrow("city"));
                double total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("createdAt"));
                orders.add(new Order(id, customerName, phone, addressLine, city, total, status, createdAt));
            }
            cursor.close();
        }
        return orders;
    }

    public List<CartItem> getCartItems() {
        List<CartItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_CART, null, null, null, null, null, "id DESC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                long productId = cursor.getLong(cursor.getColumnIndexOrThrow("productId"));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
                String size = cursor.getString(cursor.getColumnIndexOrThrow("size"));
                String color = cursor.getString(cursor.getColumnIndexOrThrow("color"));
                Product product = getProductById(productId);
                if (product != null) {
                    items.add(new CartItem(id, product, quantity, size, color));
                }
            }
            cursor.close();
        }
        return items;
    }
    private Product parseProductCursor(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
        double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
        String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow("imageUrl"));
        long categoryId = cursor.getLong(cursor.getColumnIndexOrThrow("categoryId"));
        String sizes = cursor.getString(cursor.getColumnIndexOrThrow("sizes"));
        String colors = cursor.getString(cursor.getColumnIndexOrThrow("colors"));
        return new Product(id, name, description, price, imageUrl, categoryId, sizes, colors);
    }
}

