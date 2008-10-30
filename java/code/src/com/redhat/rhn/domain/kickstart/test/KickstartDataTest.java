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
package com.redhat.rhn.domain.kickstart.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.common.CommonFactory;
import com.redhat.rhn.domain.common.FileList;
import com.redhat.rhn.domain.common.test.FileListTest;
import com.redhat.rhn.domain.kickstart.KickstartCommand;
import com.redhat.rhn.domain.kickstart.KickstartCommandName;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartDefaultRegToken;
import com.redhat.rhn.domain.kickstart.KickstartDefaults;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartInstallType;
import com.redhat.rhn.domain.kickstart.KickstartPreserveFileList;
import com.redhat.rhn.domain.kickstart.KickstartScript;
import com.redhat.rhn.domain.kickstart.KickstartVirtualizationType;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.domain.kickstart.crypto.CryptoKey;
import com.redhat.rhn.domain.kickstart.crypto.test.CryptoTest;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.rhnpackage.profile.Profile;
import com.redhat.rhn.domain.rhnpackage.test.PackageNameTest;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.token.Token;
import com.redhat.rhn.domain.token.test.TokenTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.kickstart.KickstartFormatter;
import com.redhat.rhn.manager.kickstart.KickstartWizardHelper;
import com.redhat.rhn.manager.profile.test.ProfileManagerTest;
import com.redhat.rhn.manager.rhnpackage.test.PackageManagerTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.TestStatics;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.hibernate.Session;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * KickstartDataTest
 * @version $Rev$
 */
public class KickstartDataTest extends BaseTestCaseWithUser {

    private static final String STATIC_DEV = "dhcp:eth0";
    private static final String KERNEL_PARAMS = "ide0=ata66";

    public void testKickstartDataTest() throws Exception {
        KickstartData k = createTestKickstartData(user.getOrg());
        assertNotNull(k);
        assertNotNull(k.getId());
        assertNotNull(k.getPackageNames());
        
        KickstartData k2 = lookupById(user.getOrg(), k.getId());
        assertEquals(k2.getLabel(), k.getLabel());
        //assertEquals(k2.getPostContents(), POST);
        //assertEquals(k2.getPreContents(), PRE);

        KickstartPreserveFileList kf = createTestFileList();
        assertNotNull(kf);
        kf.setKsdata(k);

        KickstartDefaults d = createDefaults(k, user);
        assertNotNull(d);
        d.setKsdata(k);

        KickstartDefaultRegToken t = new KickstartDefaultRegToken();
        t.setKsdata(k);
        t.setToken(TokenTest.createTestToken());
        TestUtils.saveAndFlush(t);

        TestUtils.saveAndFlush(k);

    }
    
    public void testProfile() throws Exception {
        user.addRole(RoleFactory.ORG_ADMIN);
        KickstartData k = createKickstartWithProfile(user);
        assertNotNull(k.getKsdefault().getProfile());
        
    }
    
    public void testLookupByLabel() throws Exception {
        user.addRole(RoleFactory.ORG_ADMIN);
        KickstartData k = createKickstartWithProfile(user);
        assertNotNull(KickstartFactory.lookupKickstartDataByLabelAndOrgId(k.getLabel(), 
                user.getOrg().getId()));
    }
    
    public void testLookupDefault() throws Exception {
        if (KickstartFactory.lookupOrgDefault(user.getOrg()) != null) {
            KickstartData orgdef = KickstartFactory.lookupOrgDefault(user.getOrg());
            orgdef.setOrgDefault(Boolean.FALSE);
            KickstartFactory.saveKickstartData(orgdef);
        }
        KickstartData k = createTestKickstartData(user.getOrg());
        k.setOrgDefault(Boolean.TRUE);
        KickstartFactory.saveKickstartData(k);
        flushAndEvict(k);
        assertNotNull(KickstartFactory.lookupOrgDefault(user.getOrg()));
    }
    
