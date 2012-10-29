import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

import ro.sync.exml.plugin.selection.SelectionPluginContext;

import edu.emory.library.oxygen_plugin.NameDropper.NameDropperPluginExtension;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.io.File;

import nu.xom.Builder;
import nu.xom.Document;
import ro.sync.contentcompletion.xml.WhatElementsCanGoHereContext;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.WSTextXMLSchemaManager;
import ro.sync.exml.workspace.api.editor.page.text.xml.WSXMLTextEditorPage;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.contentcompletion.xml.CIElement;



public class NameDropperTest {
    // Mock plugin
    NameDropperPluginExtension mockND;
    
    SelectionPluginContext mockContext;
    
    // Mock xml builder
    static Builder realXmlBuilder = new Builder();
    Builder mockXmlBuilder;
    
    
    // Fixtures
    static String autoSuggestReturn;
    static Document viafReturn;
    
    public NameDropperTest() {
    }
    
    //Read file into string used to get fixtures
    public static String readFile( String file ) throws IOException {
       BufferedReader reader = new BufferedReader( new FileReader (file));
       String         line = null;
       StringBuilder  stringBuilder = new StringBuilder();
       String         ls = System.getProperty("line.separator");

       while( ( line = reader.readLine() ) != null ) {
          stringBuilder.append( line );
          stringBuilder.append( ls );
       }

       return stringBuilder.toString();
    }

    
    

    @BeforeClass
    public static void setUpClass() throws Exception {
        // load fixtures
        autoSuggestReturn = readFile("tests/autoSuggestReturn.json");
        viafReturn = realXmlBuilder.build(new File("tests/viafReturn.xml"));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        // create mocks
        
        try {
            this.mockND = mock(NameDropperPluginExtension.class);
            this.mockContext = mock(SelectionPluginContext.class);
            this.mockXmlBuilder = mock(Builder.class);
            viafReturn = realXmlBuilder.build(new File("tests/viafReturn.xml"));
          
    }catch (Exception e){
        e.printStackTrace();
    }}
    
    @After
    public void tearDown() {
        this.mockND = null;
        this.mockXmlBuilder = null;
    }
    
     @Rule
     public ExpectedException exception = ExpectedException.none();

    
    
