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
package com.redhat.rhn.taskomatic.task;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ContentSource;
import com.redhat.rhn.domain.task.Task;
import com.redhat.rhn.domain.task.TaskFactory;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.ArrayList;
import java.util.List;


/**
 * Repo Sync
 *  Used for syncing repos (like yum repos) to a channel
 *  This really just calls a python script
 *
 * @version $Rev$
 */
public class RepoSyncTask extends RhnJavaJob {

    /**
     * Used to log stats in the RHNDAEMONSTATE table
     */
    public static final String DISPLAY_NAME = "repo_sync";

    /**
     * Default constructor
     */
    public RepoSyncTask() {
    }


    /**
     *
     * {@inheritDoc}
     */
    public void execute(JobExecutionContext context)
        throws JobExecutionException {

        for (Task task : TaskFactory.listTasks(DISPLAY_NAME)) {
            //workaround in case task is null (which can happen)

            if (task == null || task.getData() == null) {
                TaskFactory.removeTask(task);
                continue;
            }
            ContentSource src = ChannelFactory.lookupContentSource(task.getData());
            if (log.isInfoEnabled()) {
                log.info("Syncing repo " + src.getSourceUrl() + " to channel " +
                        src.getChannel().getLabel());
            }
            if (src == null) {
                log.error("Content Source could not be found: " + task.getData());
                TaskFactory.removeTask(task);
                continue;
            }
            TaskFactory.removeTask(task);

            executeExtCmd(getSyncCommand(src).toArray(new String[0]));
        }
    }

    private static List<String> getSyncCommand(ContentSource src) {
        List<String> cmd = new ArrayList<String>();
        cmd.add(Config.get().getString(ConfigDefaults.SPACEWALK_REPOSYNC_PATH,
                "/usr/bin/spacewalk-repo-sync"));
        cmd.add("--channel");
        cmd.add(src.getChannel().getLabel());
        cmd.add("--url");
        cmd.add(src.getSourceUrl());
        cmd.add("--type");
        cmd.add(src.getType().getLabel());
        cmd.add("--label");
        cmd.add(src.getLabel());
        cmd.add("--quiet");
        return cmd;
    }
}
