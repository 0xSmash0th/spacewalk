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
package com.redhat.rhn.manager.kickstart.cobbler;

import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.user.User;

import org.apache.log4j.Logger;

import java.util.Map;

/**
 * KickstartCobblerCommand - class to contain logic to communicate with cobbler
 * @version $Rev$
 */
public class CobblerProfileEditCommand extends CobblerProfileCommand {

    

    private static Logger log = Logger.getLogger(CobblerProfileEditCommand.class);
    
    /**
     * Constructor
     * @param ksDataIn to sync 
     * @param userIn - user wanting to sync with cobbler
     */
    public CobblerProfileEditCommand(KickstartData ksDataIn,
            User userIn) {
        super(ksDataIn, userIn);
    }

    /**
     * {@inheritDoc}
     */
    public ValidatorError store() {
        log.debug("ProfileMap: " + this.getProfileMap());
        Map cProfile = getProfileMap();
        String cProfileName = (String)cProfile.get("name");
        
        String handle = (String) invokeXMLRPC("get_profile_handle",
                cProfileName, xmlRpcToken);
        
        String spacewalkName = makeCobblerName(ksData);
        
        if (!spacewalkName.equals(cProfileName)) {
            invokeXMLRPC("rename_profile", handle, spacewalkName, xmlRpcToken);
            invokeCobblerUpdate();
            handle = (String) invokeXMLRPC("get_profile_handle", 
                                            spacewalkName, xmlRpcToken);            
        }
        updateCobblerFields(handle);
        
        invokeXMLRPC("save_profile", handle, xmlRpcToken);
        return null;
    }
}