    public static KickstartData createKickstartWithProfile(User user) throws Exception {
        KickstartData k = createTestKickstartData(user.getOrg()); 
        KickstartDefaults d1 = createDefaults(k, user);
        k.setKsdefault(d1);
        Profile p = ProfileManagerTest.createProfileWithServer(user);
        d1.setProfile(p);
        d1.getKstree().setChannel(p.getBaseChannel());
        KickstartFactory.saveKickstartData(k);
        k = (KickstartData) TestUtils.reload(k);
        return k;
    }
    
    public static void addCommand(User user, KickstartData owner, 
            String name, String args) {
        KickstartWizardHelper cmd = new KickstartWizardHelper(user);
        cmd.createCommand(name, args, owner);
    }
    
    public void testInstallType() throws Exception {
        
        List types = KickstartFactory.lookupKickstartInstallTypes();
        assertNotNull(types);
        assertTrue(types.size() > 0);
        assertTrue(types.get(0) instanceof KickstartInstallType);
        
        assertNotNull(KickstartFactory.lookupKickstartInstallTypeByLabel("rhel_4"));
        
        KickstartData k = createTestKickstartData(user.getOrg()); 
        KickstartDefaults d1 = createDefaults(k, user);
        k.setKsdefault(d1);
        
        TestUtils.saveAndFlush(k);
        
        KickstartableTree t2 = d1.getKstree();
        assertNotNull(t2);
        assertEquals(t2, k.getKsdefault().getKstree());
        
        KickstartDefaults d2 = k.getKsdefault();
        assertNotNull(d2);
        
        KickstartableTree t1 = d2.getKstree();
        assertNotNull(t1);
        
        KickstartInstallType i1 = t1.getInstallType();
        assertNotNull(i1);
        KickstartInstallType i2 = t2.getInstallType();
        assertNotNull(i2);
        
        assertNotNull(i1.getName());
        assertNotNull(i2.getClass());
        assertEquals(i1.getName(), i2.getName());  
        
        KickstartData k2 = lookupById(user.getOrg(), k.getId());
        assertNotNull(k2.getKsdefault());
    }
    
    public void testDeleteKickstartData() throws Exception {
        KickstartData ksd = createTestKickstartData(user.getOrg());
        assertNotNull(ksd);
        assertNotNull(ksd.getId());
        assertNotNull(ksd.getPackageNames());
        KickstartFactory.saveKickstartData(ksd);
        flushAndEvict(ksd); 
        
        assertEquals(1, KickstartFactory.removeKickstartData(ksd));
        assertNull(KickstartFactory
                .lookupKickstartDataByIdAndOrg(user.getOrg(), ksd.getId()));
        assertNull(lookupById(user.getOrg(), ksd.getId()));
        assertNull(lookupByLabel(ksd.getLabel()));
    }
    
    public void testChildChannels() throws Exception {
        KickstartData ksdata = createTestKickstartData(user.getOrg());
        ksdata.setKsdefault(createDefaults(ksdata, user));
        assertNotNull(ksdata);
        assertNotNull(ksdata.getTree());
        assertNotNull(ksdata.getTree().getChannel());
        Channel child = ChannelTestUtils.createChildChannel(user,
                ksdata.getTree().getChannel());
        assertNotNull(child);
        ksdata.addChildChannel(child);
        TestUtils.saveAndFlush(ksdata);
        ksdata = (KickstartData) reload(ksdata);
        // Check to make sure its reloaded from DB properly
        assertNotNull(ksdata.getChildChannels());
        // Make sure we have 1 child channel
        assertTrue(ksdata.getChildChannels().size() == 1);
        // Make sure we can remove child channel
        ksdata.removeChildChannel(child);
        assertTrue(ksdata.getChildChannels().size() == 0);
    }
    
    /**
     * Helper method to lookup KickstartData by id
     * @param id Id to lookup
     * @return Returns the KickstartData
     * @throws Exception
     */
    private KickstartData lookupById(Org orgIn, Long id) throws Exception {
        Session session = HibernateFactory.getSession();
        return (KickstartData) session.getNamedQuery("KickstartData.findByIdAndOrg")
                          .setLong("id", id.longValue())
                          .setLong("org_id", orgIn.getId().longValue())
                          .uniqueResult();
    }

