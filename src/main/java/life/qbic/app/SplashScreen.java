package life.qbic.app;

import javafx.application.Preloader;
import javafx.application.Preloader.StateChangeNotification.Type;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SplashScreen extends Preloader {
  private Stage preloaderStage;

  @Override
  public void start(Stage primaryStage) throws Exception {
    this.preloaderStage = primaryStage;

    Label copyright = new Label("Copyright (c) 2018 Andreas Friedrich\n\n" + "ISA-Tab Parser:\n"
        + "Copyright (c) 2008-2017 ISA Team\n" + "Developed by the ISA Team\n"
        + "http://www.isa-tools.org");

    Image isatools = new Image(getClass().getResourceAsStream("/isatools.png"));

    VBox loading = new VBox(20);
    loading.setMaxWidth(Region.USE_PREF_SIZE);
    loading.setMaxHeight(Region.USE_PREF_SIZE);
    
    loading.getChildren().add(new Label("Please wait..."));
    loading.getChildren().add(copyright);
    loading.getChildren().add(new ImageView(isatools));

    BorderPane root = new BorderPane(loading);
    Scene scene = new Scene(root);

    primaryStage.setWidth(800);
    primaryStage.setHeight(600);
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  @Override
  public void handleStateChangeNotification(StateChangeNotification stateChangeNotification) {
    if (stateChangeNotification.getType() == Type.BEFORE_START) {
      preloaderStage.hide();
    }
  }
}
