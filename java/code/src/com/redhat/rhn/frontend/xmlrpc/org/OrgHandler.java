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
package com.redhat.rhn.frontend.xmlrpc.org;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.common.db.datasource.DataList;
import com.redhat.rhn.common.hibernate.LookupException;

import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.common.validator.ValidatorResult;

import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;

import com.redhat.rhn.domain.entitlement.Entitlement;

import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;

import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;

import com.redhat.rhn.domain.user.User;

import com.redhat.rhn.frontend.dto.MultiOrgEntitlementsDto;
import com.redhat.rhn.frontend.dto.MultiOrgSystemEntitlementsDto;
import com.redhat.rhn.frontend.dto.OrgChannelFamily;
import com.redhat.rhn.frontend.dto.OrgDto;
import com.redhat.rhn.frontend.dto.OrgEntitlementDto;
import com.redhat.rhn.frontend.dto.OrgSoftwareEntitlementDto;

import com.redhat.rhn.frontend.struts.RhnValidationHelper;

import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.InvalidEntitlementException;
import com.redhat.rhn.frontend.xmlrpc.MigrationToSameOrgException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchEntitlementException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchOrgException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchSystemException;
import com.redhat.rhn.frontend.xmlrpc.OrgNotInTrustException;
import com.redhat.rhn.frontend.xmlrpc.SatelliteOrgException;
import com.redhat.rhn.frontend.xmlrpc.ValidationException;

import com.redhat.rhn.manager.channel.ChannelManager;

import com.redhat.rhn.manager.entitlement.EntitlementManager;

import com.redhat.rhn.manager.org.CreateOrgCommand;
import com.redhat.rhn.manager.org.MigrationManager;
import com.redhat.rhn.manager.org.OrgManager;
import com.redhat.rhn.manager.org.UpdateOrgSoftwareEntitlementsCommand;
import com.redhat.rhn.manager.org.UpdateOrgSystemEntitlementsCommand;

import org.apache.log4j.Logger;

/**
 * OrgHandler
 * 
 * @version $Rev$
 * 
 * @xmlrpc.namespace org
 * @xmlrpc.doc Contains methods to access common organization management 
 * functions available from the web interface.
 */
public class OrgHandler extends BaseHandler {

    private static final String VALIDATION_XSD =
        "/com/redhat/rhn/frontend/action/multiorg/validation/orgCreateForm.xsd";
    private static final String ORG_ID_KEY = "org_id";
    private static final String ORG_NAME_KEY = "org_name";
    private static final String ALLOCATED_KEY = "allocated";
    private static final String UN_ALLOCATED_KEY = "unallocated";
    private static final String USED_KEY = "used";
    private static final String FREE_KEY = "free";
    private static Logger log = Logger.getLogger(OrgHandler.class);

    /**
     * Create a new organization.
     * @param sessionKey User's session key.
     * @param orgName Organization name. Must meet same criteria as in the web UI.
     * @param adminLogin New administrator login name for the new org.
     * @param adminPassword New administrator password.
     * @param prefix New administrator's prefix.
     * @param firstName New administrator's first name.
     * @param lastName New administrator's last name.
     * @param email New administrator's e-mail.
     * @param usePamAuth Should PAM authentication be used for new administrators account.
     * @return Newly created organization object.
     *
     * @xmlrpc.doc Create a new organization and associated administrator account.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param_desc("string", "orgName", "Organization name. Must meet same 
     * criteria as in the web UI.")
     * @xmlrpc.param #param_desc("string", "adminLogin", "New administrator login name.")
     * @xmlrpc.param #param_desc("string", "adminPassword", "New administrator password.")
     * @xmlrpc.param #param_desc("string", "prefix", "New administrator's prefix. Must 
     * match one of the values available in the web UI. (i.e. Dr., Mr., Mrs., Sr., etc.)")
     * @xmlrpc.param #param_desc("string", "firstName", "New administrator's first name.")
     * @xmlrpc.param #param_desc("string", "lastName", "New administrator's first name.")
     * @xmlrpc.param #param_desc("string", "email", "New administrator's e-mail.")
     * @xmlrpc.param #param_desc("boolean", "usePamAuth", "True if PAM authentication 
     * should be used for the new administrator account.")
     * @xmlrpc.returntype $OrgDtoSerializer
     */
    public OrgDto create(String sessionKey, String orgName, String adminLogin,
            String adminPassword, String prefix, String firstName, String lastName, 
            String email, Boolean usePamAuth) {
        log.debug("OrgHandler.create");
        getSatAdmin(sessionKey);

        validateCreateOrgData(orgName, adminPassword, firstName, lastName, email);

        CreateOrgCommand cmd = new CreateOrgCommand(orgName, adminLogin, adminPassword, 
            email);
        cmd.setFirstName(firstName);
        cmd.setLastName(lastName);
        cmd.setPrefix(prefix);
        
        ValidatorError[] verrors = cmd.store();
        if (verrors != null) {
            throw new ValidationException(verrors[0].getMessage());
        }

        return OrgManager.toDetailsDto(cmd.getNewOrg());
    }

