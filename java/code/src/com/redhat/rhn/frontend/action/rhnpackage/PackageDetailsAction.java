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
package com.redhat.rhn.frontend.action.rhnpackage;

import com.redhat.rhn.common.db.datasource.ParameterValueNotFoundException;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.Patch;
import com.redhat.rhn.domain.rhnpackage.PatchSet;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.manager.download.DownloadManager;
import com.redhat.rhn.manager.rhnpackage.PackageManager;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ChannelPackagesAction
 * @version $Rev$
 */
public class PackageDetailsAction extends RhnAction {
   
    private final String PACKAGE_NAME = "package_name";
    
    /** {@inheritDoc} */
    public ActionForward execute(ActionMapping mapping,
            ActionForm formIn,
            HttpServletRequest request,
            HttpServletResponse response) {

        RequestContext requestContext = new RequestContext(request);
        User user =  requestContext.getLoggedInUser();
        
        Package pack;
        long pid;
        
        //If this is an easy one and we have the pid
        if (request.getParameter("pid") != null) {
            pid = requestContext.getRequiredParam("pid");
            pack = PackageFactory.lookupByIdAndUser(pid, user);
        }
        else { //we have to guess
            PackageListItem item = PackageListItem.parse(request.getParameter("id_combo"));
            long nameId = item.getIdOne();
            long evrId = item.getIdTwo();
            
           
            String cidParam = request.getParameter("cid");
            String sidParam = request.getParameter("sid");
            if (cidParam != null) {
                pack = PackageManager.guestimatePackageByChannel(
                   Long.parseLong(cidParam), nameId, evrId, user.getOrg());   
                
            }
            else if (sidParam != null) {
                pack = PackageManager.guestimatePackageBySystem(
                   Long.parseLong(sidParam), nameId, evrId, user.getOrg());   
                
            }
            else {
              throw new ParameterValueNotFoundException("pid, cid, or sid");   
            }
            
            Map params = new HashMap();
            params.put("pid", pack.getId());
            return getStrutsDelegate().forwardParams(mapping.findForward("package"),
                    params);
            
        }
        
        

        request.setAttribute("pack", pack);
        

        String desc = pack.getDescription();
        request.setAttribute("description", desc.replace("\n", "<BR>\n"));
        
        
        
        if (pack instanceof Patch) {
            request.setAttribute("type", "patch");
            request.setAttribute(PACKAGE_NAME, pack.getPackageName().getName());
            request.setAttribute("readme_url", DownloadManager.getPatchReadmeDownloadPath(
                    (Patch) pack, user));
            
        }
        else if (pack instanceof PatchSet) {
            request.setAttribute("type", "patchset");
            request.setAttribute(PACKAGE_NAME, pack.getNameEvra());
            
            request.setAttribute("readme_url", 
                    DownloadManager.getPatchSetReadmeDownloadPath(
                            (PatchSet) pack, user));            
        }
        else {
            request.setAttribute("type", "rpm");
            request.setAttribute(PACKAGE_NAME, pack.getFile());
        }
        
        
        
        request.setAttribute("packArches", 
                PackageFactory.findPackagesWithDifferentArch(pack));
        
        if (DownloadManager.isFileAvailable(pack.getPath())) {
            request.setAttribute("url", DownloadManager.getPackageDownloadPath(pack, user));
        }
        
        if (DownloadManager.isFileAvailable(pack.getSourcePath())) {
            request.setAttribute("srpm_url", 
                    DownloadManager.getPackageSourceDownloadPath(pack, user));
        }        
        
                
        
        request.setAttribute("pid", pid);
        
        
        
        return mapping.findForward("default");

    }
    
    

    
    
}
