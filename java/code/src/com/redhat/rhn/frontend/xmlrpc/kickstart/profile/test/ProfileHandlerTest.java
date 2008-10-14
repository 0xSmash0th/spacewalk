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
package com.redhat.rhn.frontend.xmlrpc.kickstart.profile.test;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartCommandName;
import com.redhat.rhn.domain.kickstart.KickstartCommand;
import com.redhat.rhn.domain.kickstart.KickstartIpRange;
import com.redhat.rhn.domain.kickstart.KickstartScript;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.domain.kickstart.test.KickstartDataTest;
import com.redhat.rhn.domain.kickstart.test.KickstartableTreeTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.test.ActivationKeyTest;
import com.redhat.rhn.frontend.dto.kickstart.KickstartOptionValue;
import com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler;
import com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.manager.kickstart.IpAddress;
import com.redhat.rhn.manager.kickstart.KickstartOptionsCommand;
import com.redhat.rhn.manager.token.ActivationKeyManager;
import com.redhat.rhn.testing.TestUtils;

/**
 * ProfileHandlerTest
 * @version $Rev$
 */
public class ProfileHandlerTest extends BaseHandlerTestCase {
    
    private ProfileHandler handler = new ProfileHandler();
    private KickstartHandler ksHandler = new KickstartHandler();
    
    public void testSetKickstartTree() throws Exception {
        Channel baseChan = ChannelFactoryTest.createTestChannel(admin); 
        KickstartableTree testTree = KickstartableTreeTest.
            createTestKickstartableTree(baseChan);    
              
        String profileLabel = "new-ks-profile";
        ksHandler.createProfile(adminKey, profileLabel, "none", 
                testTree.getLabel(), "localhost", "rootpw");
        
        KickstartData newKsProfile = KickstartFactory.lookupKickstartDataByLabelAndOrgId(
                profileLabel, admin.getOrg().getId());
        assertNotNull(newKsProfile);
        assertTrue(newKsProfile.getCommand("url").getArguments().contains("http"));
               
        KickstartableTree anotherTestTree = KickstartableTreeTest.
        createTestKickstartableTree(baseChan);
        int result = handler.setKickstartTree(adminKey, profileLabel, 
                anotherTestTree.getLabel());
        
        assertEquals(1, result);
    }
    
    public void testSetChildChannels() throws Exception {
        Channel baseChan = ChannelFactoryTest.createTestChannel(admin); 
        KickstartableTree testTree = KickstartableTreeTest.
            createTestKickstartableTree(baseChan);    
              
        String profileLabel = "new-ks-profile";
        ksHandler.createProfile(adminKey, profileLabel, "none", 
             testTree.getLabel(), "localhost", "rootpw");
        
        KickstartData newKsProfile = KickstartFactory.lookupKickstartDataByLabelAndOrgId(
             profileLabel, admin.getOrg().getId());
        assertNotNull(newKsProfile);
        assertTrue(newKsProfile.getCommand("url").getArguments().contains("http"));
        
        Channel c1 = ChannelFactoryTest.createTestChannel(admin);
        Channel c2 = ChannelFactoryTest.createTestChannel(admin);
        assertFalse(c1.getLabel().equals(c2.getLabel()));
        
        List<String> channelsToSubscribe = new ArrayList<String>();
        channelsToSubscribe.add(c1.getLabel());
        channelsToSubscribe.add(c2.getLabel());
        
        int result = handler.setChildChannels(adminKey, profileLabel, channelsToSubscribe);
        assertEquals(1, result);
    }

    public void testListScript() throws Exception {
        KickstartData ks  = KickstartDataTest.createTestKickstartData(admin.getOrg());
        int id = handler.addScript(adminKey, ks.getLabel(), "This is a script", "", 
                "post", true);
        ks = (KickstartData) HibernateFactory.reload(ks);
        boolean found = false;
        
        for (KickstartScript script : handler.listScripts(adminKey, ks.getLabel())) {
            if (script.getId().intValue() == id && script.getDataContents().equals(
                    "This is a script")) {
                found = true;
            }
        }
        assertTrue(found);
    }  
    
    public void testAddScript() throws Exception {
        KickstartData ks  = KickstartDataTest.createTestKickstartData(admin.getOrg());
        int id = handler.addScript(adminKey, ks.getLabel(), "This is a script", "", 
                "post", true);
        ks = (KickstartData) HibernateFactory.reload(ks);
        boolean found = false;
        for (KickstartScript script : ks.getScripts()) {
            if (script.getId().intValue() == id && 
                    script.getDataContents().equals("This is a script")) {
                found = true;
            }
        }
        assertTrue(found);
    }
    
