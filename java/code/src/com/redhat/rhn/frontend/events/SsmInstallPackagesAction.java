/**
 * Copyright (c) 2009 Red Hat, Inc.
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
package com.redhat.rhn.frontend.events;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Date;
import java.util.LinkedList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.manager.ssm.SsmOperationManager;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.frontend.dto.EssentialServerDto;
import com.redhat.rhn.frontend.action.SetLabels;

/**
 * Schedules package installations on systems in the SSM. 
 */
public class SsmInstallPackagesAction extends AbstractDatabaseAction {

    private final Log log = LogFactory.getLog(this.getClass());

    protected void doExecute(EventMessage msg) {
        SsmInstallPackagesEvent event = (SsmInstallPackagesEvent) msg;

        User user = event.getUser();

        // Log the action has been created
        LocalizationService ls = LocalizationService.getInstance();
        String operationMessage = ls.getMessage("ssm.package.install.operationname");
        long operationId =
            SsmOperationManager.createOperation(user,
                operationMessage, RhnSetDecl.SYSTEMS.getLabel());

        try {
            scheduleInstalls(event);
        }
        catch (Exception e) {
            log.error("Error scheduling package installations for event " + event, e);
        }
        finally {
            // This should stay in the finally block so the operation is
            // not perpetually left in an in progress state
            SsmOperationManager.completeOperation(user, operationId);
        }
    }

    private void scheduleInstalls(SsmInstallPackagesEvent event) {

        User user = event.getUser();
        Date earliest = event.getEarliest();
        Set<String> data = event.getPackages();
        Long channelId = event.getChannelId();

        List<EssentialServerDto> servers =
            SystemManager.systemsSubscribedToChannelInSet(channelId, user,
                SetLabels.SYSTEM_LIST);

        // Convert the package list to domain objects
        List<PackageListItem> packageListItems =
            new ArrayList<PackageListItem>(data.size());
        for (String key : data) {
            packageListItems.add(PackageListItem.parse(key));
        }

        // Convert to list of maps
        List<Map<String, Long>> packageListData =
            PackageListItem.toKeyMaps(packageListItems);

        // Create one action for all servers to which the packages are installed
        List<Server> actionServers = new ArrayList<Server>(servers.size());
        List<Long> serverIds = new LinkedList<Long>();
        for (EssentialServerDto dto : servers) {
            serverIds.add(dto.getId());
        }
        actionServers.addAll(ServerFactory.lookupByIdsAndUser(serverIds, user));

        ActionManager.schedulePackageInstall(user, actionServers,
            packageListData, earliest);
        
    }
}
