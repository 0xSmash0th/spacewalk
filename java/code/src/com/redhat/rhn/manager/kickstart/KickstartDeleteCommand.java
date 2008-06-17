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
package com.redhat.rhn.manager.kickstart;

import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.user.User;

/**
 * BaseKickstartDeleteCommand 
 * @version $Rev$
 */
public class KickstartDeleteCommand extends KickstartEditCommand {
     
    /**
     * @param ksidIn kickstartdata id
     * @param userIn kickstartdata user
     */
    public KickstartDeleteCommand(Long ksidIn, User userIn) {
        super(ksidIn, userIn);
    }

    /**
     * This is counter-intuitive however it is done this way to
     * reuse BaseKickstartEditAction
     * {@inheritDoc}
     */
    public ValidatorError store() {
        int deleted = KickstartFactory.removeKickstartData(getKickstartData());
        if (deleted == 0) {
            return new ValidatorError("kickstart.delete.error");
            
        }
        else {
            return null;
        }
    }

}
