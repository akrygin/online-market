package com.akrygin.main.service;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class ImageService {

    public static String downloadImageAndGetLocalURL(String imageURL, String itemID) {
        URL url;
        URLConnection conn;
        File image;
        FileOutputStream fos;
        String result = "src/main/webapp/images/items-images/";
        try {
            url = new URL(imageURL);
            conn = url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
            image = new File(result + itemID + ".png");
            fos = new FileOutputStream(image);
            int ch;
            while ((ch = bis.read()) != -1) {
                fos.write((char) ch);
            }
            bis.close();
            fos.flush();
            fos.close();
        } catch (IOException e) {
            //ToDo logger
            e.printStackTrace();
        }
        return "./images/items-images/" + itemID + ".png";
    }
}