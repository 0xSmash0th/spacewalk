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
package com.redhat.rhn.domain.channel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;

import com.redhat.rhn.common.db.datasource.CallableMode;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.HibernateRuntimeException;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.channel.ChannelManager;

/**
 * ChannelFactory
 * @version $Rev$
 */
public class ChannelFactory extends HibernateFactory {
    
    private static ChannelFactory singleton = new ChannelFactory();
    private static Logger log = Logger.getLogger(ChannelFactory.class);
    
    private ChannelFactory() {
        super();
    }
    
    /**
     * Get the Logger for the derived class so log messages
     * show up on the correct class
     */
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Lookup a Channel by its id
     * @param id the id to search for
     * @return the Channel found
     */
    public static Channel lookupById(Long id) {
        Session session = null;
        try {
            session = HibernateFactory.getSession();
            Channel c = (Channel) session.get(Channel.class, id);
            return c;
        }
        catch (HibernateException he) {
            log.error("Hibernate exception: " + he.toString());
        }
        return null;
    }
    
    /**
     * Lookup a Channel by id and User
     * @param id the id to search for
     * @param userIn User who is doing the looking
     * @return the Server found (null if not or not member if userIn)
     */
    public static Channel lookupByIdAndUser(Long id, User userIn) {
        Map params = new HashMap();
        params.put("cid", id);
        params.put("userId", userIn.getId());
        return (Channel) singleton.lookupObjectByNamedQuery(
                                       "Channel.findByIdAndUserId", params);
    }

    /**
     * Lookup a Channel by label and User
     * @param label the label to search for
     * @param userIn User who is doing the looking
     * @return the Server found (null if not or not member if userIn)
     */
    public static Channel lookupByLabelAndUser(String label, User userIn) {
        Map params = new HashMap();
        params.put("label", label);
        params.put("userId", userIn.getId());
        return (Channel) singleton.lookupObjectByNamedQuery(
                                       "Channel.findByLabelAndUserId", params);
    }    
    
    
    /**
     * Insert or Update a Channel.
     * @param c Channel to be stored in database.
     */
    public static void save(Channel c) {
        singleton.saveObject(c);
    }

    /**
     * Remove a Channel from the DB
     * @param c Action to be removed from database.
     */
    public static void remove(Channel c) {
        // When we change delete_channel to return the number of rows
        // affected, we can delete all of the CallableMode code below
        // and simply use singleton.removeObject(c); Until then I'm
        // using DataSource.  I must say that working with existing
        // schema, while a reality in most software projects, SUCKS!
        
        CallableMode m = ModeFactory.getCallableMode(
                "Channel_queries", "delete_channel");
        Map inParams = new HashMap();
        inParams.put("cid", c.getId());
        
        m.execute(inParams, new HashMap());
    }
    
    /**
     * Returns the base channel for the given server id.
     * @param sid Server id whose base channel we want.
     * @return Base Channel for the given server id.
     */
    public static Channel getBaseChannel(Long sid) {
        Map params = new HashMap();
        params.put("sid", sid);
        return (Channel) singleton.lookupObjectByNamedQuery(
                "Channel.findBaseChannel", params);
    }
    
    /**
     * Returns a list of Channels which have clonable errata.
     * @param org Org.
     * @return List of com.redhat.rhn.domain.Channel objects which have
     * clonable errata.
     */
    public static List getChannelsWithClonableErrata(Org org) {
        Map params = new HashMap();
        params.put("org", org);
        return singleton.listObjectsByNamedQuery(
                "Channel.channelsWithClonableErrata", params, false);
    }
    
    /**
     * Returns the list of Channel ids which the given orgid has access to.
     * @param orgid Org id
     * @param cid Base Channel id.
     * @return the list of Channel ids which the given orgid has access to.
     */
    public static List getUserAcessibleChannels(Long orgid, Long cid) {
        Map params = new HashMap();
        params.put("org_id", orgid);
        params.put("cid", cid);
        return singleton.listObjectsByNamedQuery(
                "Channel.accessibleChildChannelIds", params);
    }
    