    private void validateCreateOrgData(String orgName, String password, String firstName, 
            String lastName, String email) {
        
        Map<String, String> values = new HashMap<String, String>();
        values.put("orgName", orgName);
        values.put("desiredPassword", password);
        values.put("desiredPasswordConfirm", password);
        values.put("firstNames", firstName);
        values.put("lastName", lastName);
        
        ValidatorResult result = RhnValidationHelper.validate(this.getClass(), 
                values, new LinkedList<String>(values.keySet()), VALIDATION_XSD);

        if (!result.isEmpty()) {
            log.error("Validation errors:");
            for (ValidatorError error : result.getErrors()) {
                log.error("   " + error.getMessage());
            }
            // Multiple errors could return here, but we'll have to just throw an
            // exception for the first one and return that to the user.
            ValidatorError e = result.getErrors().get(0);
            throw new ValidationException(e.getMessage());
        }
    }

    /**
     * Returns the list of organizations.
     * @param sessionKey User's session key.
     * @return list of orgs.
     * @xmlrpc.doc Returns the list of organizations.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.returntype 
     * $OrgDtoSerializer
     */
    public List<OrgDto> listOrgs(String sessionKey) {
        User user  = getSatAdmin(sessionKey);
        return OrgManager.activeOrgs(user);
    }
    
    /**
     * Returns a list of organizations along with a trusted indicator.
     * @param sessionKey Caller's session key.
     * @param orgId the id of an organization.
     * @return Returns a list of organizations along with a trusted indicator.
     * @xmlrpc.doc Returns the list of trusted organizations.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "orgId")
     * @xmlrpc.returntype
     * $OrgTrustOverviewSerializer
     */
    @SuppressWarnings("unchecked")
    public List listTrusts(String sessionKey, Integer orgId) {
        User user = getSatAdmin(sessionKey);
        verifyOrgExists(orgId);
        return OrgManager.orgTrusts(user, Long.valueOf(orgId));
    }
    
    /**
     * Add a organization to the list of <i>trusted</i> organizations.
     * @param sessionKey Caller's session key.
     * @param orgId The id of the organization to be updated.
     * @param trustOrgId The id of the organization to be added.
     * @return 1 on success, else 0.
     * @xmlrpc.doc Add a organization to the list of trusted organizations. 
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "orgId")
     * @xmlrpc.param #param("string", "trustOrgId")
     * @xmlrpc.returntype #return_int_success()
     */
    public int addTrust(String sessionKey, Integer orgId, Integer trustOrgId) {
        getSatAdmin(sessionKey);
        Org org = OrgFactory.lookupById(Long.valueOf(orgId));
        if (org == null) {
            throw new NoSuchOrgException(orgId.toString());
        }
        Org trusted = OrgFactory.lookupById(Long.valueOf(trustOrgId));
        if (trusted == null) {
            throw new NoSuchOrgException(trustOrgId.toString());
        }
        org.getTrustedOrgs().add(trusted);
        OrgFactory.save(org);
        return 1;
    }
    
    /**
     * Remove a organization to the list of <i>trusted</i> organizations.
     * @param sessionKey Caller's session key.
     * @param orgId the id of the organization to be updated.
     * @param trustOrgId The id of the organization to be removed.
     * @return 1 on success, else 0.
     * @xmlrpc.doc Remove a organization to the list of trusted organizations. 
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "orgId")
     * @xmlrpc.param #param("string", "trustOrgId")
     * @xmlrpc.returntype #return_int_success()
     */
    public int removeTrust(String sessionKey, Integer orgId, Integer trustOrgId) {
        getSatAdmin(sessionKey);
        Org org = OrgFactory.lookupById(Long.valueOf(orgId));
        if (org == null) {
            throw new NoSuchOrgException(orgId.toString());
        }
        Org trusted = OrgFactory.lookupById(Long.valueOf(trustOrgId));
        if (trusted == null) {
            throw new NoSuchOrgException(trustOrgId.toString());
        }
        org.getTrustedOrgs().remove(trusted);
        OrgFactory.save(org);
        return 1;
    }
    
