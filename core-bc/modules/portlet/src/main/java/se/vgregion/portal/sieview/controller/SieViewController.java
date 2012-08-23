package se.vgregion.portal.sieview.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.EventMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;
import se.vgregion.portal.concurrency.ThreadSynchronizationManager;
import se.vgregion.portal.patient.event.PatientEvent;
import se.vgregion.portal.patient.event.PersonNummer;

import javax.portlet.*;
import javax.xml.namespace.QName;

/**
 * Controller class for managing the SIEview view.
 *
 * @author Patrik Bergström
 */
@Controller
@RequestMapping(value = "VIEW")
public class SieViewController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SieViewController.class);

    private final ThreadSynchronizationManager threadSynchronizationManager =
            ThreadSynchronizationManager.getInstance();

    @Value("${sieViewIframeUrl}")
    private String sieViewIframeUrl;

    /**
     * Render method to show the SIEview client.
     *
     * @param request  the request
     * @param model    the model
     * @return the view
     */
    @RenderMapping
    public String show(RenderRequest request, Model model) {

        PortletSession portletSession = request.getPortletSession();

        String personId = (String) portletSession.getAttribute("personId");

        if (personId != null) {

            model.addAttribute("iframeSrc", String.format(sieViewIframeUrl, personId));

            model.addAttribute("personId", personId);
            return "view";
        }

        return "startForm";
    }


    /**
     * Action method called when the user has chosen a personId. An event is published.
     *
     * @param personId the personId
     * @param request  the request
     * @param response the response
     * @param model    the model
     */
    @ActionMapping(params = {"personId" })
    public void start(@RequestParam String personId, ActionRequest request, ActionResponse response, Model model) {
        if (personId != null) {
            PatientEvent event = new PatientEvent(personId, PatientEvent.DEFAULT_GROUP_CODE);
            QName qname = new QName("http://vgregion.se/patientcontext/events", "pctx.change");
            response.setEvent(qname, event);
        }
    }

    /**
     * Event phase method which receives {@link PatientEvent}s and stores the personId in the portlet session.
     *
     * @param request the request
     * @param model   the model
     * @throws InterruptedException InterruptedException
     */
    @EventMapping("{http://vgregion.se/patientcontext/events}pctx.change")
    public void changeListener(EventRequest request, ModelMap model) throws InterruptedException {
        Event event = request.getEvent();
        PatientEvent patient = (PatientEvent) event.getValue();

        PortletSession portletSession = request.getPortletSession();
        String personIdInSession = (String) portletSession.getAttribute("personId");

        if (!patient.getPersonNummer().equals(PersonNummer.personummer(personIdInSession))) {
            // We have a change
            portletSession.setAttribute("personId", patient.getPersonNummer().getNormal());

        }
        threadSynchronizationManager.notifyBlockedThreads(portletSession);
    }

    /**
     * Event phase method which removes the personId attribute in the portlet session.
     *
     * @param request request
     * @throws InterruptedException InterruptedException
     */
    @EventMapping("{http://vgregion.se/patientcontext/events}pctx.reset")
    public void resetListener(EventRequest request) throws InterruptedException {
        PortletSession portletSession = request.getPortletSession();
        if (portletSession.getAttribute("personId") != null) {
            portletSession.removeAttribute("personId");
            threadSynchronizationManager.notifyBlockedThreads(portletSession);
        }
    }

    /**
     * This method blocks until either {@link SieViewController#resetListener(javax.portlet.EventRequest)} or
     * {@link SieViewController#changeListener(javax.portlet.EventRequest, org.springframework.ui.ModelMap)} is called,
     * or a timeout occurs. If it returns due to an update of the patient context "update=true" will be written to the
     * response. If it returns due to a timeout "update=false" will be written to the response.
     *
     * @param request  the request
     * @param response the response
     */
    @ResourceMapping
    public void pollForChange(ResourceRequest request, ResourceResponse response) {
        threadSynchronizationManager.pollForChange(request.getPortletSession().getId(), response);
    }

}
