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
package com.redhat.rhn.frontend.action.rhnpackage.profile;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.action.rhnpackage.PackageAction;
import com.redhat.rhn.domain.rhnpackage.MissingPackagesException;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.manager.profile.ProfileManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.system.SystemManager;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * MissingPackageAction
 * @version $Rev$
 */
public class MissingPackageAction extends BaseProfilesAction {
    
    private boolean isSystemSync(RequestContext rctx) {
        String s = rctx.getParam("sync", true);
        return "system".equals(s);
    }
    
    private boolean isProfileSync(RequestContext rctx) {
        String s = rctx.getParam("sync", true);
        return "profile".equals(s);
    }
    
    private PackageAction syncToVictim(RequestContext requestContext, Long sid,
            RhnSet pkgs, String option) {
        
        PackageAction pa = null;
        Long time = requestContext.getParamAsLong("date");
        Date earliest;
        if (time == null) {
            earliest = new Date();
        }
        else {
            earliest = new Date(time.longValue());
        }
        
        if (isProfileSync(requestContext)) {
            Long prid = requestContext.getRequiredParam("prid");
            
            pa = ProfileManager.syncToProfile(requestContext.getCurrentUser(), sid,
                    prid, pkgs.getElementValues(), option, earliest);
            
            if (pa == null) {
                createMessage(requestContext.getRequest(), "message.nopackagestosync");
                return null;
            }
            
            
            
            List args = new ArrayList();
            args.add(sid.toString());
            args.add(pa.getId().toString());
            args.add(requestContext.lookupAndBindServer().getName());
            args.add(ProfileManager.lookupByIdAndOrg(prid,
                    requestContext.getCurrentUser().getOrg()).getName());
            
            createMessage(requestContext.getRequest(), "message.syncpackages", args);
        }
        else if (isSystemSync(requestContext)) {
            Long sid1 = requestContext.getRequiredParam("sid_1");
            pa = ProfileManager.syncToSystem(requestContext.getCurrentUser(), sid,
                    sid1, pkgs.getElementValues(), option, earliest);
            
            if (pa == null) {
                createMessage(requestContext.getRequest(), "message.nopackagestosync");
                return null;
            }
            
            List args = new ArrayList();
            args.add(sid.toString());
            args.add(pa.getId().toString());
            args.add(requestContext.lookupAndBindServer().getName());
            args.add(SystemManager.lookupByIdAndUser(sid1,
                    requestContext.getCurrentUser()).getName());
            
            createMessage(requestContext.getRequest(), "message.syncpackages", args);
        }
        
        addHardwareMessage(pa, requestContext);
        
        return pa;
    }
    
    /**
     * Callback for the Select New Package Profile button, basically
     * forwards to the main Package -> Profile page allowing the user
     * to select a new package profile to compare against.
     * @param mapping ActionMapping
     * @param formIn ActionForm
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @return ActionForward
     */
    public ActionForward selectNewPackageProfile(ActionMapping mapping,
            ActionForm formIn,
            HttpServletRequest request,
            HttpServletResponse response) {
        Long sid = new RequestContext(request).getRequiredParam("sid");
        Map params = new HashMap();
        params.put("sid", sid);
        return getStrutsDelegate().forwardParams(mapping.findForward("newprofile"),
                params);
    }
    
    /**
     * Callback for the Remove packages button, it removes the missing packages
     * and proceeds with the package sync.
     * @param mapping ActionMapping
     * @param formIn ActionForm
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @return ActionForward
     */
    public ActionForward removePackagesFromSync(ActionMapping mapping,
            ActionForm formIn,
            HttpServletRequest request,
            HttpServletResponse response) {
        RequestContext rctx = new RequestContext(request);
        Long sid = new RequestContext(request).getRequiredParam("sid");
        //Long prid = RhnHelper.getParamAsLong(request, "prid", true);
        RhnSet pkgs = getSetDecl().get(rctx.getCurrentUser());
        Map params = new HashMap();
        params.put("sid", sid);
     
        try {
            syncToVictim(rctx, sid, pkgs, ProfileManager.OPTION_REMOVE);
            
            return getStrutsDelegate().forwardParams(mapping.findForward("newprofile"),
                    params);
        }
        catch (MissingPackagesException mpe) {
            return getStrutsDelegate().forwardParams(mapping.findForward("newprofile"),
                    params);
        }
    }
    
    /**
     * Callback for the Subscribe to channels button, it attempts to subscribe
     * to the channels containing the Packages.
     * @param mapping ActionMapping
     * @param formIn ActionForm
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @return ActionForward
     */
    public ActionForward subscribeToChannels(ActionMapping mapping,
            ActionForm formIn,
            HttpServletRequest request,
            HttpServletResponse response) {
        RequestContext requestContext = new RequestContext(request);
        Long sid = requestContext.getRequiredParam("sid");
        RhnSet pkgs = getSetDecl().get(requestContext.getCurrentUser());
        Map params = new HashMap();
        params.put("sid", sid);
        
        try {
            syncToVictim(requestContext, sid, pkgs, ProfileManager.OPTION_SUBSCRIBE);

            return getStrutsDelegate().forwardParams(mapping.findForward("newprofile"),
                    params);
        }
        catch (MissingPackagesException mpe) {
            return getStrutsDelegate().forwardParams(mapping.findForward("newprofile"),
                    params);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected DataResult getDataResult(User user, 
                                       ActionForm formIn, 
                                       HttpServletRequest request) {
        RequestContext requestContext = new RequestContext(request);
        Long sid = requestContext.getRequiredParam("sid");
        RhnSet pkgs = getSetDecl().get(requestContext.getCurrentUser());
        
        if (isProfileSync(requestContext)) {
            Long prid = requestContext.getRequiredParam("prid");

            return ProfileManager.getMissingProfilePackages(requestContext.getCurrentUser(),
                    sid, prid, pkgs.getElementValues(), null);
        }
        else if (isSystemSync(requestContext)) {
            Long sid1 = requestContext.getRequiredParam("sid_1");
            
            return ProfileManager.getMissingSystemPackages(requestContext.getCurrentUser(),
                    sid, sid1, pkgs, null);            
        }
        
        return null;
    }

    /**
     * {@inheritDoc}
     */
    protected void processMethodKeys(Map map) {
        map.put("missingpkgs.jsp.selectnewpackageprofile",
                "selectNewPackageProfile");
        map.put("missingpkgs.jsp.removelistedpackagesfromsync",
                "removePackagesFromSync");
        map.put("missingpkgs.jsp.subscribetochannels",
                "subscribeToChannels");
    }

    /**
     * {@inheritDoc}
     */
    protected void processParamMap(ActionForm formIn, 
                                   HttpServletRequest request, 
                                   Map params) {
        params.put("sid", request.getParameter("sid"));
        params.put("sync", request.getParameter("sync"));
    }

    /**
     * {@inheritDoc}
     */
    protected RhnSetDecl getSetDecl() {
        return RhnSetDecl.PACKAGES_FOR_SYSTEM_SYNC;
    }

}