    /**
     *  note that this is used for testing purposes. in theory we could return more 
     *  then one raid or partition..etc. this is merely a helper method to set up testing 
     *  for special command lists
     * @param lbl name to lookup
     * @return KickstartCommandName (single object)
     * @throws Exception
     */
    private static KickstartCommandName lookupByLabel(String lbl) throws Exception {
        Session session = HibernateFactory.getSession();
        return (KickstartCommandName)session
                  .getNamedQuery("KickstartCommandName.findByLabel")
                  .setString("name", lbl).
                  uniqueResult();
    }
    
    /**
     * Creates KickstartPreserveFileList for testing purposes.
     * @return Returns a committed KickstartPreserveFileList
     */
    public static KickstartPreserveFileList createTestFileList() throws Exception {
        FileList f = new FileList();

        f.setLabel("Test FileList" + TestUtils.randomString());
        f.setOrg(UserTestUtils.findNewOrg(TestStatics.TESTORG));
        f.setCreated(new Date());
        f.setModified(new Date());
        assertNull(f.getId());

        KickstartPreserveFileList flist = new KickstartPreserveFileList();
        flist.setFileList(f);
        return flist;

    }

    
    /**
     * Creates KickstartDefaults for testing purposes.
     * @return Returns a committed KickstartDefaults
     */
    public static KickstartDefaults createDefaults(KickstartData data, User owner)
            throws Exception {
        Channel channel = ChannelFactoryTest.createTestChannel(owner);
        ChannelTestUtils.addDistMapToChannel(channel);
        addKickstartPackagesToChannel(channel, false);
        return createDefaults(data, channel);
    }
    
    /**
     * Adds the minimal amount of packages to the channel for the channel to be a
     * valid kickstart channel.
     * @param c The channel to which to add kickstart packages.
     * @param rhel2 Whether to include rhel2 required packages.
     * @throws Exception for creating packages.
     */
    public static void addKickstartPackagesToChannel(Channel c, boolean rhel2) 
            throws Exception {
       addPackages(c, KickstartFormatter.UPDATE_PKG_NAMES);
       if (rhel2) {
           addPackages(c, KickstartFormatter.FRESH_PKG_NAMES_RHEL2);
       }
       else {
           addPackages(c, KickstartFormatter.FRESH_PKG_NAMES_RHEL34);
       }
       PackageManagerTest.addPackageToChannel(
               KickstartData.KICKSTART_PACKAGE_NAME + "testy", c);
       PackageManagerTest.addPackageToChannel(
               KickstartData.LEGACY_KICKSTART_PACKAGE_NAME +
                   KickstartableTreeTest.TEST_BOOT_PATH, c);
    }
    
    private static void addPackages(Channel c, String[] names)
            throws Exception {
        for (int i = 0; i < names.length; i++) {
            PackageManagerTest.addPackageToChannel(names[i], c);
        }
    }
    
    /**
     * Creates KickstartDefaults for testing purposes.
     * @return Returns a committed KickstartDefaults
     */
    public static KickstartDefaults createDefaults(KickstartData data, 
            Channel c) throws Exception {
        KickstartDefaults d = new KickstartDefaults();
        d.setKsdata(data);
        KickstartVirtualizationType type = KickstartFactory.
            lookupKickstartVirtualizationTypeByLabel(KickstartVirtualizationType.NONE);
        d.setVirtualizationType(type);
        KickstartableTree t = KickstartableTreeTest.createTestKickstartableTree(c);
        d.setKstree(t);
        d.setCfgManagementFlag(Boolean.FALSE);
        d.setRemoteCommandFlag(Boolean.FALSE);
        TestUtils.saveAndFlush(d);
        TestUtils.saveAndFlush(t);
        return d;
    }


