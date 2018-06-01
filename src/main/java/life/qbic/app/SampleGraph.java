package life.qbic.app;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import life.qbic.expdesign.SamplePreparator;
import life.qbic.expdesign.model.ExperimentalDesignType;
import life.qbic.expdesign.model.StructuredExperiment;
import netscape.javascript.JSObject;
import picocli.CommandLine.Command;

/**
 * Stand alone tool to display imported experimental designs according to their experimental
 * factors.
 */
@Command(name = "Experiment Graph",
    description = "Displays sample graph of imported experimental designs according to their experimental factors.")
public class SampleGraph extends Application {

  private static final Logger LOG = LogManager.getLogger(SampleGraph.class);

  private static final String VERSION;
  private static final String PROJECT_URL;
  static {
    final Properties properties = new Properties();
    try (final InputStream inputStream =
        SampleGraph.class.getClassLoader().getResourceAsStream("tool.properties")) {
      properties.load(inputStream);
    } catch (Exception e) {
      LOG.warn("Could not load tools.properties file. Loading default version/project-url.", e);
    }
    VERSION = properties.getProperty("version", "1.0.0-SNAPSHOT");
    PROJECT_URL = properties.getProperty("project.url", "http://github.com/qbicsoftware");
  }
  final private SamplePreparator expParser = new SamplePreparator();
  private WebEngine engine;
  private StructuredExperiment currentProject;
  /**
   * The JavaFx main scene
   */
  private Scene scene;

  /**
   * The content of the scene
   */
  private StackPane sceneContent;
  private static Stage globalStage;
  private final static int DEMO_BUTTON_WIDTH = 180;

  /**
   * Main
   * 
   * @param args no args
   */
  public static void main(String[] args) {
    launch(args);
  }

  /**
   * Displays a list of sample identifiers in a new Dialog Window.
   * @author Andreas Friedrich
   *
   */
  public static class TableReporter {
    /**
     * Method called by JavaScript when clicking on a node.
     * @param label Label of the clicked node
     * @param names Names/identifiers of samples that are part of the clicked node
     */
    public void sendNames(String label, String names) {
      final Stage dialog = new Stage();
      dialog.initModality(Modality.APPLICATION_MODAL);
      dialog.initOwner(globalStage);
      VBox dialogVbox = new VBox(20);
      // dialogVbox.getChildren().add(new Text(label+": "+names));
      Scene dialogScene = new Scene(dialogVbox, 300, 400);
      dialog.setScene(dialogScene);
      dialog.show();

      Label l = new Label(label);
      ListView<String> list = new ListView<String>();
      list.setMaxWidth(250);

      ObservableList<String> data = FXCollections.observableArrayList(names.split(","));
      list.setItems(data);

      dialogVbox.setSpacing(5);
      dialogVbox.setPadding(new Insets(10, 0, 0, 10));
      dialogVbox.getChildren().addAll(l, list);
    }
  }

  /**
   * This is the entry point method.
   * 
   * @throws MalformedURLException
   */
  @Override
  public void start(Stage stage) throws MalformedURLException {
    globalStage = stage;
    final FileChooser fileChooser = new FileChooser();

    final Button openButton = new Button("Open experimental design...");
    final ComboBox<String> factors = new ComboBox<String>();
    factors.valueProperty().addListener(new ChangeListener<String>() {

      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue,
          String newValue) {
        Gson gson = new Gson();
        String json = gson.toJson(currentProject.getFactorsToSamples().get(newValue));
        engine.executeScript("init_graph_circles(" + json + ")");
      }

    });

    openButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(final ActionEvent e) {
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
          factors.getItems().clear();
          try {
            expParser.processTSV(file, ExperimentalDesignType.Standard);
            currentProject = expParser.getSampleGraph();
            factors.setItems(
                FXCollections.observableArrayList(currentProject.getFactorsToSamples().keySet()));

          } catch (IOException | JAXBException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
          }
        }
      }
    });

    final Pane rootGroup = new VBox(12);
    rootGroup.getChildren().addAll(openButton, factors);
    rootGroup.setPadding(new Insets(12, 12, 12, 12));


    // create content node
    sceneContent = new StackPane();
    HBox hBox = new HBox();
    sceneContent.getChildren().add(hBox);
    List<Node> hBoxChildren = hBox.getChildren();

    // create box for demo case menu buttons
    VBox demoMenuBox = new VBox();
    demoMenuBox.setPrefWidth(DEMO_BUTTON_WIDTH);
    hBoxChildren.add(demoMenuBox);
    demoMenuBox.getChildren().add(rootGroup);

    String title = "Experimental Design Graph " + VERSION;
    stage.setTitle(title);

    WebView view = new WebView();
    engine = view.getEngine();

    JSObject js = (JSObject) engine.executeScript("window");
    js.setMember("test", new TableReporter());

    // get graph html
    URL html = getClass().getResource("/index.html");
    engine.load(html.toExternalForm());
    hBoxChildren.add(view);

    // create and show the scene
    final int sceneWidth = 1200;
    final int sceneHeight = 800;
    final Color sceneColor = Color.web("#666970");
    scene = new Scene(sceneContent, sceneWidth, sceneHeight, sceneColor);
    stage.setScene(scene);
    stage.show();
  }
}