    /**
     * Returns the accessible child channels associated to a base channel.
     * @param baseChannel the base channel who's child channels are needed
     * @param user the user requesting the info.. (has to be globally subscribed etc.)
     * @return the accessible child channels.. 
     */
    public static List<Channel> getAccessibleChildChannels(Channel baseChannel,
                                                                    User user) {
        Map params = new HashMap();
        params.put("userId", user.getId());
        params.put("cid", baseChannel.getId());
        return singleton.listObjectsByNamedQuery(
                "Channel.accessibleChildChannels", params);        
    }
    
    /**
     * Returns the list of Channels accessible by an org
     * Channels are accessible if they are owned by an org or public.
     * @param orgid The id for the org
     * @return A list of Channel Objects.
     */
    public static List getAccessibleChannelsByOrg(Long orgid) {
        Map params = new HashMap();
        params.put("org_id", orgid);
        return singleton.listObjectsByNamedQuery("Org.accessibleChannels", params);
    }
    
    /**
     * Returns a list of Channels matching the given labels and a child of the
     * given baseChannel.
     * @param baseChannel Parent channel whose children are sought.
     * @param labels Channel labels being searched.
     * @return List of channels matching the criteria, or empty list.
     */
    public static List getChildChannelsByLabels(Channel baseChannel, List labels) {
        Session session = getSession();
        Criteria c = session.createCriteria(Channel.class);
        c.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        c.add(Restrictions.eq("parentChannel.id", baseChannel.getId()));
        c.add(Restrictions.in("label", labels));
        return c.list();
    }

    
    /**
     * Returns list of channel architectures
     * @return list of channel architectures
     */
    public static List<ChannelArch> getChannelArchitectures() {
        Session session = getSession();
        Criteria criteria = session.createCriteria(ChannelArch.class);
        return criteria.list();
    }
    
    /**
     * returns a ChannelArch by label
     * @param label ChannelArch label
     * @return a ChannelArch by label
     */
    public static ChannelArch findArchByLabel(String label) {
        Session session = getSession();
        Criteria criteria = session.createCriteria(ChannelArch.class);
        criteria.add(Restrictions.eq("label", label));
        return (ChannelArch) criteria.uniqueResult();       
    }
    
    /**
     * Returns the Channel whose label matches the given label.
     * @param org The org of the user looking up the channel
     * @param label Channel label sought.
     * @return the Channel whose label matches the given label.
     */
    public static Channel lookupByLabel(Org org, String label) {
        Session session = getSession();
        Criteria c = session.createCriteria(Channel.class);
        c.add(Restrictions.eq("label", label));
        c.add(Restrictions.or(Restrictions.eq("org", org), 
                            Restrictions.isNull("org")));
        return (Channel) c.uniqueResult();
    }
    
    /**
     * Returns true if the given channel is globally subscribable for the
     * given org.
     * @param org Org
     * @param c Channel to validate.
     * @return true if the given channel is globally subscribable for the
     */
    public static boolean isGloballySubscribable(Org org, Channel c) {
        SelectMode mode = ModeFactory.getMode(
                "Channel_queries", "is_not_globally_subscribable");
        Map params = new HashMap();
        params.put("org_id", org.getId());
        params.put("cid", c.getId());
        params.put("label", "not_globally_subscribable");
        
        DataResult dr = mode.execute(params);
        // if the query returns something that means that this channel
        // is NOT globally subscribable by the org.  Which means the DataResult
        // will have a value in it.  If the channel IS globally subscribable
        // the DataResult will be empty (true);
        return dr.isEmpty();
    }
    
    /**
     * Set the globally subscribable attribute for a given channel
     * @param org The org containing the channel
     * @param channel The channel in question
     * @param value True to make the channel globally subscribable, false to make it not
     * globally subscribable.
     */
    public static void setGloballySubscribable(Org org, Channel channel, boolean value) {
        //we need to check here, otherwise if we try to remove and it's already removed
        //  the db throws a violation
        if (value == channel.isGloballySubscribable(org)) {
            return;
        }
        
        /*
         *  this is some bass-ackwards logic...
         *  if value == true, remove the 'not_globally_subscribable' setting
         *  if value == false, add the 'not_globally_subscribable' setting
         */
        if (value) {
            removeOrgChannelSetting(org, channel, "not_globally_subscribable");
        }
        else {
            addOrgChannelSetting(org, channel, "not_globally_subscribable");
        }
    }
    