    /**
     * Creates KickstartData for testing purposes.
     * @param orgIn 
     * @return Returns a committed KickstartData
     * @throws Exception
     */
    public static KickstartData createTestKickstartData(Org orgIn) throws Exception {
        
        String label = "KS Data: " + TestUtils.randomString();
        String name = "KS Data Name: " + TestUtils.randomString();
        String comments = "KS Data automated test";        

        Date created = new Date();
        Date modified = new Date();

        KickstartData k = new KickstartData();
        PackageName pn = PackageNameTest.createTestPackageName();
        PackageName pn2 = PackageNameTest.createTestPackageName();

        k.setLabel(label);
        k.setComments(comments);

        k.setOrg(orgIn);

        k.setCreated(created);
        k.setModified(modified);

        k.setActive(Boolean.TRUE);
        k.setOrgDefault(Boolean.FALSE);
                
        k.addScript(KickstartScriptTest.createPost(k));
        k.addScript(KickstartScriptTest.createPre(k));
        k.addScript(KickstartScriptTest.createPostChrootInt(k));
        k.addScript(KickstartScriptTest.createPreInterpreter(k));
        k.addScript(KickstartScriptTest.createPostInterpreter(k));
        
        k.setStaticDevice(STATIC_DEV);
        k.setKernelParams(KERNEL_PARAMS);

        k.addPackageName(pn);
        k.addPackageName(pn2);
        TestUtils.saveAndFlush(k);

        SortedSet<KickstartCommand> optionsSet = new TreeSet<KickstartCommand>();
        k.setCustomOptions(optionsSet);
       
               
        return k;
    }
    
    public static KickstartData createKickstartWithOptions(Org orgIn) throws Exception {
        KickstartData k = createTestKickstartData(orgIn);        
        assertNotNull(k);        
        
        
        k.setKsdefault(KickstartDataTest.createDefaults(k, 
                UserTestUtils.ensureOrgAdminExists(orgIn)));
        
        KickstartCommandName raidName = lookupByLabel("raids");
        assertNotNull(raidName);
        KickstartCommandName partitionName = lookupByLabel("partitions");
        assertNotNull(partitionName);
        KickstartCommandName logvolName = lookupByLabel("logvols");
        assertNotNull(logvolName);
        KickstartCommandName volgroupsName = lookupByLabel("volgroups");
        assertNotNull(volgroupsName);
        KickstartCommandName optionName = lookupByLabel("url");
        assertNotNull(optionName);
        //uncomment when schema supports include commands
        //KickstartCommandName includeName = lookupByLabel('include');
        
        Date created = new Date();
        Date modified = new Date();
                      
        KickstartCommand partition = new KickstartCommand();        
        partition.setCommandName(partitionName);
        partition.setArguments("swap --size=1000 --grow --maxsize=2000");
        partition.setKickstartData(k);
        partition.setCreated(created);
        partition.setModified(modified);
        k.addPartition(partition);
        
        //easier to test multiple swap loads here
        KickstartCommand partition2 = new KickstartCommand();        
        partition2.setCommandName(partitionName);
        partition2.setArguments("swap --size=1500 --grow --maxsize=2000");
        partition2.setKickstartData(k);
        partition2.setCreated(created);
        partition2.setModified(modified);
        k.addPartition(partition2);
        
        KickstartCommand raid = new KickstartCommand();        
        raid.setCommandName(raidName);
        raid.setArguments
        ("swap --fstype swap --level 0 --device 1 raid.05 raid.06 raid.07 raid.08");
        raid.setKickstartData(k);
        raid.setCreated(created);
        raid.setModified(modified);
        k.addRaid(raid);
        
        KickstartCommand logvol = new KickstartCommand();        
        logvol.setCommandName(logvolName);
        logvol.setArguments
        ("swap --fstype swap --name=lvswap --vgname=Volume00 --size=2048");
        logvol.setKickstartData(k);
        logvol.setCreated(created);
        logvol.setModified(modified);
        k.addLogVol(logvol);
        
        KickstartCommand volgroups = new KickstartCommand();        
        volgroups.setCommandName(volgroupsName);
        volgroups.setArguments("volgroup myvg pv.01");
        volgroups.setKickstartData(k);
        volgroups.setCreated(created);
        volgroups.setModified(modified);
        k.addVolGroup(volgroups);
        
        KickstartCommand option = new KickstartCommand();        
        option.setCommandName(optionName);
        option.setArguments
        ("--url http://rhn.redhat.com/kickstart/dist/ks-rhel-i386-as-3/");
        option.setKickstartData(k);
        option.setCreated(created);
        option.setModified(modified);
        k.addOption(option);
        
        return k;
    }