     @Test
     public void testQueryViaf() {
         
         String result = "";
         String docType = ""; 
         
         try {
          String searchTerm = "Smith";
           HashMap h = new HashMap();
           h.put("query", searchTerm);
           
           // setup corret returns for the method calls
           when(this.mockND.query("http://viaf.org/viaf/AutoSuggest", h)).thenReturn(autoSuggestReturn);
           when(this.mockND.query("http://viaf.org/viaf/159021806/viaf.xml", new HashMap())).thenReturn(viafReturn.toXML());
           
           // EAD version of tags
           docType = "EAD";
           when(this.mockND.queryVIAF(searchTerm, docType)).thenCallRealMethod();
           when(this.mockND.getTagName(docType, "Corporate")).thenReturn("corpname");
           
           // Corp
           result = this.mockND.queryVIAF(searchTerm, docType);
           assertEquals(result, "<corpname source=\"viaf\" authfilenumber=\"159021806\">Smith</corpname>");
           
           // Person
           
           // Change value to Person 
           viafReturn.getRootElement().getFirstChildElement("nameType", "http://viaf.org/viaf/terms#").removeChild(0);
           viafReturn.getRootElement().getFirstChildElement("nameType", "http://viaf.org/viaf/terms#").appendChild("Personal");
           when(this.mockND.query("http://viaf.org/viaf/159021806/viaf.xml", new HashMap())).thenReturn(viafReturn.toXML());
           when(this.mockND.getTagName(docType, "Personal")).thenReturn("persname");
           result = this.mockND.queryVIAF(searchTerm, docType);
           assertEquals(result, "<persname source=\"viaf\" authfilenumber=\"159021806\">Smith</persname>");
           
           // Geo
           
           // Change value to Geographic 
           viafReturn.getRootElement().getFirstChildElement("nameType", "http://viaf.org/viaf/terms#").removeChild(0);
           viafReturn.getRootElement().getFirstChildElement("nameType", "http://viaf.org/viaf/terms#").appendChild("Geographic");
           when(this.mockND.query("http://viaf.org/viaf/159021806/viaf.xml", new HashMap())).thenReturn(viafReturn.toXML());
           when(this.mockND.getTagName(docType, "Geographic")).thenReturn("geogname");
           
           result = this.mockND.queryVIAF(searchTerm, docType);
           assertEquals(result, "<geogname source=\"viaf\" authfilenumber=\"159021806\">Smith</geogname>");
           
           
          
           // TEI version of tags
           docType = "TEI";
           when(this.mockND.queryVIAF(searchTerm, docType)).thenCallRealMethod();
           when(this.mockND.getTagName(docType, "Geographic")).thenReturn("name");
           
           // Place
           result = this.mockND.queryVIAF(searchTerm, docType);
           assertEquals(result, "<name ref=\"http://viaf.org/viaf/159021806\" type=\"place\">Smith</name>");
           
           // Person
           
           // Change value to Person 
           viafReturn.getRootElement().getFirstChildElement("nameType", "http://viaf.org/viaf/terms#").removeChild(0);
           viafReturn.getRootElement().getFirstChildElement("nameType", "http://viaf.org/viaf/terms#").appendChild("Personal");
           when(this.mockND.query("http://viaf.org/viaf/159021806/viaf.xml", new HashMap())).thenReturn(viafReturn.toXML());
           when(this.mockND.getTagName(docType, "Personal")).thenReturn("name");
           
           result = this.mockND.queryVIAF(searchTerm, docType);
           assertEquals(result, "<name ref=\"http://viaf.org/viaf/159021806\" type=\"person\">Smith</name>");
           
           // Org
           
           // Change value to Geographic 
           viafReturn.getRootElement().getFirstChildElement("nameType", "http://viaf.org/viaf/terms#").removeChild(0);
           viafReturn.getRootElement().getFirstChildElement("nameType", "http://viaf.org/viaf/terms#").appendChild("Corporate");
           when(this.mockND.query("http://viaf.org/viaf/159021806/viaf.xml", new HashMap())).thenReturn(viafReturn.toXML());
           when(this.mockND.getTagName(docType, "Corporate")).thenReturn("name");
           
           result = this.mockND.queryVIAF(searchTerm, docType);
           assertEquals(result, "<name ref=\"http://viaf.org/viaf/159021806\" type=\"org\">Smith</name>");          

         } catch (Exception e){
             e.printStackTrace();
         }
         
         
     }
     
     @Test
     public void testQueryViafNoResults() throws Exception{
         
         try {
         exception.expect(Exception.class);
         exception.expectMessage("No Results");
              String result = "";
              String searchTerm = "jdfkjdfkjdfkj";
               HashMap h = new HashMap();
               h.put("query", searchTerm);
           
               // setup corret returns for the method calls
               when(this.mockND.query("http://viaf.org/viaf/AutoSuggest", h)).thenReturn("{\"query\": \"jjfkdjkfjdk\",\"result\": null}");
               when(this.mockND.queryVIAF(searchTerm, "EAD")).thenCallRealMethod();
               result = this.mockND.queryVIAF(searchTerm, "EAD");
         }catch (Exception e){
            throw e;
         }
     }
     
     @Test
     public void testQueryViafInvalidNameType() throws Exception {
         
         try {
         exception.expect(Exception.class);
         exception.expectMessage("Unsupported nameType: Invalid");
              String result = "";
              String searchTerm = "Smth";
               HashMap h = new HashMap();
               h.put("query", searchTerm);
               
           viafReturn.getRootElement().getFirstChildElement("nameType", "http://viaf.org/viaf/terms#").removeChild(0);
           viafReturn.getRootElement().getFirstChildElement("nameType", "http://viaf.org/viaf/terms#").appendChild("Invalid");
           when(this.mockND.query("http://viaf.org/viaf/159021806/viaf.xml", new HashMap())).thenReturn(viafReturn.toXML());
           
               // setup corret returns for the method calls
               when(this.mockND.query("http://viaf.org/viaf/AutoSuggest", h)).thenReturn(autoSuggestReturn);
               when(this.mockND.getTagName("EAD", "Invalid")).thenReturn(null);
               when(this.mockND.queryVIAF(searchTerm, "EAD")).thenCallRealMethod();
               when(this.mockND.query("http://viaf.org/viaf/159021806/viaf.xml", new HashMap())).thenReturn(viafReturn.toXML());
               
               result = this.mockND.queryVIAF(searchTerm, "EAD");
         }catch (Exception e){
             throw e;
         }
     }
     
