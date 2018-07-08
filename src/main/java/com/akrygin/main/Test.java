package com.akrygin.main;

import com.akrygin.main.service.ImageService;
import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException {
        ImageService.downloadImageAndGetLocalURL("https://static.svyaznoy.ru/upload/iblock/1f8/iphone7_plus_2up_matblk_ww-en-print-woadl.jpg/resize/307x224/", "3279665");
    }
}