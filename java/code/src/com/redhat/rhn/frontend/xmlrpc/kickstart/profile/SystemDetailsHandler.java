/**
 * Copyright (c) 2009 Red Hat, Inc.
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

package com.redhat.rhn.frontend.xmlrpc.kickstart.profile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.domain.common.CommonFactory;
import com.redhat.rhn.domain.common.FileList;
import com.redhat.rhn.domain.kickstart.SELinuxMode;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.FileListNotFoundException;
import com.redhat.rhn.frontend.xmlrpc.InvalidLocaleCodeException;
import com.redhat.rhn.frontend.xmlrpc.kickstart.XmlRpcKickstartHelper;
import com.redhat.rhn.manager.kickstart.KickstartEditCommand;
import com.redhat.rhn.manager.kickstart.KickstartLocaleCommand;
import com.redhat.rhn.manager.kickstart.SystemDetailsCommand;
import com.redhat.rhn.manager.kickstart.KickstartCryptoKeyCommand;

/**
* SystemDetailsHandler
* @version $Rev$
* @xmlrpc.namespace kickstart.profile.system
* @xmlrpc.doc Provides methods to set various properties of a kickstart profile.
*/
public class SystemDetailsHandler extends BaseHandler {

    /**
     * Enables the configuration management flag in a kickstart profile 
     * so that a system created using this profile will be configuration capable.
     * @param sessionKey the session key
     * @param ksLabel the ks profile label 
     * @return 1 on success
     * 
     * 
     * @xmlrpc.doc Enables the configuration management flag in a kickstart profile 
     * so that a system created using this profile will be configuration capable.
     * @xmlrpc.param #session_key() 
     * @xmlrpc.param #param_desc("string", "ksLabel","the kickstart profile label")
     * @xmlrpc.returntype #return_int_success()
     */
    public int enableConfigManagement(String sessionKey, String ksLabel) {
        return setConfigFlag(sessionKey, ksLabel, true);
    }

    /**
     * Disables the configuration management flag in a kickstart profile 
     * so that a system created using this profile will be NOT be configuration capable.
     * @param sessionKey the session key
     * @param ksLabel the ks profile label
     * @return 1 on success
     * 
     * @xmlrpc.doc Disables the configuration management flag in a kickstart profile 
     * so that a system created using this profile will be NOT be configuration capable.   
     * @xmlrpc.param #session_key() 
     * @xmlrpc.param #param_desc("string", "ksLabel","the kickstart profile label")
     * @xmlrpc.returntype #return_int_success()

     */
    public int disableConfigManagement(String sessionKey, String ksLabel) {
        return setConfigFlag(sessionKey, ksLabel, false);
    }
    
    private int setConfigFlag(String sessionKey, String ksLabel, boolean flag) {
        User user = getLoggedInUser(sessionKey);
        ensureConfigAdmin(user);
        SystemDetailsCommand command  = getSystemDetailsCommand(ksLabel, user);
        command.enableConfigManagement(flag);
        command.store();
        return 1;
    }
    
    /**
     * Enables the remote command flag in a kickstart profile 
     * so that a system created using this profile 
     * will be capable of running remote commands
     * @param sessionKey the session key
     * @param ksLabel the ks profile label
     * @return 1 on success
     * 
     * @xmlrpc.doc Enables the remote command flag in a kickstart profile 
     * so that a system created using this profile
     *  will be capable of running remote commands
     * @xmlrpc.param #session_key() 
     * @xmlrpc.param #param_desc("string", "ksLabel","the kickstart profile label")
     * @xmlrpc.returntype #return_int_success()
     */
    public int enableRemoteCommands(String sessionKey, String ksLabel) {
        return setRemoteCommandsFlag(sessionKey, ksLabel, true);
    }

    /**
     * Disables the remote command flag in a kickstart profile 
     * so that a system created using this profile
     * will be capable of running remote commands
     * @param sessionKey the session key
     * @param ksLabel the ks profile label
     * @return 1 on success
     * 
     * @xmlrpc.doc Disables the remote command flag in a kickstart profile 
     * so that a system created using this profile
     * will be capable of running remote commands
     * @xmlrpc.param #session_key() 
     * @xmlrpc.param #param_desc("string", "ksLabel","the kickstart profile label")
     * @xmlrpc.returntype #return_int_success()

     */
    public int disableRemoteCommands(String sessionKey, String ksLabel) {
        return setRemoteCommandsFlag(sessionKey, ksLabel, false);
    }
    
