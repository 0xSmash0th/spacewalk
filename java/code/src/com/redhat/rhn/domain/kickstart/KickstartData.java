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

import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.common.FileList;
import com.redhat.rhn.domain.kickstart.crypto.CryptoKey;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.token.Token;
import com.redhat.rhn.domain.user.User;

import org.apache.commons.collections.bag.HashBag;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * KickstartData - Class representation of the table RhnKSData.
 * @version $Rev: 1 $
 */
public class KickstartData {

    private Long id;
    private Org org;
    private String label;
    private String name;
    private String comments;
    private Boolean active;
    private Boolean postLog; 
    private Boolean preLog;
    private Boolean ksCfg;
    private Date created;
    private Date modified;
    private Boolean isOrgDefault;
    private String kernelParams;    
    private Boolean nonChrootPost;
    private Boolean verboseUp2date;
    private String staticDevice;

    private Set cryptoKeys;
    private Set childChannels;
    private Set defaultRegTokens;
    private Set preserveFileLists;
    private List<PackageName> packageNames;        
    private Set<KickstartCommand> commands;    
    private Collection partitions;   // rhnKickstartCommand partitions
    private Set includes;     // rhnKickstartCommand includes
    private Set raids;        // rhnKickstartCommand raids
    private Set logvols;      // rhnKickstartCommand logvols
    private Set volgroups;    // rhnKickstartCommand volgroups
    private Set<KickstartCommand> options;      // rhnKickstartCommand options
    private Set repos;        // rhnKickstartCommand repo
    private Set ips;          // rhnKickstartIpRange
    private Set<KickstartScript> scripts;      // rhnKickstartScript
    private KickstartDefaults ksdefault;
    private SortedSet<KickstartCommand> customOptions;

    public static final String LEGACY_KICKSTART_PACKAGE_NAME = "auto-kickstart-";
    public static final String KICKSTART_PACKAGE_NAME = "rhn-kickstart";
    public static final String SELINUX_MODE_COMMAND = "selinux";
    
    private String cobblerName;

    
    /**
     * Initializes properties.
     */
    public KickstartData() {
        cryptoKeys = new HashSet();
        defaultRegTokens = new HashSet();
        preserveFileLists = new HashSet();
        packageNames = new ArrayList<PackageName>();
        commands = new HashSet<KickstartCommand>();
        partitions = new HashBag();
        includes = new TreeSet();
        raids = new TreeSet();
        logvols = new TreeSet();
        volgroups = new TreeSet();
        options = new HashSet();
        ips = new HashSet();
        scripts = new HashSet<KickstartScript>();
        postLog = new Boolean(false);
        preLog = new Boolean(false);
        ksCfg = new Boolean(false);
        verboseUp2date = new Boolean(false);
        nonChrootPost = new Boolean(false);
        childChannels = new HashSet();
    }
    
    /**
     * Logger for this class
     */
    private static Logger logger = Logger
            .getLogger(KickstartData.class);

    /** 
     * Getter for id 
     * @return Long to get
    */
    public Long getId() {
        return this.id;
    }

    /** 
     * Setter for id 
     * @param idIn to set
    */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * Associates the KS with an Org.
     * @param orgIn Org to be associated to this KS.
     */
    public void setOrg(Org orgIn) {
        org = orgIn;
    }

    /** 
     * Getter for org 
     * @return org to get
    */
    public Org getOrg() {
        return org;
    }

    /** 
     * Getter for label 
     * @return String to get
    */
    public String getLabel() {
        return this.label;
    }

    /** 
     * Setter for label 
     * @param labelIn to set
    */
    public void setLabel(String labelIn) {
        this.label = labelIn;
    }

    /** 
     * Getter for name 
     * @return String to get
    */
    public String getName() {
        return this.name;
    }

    /** 
     * Setter for name 
     * @param nameIn to set
    */
    public void setName(String nameIn) {
        this.name = nameIn;
        if (this.cobblerName == null) {
            this.cobblerName = nameIn;
        }
    }