    public void testRemoveScript() throws Exception {
        KickstartData ks  = KickstartDataTest.createTestKickstartData(admin.getOrg());
        
        KickstartScript script = new KickstartScript();
        script.setKsdata(ks);
        script.setChroot("Y");
        script.setData(new String("blah").getBytes());
        script.setInterpreter("/bin/bash");
        script.setScriptType("post");
        script.setPosition(new Long(0));
        script = (KickstartScript) TestUtils.saveAndReload(script);
        
        assertEquals(1, handler.removeScript(adminKey, ks.getLabel(), 
                script.getId().intValue()));
        ks = (KickstartData) TestUtils.saveAndReload(ks);
       
        boolean found = false;
        for (KickstartScript scriptTmp : ks.getScripts()) {
            if (script.getId().equals(scriptTmp.getId())) {
                found = true;
            }
        }
        assertFalse(found);
    }
    
    public void testDownloadKickstart() throws Exception {
        KickstartData ks1  = KickstartDataTest.createKickstartWithProfile(admin);
        ks1.addPackageName(PackageFactory.lookupOrCreatePackageByName(
                "blahPackage"));
        
        ActivationKey key = ActivationKeyTest.createTestActivationKey(admin);
        ks1.addDefaultRegToken(key.getToken());
        ks1 = (KickstartData) TestUtils.saveAndReload(ks1);
        
        String file = handler.downloadKickstart(adminKey, ks1.getLabel(), "hostName");
        assertTrue(file.contains("rhnreg_ks --activationkey=\"" + key.getKey() + "\""));
        assertTrue(file.contains("blahPackage"));
    }

    public void testSetAdvancedOptions() throws Exception {
        //setup
        KickstartData ks = KickstartDataTest.createKickstartWithProfile(admin);
                        
        Object[] s1 = handler.getAdvancedOptions(adminKey, ks.getLabel());
        List<Map> l1 = new ArrayList(); 
        
        for (int i = 0; i < s1.length; i++) {
            l1.add((Map) s1[i]);
        }
        
        Map m1 = new HashMap();
        Map m2 = new HashMap();
        Map m3 = new HashMap();
        Map m4 = new HashMap();
        Map m5 = new HashMap();
        Map m6 = new HashMap();
        
        //all required options
        m1.put("name", "lang");
        m1.put("arguments", "abcd");
        l1.add(m1);
        
        m2.put("name", "keyboard");
        m2.put("arguments", "abcd");
        l1.add(m2);
        
        m3.put("name", "bootloader");
        m3.put("arguments", "abcd");
        l1.add(m3);
        
        m4.put("name", "timezone");
        m4.put("arguments", "abcd");
        l1.add(m4);
        
        m5.put("name", "auth");
        m5.put("arguments", "abcd");
        l1.add(m5);
        
        //Check encrypted password handling
        m6.put("name", "rootpw");
        m6.put("arguments", "$1$ltNG2yv4$5QpgeI1bDZykCIvC.gnGJ/");
        l1.add(m6);
                
        //test
        int result = handler.setAdvancedOptions(adminKey, ks.getLabel(), l1);
        Object[] s2 = handler.getAdvancedOptions(adminKey, ks.getLabel());
 
        //verify
        for (int i = 0; i < s1.length; i++) {
            KickstartCommand k = (KickstartCommand) s2[i];
            if (k.getCommandName().getName().equals("url")) {
                assertTrue(k.getArguments().
                        equals("--url /rhn/kickstart/ks-rhel-i386-kkk"));
            }                   
        } 
        
        assertTrue(s1.length <= s2.length);
        assertEquals(1, result);            
    }
    
