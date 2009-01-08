/**
 * Copyright (c) 2008 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 * 
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation. 
 */
package com.redhat.rhn.frontend.action.kickstart;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.util.DatePicker;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.rhnpackage.profile.Profile;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.systems.sdc.SdcHelper;
import com.redhat.rhn.frontend.dto.OrgProxyServer;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnValidationHelper;
import com.redhat.rhn.frontend.struts.wizard.RhnWizardAction;
import com.redhat.rhn.frontend.struts.wizard.WizardStep;
import com.redhat.rhn.frontend.taglibs.helpers.ListViewHelper;
import com.redhat.rhn.manager.kickstart.KickstartScheduleCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerSystemCreateCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;
import com.redhat.rhn.manager.system.SystemManager;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
    

/**
 * blah blah 
 * 
 * @version $Rev $
 */
public class ScheduleKickstartWizardAction extends RhnWizardAction {

    /**
     * Logger for this class
     */
    private static Logger log = Logger
            .getLogger(ScheduleKickstartWizardAction.class);
    
    public static final String SYNCH_PACKAGES = "syncPackages";
    public static final String ACTIVATION_KEYS = "activationKeys";
    public static final String SYNCH_SYSTEMS = "syncSystems";
    public static final String HAS_PROFILES = "hasProfiles";
    public static final String HAS_PROXIES = "hasProxies";
    public static final String SYNC_PACKAGE_DISABED = "syncPackageDisabled";
    public static final String SYNC_SYSTEM_DISABLED = "syncSystemDisabled";
    public static final String PROXIES = "proxies";
    public static final String KERNEL_PARAMS = "kernelParams";
    public static final String PROXY_HOST = "proxyHost";
    public static final String USE_EXISTING_PROFILE = "useExistingProfile";
    public static final String ACTIVATION_KEY = "activationKey";
    public static final String IS_VIRTUAL_GUEST = "isVirtualGuest";
    public static final String HOST_SID = "hostSid";
    public static final String VIRT_HOST_IS_REGISTERED = "virtHostIsRegistered";
    
