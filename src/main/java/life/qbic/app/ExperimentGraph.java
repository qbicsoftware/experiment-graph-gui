package life.qbic.app;

import org.treez.javafxd3.d3.behaviour.Zoom;
import org.treez.javafxd3.d3.behaviour.Zoom.ZoomEventType;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.treez.javafxd3.d3.D3;
import org.treez.javafxd3.d3.arrays.Array;
import org.treez.javafxd3.d3.core.Selection;
import org.treez.javafxd3.d3.demo.AbstractDemoCase;
import org.treez.javafxd3.d3.demo.DemoFactory;
import org.treez.javafxd3.d3.demo.Margin;
import org.treez.javafxd3.d3.functions.DataFunction;
import org.treez.javafxd3.d3.functions.data.wrapper.DataFunctionWrapper;
import org.treez.javafxd3.d3.scales.OrdinalScale;
import org.treez.javafxd3.javafx.JavaFxJsObject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import life.qbic.datamodel.samples.ISampleBean;
import life.qbic.datamodel.samples.SampleSummary;

public class ExperimentGraph extends AbstractDemoCase {

  // jfx
  private static Stage globalStage;
  // js libs
  private static final List<String> libs = new ArrayList<String>(Arrays.asList("js/dagre.min.js"));
  // d3
  private Selection svg;
  private OrdinalScale colorScale;
  private static final String SYMBOL_NODE_COLOR = "#3494F8";
  final Margin margin = new Margin(20, 20, 30, 40);
  // final int width = 1200 - margin.left - margin.right;
  // final int height = 800 - margin.top - margin.bottom;

  int width;
  int height;
  // model
  Set<String> usedSymbols;
  Set<String> noSymbols;
  private Map<Integer, SampleSummary> idToSample;
  private final Map<String, String> icons = new HashMap<String, String>() {
    {
      put("dna", "img/dna_filled.svg");
      put("rna", "img/rna.png");
      put("peptides", "img/peptide.svg");
      put("proteins", "img/protein.png");
      put("smallmolecules", "img/mol.png");
    }
  };

  /**
   * initialize Experiment Graph
   * 
   * @param d3 D3 wrapper
   * @param nodes List of Sample Summaries that will be drawn as nodes
   * @param stage the main jfx stage
   * @param height available WebView height
   * @param width available WebView width
   */
  public ExperimentGraph(D3 d3, List<SampleSummary> nodes, Stage stage, double width,
      double height) {
    super(d3);
    globalStage = stage;
    width = (int) width - margin.left;
    height = (int) height - margin.top;
    try {
      init(nodes);
    } catch (IOException e) {
      System.err.println("file not found");
      e.printStackTrace();
    }
  }

  /**
   * Factory function for Experiment Graph initialization
   * 
   * @param d3 D3 wrapper
   * @param nodes List of Sample Summaries that will be drawn as nodes
   * @param stage the main jfx stage
   * @param width available WebView width
   * @param height available WebView height
   * @return a new factory
   */
  public static DemoFactory factory(D3 d3, List<SampleSummary> nodes, Stage stage, double width,
      double height) {
    return new DemoFactory() {
      @Override
      public ExperimentGraph newInstance() {
        return new ExperimentGraph(d3, nodes, stage, width, height);
      }
    };
  }

  private void init(List<SampleSummary> nodes) throws IOException {
    int factor = 1;
    int rad = 20 * factor;

    svg = d3.select("svg") //
        .attr("width", width) //
        .attr("height", height) //
        .append("g") //
        .attr("transform", "translate(" + margin.left + ", " + margin.top + ")");

    injectJSLibraries();
    computeGraphCoordinates(rad, nodes);

    drawGraph(factor, rad);

    // create zoom behavior
    DataFunction<Void> zoomDataFunction = new DataFunctionWrapper<>(() -> {
      Array<Double> xy = d3.zoomEvent().translate();
      String translation = xy.get(0).toString() + ", " + xy.get(1).toString();
      svg.attr("transform", "translate(" + translation + ")scale(" + d3.zoomEvent().scale() + ")");
    });

    Zoom zoom = d3.behavior() //
        .zoom().scaleExtent(new Double[] {0.1, 10.0}).on(ZoomEventType.ZOOM, zoomDataFunction);

    d3.select("svg").call(zoom);

  }

