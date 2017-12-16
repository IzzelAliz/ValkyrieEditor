package com.ilummc.valkyrie;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class ValkyrieEditor extends Application {

    private static ValkyrieEditor instance;

    @FXML
    TextField folderField, missingField, exceedField, timeoutMsgField, timeoutField;

    @FXML
    Button generate;

    @FXML
    TextArea output;

    @FXML
    public void onLoad() {
        generate.requestFocus();
    }

    @FXML
    public void onClick() {
        generate.setText("正在计算 MD5 并生成，请耐心等待");
        new Thread(() -> {
            List<File> files = fetchFiles(new ArrayList<>(), new File(folderField.getText()));
            StringBuilder builder = new StringBuilder("optional:\n");
            for (File file : files) {
                try {
                    builder.append("    # ").append(file.getName()).append("\n").append("  - ").append(md5(file)).append("\n");
                } catch (Exception ignored) {
                }
            }
            builder.append("required: []\n");
            builder.append("mod-missing-msg: '").append(missingField.getText().replace("'", "/'")).append("'\n");
            builder.append("mod-exceeded-msg: '").append(exceedField.getText().replace("'", "/'")).append("'\n");
            builder.append("auth-timeout-msg: '").append(timeoutMsgField.getText().replace("'", "/'")).append("'\n");
            builder.append("timeout: ").append(timeoutField.getText()).append("\noutput: true\nversion: 1");
            Platform.runLater(() -> {
                output.setText(builder.toString());
                generate.setText("点击重新生成");
                output.requestFocus();
            });
        }).start();
    }

    private String md5(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[1024];
        int length = -1;
        while ((length = fis.read(buffer, 0, 1024)) != -1) {
            md.update(buffer, 0, length);
        }
        BigInteger bigInt = new BigInteger(1, md.digest());
        fis.close();
        return bigInt.toString(16);
    }

    public List<File> fetchFiles(List<File> container, File folder) {
        try {
            if (folder == null || !folder.isDirectory())
                throw new NullPointerException("指定目录不是文件夹或不存在");
            BlockingDeque<File> todo = new LinkedBlockingDeque<>();
            Collections.addAll(todo, folder.listFiles());
            while (!todo.isEmpty()) {
                File file = todo.takeFirst();
                if (file.isDirectory()) Collections.addAll(todo, file.listFiles());
                else if (file.getName().endsWith(".jar") || file.getName().endsWith(".zip") ||
                        file.getName().endsWith(".class") || file.getName().endsWith(".litemod")) {
                    container.add(file);
                }
            }
        } catch (Exception e) {
            output.setText(e.toString() + "\n" + e.getStackTrace()[0].toString()
                    + "\n" + e.getStackTrace()[1].toString());
        }
        return container;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
        primaryStage.setTitle("ValkyrieEditor");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setOnCloseRequest(event -> {
            System.exit(0);
        });
        primaryStage.getIcons().add(new Image(ValkyrieEditor.class.getResourceAsStream("/PCD.png")));
        primaryStage.show();
    }
}
