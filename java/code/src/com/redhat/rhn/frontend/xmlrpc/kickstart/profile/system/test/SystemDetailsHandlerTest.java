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

package com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.test;

import com.redhat.rhn.domain.common.CommonFactory;
import com.redhat.rhn.domain.common.FileList;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.SELinuxMode;
import com.redhat.rhn.domain.kickstart.crypto.CryptoKey;
import com.redhat.rhn.domain.kickstart.crypto.test.CryptoTest;
import com.redhat.rhn.domain.kickstart.test.KickstartDataTest;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.InvalidLocaleCodeException;
import com.redhat.rhn.frontend.xmlrpc.kickstart.InvalidKickstartLabelException;
import com.redhat.rhn.frontend.xmlrpc.kickstart.filepreservation.FilePreservationListHandler;
import com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.frontend.xmlrpc.test.XmlRpcTestUtils;
import com.redhat.rhn.manager.kickstart.KickstartCryptoKeyCommand;
import com.redhat.rhn.manager.kickstart.KickstartEditCommand;
import com.redhat.rhn.testing.UserTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author paji
 *
 */
public class SystemDetailsHandlerTest  extends BaseHandlerTestCase {
    
    private SystemDetailsHandler handler = new SystemDetailsHandler();
    private FilePreservationListHandler fpHandler = new FilePreservationListHandler();
    private User userNotOrgOne;
    private String userKey;

    public void setUp() throws Exception {
        super.setUp();
        userNotOrgOne = UserTestUtils.findNewUser();
        userKey = XmlRpcTestUtils.getSessionKey(userNotOrgOne);
        userNotOrgOne.addRole(RoleFactory.ORG_ADMIN);
    }


    public void testSELinux() throws Exception {
        KickstartData profile = createProfile();
 
        handler.setSELinux(adminKey, profile.getLabel(), SELinuxMode.PERMISSIVE.getValue());
        String mode = handler.getSELinux(adminKey, profile.getLabel());
        assertEquals(SELinuxMode.PERMISSIVE.toString(), mode);
        
        handler.setSELinux(adminKey, profile.getLabel(), SELinuxMode.ENFORCING.getValue());
        mode = handler.getSELinux(adminKey, profile.getLabel());
        assertEquals(SELinuxMode.ENFORCING.toString(), mode);
        
        handler.setSELinux(adminKey, profile.getLabel(), SELinuxMode.DISABLED.getValue());
        mode = handler.getSELinux(adminKey, profile.getLabel());
        assertEquals(SELinuxMode.DISABLED.toString(), mode);
        
        try {
            handler.setSELinux(adminKey, profile.getLabel(), "HOHOHOH!!!");
            fail("No exception thrown on invlaid input for SE linux mode..");
        }
        catch (Exception e) {
            //successful this Correctly fail since there is no se linux mode called HOHOHOH
        }
    }
    
    public void testConfigMgmt() throws Exception {
        KickstartData profile = createProfile();
        handler.enableConfigManagement(adminKey, profile.getLabel());
        KickstartData newKsProfile = KickstartFactory.lookupKickstartDataByLabelAndOrgId(
                profile.getLabel(), admin.getOrg().getId());
        assertEquals(true, newKsProfile.isConfigManageable());
        handler.disableConfigManagement(adminKey, profile.getLabel());
        newKsProfile = KickstartFactory.lookupKickstartDataByLabelAndOrgId(
                profile.getLabel(), admin.getOrg().getId());  
        assertEquals(false, newKsProfile.isConfigManageable());
    }
    
    public void testRemoteCommands() throws Exception {
        KickstartData profile = createProfile();
        handler.enableRemoteCommands(adminKey, profile.getLabel());
        KickstartData newKsProfile = KickstartFactory.lookupKickstartDataByLabelAndOrgId(
                profile.getLabel(), admin.getOrg().getId());
        assertEquals(true, newKsProfile.isRemoteCommandable());
        handler.disableRemoteCommands(adminKey, profile.getLabel());
        newKsProfile = KickstartFactory.lookupKickstartDataByLabelAndOrgId(
                profile.getLabel(), admin.getOrg().getId());  
        assertEquals(false, newKsProfile.isRemoteCommandable());
    }    
    
    public void testNetworkConnection() throws Exception {
        KickstartData profile = createProfile();
        String interfaceName = "eth0";
        handler.setNetworkConnection(adminKey, profile.getLabel(), true, interfaceName);
        
        Map<String, Object> connection = new HashMap<String, Object>();
        connection = handler.getNetworkConnection(adminKey, profile.getLabel());
        
        assertTrue(connection.containsKey("interface_name"));
        assertTrue(connection.containsKey("is_dhcp"));
        assertEquals(interfaceName, connection.get("interface_name"));
        // connection type is dhcp
        assertEquals(new Integer(1), connection.get("is_dhcp"));
               
        handler.setNetworkConnection(adminKey, profile.getLabel(), false, interfaceName);
        connection = handler.getNetworkConnection(adminKey, profile.getLabel());
        
        assertTrue(connection.containsKey("interface_name"));
        assertTrue(connection.containsKey("is_dhcp"));
        assertEquals(interfaceName, connection.get("interface_name"));
        // connection type is static
        assertEquals(new Integer(0), connection.get("is_dhcp"));
    }
    