  private void injectJSLibraries() throws IOException {
    for (String lib : libs) {
      InputStream in = getClass().getResourceAsStream("/" + lib);
      String script = IOUtils.toString(in, "utf-8");
      engine.executeScript(script);
    }
  }

  private void drawGraph(int factor, int rad) {
    int maxY = 0, maxX = 0;// maximal node positions
    String longest_label = "x";

    JavaFxJsObject json = (JavaFxJsObject) engine.executeScript("dagre.graphlib.json.write(g)");
    JavaFxJsObject edges = (JavaFxJsObject) json.getMember("edges");
    JavaFxJsObject nodes = (JavaFxJsObject) engine.executeScript("g.nodes()");
    List<String> nodeIDs = Arrays.asList(nodes.toString().split("\\s*,\\s*"));
    List<String> edgeIDs = Arrays.asList(edges.toString().split("\\s*,\\s*"));

    // edges
    for (int i = 0; i < edgeIDs.size(); i++) {
      JavaFxJsObject edge = (JavaFxJsObject) edges.getSlot(i);
      String v = (String) edge.getMember("v");
      String w = (String) edge.getMember("w");

      JavaFxJsObject val = (JavaFxJsObject) edge.getMember("value");
      JavaFxJsObject points = (JavaFxJsObject) val.getMember("points");
      JavaFxJsObject start = (JavaFxJsObject) engine.executeScript("g.node(" + v + ")");
      JavaFxJsObject mid = (JavaFxJsObject) points.getSlot(1);
      JavaFxJsObject end = (JavaFxJsObject) engine.executeScript("g.node(" + w + ")");

      double top_x = toDouble(start.getMember("x")) * factor;
      double top_y = toDouble(start.getMember("y")) * factor;
      double mid_x = toDouble(mid.getMember("x")) * factor;
      double mid_y = toDouble(mid.getMember("y")) * factor;
      double bot_x = toDouble(end.getMember("x")) * factor;
      double bot_y = toDouble(end.getMember("y")) * factor;

      d3.select("g").append("line") //
          .attr("x1", top_x) //
          .attr("y1", top_y) //
          .attr("x2", mid_x) //
          .attr("y2", mid_y) //
          .attr("stroke-width", 2).attr("stroke", "black"); //
      d3.select("g").append("line") //
          .attr("x1", mid_x) //
          .attr("y1", mid_y) //
          .attr("x2", bot_x) //
          .attr("y2", bot_y) //
          .attr("stroke-width", 2).attr("stroke", "black"); //
    }

    for (String nodeID : nodeIDs) {
      int id = Integer.parseInt(nodeID);
      SampleSummary summary = idToSample.get(id);
      JavaFxJsObject nd = (JavaFxJsObject) engine.executeScript("g.node(" + id + ")");
      String label = (String) nd.getMember("label");
      if (!label.isEmpty()) {
        String lowerLabel = label.toLowerCase().replaceAll("\\s", "");
        double x = toDouble(nd.getMember("x")) * factor;
        double y = toDouble(nd.getMember("y")) * factor;

        maxX = (int) Math.max(maxX, x);
        maxY = (int) Math.max(maxY, y);
        if (label.length() > longest_label.length()) {
          longest_label = label;
        }

        String color = colorScale.applyForString(label);
        if (usedSymbols.contains(label))
          color = SYMBOL_NODE_COLOR;
        // main circles
        Selection circle = d3.select("g").append("circle")//
            .attr("cx", x) //
            .attr("cy", y) //
            .attr("r", rad) //
            .attr("stroke", "black") //
            .style("fill", color) //
            .on("click", showSamples(label, summary.getSamples())); //
        circle.on("mouseover", fade(circle, 0.75)) //
            .on("mouseout", fade(circle, 1.0)); //

        // circles containing symbols
        if (usedSymbols.contains(label)) {
          drawAnalyteIcon(d3.select("g"), lowerLabel, x, y, rad);
        }
        // sample amount
        d3.select("g").append("text") //
            .text(Integer.toString(summary.getAmount())) //
            .attr("font-family", "sans-serif") //
            .attr("font-size", "14px") //
            .attr("stroke", "black") //
            .attr("text-anchor", "middle") //
            .attr("x", x + 22) //
            .attr("y", y - 22); //
      }
    }
    // legend
    int legendEntryHeight = rad + 5;
    int legendX = margin.left;
    int legendY = maxY * factor + rad + 20;

    int i = -1;
    for (String label : noSymbols) {
      i++;
      d3.select("g") //
          .append("circle") //
          .attr("cx", legendX) //
          .attr("cy", legendY + legendEntryHeight * i + 10) //
          .attr("r", rad / 2) //
          .attr("stroke", "black") //
          .attr("fill", colorScale.applyForString(label)); //

      d3.select("g") //
          .append("text") //
          .text(label) //
          .attr("font-family", "sans-serif") //
          .attr("font-size", "14px") //
          .attr("stroke", "black") //
          .attr("text-anchor", "left") //
          .attr("x", legendX + margin.left) //
          .attr("y", legendY + legendEntryHeight * i + 15); //
    }

    i = -1;
    for (String label : usedSymbols) {
      i++;
      d3.select("g") //
          .append("circle") //
          .attr("cx", legendX) //
          .attr("cy", legendY + legendEntryHeight * (noSymbols.size() + i) + 10) //
          .attr("r", rad / 2) //
          .attr("stroke", "black") //
          .attr("fill", SYMBOL_NODE_COLOR); //

      String type = label.toLowerCase().replaceAll("\\s", "");

      drawAnalyteIcon(d3.select("g"), type, legendX,
          legendY + legendEntryHeight * (noSymbols.size() + i) + 10, rad / 2);//

      d3.select("g") //
          .append("text") //
          .text(shortenInfo(label)) //
          .attr("font-family", "sans-serif") //
          .attr("font-size", "14px") //
          .attr("stroke", "black") //
          .attr("text-anchor", "left") //
          .attr("x", legendX + margin.left) //
          .attr("y", legendY + legendEntryHeight * (noSymbols.size() + i) + 15); //
    }

  }

