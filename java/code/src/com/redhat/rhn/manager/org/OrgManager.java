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
package com.redhat.rhn.manager.org;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.redhat.rhn.common.db.datasource.DataList;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;

import com.redhat.rhn.common.localization.LocalizationService;

import com.redhat.rhn.common.security.PermissionException;

import com.redhat.rhn.common.validator.ValidatorException;

import com.redhat.rhn.domain.channel.ChannelFamily;

import com.redhat.rhn.domain.entitlement.Entitlement;

import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;

import com.redhat.rhn.domain.role.RoleFactory;

import com.redhat.rhn.domain.user.User;

import com.redhat.rhn.frontend.dto.MultiOrgSystemEntitlementsDto;
import com.redhat.rhn.frontend.dto.MultiOrgUserOverview;
import com.redhat.rhn.frontend.dto.TrustedOrgDto;
import com.redhat.rhn.frontend.dto.OrgDto;
import com.redhat.rhn.frontend.dto.OrgEntitlementDto;

import com.redhat.rhn.manager.BaseManager;

import com.redhat.rhn.manager.entitlement.EntitlementManager;

/**
 * OrgManager - Manages MultiOrg tasks
 * @version $Rev$
 */
public class OrgManager extends BaseManager {

    private OrgManager() {
    }
    
    
    /**
     * Basically transfers relevant data
     * from Org object to the Dto object
     * returns a new OrgDto object.
     * This method is typically used in OrgDetails views
     * @param org the org object to transfer from 
     * @return the created Dto. 
     */
    public static OrgDto toDetailsDto(Org org) {
        OrgDto dto = new OrgDto();
        dto.setId(org.getId());
        dto.setName(org.getName());
        dto.setUsers(OrgFactory.getActiveUsers(org));
        dto.setSystems(OrgFactory.getActiveSystems(org));
        dto.setActivationKeys(OrgFactory.getActivationKeys(org));
        dto.setKickstartProfiles(OrgFactory.getKickstarts(org));
        dto.setServerGroups(OrgFactory.getServerGroups(org));
        dto.setConfigChannels(OrgFactory.getConfigChannels(org));
        return dto;
    }    
    

    /**
     * 
     * @param user User to cross security check
     * @return List of Orgs on satellite
     */
    public static DataList<OrgDto> activeOrgs(User user) {
        if (!user.hasRole(RoleFactory.SAT_ADMIN)) {
            // Throw an exception w/error msg so the user knows what went wrong.
            LocalizationService ls = LocalizationService.getInstance();
            PermissionException pex = new PermissionException("User must be a " +
                    RoleFactory.SAT_ADMIN.getName() + " to access the org list");
            pex.setLocalizedTitle(ls.getMessage("permission.jsp.title.orglist"));
            pex.setLocalizedSummary(ls.getMessage("permission.jsp.summary.general"));
            throw pex;
        }
        SelectMode m = ModeFactory.getMode("Org_queries", "orgs_in_satellite");

        return DataList.getDataList(m, Collections.EMPTY_MAP,
                Collections.EMPTY_MAP);
    }

    /**
     * 
     * @param user User to cross security check
     * @return List of Orgs on satellite
     */
    public static DataList<TrustedOrgDto> trustedOrgs(User user) {
        if (!user.hasRole(RoleFactory.ORG_ADMIN)) {
            // Throw an exception w/error msg so the user knows what went wrong.
            LocalizationService ls = LocalizationService.getInstance();
            PermissionException pex = new PermissionException("User must be a " +
                    RoleFactory.ORG_ADMIN.getName() + " to access the trusted org list");
            pex.setLocalizedTitle(ls.getMessage("permission.jsp.title.orglist"));
            pex.setLocalizedSummary(ls.getMessage("permission.jsp.summary.general"));
            throw pex;
        }
        SelectMode m = ModeFactory.getMode("Org_queries", "trusted_orgs");

        Long orgIdIn = user.getOrg().getId();        
        Map params = new HashMap();
        params.put("org_id", orgIdIn);

        return DataList.getDataList(m, params,
                Collections.EMPTY_MAP);
    }

    /**
     * 
     * @param orgIdIn to check active users
     * @return DataList of UserOverview Objects
     */
    public static DataList<MultiOrgUserOverview> activeUsers(Long orgIdIn) {
        SelectMode m = ModeFactory.getMode("User_queries", "users_in_multiorg");
        Map params = new HashMap();
        params.put("org_id", orgIdIn);
        return DataList.getDataList(m, params, Collections.EMPTY_MAP);
    }

    /**
     * 
     * @return all users on sat
     */
    public static DataList allUsers() {
        SelectMode m = ModeFactory.getMode("User_queries",
                "all_users_in_multiorg");
        return DataList.getDataList(m, Collections.EMPTY_MAP,
                Collections.EMPTY_MAP);
    }

    /**
     * 
     * @return all entitlements across all orgs on sat
     */
    public static DataList <MultiOrgSystemEntitlementsDto> allOrgsEntitlements() {
        SelectMode m = ModeFactory.getMode("Org_queries",
                "get_total_entitlement_counts");
        return DataList.getDataList(m, Collections.EMPTY_MAP,
                Collections.EMPTY_MAP);
    }
    
    /**
     * @param entLabel Entitlement Label
     * @return single entitlement, entLabel, across all orgs on sat
     */
    public static DataList allOrgsSingleEntitlement(String entLabel) {
        SelectMode m = ModeFactory.getMode("Org_queries",
                "get_org_entitlement_counts");
        Map params = new HashMap();
        params.put("label", entLabel);
        return DataList.getDataList(m, params,
                Collections.EMPTY_MAP);
    }
    
