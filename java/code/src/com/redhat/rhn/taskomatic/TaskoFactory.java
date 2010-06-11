/**
 * Copyright (c) 2010 Red Hat, Inc.
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
package com.redhat.rhn.taskomatic;


import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * TaskoFactory
 * @version $Rev$
 */
public class TaskoFactory extends HibernateFactory {
    private static TaskoFactory singleton = new TaskoFactory();
    private static Logger log = Logger.getLogger(TaskoFactory.class);

    TaskoFactory() {
        super();
    }

    protected Logger getLogger() {
        return log;
    }

    public static TaskoBunch lookupOrgBunchByName(String bunchName) {
        Map params = new HashMap();
        params.put("name", bunchName);
        return (TaskoBunch) singleton.lookupObjectByNamedQuery(
                                       "TaskoBunch.findOrgBunchByName", params);
    }

    public static TaskoTemplate lookupTemplateByBunchAndOrder(Long bunchId, Long order) {
        Map params = new HashMap();
        params.put("bunch_id", bunchId);
        params.put("order", order);
        return (TaskoTemplate) singleton.lookupObjectByNamedQuery(
                                       "TaskoTemplate.findByBunchAndOrder", params);
    }

    public static List<TaskoBunch> listBunches() {
        return (List) singleton.listObjectsByNamedQuery(
                                       "TaskoBunch.listBunches", null);
    }

    public static void save(TaskoRun taskoRun) {
        singleton.saveObject(taskoRun);
    }

    public static void save(TaskoTemplate taskoTemplate) {
        singleton.saveObject(taskoTemplate);
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException e) {
            log.warn("InterruptedException");
        }
    }

    public static List<TaskoTask> listTasks() {
        return (List<TaskoTask>) singleton.listObjectsByNamedQuery(
                                       "TaskoTask.listTasks", new HashMap());
    }
}