    private int setRemoteCommandsFlag(String sessionKey, String ksLabel, boolean flag) {
        User user = getLoggedInUser(sessionKey);
        ensureConfigAdmin(user);
        SystemDetailsCommand command  = getSystemDetailsCommand(ksLabel, user);
        command.enableRemoteCommands(flag);
        command.store();
        return 1;
    }
    
    /**
     * Sets the SELinux enforcing mode property of a kickstart profile 
     * so that a system created using this profile will be have 
     * the appropriate SELinux enforcing mode.
     * @param sessionKey the session key
     * @param ksLabel the ks profile label
     * @param enforcingMode the SELinux enforcing mode. 
     * @return 1 on success
     * 
     * @xmlrpc.doc Sets the SELinux enforcing mode property of a kickstart profile 
     * so that a system created using this profile will be have 
     * the appropriate SELinux enforcing mode.
     * @xmlrpc.param #session_key() 
     * @xmlrpc.param #param_desc("string", "ksLabel","the kickstart profile label")
     * @xmlrpc.param #param_desc("string", "enforcingMode","the selinux enforcing mode")
     *      #options()
     *          #item ("enforcing")
     *          #item ("permissive")
     *          #item ("disabled")
     *      #options_end()
     * @xmlrpc.returntype #return_int_success()
     */
    public int setSELinux(String sessionKey, String ksLabel, String enforcingMode) {
        User user = getLoggedInUser(sessionKey);
        ensureConfigAdmin(user);
        SystemDetailsCommand command  = getSystemDetailsCommand(ksLabel, user);
        command.setMode(SELinuxMode.lookup(enforcingMode));
        return setRemoteCommandsFlag(sessionKey, ksLabel, true);
    }

    /**
     * Sets the network device property of a kickstart profile 
     * so that a system created using this profile will be have 
     * the appropriate network device associated to it.
     * @param sessionKey the session key
     * @param ksLabel the ks profile label
     * @param isDhcp true if the network device uses DHCP
     * @param interfaceName network interface name
     * @return 1 on success
     * 
     * @xmlrpc.doc Sets the network device property of a kickstart profile 
     * so that a system created using this profile will be have 
     * the appropriate network device associated to it.
     * @xmlrpc.param #session_key() 
     * @xmlrpc.param #param_desc("string", "ksLabel","the kickstart profile label")
     * @xmlrpc.param #param("int", "isDhcp")
     *      #options()
     *          #item_desc ("1", 
     *          "to set the network type of the connection type to dhcp")
     *          #item_desc ("0", 
     *          "to set the network type of the connection type to static device")
     *      #options_end()
     * @xmlrpc.param #param("string", "interfaceName",
     *                           "name of the network interface- eg:- eth0")
     * @xmlrpc.returntype #return_int_success()
     */
    public int setNetworkConnection(String sessionKey, String ksLabel, 
                                            boolean isDhcp, String interfaceName) {
        User user = getLoggedInUser(sessionKey);
        ensureConfigAdmin(user);
        SystemDetailsCommand command  = getSystemDetailsCommand(ksLabel, user);
        
        command.setNetworkDevice(interfaceName, isDhcp);
        return setRemoteCommandsFlag(sessionKey, ksLabel, true);
    }
 
    /**
     * Retrieves the locale for a kickstart profile.
     * @param sessionKey The current user's session key
     * @param ksLabel The kickstart profile label
     * @return Returns a map containing the local and useUtc.
     * @throws FaultException A FaultException is thrown if:
     *   - The profile associated with ksLabel cannot be found
     *
     * @xmlrpc.doc Retrieves the locale for a kickstart profile.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param_desc("string", "ksLabel", "the kickstart profile label")
     * @xmlrpc.returntype 
     *          #struct("locale info")
     *              #prop("string", "locale")
     *              #prop("boolean", "useUtc")
     *                  #options()
     *                      #item_desc ("true", "the hardware clock uses UTC")
     *                      #item_desc ("false", "the hardware clock does not use UTC")
     *                  #options_end()
     *          #struct_end()
     */
    public Map getLocale(String sessionKey, String ksLabel) throws FaultException {

        User user = getLoggedInUser(sessionKey);
        ensureConfigAdmin(user);
        
        KickstartLocaleCommand command  = getLocaleCommand(ksLabel, user);
        
        Map locale = new HashMap();
        locale.put("locale", command.getTimezone());
        locale.put("useUtc", command.isUsingUtc());
        
        return locale;
    }

