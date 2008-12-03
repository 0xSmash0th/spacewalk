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
package com.redhat.rhn.manager.rhnpackage.test;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageCapability;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.rhnpackage.test.PackageCapabilityTest;
import com.redhat.rhn.domain.rhnpackage.test.PackageNameTest;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.frontend.dto.PackageOverview;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.kickstart.tree.BaseTreeEditOperation;
import com.redhat.rhn.manager.rhnpackage.PackageManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.TestUtils;

import org.hibernate.Query;
import org.hibernate.Session;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * PackageManagerTest
 * @version $Rev$
 */
public class PackageManagerTest extends BaseTestCaseWithUser {

    public void testSystemPackageList() throws Exception {
        // need a system
        // need to add packages to that system
        // then need to query those values
        PageControl pc = new PageControl();
        pc.setIndexData(false);
        pc.setStart(1);
        
        user.addRole(RoleFactory.ORG_ADMIN);
        
        Server server = ServerFactoryTest.createTestServer(user, true);
        PackageManagerTest.addPackageToSystemAndChannel(
                "test-package-name" + TestUtils.randomString(), server, 
                ChannelFactoryTest.createTestChannel(user));

        DataResult dr = PackageManager.systemPackageList(server.getId(), pc);
        assertNotNull(dr);
        assertEquals(1, dr.size());

        for (Iterator itr = dr.iterator(); itr.hasNext();) {
            Object o = itr.next();
            assertTrue(o instanceof PackageListItem);
        }
    }
    
    public void testSystemAvailablePackages() throws Exception {
        // need a system
        // need to add packages to that system
        // then need to query those values
        PageControl pc = new PageControl();
        pc.setIndexData(false);
        pc.setStart(1);
        
        user.addRole(RoleFactory.ORG_ADMIN);
        
        Server server = ServerFactoryTest.createTestServer(user, true);

        PackageManagerTest.addPackageToSystemAndChannel(
                "test-package-name" + TestUtils.randomString(), server, 
                ChannelFactoryTest.createTestChannel(user));

        // hard code for now.
        DataResult dr = PackageManager.systemAvailablePackages(server.getId(), pc);
        assertNotNull(dr);
        assertEquals(0, dr.size());

        for (Iterator itr = dr.iterator(); itr.hasNext();) {
            Object o = itr.next();
            assertTrue(o instanceof PackageListItem);
        }
    }
    
