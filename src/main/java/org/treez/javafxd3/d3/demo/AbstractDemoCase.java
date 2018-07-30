package org.treez.javafxd3.d3.demo;

import java.net.URL;

import org.treez.javafxd3.d3.D3;
import org.treez.javafxd3.d3.core.Selection;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.treez.javafxd3.d3.core.JsEngine;

/**
 * Abstract parent class for all demo cases
 * 
 */
public abstract class AbstractDemoCase implements DemoCase {

	//#region ATTRIBUTES

	/**
	 * The d3 wrapper
	 */
	protected D3 d3;

	/**
	 * Controls the browser
	 */
	protected JsEngine engine;

	/**
	 * Box for demo case preferences and buttons
	 */
	protected VBox demoPreferenceBox;

	//#end region

	//#region CONSTRUCTORS

	/**
	 * Constructor
	 * 
	 * @param d3 the D3 wrapper class
	 */
	public AbstractDemoCase(D3 d3) {
		this.d3 = d3;
		this.engine = d3.getJsEngine();
		loadCssForThisClass();
	}
	
	//#end region
	
	//#region METHODS
	/**
	 * @return the selection element
	 */
	public Selection getSvg() {
		Selection svg = d3.select("#svg");
		return svg;
	}
	
	/**
	 * If a css file exists that has the same name as the java/class file and
	 * is located next to that file, the css file is loaded with this method. 
	 */
	private void loadCssForThisClass() {
		String className = getClass().getName();
		String cssPath = className.replace(".", "/") + ".css";		
		URL cssUrl = getClass().getClassLoader().getResource(cssPath);	//root is for example file:/D:/javafx-d3/javafx-d3-demo/target/classes/
		if(cssUrl!=null){
			loadCss(cssUrl);
		}
		
	}
	
	public void loadCss(URL cssUrl){
		
		String command = "var head  = document.getElementsByTagName('head')[0];" //
		+ "    var link  = document.createElement('link');" //		
		+ "    link.rel  = 'stylesheet';" //
		+ "    link.type = 'text/css';" //
		+ "    link.href = '"+ cssUrl + "';" //
		+ "    link.media = 'all';" //
		+ "    head.appendChild(link);";		
		engine.executeScript(command);		
	}
	
	
	
	//#end region

}
