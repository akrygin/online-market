package com.akrygin.main;

import com.akrygin.bean.ItemBean;
import com.akrygin.bean.ProducerBean;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.*;
import java.util.*;

import static com.akrygin.main.service.ImageService.downloadImageAndGetLocalURL;

public class RunParser {
    private final static Logger logger = Logger.getLogger(RunParser.class);
    private static final String LINK = "https://www.svyaznoy.ru/catalog/phone/224/page-%s?ORDER_BY=-price";
    private static final String ITEM_LINK = "https://www.svyaznoy.ru/catalog/phone/224/%s#mainContent";
    private static final String ITEM_LINK_SPECS = "https://www.svyaznoy.ru/catalog/phone/224/%s/specs#mainContent";
    private static final String FILENAME = "src/main/webapp/index.html";
    private static List<ItemBean> itemsOnPage = new ArrayList<ItemBean>();
    private static PhantomJSDriver phantomJSDriver;
    private static List<String> brands;
    private static Properties properties;
    private static Set<ProducerBean> producers = new LinkedHashSet<>();

    public static void main(String[] args) throws IOException {
        System.setProperty("phantomjs.binary.path", "src/main/resources/libs/phantomjs/bin/phantomjs.exe");
        DesiredCapabilities dcap = new DesiredCapabilities();
        String[] phantomArgs = new String[]{
                "--webdriver-loglevel=NONE"
        };
        dcap.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);
        phantomJSDriver = new PhantomJSDriver(dcap);
        logger.info("Start properties init");
        propertiesInit();
        logger.info("Properties initialized");
        logger.info("Start getting elements for generation");
        Elements elements = getElements(phantomJSDriver, Integer.valueOf(properties.get("number_of_items").toString()));
        logger.info("Elements collected, starting generation process");
        getDataAndGeneratePagesForItems(elements);
        logger.info("Generation of main page");
        generateMainPage();
        logger.info("Parser finished");
    }

    private static Elements getElements(PhantomJSDriver phantomJSDriver, int numberOfItems) {
        Elements result = new Elements();
        Document firstPage = getData(phantomJSDriver, String.format(LINK, 1));
        Elements items = firstPage.select("[data-key]");
        int numberOfPages = Math.abs(numberOfItems / items.size()) + 1;
        Elements itemsFromAllNeededPages = new Elements();
        for (int i = 1; i < numberOfPages + 1; i++){
            Document page = getData(phantomJSDriver, String.format(LINK, i));
            itemsFromAllNeededPages.addAll(page.select("[data-key]"));
        }
        int resultNumberOfItems = 0;
        for (Element item : itemsFromAllNeededPages) {
            if (numberOfItems > resultNumberOfItems){
                result.add(item);
                resultNumberOfItems++;
            }
        }
        return result;
    }

    private static void getDataAndGeneratePagesForItems(Elements items) throws IOException {
        for (Element item : items) {
            String dataId = item.attr("data-key");
            String name = item.select("[itemprop=name]").get(0).childNode(0).attr("#text");
            String price = item.select("*.b-product-block__visible-price").get(0).childNode(0).attr("#text");
            String imageURL = getImageURL(item);
            ItemBean itemBean = new ItemBean(dataId, name, price, downloadImageAndGetLocalURL(imageURL, dataId));
            itemBean.setAbout(getItemAbout(dataId));
            itemBean.setProducer(getItemProducer(name));
            itemBean.setCharacteristics(getItemCharacteristics(dataId));
            String brandName = getItemProducer(name);
            producers.add(new ProducerBean(brandName, "", brandName));
            itemBean.setItemURL(dataId + ".html");
            itemsOnPage.add(itemBean);
            generatePageForItem(itemBean);
        }
    }

    private static String getImageURL(Element item) {
        String variant1 = item.select("[itemprop=contentUrl]").get(0).attr("data-original");
        String variant2 = item.select("[itemprop=contentUrl]").get(0).attr("src");
        if (!variant1.isEmpty()){
            return variant1;
        } else if (!variant2.isEmpty()){
            return variant2;
        }
        return "";
    }

    private static void generatePageForItem(ItemBean itemBean) throws IOException {
        Velocity.init();
        VelocityContext vc = new VelocityContext();
        vc.put("itemBean", itemBean);
        Template t = Velocity.getTemplate("./src/main/resources/templates/item.vm", "utf-8");
        FileWriter fw = new FileWriter("src/main/webapp/items/" + itemBean.getItemURL());
        BufferedWriter bw = new BufferedWriter(fw);
        t.merge(vc, bw);
        logger.info("Generated template for " + itemBean.getDataId());
        bw.flush();
        bw.close();
    }

    private static String getItemProducer(String name) {
        for (String brand : brands) {
            if (name.contains(brand)){
                return brand;
            }
        }
        return "No Brand";
    }

    private static String getItemAbout(String dataId) {
        Document itemData = getData(phantomJSDriver, String.format(ITEM_LINK, dataId));
        return itemData.select(".b-product-view-about__text-block").get(0).toString();
    }

    private static String getItemCharacteristics(String dataId) {
        Document itemData = getData(phantomJSDriver, String.format(ITEM_LINK_SPECS, dataId));
        Element element = itemData.select(".b-product-view-box__left-column").get(0);
        removeAllUnnecessaryData(element);
        return element.toString();
    }

    private static void removeAllUnnecessaryData(Element element) {
        element.select(".b-comment").remove();
        element.select(".b-faq").remove();
    }

    private static void generateMainPage() throws IOException {
        Velocity.init();
        VelocityContext vc = new VelocityContext();
        vc.put("itemsOnPage", itemsOnPage);
        vc.put("producers", producers);
        Template t = Velocity.getTemplate("./src/main/resources/templates/main.vm", "utf-8");
        FileWriter fw = new FileWriter(FILENAME);
        BufferedWriter bw = new BufferedWriter(fw);
        t.merge(vc, bw);
        bw.flush();
        bw.close();
    }

    private static Document getData(PhantomJSDriver phantomJSDriver, String link) {
        phantomJSDriver.get(link);
        return Jsoup.parse(phantomJSDriver.getPageSource());
    }
    
    private static void propertiesInit() throws IOException {
        FileReader input = new FileReader("src/main/resources/brands.properties");
        BufferedReader bufRead = new BufferedReader(input);
        String strLine;
        brands = new ArrayList<>();
        while ((strLine = bufRead.readLine()) != null)
        {
            brands.add(strLine);
        }
        properties = new Properties();
        FileInputStream in = new FileInputStream("src/main/resources/defaultProperties.properties");
        properties.load(in);
        in.close();
    }
}