    public static KickstartData createKickstartWithChannel(Org orgIn) throws Exception {
        KickstartData ksdata = KickstartDataTest.createTestKickstartData(orgIn); 
        KickstartDefaults d1 = KickstartDataTest.createDefaults(ksdata, 
                UserTestUtils.ensureOrgAdminExists(orgIn));
        ksdata.setKsdefault(d1);
        return ksdata;
    }
    
    public static FileList createFileList1(Org org) {
        FileList list1 = FileListTest.createTestFileList(org);

        list1.addFileName("/tmp/foo.txt");

        return list1;
    }

    public static FileList createFileList2(Org org) {
        FileList list2 = FileListTest.createTestFileList(org);

        list2.addFileName("/tmp/foo.txt");
        list2.addFileName("/tmp/bar.txt");
     
        return list2;
    }

    public static FileList createFileList3(Org org) {
        FileList list3 = FileListTest.createTestFileList(org);

        list3.addFileName("/tmp/bar.txt");
        list3.addFileName("/tmp/baz.txt");
        list3.addFileName("/tmp/baz2.txt");
        
        return list3;
    }

    public void testPreserveFileLists() throws Exception {
        Org org = UserTestUtils.findNewOrg(TestStatics.TESTORG);

        FileList list1 = createFileList1(org);
        FileList list2 = createFileList2(org);
        FileList list3 = createFileList3(org);

        KickstartData kickstart = createKickstartWithOptions(org);
        
        kickstart.addPreserveFileList(list1);
        assertEquals(1, kickstart.getPreserveFileLists().size());
        kickstart.addPreserveFileList(list2);
        assertEquals(2, kickstart.getPreserveFileLists().size());
        kickstart.addPreserveFileList(list3);
        assertEquals(3, kickstart.getPreserveFileLists().size());

        kickstart.setPreserveFileLists(new HashSet());
        assertEquals(0, kickstart.getPreserveFileLists().size());
    }

    public void testCommands() throws Exception {
        KickstartData k = createKickstartWithOptions(user.getOrg());

        Long ksid = k.getId();
        KickstartFactory.saveKickstartData(k);
        flushAndEvict(k);

        KickstartData k2 = lookupById(user.getOrg(), ksid);
        assertNotNull(k2);

        assertEquals(2, k2.getPartitions().size());
        assertEquals(1, k2.getRaids().size());
        assertEquals(1, k2.getVolgroups().size());
        assertEquals(1, k2.getLogvols().size());
        assertEquals(1, k2.getOptions().size()); // url and command from k creation
    }