    /**
     * Delete an organization.
     * 
     * @param sessionKey User's session key.
     * @param orgId ID of organization to delete.
     * @return 1 on success, exception thrown otherwise.
     *
     * @xmlrpc.doc Delete an organization. The default organization 
     * (i.e. orgId=1) cannot be deleted.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "orgId")
     * @xmlrpc.returntype #return_int_success()
     */
    public int delete(String sessionKey, Integer orgId) {
        getSatAdmin(sessionKey);
        Org org = verifyOrgExists(orgId);

        // Verify we're not trying to delete the default org (id 1):
        Org defaultOrg = OrgFactory.getSatelliteOrg();
        if (orgId.longValue() == defaultOrg.getId().longValue()) {
            throw new SatelliteOrgException();
        }

        OrgFactory.deleteOrg(org.getId());

        return 1;
    }

    private Org verifyOrgExists(Number orgId) {
        if (orgId == null) {
            throw new NoSuchOrgException("null Id");
        }
        Org org = OrgFactory.lookupById(orgId.longValue());
        if (org == null) {
            throw new NoSuchOrgException(orgId.toString());
        }
        return org;
    }

    private Org verifyOrgExists(String name) {
        Org org = OrgFactory.lookupByName(name);
        if (org == null) {
            throw new NoSuchOrgException(name);
        }
        return org;
    }
    
    private Entitlement verifyEntitlementExists(String sysLabel) {
        Entitlement ent = EntitlementManager.getByName(sysLabel);
        if (ent == null) {
            throw new NoSuchEntitlementException(sysLabel);
        }
        return ent;
    }
    
    /**
     * Returns the list of active users in a given organization 
     * @param sessionKey Caller's session key.
     * @param orgId the orgId of the organization to lookup on.
     * @return the list of users in a organization.
     * @xmlrpc.doc Returns the list of users in a given organization.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "orgId")
     * @xmlrpc.returntype 
     *   #array()
     *     $MultiOrgUserOverviewSerializer
     *   #array_end()
     */
    public List listUsers(String sessionKey, Integer orgId) {
        getSatAdmin(sessionKey);
        verifyOrgExists(orgId);
        return OrgManager.activeUsers(Long.valueOf(orgId));
    }

    /**
     * Returns the detailed information about an organization
     * given the org_id.  
     * @param sessionKey Caller's session key.
     * @param orgId the orgId of the organization to lookup on.
     * @return the list of users in a organization.
     *
     * @xmlrpc.doc The detailed information about an organization given 
     * the organization ID.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "orgId")
     * @xmlrpc.returntype $OrgDtoSerializer
     */
    public OrgDto getDetails(String sessionKey, Integer orgId) {
        getSatAdmin(sessionKey);
        return OrgManager.toDetailsDto(verifyOrgExists(orgId));
    }

    /**
     * Returns the detailed information about an organization
     * given the org_name.  
     * @param sessionKey Caller's session key.
     * @param name the name of the organization to lookup on.
     * @return the list of users in a organization.
     *
     * @xmlrpc.doc The detailed information about an organization given 
     * the organization name.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "name")
     * @xmlrpc.returntype $OrgDtoSerializer
     */
    public OrgDto getDetails(String sessionKey, String name) {
        getSatAdmin(sessionKey);
        return OrgManager.toDetailsDto(verifyOrgExists(name));
    }    
    
    /**
     * 
     * @param sessionKey Caller's session key.
     * @param orgId the orgId of the organization to set name on
     * @param name the new name for the org.
     * @return the updated org.
     *
     * @xmlrpc.doc Updates the name of an organization
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "orgId") 
     * @xmlrpc.param #param_desc("string", "name", "Organization name. Must meet same 
     * criteria as in the web UI.")
     * @xmlrpc.returntype $OrgDtoSerializer
     */
    public OrgDto updateName(String sessionKey, Integer orgId, String name) {
        getSatAdmin(sessionKey);
        Org org = verifyOrgExists(orgId);
        if (!org.getName().equals(name)) {
            try {
                OrgManager.checkOrgName(name);
                org.setName(name);    
            }
            catch (ValidatorException ve) {
                throw new ValidationException(ve.getMessage());
            }
        }
        return OrgManager.toDetailsDto(org);
    }
    