  private String shortenInfo(String info) {
    switch (info) {
      case "CARBOHYDRATES":
        return "Carbohydrates";
      case "SMALLMOLECULES":
        return "Smallmolecules";
      case "DNA":
        return "DNA";
      case "RNA":
        return "RNA";
      default:
        return WordUtils.capitalizeFully(info.replace("_", " "));
    }
  }

  private DataFunction<Void> showSamples(String label, List<ISampleBean> samples) {
    return new DataFunctionWrapper<>(String.class, engine, (x) -> {
      showNodeInfo(label, samples);
      return null;
    });
  }

  /**
   * Method called by JavaScript when clicking on a node.
   * 
   * @param label Label of the clicked node
   * @param samples Samples that are part of the clicked node
   */
  public void showNodeInfo(String label, List<ISampleBean> samples) {
    List<String> names = new ArrayList<String>();
    for (ISampleBean s : samples) {
      names.add(s.getCode());
    }

    ListView<String> list = new ListView<String>();
    list.setMaxWidth(250);
    ObservableList<String> data = FXCollections.observableArrayList(names);
    list.setItems(data);

    VBox dialogVbox = new VBox(20);
    Label l = new Label(label);

    dialogVbox.setAlignment(Pos.CENTER);
    dialogVbox.setSpacing(5);
    dialogVbox.setPadding(new Insets(10, 10, 10, 10));
    dialogVbox.getChildren().addAll(l, list);
    showPopup(dialogVbox, l.getMaxWidth(), 500);
  }

  private void showPopup(Parent content, double width, double height) {
    Scene dialogScene = new Scene(content, width, height);
    final Stage dialog = new Stage();
    dialog.initModality(Modality.APPLICATION_MODAL);
    dialog.initOwner(globalStage);
    dialog.setScene(dialogScene);
    dialog.show();
  }

  private double toDouble(Object o) {
    try {
      return (double) o;
    } catch (Exception e) {
      double r = (int) o;
      return r;
    }
  }

  private DataFunction<Void> fade(final Selection element, double opacity) {
    return new DataFunctionWrapper<>(String.class, engine, (x) -> {
      element.transition() //
          .style("opacity", opacity);
      return null;
    });

  }

  private Pair<Double, Double> pointOnCircle(double r, double arc) {
    double x = Math.sin(Math.toRadians(arc)) * r;
    double y = Math.cos(Math.toRadians(arc)) * r;
    return new ImmutablePair<Double, Double>(x, y);
  }

