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
package com.redhat.rhn.manager.kickstart.cobbler.test;

import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.test.KickstartDataTest;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.NetworkInterfaceTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.kickstart.KickstartUrlHelper;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerDistroCreateCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerDistroDeleteCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerDistroEditCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerLoginCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerProfileCreateCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerProfileDeleteCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerProfileEditCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerSystemCreateCommand;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import java.util.Map;

/**
 * CobblerCommandTest
 */
public class CobblerCommandTest extends BaseTestCaseWithUser {
    protected KickstartData ksdata;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        user = UserTestUtils.createUserInOrgOne();
        user.addRole(RoleFactory.ORG_ADMIN);
        this.ksdata = KickstartDataTest.createKickstartWithDefaultKey(this.user.getOrg());
        this.ksdata.getTree().setBasePath("/var/satellite/rhn/kickstart/ks-f9-x86_64/");
        // KickstartableTree tree = this.ksdata.getTree();
        // TestObjectStore.get().putObject("distro_name", tree.getLabel());
        // tree.setCobblerId((String) TestObjectStore.get().getObject("distro_uid"));
        //System.out.println("TreeID1: " + tree.getId());
        //System.out.println("TreeID1.cobblerId: " + tree.getCobblerId());
        //tree = (KickstartableTree) TestUtils.saveAndReload(tree);
        

        // Uncomment this if you want the tests to actually talk to cobbler
        // Config.get().setString(CobblerXMLRPCHelper.class.getName(),
        //        CobblerXMLRPCHelper.class.getName());
        //commitAndCloseSession();
        
        CobblerDistroCreateCommand dcreate = new 
            CobblerDistroCreateCommand(ksdata.getTree(), user);
        dcreate.store();
    }

    public void testSystemCreate() throws Exception {

        Server s = ServerTestUtils.createTestSystem(user);
        NetworkInterface device = NetworkInterfaceTest.createTestNetworkInterface(s);
        s.addNetworkInterface(device);
        
        CobblerSystemCreateCommand cmd = new CobblerSystemCreateCommand(user, s, ksdata, 
                    "http://localhost/test/path", TestUtils.randomString());
        cmd.store();
        assertNotNull(s.getCobblerId());
        Map systemMap = cmd.getSystemMap(); 
        assertNotNull(systemMap);
        assertTrue(systemMap.containsKey("name"));
        
        // Ensure we can call it twice.
        cmd = new CobblerSystemCreateCommand(user, s, ksdata, 
                "http://localhost/test/path", TestUtils.randomString());
        cmd.store();
        
    }
    
    public void testProfileCreate() throws Exception {
        CobblerProfileCreateCommand cmd = new CobblerProfileCreateCommand(
                ksdata, user);
        assertNull(cmd.store());
        Map profile = cmd.getProfileMap();
        assertNotNull(profile);
        assertNotNull(profile.get("name"));
    }

    public void testProfileEdit() throws Exception {
        // create one first
        CobblerProfileCreateCommand cmd = new CobblerProfileCreateCommand(
                ksdata, user);
        assertNull(cmd.store());

        // Now test edit
        CobblerProfileEditCommand pec = new 
            CobblerProfileEditCommand(ksdata, user);
        String newName = "some-new-name-" + System.currentTimeMillis();
        ksdata.setLabel(newName);
        assertNull(pec.store());
        Map profile = pec.getProfileMap(); 
        String profileName = (String) profile.get("name"); 
        assertNotNull(profileName);
    }

    public void testProfileDelete() throws Exception {
        CobblerProfileCreateCommand createCmd = new CobblerProfileCreateCommand(
                ksdata, user);
        assertNull(createCmd.store());

        CobblerProfileDeleteCommand cmd = new CobblerProfileDeleteCommand(ksdata, user);
        assertNull(cmd.store());
        assertTrue(cmd.getProfileMap().isEmpty());
    }

    public void testDistroCreate() throws Exception {
        CobblerDistroCreateCommand cmd = new 
            CobblerDistroCreateCommand(ksdata.getTree(), user);
        assertNull(cmd.store());
        assertNotNull(cmd.getDistroMap());
        Map ksmeta = (Map) cmd.getDistroMap().get("ksmeta");
        assertNotNull(ksmeta.get(KickstartUrlHelper.COBBLER_MEDIA_VARIABLE));
    }

    
    public void testDistroDelete() throws Exception {
        CobblerDistroDeleteCommand cmd = new 
            CobblerDistroDeleteCommand(ksdata.getTree(), user);
        assertNull(cmd.store());
    }
    
    public void testDistroEdit() throws Exception {
        CobblerDistroEditCommand cmd = new 
            CobblerDistroEditCommand(ksdata.getTree(), user);
        String newName = TestUtils.randomString();
        ksdata.getKickstartDefaults().getKstree().setLabel(newName);
        assertNull(cmd.store());
        Map distro = cmd.getDistroMap(); 
        String distroName = (String) distro.get("name"); 
        assertNotNull(distroName);
    }
    
    public void testLogin() throws Exception {
        user.addRole(RoleFactory.ORG_ADMIN);
        UserFactory.save(user);
        user = (User) reload(user);
        CobblerLoginCommand cmd = new CobblerLoginCommand();
        String cobblertoken = cmd.login(user.getLogin(), "password");
        assertNotNull(cobblertoken);
        assertTrue(cmd.checkToken(cobblertoken));
    }
    
}
