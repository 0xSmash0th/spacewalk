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
package com.redhat.rhn.taskomatic.task.repomd;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.taskomatic.task.ChannelRepodata;
import com.redhat.rhn.taskomatic.task.TaskConstants;
import com.redhat.rhn.taskomatic.task.threaded.QueueDriver;
import com.redhat.rhn.taskomatic.task.threaded.QueueWorker;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 
 * @version $Rev $
 * 
 */
public class ChannelRepodataDriver implements QueueDriver {

    private static final Logger LOG = Logger.getLogger(ChannelRepodata.class);

    /**
     * default Constructor
     */
    public ChannelRepodataDriver() {
        LOG.info("ChannelRepodataDriver()--resetting orphanned rhnRepoRegenQueue entries");
        WriteMode resetChannelRepodata = ModeFactory.getWriteMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_REPOMOD_CLEAR_IN_PROGRESS);
        try {
            int eqReset = resetChannelRepodata.executeUpdate(new HashMap());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Reset " + eqReset +
                        " rows from the rhnRepoRegenQueue table in progress by " +
                        "setting next_action to sysdate");
            }
            HibernateFactory.commitTransaction();
        }
        catch (Exception e) {
            LOG.error("Error resetting rhnRepoRegenQueue.next_action", e);
            HibernateFactory.rollbackTransaction();
        }
        finally {
            HibernateFactory.closeSession();
        }
    }

    /**
     * @return Returns boolean canContinue
     */
    public boolean canContinue() {
        return true;
    }

    /**
     * @return Returns candidates
     */
    public List getCandidates() {
        SelectMode select = ModeFactory.getMode(TaskConstants.MODE_NAME,
                "repomd_driver_query");

        Map params = new HashMap();
        List<Object> retval = new LinkedList<Object>();
        try {
            List results = select.execute(params);
            if (results != null) {
                for (Iterator iter = results.iterator(); iter.hasNext();) {
                    retval.add(iter.next());
                }
            }
            return retval;
        }
        finally {
            HibernateFactory.closeSession();
        }
    }

    /**
     * @return Returns Logger
     */
    public Logger getLogger() {
        return LOG;
    }

    /**
     * @return Returns max workers
     */
    public int getMaxWorkers() {
        return Config.get().getInt("taskomatic.channel_repodata_workers", 2);
    }

    /**
     * @param workItem work item
     * @return Returns channel repodata worker object
     */
    public QueueWorker makeWorker(Object workItem) {
        return new ChannelRepodataWorker((Map) workItem, getLogger());
    }

}
