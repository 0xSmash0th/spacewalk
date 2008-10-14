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
package com.redhat.rhn.frontend.dto;



/**
 * DTO for a com.redhat.rhn.domain.ActivationKey
 * @version $Rev$
 */
public class OrgChannelDto extends BaseDto {

    private Long id;
    private boolean disabled;
    private String organization;
    private Integer systems;

    /**
     * Gets the value of id
     *
     * @return the value of id
     */
    public Long getId() {
        return this.id;
    }

    /**
     * @return Organization Name
     */
    public String getOrganization() {
        return organization;
    }

    /**
     * @param organizationIn name to set
     */
    public void setOrganization(String organizationIn) {
        this.organization = organizationIn;
    }

    /**
     * 
     * @return number of subscribed systems for organization
     */
    public Integer getSystems() {
        return systems;
    }

    /**
     * 
     * @param systemsIn number of subscribed systems to set for this org
     */
    public void setSystems(Integer systemsIn) {
        this.systems = systemsIn;
    }

    /**
     * Sets the value of id
     *
     * @param argId Value to assign to this.id
     */
    public void setId(Long argId) {
        this.id = argId;
    }

    /**
     * Is the key disabled?
     * @return Returns true if disabled, false if enabled.
     */
    public boolean isDisabled() {
        return disabled;
    }
    
    /**
     * Disable (or enable) the key
     * @param value 1 if the key is disabled
     */
    public void setDisabled(Integer value) {
        if (value == null || (!value.equals(new Integer(1)))) {
            this.disabled = false;
        }
        else {
            this.disabled = true;
        }

        return;
    }


}
