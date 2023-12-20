import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


//algo starts

class Steganographer {

    private static int bytesForTextLengthData = 4;
    private static int bitsInByte = 8;
    // Encode
    public static String encode(String imagePath, String text) {
        BufferedImage originalImage = getImageFromPath(imagePath);                // Getting image from the given path
        BufferedImage imageInUserSpace = getImageInUserSpace(originalImage);      // Creating a copy of the image

        byte imageInBytes[] = getBytesFromImage(imageInUserSpace);                // extracting pixel values 
        byte textInBytes[] = text.getBytes();                                     // convert text to a array of its ascii values
        byte textLengthInBytes[] = getBytesFromInt(textInBytes.length);           // 
        try {
            encodeImage(imageInBytes, textLengthInBytes,  0);
            encodeImage(imageInBytes, textInBytes, bytesForTextLengthData*bitsInByte);
        }
        catch (Exception exception) {
            return "Couldn't hide text in image. Error: " + exception;
        }

        String fileName = imagePath;
        int position = fileName.lastIndexOf(".");
        if (position > 0) {
            fileName = fileName.substring(0, position);
        }

        String finalFileName = fileName + "D.png";
        saveImageToPath(imageInUserSpace, new File(finalFileName),"png");
        return "Successfully encoded text";
    }

    public static byte[] encodeImage(byte[] image, byte[] addition, int offset) {
        if (addition.length + offset > image.length) {
            throw new IllegalArgumentException("Image file is not long enough to store provided text");
        }
        for (int i=0; i<addition.length; i++) {
            int additionByte = addition[i];
            for (int bit=bitsInByte-1; bit>=0; --bit, offset++) {
                int b = (additionByte >>> bit) & 0x1;
                image[offset] = (byte)((image[offset] & 0xFE) | b);
            }
        }
        return image;
    }


    // Decode

    public static String decode(String imagePath) {
        byte[] decodedHiddenText;
        try {
            BufferedImage imageFromPath = getImageFromPath(imagePath);
            BufferedImage imageInUserSpace = getImageInUserSpace(imageFromPath);
            byte imageInBytes[] = getBytesFromImage(imageInUserSpace);
            decodedHiddenText = decodeImage(imageInBytes);
            String hiddenText = new String(decodedHiddenText);
            //String outputFileName = "hidden_text.txt";
            //saveTextToPath(hiddenText, new File(outputFileName));
            //System.out.println("Successfully extracted text to: " + hiddenText);
            return hiddenText;
        } catch (Exception exception) {
            return "No hidden message. Error: " + exception;
        }
    }

    public static byte[] decodeImage(byte[] image) {
        int length = 0;
        int offset  = bytesForTextLengthData*bitsInByte;

        for (int i=0; i<offset; i++) {
            length = (length << 1) | (image[i] & 0x1);
        }

        byte[] result = new byte[length];

        for (int b=0; b<result.length; b++ ) {
            for (int i=0; i<bitsInByte; i++, offset++) {
                result[b] = (byte)((result[b] << 1) | (image[offset] & 0x1));
            }
        }
        return result;
    }


    // File I/O methods

    public static void saveImageToPath(BufferedImage image, File file, String extension) {
        try {
            file.delete();
            ImageIO.write(image, extension, file);
        } catch (Exception exception) {
            System.out.println("Image file could not be saved. Error: " + exception);
        }
    }

    public static BufferedImage getImageFromPath(String path) {
        BufferedImage image	= null;
        File file = new File(path);
        try {
            image = ImageIO.read(file);
        } catch (Exception exception) {
            System.out.println("Input image cannot be read. Error: " + exception);
        }
        return image;
    }

    public static BufferedImage getImageInUserSpace(BufferedImage image) {
        BufferedImage imageInUserSpace  = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = imageInUserSpace.createGraphics();
        graphics.drawRenderedImage(image, null);
        graphics.dispose();
        return imageInUserSpace;
    }

    public static byte[] getBytesFromImage(BufferedImage image) {
        WritableRaster raster = image.getRaster();
        DataBufferByte buffer = (DataBufferByte)raster.getDataBuffer();
        return buffer.getData();
    }