    /**
     * Convenience method to get the loggedInUser 
     * and ensure the logged in user is a SatelliteAdmin
     * @param sessionKey  User's session key.
     * @return the logged in user with him guaranteed to be satellite admin.
     */
    private User getSatAdmin(String sessionKey) {
        User user = BaseHandler.getLoggedInUser(sessionKey);
        ensureUserRole(user, RoleFactory.SAT_ADMIN);
        return user;
    }

    /**
     * Convenience method to get the loggedInUser 
     * and ensure the logged in user is an Org admin
     * @param sessionKey  User's session key.
     * @return the logged in user with him guaranteed to be org admin.
     */
    private User getOrgAdmin(String sessionKey) {
        User user = BaseHandler.getLoggedInUser(sessionKey);
        ensureUserRole(user, RoleFactory.ORG_ADMIN);
        return user;
    }
    
    /**
     * Lists software entitlement allocation/distribution information
     *  across all organizations.
     * User needs to be a satellite administrator to get this information 
     * @param sessionKey User's session key.
     * @return Array of MultiOrgEntitlementsDto.
     *
     * @xmlrpc.doc List software entitlement allocation information
     * across all organizations.
     * Caller must be a satellite administrator.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.returntype 
     *   #array()
     *      $MultiOrgEntitlementsDtoSerializer
     *   #array_end()
     */    
    public List<MultiOrgEntitlementsDto> listSoftwareEntitlements(String sessionKey) {
        getSatAdmin(sessionKey);
        return ChannelManager.entitlementsForAllMOrgs();
    }
    
    
    /**
     * List an organization's allocation for each software entitlement.
     * If the organization has no allocation for a particular entitlement, it will
     * not appear in the list. A value of -1 indicates unlimited entitlements.
     *
     * @param sessionKey User's session key.
     * @param orgId Organization ID
     * @return Array of maps.
     *
     * @xmlrpc.doc List an organization's allocation of each software entitlement.
     * If the organization has no allocation for a particular entitlement, it will
     * not appear in the list. A value of -1 indicates unlimited entitlements.
     * 
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "orgId")
     * @xmlrpc.returntype 
     *   #array()
     *      $OrgChannelFamilySerializer
     *   #array_end()
     */
    public List<OrgChannelFamily> listSoftwareEntitlementsForOrg(String sessionKey, 
            Integer orgId) {

        getSatAdmin(sessionKey);
        Org org = verifyOrgExists(orgId);
        
        return ChannelManager.listChannelFamilySubscriptionsFor(org);
    }
    
    /**
     * List each organization's allocation of a given software entitlement.
     * Organizations with no allocations will not be present in the list. A value of -1
     * indicates unlimited entitlements.
     * 
     * @param sessionKey User's session key.
     * @param channelFamilyLabel Software entitlement label.
     * @return Array of maps.
     *
     * @xmlrpc.doc List each organization's allocation of a given software entitlement.
     * Organizations with no allocation will not be present in the list. A value of -1
     * indicates unlimited entitlements.
     * 
     * @xmlrpc.param sessionKey
     * @xmlrpc.param label - Software entitlement label.
     * @xmlrpc.returntype 
     *   #array()
     *     $OrgSoftwareEntitlementDtoSerializer
     *   #array_end()
     */
    public List<OrgSoftwareEntitlementDto> listSoftwareEntitlements(String sessionKey, 
            String channelFamilyLabel) {

        User user = getSatAdmin(sessionKey);

        ChannelFamily cf = ChannelFamilyFactory.lookupByLabel(channelFamilyLabel, null);
        if (cf == null) {
            throw new InvalidEntitlementException();
        }
       return ChannelManager.listEntitlementsForAllOrgs(cf, user);
    }
    