    public void testGetAdvancedOptions() throws Exception {
        //setup
        KickstartData ks = KickstartDataTest.createKickstartWithProfile(admin);
                        
        Object[] s1 = handler.getAdvancedOptions(adminKey, ks.getLabel());
        List<Map> l1 = new ArrayList(); 
        
        for (int i = 0; i < s1.length; i++) {
            l1.add((Map) s1[i]);
        }
        
        Map m1 = new HashMap();
        Map m2 = new HashMap();
        Map m3 = new HashMap();
        Map m4 = new HashMap();
        Map m5 = new HashMap();
        Map m6 = new HashMap();
        
        //all required options
        m1.put("name", "lang");
        m1.put("arguments", "abcd");
        l1.add(m1);
        
        m2.put("name", "keyboard");
        m2.put("arguments", "abcd");
        l1.add(m2);
        
        m3.put("name", "bootloader");
        m3.put("arguments", "abcd");
        l1.add(m3);
        
        m4.put("name", "timezone");
        m4.put("arguments", "abcd");
        l1.add(m4);
        
        m5.put("name", "auth");
        m5.put("arguments", "abcd");
        l1.add(m5);
        
        //Check encrypted password handling
        m6.put("name", "rootpw");
        m6.put("arguments", "asdf1234");
        l1.add(m6);
                
        //test
        int result = handler.setAdvancedOptions(adminKey, ks.getLabel(), l1);
        Object[] s2 = handler.getAdvancedOptions(adminKey, ks.getLabel());
        
        //verify
        for (int i = 0; i < s1.length; i++) {
            KickstartCommand k = (KickstartCommand) s2[i];
            if (k.getCommandName().getName().equals("url")) {
                assertTrue(k.getArguments().
                        equals("--url /rhn/kickstart/ks-rhel-i386-kkk"));
            }                   
        }
        
        assertTrue(s1.length <= s2.length);
        assertEquals(1, result);      
    }
    
    
    public void testListIpRanges() throws Exception {
        KickstartData ks1 = setupIpRanges();
        KickstartData ks2 = setupIpRanges();
        Set set = handler.listIpRanges(adminKey, ks1.getLabel());
        
        assertTrue(set.contains(ks1.getIps().iterator().next()));
        assertFalse(set.contains(ks2.getIps().iterator().next()));
    }
    
    public void testAddIpRange() throws Exception {
        KickstartData ks1 = setupIpRanges();
        handler.addIpRange(adminKey, ks1.getLabel(), "192.168.1.1", "192.168.1.10");
        ks1 = KickstartFactory.lookupKickstartDataByLabelAndOrgId(ks1.getLabel(), 
                admin.getOrg().getId());
        assertTrue(ks1.getIps().size() == 2);        
    }
    
    public void testAddIpRange1() throws Exception {
        KickstartData ks1 = setupIpRanges();
        boolean caught = false;
        try {
            handler.addIpRange(adminKey, ks1.getLabel(), "192.168.0.3", "192.168.1.10");
        }
        catch (Exception e) {
            caught = true;
        }
        assertTrue(caught);
        ks1 = KickstartFactory.lookupKickstartDataByLabelAndOrgId(ks1.getLabel(), 
                admin.getOrg().getId());
        assertTrue(ks1.getIps().size() == 1);        
    }

    public void testRemoveIpRange() throws Exception {
        KickstartData ks1 = setupIpRanges();
        handler.removeIpRange(adminKey, ks1.getLabel(), "192.168.0.1");
        ks1 = KickstartFactory.lookupKickstartDataByLabelAndOrgId(ks1.getLabel(), 
                admin.getOrg().getId());
        assertTrue(ks1.getIps().size() == 0);
    }

    public void testCompareActivationKeys() throws Exception {
        // Setup
        KickstartData ks1 = KickstartDataTest.createKickstartWithProfile(admin);
        KickstartData ks2 = KickstartDataTest.createKickstartWithProfile(admin);
        
        ActivationKeyManager manager = ActivationKeyManager.getInstance();
        ActivationKey activationKey1 = manager.createNewActivationKey(admin, "Test1");
        ActivationKey activationKey2 = manager.createNewActivationKey(admin, "Test2");
        ActivationKey activationKey3 = manager.createNewActivationKey(admin, "Test3");
        
        ks1.getDefaultRegTokens().add(activationKey1.getToken());
        ks1.getDefaultRegTokens().add(activationKey2.getToken());
        
        ks2.getDefaultRegTokens().add(activationKey1.getToken());
        ks2.getDefaultRegTokens().add(activationKey3.getToken());
        
        KickstartFactory.saveKickstartData(ks1);
        KickstartFactory.saveKickstartData(ks2);
        
        // Test
        Map<String, List<ActivationKey>> keysDiff =
            handler.compareActivationKeys(adminKey, ks1.getLabel(), ks2.getLabel());
        
        // Verify
        assertNotNull(keysDiff);

        List<ActivationKey> ks1KeyList = keysDiff.get(ks1.getLabel());
        assertNotNull(ks1KeyList);
        assertEquals(ks1KeyList.size(), 1);
        
        ActivationKey ks1DiffKey = ks1KeyList.iterator().next();
        assertEquals(ks1DiffKey.getToken(), activationKey2.getToken());
        
        List<ActivationKey> ks2KeyList = keysDiff.get(ks2.getLabel());
        assertNotNull(ks2KeyList);
        assertEquals(ks2KeyList.size(), 1);
        
        ActivationKey ks2DiffKey = ks2KeyList.iterator().next();
        assertEquals(ks2DiffKey.getToken(), activationKey3.getToken());
    }
    
