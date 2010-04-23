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
package com.redhat.rhn.frontend.dto;

import java.util.Date;

/**
 * 
 * NetworkDto
 * @version $Rev$
 */
public class NetworkDto {

    
    private Long systemId;
    private String key;
    private Date lastCheckin;
    
    
    
    /**
     * @return Returns the lastCheckin.
     */
    public Date getLastCheckin() {
        return lastCheckin;
    }


    
    /**
     * @param lastCheckinIn The lastCheckin to set.
     */
    public void setLastCheckin(Date lastCheckinIn) {
        this.lastCheckin = lastCheckinIn;
    }


    /**
     * @return Returns the key.
     */
    public String getKey() {
        return key;
    }

    
    /**
     * @param keyIn The key to set.
     */
    public void setKey(String keyIn) {
        this.key = keyIn;
    }

    /**
     * @return Returns the systemId.
     */
    public Long getSystemId() {
        return systemId;
    }
    
    /**
     * @param systemIdIn The systemId to set.
     */
    public void setSystemId(Long systemIdIn) {
        this.systemId = systemIdIn;
    }
    
    
}
