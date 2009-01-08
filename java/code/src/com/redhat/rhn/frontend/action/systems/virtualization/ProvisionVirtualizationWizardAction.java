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
package com.redhat.rhn.frontend.action.systems.virtualization;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.kickstart.KickstartHelper;
import com.redhat.rhn.frontend.action.kickstart.ScheduleKickstartWizardAction;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnValidationHelper;
import com.redhat.rhn.frontend.struts.wizard.WizardStep;
import com.redhat.rhn.manager.kickstart.KickstartScheduleCommand;
import com.redhat.rhn.manager.kickstart.ProvisionVirtualInstanceCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.DynaActionForm;
import org.cobbler.Profile;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
/**
 * ProvisionVirtualizationWizardAction extends ScheduleKickstartWizardAction
 * @version $Rev$
 */
public class ProvisionVirtualizationWizardAction extends ScheduleKickstartWizardAction {

    public static final String MEMORY_ALLOCATION = "memoryAllocation";
    public static final String VIRTUAL_CPUS = "virtualCpus";
    public static final String VIRTUAL_BRIDGE = "virtBridge";
    public static final String VIRTUAL_FILE_PATH = "diskPath";
    public static final String LOCAL_STORAGE_MB = "localStorageMegabytes";
    
    
    public static final String GUEST_NAME = "guestName";
    public static final int MIN_NAME_SIZE = 4;
    public static final int MAX_CPU = 32;
    
    private Profile cobblerProfile;
    
    private LocalizationService ls = LocalizationService.getInstance();
    
    /**
     * {@inheritDoc}
     */
    public ActionForward runFirst(ActionMapping mapping, DynaActionForm form, 
            RequestContext ctx, HttpServletResponse response, 
            WizardStep step) throws Exception {
          
        
        if (StringUtils.isEmpty(form.getString(MEMORY_ALLOCATION))) {
            form.set(MEMORY_ALLOCATION, "");                            
        }

        if (StringUtils.isEmpty(form.getString(VIRTUAL_CPUS))) {
            form.set(VIRTUAL_CPUS, "");
        }

        if (StringUtils.isEmpty(form.getString(LOCAL_STORAGE_MB))) {
                form.set(LOCAL_STORAGE_MB, "");
        }
        

        return super.runFirst(mapping, form, ctx, response, step);
    }

    /**
     * {@inheritDoc}
     */
    public ActionForward runSecond(ActionMapping mapping, DynaActionForm form, 
            RequestContext ctx, HttpServletResponse response, 
            WizardStep step) throws Exception {        

        return super.runSecond(mapping, form, ctx, response, step);
    }

    /**
     * {@inheritDoc}
     */
    public ActionForward runThird(ActionMapping mapping, DynaActionForm form, 
            RequestContext ctx, HttpServletResponse response, 
            WizardStep step) throws Exception {        
        if (!validateFirstSelections(form, ctx)) {            
            return runFirst(mapping, form, ctx, response, step);
        }
            
        ActionErrors errors = validateInput(form);
        if (!errors.isEmpty()) {
            addErrors(ctx.getRequest(), errors);
            //saveMessages(ctx.getRequest(), errors);
            return runFirst(mapping, form, ctx, response, step);
        }
        
        String scheduleAsap = form.getString("scheduleAsap");
        Date scheduleTime = null;
        if (scheduleAsap != null && scheduleAsap.equals("false")) {
            scheduleTime = (Date) form.get("scheduleDate");
        }
        else {
            scheduleTime = new Date();
        }
        KickstartHelper helper = new KickstartHelper(ctx.getRequest());
        
        ProvisionVirtualInstanceCommand cmd = getScheduleCommand(form,
                                ctx, scheduleTime, helper.getKickstartHost());
        
        if (StringUtils.isEmpty(form.getString(USE_EXISTING_PROFILE))) {
            form.set(USE_EXISTING_PROFILE, Boolean.TRUE.toString());
        }

        boolean useExistingProfile = 
            BooleanUtils.toBoolean(form.getString(USE_EXISTING_PROFILE));
        if (useExistingProfile) {
            cmd.setActivationType(
                KickstartScheduleCommand.ACTIVATION_TYPE_EXISTING);
        }
        else {
            cmd.setActivationType(
                KickstartScheduleCommand.ACTIVATION_TYPE_KEY);
            cmd.setActivationKeyId((Long) form.get(ACTIVATION_KEY));
        }
        
        cmd.setKernelParams(form.getString(KERNEL_PARAMS));
                                    
        cmd.setProfileType(form.getString("targetProfileType"));
        cmd.setServerProfileId((Long) form.get("targetProfile"));
        cmd.setProfileId((Long) form.get("targetProfile"));
        
        
        cmd.setGuestName(GUEST_NAME);
        
        
        //If the virt options are overridden use them, otherwise use 
        // The profile's values
        if (!StringUtils.isEmpty(form.getString(MEMORY_ALLOCATION))) {
            cmd.setMemoryAllocation(new Long(form.getString(MEMORY_ALLOCATION)));
        }
        else {
            cmd.setMemoryAllocation(new Long(this.getCobblerProfile().getVirtRam()));
        }
        
        if (!StringUtils.isEmpty(form.getString(VIRTUAL_CPUS))) {
            cmd.setVirtualCpus(new Long(form.getString(VIRTUAL_CPUS)));
        }        
        else {
            cmd.setVirtualCpus(new Long(this.getCobblerProfile().getVirtCpus()));
        }
        
        if (!StringUtils.isEmpty(form.getString(LOCAL_STORAGE_MB))) {
            cmd.setLocalStorageSize(new Long(form.getString(LOCAL_STORAGE_MB)));
        } 
        else {
            cmd.setLocalStorageSize(new Long(this.getCobblerProfile().getVirtFileSize()));
        }
        
        if (!StringUtils.isEmpty(form.getString(VIRTUAL_BRIDGE))) {
            cmd.setVirtBridge(form.getString(VIRTUAL_BRIDGE));
        }
        else {
            cmd.setVirtBridge(this.getCobblerProfile().getVirtBridge());
        }
        
        if (!StringUtils.isEmpty(form.getString(VIRTUAL_FILE_PATH))) {
            cmd.setFilePath(form.getString(VIRTUAL_FILE_PATH));
        }
        else {
            cmd.setFilePath(this.getCobblerProfile().getVirtPath());
        }
                
        storeProxyInfo(form, ctx, cmd);
        // Store the new KickstartSession to the DB.
        ValidatorError ve = cmd.store();
        if (ve != null) {
            errors = RhnValidationHelper.validatorErrorToActionErrors(ve);
            if (!errors.isEmpty()) {
                getStrutsDelegate().saveMessages(ctx.getRequest(), errors);
                return runFirst(mapping, form, ctx, response, step);
            }
        }

        createSuccessMessage(ctx.getRequest(), "kickstart.schedule.success", 
                LocalizationService.getInstance().formatDate(scheduleTime));
        Map params = new HashMap();
        params.put(RequestContext.SID, form.get(RequestContext.SID));
        
        return getStrutsDelegate().forwardParams(mapping.findForward("success"), params);
    }
    @Override
    protected KickstartScheduleCommand getKickstartScheduleCommand(Long sid,
                                                                   User currentUser) {
        return (KickstartScheduleCommand)
            new ProvisionVirtualInstanceCommand(sid, currentUser);
    }
    
    
    private ActionErrors  validateInput(DynaActionForm form) {
        ActionErrors errors = new ActionErrors();
        String name = form.getString(GUEST_NAME);
        
        if (name.length() != 0 && name.length() < MIN_NAME_SIZE) {
            errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage(
                    "frontend.actions.systems.virt.invalidguestnamelength", 
                    (MIN_NAME_SIZE)));
        }