    public void testCompareActivationKeysSameProfile() throws Exception {
        // Setup
        KickstartData ks1 = KickstartDataTest.createKickstartWithProfile(admin);
        
        ActivationKeyManager manager = ActivationKeyManager.getInstance();
        ActivationKey activationKey1 = manager.createNewActivationKey(admin, "Test1");
        
        ks1.getDefaultRegTokens().add(activationKey1.getToken());
        
        KickstartFactory.saveKickstartData(ks1);
        
        // Test
        Map<String, List<ActivationKey>> keysDiff =
            handler.compareActivationKeys(adminKey, ks1.getLabel(), ks1.getLabel());
        
        // Verify
        assertNotNull(keysDiff);

        List<ActivationKey> ks1KeyList = keysDiff.get(ks1.getLabel());
        assertNotNull(ks1KeyList);
        assertEquals(ks1KeyList.size(), 0);
    }
    
    public void testCompareActivationKeysNoKeys() throws Exception {
        // Setup
        KickstartData ks1 = KickstartDataTest.createKickstartWithProfile(admin);
        KickstartData ks2 = KickstartDataTest.createKickstartWithProfile(admin);
        
        KickstartFactory.saveKickstartData(ks1);
        KickstartFactory.saveKickstartData(ks2);
        
        // Test
        Map<String, List<ActivationKey>> keysDiff =
            handler.compareActivationKeys(adminKey, ks1.getLabel(), ks2.getLabel());
        
        // Verify
        assertNotNull(keysDiff);

        List<ActivationKey> ks1KeyList = keysDiff.get(ks1.getLabel());
        assertNotNull(ks1KeyList);
        assertEquals(ks1KeyList.size(), 0);
    }
    
    public void testComparePackages() throws Exception {
        // Setup
        
        //   Clear any packages on the profile so we have a known starting state
        KickstartData ks1 = KickstartDataTest.createKickstartWithProfile(admin);
        ks1.getPackageNames().clear();
        
        KickstartData ks2 = KickstartDataTest.createKickstartWithProfile(admin);
        ks2.getPackageNames().clear();
        
        Package package1 = PackageTest.createTestPackage(admin.getOrg());
        Package package2 = PackageTest.createTestPackage(admin.getOrg());
        Package package3 = PackageTest.createTestPackage(admin.getOrg());
        
        ks1.addPackageName(package1.getPackageName());
        ks1.addPackageName(package2.getPackageName());

        ks2.addPackageName(package1.getPackageName());
        ks2.addPackageName(package3.getPackageName());
        
        KickstartFactory.saveKickstartData(ks1);
        KickstartFactory.saveKickstartData(ks2);
        
        // Test
        Map<String, Set<String>> packagesDiff =
            handler.comparePackages(adminKey, ks1.getLabel(), ks2.getLabel());
        
        // Verify
        assertNotNull(packagesDiff);

        Set<String> ks1PackageNameList = packagesDiff.get(ks1.getLabel());
        assertNotNull(ks1PackageNameList);
        assertEquals(1, ks1PackageNameList.size());
        
        String ks1PackageName = ks1PackageNameList.iterator().next();
        assertEquals(package2.getPackageName().getName(), ks1PackageName);
        
        Set<String> ks2PackageNameList = packagesDiff.get(ks2.getLabel());
        assertNotNull(ks2PackageNameList);
        assertEquals(1, ks2PackageNameList.size());
        
        String ks2PackageName = ks2PackageNameList.iterator().next();
        assertEquals(package3.getPackageName().getName(), ks2PackageName);
    }
    
