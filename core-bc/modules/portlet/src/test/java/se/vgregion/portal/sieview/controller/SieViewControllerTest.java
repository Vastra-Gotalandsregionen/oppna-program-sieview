package se.vgregion.portal.sieview.controller;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.portlet.MockResourceResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.Model;
import se.vgregion.portal.concurrency.ThreadSynchronizationManager;
import se.vgregion.portal.patient.event.PatientEvent;
import se.vgregion.portal.patient.event.PersonNummer;

import javax.portlet.*;
import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Patrik Bergström
 */
public class SieViewControllerTest {

    private SieViewController sieViewController = new SieViewController();

    @Before
    public void setup() {
        ReflectionTestUtils.setField(sieViewController, "sieViewIframeUrl", "asdf____%s______");
        ThreadSynchronizationManager.getInstance().setTimeout(10);
        ThreadSynchronizationManager.getInstance().setTimeoutTimeUnit(TimeUnit.MILLISECONDS);
    }

    @Test
    public void testShow() throws Exception {
        // Given
        final RenderRequest renderRequest = mock(RenderRequest.class);
        final Model model = mock(Model.class);

        PortletSession portletSession = mock(PortletSession.class);
        when(portletSession.getAttribute("personId")).thenReturn("191212121212");
        when(renderRequest.getPortletSession()).thenReturn(portletSession);

        // When
        String view = sieViewController.show(renderRequest, model);

        // Then
        assertEquals(SieViewController.VIEW_JSP, view);
    }

    @Test
    public void testShowNoPersonIdInSession() throws Exception {
        // Given
        final RenderRequest renderRequest = mock(RenderRequest.class);
        final Model model = mock(Model.class);

        PortletSession portletSession = mock(PortletSession.class);
        when(portletSession.getAttribute("personId")).thenReturn(null);
        when(renderRequest.getPortletSession()).thenReturn(portletSession);

        // When
        String view = sieViewController.show(renderRequest, model);

        // Then
        assertEquals(SieViewController.START_FORM_JSP, view);
    }

    @Test
    public void testStart() throws Exception {

        // Given
        Model model = mock(Model.class);
        ActionResponse actionResponse = mock(ActionResponse.class);
        ActionRequest actionRequest = mock(ActionRequest.class);

        // When
        sieViewController.start("191212121212", actionRequest, actionResponse, model);

        // Then (verify an event is set on the response)
        verify(actionResponse).setEvent(any(QName.class), any(Serializable.class));
    }
    @Test
    public void testStartNoPersonId() throws Exception {

        // Given
        Model model = mock(Model.class);
        ActionResponse actionResponse = mock(ActionResponse.class);
        ActionRequest actionRequest = mock(ActionRequest.class);

        // When
        sieViewController.start(null, actionRequest, actionResponse, model);

        // Then (verify an event is NOT set on the response)
        verify(actionResponse, times(0)).setEvent(any(QName.class), any(Serializable.class));
    }

    /* This method goes through the synchronization steps made possible to the ThreadSynchronizationManager. */
    @Test
    public void testChangeListener() throws Exception {

        // Given
        MockResourceResponse resourceResponse = new MockResourceResponse();
        ResourceRequest resourceRequest = mock(ResourceRequest.class);

        PortletSession portletSession = mock(PortletSession.class);
        when(portletSession.getId()).thenReturn("1234");
        when(resourceRequest.getPortletSession()).thenReturn(portletSession);

        EventRequest eventRequest = mock(EventRequest.class);
        Event event = mock(Event.class);
        PatientEvent patientEvent = mock(PatientEvent.class);
        when(patientEvent.getPersonNummer()).thenReturn(PersonNummer.personummer("7012121212"));
        when(event.getValue()).thenReturn(patientEvent);
        when(eventRequest.getEvent()).thenReturn(event);

        PortletSession portletSession1 = mock(PortletSession.class);
        when(portletSession1.getAttribute("personId")).thenReturn("191201010101");
        when(eventRequest.getPortletSession()).thenReturn(portletSession1);

        // When
        sieViewController.pollForChange(resourceRequest, resourceResponse);
        // We get here only since the above method times out...
        sieViewController.changeListener(eventRequest);

        // Then
        // ... therefore, "update=false" is written to the response.
        assertEquals("update=false", resourceResponse.getContentAsString());
        verify(portletSession1).setAttribute("personId", "701212-1212");
    }

    @Test
    public void testChangeListenerNoChangeInSession() throws Exception {

        // Given
        MockResourceResponse resourceResponse = new MockResourceResponse();
        ResourceRequest resourceRequest = mock(ResourceRequest.class);

        PortletSession portletSession = mock(PortletSession.class);
        when(portletSession.getId()).thenReturn("1234");
        when(resourceRequest.getPortletSession()).thenReturn(portletSession);

        EventRequest eventRequest = mock(EventRequest.class);
        Event event = mock(Event.class);
        PatientEvent patientEvent = mock(PatientEvent.class);
        when(patientEvent.getPersonNummer()).thenReturn(PersonNummer.personummer("7012121212"));
        when(event.getValue()).thenReturn(patientEvent);
        when(eventRequest.getEvent()).thenReturn(event);

        PortletSession portletSession1 = mock(PortletSession.class);
        when(portletSession1.getAttribute("personId")).thenReturn("701212-1212");
        when(eventRequest.getPortletSession()).thenReturn(portletSession1);

        // When
        sieViewController.pollForChange(resourceRequest, resourceResponse);
        // We get here only since the above method times out...
        sieViewController.changeListener(eventRequest);

        // Then
        // ... therefore, "update=false" is written to the response.
        assertEquals("update=false", resourceResponse.getContentAsString());
        verify(portletSession1, times(0)).setAttribute(anyString(), anyString()); // No call to setAttribute(...)
    }

    @Test
    public void testResetListener() throws Exception {

        // Given
        EventRequest eventRequest = mock(EventRequest.class);
        PortletSession portletSession = mock(PortletSession.class);
        when(portletSession.getAttribute("personId")).thenReturn("191201010101");
        when(eventRequest.getPortletSession()).thenReturn(portletSession);

        // When
        sieViewController.resetListener(eventRequest);

        // Then
        verify(portletSession).removeAttribute("personId");
    }

    @Test
    public void testResetListenerNothingToRemoveFromSession() throws Exception {

        // Given
        EventRequest eventRequest = mock(EventRequest.class);
        PortletSession portletSession = mock(PortletSession.class);
        when(portletSession.getAttribute("personId")).thenReturn(null);
        when(eventRequest.getPortletSession()).thenReturn(portletSession);

        // When
        sieViewController.resetListener(eventRequest);

        // Then
        verify(portletSession, times(0)).removeAttribute("personId");
    }
}
