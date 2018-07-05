package life.qbic.app;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.isatools.isacreator.model.Study;
import org.treez.javafxd3.d3.D3;
import org.treez.javafxd3.d3.core.Selection;
import org.treez.javafxd3.d3.demo.DemoCase;
import org.treez.javafxd3.d3.demo.DemoFactory;
import org.treez.javafxd3.javafx.JavaFxD3Browser;

import com.sun.javafx.application.LauncherImpl;
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
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import life.qbic.expdesign.SamplePreparator;
import life.qbic.expdesign.model.ExperimentalDesignType;
import life.qbic.expdesign.model.StructuredExperiment;
import life.qbic.isatab.ISAToGraph;

/**
 * Stand alone tool to display imported experimental designs according to their experimental
 * factors.
 */
public class ExperimentGraphApp extends Application {

  final private SamplePreparator expParser = new SamplePreparator();
  private StructuredExperiment currentProject;
  private Map<Study, StructuredExperiment> isaStudies;

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
    LauncherImpl.launchApplication(ExperimentGraphApp.class, SplashScreen.class, args);
 }
  
  @Override
  public void init() throws Exception {
    Thread.sleep(3000);
  }

  /**
   * This is the entry point method.
   */
  @Override
  public void start(Stage stage) {

    final ToggleGroup designtypeGroup = new ToggleGroup();

    final FileChooser fileChooser = new FileChooser();
    final DirectoryChooser dirChooser = new DirectoryChooser();

    final Button openButton = new Button("Load experimental design");
    final ComboBox<String> studies = new ComboBox<String>();
    final ComboBox<String> factors = new ComboBox<String>();

    final Pane rootGroup = new VBox(12);

    RadioButton rb1 = new RadioButton("Upload QBiC Design");
    rb1.setToggleGroup(designtypeGroup);
    rb1.setSelected(true);
    rb1.setUserData(false);

    RadioButton rb2 = new RadioButton("Upload ISATab");
    rb2.setToggleGroup(designtypeGroup);
    rb2.setUserData(true);

    studies.setVisible(false);

    rootGroup.getChildren().addAll(rb1, rb2, openButton, studies, factors);
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
    browser = new JavaFxD3Browser(afterBrowserLoadingHook, false); // set true for firebug debug
    // add browser
    hBoxChildren.add(browser);

    designtypeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
      public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle,
          Toggle new_toggle) {
        Toggle selected = designtypeGroup.getSelectedToggle();
        if (selected != null) {
          if ((boolean) selected.getUserData()) {
            dirChooser.setTitle("Select ISATab folder");
          }
        }
      }
    });

    studies.valueProperty().addListener(new ChangeListener<String>() {

      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue,
          String newValue) {
        for (Study s : isaStudies.keySet()) {
          if (s.toString().equals(newValue)) {
            currentProject = isaStudies.get(s);
            factors.setItems(
                FXCollections.observableArrayList(currentProject.getFactorsToSamples().keySet()));
          }
        }

      }
    });

    openButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(final ActionEvent e) {

        Toggle selected = designtypeGroup.getSelectedToggle();
        if (selected != null) {
          File file = null;
          boolean isa = (boolean) selected.getUserData();
          if (isa) {
            file = dirChooser.showDialog(stage);
          } else {
            file = fileChooser.showOpenDialog(stage);
          }
          if (file != null) {
            factors.getItems().clear();
            studies.getItems().clear();
            if (isa) {
              ISAToGraph isaParser = new ISAToGraph();
              isaParser.read(file);
              isaStudies = isaParser.getGraphsByStudy();
              studies.setVisible(true);
              List<String> studyNames = new ArrayList<String>();
              for (Study study : isaParser.getGraphsByStudy().keySet()) {
                studyNames.add(study.toString());
              }
              studies.setItems(FXCollections.observableArrayList(studyNames));

            } else {
              studies.setVisible(false);
              try {
                expParser.processTSV(file, ExperimentalDesignType.Standard, true);
                currentProject = expParser.getSampleGraph();
              } catch (IOException | JAXBException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
              }
              if (currentProject.getFactorsToSamples().isEmpty()) {
                showPopupMessage("Experimental design could not be parsed.", stage);
              }
              factors.setItems(
                  FXCollections.observableArrayList(currentProject.getFactorsToSamples().keySet()));
            }
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
    String title = "Experiment Graph - " + versionString;
    stage.setTitle(title);

    factors.valueProperty().addListener(new ChangeListener<String>() {

      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue,
          String newValue) {
        stopCurrentView();
        clearContent();
        if (newValue != null && !newValue.isEmpty())
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