    /**
     * @param entLabel Entitlement Label
     * @return single entitlement, entLabel, across all orgs on sat
     */
    public static DataList getSatEntitlementUsage(String entLabel) {
        SelectMode m = ModeFactory.getMode("Org_queries",
                "get_sat_entitlement_usage");
        Map params = new HashMap();
        params.put("label", entLabel);
        return DataList.getDataList(m, params,
                Collections.EMPTY_MAP);
    }


    /**
     * Lookup orgs with servers with access to any channel that's a part of the
     * given family.
     * @param family Channel family
     * @param user User performing the query
     * @return List of orgs.
     */
    public static List<Org> orgsUsingChannelFamily(ChannelFamily family,
            User user) {

        if (!user.hasRole(RoleFactory.SAT_ADMIN)) {
            // Throw an exception w/error msg so the user knows what went wrong.
            LocalizationService ls = LocalizationService.getInstance();
            PermissionException pex = new PermissionException("User must be a " +
                    RoleFactory.SAT_ADMIN.getName() + " to access the org list");
            pex.setLocalizedTitle(ls.getMessage("permission.jsp.title.orglist"));
            pex.setLocalizedSummary(ls.getMessage("permission.jsp.summary.general"));
            throw pex;
        }

        return OrgFactory.lookupOrgsUsingChannelFamily(family);
    }

    /**
     * 
     * @param entLabel to check used active orgs
     * @return DataList of Objects
     */
    public static DataList getUsedActiveOrgCount(String entLabel) {
        SelectMode m = ModeFactory
                .getMode("Org_queries", "get_used_org_counts");
        Map params = new HashMap();
        params.put("label", entLabel);
        return DataList.getDataList(m, params, Collections.EMPTY_MAP);
    }

    /**
     * @param user User to cross security check
     * @param entLabel to check used active orgs 
     * @return DataList of Objects
     */
    public static DataList getAllOrgs(User user, String entLabel) {
        if (!user.hasRole(RoleFactory.SAT_ADMIN)) {
            // Throw an exception w/error msg so the user knows what went wrong.
            LocalizationService ls = LocalizationService.getInstance();
            PermissionException pex = new PermissionException("User must be a " +
                    RoleFactory.SAT_ADMIN.getName() + " to access the org list");
            pex.setLocalizedTitle(ls.getMessage("permission.jsp.title.orglist"));
            pex.setLocalizedSummary(ls.getMessage("permission.jsp.summary.general"));
            throw pex;
        }
        SelectMode m = ModeFactory
                .getMode("Org_queries", "get_all_orgs");
        Map params = new HashMap();
        params.put("label", entLabel);
        return DataList.getDataList(m, params, Collections.EMPTY_MAP);
    }

    /**
     * Returns the total number of orgs on this satellite.
     * @param user User performing the query.
     * @return Total number of orgs.
     */
    public static Long getTotalOrgCount(User user) {
        if (!user.hasRole(RoleFactory.SAT_ADMIN)) {
            // Throw an exception w/error msg so the user knows what went wrong.
            LocalizationService ls = LocalizationService.getInstance();
            PermissionException pex = new PermissionException("User must be a " +
                    RoleFactory.SAT_ADMIN.getName() + " to access the org list");
            pex.setLocalizedTitle(ls.getMessage("permission.jsp.title.orglist"));
            pex.setLocalizedSummary(ls.getMessage("permission.jsp.summary.general"));
            throw pex;
        }

        return OrgFactory.getTotalOrgCount();
    }

    /**
     * Returns the total number of orgs on this satellite.
     * @param user User performing the query.
     * @return Total number of orgs.
     */
    public static List<Org> allOrgs(User user) {
        if (!user.hasRole(RoleFactory.SAT_ADMIN)) {
            // Throw an exception w/error msg so the user knows what went wrong.
            LocalizationService ls = LocalizationService.getInstance();
            PermissionException pex = new PermissionException("User must be a " +
                    RoleFactory.SAT_ADMIN.getName() + " to access the org list");
            pex.setLocalizedTitle(ls.getMessage("permission.jsp.title.orglist"));
            pex.setLocalizedSummary(ls.getMessage("permission.jsp.summary.general"));
            throw pex;
        }

        return OrgFactory.lookupAllOrgs();
    }
    
    /**
     * Check if the passed in org is a valid name and raises an 
     * exception if its invalid..
     * @param newOrgName the orgname to be applied 
     * @throws ValidatorException in case of bad/duplicate name
     */
    public static void checkOrgName(String newOrgName) throws ValidatorException {
        if (newOrgName == null || 
                newOrgName.trim().length() == 0 ||
                newOrgName.trim().length() < 3 ||
                newOrgName.trim().length() > 128) {
            ValidatorException.raiseException("orgname.jsp.error");
        }
        else if (OrgFactory.lookupByName(newOrgName) != null) {
            ValidatorException.raiseException("error.org_already_taken", newOrgName);
        }        
    }

    /**
     * Returns a list of entitlement dtos for a given org ..
     * Basically collects a list of all entitlements and provides
     * a DTO with information abt the entitlements
     *  (like current members, available members etc.)
     * @param org the org to lookup on
     * @return List of dtos for all entitlements.
     */
    public static List <OrgEntitlementDto> listEntitlementsFor(Org org) {
        List <OrgEntitlementDto> dtos = new LinkedList<OrgEntitlementDto>();
        List <Entitlement> entitlements = new LinkedList<Entitlement>();
        entitlements.addAll(EntitlementManager.getBaseEntitlements());
        entitlements.addAll(EntitlementManager.getAddonEntitlements()); 
        for (Entitlement ent : entitlements) {
            dtos.add(new OrgEntitlementDto(ent, org));
        }
        return dtos;
    }

}