    /**
     * Set an organizations entitlement allocation for a channel family. 
     *
     * If increasing the entitlement allocation, the default organization
     * must have a sufficient number of free entitlements.
     * 
     * @param sessionKey User's session key.
     * @param orgId Organization ID to set allocation for.
     * @param channelFamilyLabel Channel family to set allocation for.
     * @param allocation New entitlement allocation.
     * @return 1 on success.
     *
     * @xmlrpc.doc Set an organization's entitlement allocation for the given software
     * entitlement.
     *
     * If increasing the entitlement allocation, the default organization 
     * (i.e. orgId=1) must have a sufficient number of free entitlements.
     * 
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "orgId")
     * @xmlrpc.param #param_desc("string", "label", "Software entitlement label.")
     * @xmlrpc.param #param("int", "allocation")
     * @xmlrpc.returntype #return_int_success()
     */
    public int setSoftwareEntitlements(String sessionKey, Integer orgId, 
            String channelFamilyLabel, Integer allocation) {

        getSatAdmin(sessionKey);

        Org org = verifyOrgExists(orgId);
        lookupChannelFamily(channelFamilyLabel);

        UpdateOrgSoftwareEntitlementsCommand cmd = 
            new UpdateOrgSoftwareEntitlementsCommand(channelFamilyLabel, org, 
                    new Long(allocation));
        ValidatorError ve = cmd.store();
        if (ve != null) {
            throw new ValidationException(ve.getMessage());
        }

        return 1;
    }

    /**
     * Lookup a channel family, throwing an exception if it cannot be found.
     * 
     * @param channelFamilyLabel Channel family label to look up.
     */
    private ChannelFamily lookupChannelFamily(String channelFamilyLabel) {
        ChannelFamily cf = ChannelFamilyFactory.lookupByLabel(channelFamilyLabel, null);
        if (cf == null) {
            throw new InvalidEntitlementException();
        }
        return cf;
    }
    
    /**
     * Lists system entitlement allocation/distribution information
     *  across all organizations.
     * User needs to be a satellite administrator to get this information 
     * @param sessionKey User's session key.
     * @return Array of MultiOrgSystemEntitlementsDto.
     *
     * @xmlrpc.doc Lists system entitlement allocation information
     * across all organizations.
     * Caller must be a satellite administrator.
     *
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.returntype 
     *   #array()
     *     $MultiOrgEntitlementsDtoSerializer
     *   #array_end()
     */   
    public List<MultiOrgSystemEntitlementsDto> listSystemEntitlements(String sessionKey) {
        getSatAdmin(sessionKey);
        return OrgManager.allOrgsEntitlements();
    }
    
    /**
     * List an organization's allocation of a system entitlement.
     * If the organization has no allocation for a particular entitlement, it will
     * not appear in the list.
     *
     * @param sessionKey User's session key.
     * @param label system entitlement label
     * @return a list of Maps having the system entitlements info.  
     * 
     * @xmlrpc.doc List each organization's allocation of a system entitlement.
     * If the organization has no allocation for a particular entitlement, it will
     * not appear in the list.
     * 
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "label")
     * @xmlrpc.returntype 
     *   #array()
     *     #struct("entitlement usage")
     *       #prop("int", "org_id")
     *       #prop("string", "org_name")
     *       #prop("int", "allocated")
     *       #prop("int", "unallocated")
     *       #prop("int", "used")
     *       #prop("int", "free")
     *     #struct_end()
     *   #array_end()
     */
    public List<Map> listSystemEntitlements(String sessionKey,    
                    String label) {
        getSatAdmin(sessionKey);
        verifyEntitlementExists(label);
        DataList<Map> result = OrgManager.allOrgsSingleEntitlement(label);
        List<Map> details = new LinkedList<Map>();
        for (Map row : result) {
            Map <String, Object> map = new HashMap<String, Object>();
            Org org = OrgFactory.lookupById((Long)row.get("orgid"));
            map.put(ORG_ID_KEY, new Integer(org.getId().intValue()));
            map.put(ORG_NAME_KEY, org.getName());
            map.put(ALLOCATED_KEY, ((Long)row.get("total")).intValue());
            map.put(USED_KEY, row.get("usage"));
            long free  = (Long)row.get("total") - (Long)row.get("usage");
            map.put(FREE_KEY, free);
            long unallocated  = (Long)row.get("upper") - (Long)row.get("total");
            map.put(UN_ALLOCATED_KEY, unallocated);
            details.add(map);
        }
        return details;
    }
    
    
    /**
     * List an organization's allocations of each system entitlement.
     * If the organization has no allocation for a particular entitlement, it will
     * not appear in the list.
     *
     * @param sessionKey User's session key.
     * @param orgId Organization ID
     * @return Array of maps.
     *
     * @xmlrpc.doc List an organization's allocation of each system entitlement.
     * If the organization has no allocation for a particular entitlement, it will
     * not appear in the list.
     * 
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "orgId")
     * @xmlrpc.returntype
     *   #array()
     *     $OrgEntitlementDtoSerializer
     *   #array_end()
     */
    public List<OrgEntitlementDto> listSystemEntitlementsForOrg(String sessionKey, 
                            Integer orgId)  {
        getSatAdmin(sessionKey);
        Org org = verifyOrgExists(orgId);
        return OrgManager.listEntitlementsFor(org);
    }

