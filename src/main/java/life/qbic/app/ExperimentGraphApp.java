package life.qbic.app;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.isatools.isacreator.model.Investigation;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import life.qbic.expdesign.SamplePreparator;
import life.qbic.expdesign.io.EasyDesignReader;
import life.qbic.expdesign.model.StructuredExperiment;
import life.qbic.isatab.ISAReader;
import life.qbic.isatab.ISAToReadable;

/**
 * Stand alone tool to display imported experimental designs according to their experimental
 * factors.
 */
public class ExperimentGraphApp extends Application {

  final private static int SPLASH_TIME = 2000;

  final private SamplePreparator expParser = new SamplePreparator();
  private StructuredExperiment currentProject;
  private Map<Study, StructuredExperiment> isaStudies;

  Label investigationInfo = new Label();
  final Label studyLabel = new Label("Study");
  Label studyInfo = new Label();
  final ComboBox<String> studies = new ComboBox<String>();
  final Label factorLabel = new Label("Exp. Factor");
  final ComboBox<String> factors = new ComboBox<String>();

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
   * @param args launch arguments
   */
  public static void main(String[] args) {
    LauncherImpl.launchApplication(ExperimentGraphApp.class, SplashScreen.class, args);
  }

  @Override
  public void init() throws Exception {
    Thread.sleep(SPLASH_TIME);
  }

  /**
   * This is the entry point method.
   */
  @Override
  public void start(Stage stage) {

    final double sceneWidth = Screen.getPrimary().getBounds().getWidth() - 50;
    final double sceneHeight = Screen.getPrimary().getBounds().getHeight() - 100;

    final ToggleGroup designtypeGroup = new ToggleGroup();

    final FileChooser fileChooser = new FileChooser();
    final DirectoryChooser dirChooser = new DirectoryChooser();

    final Button openButton = new Button("Load experimental design");

    final Pane leftPaneItems = new VBox(12);

    investigationInfo.setMaxWidth(200);
    investigationInfo.setWrapText(true);
    studyInfo.setMaxWidth(200);
    studyInfo.setWrapText(true);

    RadioButton rb1 = new RadioButton("Upload QBiC Design");
    rb1.setToggleGroup(designtypeGroup);
    rb1.setSelected(true);
    rb1.setUserData(false);

    RadioButton rb2 = new RadioButton("Upload ISATab");
    rb2.setToggleGroup(designtypeGroup);
    rb2.setUserData(true);

    showFactors(false);
    showStudies(false);

    leftPaneItems.getChildren().addAll(rb1, rb2, openButton, investigationInfo, studyLabel, studies,
        studyInfo, factorLabel, factors);
    leftPaneItems.setPadding(new Insets(12, 12, 12, 12));

    // create content node
    sceneContent = new StackPane();
    HBox largeHBox = new HBox();
    sceneContent.getChildren().add(largeHBox);
    List<Node> hBoxChildren = largeHBox.getChildren();

    VBox leftPaneVBox = new VBox();
    hBoxChildren.add(leftPaneVBox);
    leftPaneVBox.getChildren().add(leftPaneItems);

    // define what to do after the browser has initialized
    Runnable afterBrowserLoadingHook = () -> {
      runDemoSuite(stage, factors, browser.getWidth(), browser.getHeight());
    };
    // create browser
    browser = new JavaFxD3Browser(afterBrowserLoadingHook, false); // set true for firebug debugging
    // add browser and allow it to use all available space
    hBoxChildren.add(browser);
    HBox.setHgrow(browser, Priority.ALWAYS);

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
            setStudyInfo(s);
            currentProject = isaStudies.get(s);
            showFactors(true);
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
              ISAReader isaParser = new ISAReader(new ISAToReadable());
              isaParser.createAllGraphs(file);
              isaStudies = new HashMap<Study, StructuredExperiment>();
              List<String> studyNames = new ArrayList<String>();
              setInvestigationInfo(isaParser.getInvestigation());

              for (StructuredExperiment struc : isaParser.getGraphsByStudy()) {
                Study s = struc.getStudy();
                isaStudies.put(s, struc);
                studyNames.add(s.toString());
              }

              showFactors(false);
              showStudies(true);
              studies.setItems(FXCollections.observableArrayList(studyNames));

            } else {
              showFactors(true);
              showStudies(false);
              try {
                expParser.processTSV(file, new EasyDesignReader(), true);
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
    final Color sceneColor = Color.web("#666970");
    scene = new Scene(sceneContent, sceneWidth, sceneHeight, sceneColor);
    stage.setScene(scene);
    stage.show();
    stage.setResizable(false);
  }

  protected void setInvestigationInfo(Investigation investigation) {
    investigationInfo.setText(investigation.getInvestigationTitle());
    investigationInfo.setVisible(true);
  }

  protected void setStudyInfo(Study s) {
    studyInfo.setText(s.getStudyTitle());
    studyInfo.setVisible(true);
  }

  private void showFactors(boolean show) {
    factors.setVisible(show);
    factorLabel.setVisible(show);
  }

  private void showStudies(boolean show) {
    studies.setVisible(show);
    studyLabel.setVisible(show);
    studyInfo.setVisible(false);
    investigationInfo.setVisible(false);
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

  private void runDemoSuite(Stage stage, ComboBox<String> factors, double w, double h) {
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
          createAndStartNewView(ExperimentGraph.factory(d3,
              currentProject.getFactorsToSamples().get(newValue), stage, w, h));
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
   * @return the empty root element
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