    /**
     * Remove an org-channel setting
     * @param org The org in question
     * @param channel The channel in question
     * @param label the label of the setting to remove
     */
    private static void removeOrgChannelSetting(Org org, Channel channel, String label) {
        WriteMode m = ModeFactory.getWriteMode("Channel_queries", 
                                      "remove_org_channel_setting");
        Map params = new HashMap();
        params.put("org_id", org.getId());
        params.put("cid", channel.getId());
        params.put("label", label);
        m.executeUpdate(params);
    }
    
    /**
     * Adds an org-channel setting
     * @param org The org in question
     * @param channel The channel in question
     * @param label the label of the setting to add
     */
    private static void addOrgChannelSetting(Org org, Channel channel, String label) {
        WriteMode m = ModeFactory.getWriteMode("Channel_queries", 
                                      "add_org_channel_setting");
        Map params = new HashMap();
        params.put("org_id", org.getId());
        params.put("cid", channel.getId());
        params.put("label", label);
        m.executeUpdate(params);
    }
    
    /**
     * 
     * @param cid Channel package is being added to
     * @param pid Package id from rhnPackage
     */
    public static void addChannelPackage(Long cid, Long pid) {
        WriteMode m = ModeFactory.getWriteMode("Channel_queries", 
        "add_channel_package");
        Map params = new HashMap();        
        params.put("cid", cid);
        params.put("pid", pid);
        m.executeUpdate(params);        
    }
    
    /**
     * Returns available entitlements for the org and the given channel.
     * @param org Org (used <b>only</b> when channel's org is NULL)
     * @param c Channel
     * @return available entitlements for the org and the given channel.
     */
    public static Long getAvailableEntitlements(Org org, Channel c) {
        //
        // The channel's org is used when not NULL to support
        // shared channels.
        //
        Org channelOrg = c.getOrg();
        if (channelOrg != null) {
            org = channelOrg;
        }
        Map params = new HashMap();
        params.put("channel_id", c.getId());
        params.put("org_id", org.getId());
        return (Long) singleton.lookupObjectByNamedQuery(
                "Channel.availableEntitlements", params);

    }

    /**
     * Creates an empty Channel
     * @return empty Channel
     */
    public static Channel createChannel() {
        return new Channel();
    }

    /**
     * Refreshes the channel with the "newest" packages.  Newest isn't just
     * the latest versions, an errata could have obsoleted a package in which
     * case this would have removed said package from the channel.
     * @param c Channel to be refreshed.
     * @param label the label.
     */
    public static void refreshNewestPackageCache(Channel c, String label) {
        CallableMode m = ModeFactory.getCallableMode(
                "Channel_queries", "refresh_newest_package");
        Map inParams = new HashMap();
        inParams.put("cid", c.getId());
        inParams.put("label", label);
        
        m.execute(inParams, new HashMap());
    }
    
    /**
     * Returns true if the given label is in use.
     * @param label Label 
     * @return true if the given label is in use.
     */
    public static boolean doesChannelLabelExist(String label) {
        Map params = new HashMap();
        params.put("label", label);
        Object o = singleton.lookupObjectByNamedQuery(
                "Channel.verifyLabel", params, false);
        return (o != null);
    }
    
    /**
     * 
     * @param cIn Channel coming in
     * @param userIn User coming in
     * @param pkgIn Package Name coming in
     * @return Package object for latest in channel
     */
    public static Package lookupLatestPackage(Channel cIn, User userIn, String pkgIn) {
        Session session = null;
        List retval;
        try {            
            session = HibernateFactory.getSession();
            retval =  session.getNamedQuery("Channel.latestPackage")
                                          .setString("package_name", pkgIn)
                                          .setLong("user_id", userIn.getId().longValue())
                                          .setLong("channel_id", cIn.getId().longValue())
                                          .list();
        }
        catch (HibernateException e) {
            log.error(e);
            throw new 
                HibernateRuntimeException("Error looking up latest package in channel");
        }
                
        if (retval.size() > 0) {
            return (Package)retval.get(0);
        }
        else {            
            return null;
        }
    }
    
