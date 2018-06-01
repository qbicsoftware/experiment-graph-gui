package life.qbic.app;

import org.apache.logging.log4j.Logger;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.Assertion;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import org.powermock.reflect.Whitebox;

import life.qbic.app.SampleGraph;

/**
 * Unit tests.
 */
public class SampleGraphTest  {

    @Mock
    private Logger mockLogger;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();


    @BeforeClass
    public static void loggerSetUp() {
      System.setProperty("log4j.defaultInitOverride", Boolean.toString(true));
      System.setProperty("log4j.ignoreTCL", Boolean.toString(true));
    }
  
    @Before
    public void setUpTest() {
        // inject mock logger
        Whitebox.setInternalState(SampleGraph.class, "LOG", mockLogger);
    }

//    @Test
//    public void testPrintVersion() throws IOException, JAXBException {
//        exit.expectSystemExitWithStatus(0);
//        exit.checkAssertionAfterwards(new Assertion() {
//            public void checkAssertion() {
//                // check that version has been loaded from tool.properties
//                Mockito.verify(mockLogger).info(ArgumentMatchers.anyString(), ArgumentMatchers.eq("1.2.3-SNAPSHOT"));
//            }
//        });
//        
//        SampleGraph.main(new String[] {"--version"});
//    }
    
}