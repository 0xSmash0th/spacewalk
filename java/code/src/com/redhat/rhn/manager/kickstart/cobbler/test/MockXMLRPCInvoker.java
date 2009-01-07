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

import com.redhat.rhn.frontend.xmlrpc.util.XMLRPCInvoker;
import com.redhat.rhn.testing.TestObjectStore;
import com.redhat.rhn.testing.TestUtils;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mock class for invoking xmlrpc
 * @author mmccune
 *
 */
public class MockXMLRPCInvoker implements XMLRPCInvoker {
    
    private static Logger log = Logger.getLogger(MockXMLRPCInvoker.class);
    
    private Set methodsCalled = new HashSet();
    
    public MockXMLRPCInvoker() { 
        log.debug("Constructor: " + TestUtils.randomString());
    }
    
    public Object invokeMethod(String procedureName, List args) {
        methodsCalled.add(procedureName);
        log.debug("invoking: " + procedureName + " with: " + args);
        
        // Check that none of the args are null
        // because xmlrpc doesnt allow this.
        for (int i = 0; i < args.size(); i++) {
            if (args.get(i) == null) {
                throw new NullPointerException(
                        "One of the args is null: " + args);
            }
        }
        
        
        if (procedureName.equals("new_profile") ||
                procedureName.equals("new_distro")) {
            return new String("1");
        }
        else if (procedureName.equals("modify_distro")) {
            if (args.get(1).equals("name")) {
                log.debug("ARGS: " + args);
                // Stick the mock name into our mockObjects map
                // so we can add it to the return list when calling
                // get_distros
                log.debug("Putting distro_name into map: " + args.get(2));
                TestObjectStore.get().putObject("distro_name", args.get(2));
                log.debug("mockobjects111: " + TestObjectStore.get().getObjects());
            }
            if (args.get(1).equals("ksmeta")) {
                log.debug("putting ksmeta into store: " + args.get(2));
                if (TestObjectStore.get().getObject("ksmeta") != null) {
                    Map ksmeta = (Map) TestObjectStore.get().getObject("ksmeta");
                    ksmeta.putAll((Map) args.get(2));
                }
                else {
                    TestObjectStore.get().putObject("ksmeta", args.get(2));
                }
            }
            return new String("1");
        }
        else if (procedureName.equals("get_distros")) {
            List retval = new LinkedList();
            if (methodsCalled.contains("remove_distro")) {
                return retval;
            }
            else {
                for (int i = 0; i < 10; i++) {
                    Map distro = new HashMap();
                    distro.put("name", TestUtils.randomString());
                    retval.add(distro);
                }
                // Put the mock distro we created with the call to modify_distro
                // into the return value.  Useful if you want to test creation
                // then a fetch.
                Map distro = new HashMap();
                log.debug("mockobjects in getdistros: " + 
                        TestObjectStore.get().getObjects());
                distro.put("name", TestObjectStore.get().getObject("distro_name"));
                distro.put("ksmeta", TestObjectStore.get().getObject("ksmeta"));
                retval.add(distro);
                return retval;
            }
        }
        else if (procedureName.equals("modify_profile")) {
            if (args.get(1).equals("name")) {
                log.debug("ARGS: " + args);
                TestObjectStore.get().putObject("profile_name", args.get(2));
            }
            return new String("1");
        }
        else if (procedureName.equals("get_profiles")) {
            List retval = new LinkedList();
            if (methodsCalled.contains("remove_profile")) {
                return retval;
            }
            else {
                for (int i = 0; i < 10; i++) {
                    Map distro = new HashMap();
                    distro.put("name", TestUtils.randomString());
                    distro.put("uid", String.valueOf(RandomUtils.nextInt(5000)));
                    retval.add(distro);
                }
                // Put the mock distro we created with the call to modify_distro
                // into the return value.  Useful if you want to test creation
                // then a fetch.
                Map profile = new HashMap();
                log.debug("mockobjects222: " + TestObjectStore.get().getObjects());
                profile.put("name", TestObjectStore.get().getObject("profile_name"));
                profile.put("uid", TestObjectStore.get().getObject("uid"));
                retval.add(profile);
                return retval;
            }
        }
        else if (procedureName.equals("get_profile")) {
            Map retval = new HashMap();
            if (methodsCalled.contains("remove_profile")) {
                return retval;
            }
            else {
                retval.put("name", TestUtils.randomString());
                return retval;
            }
        }
        else if (procedureName.equals("get_distro")) {
            Map retval = new HashMap();
            if (methodsCalled.contains("remove_distro")) {
                return retval;
            }
            else {
                log.debug("mockobjects in getdistros: " + 
                        TestObjectStore.get().getObjects());
                if (TestObjectStore.get().getObjects().containsKey("distro_name")) {
                    retval.put("name", TestObjectStore.get().getObject("distro_name"));
                    retval.put("ksmeta", TestObjectStore.get().getObject("ksmeta"));
                }
                else {
                    retval.put("name", TestUtils.randomString());
                }
                return retval;
            }
        }
        else if (procedureName.equals("get_system")) {
            Map retval = new HashMap();
            if (methodsCalled.contains("remove_system") ||
                    !methodsCalled.contains("save_system")) {
                return retval;
            }
            else {
                retval.put("name", TestUtils.randomString());
                retval.put("redhat-management-key", TestUtils.randomString());
                retval.put("uid", String.valueOf(RandomUtils.nextInt(5000)));
                return retval;
            }
        }
        else if (procedureName.equals("get_profile_handle")) {
            log.debug("get_profile_handle.ARGS: " + args);
            TestObjectStore.get().putObject("profile_name", args.get(0));
            return TestUtils.randomString();
        }
        else if (procedureName.equals("get_distro_handle")) {
            log.debug("get_distro_handle.ARGS: " + args);
            TestObjectStore.get().putObject("distro_name", args.get(0));
            return TestUtils.randomString();
        }
        else if (procedureName.equals("remove_distro")) {
            return new Boolean(true);
        }
        else if (procedureName.equals("remove_profile")) {
            return new Boolean(true);
        }
        else if (procedureName.equals("token_check")) {
            return new Boolean(true);
        }
        else if (procedureName.equals("login")) {
            return TestUtils.randomString();
        }
        else if (procedureName.equals("new_system")) {
            return TestUtils.randomString();
        }
        return new Object();
    }

}