    /**
     * Returns true if the given name is in use.
     * @param name name
     * @return true if the given name is in use.
     */
    public static boolean doesChannelNameExist(String name) {
        Map params = new HashMap();
        params.put("name", name);
        Object o = singleton.lookupObjectByNamedQuery(
                "Channel.verifyName", params, false);
        return (o != null);
    }

    /**
     * Get the List of Channel's that are kickstartable to the
     * Org passed in.
     * @param org who you want to get kickstartable channels
     * @return List of Channel objects
     */
    public static List<Channel> getKickstartableChannels(Org org) {
        Map params = new HashMap();
        params.put("org_id", org.getId());
        return singleton.listObjectsByNamedQuery(
                "Channel.kickstartableChannels", params, false);
    }
    
    /**
     * Get a list of base channels that have an org associated
     * @param   org the org 
     * @return List of Channels
     */
    public static List<Channel> listCustomBaseChannels(Org org) {
        Map params = new HashMap();
        params.put("org", org);
        return singleton.listObjectsByNamedQuery(
                "Channel.findCustomBaseChannels", params);
    }
    
    
    
    /**
     * Find all trees using a given channel
     * @param channel channel
     * @return List of KickstartableTrees instances
     */
    public static List getTreesForChannel(Channel channel) {
        Map params = new HashMap();
        params.put("channel_id", channel.getId());
        return singleton.listObjectsByNamedQuery(
                "KickstartableTree.findTreesForChannel", params);
    }
    
    /**
     * Lookup a ChannelArch based on its name
     * @param name arch name
     * @return ChannelArch if found, otherwise null
     */
    public static ChannelArch lookupArchByName(String name) {
        Map params = new HashMap();
        params.put("name", name);
        return (ChannelArch)  
            singleton.lookupObjectByNamedQuery("ChannelArch.findByName", params);
    }
    
    /**
     * Lookup list of server ids associated with this channel.
     * @param cid Channel id
     * @return List of server ids associated with this channel.
     */
    public static List getServerIds(Long cid) {
        if (cid == null) {
            return Collections.EMPTY_LIST;
        }
        Map params = new HashMap();
        params.put("cid", cid);
        return singleton.listObjectsByNamedQuery("Channel.getServerIds", params);
    }
       
    /**
     * Looksup the number of Packages in a channel 
     * @param channel the Channel who's package count you are interested in. 
     * @return number of packages in this channel.
     */
    public static int getPackageCount(Channel channel) {
        Map params = new HashMap();
        params.put("cid", channel.getId());
        return (Integer)singleton.lookupObjectByNamedQuery
                                ("Channel.getPackageCount", params);
    }    
    /**
     * Find the original packages that were part of a channel.  This list 
     *      includes only those packages that have not had errata released for them.
     * @param channel the channel to clone from
     * @param org the org doing the cloning. 
     * @return List of packages
     */
    public static List findOriginalPackages(Channel channel, Org org) {

            Map params = new HashMap();
            params.put("from_cid", channel.getId());
            params.put("org_id", org.getId());
            List idList = singleton.listObjectsByNamedQuery(
                    "Channel.lookupOriginalPackages", params);
            return idList;
    }
    
    /**
     * Lookup the default release channel map for the given channel. Returns null if no 
     * default is found.
     * 
     * @param channel Channel to lookup mapping for
     * @return Default ReleaseChannelMap
     */
    public static ReleaseChannelMap lookupDefaultReleaseChannelMapForChannel(
            Channel channel) {
        
        Map params = new HashMap();
        params.put("channel", channel);
        return (ReleaseChannelMap)singleton.lookupObjectByNamedQuery(
                "ReleaseChannelMap.findDefaultForChannel", params);
    }
    