    /** 
     * Getter for comments 
     * @return String to get
    */
    public String getComments() {
        return this.comments;
    }

    /** 
     * Setter for comments 
     * @param commentsIn to set
    */
    public void setComments(String commentsIn) {
        this.comments = commentsIn;
    }

    /** 
     * Getter for active 
     * @return String to get
    */
    public Boolean getActive() {
        return this.active;
    }

    /** 
     * Setter for active 
     * @param activeIn to set
    */
    public void setActive(Boolean activeIn) {
        this.active = activeIn;
    }

    /** 
     * Getter for created 
     * @return Date to get
    */
    public Date getCreated() {
        return this.created;
    }

    /** 
     * Setter for created 
     * @param createdIn to set
    */
    public void setCreated(Date createdIn) {
        this.created = createdIn;
    }

    /** 
     * Getter for modified 
     * @return Date to get
    */
    public Date getModified() {
        return this.modified;
    }

    /** 
     * Setter for modified 
     * @param modifiedIn to set
    */
    public void setModified(Date modifiedIn) {
        this.modified = modifiedIn;
    }

    /** 
     * Getter for isOrgDefault 
     * @return String to get
    */
    public Boolean getIsOrgDefault() {
        return this.isOrgDefault;
    }

    /** 
     * Setter for isOrgDefault 
     * @param isOrgDefaultIn to set
    */
    public void setIsOrgDefault(Boolean isOrgDefaultIn) {
        this.isOrgDefault = isOrgDefaultIn;
    }

    /** 
     * Getter for kernelParams 
     * @return String to get
    */
    public String getKernelParams() {
        return this.kernelParams;
    }

    /** 
     * Setter for kernelParams 
     * @param kernelParamsIn to set
    */
    public void setKernelParams(String kernelParamsIn) {
        this.kernelParams = kernelParamsIn;
    }

    /** 
     * Getter for staticDevice 
     * @return String to get
    */
    public String getStaticDevice() {
        return this.staticDevice;
    }

    /** 
     * Setter for staticDevice 
     * @param staticDeviceIn to set
    */
    public void setStaticDevice(String staticDeviceIn) {
        this.staticDevice = staticDeviceIn;
    }

    
    /**
     * @return the cryptoKeys
     */
    public Set getCryptoKeys() {
        return cryptoKeys;
    }

    
    /**
     * @param cryptoKeysIn The cryptoKeys to set.
     */
    public void setCryptoKeys(Set cryptoKeysIn) {
        this.cryptoKeys = cryptoKeysIn;
    }

    /**
     * Add a CryptoKey to this kickstart
     * @param key to add
     */
    public void addCryptoKey(CryptoKey key) {
        this.cryptoKeys.add(key);
    }
    
    /**
     * Remove a crypto key from the set.
     * @param key to remove.
    */
    public void removeCryptoKey(CryptoKey key) {
        this.cryptoKeys.remove(key);
    }

    /**
     * @return the childChannels
     */
    public Set getChildChannels() {
        return childChannels;
    }

    /**
     * @param childChannelsIn childChannels to set.
     */
    public void setChildChannels(Set childChannelsIn) {
        this.childChannels = childChannelsIn;
    }

    /**
     * Add a ChildChannel to this kickstart
     * @param childChnl to add
     */
    public void addChildChannel(Channel childChnl) {
        if (this.childChannels == null) {
            this.childChannels = new HashSet();
        }
        this.childChannels.add(childChnl);
    }

    /**
     * Remove a child Channel from the set.
     * @param childChnl to remove.
     */
    public void removeChildChannel(Channel childChnl) {
        this.childChannels.remove(childChnl);
    }

    /**
     * Adds an Token object to default.
     * Note that an ActivationKey is almost the same as a Token.  Sorry.
     * @param key Token to add
     */
    public void addDefaultRegToken(Token key) {
        defaultRegTokens.add(key);
    }

