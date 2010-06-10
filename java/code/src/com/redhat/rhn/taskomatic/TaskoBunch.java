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

import com.redhat.rhn.taskomatic.core.SchedulerKernel;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TaskoBunch
 * @version $Rev$
 */
public class TaskoBunch implements Job {

    private static Logger log = Logger.getLogger(TaskoBunch.class);
    private static Map<String, Integer> tasks = new HashMap();
    private Long id;
    private String name;
    private String description;
    private String orgTask;
    private Date activeFrom;
    private Date activeTill;
    private String orgBunch;
    private List<TaskoTemplate> templates = new ArrayList();
    private Date created;
    private Date modified;

    static {
        for (TaskoTask task : TaskoFactory.listTasks()) {
            tasks.put(task.getName(), 0);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void execute(JobExecutionContext context)
        throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Integer orgId = dataMap.getInt("org_id");
        String jobLabel = dataMap.getString("job_label");
        TaskoRun previousRun = null;

        log.info("Starting " + this.name + " (" + jobLabel + ") at " + new Date());

        for (TaskoTemplate template : this.templates) {
            if ((previousRun == null)
                    || (previousRun.getStatus() == template.getStartIf())) {

                while (isTaskRunning(template.getTask())) {
                    log.info("Task " + template.getTask().getName() + " currently executing. Sleeping for 10 secs.");
                    TaskoFactory.sleep(10000);
                }
                markTaskRunning(template.getTask());
                TaskoRun taskRun = new TaskoRun(orgId, template, jobLabel);
                taskRun.execute(context);
                unmarkTaskRunning(template.getTask());
                log.debug(template.getTask().getName() + " ... " + taskRun.getStatus());
                previousRun = taskRun;
            }
            else {
                log.info("Interrupting " + this.name + " (" + jobLabel + ")");
                break;
            }
        }
        TaskoFactory.commitTransaction();

        log.info("Finishing " + this.name + " (" + jobLabel + ") at " + new Date());
    }

    private boolean isTaskRunning(TaskoTask task) {
        return tasks.get(task.getName()) > 0;
    }

    private void markTaskRunning(TaskoTask task) {
        synchronized (getClass()) {
            int count = tasks.get(task.getName());
            count ++;
            tasks.put(task.getName(), count);
        }
    }

    private void unmarkTaskRunning(TaskoTask task) {
        synchronized (getClass()) {
            int count = tasks.get(task.getName());
            count --;
            tasks.put(task.getName(), count);
        }
    }

    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id The id to set.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the activeFrom.
     */
    public Date getActiveFrom() {
        return activeFrom;
    }

    /**
     * @param activeFrom The activeFrom to set.
     */
    public void setActiveFrom(Date activeFrom) {
        this.activeFrom = activeFrom;
    }

    /**
     * @return Returns the activeTill.
     */
    public Date getActiveTill() {
        return activeTill;
    }

    /**
     * @param activeTill The activeTill to set.
     */
    public void setActiveTill(Date activeTill) {
        this.activeTill = activeTill;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return Returns the created.
     */
    public Date getCreated() {
        return created;
    }

    /**
     * @param created The created to set.
     */
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * @return Returns the modified.
     */
    public Date getModified() {
        return modified;
    }

    /**
     * @param modified The modified to set.
     */
    public void setModified(Date modified) {
        this.modified = modified;
    }

    /**
     * @return Returns the templates.
     */
    public List<TaskoTemplate> getTemplates() {
        return templates;
    }

    /**
     * @param templates The templates to set.
     */
    public void setTemplates(List<TaskoTemplate> templates) {
        this.templates = templates;
    }


    /**
     * @return Returns the orgTask.
     */
    public String getOrgTask() {
        return orgTask;
    }


    /**
     * @param orgTask The orgTask to set.
     */
    public void setOrgTask(String orgTask) {
        this.orgTask = orgTask;
    }

    /**
     * @return Returns the orgBunch.
     */
    public String getOrgBunch() {
        return orgBunch;
    }

    /**
     * @param orgBunch The orgBunch to set.
     */
    public void setOrgBunch(String orgBunch) {
        this.orgBunch = orgBunch;
    }
}