    /**
     * This method inserts a record into the rhnServerPackage mapping
     * table to associate a given Server with a particular Package.
     * The web code doesn't actually create any of these records, but
     * this will be needed by the backend code.
     * @param srvr Server to associate with the packages
     * @param pn The package name to associate
     * @param pe The package evr (version and release).
     */
    public static void associateSystemToPackage(Server srvr,
            PackageName pn, PackageEvr pe) {
        try {
            WriteMode m = 
                ModeFactory.
                getWriteMode("test_queries", "insert_into_rhnServerPackage");
            Map params = new HashMap();
            params.put("server_id", srvr.getId());
            params.put("pn_id", pn.getId());
            params.put("p_epoch", pe.getEpoch());
            params.put("p_version", pe.getVersion());
            params.put("p_release", pe.getRelease());

            m.executeUpdate(params);
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method inserts a record into the rhnServerPackage mapping
     * table to associate a given Server with a particular Package.
     * The web code doesn't actually create any of these records, but
     * this will be needed by the backend code.
     * @param srvr Server to associate with the packages
     * @param pn The package name to associate
     * @param pe The package evr (version and release).
     */
    public static void associateSystemToPackage(Server srvr, Package p) {
        try {
            WriteMode m = 
                ModeFactory.
                getWriteMode("test_queries", "insert_into_rhnServerPackageSimple");
            Map params = new HashMap();
            params.put("server_id", srvr.getId());
            params.put("name_id", p.getPackageName().getId());
            params.put("evr_id", p.getPackageEvr().getId());

            m.executeUpdate(params);
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Add a new Package to the specified Channel and associate the system 
     * with it. 
     * @param s
     * @param p
     * @param c
     * @throws Exception 
     */
    public static Package addPackageToSystemAndChannel(String packageName,
            Server s, Channel c) throws Exception {
        Package retval = addPackageToChannel(packageName, c);
        PackageManagerTest.associateSystemToPackage(s, retval);
        return retval;
    }

    /**
     * Create a package with the given name and add it to the given channel.
     * If a package by that name already exists, this simply returns that package.
     * @param packageName The name of the package to create.
     * @param c The channel to which to add the package
     * @return The package with that name in the channel.
     * @throws Exception
     */
    public static Package addPackageToChannel(String packageName, Channel c)
            throws Exception {
        
        PackageName pn = PackageFactory.lookupOrCreatePackageByName(packageName);
        if (pn == null) {
            pn = PackageNameTest.createTestPackageName();
            pn.setName(packageName);
        }

        Long existingId = ChannelManager.getLatestPackageEqual(c.getId(), packageName);
        
        if (existingId != null) {
            return PackageFactory.lookupByIdAndOrg(existingId, c.getOrg());
        }
    
        //existingId = 
        Session session = HibernateFactory.getSession();    
        Query query = session.createQuery(
                "from Package as " +
                "package where package.org.id = " + c.getOrg().getId() + 
                " and package.packageName.id = " + pn.getId());
        List packages = query.list();
        Package retval = null;
        if (packages != null && packages.size() > 0) {
            retval = (Package) packages.get(0);
        }
        else {
            retval = PackageTest.createTestPackage(c.getOrg());
        }
        
        retval.setPackageName(pn);
        TestUtils.saveAndFlush(retval);
        PackageTest.addPackageToChannelNewestPackage(retval, c);
        
        return retval;
    }
    
    public void testCreateLotsofPackagesInChannel() throws Exception {
        String rand = TestUtils.randomString();
        Channel c = ChannelTestUtils.createTestChannel(user);
        for (int i = 0; i < 10; i++) {
            addPackageToChannel(rand, c);
        }
    }
    
    public void testPossiblePackagesForPushingIntoChannel() throws Exception {
        Errata e = ErrataFactoryTest.createTestPublishedErrata(user.getOrg().getId());
        Channel c = ChannelTestUtils.createTestChannel(user);
        DataResult dr = PackageManager.possiblePackagesForPushingIntoChannel(c.getId(), 
                e.getId(), null);
        assertTrue(dr.size() > 0);
   }
    
    
    public void testGetServerNeededUpdatePackageByName() throws Exception {
        user.addRole(RoleFactory.ORG_ADMIN);
        Server s = ServerFactoryTest.createTestServer(user);
        Channel c = ChannelFactoryTest.createTestChannel(user);
        addPackageToSystemAndChannel("some-test-package", s, c);
        // Not enough time actually test the results of this query for now
        // Just testing that it runs without SQL error. -mmccune
        assertNull(PackageManager.
                getServerNeededUpdatePackageByName(s.getId(), "some-test-package"));
    }
    
    public void testPackagesAvailableToErrata() throws Exception {
        Errata errata = ErrataFactoryTest.createTestPublishedErrata(user.getOrg().getId());
        PageControl pc = new PageControl();
        
        DataResult dr = PackageManager.packagesAvailableToErrata(errata, user, pc);
        assertNotNull(dr);
        
        Channel c = ChannelFactoryTest.createTestChannel();
        dr = PackageManager.packagesAvailableToErrataInChannel(errata, c.getId(), user, pc);
        assertNotNull(dr);
    }
    
    public void testPackageIdsInSet() throws Exception {
        
        DataResult dr = PackageManager.packageIdsInSet(user, "packages_to_add", 
                                                       new PageControl());
        
        assertNotNull(dr);
    }
    
    public void testVerCmp() {
        
        //epoch
        int result = testEpoch("1", "2");
        assertEquals(-1, result);
        
        result = testEpoch("2", "1");
        assertEquals(1, result);
        
        result = testEpoch(null, "1");
        assertEquals(-1, result);
        
        result = testEpoch("2", null);
        assertEquals(1, result);
        
        //version
        result = testVersion("1", "2");
        assertEquals(-1, result);
        
        result = testVersion("2", "1");
        assertEquals(1, result);
        
        result = testVersion(null, "1");
        assertEquals(-1, result);
        
        result = testVersion("1", null);
        assertEquals(1, result);
        
        //release
        result = testRelease("1", "2");
        assertEquals(-1, result);
        
        result = testRelease("2", "1");
        assertEquals(1, result);
        
        result = testRelease(null, "1");
        assertEquals(-1, result);
        
        result = testRelease("1", null);
        assertEquals(1, result);
        
        //make sure we test alpha-numerics through rpmVersionComparator
        result = testRelease("1.2b", "1.2c");
        assertEquals(-1, result);
        
        result = testRelease("1.2b3a", "1.2b2");
        assertEquals(1, result);
        
        //test all nulls
        result = testEpoch(null, null);
        assertEquals(0, result);
        
        //test equals
        result = PackageManager.verCmp("4", "2.1", "b3", 
                                       "4", "2.1", "b3");
        assertEquals(0, result);
    }
    
    // This test only works if you have Channels syched to your sat
    public void xxxxPackageNamesByCapability() throws Exception {
        
        /*user.addRole(RoleFactory.ORG_ADMIN);
        Channel c = ChannelFactoryTest.createTestChannel(user);
        Package pak = addPackageToChannel(user, TestUtils.randomString(), c);
        PackageCapability cap = PackageCapabilityTest.createTestCapability();*/
        DataResult dr = PackageManager.packageNamesByCapability(user.getOrg(), 
                "rhn.kickstart.boot_image");
        assertNotNull(dr);
        assertTrue(dr.size() > 0);
    }
    
    /**
     * Add the up2date package to a system and a channel.  Version 
     * should be specified such as "2.9.0"
     * 
     * @param userIn
     * @param s
     * @param version
     * @param c
     * @return
     * @throws Exception
     */
    public static Package addUp2dateToSystemAndChannel(User userIn, Server s, 
            String version, Channel c) throws Exception {
        
        Package p = null;
        PackageName pn = PackageFactory.lookupOrCreatePackageByName("up2date");
        if (pn != null) {
            List packages = PackageFactory.listPackagesByPackageName(pn);
            Iterator i = packages.iterator();
            while (i.hasNext()) {
                Package innerp = (Package) i.next();
                PackageEvr evr = innerp.getPackageEvr();
                if (evr != null && 
                        evr.getVersion().equals(version)) {
                    p = innerp;
                }
            }
        }
        if (p == null) {
            p = PackageManagerTest.
            addPackageToSystemAndChannel("up2date", s, c);
            PackageEvr pevr = p.getPackageEvr();
            pevr.setEpoch("0");
            pevr.setVersion(version);
            pevr.setRelease("0");
            TestUtils.saveAndFlush(p);
            TestUtils.saveAndFlush(pevr);
        }
        else {
            PackageManagerTest.associateSystemToPackage(s, p);
        }


        return p;
    }
    
    private int testEpoch(String e1, String e2) {
        return PackageManager.verCmp(e1, null, null, e2, null, null);
    }
    
    private int testVersion(String v1, String v2) {
        return PackageManager.verCmp("1", v1, null, "1", v2, null);
    }
    
    private int testRelease(String r1, String r2) {
        return PackageManager.verCmp("1", "2", r1, "1", "2", r2);
    }
    
    /**
     * Add a kickstart package with the given name to the given channel.
     * @param packageName
     * @param channel
     * @throws Exception
     */
    public static void addKickstartPackageToChannel(String packageName, Channel channel) 
    throws Exception {
        PackageCapability kickstartCapability =  findOrCreateKickstartCapability();
        com.redhat.rhn.domain.rhnpackage.Package kickstartPkg = 
            PackageManagerTest.addPackageToChannel(packageName, channel);
        
        WriteMode m = ModeFactory.getWriteMode("test_queries", 
                "insert_into_rhnPackageProvides");
        Map params = new HashMap();
        params.put("pkg_id", kickstartPkg.getId());
        params.put("capability_id", kickstartCapability.getId());
        params.put("sense_id", "8");
        m.executeUpdate(params);
        
        // Repeast for another sense:
        params.put("sense_id", "268435464");
        m.executeUpdate(params);
    }

    /**
     * Find the kickstart package capability if it exists, create it otherwise.
     * @return The kickstart package capability.
     * @throws Exception
     */
    private static PackageCapability findOrCreateKickstartCapability() throws Exception {
        Session session = HibernateFactory.getSession();
        Query query = session.createQuery(
                "from PackageCapability where name = :capability");
        query.setParameter("capability", BaseTreeEditOperation.KICKSTART_CAPABILITY);
        List results = query.list();
        
        // Multiple results could be returned for this capability, 
        // take the first:
        if (results.size() >= 1) {
            return (PackageCapability)results.get(0);
        }
        
        return PackageCapabilityTest.createTestCapability(
                BaseTreeEditOperation.KICKSTART_CAPABILITY);
    }
    
    public void testPackageNamesByCapabilityAndChannel() throws Exception {
        Channel channel1 = ChannelFactoryTest.createTestChannel(user);
        addKickstartPackageToChannel(Config.get().getKickstartPackageName(), channel1);
        
        // Add a regular non-kickstart package as well:
        PackageManagerTest.addPackageToChannel("Another package", channel1);
        
        DataResult dr = PackageManager.packageNamesByCapabilityAndChannel(user.getOrg(), 
                BaseTreeEditOperation.KICKSTART_CAPABILITY, channel1);
        assertNotNull(dr);
        assertEquals(1, dr.size());
        PackageListItem item = (PackageListItem)dr.get(0);
        assertEquals(Config.get().getKickstartPackageName(), item.getName());
    }
    
    // Verify that packages with the given capability in other channels are not visible:
    public void testPackageNamesByCapabilityAndChannelFiltering() throws Exception {
        Channel channel1 = ChannelFactoryTest.createTestChannel(user);
        addKickstartPackageToChannel(Config.get().getKickstartPackageName(), channel1);
        
        Channel channel2 = ChannelFactoryTest.createTestChannel(user);
        addKickstartPackageToChannel(Config.get().
                        getKickstartPackageName() + "2", channel2);
        
        DataResult dr = PackageManager.packageNamesByCapabilityAndChannel(user.getOrg(), 
                BaseTreeEditOperation.KICKSTART_CAPABILITY, channel1);
        assertNotNull(dr);
        
        assertEquals(1, dr.size());
        PackageListItem item = (PackageListItem)dr.get(0);
        assertEquals(Config.get().getKickstartPackageName(), item.getName());
    }
    
    public void testPackageNameOverview() {
        String packageName = "kernel";
        String[] channelarches = {"channel-ia32", "channel-x86_64"};
        DataResult dr = PackageManager.lookupPackageNameOverview(
                user.getOrg(), packageName, channelarches);
        
        assertNotNull(dr);
    }
    
    public void testLookupPackageForChannelFromChannel() throws Exception {
        Channel channel1 = ChannelFactoryTest.createTestChannel(user);
        Channel channel2 = ChannelFactoryTest.createTestChannel(user);
        
        Package pack = PackageTest.createTestPackage(null);
        channel1.addPackage(pack);
        
        List test = PackageManager.lookupPackageForChannelFromChannel(channel1.getId(), 
                channel2.getId());
        assertTrue(test.size() == 1);
        PackageOverview packOver = (PackageOverview) test.get(0);
        assertEquals(pack.getId(), packOver.getId());
        
        
        channel2.addPackage(pack);
        test = PackageManager.lookupPackageForChannelFromChannel(channel1.getId(), 
                channel2.getId());
        assertTrue(test.size() == 0);
    }
    
    public void testLookupCustomPackagesForChannel() throws Exception {
        Channel channel1 = ChannelFactoryTest.createTestChannel(user);
        Package pack = PackageTest.createTestPackage(user.getOrg());
        List test = PackageManager.lookupCustomPackagesForChannel(
                channel1.getId(), user.getOrg().getId());
        
        assertTrue(test.size() == 1);
        PackageOverview packOver = (PackageOverview) test.get(0);
        assertEquals(pack.getId(), packOver.getId());
        
        channel1.addPackage(pack);
        test = PackageManager.lookupCustomPackagesForChannel(
                channel1.getId(), user.getOrg().getId());
        
        assertTrue(test.size() == 0);
    }
    
    public void testLookupOrphanPackagesForChannel() throws Exception {
        Channel channel1 = ChannelFactoryTest.createTestChannel(user);
        Package pack = PackageTest.createTestPackage(user.getOrg());
        List test = PackageManager.lookupOrphanPackagesForChannel(
                channel1.getId(), user.getOrg().getId());
        
        assertTrue(test.size() == 1);
        PackageOverview packOver = (PackageOverview) test.get(0);
        assertEquals(pack.getId(), packOver.getId());
        
        channel1.addPackage(pack);
        test = PackageManager.lookupOrphanPackagesForChannel(
                channel1.getId(), user.getOrg().getId());
        
        assertTrue(test.size() == 0);
        Package pack2 = PackageTest.createTestPackage(user.getOrg());
        test = PackageManager.lookupOrphanPackagesForChannel(
                channel1.getId(), user.getOrg().getId());
        
        assertTrue(test.size() == 1);
        
    }    
    
    
}