    /**
     * Getter for defaultRegTokens
     * @return Returns the pacakageLists.
     */
    public Set getDefaultRegTokens() {
        return defaultRegTokens;
    }

    /**
     * Setter for defaultRegTokens
     * @param p The pacakgeLists to set.
     */
    public void setDefaultRegTokens(Set p) {
        this.defaultRegTokens = p;
    }

    /**
     * Gets the value of preserveFileLists
     *
     * @return the value of preserveFileLists
     */
    public Set getPreserveFileLists() {
        return this.preserveFileLists;
    }

    /**
     * Sets the value of preserveFileLists
     *
     * @param preserveFileListsIn set of FileList objects to assign to
     * this.preserveFileLists
     */
    public void setPreserveFileLists(Set preserveFileListsIn) {
        this.preserveFileLists = preserveFileListsIn;
    }

    /**
     * Adds a PreserveFileList object to preserveFileLists
     * @param fileList preserveFileList to add
     */
    public void addPreserveFileList(FileList fileList) {
        preserveFileLists.add(fileList);
    }

    /**
     * Adds a PackageName object to packageNames.
     * @param p PackageName to add
     */
    public void addPackageName(PackageName p) {
        packageNames.add(p);
    }

    /**
     * Getter for packageNames
     * @return Returns the pacakageNames.
     */
    public List<PackageName> getPackageNames() {
        return packageNames;
    }

    /**
     * Setter for packageNames
     * @param p The pacakgeLists to set.
     */
    public void setPackageNames(List<PackageName> p) {
        this.packageNames = p;
    }
 
    /**
     * Get the KickstartScript of type "pre"
     * @return KickstartScript used by the Pre section.  Null if not used
     */
    public KickstartScript getPreKickstartScript() {
        return lookupScriptByType(KickstartScript.TYPE_PRE); 
    }
    
    /**
     * Get the KickstartScript of type "post" 
     * @return KickstartScript used by the post section.  Null if not used
     */
    public KickstartScript getPostKickstartScript() {
        return lookupScriptByType(KickstartScript.TYPE_POST); 
    }
    
    
    private KickstartScript lookupScriptByType(String typeIn) {
        if (this.getScripts() != null && 
            this.getScripts().size() > 0) {
            Iterator i = this.getScripts().iterator();
            while (i.hasNext()) {
                KickstartScript kss = (KickstartScript) i.next();
                if (kss.getScriptType().equals(typeIn)) {
                    return kss;
                }
            }
        } 
        return null;
    }
    
    /**
     * Getter for commands
     * @return Returns commands 
     */
    public Set<KickstartCommand> getCommands() {
        return this.commands;
    }
    
    /**
     * Convenience method to detect if command is set
     * @param commandName Command name
     * @return true if found, otherwise false
     */
    public boolean hasCommand(String commandName) {
        boolean retval = false;
        if (this.commands != null && this.commands.size() > 0) {
            for (Iterator iter = this.commands.iterator(); iter.hasNext();) {
                KickstartCommand cmd = (KickstartCommand) iter.next();
                if (cmd.getCommandName().getName().equals(name)) {
                    retval = true;
                    break;
                }
            }
        }
        return retval;
    }
    
