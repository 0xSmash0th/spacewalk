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
package com.redhat.rhn.domain.kickstart;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerCommand;

import java.util.Date;

/**
 * KickstartableTree
 * @version $Rev$
 */
public class KickstartableTree extends BaseDomainHelper {

    private String basePath;
    private Channel channel;
    private Long id;
    private KickstartInstallType installType;
    private String label;
    private Date lastModified;
    private String cobblerId;
    private String cobblerXenId;

    private Org org;
    private KickstartTreeType treeType;
    
    /**
     * @return Returns the basePath.
     */
    public String getBasePath() {
        return basePath;
    }
    
    /**
     * @param b The basePath to set.
     */
    public void setBasePath(String b) {
        this.basePath = b;
    }

    /**
     * @return Returns the channel.
     */
    public Channel getChannel() {
        return channel;
    }
    
    /**
     * @param c The channel to set.
     */
    public void setChannel(Channel c) {
        this.channel = c;
    }
    
    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }
    
    /**
     * @param i The id to set.
     */
    public void setId(Long i) {
        this.id = i;
    }
    
    /**
     * @return Returns the installType.
     */
    public KickstartInstallType getInstallType() {
        return installType;
    }
    
    /**
     * @param i The installType to set.
     */
    public void setInstallType(KickstartInstallType i) {
        this.installType = i;
    }
    
    /**
     * @return Returns the label.
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * @param l The label to set.
     */
    public void setLabel(String l) {
        this.label = l;
    }
    
    /**
     * @return Returns the lastModified.
     */
    public Date getLastModified() {
        return lastModified;
    }
    
    /**
     * @param l The lastModified to set.
     */
    public void setLastModified(Date l) {
        this.lastModified = l;
    }
    
    /**
     * @return Returns the orgId.
     */
    public Long getOrgId() {
        if (isRhnTree()) {
            return null;
        }
        return getOrg().getId();
    }

    /**
     * @return Returns the org.
     */    
    public Org getOrg() {
        return org;
    }
    
    /**
     * @param o The org to set.
     */
    public void setOrg(Org o) {
        this.org = o;
    }
    
    /**
     * @return Returns the treeType.
     */
    public KickstartTreeType getTreeType() {
        return treeType;
    }
    
    /**
     * @param t The treeType to set.
     */
    public void setTreeType(KickstartTreeType t) {
        this.treeType = t;
    }
    
    /**
     * Check to see if this tree is 'owned' by RHN.
     * @return boolean if this tree is owned or not by RHN
     */
    public boolean isRhnTree() {
        return this.org == null;  
    }
    
    /**
     * Get the default download location for this KickstartableTree.
     * 
     * eg: http://rlx-3-10.rhndev.redhat.com/rhn/kickstart/ks-rhel-i386-as-4
     * 
     * @param host used to Kickstart from
     * @return String url
     */
    public String getDefaultDownloadLocation(String host) {
        if (this.getBasePath() != null) {
            String defaultLocation = "ks/dist/" + this.getLabel();
            defaultLocation = defaultLocation.toLowerCase();
            if (basePathIsUrl()) {
                return this.getBasePath();
            }
            else {
                StringBuffer buf = new StringBuffer();
                if (host != null && host.length() > 0) {
                    buf.append("http://").append(host);
                }
                if (!defaultLocation.startsWith("/")) {
                    buf.append("/");
                }
                buf.append(defaultLocation);
                return buf.toString();                                    
            }
        }
        else {
            return "";
        }

    }
    
    /**
     * Check if the tree's base path is a fully qualified URL or just a relative path.
     * 
     * @return True if base path is a URL.
     */
    public boolean basePathIsUrl() {
        String defaultLocation = this.getBasePath().toLowerCase();
        return (defaultLocation.startsWith("http://") || 
                defaultLocation.startsWith("ftp://"));
    }
    

    /**
     * @return the cobblerDistroName
     */
    public String getCobblerDistroName() {
        return CobblerCommand.makeCobblerName(getLabel(), getOrg());
    }
    
    /**
     * @return the cobblerDistroName
     */
    public String getCobblerXenDistroName() {
        return CobblerCommand.makeCobblerName(getLabel() + ":xen", getOrg());
    }    


    /**
     * Basically returns the actual basepath
     * we need this method becasue the
     * database stores rhn/.... as basepath for redhat channels
     * and actual path for non redhat channels... 
     * @return the actual basepath.
     */
    private  String getAbsolutePath() {
        if (isRhnTree()) {
            //redhat channel append the mount point to 
            //base path...
            return Config.get().getKickstartMountPoint() + getBasePath();
        }
        //its a base channel return the 
        return getBasePath();
    }
    
    /**
     * Returns the kernel path 
     * includes the mount point 
     * its an absolute path.
     * @return the kernel path
     */
    public String getKernelPath() {
        return StringUtil.addPath(getAbsolutePath(), "/images/pxeboot/vmlinuz");
    }

    /**
     * Returns the Initrd path 
     * includes the mount point 
     * its an absolute path.
     * @return the Initrd path
     */
    public String getInitrdPath() {
        return StringUtil.addPath(getAbsolutePath(), "/images/pxeboot/initrd.img");
    }

    /**
     * Returns the kernel path for the xen kernel 
     * includes the mount point 
     * its an absolute path.
     * @return the kernel path
     */
    public String getKernelXenPath() {
        return StringUtil.addPath(getAbsolutePath(), "/images/xen/vmlinuz");
    }
    
    /**
     * Returns the Initrd path for the xen kernel
     * includes the mount point 
     * its an absolute path.
     * @return the Initrd path
     */
    public String getInitrdXenPath() {
        return StringUtil.addPath(getAbsolutePath(), "/images/xen/initrd.img");
    }    
    
    /**
     * @return Returns the cobblerId.
     */
    public String getCobblerId() {
        return cobblerId;
    }

    
    /**
     * @param cobblerIdIn The cobblerId to set.
     */
    public void setCobblerId(String cobblerIdIn) {
        this.cobblerId = cobblerIdIn;
    }

    
    /**
     * Gets the cobblerXenId, which is the cobbler id corresponding to the 
     *      cobbler distro that is pointing to Xen PV boot images instead of regular
     *      boot images (yes this sucks) 
     * @return Returns the cobblerXenId.
     */
    public String getCobblerXenId() {
        return cobblerXenId;
    }

    
    /**
     * @param cobblerXenIdIn The cobblerXenId to set.
     */
    public void setCobblerXenId(String cobblerXenIdIn) {
        this.cobblerXenId = cobblerXenIdIn;
    }
    
}
