package org.bahmni.feed.openerp.worker;

import org.bahmni.feed.openerp.OpenERPAtomFeedProperties;
import org.bahmni.feed.openerp.client.OpenMRSWebClient;
import org.bahmni.openerp.web.client.OpenERPClient;
import org.bahmni.openerp.web.request.OpenERPRequest;
import org.bahmni.openerp.web.request.builder.Parameter;
import org.ict4h.atomfeed.client.domain.Event;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OpenERPDrugServiceEventWorkerTest {

    @Mock
    private OpenERPAtomFeedProperties atomFeedProperties;
    @Mock
    private OpenERPClient openERPClient;
    @Mock
    private OpenMRSWebClient webClient;

    private String feedUri = "http://feeduri";
    private String drugOrderJson;
    private OpenERPDrugServiceEventWorker worker;

    @Before
    public void setUp(){
        drugOrderJson = "{\n" +
                "    \"shortName\": \"shortName\",\n" +
                "    \"uuid\": \"a8268ab9-3341-4bf6-bdb1-cff0931f9d77\",\n" +
                "    \"strength\": \"250mg\",\n" +
                "    \"dosageForm\": \"Tablet(s)\",\n" +
                "    \"combination\": true,\n" +
                "    \"minimumDose\": \"2.0\",\n" +
                "    \"maximumDose\": \"50.0\",\n" +
                "    \"genericName\": \"Paracetamol\",\n" +
                "    \"name\": \"Paracetamol 250mg\"\n" +
                "}";
        MockitoAnnotations.initMocks(this);
        worker = new OpenERPDrugServiceEventWorker(feedUri,openERPClient,webClient,"http://prefix/");

    }

    @Test
    public void testProcess() throws IOException {
        Event event = new Event("event-id","content","test",feedUri);
        when(webClient.get(URI.create("http://prefix/content"))).thenReturn(drugOrderJson);

        worker.process(event);
        ArgumentCaptor<OpenERPRequest> erpRequestCatcher = ArgumentCaptor.forClass(OpenERPRequest.class);
        verify(openERPClient).execute(erpRequestCatcher.capture());

        OpenERPRequest openERPRequest = erpRequestCatcher.getValue();
        List<Parameter> actualParameters = openERPRequest.getParameters();
        Assert.assertTrue(actualParameters.contains(new Parameter("shortName","shortName")));
        Assert.assertTrue(actualParameters.contains(new Parameter("uuid","a8268ab9-3341-4bf6-bdb1-cff0931f9d77")));
        Assert.assertTrue(actualParameters.contains(new Parameter("strength","250mg")));
        Assert.assertTrue(actualParameters.contains(new Parameter("dosageForm","Tablet(s)")));
        Assert.assertTrue(actualParameters.contains(new Parameter("combination","true")));
        Assert.assertTrue(actualParameters.contains(new Parameter("minimumDose","2.0")));
        Assert.assertTrue(actualParameters.contains(new Parameter("maximumDose","50.0")));
        Assert.assertTrue(actualParameters.contains(new Parameter("genericName","Paracetamol")));
        Assert.assertTrue(actualParameters.contains(new Parameter("name","Paracetamol 250mg")));

        Assert.assertTrue(actualParameters.contains(new Parameter("last_read_entry_id","event-id")));
        Assert.assertTrue(actualParameters.contains(new Parameter("category","create.drug")));
        Assert.assertTrue(actualParameters.contains(new Parameter("feed_uri_for_last_read_entry",feedUri)));
        Assert.assertFalse(actualParameters.contains(new Parameter("is_failed_event","1","boolean")));
    }

    @Test
    public void testFailedEvent() throws IOException{
        Event event = new Event("event-id","content","test",null);
        when(webClient.get(URI.create("http://prefix/content"))).thenReturn(drugOrderJson);

        worker.process(event);
        ArgumentCaptor<OpenERPRequest> erpRequestCatcher = ArgumentCaptor.forClass(OpenERPRequest.class);
        verify(openERPClient).execute(erpRequestCatcher.capture());

        OpenERPRequest openERPRequest = erpRequestCatcher.getValue();
        List<Parameter> actualParameters = openERPRequest.getParameters();
        Assert.assertTrue(actualParameters.contains(new Parameter("is_failed_event","1","boolean")));

    }
}