    public void testGetLocale() throws Exception {
        
        KickstartData newProfile = createProfile();

        try {
            handler.getLocale(adminKey, "InvalidProfile");
            fail("SystemDetailsHandler.getLocale allowed execution with invalid " +
                    "profile label");
        }
        catch (InvalidKickstartLabelException e) {
            //success
        }
       
        handler.setLocale(adminKey, newProfile.getLabel(), "America/Guayaquil", 
                Boolean.TRUE);

        Map locale = handler.getLocale(adminKey, newProfile.getLabel());
        
        assertEquals(locale.size(), 2);
        assertEquals(locale.get("locale"), "America/Guayaquil");
        assertEquals(locale.get("useUtc"), Boolean.TRUE);
    }

    public void testSetLocale() throws Exception {
        
        KickstartData newProfile = createProfile();

        try {
            handler.setLocale(adminKey, "InvalidProfile", "Pacific/Galapagos", 
                    Boolean.TRUE);
            fail("SystemDetailsHandler.setLocale allowed execution with invalid " +
                    "profile label");
        }
        catch (InvalidKickstartLabelException e) {
            //success
        }
        
        try {
            handler.setLocale(adminKey, newProfile.getLabel(), "InvalidLocale", 
                    Boolean.TRUE);
            fail("SystemDetailsHandler.setLocale allowed setting invalid locale.");
        }
        catch (InvalidLocaleCodeException e) {
            //success
        }
        
        handler.setLocale(adminKey, newProfile.getLabel(), "America/Guayaquil", 
                Boolean.TRUE);
        
        KickstartData profile = KickstartFactory.lookupKickstartDataByLabelAndOrgId(
                newProfile.getLabel(), admin.getOrg().getId());
        
        assertEquals(profile.getTimezone(), "America/Guayaquil");
        
        assertTrue(profile.isUsingUtc());
    }

    private KickstartData createProfile(User user, String key) throws Exception {
        return KickstartDataTest.createKickstartWithChannel(user.getOrg());
    }
    
    private KickstartData createProfile() throws Exception {
        return createProfile(admin, adminKey);
    }
    
    public void testListKeys() throws Exception {
        // Setup
        
        //   Create key to add
        CryptoKey key = CryptoTest.createTestKey(userNotOrgOne.getOrg());
        KickstartFactory.saveCryptoKey(key);
        assertNotNull(KickstartFactory.lookupCryptoKeyById(key.getId(), key.getOrg()));
        flushAndEvict(key);
        
        //   Create profile to add the key to
        KickstartData profile = createProfile(userNotOrgOne, userKey);
        
        //   Add the key to the profile
        KickstartCryptoKeyCommand command =
            new KickstartCryptoKeyCommand(profile.getId(), userNotOrgOne);
        List keyList = new ArrayList();
        keyList.add(key.getDescription());
        command.addKeysByDescriptionAndOrg(keyList, userNotOrgOne.getOrg());
        command.store();
        
        // Test
        Set associatedKeys = handler.listKeys(userKey, profile.getLabel());
        System.out.println("Keys: " + associatedKeys);
        // Verify
        assertNotNull(associatedKeys);
        assertEquals(associatedKeys.size(), 1);
        
        CryptoKey foundKey = (CryptoKey)associatedKeys.iterator().next();
        assertEquals(key.getDescription(), foundKey.getDescription());
    }
    
    public void testListKeysNoKeys() throws Exception {
        // Setup
        KickstartData profile = createProfile(userNotOrgOne, userKey);
        
        // Test
        Set associatedKeys = handler.listKeys(userKey, profile.getLabel());

        // Verify
        assertNotNull(associatedKeys);
        assertEquals(associatedKeys.size(), 0);
    }
    
    public void testAddKeys() throws Exception {
        // Setup

        //   Create key to add
        CryptoKey key = CryptoTest.createTestKey(userNotOrgOne.getOrg());
        KickstartFactory.saveCryptoKey(key);
        assertNotNull(KickstartFactory.lookupCryptoKeyById(key.getId(), key.getOrg()));
        flushAndEvict(key);
        
        //   Create profile to add the key to
        KickstartData profile = createProfile(userNotOrgOne, userKey);

        // Test
        List descriptions = new ArrayList();
        descriptions.add(key.getDescription());
        int result = handler.addKeys(userKey, profile.getLabel(), descriptions);
        
        // Verify
        assertEquals(result, 1);
        
        KickstartData data =
            KickstartFactory.lookupKickstartDataByLabelAndOrgId(profile.getLabel(), 
                userNotOrgOne.getOrg().getId());

        Set foundKeys = data.getCryptoKeys();
        
        assertNotNull(foundKeys);
        assertEquals(foundKeys.size(), 1);
        
        CryptoKey foundKey = (CryptoKey)foundKeys.iterator().next();
        assertEquals(key.getDescription(), foundKey.getDescription());
    }
    