    /**
     * Convenience method to remove commands by name
     * @param commandName Command name
     * @param removeFirst if true only stop at first instance, otherwise remove all
     */
    public void removeCommand(String commandName, boolean removeFirst) {
        if (this.commands != null && this.commands.size() > 0) {
            for (Iterator iter = this.commands.iterator(); iter.hasNext();) {
                KickstartCommand cmd = (KickstartCommand) iter.next();
                if (cmd.getCommandName().getName().equals(commandName)) {
                    iter.remove();
                    if (removeFirst) {
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Convenience method to find a command by name stopping at the first match
     * @param commandName Command name
     * @return command if found, otherwise null
     */
    public KickstartCommand getCommand(String commandName) {
        KickstartCommand retval = null;
        if (this.commands != null && this.commands.size() > 0) {
            for (Iterator iter = this.commands.iterator(); iter.hasNext();) {
                KickstartCommand cmd = (KickstartCommand) iter.next();
                if (cmd.getCommandName().getName().equals(commandName)) {
                    retval = cmd;
                    break;
                }
            }
        }
        return retval;
    }

    /**
     * Setter for commands
     * @param c The Command List to set.
     */
    public void setCommands(Set<KickstartCommand> c) {
        this.commands = c;
    }
    
    /**
     * Getter for commandPartion
     * @return Returns commandPartions 
     */
    public Collection getPartitions() {
        return this.partitions;
    }

    /**
     * Setter for commandPartion
     * @param p The Command Partition List to set.
     */
    public void setPartitions(Collection p) {
        this.partitions = p;
    }
    
    /**
     * Adds a Partition Command object to partitions.
     * @param p partition to add
     */
    public void addPartition(KickstartCommand p) {
        partitions.add(p);
    }
    
    /**
     * Getter for commandIncludes
     * @return Returns commandIncludes 
     */
    public Set getIncludes() {
        return this.includes;
    }

    /**
     * Setter for commandIncludes
     * @param i The Command Includes List to set.
     */
    public void setIncludes(Set i) {
        this.includes = i;
    }
    
    /**
     * Adds a include KickstartCommand object to includes.
     * @param i Include to add
     */
    public void addInclude(KickstartCommand i) {
        includes.add(i);
    }
    
    /**
     * Getter for commandVolGroups
     * @return Returns commandVolGroups 
     */
    public Set getVolgroups() {
        return this.volgroups;
    }
    
    /**
     * Setter for commandvolgroups
     * @param v The Command VolGroup List to set.
     */
    public void setVolgroups(Set v) {
        this.volgroups = v;
    }

    /**
     * Adds a include KickstartCommand volgroup object to volgroups.
     * @param v Include to add
     */
    public void addVolGroup(KickstartCommand v) {
        volgroups.add(v);
    }
     
    /**
     * Getter for commandLogVols
     * @return Returns commandLogVols 
     */
    public Set getLogvols() {
        return this.logvols;
    }
       
    /**
     * Setter for commandLogVols
     * @param l The Command Log Vol List to set.
     */
    public void setLogvols(Set l) {
        this.logvols = l;
    }
    
    /**
     * Adds a logvol KickstartCommand object to logvols.
     * @param l logvol to add
     */
    public void addLogVol(KickstartCommand l) {
        logvols.add(l);
    }
    
    /**
     * Getter for command raids
     * @return Returns Kickstartcommand raids 
     */
    public Set getRaids() {
        return this.raids;
    }
 
    /**
     * Setter for commandRaids
     * @param r The Command Raids List to set.
     */
    public void setRaids(Set r) {
        this.raids = r;
    }
        
    /**
     * Adds a raid KickstartCommand object to raids.
     * @param r raid to add
     */
    public void addRaid(KickstartCommand r) {
        raids.add(r);
    }
    
    /**
     * Getter for command options
     * @return Returns Kickstartcommand options 
     */
    public Set<KickstartCommand> getOptions() {
        return this.options;
    }
 
    /**
     * Setter for command Options
     * @param o The Command Options List to set.
     */
    public void setOptions(Set o) {
        this.options = o;
    }
        
    /**
     * Adds a option KickstartCommand object to options set.
     * @param o option to add
     */
    public void addOption(KickstartCommand o) {
        options.add(o);
    }
    
    /**
     * 
     * @param kd KickstartDefaults to set
     */
    public void setKsdefault(KickstartDefaults kd) {
        this.ksdefault = kd;        
    }
    
    /**
     * 
     * @return the Kickstart Defaults assoc w/this Kickstart
     */
    public KickstartDefaults getKsdefault() {
        return this.ksdefault;
    }
        
    /**
     * Conv method 
     * @return Install Type for Kickstart
     */
    public String getInstallType() {
        String installType = null;
        if (this.getTree() != null && this.getTree().getInstallType() != null) {
            installType = this.getTree().getInstallType().getLabel();
        }   
        return installType;
    }

    /**
     * @return if this kickstart profile is rhel 5 installer type
     */
    public boolean isRhel5() {
        return this.getInstallType().endsWith("5");
    }

    /**
     * @return if this kickstart profile is rhel 5 installer type or greater (for rhel6)
     */
    public boolean isRhel5OrGreater() {
        return (!this.isRhel2() && !this.isRhel3() && !this.isRhel4());
    }

    /**
     * returns true if this is a fedora kickstart
     * @return if this is a fedora kickstart or not
     */
    public boolean isFedora() {
        return this.getInstallType().startsWith("fedora");
    }
    
    /**
     * @return if this kickstart profile is rhel 4 installer type
     */
    public boolean isRhel4() {
        return this.getInstallType().endsWith("4");
    }

    /**
     * @return if this kickstart profile is rhel 3 installer type
     */
    public boolean isRhel3() {
        return this.getInstallType().endsWith("3");
    }
    
    /**
     * 
     * @return if this kickstart profile is rhel 2 installer type
     */
    public boolean isRhel2() {
        return this.getInstallType().endsWith("2.1");
    }

    /**
     * 
     * @return Set of IpRanges for Kickstart
     */
    public Set<KickstartIpRange> getIps() {
        return ips;
    }

    /**
     * 
     * @param ipsIn Set of IPRanges to set
     */
    public void setIps(Set ipsIn) {        
        this.ips = ipsIn;
    }    
    
    /**
     * 
     * @param ipIn KickstartIpRange to add
     */
    public void addIpRange(KickstartIpRange ipIn) {
        ips.add(ipIn);
    }

    /**
     * Convenience method to get the KickstartableTree object
     * @return KickstartableTree object associated with this KSData.
     */
    public KickstartableTree getTree() {
        if (this.getKsdefault() != null) {
            return this.getKsdefault().getKstree();
        }
        return null;
    }

    /**
     * Setter for KickstartableTree object
     * @param kstreeIn the KickstartableTree to set
     */
    public void setTree(KickstartableTree kstreeIn) {
        this.getKsdefault().setKstree(kstreeIn);
    }
     
    /**
     * @return the scripts
     */
    public Set<KickstartScript> getScripts() {
        return scripts;
    }

    
    /**
     * @param scriptsIn The scripts to set.
     */
    public void setScripts(Set scriptsIn) {
        this.scripts = scriptsIn;
    }

    /**
     * Add a KickstartScript to the KickstartData
     * @param ksIn to add
     */
    public void addScript(KickstartScript ksIn) {
        // Calc the max position and add this script at the end
        Iterator i = scripts.iterator();
        long maxPosition = 0;
        while (i.hasNext()) {
            KickstartScript kss = (KickstartScript) i.next();
            if (kss.getPosition().longValue() > maxPosition) {
                maxPosition = kss.getPosition().longValue();
            }
        }
        ksIn.setPosition(new Long(maxPosition + 1));
        ksIn.setKsdata(this);
        
        scripts.add(ksIn);
    }
    
    /**
     * Remove a KickstartScript from this Profile.
     * @param ksIn to remove.
     */
    public void removeScript(KickstartScript ksIn) {
        scripts.remove(ksIn);
    }


    /**
     * Is ELILO required for this kickstart profile?
     * We base this off of the channel arch, because IA64 systems
     * require elilo
     * @return boolean - required, or not
     */
    public boolean getEliloRequired() {
        return this.getKsdefault().getKstree().getChannel()
            .getChannelArch().getLabel().equals("channel-ia64");
    }

    /**
     * Get the bootloader type
     *
     * @return String: lilo or grub
     */
    public String getBootloaderType() {
        KickstartCommand bootloaderCommand = this.getCommand("bootloader");

        if (bootloaderCommand == null || bootloaderCommand.getArguments() == null) {
            return "grub";
        }

        String regEx = ".*--useLilo.*";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(bootloaderCommand.getArguments());

        if (matcher.matches()) {
            return "lilo";
        }
        else {
            return "grub";
        }
    }
    
    /**
     * Changes the bootloader
     * @param type either "grub" or "lilo"
     * @return true if changed, false otherwise
     */
    public boolean changeBootloaderType(String type) {
        boolean retval = false;
        KickstartCommand bootloaderCommand = this.getCommand("bootloader");
        if (bootloaderCommand != null) {
            retval =  true;
            bootloaderCommand.setArguments(
                    bootloaderCommand.getArguments().replaceAll(
                            "--useLilo", "").trim());            
            if (type.equalsIgnoreCase("lilo")) {
                bootloaderCommand.setArguments(bootloaderCommand.getArguments() + 
                        " --useLilo");
            }
        }
        
        return retval;
    }
    
    /**
     * Convenience method to get the Channel associated with this profile
     * KickstartData -> KickstartDefault -> KickstartTree -> Channel
     * @return Channel object associated with this KickstartData
     */
    public Channel getChannel() {
        if (this.ksdefault != null) {
            if (this.ksdefault.getKstree() != null) {
                return this.ksdefault.getKstree().getChannel();
            }
        }
        return null;
    }

    /**
     * Get the timezone - just the timezone, not the --utc or other args
     *
     * @return String: The timezone (like "Asia/Qatar")
     */
    public String getTimezone() {
        KickstartCommand tzCommand = this.getCommand("timezone");

        // my @args = grep { not /--/ } split / /, $tzCommand;
        // return @args ? $args[0] : "";

        if (tzCommand == null || tzCommand.getArguments() == null) {
            return "";
        }

        LinkedList tokens =
            (LinkedList) StringUtil.stringToList(tzCommand.getArguments());

        Iterator iter = tokens.iterator();

        while (iter.hasNext()) {
            String token = (String) iter.next();

            if (!token.startsWith("--")) {
                return token;
            }
        }

        return null;
    }

    /**
     * Will the system hardware clock use UTC
     *
     * @return Boolean Are we using UTC?
     */
    public Boolean isUsingUtc() {
        KickstartCommand tzCommand = this.getCommand("timezone");

        if (tzCommand == null || tzCommand.getArguments() == null) {
            return Boolean.FALSE;
        }

        LinkedList tokens =
            (LinkedList) StringUtil.stringToList(tzCommand.getArguments());

        Iterator iter = tokens.iterator();

        while (iter.hasNext()) {
            String token = (String) iter.next();

            if (token.equals("--utc")) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    /**
     * Copy this KickstartData into a new one.  NOTE:  We don't clone
     * the following sub-objects:
     * 
     * KickstartIpRange
     * 
     * NOTE: We also don't clone isOrgDefault.
     * 
     * @param user who is doing the cloning
     * @param newName to set on the cloned object
     * @param newLabel to set on the cloned object
     * @return KickstartData that is cloned.
     */
    public KickstartData deepCopy(User user, String newName, String newLabel) {
        KickstartData cloned = new KickstartData();
        cloned.setName(newName);
        cloned.setLabel(newLabel);
        cloned.setActive(this.getActive());
        cloned.setPostLog(this.getPostLog());
        cloned.setPreLog(this.getPreLog());
        cloned.setKsCfg(this.getKsCfg());
        cloned.setComments(this.getComments());
        cloned.setNonChrootPost(this.getNonChrootPost());
        cloned.setVerboseUp2date(this.getVerboseUp2date());
        cloned.setOrg(this.getOrg());
        cloned.setChildChannels(new HashSet(this.getChildChannels()));
        
        if (this.getCommands() != null) {
            Iterator i = this.getCommands().iterator();
            while (i.hasNext()) {
                KickstartCommand cmd = (KickstartCommand) i.next();
                KickstartCommand clonedCmd = cmd.deepCopy(cloned);
                cloned.addCommand(clonedCmd);
            }
        }
        // Gotta remember to create a new HashSet with
        // the other objects.  Otherwise hibernate will
        // complain that you are using the same collection
        // in two objects.
        if (this.getCryptoKeys() != null) {
            cloned.setCryptoKeys(new HashSet(this.getCryptoKeys()));
        }
        
        if (this.getDefaultRegTokens() != null) {
            cloned.setDefaultRegTokens(new HashSet(this.getDefaultRegTokens()));
        }

        // NOTE: Make sure we *DONT* clone isOrgDefault
        cloned.setIsOrgDefault(Boolean.FALSE);
        cloned.setKernelParams(this.getKernelParams());
        if (this.getKsdefault() != null) {
            cloned.setKsdefault(this.getKsdefault().deepCopy(cloned));
        }
        cloned.setOrg(this.getOrg());
        if (this.getPackageNames() != null) {
            cloned.setPackageNames(new ArrayList(this.getPackageNames()));
        }
        if (this.getPreserveFileLists() != null) {
            cloned.setPreserveFileLists(new HashSet(this.getPreserveFileLists()));
        }
        
        if (this.getScripts() != null) {
            Iterator i = this.getScripts().iterator();
            while (i.hasNext()) {
                KickstartScript kss = (KickstartScript) i.next();
                KickstartScript ksscloned = kss.deepCopy(cloned);
                cloned.addScript(ksscloned);
            }
        }

        cloned.setStaticDevice(this.getStaticDevice());
        return cloned;
    }
    
    
    /**
     * Add a kickstartCommand object
     * @param clonedCmd The kickstartCommand to add
     */
    public void addCommand(KickstartCommand clonedCmd) {
        commands.add(clonedCmd);
    }
    
    /**
     * Util method to determine if we are RHEL3/2.1
     * @return boolean if this KickstartData is using RHEL2.1 or RHEL3
     */
    public boolean isLegacyKickstart() {
        if (this.getTree() != null && this.getTree().getInstallType() != null) {
            String installType = this.getTree().getInstallType().getLabel();
            return (installType.equals(KickstartInstallType.RHEL_21) ||
                    installType.equals(KickstartInstallType.RHEL_3));
        }
        else {
            return false;
        }
        
    }
    
    /**
     * Util method to determine if we are pre RHEL5
     * @return boolean if this KickstartData is using RHEL2.1, RHEL3, or RHEL4
     */
    public boolean isPreRHEL5Kickstart() {
        if (this.isLegacyKickstart()) {
            return true;
        }
        if (this.getTree() != null && this.getTree().getInstallType() != null) {
            String installType = this.getTree().getInstallType().getLabel();
            return (installType.equals(KickstartInstallType.RHEL_4));
        } 
        else {
            return false;
        }
        
    }
    
    /**
     * Bean wrapper so we can call isLegacyKickstart() from JSTL
     * @return boolean if this KickstartData is using RHEL2.1 or RHEL3
     */
    public boolean getLegacyKickstart() {
        return isLegacyKickstart();
    }

    /**
     * Get the name of the kickstart package this KS will use.
     * @return String kickstart package like auto-kickstart-ks-rhel-i386-as-4
     */
    public String getKickstartPackageName() {
        String installType = this.getInstallType();
        // For the older revs of RHEL we want to use auto-kickstart-*
        if (installType.equals(KickstartInstallType.RHEL_4) || 
                installType.equals(KickstartInstallType.RHEL_3) ||
                    installType.equals(KickstartInstallType.RHEL_21)) {
            String packageName = this.getKsdefault().getKstree().getBootImage();
            if (!packageName.startsWith(LEGACY_KICKSTART_PACKAGE_NAME)) {
                packageName = LEGACY_KICKSTART_PACKAGE_NAME  + 
                    this.getKsdefault().getKstree().getBootImage();
            }
            return packageName;
        }
        else {
            return KICKSTART_PACKAGE_NAME;
        }
    }

    
    /**
     * @return Returns the repos.
     */
    public Set getRepos() {
        return repos;
    }

    
    /**
     * @param reposIn The repos to set.
     */
    public void setRepos(Set reposIn) {
        this.repos = reposIn;
    }

    
    /**
     * @return Returns the customOptions.
     */
    public SortedSet getCustomOptions() {
        
        return customOptions;
    }

    
    /**
     * @param customOptionsIn The customOptions to set.
     */
    public void setCustomOptions(SortedSet customOptionsIn) {
        this.customOptions = customOptionsIn;
    }

    
    /**
     * @return Returns if the post scripts should be logged.
     */
    public Boolean getPostLog() {
        return postLog;
    }

    /**
     * @return Returns if the pre scripts should be logged.
     */
    public Boolean getPreLog() {
        return preLog;
    }

    /**
     * @return Returns if we should copy ks.cfg and %include'd fragments to /root
     */
    public Boolean getKsCfg() {
        return ksCfg;
    }

    
    /**
     * @param postLogIn The postLog to set.
     */
    public void setPostLog(Boolean postLogIn) {
        this.postLog = postLogIn;
    }

    /**
     * @param preLogIn The preLog to set.
     */
    public void setPreLog(Boolean preLogIn) {
        this.preLog = preLogIn;
    }

    /**
     * @param ksCfgIn The ksCfg to set.
     */
    public void setKsCfg(Boolean ksCfgIn) {
        this.ksCfg = ksCfgIn;
    }

    
    /**
     * Returns the SE Linux mode associated to this kickstart profile
     * @return the se linux mode or the default SE Liunx mode (i.e. enforcing)..
     */
    public SELinuxMode getSELinuxMode() {
        KickstartCommand cmd = getCommand(SELINUX_MODE_COMMAND);
        if (cmd != null) {
            String args = cmd.getArguments();
            if (!StringUtils.isBlank(args)) {
                if (args.endsWith(SELinuxMode.PERMISSIVE.getValue())) {
                    return SELinuxMode.PERMISSIVE;
                }
                else if (args.endsWith(SELinuxMode.ENFORCING.getValue())) {
                    return SELinuxMode.ENFORCING;
                }
                else if (args.endsWith(SELinuxMode.DISABLED.getValue())) {
                    return SELinuxMode.DISABLED;
                }
            }
        }
        return SELinuxMode.ENFORCING;
    }
    
    /**
     * True if config management is enabled in this profile..
     * @return True if config management is enabled in this profile..
     */
    public boolean isConfigManageable() {
        return getKsdefault() != null && getKsdefault().getCfgManagementFlag();
    }
    
    /**
     * True if remote command flag is  enabled in this profile..
     * @return True if remote command flag is  enabled in this profile..
     */
    public boolean isRemoteCommandable() {
        return getKsdefault() != null && getKsdefault().getRemoteCommandFlag();
    }

    /**
     * @return the cobblerName
     */
    public String getCobblerName() {
        return cobblerName;
    }
    
    /**
     * @param cobblerNameIn the cobblerName to set
     */
    public void setCobblerName(String cobblerNameIn) {
        this.cobblerName = cobblerNameIn;
    }    

    /**
     * @return Returns if up2date/yum should be verbose
     */
    public Boolean getVerboseUp2date() {
        return this.verboseUp2date;
    }

    /**
     * @param verboseup2dateIn The verboseup2date to set.
     */
    public void setVerboseUp2date(Boolean verboseup2dateIn) {
        this.verboseUp2date = verboseup2dateIn;
    }

    /**
     * @return Returns if nonchroot post script is to be logged
     */
    public Boolean getNonChrootPost() {
        return this.nonChrootPost;
    }


    /**
     * @param nonchrootpostIn The nonchrootpost to set.
     */
    public void setNonChrootPost(Boolean nonchrootpostIn) {
        this.nonChrootPost = nonchrootpostIn;
    }


}
