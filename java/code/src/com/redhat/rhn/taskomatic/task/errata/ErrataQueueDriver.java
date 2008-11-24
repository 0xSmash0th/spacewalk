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
package com.redhat.rhn.taskomatic.task.errata;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.taskomatic.task.ErrataQueue;
import com.redhat.rhn.taskomatic.task.TaskConstants;
import com.redhat.rhn.taskomatic.task.threaded.QueueDriver;
import com.redhat.rhn.taskomatic.task.threaded.QueueWorker;

/**
 * Driver for the threaded errata queue
 * @version $Rev$
 */
public class ErrataQueueDriver implements QueueDriver {

    private static final Logger LOG = Logger.getLogger(ErrataQueue.class);    
    
    /**
     * {@inheritDoc}
     */    
    public boolean canContinue() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public List getCandidates() {
        SelectMode select = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_ERRATA_QUEUE_FIND_CANDIDATES);
        Map params = new HashMap();
        List retval = new LinkedList();
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
     * {@inheritDoc}
     */
    public Logger getLogger() {
        return LOG;
    }

    /**
     * {@inheritDoc}
     */    
    public int getMaxWorkers() {
        return Config.get().getInt("taskomatic.errata_queue_workers", 2);
    }

    /**
     * {@inheritDoc}
     */    
    public QueueWorker makeWorker(Object workItem) {
        return new ErrataQueueWorker((Map) workItem, LOG);
    }

}