    /**
     * Sets the locale for a kickstart profile.
     * @param sessionKey The current user's session key
     * @param ksLabel The kickstart profile label
     * @param locale The locale
     * @param useUtc true if the hardware clock uses UTC
     * @return 1 on success, exception thrown otherwise
     * @throws FaultException A FaultException is thrown if:
     *   - The profile associated with ksLabel cannot be found
     *   - The locale provided is invalid
     *
     * @xmlrpc.doc Sets the locale for a kickstart profile.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param_desc("string", "ksLabel", "the kickstart profile label")
     * @xmlrpc.param #param_desc("string", "locale", "the locale")
     * @xmlrpc.param #param("boolean", "useUtc")
     *      #options()
     *          #item_desc ("true", 
     *          "the hardware clock uses UTC")
     *          #item_desc ("false", 
     *          "the hardware clock does not use UTC")
     *      #options_end()
     * @xmlrpc.returntype #return_int_success()
     */
    public int setLocale(String sessionKey, String ksLabel, String locale, 
            boolean useUtc) throws FaultException {

        User user = getLoggedInUser(sessionKey);
        ensureConfigAdmin(user);
        
        KickstartLocaleCommand command  = getLocaleCommand(ksLabel, user);
        
        if (command.isValidTimezone(locale) == Boolean.FALSE) {
            throw new InvalidLocaleCodeException(locale);
        }
        
        command.setTimezone(locale);
        if (useUtc) {
            command.useUtc();
        }
        else {
            command.doNotUseUtc();
        }
        command.store();
        return 1;
    }
    
    private KickstartLocaleCommand getLocaleCommand(String label, User user) {
        XmlRpcKickstartHelper helper = XmlRpcKickstartHelper.getInstance();
        return new KickstartLocaleCommand(helper.lookupKsData(label, user), user);
    }
    
    private SystemDetailsCommand getSystemDetailsCommand(String label, User user) {
        XmlRpcKickstartHelper helper = XmlRpcKickstartHelper.getInstance();
        return new SystemDetailsCommand(helper.lookupKsData(label, user), user);
    }

    /**
     * Returns the set of all keys associated with the indicated kickstart profile.
     * 
     * @param sessionKey     identifies the user's session; cannot be <code>null</code> 
     * @param kickstartLabel identifies the profile; cannot be <code>null</code> 
     * @return set of all keys associated with the given profile
     * 
     * @xmlrpc.doc Returns the set of all keys associated with the given kickstart
     *             profile.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "kickstartLabel")
     * @xmlrpc.returntype
     *      #array()
     *          #struct("key")
     *              #prop("string", "description")
     *              #prop("string", "type")
     *              #prop("string", "content")
     *          #struct_end()
     *      #array_end()
     */
    public Set listKeys(String sessionKey, String kickstartLabel) {
        
        // TODO: Determine if null or empty set is returned when no keys associated
        
        if (sessionKey == null) {
            throw new IllegalArgumentException("sessionKey cannot be null");
        }

        if (kickstartLabel == null) {
            throw new IllegalArgumentException("kickstartLabel cannot be null");
        }
        
        User user = getLoggedInUser(sessionKey);
        Org org = user.getOrg();
        
        KickstartData data =
            KickstartFactory.lookupKickstartDataByLabelAndOrgId(kickstartLabel, 
                org.getId());
        
        // Set will contain crypto key
        Set keys = data.getCryptoKeys();
        return keys;
    }
    