        if (!StringUtils.isEmpty(form.getString(MEMORY_ALLOCATION))) {
            try {
                Long memory = Long.parseLong(form.getString(MEMORY_ALLOCATION));
                if (memory <= 0) {
                    throw new NumberFormatException();
                }
            } 
            catch (NumberFormatException e) {
                errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage(
                        "frontend.actions.systems.virt.invalidmemvalue"));
            }
        }
        
        if (!StringUtils.isEmpty(form.getString(VIRTUAL_CPUS))) {
            try {
                Long cpus = Long.parseLong(form.getString(VIRTUAL_CPUS));
                if (cpus <= 0 || cpus > MAX_CPU) {
                    throw new NumberFormatException();
                }
            } 
            catch (NumberFormatException e) {
                errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage(
                        "frontend.actions.systems.virt.invalidcpuvalue", 
                                (MAX_CPU + 1)));
            }
        }
        
        if (!StringUtils.isEmpty(form.getString(LOCAL_STORAGE_MB))) {        
            try {
                Long storage = Long.parseLong(form.getString(LOCAL_STORAGE_MB));
                if (storage <= 0) {
                    throw new NumberFormatException();
                }
            } 
            catch (NumberFormatException e) {
                errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage(
                        "frontend.actions.systems.virt.invalidstoragevalue"));
            }
        }

        return errors;
    }
    
    /**
     * Get the cobbler profile
     * @return the cobbler profile
     */
    public Profile getCobblerProfile() {
        return cobblerProfile;
    }
    
    @Override
    protected ProvisionVirtualInstanceCommand getScheduleCommand(DynaActionForm form,
            RequestContext ctx, Date scheduleTime, String host) {
        String cobblerId = form.getString(RequestContext.COBBLER_ID);
        User user = ctx.getLoggedInUser();
        ProvisionVirtualInstanceCommand cmd;
        KickstartData data = KickstartFactory.
                lookupKickstartDataByCobblerIdAndOrg(user.getOrg(), cobblerId);
        cobblerProfile = org.cobbler.Profile.lookupById(
                CobblerXMLRPCHelper.getConnection(user), cobblerId);        
        
        if (data != null) {
            cmd = 
                new ProvisionVirtualInstanceCommand(
                        (Long) form.get(RequestContext.SID),
                        data,
                        ctx.getCurrentUser(),
                        scheduleTime,
                        host);            
        }
        else {
            cmd = ProvisionVirtualInstanceCommand.createCobblerScheduleCommand((Long)
                     form.get(RequestContext.SID), cobblerProfile.getName(), 
                     user, scheduleTime,  host);
        }
        return cmd;
    }
}