    /**
     * {@inheritDoc}
     */
    protected void generateWizardSteps(Map wizardSteps) {
        List methods = findMethods("run");
        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            Method m = (Method) iter.next();
            String stepName = m.getName().substring(3).toLowerCase();
            WizardStep wizStep = new WizardStep();
            wizStep.setWizardMethod(m);
            log.debug("Step name: " + stepName);
            if (stepName.equals("first")) {
                wizStep.setNext("second");
                wizardSteps.put(RhnWizardAction.STEP_START, wizStep);
            }
            else if (stepName.equals("second")) {
                wizStep.setPrevious("first");
                wizStep.setNext("third");
            }
            else if (stepName.equals("third")) {
                wizStep.setPrevious("second");
            }
            else if (stepName.equals("fourth")) {
                wizStep.setPrevious("first");
            }
            wizardSteps.put(stepName, wizStep);
        }        
    }    
    
    private List getProxies(KickstartScheduleCommand cmd) {
        List proxies = cmd.getProxies();
        if (proxies == null) {
            return Collections.EMPTY_LIST;
        }
        
        List formatted = new LinkedList();

        formatted.add(lvl10n("kickstart.schedule.default.proxy.jsp", ""));
        for (Iterator itr = proxies.iterator(); itr.hasNext();) {
            OrgProxyServer serv = (OrgProxyServer) itr.next();
            
            formatted.add(lv(serv.getName() + " (" + serv.getCheckin() + ")", 
                                                      serv.getId().toString()));
        }
        return formatted;
    }

    /**
     * The first step in the wizard
     * @param mapping ActionMapping for struts
     * @param form DynaActionForm representing the form
     * @param ctx RequestContext request context
     * @param response HttpServletResponse response object
     * @param step WizardStep what step are we on?
     *
     * @return ActionForward struts action forward
     * @throws Exception if something goes amiss
     */
    public ActionForward runFirst(ActionMapping mapping, DynaActionForm form, 
            RequestContext ctx, HttpServletResponse response, 
            WizardStep step) throws Exception {
        log.debug("runFirst");
        Long sid = (Long) form.get(RequestContext.SID);
        User user = ctx.getCurrentUser();
        
        KickstartScheduleCommand cmd
            = getKickstartScheduleCommand(sid, user);
        
        Server system = SystemManager.lookupByIdAndUser(sid, user);
        if (system.isVirtualGuest()) {
            ctx.getRequest().setAttribute(IS_VIRTUAL_GUEST, Boolean.TRUE.toString());
            
            ctx.getRequest().setAttribute(VIRT_HOST_IS_REGISTERED, 
                    Boolean.FALSE.toString());
            if (system.getVirtualInstance().getHostSystem() != null) {
                Long hostSid = system.getVirtualInstance().getHostSystem().getId();
                ctx.getRequest().setAttribute(VIRT_HOST_IS_REGISTERED, 
                        Boolean.TRUE.toString());
                ctx.getRequest().setAttribute(HOST_SID, hostSid);
            }
        }
        else {
            ctx.getRequest().setAttribute(IS_VIRTUAL_GUEST, Boolean.FALSE.toString());
        }

        addRequestAttributes(ctx, cmd);
        checkForKickstart(form, cmd, ctx);
        ListViewHelper helper = new ListViewHelper(ctx, "label");
        DataResult profiles = cmd.getKickstartProfiles();
        if (profiles.size() == 0) {
            addMessage(ctx.getRequest(), "kickstart.schedule.noprofiles");
            ctx.getRequest().setAttribute(HAS_PROFILES, Boolean.FALSE.toString());
        } 
        else {
            ctx.getRequest().setAttribute(HAS_PROFILES, Boolean.TRUE.toString());
        }
        
        List proxies = getProxies(cmd);
        if (proxies != null && proxies.size() > 0) {
            ctx.getRequest().setAttribute(HAS_PROXIES, Boolean.TRUE.toString());
            ctx.getRequest().setAttribute(PROXIES, proxies);
            if (form.get(PROXY_HOST) == null) {
                form.set(PROXY_HOST, "");
            }
        }
        else {
            ctx.getRequest().setAttribute(HAS_PROXIES, Boolean.FALSE.toString());       
        }
        
        helper.setData(profiles);
        helper.isFiltering(true);
        helper.prepare();
        
        //create and prepopulate the date picker.
        DatePicker picker = getStrutsDelegate().prepopulateDatePicker(ctx.getRequest(),
                form, "date", DatePicker.YEAR_RANGE_POSITIVE);
        
        SdcHelper.ssmCheck(ctx.getRequest(), system.getId(), user);
        ctx.getRequest().setAttribute("date", picker);
        ActionForward retval =  mapping.findForward("first");
        return retval;
    }

    /**
     * The second step in the wizard
     * @param mapping ActionMapping for struts
     * @param form DynaActionForm representing the form
     * @param ctx RequestContext request context
     * @param response HttpServletResponse response object
     * @param step WizardStep what step are we on?
     *
     * @return ActionForward struts action forward
     * @throws Exception if something goes amiss
     */
    public ActionForward runSecond(ActionMapping mapping, DynaActionForm form, 
            RequestContext ctx, HttpServletResponse response, 
            WizardStep step) throws Exception {
        log.debug("runSecond");
        Long sid = (Long) form.get(RequestContext.SID);
        User user = ctx.getCurrentUser();
        
        
        if (!validateFirstSelections(form, ctx)) {   
            return runFirst(mapping, form, ctx, response, step);
        }
        KickstartScheduleCommand cmd = getScheduleCommand(form, ctx, null, null);
        
        checkForKickstart(form, cmd, ctx);
        addRequestAttributes(ctx, cmd);
        form.set(ACTIVATION_KEYS, cmd.getActivationKeys());
        DataResult packageProfiles = cmd.getProfiles();
        form.set(SYNCH_PACKAGES, packageProfiles);
        DataResult systemProfiles = cmd.getCompatibleSystems();
        form.set(SYNCH_SYSTEMS, systemProfiles);

        // Disable the package/system sync radio buttons if no profiles are available:
        String syncPackageDisabled = "false";
        if (packageProfiles.size() == 0) {
            syncPackageDisabled = "true";
        }
        String syncSystemDisabled = "false";
        if (systemProfiles.size() == 0) {
            syncSystemDisabled = "true";
        }
        ctx.getRequest().setAttribute(SYNC_PACKAGE_DISABED, syncPackageDisabled);
        ctx.getRequest().setAttribute(SYNC_SYSTEM_DISABLED, syncSystemDisabled);

        if (StringUtils.isEmpty(form.getString(USE_EXISTING_PROFILE))) {
            form.set(USE_EXISTING_PROFILE, Boolean.TRUE.toString());
        }
        SdcHelper.ssmCheck(ctx.getRequest(), sid, user);
        return mapping.findForward("second");
    }
    
    protected void addRequestAttributes(RequestContext ctx, 
            KickstartScheduleCommand cmd) {
        ctx.getRequest().setAttribute(RequestContext.SYSTEM, cmd.getServer());
        ctx.getRequest().setAttribute(RequestContext.KICKSTART, cmd.getKsdata());
   }
    
    /**
     * The third step in the wizard
     * @param mapping ActionMapping for struts
     * @param form DynaActionForm representing the form
     * @param ctx RequestContext request context
     * @param response HttpServletResponse response object
     * @param step WizardStep what step are we on?
     *
     * @return ActionForward struts action forward
     * @throws Exception if something goes amiss
     */
    public ActionForward runThird(ActionMapping mapping, DynaActionForm form, 
            RequestContext ctx, HttpServletResponse response, 
            WizardStep step) throws Exception {
        log.debug("runThird");
        if (!validateFirstSelections(form, ctx)) {            
            return runFirst(mapping, form, ctx, response, step);
        }
        String scheduleAsap = form.getString("scheduleAsap");
        Date scheduleTime = null;
        if (scheduleAsap != null && scheduleAsap.equals("false")) {
            scheduleTime = getStrutsDelegate().readDatePicker(form, "date",
                    DatePicker.YEAR_RANGE_POSITIVE);
        }
        else {
            scheduleTime = new Date();
        }
        KickstartHelper helper = new KickstartHelper(ctx.getRequest());
        KickstartScheduleCommand cmd = getScheduleCommand(form, ctx, 
                                    scheduleTime, helper.getKickstartHost());
        
        
        cmd.setKernelParams(form.getString(KERNEL_PARAMS));
        boolean advancedConfig = false;
        // if existing profile is not set then actions froms from normal config page
        if (StringUtils.isEmpty(form.getString(USE_EXISTING_PROFILE))) {
            form.set(USE_EXISTING_PROFILE, Boolean.TRUE.toString());
        }
        else {
            advancedConfig = true;
        }
        
        if (BooleanUtils.toBoolean(form.getString(USE_EXISTING_PROFILE))) {
            cmd.setActivationType(KickstartScheduleCommand.ACTIVATION_TYPE_EXISTING);
        }
        else {
            cmd.setActivationType(KickstartScheduleCommand.ACTIVATION_TYPE_KEY);
            cmd.setActivationKeyId((Long) form.get(ACTIVATION_KEY));
        }
        if (!cmd.isCobblerOnly()) {
            // now setup system/package profiles for kickstart to sync
            Profile pkgProfile = cmd.getKsdata().getKickstartDefaults().getProfile();
            Long packageProfileId = pkgProfile != null ? pkgProfile.getId() : null;        
            
            //if user did not override package profile, then grab from ks profile if avail
            if (!advancedConfig && packageProfileId != null) {
                cmd.setProfileId(packageProfileId);
                cmd.setProfileType(KickstartScheduleCommand.TARGET_PROFILE_TYPE_PACKAGE);
            }
            else {
                /*NOTE: these values are essentially ignored if user did 
                 not go through advanced config and there is no package
                 profile to sync in the kickstart profile */
                cmd.setProfileType(form.getString("targetProfileType"));
                cmd.setServerProfileId((Long) form.get("targetProfile"));
                cmd.setProfileId((Long) form.get("targetProfile"));
            }
        }
        
        storeProxyInfo(form, ctx, cmd);

        // Store the new KickstartSession to the DB.
        ValidatorError ve = cmd.store();
        if (ve != null) {
            ActionErrors errors = RhnValidationHelper.validatorErrorToActionErrors(ve);
            if (!errors.isEmpty()) {
                getStrutsDelegate().saveMessages(ctx.getRequest(), errors);
                return runFirst(mapping, form, ctx, response, step);
            }
        }
        Map params = new HashMap();
        params.put(RequestContext.SID, form.get(RequestContext.SID));
        

        if (cmd.isCobblerOnly()) {
            createSuccessMessage(ctx.getRequest(), "kickstart.cobbler.schedule.success", 
                    LocalizationService.getInstance().formatDate(scheduleTime));            
            return getStrutsDelegate().forwardParams(
                                mapping.findForward("cobbler-success"), params);
        }
        createSuccessMessage(ctx.getRequest(), "kickstart.schedule.success", 
                LocalizationService.getInstance().formatDate(scheduleTime));        
        return getStrutsDelegate().forwardParams(mapping.findForward("success"), params);
    }

    /**
     * Setup the system for provisioning with cobbler.
     * 
     * @param mapping ActionMapping for struts
     * @param form DynaActionForm representing the form
     * @param ctx RequestContext request context
     * @param response HttpServletResponse response object
     * @param step WizardStep what step are we on?
     *
     * @return ActionForward struts action forward
     * @throws Exception if something goes amiss
     */
    public ActionForward runFourth(ActionMapping mapping, DynaActionForm form, 
            RequestContext ctx, HttpServletResponse response, 
            WizardStep step) throws Exception {
    
        log.debug("runFourth");
        Long sid = (Long) form.get(RequestContext.SID);
        String cobblerId = form.getString(RequestContext.COBBLER_ID);
        
        log.debug("runFourth.cobblerId: " + cobblerId);
        
        User user = ctx.getCurrentUser();
        Server server = SystemManager.lookupByIdAndUser(sid, user);
        
        Map params = new HashMap();
        params.put(RequestContext.SID, sid);
        
        log.debug("Creating cobbler system record");
        org.cobbler.Profile profile = org.cobbler.Profile.
            lookupById(CobblerXMLRPCHelper.getConnection(user), cobblerId);
        CobblerSystemCreateCommand cmd = 
            new CobblerSystemCreateCommand(server, profile.getName());
        cmd.store();
        log.debug("cobbler system record created.");
        String[] args = new String[2];
        args[0] = server.getName();
        args[1] = profile.getName();
        createMessage(ctx.getRequest(), "kickstart.schedule.cobblercreate", args);        
        return getStrutsDelegate().
            forwardParams(mapping.findForward("cobbler-success"), params);
    }
    
    /**
     * Returns the kickstart schedule command
     * @param form the dyna aciton form
     * @param ctx the request context
     * @param scheduleTime the schedule time
     * @param host the  host url.
     * @return the Ks schedule command
     */
    protected KickstartScheduleCommand getScheduleCommand(DynaActionForm form,
            RequestContext ctx, Date scheduleTime, String host) {
        String cobblerId = form.getString(RequestContext.COBBLER_ID);
        User user = ctx.getLoggedInUser();
        KickstartScheduleCommand cmd;
        KickstartData data = KickstartFactory.
                lookupKickstartDataByCobblerIdAndOrg(user.getOrg(), cobblerId);
        if (data != null) {
            cmd = 
                new KickstartScheduleCommand(
                        (Long) form.get(RequestContext.SID),
                        data,
                        ctx.getCurrentUser(),
                        scheduleTime,
                        host);            
        }
        else {
            org.cobbler.Profile profile = org.cobbler.Profile.
                    lookupById(CobblerXMLRPCHelper.getConnection(user), cobblerId);
            cmd = KickstartScheduleCommand.createCobblerScheduleCommand((Long)
                                                form.get(RequestContext.SID),
                                     profile.getName(), user, scheduleTime,  host);
        }
        return cmd;
    }

    /**
     * @param form the form containing the proxy info
     * @param ctx the request context associated to this request
     * @param cmd the kicktstart command to which the 
     *              proxy info will be copied..
     */
    protected void storeProxyInfo(DynaActionForm form, RequestContext ctx,
            KickstartScheduleCommand cmd) {
        // if we need to go through a proxy, do it here. 
        String phost = form.getString(PROXY_HOST);
        
        if (!StringUtils.isEmpty(phost)) {
            cmd.setProxy(SystemManager.lookupByIdAndOrg(new Long(phost), 
                    ctx.getCurrentUser().getOrg())); 
        }
    }
    
    protected boolean validateFirstSelections(DynaActionForm form, RequestContext ctx) {
        boolean retval = false;
        String rawItemSelected = ctx.getRequest().getParameter("items_selected");
        if (rawItemSelected != null) {
            form.set(RequestContext.COBBLER_ID, rawItemSelected);
            if (form.get("scheduleAsap") != null) {
                retval = true;
            }                
        } 
        else if (form.get(RequestContext.COBBLER_ID) != null) {
            return true;
        }        
        return retval;
    }
    
    private void checkForKickstart(DynaActionForm form, KickstartScheduleCommand cmd, 
            RequestContext ctx) {
        if (ActionFactory.doesServerHaveKickstartScheduled((Long)
                form.get(RequestContext.SID))) {
            String[] params = {cmd.getServer().getName()};
            getStrutsDelegate().saveMessage("kickstart.schedule.already.scheduled.jsp", 
                    params, ctx.getRequest());
        }        
    }

    protected KickstartScheduleCommand getKickstartScheduleCommand(Long sid,
                                                                   User currentUser) {
        return new KickstartScheduleCommand(sid, currentUser);
    }

}