    /**
     * Lookup the dist channel map for the given os, release, and channel arch. 
     * Returns null if none is found.
     * 
     * @param os Product name.
     * @param release Version.
     * @param channelArch Channel arch.
     * @return DistChannelMap, null if none is found
     */
    public static DistChannelMap lookupDistChannelMap(String os, String release, 
            ChannelArch channelArch) {
        
        Map params = new HashMap();
        params.put("os", os);
        params.put("release", release);
        params.put("channelArch", channelArch);
        return (DistChannelMap)singleton.lookupObjectByNamedQuery(
                "DistChannelMap.findByOsReleaseAndChannelArch", params);
    }
    
    /**
     * Lookup the dist channel map for the given channel. 
     * Returns null if none is found.
     * 
     * @param c Channel to lookup mapping for
     * @return DistChannelMap, null if none is found
     */
    public static DistChannelMap lookupDistChannelMap(Channel c) {
        
        Map params = new HashMap();
        params.put("channel", c);
        return (DistChannelMap)singleton.lookupObjectByNamedQuery(
                "DistChannelMap.findByChannel", params);
    }
    
    /**
     * Get a list of channels with no org that are not a child
     * @return List of Channels
     */
    public static List<Channel> listRedHatBaseChannels() {
        Map params = new HashMap();
        return singleton.listObjectsByNamedQuery("Channel.findRedHatBaseChannels", params);
    }
    

    /**
     * Lookup a List of redhat base channels with a given ChannelVersion
     * @param version the version string to find
     * @return The List of Channels
     */
    public static List<Channel> listRedHatBaseChannelsByVersion(ChannelVersion version) {
        List<Channel> toReturn = new ArrayList();
        List<Channel> channels = listRedHatBaseChannels();
        for (Channel chan : channels) {
            Set versions = ChannelManager.getChannelVersions(chan);
            if (versions.contains(version)) {
                toReturn.add(chan);
            }
        }
        return toReturn;
    }
    
    /**
     * Lookup the original channel of a cloned channel
     * @param chan the channel to find the original of
     * @return The channel that was cloned, null if none
     */
    public static Channel lookupOriginalChannel(Channel chan) {
        Map params = new HashMap();
        params.put("clone", chan);
        return (Channel)singleton.lookupObjectByNamedQuery(
                "Channel.lookupOriginal", params);
    }

    /**
     * Lookup a product name by label.
     *
     * @param label Product name label to search for.
     * @return Product name if found, null otherwise.
     */
    public static ProductName lookupProductNameByLabel(String label) {
        Map params = new HashMap();
        params.put("label", label);
        return (ProductName)singleton.lookupObjectByNamedQuery(
                "ProductName.findByLabel", params);
    }
    
    /**
     * Returns a distinct list of ChannelArch labels for all synch'd and custom
     * channels in the satellite. 
     * @return a distinct list of ChannelArch labels for all synch'd and custom
     * channels in the satellite. 
     */
    public static List<String> findChannelArchLabelsSyncdChannels() {
        return singleton.listObjectsByNamedQuery(
                "Channel.findChannelArchLabelsSyncdChannels", null);
    }

    /**
     * List custom channels for an org
     * @param org the org doing the searching
     * @return list of custom channels
     */
    public static List<Channel> listCustomChannels(Org org) {
        Map params = new HashMap();
        params.put("org", org);
        return singleton.listObjectsByNamedQuery(
                "Channel.listCustomChannels", params);
    }
    
    /**
     * List all accessible base channels for an org
     * @param org the org doing the searching
     * @return list of custom channels
     */
    public static List<Channel> listAllBaseChannels(Org org) {
        Map params = new HashMap();
        params.put("org", org);
        return singleton.listObjectsByNamedQuery(
                "Channel.findAllBaseChannels", params);
    }
    
    /**
     * List all accessible base channels for the entire satellite
     * @return list of base channels
     */
    public static List<Channel> listAllBaseChannels() {
        Map params = new HashMap();
        return singleton.listObjectsByNamedQuery(
                "Channel.findAllBaseChannelsOnSatellite", params);
    }
    

    /**
     * List all child channels of the given parent regardless of the user
     * @param parent the parent channel
     * @return list of children of the parent 
     */
    public static List<Channel> listAllChildrenForChannel(Channel parent) {
        Map params = new HashMap();
        params.put("parent", parent);
        return singleton.listObjectsByNamedQuery(
                "Channel.listAllChildren", params);
    }
}
