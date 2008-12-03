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
package com.redhat.rhn.frontend.xmlrpc.kickstart.tree;


import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartInstallType;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelLabelException;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.kickstart.tree.TreeCreateOperation;
import com.redhat.rhn.manager.kickstart.tree.TreeDeleteOperation;
import com.redhat.rhn.manager.kickstart.tree.TreeEditOperation;

import java.util.List;

/**
 * KickstartTreeHandler - methods related to CRUD operations
 * on KickstartableTree objects.
 * @xmlrpc.namespace kickstart.tree
 * @xmlrpc.doc Provides methods to create kickstart files
 * @version $Rev$
 */
public class KickstartTreeHandler extends BaseHandler {

    /**
     * List the available kickstartable trees for the given channel.
     * @param sessionKey User's session key.
     * @param channelLabel Label of channel to search.
     * @return Array of KickstartableTreeObjects
     * 
     * @xmlrpc.doc List the available kickstartable trees for the given channel.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param_desc("string", "channelLabel", "Label of channel to
     * search.")
     * @xmlrpc.returntype #array() $KickstartTreeSerializer #array_end()
     */
    public List listKickstartableTrees(String sessionKey,
            String channelLabel) {
        User loggedInUser = getLoggedInUser(sessionKey);
        ensureConfigAdmin(loggedInUser);
        
        List<KickstartableTree> ksTrees = KickstartFactory
                .lookupKickstartableTrees(
                        getChannel(channelLabel, loggedInUser).getId(), 
                            loggedInUser.getOrg());
        return ksTrees;
    }

    /**
     * List the available kickstartable tree types (rhel2,3,4,5 and fedora9+)
     * @param sessionKey User's session key.
     * @return Array of KickstartInstallType objects
     * 
     * @xmlrpc.doc List the available kickstartable install types (rhel2,3,4,5 and fedora9+)
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.returntype #array() $KickstartInstallType #array_end()
     */
    public List listKickstartInstallTypes(String sessionKey) {
        return KickstartFactory.lookupKickstartInstallTypes();
    }
    
    /**
     * Create a Kickstart Tree (Distribution) in Satellite
     * 
     * @param sessionKey User's session key.
     * @param treeLabel Label for the new kickstart tree
     * @param basePath path to the base/root of the kickstart tree.
     * @param channelLabel label of channel to associate with ks tree. 
     * @param installType String label for KickstartInstallType (rhel_2.1, 
     * rhel_3, rhel_4, rhel_5, fedora_9)
     * @return 1 if successful, exception otherwise.
     * 
     * @xmlrpc.doc Create a Kickstart Tree (Distribution) in Satellite
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "treeLabel" "Label for the new kickstart tree")
     * @xmlrpc.param #param_desc("string", "basePath", "path to the base or
     * root of the kickstart tree.")
     * @xmlrpc.param #param_desc("string", "channelLabel", "label of channel to 
     * associate with ks tree. ")
     * @xmlrpc.param #param_desc("string", "installType", "String label for 
     * KickstartInstallType (rhel_2.1, rhel_3, rhel_4, rhel_5, fedora_9")
     * @xmlrpc.returntype #return_int_success()
     */
    public int createTree(String sessionKey, String treeLabel,
            String basePath, String channelLabel,
            String installType) {

        User loggedInUser = getLoggedInUser(sessionKey);
        ensureConfigAdmin(loggedInUser);
        
        TreeCreateOperation create = new TreeCreateOperation(loggedInUser);
        create.setBasePath(basePath);
        create.setChannel(getChannel(channelLabel, loggedInUser));
        create.setInstallType(getKickstartInstallType(installType));
        create.setLabel(treeLabel);
        ValidatorError ve = create.store();
        if (ve != null) {
            throw new InvalidKickstartTreeException(ve.getKey());
        }
        return 1;
    }


    /**
     * Delete a kickstarttree
     * kickstartable tree and kickstart host specified.
     * 
     * @param sessionKey User's session key.
     * @param treeLabel Label for the new kickstart tree
     * @return 1 if successful, exception otherwise.
     * 
     * @xmlrpc.doc Delete a Kickstart Tree (Distribution) in Satellite
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "treeLabel" "Label for the
     * kickstart tree you want to delete")
     * @xmlrpc.returntype #return_int_success()
     */
    public int deleteTree(String sessionKey, String treeLabel) {

        User loggedInUser = getLoggedInUser(sessionKey);
        ensureConfigAdmin(loggedInUser);
        
        TreeDeleteOperation op = new TreeDeleteOperation(treeLabel, loggedInUser);
        if (op.getTree() == null) {
            throw new InvalidKickstartTreeException("api.kickstart.tree.notfound");
        }
        ValidatorError ve = op.store();
        if (ve != null) {
            throw new InvalidKickstartTreeException(ve.getKey());
        }
        return 1;
    }