  private void drawAnalyteIcon(final Selection element, String type, double x, double y, int rad) {
    double left = x - rad;
    double top = y - rad;
    double right = x + rad;
    double bottom = y + rad;
    Pair<Double, Double> topRight = pointOnCircle(rad, 45);
    double xyFromCenter = topRight.getLeft();

    switch (type) {
      case "dna":
        element.append("line") //
            .attr("x1", x + xyFromCenter) //
            .attr("y1", y + xyFromCenter) //
            .attr("x2", x - xyFromCenter) //
            .attr("y2", y - xyFromCenter) //
            .attr("stroke-width", 2).attr("stroke", "black"); //
        break;
      case "rna":
        element.append("line") //
            .attr("x1", x - xyFromCenter) //
            .attr("y1", y + xyFromCenter) //
            .attr("x2", x + xyFromCenter) //
            .attr("y2", y - xyFromCenter) //
            .attr("stroke-width", 2).attr("stroke", "black"); //
        break;
      case "peptides":
        element.append("line") //
            .attr("x1", left) //
            .attr("y1", bottom - rad) //
            .attr("x2", right - rad) //
            .attr("y2", top) //
            .attr("stroke-width", 2).attr("stroke", "black"); //
        element.append("line") //
            .attr("x1", right - rad) //
            .attr("y1", top) //
            .attr("x2", right) //
            .attr("y2", bottom - rad) //
            .attr("stroke-width", 2).attr("stroke", "black"); //
        break;
      case "proteins":
        element.append("line") //
            .attr("x1", x - xyFromCenter) //
            .attr("y1", y + xyFromCenter) //
            .attr("x2", x + xyFromCenter) //
            .attr("y2", y - xyFromCenter) //
            .attr("stroke-width", 2).attr("stroke", "black"); //
      case "smallmolecules":
        element.append("line") //
            .attr("x1", left) //
            .attr("y1", bottom - rad) //
            .attr("x2", right - rad) //
            .attr("y2", top) //
            .attr("stroke-width", 2).attr("stroke", "black"); //
        element.append("line") //
            .attr("x1", right - rad) //
            .attr("y1", top) //
            .attr("x2", right) //
            .attr("y2", bottom - rad) //
            .attr("stroke-width", 2).attr("stroke", "black"); //
        element.append("line") //
            .attr("x1", right) //
            .attr("y1", bottom - rad) //
            .attr("x2", right - rad) //
            .attr("y2", bottom) //
            .attr("stroke-width", 2).attr("stroke", "black"); //
        element.append("line") //
            .attr("x1", right - rad) //
            .attr("y1", bottom) //
            .attr("x2", left) //
            .attr("y2", bottom - rad) //
            .attr("stroke-width", 2).attr("stroke", "black"); //
        break;
      default:
        break;
    }
  }

  private void computeGraphCoordinates(int radius, List<SampleSummary> summaries)
      throws IOException {
    // call function from script file
    engine.executeScript("new dagre.graphlib.Graph()");
    engine.executeScript("var g = new dagre.graphlib.Graph()");
    // Set an object for the graph label
    engine.executeScript("g.setGraph({marginx:" + margin.left + ",marginy:" + margin.top + "})");

    // Default to assigning a new object as a label for each new edge.
    engine.executeScript("g.setDefaultEdgeLabel(function() { return {}; });");

    usedSymbols = new LinkedHashSet<String>();
    noSymbols = new LinkedHashSet<String>();

    idToSample = new HashMap<Integer, SampleSummary>();
    Collections.sort(summaries);
    for (SampleSummary s : summaries) {

      int id = s.getId();
      idToSample.put(id, s);

      String name = s.getName();
      String lowerLabel = name.toLowerCase().replaceAll("\\s", "");
      if (icons.containsKey(lowerLabel)) {
        usedSymbols.add(name);
      } else {
        if (!name.isEmpty())
          noSymbols.add(name);
      }

      engine.executeScript("g.setNode(" + id + ", {"//
          + "label : '" + name + "',"//
          + "width : " + radius * 2 + ","//
          + "height : " + radius * 2 + "});");//
      for (int child : s.getChildIDs()) {
        engine.executeScript("g.setEdge(" + id + ", " + child + ")");
      }
    }
    OrdinalScale cat = d3.scale().category10();
    if (noSymbols.size() > 10) {
      cat = d3.scale().category20();
    }
    colorScale = cat.domain(noSymbols.toArray(new String[noSymbols.size()]));

    engine.executeScript("dagre.layout(g)");
  }

  @Override
  public void start() {}

  @Override
  public void stop() {}

}
