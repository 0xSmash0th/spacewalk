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
package com.redhat.rhn.frontend.xmlrpc.test;

import com.redhat.rhn.common.db.datasource.CallableMode;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.dto.kickstart.KickstartDto;
import com.redhat.rhn.manager.kickstart.KickstartLister;
import com.redhat.rhn.manager.user.UserManager;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SatScrubberTest - this test actually cleans up old junit created test data.
 * After the 410 build is turned off on digdug/cruisecontrol we can remove this
 * test.  Didn't want to check it into our release branch.
 * @version $Rev$
 */
public class SatScrubberTest extends RhnBaseTestCase {
    
    private User orgAdmin;
    private static Logger log = Logger.getLogger(SatScrubberTest.class);

    
    public void testCleanupKickstarts() throws Exception {
        orgAdmin = UserFactory.findRandomOrgAdmin(OrgFactory.getSatelliteOrg());
        List kickstarts = KickstartLister.
            getInstance().kickstartsInOrg(orgAdmin.getOrg(), null);
        for (int i = 0; i < kickstarts.size(); i++) {
            KickstartDto dto = (KickstartDto) kickstarts.get(i);
            KickstartData ksdata = KickstartFactory.
                lookupKickstartDataByIdAndOrg(orgAdmin.getOrg(), dto.getId());
            if (ksdata.getLabel().startsWith("KS Data: ")) {
                KickstartFactory.removeKickstartData(ksdata);
            }
        }
        commitAndCloseSession();
    }
    
    
    public void testCleanupUsers() throws Exception {
        orgAdmin = UserFactory.findRandomOrgAdmin(OrgFactory.getSatelliteOrg());
        List users = UserManager.usersInOrg(orgAdmin, null, Map.class);
        for (int i = 0; i < users.size(); i++) {
            Map row = (Map) users.get(i);
            String login = (String) row.get("login");
            if (login.indexOf("test") > -1) {
                User lookedup = UserFactory.lookupByLogin(login);
                UserManager.deleteUser(orgAdmin, lookedup.getId());
            }
            if (i % 100 == 0) {
                log.debug("Deleted [" + i + "] users");
                commitAndCloseSession();
            }
        }
        commitAndCloseSession();
    }
    
    public void testCleanupServers() throws Exception {

        orgAdmin = UserFactory.findRandomOrgAdmin(OrgFactory.getSatelliteOrg());
        List systems = UserManager.visibleSystemsAsMaps(orgAdmin);
        int numdeleted = 0;
        for (int i = 0; i < systems.size(); i++) {
            Long sid = (Long) ((Map) systems.get(i)).get("id");
            String name = (String) ((Map) systems.get(i)).get("name");
            if (name.startsWith("serverfactorytest")) {
                CallableMode m = ModeFactory.
                    getCallableMode("System_queries", "delete_server");
                Map in = new HashMap();
                in.put("server_id", sid);
                m.execute(in, new HashMap());
                numdeleted++;
            }
            if (i % 100 == 0) {
                log.debug("Deleted [" + numdeleted + "] systems");
                commitAndCloseSession();
            }

        }
        commitAndCloseSession();
        log.debug("Done deleting [" + numdeleted + "] systems");
    }

    public void testCleanupOrgs() throws Exception {
        // testOrg
        DataResult dr = TestUtils.runTestQuery("get_test_orgs", new HashMap());
        for (int i = 0; i < dr.size(); i++) {
            Map row = (Map) dr.get(i);
            Long id = (Long) row.get("id");
            log.debug("Deleting org: " + id);
            try {
                OrgFactory.deleteOrg(new Long(id.longValue()));
            }
            catch (Exception e) {
                log.debug("Error deleting org: " + id);
            }
            if (i % 10 == 0) {
                log.debug("Deleted [" + i + "] orgs");
                commitAndCloseSession();
            }
        }
    }
}