    public void testComparePackagesSameProfile() throws Exception {
        // Setup
        
        //   Clear any packages on the profile so we have a known starting state
        KickstartData ks1 = KickstartDataTest.createKickstartWithProfile(admin);
        ks1.getPackageNames().clear();
                
        Package package1 = PackageTest.createTestPackage(admin.getOrg());
        
        ks1.addPackageName(package1.getPackageName());

        KickstartFactory.saveKickstartData(ks1);
        
        // Test
        Map<String, Set<String>> packagesDiff =
            handler.comparePackages(adminKey, ks1.getLabel(), ks1.getLabel());
        
        // Verify
        assertNotNull(packagesDiff);

        Set<String> ks1PackageNameList = packagesDiff.get(ks1.getLabel());
        assertNotNull(ks1PackageNameList);
        assertEquals(0, ks1PackageNameList.size());
    }
    
    public void testComparePackagesNoPackages() throws Exception {
        // Setup
        
        //   Clear any packages on the profile so we have a known starting state
        KickstartData ks1 = KickstartDataTest.createKickstartWithProfile(admin);
        ks1.getPackageNames().clear();
        
        KickstartData ks2 = KickstartDataTest.createKickstartWithProfile(admin);
        ks2.getPackageNames().clear();
        
        KickstartFactory.saveKickstartData(ks1);
        KickstartFactory.saveKickstartData(ks2);
        
        // Test
        Map<String, Set<String>> packagesDiff =
            handler.comparePackages(adminKey, ks1.getLabel(), ks2.getLabel());
        
        // Verify
        assertNotNull(packagesDiff);

        Set<String> ks1PackageNameList = packagesDiff.get(ks1.getLabel());
        assertNotNull(ks1PackageNameList);
        assertEquals(0, ks1PackageNameList.size());
        
        Set<String> ks2PackageNameList = packagesDiff.get(ks2.getLabel());
        assertNotNull(ks2PackageNameList);
        assertEquals(0, ks2PackageNameList.size());
    }
    
    public void testCompareAdvancedOptions() throws Exception {
        // Setup
        KickstartData ks1 =
            KickstartDataTest.createKickstartWithOptions(admin.getOrg());
        KickstartData ks2 =
            KickstartDataTest.createKickstartWithOptions(admin.getOrg());

        //   Add new value to only one of the profiles so there is something to diff
        KickstartOptionsCommand command1 = new KickstartOptionsCommand(ks1.getId(), admin);
        KickstartCommandName commandName = 
            (KickstartCommandName)command1.getAvailableOptions().iterator().next();
        
        KickstartCommand kc = new KickstartCommand();
        kc.setCommandName(commandName);
        kc.setKickstartData(ks1);
        kc.setCreated(new Date());
        kc.setModified(new Date());                        
        kc.setArguments("test value");                        
        
        command1.getKickstartData().getOptions().add(kc);
        command1.store();
        
        KickstartFactory.saveKickstartData(ks1);
        KickstartFactory.saveKickstartData(ks2);
        
        // Test
        Map<String, List<KickstartOptionValue>> optionsDiff =
            handler.compareAdvancedOptions(adminKey, ks1.getLabel(), ks2.getLabel());
        
        // Verify
        assertNotNull(optionsDiff);

        List<KickstartOptionValue> ks1Values = optionsDiff.get(ks1.getLabel());
        List<KickstartOptionValue> ks2Values = optionsDiff.get(ks2.getLabel());

        assertNotNull(ks1Values);
        assertNotNull(ks2Values);
        
        assertEquals(1, ks1Values.size());
        assertEquals(1, ks2Values.size());

        KickstartOptionValue value1 = ks1Values.get(0);
        assertEquals("test value", value1.getArg());

        KickstartOptionValue value2 = ks2Values.get(0);
        assertEquals("", value2.getArg());
        
        assertEquals(value1.getName(), value2.getName());
    }
    
    private KickstartData setupIpRanges() throws Exception {
        KickstartData ks1  = KickstartDataTest.createKickstartWithProfile(admin);
        KickstartIpRange range = new KickstartIpRange();
        range.setMax(new IpAddress("192.168.0.10").getNumber());
        range.setMin(new IpAddress("192.168.0.1").getNumber());
        range.setKsdata(ks1);
        range.setOrg(admin.getOrg());
        ks1.getIps().add(range);   
        KickstartFactory.saveKickstartData(ks1);
        return ks1;
    }
}
