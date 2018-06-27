package life.qbic.app;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.treez.javafxd3.d3.D3;
import org.treez.javafxd3.d3.core.Selection;
import org.treez.javafxd3.d3.demo.DemoCase;
import org.treez.javafxd3.d3.demo.DemoFactory;
import org.treez.javafxd3.javafx.JavaFxD3Browser;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import life.qbic.expdesign.SamplePreparator;
import life.qbic.expdesign.model.ExperimentalDesignType;
import life.qbic.expdesign.model.StructuredExperiment;

/**
 * Stand alone tool to display imported experimental designs according to their experimental
 * factors.
 */
public class ExperimentGraphApp extends Application {

  final private SamplePreparator expParser = new SamplePreparator();
  private StructuredExperiment currentProject;

  /**
   * The JavaFx main scene
   */
  private Scene scene;

  /**
   * The content of the scene
   */
  private StackPane sceneContent;

  /**
   * The java fx browser node
   */
  private JavaFxD3Browser browser;

  /**
   * The currently active view
   */
  private DemoCase currentView;

  /**
   * Main
   * 
   * @param args
   */
  public static void main(String[] args) {
    launch(args);
  }

  /**
   * This is the entry point method.
   */
  @Override
  public void start(Stage stage) {

    final FileChooser fileChooser = new FileChooser();

    final Button openButton = new Button("Load experimental design");
    final ComboBox<String> factors = new ComboBox<String>();

    final Pane rootGroup = new VBox(12);
    rootGroup.getChildren().addAll(openButton, factors);
    rootGroup.setPadding(new Insets(12, 12, 12, 12));

    // create content node
    sceneContent = new StackPane();
    HBox hBox = new HBox();
    sceneContent.getChildren().add(hBox);
    List<Node> hBoxChildren = hBox.getChildren();

    VBox demoMenuBox = new VBox();
    // demoMenuBox.setPrefWidth(DEMO_BUTTON_WIDTH);
    hBoxChildren.add(demoMenuBox);
    demoMenuBox.getChildren().add(rootGroup);

    // create box for preferences of active demo
    VBox demoPreferenceBox = new VBox();
    demoPreferenceBox.setStyle("-fx-background-color: steelblue");
    hBoxChildren.add(demoPreferenceBox);

    // define what to do after the browser has initialized
    Runnable afterBrowserLoadingHook = () -> {
      runDemoSuite(stage, demoMenuBox, demoPreferenceBox, factors);
    };
    // create browser
    browser = new JavaFxD3Browser(afterBrowserLoadingHook, true); // set true for firebug debug
    // add browser
    hBoxChildren.add(browser);

    openButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(final ActionEvent e) {
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
          factors.getItems().clear();
          try {
            expParser.processTSV(file, ExperimentalDesignType.Standard, true);
            currentProject = expParser.getSampleGraph();

            if (currentProject.getFactorsToSamples().isEmpty()) {
              System.out.println("is empty");
              showPopupMessage("Experimental design could not be parsed.", stage);
            }


            factors.setItems(
                FXCollections.observableArrayList(currentProject.getFactorsToSamples().keySet()));
          } catch (IOException | JAXBException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
          }
        }
      }
    });

    // create and show the scene
    final int sceneWidth = 1200;
    final int sceneHeight = 800;
    final Color sceneColor = Color.web("#666970");
    scene = new Scene(sceneContent, sceneWidth, sceneHeight, sceneColor);
    stage.setScene(scene);
    stage.show();
  }

  private void showPopupMessage(String message, Stage stage) {
    VBox dialogVbox = new VBox(20);
    Label l = new Label(message);

    dialogVbox.setSpacing(5);
    dialogVbox.setPadding(new Insets(10, 0, 0, 10));
    dialogVbox.getChildren().addAll(l);
      Scene dialogScene = new Scene(dialogVbox, 300, 150);
      final Stage dialog = new Stage();
      dialog.initModality(Modality.APPLICATION_MODAL);
      dialog.initOwner(stage);
      dialog.setScene(dialogScene);
      dialog.show();
  }
  
  private void runDemoSuite(Stage stage, VBox buttonPane, VBox demoPreferenceBox,
      ComboBox<String> factors) {
    D3 d3 = browser.getD3();

    // set stage title
    String versionString = "D3 API version: " + d3.version();
    String title = "Welcome to javax-d3 : A thin Java wrapper around d3." + versionString;
    stage.setTitle(title);

    factors.valueProperty().addListener(new ChangeListener<String>() {

      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue,
          String newValue) {
        stopCurrentView();
        createAndStartNewView(ExperimentGraph.factory(d3, demoPreferenceBox,
            currentProject.getFactorsToSamples().get(newValue), stage));
      }

      private void stopCurrentView() {
        if (currentView != null) {
          currentView.stop();
          currentView = null;
        }
      }

      private void createAndStartNewView(final DemoFactory demoClass) {
        clearContent();
        DemoCase demo = demoClass.newInstance();
        currentView = demo;
        demo.start();
      }
    });
  }

  /**
   * Clears the content of the root element and returns the root as Selection
   * 
   * @return
   */
  public Selection clearContent() {

    D3 d3 = browser.getD3();
    d3.selectAll("svg").remove();
    d3.select("#root").selectAll("*").remove();
    d3.select("head").selectAll("link").remove();

    Selection svg = d3.select("#root") //
        .append("svg") //
        .attr("id", "svg");
    return svg;
  }
}