     @Test
     public void testQueryViafNoDocType() throws Exception {
         
         try {
         exception.expect(Exception.class);
         exception.expectMessage("No DocType selected");
              String result = "";
              String searchTerm = "Smth";
               HashMap h = new HashMap();
               h.put("query", searchTerm);
           
               // setup corret returns for the method calls
               //when(this.mockND.query("http://viaf.org/viaf/AutoSuggest", h)).thenReturn(autoSuggestReturn);
               when(this.mockND.queryVIAF(searchTerm, "")).thenCallRealMethod();
               //when(this.mockND.query("http://viaf.org/viaf/159021806/viaf.xml", new HashMap())).thenReturn(viafReturn.toXML());
               
               result = this.mockND.queryVIAF(searchTerm, "");
         } catch (Exception e){
            throw e;
            
         }
     }
     
     @Test
     public void testGetTagName() {
         
         NameDropperPluginExtension nd = new NameDropperPluginExtension();
         String result = "";
         String docType = ""; 
         
         // no docType set
         result = nd.getTagName(docType);
         assertEquals(null, result);
         
         // TEI document
         docType = "TEI";
         result = nd.getTagName(docType);
         assertEquals("name", result);
         
         // EAD document, no name type
         docType = "EAD";
         result = nd.getTagName(docType);
         assertEquals(null, result);
         
         // EAD with name type
         String nameType = "Personal";
         result = nd.getTagName(docType, nameType);
         assertEquals("persname", result);
         nameType = "Corporate";
         result = nd.getTagName(docType, nameType);
         assertEquals("corpname", result);
         nameType = "Geographic";
         result = nd.getTagName(docType, nameType);
         assertEquals("geogname", result);
         nameType = "Bogus Type";
         result = nd.getTagName(docType, nameType);
         assertEquals(null, result);
         
     }
     
     @Test
     public void testTagAllowed() {
         
         int wsId = StandalonePluginWorkspace.MAIN_EDITING_AREA;
         StandalonePluginWorkspace mockWS = mock(StandalonePluginWorkspace.class);
         
         // simulate editor unavailable
         when(this.mockContext.getPluginWorkspace()).thenReturn(mockWS);
         when(mockWS.getCurrentEditorAccess(wsId)).thenReturn(null);
         when(this.mockND.tagAllowed("EAD", this.mockContext)).thenCallRealMethod();
         assertEquals(null, this.mockND.tagAllowed("EAD", this.mockContext));
         
         // simulate no page available
         WSEditor mockEd = mock(WSEditor.class);
         when(mockWS.getCurrentEditorAccess(wsId)).thenReturn(null);
         when(mockEd.getCurrentPage()).thenReturn(null);
         assertEquals(null, this.mockND.tagAllowed("EAD", this.mockContext));
                 
         // simulate full schema access; no elements allowed 
         WSXMLTextEditorPage mockPage = mock(WSXMLTextEditorPage.class);
         WSTextXMLSchemaManager mockSchema = mock(WSTextXMLSchemaManager.class);
         when(mockEd.getCurrentPage()).thenReturn(mockPage);
         when(mockPage.getXMLSchemaManager()).thenReturn(mockSchema);
         int offset = 1;
         when(mockPage.getSelectionStart()).thenReturn(offset);
         WhatElementsCanGoHereContext mockContext = mock(WhatElementsCanGoHereContext.class);
         //when(mockSchema.createWhatElementsCanGoHereContext(offset)).thenReturn(mockContext);
         java.util.List<CIElement> elements = new java.util.ArrayList<CIElement>();
         when(mockSchema.whatElementsCanGoHere(mockContext)).thenReturn(elements);
         assertEquals(false, this.mockND.tagAllowed("EAD", this.mockContext));
     }
}
