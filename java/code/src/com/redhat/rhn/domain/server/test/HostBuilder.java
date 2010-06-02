/**
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.domain.server.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.ServerGroupType;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.testing.TestUtils;

import org.hibernate.Session;

import java.util.Date;
import java.util.Iterator;



/**
 * HostBuilder is a class based on the GoF Builder pattern that constructs systems that are
 * virtual hosts. That is, systems with one of the virtualization entitlements.
 * @version $Rev$
 */
public class HostBuilder {
    
    private User owner;
    private Server host;
    
    public HostBuilder(User theOwner) {
        owner = theOwner;
    }
    
    /**
     * This is the final step in building or compiling a host. The host and its guests (if
     * there are any) will be persisted, flushed, and evicted from the current hibernate 
     * session.
     * 
     * <br/><br/>
     * 
     * The builder does not maintain a reference to a host once it is built; so, calling
     * <code>build</code> successive times will simply return <code>null</code>. One of the
     * <i>create</i> methods must be called before every invocation of this method.
     * 
     * @return The built host whose state will have persisted and synhronized with the
     * database. The host and its guests will have been removed from the hibernate session 
     * cache.
     */
    public Server build() {
       if (host == null) {
           return null;
       }
       
       Server compiledHost;
       Session session = HibernateFactory.getSession();
       
       session.flush();
       session.evict(host);
       
       for (Iterator iterator = host.getGuests().iterator(); iterator.hasNext();) {
           session.evict(iterator.next());
       }
       
       compiledHost = host;
       host = null;
       
       return compiledHost;
    }
    
    private HostBuilder createHost(ServerGroupType type) throws Exception {
        host = ServerFactoryTest.createTestServer(owner, true, type);
        if (host.getBaseEntitlement() == null) {
            SystemManager.entitleServer(host, EntitlementManager.MANAGEMENT);
        }
        return this;
    }
    
    /**
     * Creates a Server with the Virtualization Host entitlement.
     * 
     * @return This builder
     * 
     * @throws Exception if an error occurs
     */
    public HostBuilder createVirtHost() throws Exception {
        ServerGroupType type =
            ServerConstants.getServerGroupTypeVirtualizationEntitled();
        return createHost(type);
    }
    
    /**
     * Creates a Server with the Virtualization Platform entitlement.
     * 
     * @return This builder
     * 
     * @throws Exception if an error occurs
     */
    public HostBuilder createVirtPlatformHost() throws Exception {
        ServerGroupType type = 
            ServerConstants.getServerGroupTypeVirtualizationPlatformEntitled();
        return createHost(type);
    }
    
    /**
     * Creates a server without any of the virtualization entitlements. Note that a non-virt
     * host is useful in those situations in which a guest consumes physical entitlements.
     * 
     * @return This builder
     * 
     * @throws Exception if an error occurs
     */
    public HostBuilder createNonVirtHost() throws Exception {
        ServerGroupType type =
            ServerConstants.getServerGroupTypeEnterpriseEntitled();
        return createHost(type);
    }
    
    /**
     * Creates the specified number of guests for the host under construction. Each guest
     * will be registered and therefore have an associated system.
     * 
     * @param numberOfGuests The number of guests to create
     * 
     * @return This builder
     * 
     * @throws Exception if an error occurs
     */
    public HostBuilder withGuests(int numberOfGuests) throws Exception {
        createGuests(numberOfGuests, true);
        return this;
    }
    
    /**
     * Creates the specified number of guests for the host under construction. None of the
     * guests will be registered and therefore will not have an associated system.
     * 
     * @param numberOfGuests The number of guests to create
     * 
     * @return This builder
     * 
     * @throws Exception if an error occurs
     */
    public HostBuilder withUnregisteredGuests(int numberOfGuests) throws Exception {
        createGuests(numberOfGuests, false);
        return this;
    }
    
    private void createGuests(int numberOfGuests, boolean register) throws Exception {
        VirtualInstance virtualInstance = null;
        Server guest = null;
        
        for (int i = 0; i < numberOfGuests; ++i) {
            virtualInstance = new VirtualInstance();
            virtualInstance.setUuid(TestUtils.randomString());
            
            if (register) {
                guest = ServerFactoryTest.createUnentitledTestServer(owner, true,
                        ServerFactoryTest.TYPE_SERVER_NORMAL , new Date());
                virtualInstance.setGuestSystem(guest);
            }
            
            host.addGuest(virtualInstance);
            TestUtils.saveAndFlush(host);
            TestUtils.saveAndFlush(guest);
            //ServerFactory.save(guest);

        }
    }

}