    public void testDeepCopy() throws Exception {
        // Setup the object for testing.
        KickstartData k = createKickstartWithOptions(user.getOrg());
        FileList list1 = createFileList1(user.getOrg());
        CommonFactory.saveFileList(list1);
        k.addPreserveFileList(list1);
        KickstartDefaultRegToken t = new KickstartDefaultRegToken();
        t.setKsdata(k);
        t.setToken(TokenTest.createTestToken());
        k.addDefaultRegToken(t.getToken());
        KickstartFactory.saveKickstartData(k);
        k = (KickstartData) reload(k);
        k = CryptoTest.addKeyToKickstart(k);
        k = KickstartIpTest.addIpRangesToKickstart(k);

        // save it and reload it
        KickstartFactory.saveKickstartData(k);
        k = (KickstartData) reload(k);
        
        // Now we deep copy it, save and reload
        KickstartData cloned = k.deepCopy(user, 
                "someNewName" + TestUtils.randomString(), 
                "someNewLabel" + TestUtils.randomString());
        KickstartFactory.saveKickstartData(cloned);
        cloned = (KickstartData) reload(cloned);
        
        // Test the basic fields
        assertEquals(k.getComments(), cloned.getComments());
        assertEquals(k.getBootloaderType(), cloned.getBootloaderType());
        assertEquals(k.getInstallType(), cloned.getInstallType());
        assertEquals(k.getKernelParams(), cloned.getKernelParams());
        assertEquals(k.getStaticDevice(), cloned.getStaticDevice());
        assertEquals(k.isActive(), cloned.isActive());
        assertEquals(k.isOrgDefault(), cloned.isOrgDefault());
        assertEquals(k.getOrg(), cloned.getOrg());
        
        // Test the advanced fields
        assertEquals(cloned.getKsdefault().getKstree(), k.getKsdefault().getKstree());
        verifySet(cloned.getCommands(),  k.getCommands(), KickstartCommand.class);
        verifySet(cloned.getCryptoKeys(), k.getCryptoKeys(), CryptoKey.class);
        verifySet(cloned.getDefaultRegTokens(), k.getDefaultRegTokens(), Token.class);
        
        verifySet(cloned.getLogvols(), k.getLogvols(), KickstartCommand.class);
        verifyList(cloned.getPackageNames(), k.getPackageNames(), PackageName.class);
        verifySet(cloned.getPreserveFileLists(), k.getPreserveFileLists(), FileList.class);
        verifySet(cloned.getRaids(), k.getRaids(), KickstartCommand.class);
        verifySet(cloned.getVolgroups(), k.getVolgroups(), KickstartCommand.class);
        verifySet(cloned.getScripts(), k.getScripts(), KickstartScript.class);
        // We don't clone IP ranges.  
        // verifySet(cloned.getIps(), k.getIps(), KickstartIpRange.class);
        
        KickstartScript ksscloned = (KickstartScript) cloned.getScripts().iterator().next();
        KickstartScript kss = (KickstartScript) k.getScripts().iterator().next();
        assertEquals(ksscloned.getDataContents(), kss.getDataContents());
        
    }
    
    // Test to make sure 
    public void testDeepCopyEmptySets() throws Exception {
        KickstartData k = createTestKickstartData(user.getOrg());
        k.setCryptoKeys(null);
        k.setCommands(null);
        k.setDefaultRegTokens(null);
        k.setIps(null);
        k.setPackageNames(null);
        k.setPreserveFileLists(null);
        k.setScripts(null);
        // Now we deep copy it, save and reload
        KickstartData cloned = k.deepCopy(user, 
                "someNewName" + TestUtils.randomString(), 
                "someNewLabel" + TestUtils.randomString());
        assertNotNull(cloned);
    }
    
    private void verifySet(Set cloned, Set orig, Class clazz) {
        assertTrue("orig doesnt have any: " + clazz.getName(), orig.size() > 0);
        assertTrue("cloned doesnt have any: " + clazz.getName(), cloned.size() > 0);
        assertEquals(cloned.size(), orig.size());
        assertTrue("Not instance of: " + clazz.getName(), 
                clazz.isInstance(cloned.iterator().next()));
    }
    
    private void verifyList(List cloned, List orig, Class clazz) {
        assertTrue("orig doesnt have any: " + clazz.getName(), orig.size() > 0);
        assertTrue("cloned doesnt have any: " + clazz.getName(), cloned.size() > 0);
        assertEquals(cloned.size(), orig.size());
        assertTrue("Not instance of: " + clazz.getName(), 
                clazz.isInstance(cloned.iterator().next()));
    }
    
    public void testISRhelRevMethods() throws Exception {
        
        KickstartData k = createKickstartWithChannel(user.getOrg());
        k.getTree().setInstallType(KickstartFactory.
                lookupKickstartInstallTypeByLabel(KickstartInstallType.RHEL_5));
        assertFalse(k.isRhel2());
        assertFalse(k.isRhel3());
        assertFalse(k.isRhel4());
        assertTrue(k.isRhel5());
        assertTrue(k.isRhel5OrGreater());
    }
    
    
}
