package com.fenghuaxz.ipay.utils;

import java.io.*;

public class FileUtils {

    @SuppressWarnings("unchecked")
    public static <T> T readFromFile(File file, T def) {
        if (!file.exists()) {
            return def;
        }

        try {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                return (T) ois.readObject();
            }
        } catch (Exception e) {
            return def;
        }
    }

    public static void saveToFile(File file, Object obj) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(obj);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            throw new RuntimeException("saveToFile", e);
        }
    }
}
