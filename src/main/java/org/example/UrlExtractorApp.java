package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import kotlin.Pair;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlExtractorApp extends Application {

    private TextField fileSavePathTextField;
    private TextField pathTextField;
    private TextField suffixTextField;
    private TextField startTextField;
    private TextFlow logTextFlow;
    private Button startButton;
    private Button exitButton;

    private UrlExtractService urlExtractService;
    private static String dictionary = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static Set<String> fileNames = new HashSet<>();
    private static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30000, TimeUnit.SECONDS) // 设置连接超时时间为 10 秒
            .readTimeout(50000, TimeUnit.SECONDS) // 设置读取超时时间为 30 秒
            .writeTimeout(30000, TimeUnit.SECONDS) //
            .build();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("rulai-URL提取");

        fileSavePathTextField = new TextField();
        fileSavePathTextField.setText("D:\\Java\\a\\aa\\");

        pathTextField = new TextField();
        pathTextField.setText("https://www.pgyer.com/");
        pathTextField.setEditable(false);

        suffixTextField = new TextField();
        suffixTextField.setPromptText("输入位数");
        suffixTextField.setText("4");

        startTextField = new TextField();

        startButton = new Button("开始提取");
        exitButton = new Button("清除日志记录");

        logTextFlow  = new TextFlow();
        logTextFlow.setPrefHeight(400); // 设置高度
        ScrollPane logScrollPane = new ScrollPane(logTextFlow);
        logScrollPane.setFitToWidth(true);

        HBox fileSavePathBox = new HBox(10);
        fileSavePathBox.getChildren().addAll(new Label("app保存路径: "), fileSavePathTextField);

        HBox pathBox = new HBox(5);
        pathBox.getChildren().addAll(new Label("路径（不可变）: "), pathTextField);

        HBox suffixBox = new HBox(10);
        suffixBox.getChildren().addAll(new Label("后缀（字母位数）: "), suffixTextField);

        HBox startBox = new HBox(10);
        startBox.getChildren().addAll(new Label("开始字母: "), startTextField);

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(
                fileSavePathBox,
                pathBox,
                suffixBox,
                startBox,
                startButton,
                logScrollPane,
                exitButton
        );

        Scene scene = new Scene(layout, 400, 600);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        startButton.setOnAction(e -> {
            String text = startButton.getText();
            if("开始提取".equalsIgnoreCase(text)){
                if(urlExtractService != null){
                    urlExtractService.cancel();
                }
                startButton.setText("停止");
                urlExtractService = new UrlExtractService();
                urlExtractService.start();
            } else if("停止".equalsIgnoreCase(text)){
                startButton.setText("开始提取");
                urlExtractService.cancel();
                urlExtractService = new UrlExtractService();
                urlExtractService.start();
            }

        });

        exitButton.setOnAction(e -> clearLog());
    }

    private class UrlExtractService extends Service<Void> {
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    String fileSavePathText = fileSavePathTextField.getText();
                    if (fileSavePathText.isEmpty()) {
                        log("请输入文件保存路径", null, Color.RED);
                        return null;
                    }

                    String suffixText = suffixTextField.getText();
                    if (suffixText.isEmpty()) {
                        log("请输入后缀位数", null, Color.RED);
                        startButton.setDisable(false);
                        return null;
                    }

                    String startTextFieldText = startTextField.getText();
                    if (startTextFieldText.isEmpty()) {
                        log("请输入开始字母", null, Color.RED);
                        startButton.setDisable(false);
                        return null;
                    }

                    if(!(Integer.parseInt(suffixText) == startTextFieldText.length())){
                        log("开始字母不是" + suffixText + "位数的", null, Color.RED);
                        startButton.setDisable(false);
                        return null;
                    }

                    suffixTextField.setDisable(true);
                    File file = new File(fileSavePathText);
                    if(!file.exists()){
                        file.mkdirs();
                    }

                    String[] fileName = file.list();

                    for (String f : fileName){
                        fileNames.add(f);
                    }

                    if(!fileSavePathText.endsWith("\\")){
                        fileSavePathText = fileSavePathText + "\\";
                    }
                    fileSavePathTextField.setText(fileSavePathText);
                    fileSavePathTextField.setEditable(false);

                    String prefix = pathTextField.getText();
                    try {
                        int length = Integer.parseInt(suffixText);
                        log("开始提取......", null, Color.BLUE);
                        int[] indices = new int[length];

                        boolean flag = true;
                        while (true) {
                            StringBuilder currentCombination = new StringBuilder();
                            for (int index : indices) {
                                currentCombination.append(dictionary.charAt(index));
                            }
                            String code = currentCombination.toString();

                            if(flag){
                                if(code.equalsIgnoreCase(startTextFieldText)){
                                    flag = false;
                                }
                            }
                            if(!flag){
                                if(!isExist(code)){
                                    requestUrl(prefix, code);
                                }
                            }
                            //Thread.sleep(1000);

                            int i = length - 1;
                            while (i >= 0 && indices[i] == dictionary.length() - 1) {
                                indices[i] = 0;
                                i--;
                            }
                            if (i < 0) {
                                break; // All combinations generated
                            }
                            indices[i]++;
                        }

                    } catch (NumberFormatException e) {
                        log("请输入数字", null, Color.RED);
                        startButton.setDisable(false);
                    }

                    return null;
                }
            };
        }
    }

    private boolean isExist(String code){
        for(String f : fileNames){
            if(f.contains(code)){
                return true;
            }
        }
        return false;
    }

    private void sleep(int s){
        log("等待......" + s + "秒", null, Color.BLUE);
        for(int i = 1; i <= s; i++){
            try {
                log("等待第" + i + "秒", null, Color.BLUE);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void requestUrl(String prefix, String letterCode) {
        String url = prefix + letterCode;
        log("开始请求：", url, Color.BLUE);
        while (true){
            try {
                Request request = new Request.Builder()
                        .url(url)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                        //.header("Accept-Encoding", "gzip, deflate, br")
                        .header("Accept-Language", "zh-CN,zh;q=0.9")
                        .header("Cache-Control", "max-age=0")
                        .header("Connection", "keep-alive")
                        //.header("Cookie", "aliyungf_tc=2c3b84ab4b39ba9f3bb993a1deadec3390ef8b40a3253a249f7a709e42239508; PHPSESSID=nr3p899hejtml9mu4bf8oiqh3g; _gid=GA1.2.922028848.1699545854; _gcl_au=1.1.314238471.1699545854; Hm_lvt_8748fc8d44d4c96b145053b62e3788fe=1699545888; cw_conversation=eyJhbGciOiJIUzI1NiJ9.eyJzb3VyY2VfaWQiOiI4MjA2ODk0Zi05ODc4LTRjNDItYWFlZS1jNDhkYjYxY2JlZjAiLCJpbmJveF9pZCI6MX0.kdJTepjSaGazw2jjMUyQvqCv3fOC7eO25lJtt89oRT4; _ga_C4Y982P086=deleted; _ga_C4Y982P086=deleted; __gads=ID=c4099d4ffe151cbb:T=1699885477:RT=1699975783:S=ALNI_MbLMTBnGevV8NU5V9Csq2rdOncz5A; __gpi=UID=00000c85b8e7654c:T=1699885477:RT=1699975783:S=ALNI_MbM8nvNzWTRcGdmQvjAwpapzBKrCQ; Hm_lpvt_8748fc8d44d4c96b145053b62e3788fe=1700165222; acw_tc=ac11000117002073191493060e11f07f40d389f720080d689100e194feeccf; _ga=GA1.1.1101920610.1699545854; _gat_gtag_UA_52814215_1=1; _ga_C4Y982P086=GS1.1.1700207306.43.1.1700207307.59.0.0; pgyx2_session=bNy1XauxjXUsP7%2Bs0pFy6dM8PbyxHzwYkuBEiQnEG%2FVBXHt9saWDgfrAOhfJbDdYsWl2R23nQG%2FsDIrpZD9QeGx%2BnJ633qXQng6BWVzM1YEtnk%2F6q3rFPGCD%2BGARt%2BXEcGH1r5Q%2BkjHpY6bSLWRLYWWpR%2Fp3tMRRJuNAdTuhtGTK2IdQQpPeV8s7oIUPrJwKEmOpKJhB5t8EHVkyx%2FpjPrfmbVvudUTI6zUnXUi4t%2BmwwNX5tWlXGH9XnY3b9%2BoFUq8WutBeUue5Hv%2FVitAYGl%2BKpPCNt9x2v4ailzwx4WwZpxhuumx5vuC3jHlT8DFSTqgqCMbSxueB9KeCCIaNO03lrnG%2FgJPFKIuA%2Fy7acUOamV7ZPiU6g8z%2FlLAH2rcVJT%2BaCVRGIRnbuQcGomY3Ky7HQnTHwzcDYYn1jRBjIFcCDxyifoEsVhHW7ANzxNmZfRQnlsurhVbAfGjv0DFrpNCoTOrrjLewNRfE1951utS4Xao4Q%2Behm2lMY24dMPBtXGutCslI37v6Q2v5HTLxUmjNUJQkmQxla%2FK%2FQmpyNAhD6jv8KqML9f0IVlB5LI2bb8RWNroCWdHa%2FBXGs5yNZihoh0JO6Z%2BG7%2BKoQZV2n2dzDZ9MIXBvMkuPpx2V4KHmc8xWYF%2FT1UnabYPATpmp6MjMZyXR%2BqfclvyinF7xdILpQSXsKO3UNlwDqe0FzHtbgwfPXs6r0voSrCL1iHNpoqbDegEywwA%2FdRmhWwjlGzBzatTBjygAIq8pnyEqT5T7WH95%2BFSeHDwYhoA2xyp0hN%2Bp7ogLMTml9yikyML7naYJkgA4iFBtcD31zDNacHJ5T8OzX%2B8x%2FlbYr6lPaazLcg%3D%3D")
                        .header("Host", "www.pgyer.com")
                        .header("Sec-Fetch-Dest", "document")
                        .header("Sec-Fetch-Mode", "navigate")
                        .header("Sec-Fetch-Site", "none")
                        .header("Sec-Fetch-User", "?1")
                        .header("Upgrade-Insecure-Requests", "1")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36")
                        .header("sec-ch-ua", "\"Google Chrome\";v=\"119\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\"")
                        .header("sec-ch-ua-mobile", "?0")
                        .header("sec-ch-ua-platform", "\"Windows\"")
                        .build();

                Response response = client.newCall(request).execute();

                int code = response.code();
                if (response.isSuccessful()) {
                    InputStream inputStream = response.body().byteStream();
                    String responseBody = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                    response.close();
                    if(responseBody.contains("The address you visited does not exist")) {
                        log("访问成功！URL不存在 ", url, Color.BLUE);
                        return;
                    }else if(responseBody.contains("The app has expired and developers are required to renew it in a timely manner")){
                        log("访问成功！应用已过期 ", url, Color.BLUE);
                        return;
                    }else if(responseBody.contains("The app has been removed")){
                        log("访问成功！应用程序已被删除 ", url, Color.BLUE);
                        return;
                    }else if(responseBody.contains("该应用已关闭下载")){
                        log("访问成功！应用已关闭下载 ", url, Color.BLUE);
                        return;
                    }else if(responseBody.contains("Sign Up")){
                        log("访问成功！跳转到首页了 ", url, Color.BLUE);
                        return;
                    }else if(responseBody.contains("There are no downloadable versions of the apps")){
                        log("访问成功！无可下载版本 ", url, Color.BLUE);
                        return;
                    }else if(responseBody.contains("The app has not been released yet, please be patient")){
                        log("访问成功！应用尚未发布 ", url, Color.BLUE);
                        return;
                    }else if(responseBody.contains("<p>1</p>")){
                        log("无效连接 ", url, Color.BLUE);
                        return;
                    }else if(responseBody.contains("该应用已下架")){
                        log("访问成功！应用已下架 ", url, Color.BLUE);
                        return;
                    }else if(responseBody.contains("该应用已过期，请开发人员及时续签")){
                        log("访问成功！应用已过期 ", url, Color.BLUE);
                        return;
                    }else if(responseBody.contains("Start validation")){
                        log("访问失败！需要人工校验 ", url, Color.RED);
                        sleep(20);

                    }else {
                        log("访问成功 ", url, Color.GREEN);
                        download(responseBody, prefix, letterCode);
                        return;
                    }
                } else {
                    response.close();
                    if(code == 404){
                        log("不存在 ", url, Color.BLUE);
                        return;
                    } else if(code == 503){
                        log("服务不可用 ", url, Color.RED);
                        return;
                    } else{
                        log("无效 ", url, Color.BLUE);
                        return;
                    }

                }
            }catch (Exception e){

            }

        }
    }

    private void download(String responseBody, String prefix, String letterCode) {
        String regex = "aKey\\s*=\\s*'([^']+)'";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(responseBody);

        if (matcher.find()) {
            String aKeyValue = matcher.group(1);
            System.out.println("aKey的值是：" + aKeyValue);

            // https://www.pgyer.com/app/install/c83a35235057389af1ccab8f877fc0c9
            OkHttpClient client = new OkHttpClient();

            // 创建请求对象
            Request request = new Request.Builder()
                    .url("https://www.pgyer.com/app/install/" + aKeyValue)
                    .build();

            try {
                // 执行请求
                Response response = client.newCall(request).execute();

                // 获取响应码
                int responseCode = response.code();
                System.out.println("Response Code: " + responseCode);
                if(responseCode == 200){
                    Headers headers = response.headers();
                    Iterator<Pair<String, String>> iterator =  headers.iterator();
                    boolean flag = false;
                    while (iterator.hasNext()){
                        Pair<String, String> pair = iterator.next();
                        System.out.println(pair.getFirst() + "=" + pair.getSecond());
                        if("Content-Disposition".equals(pair.getFirst())){
                            String filename = extractFileName(pair.getSecond());
                            if(StringUtils.isNotBlank(filename)){
                                flag = true;
                                filename = letterCode + "--" + filename;
                                log("开始下载文件 ", prefix + letterCode, Color.BLUE);
                                // 获取输入流
                                InputStream inputStream = response.body().byteStream();

                                fileNames.add(filename);
                                // 创建输出流
                                FileOutputStream outputStream = new FileOutputStream(fileSavePathTextField.getText().trim() + filename);

                                // 读取输入流并写入输出流
                                byte[] buffer = new byte[4096];
                                int bytesRead;
                                while ((bytesRead = inputStream.read(buffer)) != -1) {
                                    outputStream.write(buffer, 0, bytesRead);
                                }

                                // 关闭输入流和输出流
                                inputStream.close();
                                outputStream.close();
                                log("下载完成 ", prefix + letterCode, Color.BLUE);
                            }
                        }
                    }

                    if (!flag){
                        log("没有可下载的文件 ", prefix + letterCode, Color.RED);
                    }
                }else {
                    log("没有可下载的文件 ", prefix + letterCode, Color.RED);
                }
                // 关闭响应体
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
                log("没有可下载的文件 ", prefix + letterCode, Color.RED);
            }

        } else {
            System.out.println("未找到aKey的值。");
            log("没有可下载的文件 ", prefix + letterCode, Color.RED);
        }

        //找到下载链接并下载
    }

    private static String extractFileName(String input) {
        // 匹配文件名的正则表达式
        String regex = "filename=\"([^\"]+)\"";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        // 查找匹配的部分
        if (matcher.find()) {
            // 提取捕获组中的文件名
            return matcher.group(1);
        } else {
            // 如果未找到匹配项，返回空字符串或者抛出异常，具体取决于你的需求
            return "";
        }
    }

    private void clearLog() {
        Platform.runLater(() -> logTextFlow.getChildren().clear());
    }

    private void log(String message, String url, Color color) {
        Text text = null;
        if(StringUtils.isNotBlank(url)){
            text = new Text(message);
        } else {
            text = new Text(message + "\n");
        }
        text.setFill(color);
        Text text1 = text;
        Platform.runLater(() -> logTextFlow.getChildren().add(text1));
        if(StringUtils.isNotBlank(url)){
            Hyperlink hyperlink = new Hyperlink(url + "\n");
            hyperlink.setTextFill(color);
            hyperlink.setOnAction(event -> {
                // 在默认浏览器中打开URL
                getHostServices().showDocument(url);
            });
            Platform.runLater(() -> logTextFlow.getChildren().add(hyperlink));
            Platform.runLater(() -> logTextFlow.getChildren().add(new Text("\n")));
        }



    }

    public static void main(String[] args) {
        launch(args);
    }
}
