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
package com.redhat.rhn.taskomatic;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.taskomatic.TaskoBunch;
import com.redhat.rhn.taskomatic.task.TaskConstants;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

    public static TaskoBunch lookupByName(String bunchName) {
        Map params = new HashMap();
        params.put("name", bunchName);
        return (TaskoBunch) singleton.lookupObjectByNamedQuery(
                                       "TaskoBunch.findByName", params);
    }

    public static List<TaskoBunch> listBunches() {
        return (List) singleton.listObjectsByNamedQuery(
                                       "TaskoBunch.listBunches", null);
    }

    public static Class traslateTaskNameToClass(String taskName) {
        Map params = new HashMap();
        params.put("task_name", taskName);
        SelectMode select = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_TASK_NAME_TRANSLATE);

        DataResult result = select.execute(params);
        if (!result.isEmpty()) {
            Map row = (Map) result.get(0);
            String taskClass = (String) row.get("class");
            try {
                Class clazz = Class.forName(taskClass);
                return clazz;
            }
            catch (ClassNotFoundException e) {
                return null;
            }
        }

        return null;
    }
/*
    public static List<Map> getTaskList(String bunchName) {
        Map params = new HashMap();
        params.put("bunch_name", bunchName);
        SelectMode select = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_GET_BUNCH_TASKS);

        DataResult result = select.execute(params);
        List<Map> list = new ArrayList();
        for (Iterator iter = result.iterator(); iter.hasNext();) {
            Map job = (Map) iter.next();
            list.add(job);
        }
        return list;
    }
*/
}
