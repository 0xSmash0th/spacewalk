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
package com.redhat.rhn.frontend.xmlrpc.kickstart.tree.test;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartInstallType;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.domain.kickstart.test.KickstartableTreeTest;
import com.redhat.rhn.frontend.xmlrpc.kickstart.tree.KickstartTreeHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.testing.TestUtils;

import java.util.List;

/**
 * KickstartHandlerTest
 * @version $Rev$
 */
public class KickstartTreeHandlerTest extends BaseHandlerTestCase {
    
    private KickstartTreeHandler handler = new KickstartTreeHandler();
        
    public void testListKickstartableTrees() throws Exception {
        Channel baseChan = ChannelFactoryTest.createTestChannel(admin); 
        KickstartableTree testTree = KickstartableTreeTest.
            createTestKickstartableTree(baseChan);
        List ksTrees = handler.listKickstartableTrees(adminKey, 
                baseChan.getLabel());
        assertTrue(ksTrees.size() > 0);
        
        boolean found = false;
        for (int i = 0; i < ksTrees.size(); i++) {
            KickstartableTree t = (KickstartableTree)ksTrees.get(i);
            if (t.getId().equals(testTree.getId())) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }
    
    public void testCreateKickstartableTree() throws Exception {
        String label = TestUtils.randomString();
        List trees = KickstartFactory.
            lookupKickstartTreesByOrg(admin.getOrg());
        int origCount = 0;
        if (trees != null) {
            origCount = trees.size();
        }
        Channel baseChan = ChannelFactoryTest.createTestChannel(admin);
        handler.createTree(adminKey, label, 
                "http://localhost/ks", 
                baseChan.getLabel(), KickstartInstallType.RHEL_5);
        assertTrue(origCount + 1 == KickstartFactory.
                lookupKickstartTreesByOrg(admin.getOrg()).size());
    }
    
    public void testEditKickstartableTree() throws Exception {
        Channel baseChan = ChannelFactoryTest.createTestChannel(admin); 
        KickstartableTree testTree = KickstartableTreeTest.
            createTestKickstartableTree(baseChan);
        String newBase = "/tmp/new-base-path";
        Channel newChan = ChannelFactoryTest.createTestChannel(admin);
        
        handler.editTree(adminKey, testTree.getLabel(), 
                newBase, newChan.getLabel(), 
                testTree.getInstallType().getLabel());

        assertEquals(testTree.getBasePath(), newBase);
        assertEquals(testTree.getChannel(), newChan);
        assertNotNull(testTree.getInstallType());
    }
    
    public void testRenameKickstartableTree() throws Exception {
        Channel baseChan = ChannelFactoryTest.createTestChannel(admin); 
        KickstartableTree testTree = KickstartableTreeTest.
            createTestKickstartableTree(baseChan);
        String newLabel = "newlabel-" + TestUtils.randomString();
        handler.renameTree(adminKey, testTree.getLabel(), newLabel);
        assertEquals(newLabel, testTree.getLabel());
    }

    public void testDeleteKickstartableTree() throws Exception {
        Channel baseChan = ChannelFactoryTest.createTestChannel(admin); 
        KickstartableTree testTree = KickstartableTreeTest.
            createTestKickstartableTree(baseChan);
        String label = testTree.getLabel();
        handler.deleteTree(adminKey, label);
        assertNull(KickstartFactory.lookupKickstartTreeByLabel(label, admin.getOrg()));
    }

    public void testListTreeTypes() throws Exception {
        List types = handler.listKickstartInstallTypes(adminKey);
        assertNotNull(types);
        assertTrue(types.size() > 0);
        System.out.println("type: " + types.get(0).getClass().getName());
        assertTrue(types.get(0) instanceof KickstartInstallType);
    }
}