    public void testRemoveKeys() throws Exception {
        
        // Setup

        //   Create key to add
        CryptoKey key = CryptoTest.createTestKey(userNotOrgOne.getOrg());
        KickstartFactory.saveCryptoKey(key);
        assertNotNull(KickstartFactory.lookupCryptoKeyById(key.getId(), key.getOrg()));
        flushAndEvict(key);
        
        //   Create profile to add the key to
        KickstartData profile = createProfile(userNotOrgOne, userKey);

        List descriptions = new ArrayList();
        descriptions.add(key.getDescription());
        int result = handler.addKeys(userKey, profile.getLabel(), descriptions);
        
        KickstartData data =
            KickstartFactory.lookupKickstartDataByLabelAndOrgId(profile.getLabel(), 
                userNotOrgOne.getOrg().getId());
        assertNotNull(data);
        assertEquals(1, data.getCryptoKeys().size());
        
        // Test
        result = handler.removeKeys(userKey, profile.getLabel(), descriptions);
        
        // Verify
        assertEquals(1, result);
        
        data = KickstartFactory.lookupKickstartDataByLabelAndOrgId(profile.getLabel(), 
                userNotOrgOne.getOrg().getId());

        Set foundKeys = data.getCryptoKeys();
        assertNotNull(foundKeys);
        assertEquals(0, foundKeys.size());
    }

    public void testListFilePreservations() throws Exception {
        
        // Setup
        FileList fileList = createFileList();
        KickstartData profile = createProfile();
        
        // Associate the file preservation list with the profile
        KickstartEditCommand command =
            new KickstartEditCommand(profile.getId(), regular);
        command.getKickstartData().addPreserveFileList(fileList);
        command.store();
        
        // Test
        Set associatedFL = handler.listFilePreservations(regularKey,
                profile.getLabel());
        
        // Verify
        assertNotNull(associatedFL);
        assertEquals(1, associatedFL.size());
        
        FileList foundFL = (FileList) associatedFL.iterator().next();
        assertEquals(fileList.getLabel(), foundFL.getLabel());
        assertEquals(fileList.getFileNames(), foundFL.getFileNames());
    }
    
    public void testListFilePreservationsNone() throws Exception {
        // Setup
        KickstartData profile = createProfile();
        
        // Test
        Set associatedFL = handler.listFilePreservations(regularKey,
                profile.getLabel());
        
        // Verify
        assertNotNull(associatedFL);
        assertEquals(0, associatedFL.size());
    }
    
    public void testAddFilePreservations() throws Exception {
        
        // Setup
        FileList fileList = createFileList();
        KickstartData profile = createProfile();
        
        // Test
        List<String> fileLists = new ArrayList<String>();
        fileLists.add(fileList.getLabel());
        int result = handler.addFilePreservations(regularKey, profile.getLabel(), 
                fileLists);
        
        // Verify
        assertEquals(1, result);
        
        KickstartData data =
            KickstartFactory.lookupKickstartDataByLabelAndOrgId(profile.getLabel(), 
                regular.getOrg().getId());

        Set<FileList> foundLists = data.getPreserveFileLists();
        
        assertNotNull(foundLists);
        assertEquals(1, foundLists.size());
        
        FileList foundList = (FileList)foundLists.iterator().next();
        assertEquals(fileList.getLabel(), foundList.getLabel());
        assertEquals(fileList.getFileNames(), foundList.getFileNames());
    }

    public void testRemoveFilePreservations() throws Exception {
        
        // Setup
        FileList fileList = createFileList();
        KickstartData profile = createProfile();
        
        List<String> fileLists = new ArrayList<String>();
        fileLists.add(fileList.getLabel());
        int result = handler.addFilePreservations(regularKey, profile.getLabel(), 
                fileLists);
        assertEquals(1, result);
        
        KickstartData data =
            KickstartFactory.lookupKickstartDataByLabelAndOrgId(profile.getLabel(), 
                regular.getOrg().getId());
        assertNotNull(data);
        assertEquals(1, data.getPreserveFileLists().size());

        // Test
        result = handler.removeFilePreservations(regularKey, profile.getLabel(),
                fileLists);
        
        // Verify
        assertEquals(1, result);

        data = KickstartFactory.lookupKickstartDataByLabelAndOrgId(profile.getLabel(), 
                regular.getOrg().getId());
        Set<FileList> foundLists = data.getPreserveFileLists();
        assertNotNull(foundLists);
        assertEquals(0, foundLists.size());
    }

    private FileList createFileList() {
        List<String> files = new ArrayList<String>();
        files.add("file1");
        files.add("file2");
        int result = fpHandler.create(adminKey, "list1", files);
        assertEquals(1, result);
        return CommonFactory.lookupFileList("list1", admin.getOrg());
    }
}