    /**
     * Adds the given list of keys to the specified kickstart profile.
     * 
     * @param sessionKey     identifies the user's session; cannot be <code>null</code> 
     * @param kickstartLabel identifies the profile; cannot be <code>null</code>
     * @param descriptions   list identifiying the keys to add 
     * @return 1 if the associations were performed correctly
     * 
     * @xmlrpc.doc Adds the given list of keys to the specified kickstart profile.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "kickstartLabel")
     * @xmlrpc.param #array_single("string", "keyDescription")
     * @xmlrpc.returntype #return_int_success()
     */
    public int addKeys(String sessionKey, String kickstartLabel,
                             List descriptions) {
        if (sessionKey == null) {
            throw new IllegalArgumentException("sessionKey cannot be null");
        }

        if (kickstartLabel == null) {
            throw new IllegalArgumentException("kickstartLabel cannot be null");
        }

        if (descriptions == null) {
            throw new IllegalArgumentException("descriptions cannot be null");
        }
        
        // Load the kickstart profile
        User user = getLoggedInUser(sessionKey);
        Org org = user.getOrg();
        
        KickstartData data =
            KickstartFactory.lookupKickstartDataByLabelAndOrgId(kickstartLabel, 
                org.getId());
        
        // Associate the keys
        KickstartCryptoKeyCommand command =
            new KickstartCryptoKeyCommand(data.getId(), user);

        command.addKeysByDescriptionAndOrg(descriptions, org);
        command.store();
        
        return 1;
    }
    
    /**
     * Removes the given list of keys from the specified kickstart profile.
     * 
     * @param sessionKey     identifies the user's session; cannot be <code>null</code> 
     * @param kickstartLabel identifies the profile; cannot be <code>null</code>
     * @param descriptions   list identifiying the keys to remove 
     * @return 1 if the associations were performed correctly
     * 
     * @xmlrpc.doc Removes the given list of keys from the specified kickstart profile.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "kickstartLabel")
     * @xmlrpc.param #array_single("string", "keyDescription")
     * @xmlrpc.returntype #return_int_success()
     */
    public int removeKeys(String sessionKey, String kickstartLabel,
                             List descriptions) {
        if (sessionKey == null) {
            throw new IllegalArgumentException("sessionKey cannot be null");
        }

        if (kickstartLabel == null) {
            throw new IllegalArgumentException("kickstartLabel cannot be null");
        }

        if (descriptions == null) {
            throw new IllegalArgumentException("descriptions cannot be null");
        }
        
        // Load the kickstart profile
        User user = getLoggedInUser(sessionKey);
        Org org = user.getOrg();
        
        KickstartData data =
            KickstartFactory.lookupKickstartDataByLabelAndOrgId(kickstartLabel, 
                org.getId());
        
        KickstartCryptoKeyCommand command =
            new KickstartCryptoKeyCommand(data.getId(), user);
        
        command.removeKeysByDescriptionAndOrg(descriptions, org);
        command.store();
        
        return 1;
    }
    
    /**
     * Returns the set of all file preservations associated with the given kickstart 
     * profile.
     * 
     * @param sessionKey     identifies the user's session; cannot be <code>null</code> 
     * @param kickstartLabel identifies the profile; cannot be <code>null</code> 
     * @throws FaultException A FaultException is thrown if:
     *   - The sessionKey is invalid
     *   - The kickstartLabel is invalid
     * @return set of all file preservations associated with the given profile
     * 
     * @xmlrpc.doc Returns the set of all file preservations associated with the given 
     * kickstart profile.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "kickstartLabel")
     * @xmlrpc.returntype
     *     #array()
     *         $FileListSerializer
     *     #array_end()
     */
    public Set listFilePreservations(String sessionKey, String kickstartLabel) 
        throws FaultException {
        
        if (sessionKey == null) {
            throw new IllegalArgumentException("sessionKey cannot be null");
        }

        if (kickstartLabel == null) {
            throw new IllegalArgumentException("kickstartLabel cannot be null");
        }
        
        User user = getLoggedInUser(sessionKey);
        Org org = user.getOrg();
        
        KickstartData data =
            KickstartFactory.lookupKickstartDataByLabelAndOrgId(kickstartLabel, 
                org.getId());
        
        return data.getPreserveFileLists();
    }
    