    /**
     * Set an organizations entitlement allocation for a channel family. 
     *
     * If increasing the entitlement allocation, the default organization
     * (i.e. orgId=1) must have a sufficient number of free entitlements.
     * 
     * @param sessionKey User's session key.
     * @param orgId Organization ID to set allocation for.
     * @param systemEntitlementLabel System entitlement to set allocation for.
     * @param allocation New entitlement allocation.
     * @return 1 on success.
     *
     * @xmlrpc.doc Set an organization's entitlement allocation for the given 
     * software entitlement.
     *
     * If increasing the entitlement allocation, the default organization
     * (i.e. orgId=1) must have a sufficient number of free entitlements.
     * 
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "orgId")
     * @xmlrpc.param #param_desc("string", "label", "System entitlement label.")
     * @xmlrpc.param #param("int", "allocation")
     * @xmlrpc.returntype #return_int_success()
     */
    public int setSystemEntitlements(String sessionKey, Integer orgId, 
            String systemEntitlementLabel, Integer allocation) {

        getSatAdmin(sessionKey);

        Org org = verifyOrgExists(orgId);

        Entitlement ent = EntitlementManager.getByName(systemEntitlementLabel);
        if (ent == null || !EntitlementManager.getAddonEntitlements().contains(ent)) {
            throw new InvalidEntitlementException();
        }

        UpdateOrgSystemEntitlementsCommand cmd = 
            new UpdateOrgSystemEntitlementsCommand(ent, org, new Long(allocation));
        ValidatorError ve = cmd.store();
        if (ve != null) {
            throw new ValidationException(ve.getMessage());
        }

        return 1;
    }

    /**
     * Migrate systems to a new organization. Caller must be an organization admnistrator. 
     * 
     * @param sessionKey User's session key.
     * @param sids System IDs.
     * @param toOrgId New organization ID.
     * @return list of systems migrated.
     * @throws FaultException A FaultException is thrown if:
     *   - The user performing the request is not an organization administrator
     *   - The user performing the request is in the same org as the targetted destination
     *     org
     *   - One or more of the servers provides do not exist
     *   - The destination organization does not exist
     *   - The user is not defined in the destination organization's trust
     *   
     * @xmlrpc.doc Migrate systems from the user's organization to the organization 
     * specified.  The user performing the migration must be an organization 
     * adminstrator and the originating organization must be defined within the 
     * trust of the destination organization.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #array_single("int", "systemId")
     * @xmlrpc.param #param_desc("int", "toOrgId", "ID of the destination organization.")
     * @xmlrpc.returntype
     * #array_single("int", "serverIdMigrated")
     */
    public Object[] migrateSystems(String sessionKey, List<Integer> sids, Integer toOrgId) 
        throws FaultException {

        // if user is not at least an org admin, they are not permitted to perform
        // a migration.
        User admin = getOrgAdmin(sessionKey);
        Org toOrg = verifyOrgExists(toOrgId);

        if (toOrg.equals(admin.getOrg())) {
            // user is in same org as the destination org; therefore, deny the request.
            throw new MigrationToSameOrgException(admin);
        }
        if (!toOrg.getTrustedOrgs().contains(admin.getOrg())) {
            // the user's org isn't trusted by the destination org; therefore, we cannot
            // perform the requested migration
            throw new OrgNotInTrustException(toOrgId);
        }
        
        List<Server> servers = new LinkedList<Server>();
        for (Integer sid : sids) {
            Long serverId = new Long(sid.longValue());
            Server server = null;
            try {
                server = ServerFactory.lookupByIdAndOrg(serverId, admin.getOrg());
            
                // throw a no_such_system exception if the server was not found.
                if (server == null) {
                    throw new NoSuchSystemException(
                            "No such system - sid[" + sid + "] in org[" +
                            admin.getOrg().getId() + "]");
                }
            }
            catch (LookupException e) {
                throw new NoSuchSystemException(
                        "No such system - sid[" + sid + "] in org[" +
                        admin.getOrg().getId() + "]");
            }
            servers.add(server);
        }
            
        List<Long> serversMigrated = MigrationManager.migrateServers(admin, servers, toOrg);
        return serversMigrated.toArray();
    }
}