    /**
     * Delete a kickstarttree and any profiles associated with this kickstart tree.
     * WARNING:  This will delete all profiles associated with this kickstart tree!
     * 
     * @param sessionKey User's session key.
     * @param treeLabel Label for the new kickstart tree
     * @return 1 if successful, exception otherwise.
     * 
     * @xmlrpc.doc Delete a kickstarttree and any profiles associated with
     * this kickstart tree.  WARNING:  This will delete all profiles 
     * associated with this kickstart tree!
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "treeLabel" "Label for the
     * kickstart tree you want to delete")
     * @xmlrpc.returntype #return_int_success()
     */
    public int deleteTreeAndProfiles(String sessionKey, String treeLabel) {

        User loggedInUser = getLoggedInUser(sessionKey);
        ensureConfigAdmin(loggedInUser);
        
        TreeDeleteOperation op = new TreeDeleteOperation(treeLabel, loggedInUser);
        if (op.getTree() == null) {
            throw new InvalidKickstartTreeException("api.kickstart.tree.notfound");
        }
        ValidatorError ve = op.store();
        if (ve != null) {
            throw new InvalidKickstartTreeException(ve.getKey());
        }
        return 1;
    }

    /**
     * Edit a kickstarttree.  This method will not edit the label of the tree, see 
     * renameTree().
     * 
     * @param sessionKey User's session key.
     * @param treeLabel Label for the existing kickstart tree
     * @param basePath New basepath for tree.
     * rhn-kickstart.
     * @param channelLabel New channel label to lookup and assign to 
     * the kickstart tree.
     * @param installType String label for KickstartInstallType (rhel_2.1, 
     * rhel_3, rhel_4, rhel_5, fedora_9)
     * 
     * @return 1 if successful, exception otherwise.
     * 
     * @xmlrpc.doc Edit a Kickstart Tree (Distribution) in Satellite
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "treeLabel" "Label for the kickstart tree")
     * @xmlrpc.param #param_desc("string", "basePath", "path to the base or
     * root of the kickstart tree.")
     * @xmlrpc.param #param_desc("string", "channelLabel", "label of channel to 
     * associate with ks tree. ")
     * @xmlrpc.param #param_desc("string", "installType", "String label for 
     * KickstartInstallType (rhel_2.1, rhel_3, rhel_4, rhel_5, fedora_9")
     *
     * @xmlrpc.returntype #return_int_success()
     */
    public int editTree(String sessionKey, String treeLabel, String basePath,
                 String channelLabel, String installType) {

        
        User loggedInUser = getLoggedInUser(sessionKey);
        ensureConfigAdmin(loggedInUser);
        
        TreeEditOperation op = new TreeEditOperation(treeLabel, loggedInUser);
        if (op.getTree() == null) {
            throw new InvalidKickstartTreeException("api.kickstart.tree.notfound");
        }
        op.setBasePath(basePath);
        op.setChannel(getChannel(channelLabel, loggedInUser));
        op.setInstallType(getKickstartInstallType(installType));
        
        ValidatorError ve = op.store();
        if (ve != null) {
            throw new InvalidKickstartTreeException(ve.getKey());
        }
        return 1;
    }
    
    /**
     * Rename a kickstart tree.
     * 
     * @param sessionKey User's session key.
     * @param originalLabel Label for tree we want to edit
     * @param newLabel to assign to tree.
     * @return 1 if successful, exception otherwise.
     * 
     * @xmlrpc.doc Rename a Kickstart Tree (Distribution) in Satellite
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "originalLabel" "Label for the
     * kickstart tree you want to rename")
     * @xmlrpc.param #param_desc("string", "newLabel" "new label to change too")
     * @xmlrpc.returntype #return_int_success()
     */
    public int renameTree(String sessionKey, String originalLabel, String newLabel) {

        User loggedInUser = getLoggedInUser(sessionKey);
        ensureConfigAdmin(loggedInUser);
        
        TreeEditOperation op = new TreeEditOperation(originalLabel, loggedInUser);
        
        if (op.getTree() == null) {
            throw new InvalidKickstartTreeException("api.kickstart.tree.notfound");
        }
        op.setLabel(newLabel);
        ValidatorError ve = op.store();
        if (ve != null) {
            throw new InvalidKickstartTreeException(ve.getKey());
        }
        return 1;
    }

    private Channel getChannel(String label, User user) {
        Channel channel = ChannelManager.lookupByLabelAndUser(label,
                user);
        if (channel == null) {
            throw new InvalidChannelLabelException();
        }
        return channel;
    }
    
    private KickstartInstallType getKickstartInstallType(String installType) {
        KickstartInstallType type = 
            KickstartFactory.lookupKickstartInstallTypeByLabel(installType);
        if (type == null) {
            throw new NoSuchKickstartInstallTypeException(installType);
        }
        return type;

    }

}