    /**
     * Adds the given list of file preservations to the specified kickstart profile.
     * 
     * @param sessionKey     identifies the user's session; cannot be <code>null</code> 
     * @param kickstartLabel identifies the profile; cannot be <code>null</code>
     * @param filePreservations   list identifying the file preservations to add 
     * @throws FaultException A FaultException is thrown if:
     *   - The sessionKey is invalid
     *   - The kickstartLabel is invalid
     *   - One of the filePreservations is invalid
     * @return 1 if the associations were performed correctly
     * 
     * @xmlrpc.doc Adds the given list of file preservations to the specified kickstart
     * profile.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "kickstartLabel")
     * @xmlrpc.param #array_single("string", "filePreservations")
     * @xmlrpc.returntype #return_int_success()
     */
    public int addFilePreservations(String sessionKey, String kickstartLabel,
                             List<String> filePreservations) throws FaultException {
        if (sessionKey == null) {
            throw new IllegalArgumentException("sessionKey cannot be null");
        }

        if (kickstartLabel == null) {
            throw new IllegalArgumentException("kickstartLabel cannot be null");
        }

        if (filePreservations == null) {
            throw new IllegalArgumentException("filePreservations cannot be null");
        }
        
        // Load the kickstart profile
        User user = getLoggedInUser(sessionKey);
        Org org = user.getOrg();
        
        KickstartData data =
            KickstartFactory.lookupKickstartDataByLabelAndOrgId(kickstartLabel, 
                org.getId());
        
        // Add the file preservations
        KickstartEditCommand command =
            new KickstartEditCommand(data.getId(), user);

        Set<FileList> fileLists = new HashSet<FileList>();
        for (String name : filePreservations) {
            FileList fileList = CommonFactory.lookupFileList(name, user.getOrg());
            if (fileList == null) {
                throw new FileListNotFoundException(name);
            }
            else {
                fileLists.add(fileList);
            }
        }
        // Cycle through the list of file list objects retrieved and add
        // them to the profile.  We do this on a second pass because, we
        // don't want to remove anything if there was an error that would have 
        // resulted in an exception being thrown.
        for (FileList fileList : fileLists) {
            command.getKickstartData().addPreserveFileList(fileList);
        }
        command.store();
        return 1;
    }
    
    /**
     * Removes the given list of file preservations from the specified kickstart profile.
     * 
     * @param sessionKey     identifies the user's session; cannot be <code>null</code> 
     * @param kickstartLabel identifies the profile; cannot be <code>null</code>
     * @param filePreservations   list identifying the file preservations to remove 
     * @throws FaultException A FaultException is thrown if:
     *   - The sessionKey is invalid
     *   - The kickstartLabel is invalid
     *   - One of the filePreservations is invalid
     * @return 1 if the associations were performed correctly
     * 
     * @xmlrpc.doc Removes the given list of file preservations from the specified 
     * kickstart profile.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "kickstartLabel")
     * @xmlrpc.param #array_single("string", "filePreservations")
     * @xmlrpc.returntype #return_int_success()
     */
    public int removeFilePreservations(String sessionKey, String kickstartLabel,
                             List<String> filePreservations) throws FaultException {
        if (sessionKey == null) {
            throw new IllegalArgumentException("sessionKey cannot be null");
        }

        if (kickstartLabel == null) {
            throw new IllegalArgumentException("kickstartLabel cannot be null");
        }

        if (filePreservations == null) {
            throw new IllegalArgumentException("filePreservations cannot be null");
        }
        
        // Load the kickstart profile
        User user = getLoggedInUser(sessionKey);
        Org org = user.getOrg();
        
        KickstartData data =
            KickstartFactory.lookupKickstartDataByLabelAndOrgId(kickstartLabel, 
                org.getId());
        
        // Associate the file preservations
        KickstartEditCommand command =
            new KickstartEditCommand(data.getId(), user);

        Set<FileList> fileLists = new HashSet<FileList>();
        for (String name : filePreservations) {
            FileList fileList = CommonFactory.lookupFileList(name, user.getOrg());
            if (fileList == null) {
                throw new FileListNotFoundException(name);
            }
            else {
                fileLists.add(fileList);
            }
        }
        // Cycle through the list of file list objects retrieved and remove
        // them from the profile.  We do this on a second pass because, we
        // don't want to remove anything if there was an error that would have 
        // resulted in an exception being thrown.
        for (FileList fileList : fileLists) {
            command.getKickstartData().removePreserveFileList(fileList);
        }
        command.store();
        return 1;
    }
}