    public static byte[] getBytesFromInt(int integer) {
        return ByteBuffer.allocate(bytesForTextLengthData).putInt(integer).array();
    }

}


public class Welcome extends Application {
    private String username;
    private Connection connection;
    private static final Logger LOGGER = Logger.getLogger(log.class.getName());
    Steganographer steganographer;
    @Override
    public void start(Stage stage) throws Exception {
        try {
            FileHandler fileHandler = new FileHandler("log.txt");
            SimpleFormatter simpleFormatter = new SimpleFormatter();
            fileHandler.setFormatter(simpleFormatter);
            LOGGER.addHandler(fileHandler);
            LOGGER.setLevel(Level.INFO);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up logger", e);
        }
        Alert a = new Alert(Alert.AlertType.NONE);
        Image welcomepage = new Image("Image_path");
        Image loginpage = new Image("Image_path");
        Image signinpage = new Image("Image_path");
        Image aboutuspage= new Image("Image_path");
        Image encryptpage = new Image("Image_path");
        Image decryptpage = new Image("Image_path");

        Button login1 = new Button("LOGIN");
        Button signin1 = new Button("SIGN IN");
        BackgroundImage backgroundImage1 = new BackgroundImage(welcomepage,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);

        BackgroundImage backgroundImage2 = new BackgroundImage(loginpage,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);

        BackgroundImage backgroundImage3 = new BackgroundImage(signinpage ,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);

        BackgroundImage backgroundImage4 = new BackgroundImage(aboutuspage,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);

        BackgroundImage backgroundImage5 = new BackgroundImage(encryptpage,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);

        BackgroundImage backgroundImage6 = new BackgroundImage(decryptpage,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);

        Background background1 = new Background(backgroundImage1);
        Background background2 = new Background(backgroundImage2);
        Background background3 = new Background(backgroundImage3);
        Background background4 = new Background(backgroundImage4);
        Background background5 = new Background(backgroundImage5);
        Background background6 = new Background(backgroundImage6);;

        GridPane grid1 = new GridPane();
        GridPane grid2 = new GridPane();
        GridPane grid3 = new GridPane();
        GridPane grid4 = new GridPane();
        GridPane grid5 = new GridPane();
        GridPane grid6 = new GridPane();

        grid1.setBackground(background1);
        grid2.setBackground(background2);
        grid3.setBackground(background3);
        grid4.setBackground(background4);
        grid5.setBackground(background5);
        grid6.setBackground(background6);

        grid1.setAlignment(Pos.CENTER);
        grid2.setAlignment(Pos.CENTER);
        grid3.setAlignment(Pos.CENTER);
        grid4.setAlignment(Pos.CENTER);
        grid5.setAlignment(Pos.CENTER);
        grid6.setAlignment(Pos.CENTER);

        grid1.setHgap(80);
        grid1.setVgap(80);
        grid2.setHgap(20);
        grid2.setVgap(20);
        grid3.setHgap(20);
        grid3.setVgap(20);
        grid4.setHgap(30);
        grid4.setVgap(30);
        grid5.setHgap(20);
        grid5.setVgap(20);
        grid6.setHgap(20);
        grid6.setVgap(20);

        grid1.setPadding(new Insets(25, 25, 25, 25));
        grid2.setPadding(new Insets(25, 25, 25, 25));
        grid3.setPadding(new Insets(25, 25, 25, 25));
        grid4.setPadding(new Insets(25, 25, 25, 25));
        grid5.setPadding(new Insets(25, 25, 25, 25));
        grid6.setPadding(new Insets(25, 25, 25, 25));


        Button loginButton1 = new Button("LOGIN");
        Button signupButton1 = new Button("SIGN UP");
        Button signupButton2 = new Button("Signup");
        Button clearButton2 = new Button("Clear");
        Button loginButton3 = new Button("Login");
        Button clearButton3 = new Button("Clear");
        Button encryptButton4 = new Button(("Encrypt"));
        Button decryptButton4 = new Button(("Decrypt"));
        Button logoutButton4 = new Button(("LOG OUT"));
        Button uploadButton5 = new Button("Upload File");
        Button encryptButton5 = new Button("Encrypt");
        Button clearButton5 = new Button("Clear");
        Button backButton5 = new Button("Back");
        Button uploadButton6 = new Button("Upload File");
        Button decryptButton6 = new Button("Decrypt");
        Button clearButton6 = new Button("Clear");
        Button backButton6 = new Button("Back");

        loginButton1.setPrefWidth(80);
        loginButton1.setPrefHeight(50);
        signupButton1.setPrefWidth(80);
        signupButton1.setPrefHeight(50);

        Label welcome = new Label("WELCOME");
        Label usernameLable2 = new Label("Username ");
        Label mailLable2 = new Label("E-Mail ID ");
        Label passwordLable2 = new Label("Password ");
        Label cpasswordLable2 = new Label("Confirm Password ");
        Label usernameLable3 = new Label("Username ");
        Label passwordLable3 = new Label("Password ");
        Label aboutus = new Label("About Us");
        Label desc = new Label("We are a team of developers.");
        Label textLable4 = new Label("Text");

        welcome.setStyle("-fx-text-fill: white;");
        usernameLable2.setStyle("-fx-text-fill: white;");
        mailLable2.setStyle("-fx-text-fill: white;");
        passwordLable2.setStyle("-fx-text-fill: white;");
        cpasswordLable2.setStyle("-fx-text-fill: white;");
        usernameLable3.setStyle("-fx-text-fill: white;");
        passwordLable3.setStyle("-fx-text-fill: white;");
        aboutus.setStyle("-fx-text-fill: white;");
        desc.setStyle("-fx-text-fill: white;");
        textLable4.setStyle("-fx-text-fill: white;");

        welcome.setFont(Font.font("Arial", 50));
        usernameLable2.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        mailLable2.setFont(Font.font("Arial", FontWeight.BOLD,20));
        passwordLable2.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        cpasswordLable2.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        usernameLable3.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        passwordLable3.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        aboutus.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        desc.setFont(Font.font("Arial", FontWeight.BOLD, 17));
        textLable4.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        TextField usernameField2 = new TextField();
        TextField mailField2 = new TextField();
        PasswordField passwordField2 = new PasswordField();
        PasswordField cpasswordField2 = new PasswordField();
        TextField usernameField3 = new TextField();
        PasswordField passwordField3 = new PasswordField();
        TextField pathField5 = new TextField();
        TextField value5 = new TextField();
        TextField pathField6 = new TextField();
        pathField5.setEditable(false);
        pathField6.setEditable(false);

        //welcome page

        grid1.addRow(0,welcome);
        grid1.addRow(1,loginButton1,signupButton1);

        //Sign up page

        grid2.addRow(0,usernameLable2,usernameField2);
        grid2.addRow(1,mailLable2,mailField2);
        grid2.addRow(2,passwordLable2,passwordField2);
        grid2.addRow(3,cpasswordLable2,cpasswordField2);
        grid2.addRow(4,signupButton2,clearButton2);

        grid3.addRow(0,usernameLable3,usernameField3);
        grid3.addRow(1,passwordLable3,passwordField3);
        grid3.addRow(2,loginButton3,clearButton3);

        grid4.addRow(0,aboutus);
        grid4.addRow(1,desc);
        grid4.addRow(2,encryptButton4,decryptButton4);
        grid4.addRow(3,logoutButton4);

        grid5.addRow(0,uploadButton5,pathField5);
        grid5.addRow(1,textLable4,value5);
        grid5.addRow(2,encryptButton5,clearButton5);
        grid5.addRow(3,backButton5);

        grid6.addRow(0,uploadButton6,pathField6);
        grid6.addRow(1,decryptButton6,clearButton6);
        grid6.addRow(2,backButton6);


        stage.setTitle("STEGANOGRAPHER");
        Scene scene1 = new Scene(grid1, 1120, 760);
        Scene scene2 = new Scene(grid2, 1120, 760);
        Scene scene3 = new Scene(grid3, 1120, 760);
        Scene scene4 = new Scene(grid4, 1120, 760);
        Scene scene5 = new Scene(grid5, 1120, 760);
        Scene scene6 = new Scene(grid6, 1120, 760);
        stage.setScene(scene1);
        stage.show();

        loginButton1.setOnAction(e-> stage.setScene(scene3));
        signupButton1.setOnAction(e->stage.setScene(scene2));

        clearButton2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                usernameField2.clear();
                mailField2.clear();
                passwordField2.clear();
                cpasswordField2.clear();
            }
        });
        signupButton2.setOnAction(e -> {
            username = usernameField2.getText();
            String mail = mailField2.getText();
            String password = passwordField2.getText();
            String confirmPassword = cpasswordField2.getText();
            /*boolean b= !confirmPassword.equals(password);
            System.out.println(b);*/
            if (!confirmPassword.equals(password)) {
                a.setAlertType(Alert.AlertType.INFORMATION);
                a.setContentText("Passwords do not match.");
                a.show();
            }
            if (createUser(username,mail, password)) {
                a.setAlertType(Alert.AlertType.INFORMATION);
                a.setContentText("User created successfully!");
                a.show();
                stage.setScene(scene1);
            } else {
                a.setAlertType(Alert.AlertType.INFORMATION);
                a.setContentText("User creation failed.");
                a.show();
            }
            //a.close();
            //stage.setScene(scene2);
        });

        loginButton3.setOnAction(e -> {
            username = usernameField3.getText();
            String password = passwordField3.getText();
            if (isValidCredentials(username, password)) {
                a.setAlertType(Alert.AlertType.INFORMATION);
                a.setContentText("Login successful!");
                a.show();
                stage.setScene(scene4);
            } else {
                a.setAlertType(Alert.AlertType.INFORMATION);
                a.setContentText("Invalid username or password.");
                a.show();
                usernameField3.clear();
                passwordField3.clear();
                stage.setScene(scene1);
            }
        });

        clearButton3.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                usernameField3.clear();
                passwordField3.clear();
            }
        });
        logoutButton4.setOnAction(e-> {
            stage.setScene(scene1);
            LOGGER.info("Process Completed Successfully.");
        });
        encryptButton4.setOnAction(e->stage.setScene(scene5));
        decryptButton4.setOnAction(e->stage.setScene(scene6));
        uploadButton5.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                pathField5.setText(selectedFile.getAbsolutePath());
            }
        });
        encryptButton5.setOnAction(e->{
            String path = pathField5.getText();
            String data = value5.getText();
            String msg = steganographer.encode(path,data);
            a.setContentText(msg);
            a.setAlertType(Alert.AlertType.INFORMATION);
            a.show();
        });
        clearButton5.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                value5.clear();
                pathField5.clear();
            }
        });
        backButton5.setOnAction(e->stage.setScene(scene4));
        uploadButton6.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                pathField6.setText(selectedFile.getAbsolutePath());
            }
        });
        decryptButton6.setOnAction(e->{
            String path1 = pathField6.getText();
            String msg = steganographer.decode(path1);
            a.setHeight(533);
            a.setWidth(533);
            a.setContentText(msg);
            a.setAlertType(Alert.AlertType.INFORMATION);
            a.show();
        });
        clearButton6.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                pathField6.clear();
            }
        });
        backButton6.setOnAction(e->stage.setScene(scene4));
    }

    private boolean createUser(String username, String mail, String password) {
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO user_details VALUES (?, ?, ?)");
            statement.setString(1, username);
            statement.setString(2, mail);
            String password1 = hash(password);
            statement.setString(3, password1);
            int result = statement.executeUpdate();
            return result == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    private boolean isValidCredentials(String username, String password) {
        try {
            String password1 = hash(password);
            System.out.print
            ln(password1);
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM user_details WHERE username = ? AND password = ?");
            statement.setString(1, username);
            statement.setString(2, password1);
            ResultSet result = statement.executeQuery();
            return result.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static String hash(String input) {
        String key = "2,147,483,647";
        String output = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(key.getBytes());
            byte[] bytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            output = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return output;
    }
    @Override
    public void init() {
        try {
            String url = "url";
            String username = "username";
            String password = "password";
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    @Override
    public void stop() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
