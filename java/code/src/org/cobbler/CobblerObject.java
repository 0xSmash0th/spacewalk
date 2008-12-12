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

package org.cobbler;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Base class has attributes common to 
 * distros, profiles, system records
 * @author paji
 * @version $Rev$
 */
public abstract class CobblerObject {
    protected static final String COMMENT = "comment";
    protected static final String OWNERS = "owners";
    protected static final String CTIME = "ctime";
    protected static final String KERNEL_OPTIONS_POST = "kernel_options_post";
    protected static final String SET_KERNEL_OPTIONS_POST = "kopts-post";
    protected static final String DEPTH = "depth";
    protected static final String KERNEL_OPTIONS = "kernel_options";
    protected static final String SET_KERNEL_OPTIONS = "kopts";
    protected static final String NAME = "name";
    protected static final String KS_META = "ks_meta";
    protected static final String SET_KS_META = "ksmeta";
    protected static final String PARENT = "parent";
    protected static final String MTIME = "mtime";
    protected static final String MGMT_CLASSES = "mgmt_classes";
    protected static final String TEMPLATE_FILES = "template_files";
    protected static final String UID = "uid";
    
    protected String handle;
    protected Map<String, Object> dataMap = new HashMap<String, Object>();
    protected CobblerConnection client;    

    protected abstract void invokeModify(String key, Object value);
    protected abstract void invokeSave();
    protected abstract void invokeRemove();
    protected abstract String invokeGetHandle();
    protected abstract void reload();
    protected abstract void invokeRename(String newName);
    
    protected String getHandle() {
        if (isBlank(handle)) {
            handle = invokeGetHandle();
        }
        return handle;
    }
    
    protected void modify(String key, Object value) {
        invokeModify(key, value);
        dataMap.put(key, value);
    }
    
    /**
     * calls save object to complete the commit
     */
    public void save() {
        invokeSave();
        update();
    }

    /**
     * removes the kickstart object from cobbler.
     */
    public void remove() {
        invokeRemove();
        client.invokeTokenMethod("remove_profile", getName());
    }
    
    
    /**
     * @return the comment
     */
    public String getComment() {
        return (String)dataMap.get(COMMENT);
    }

    
    /**
     * @param commentIn the comment to set
     */
    public void setComment(String commentIn) {
        modify(COMMENT, commentIn);
    }
    
    /**
     * @return the managementClasses
     */
    public List<String> getManagementClasses() {
        return (List<String>)dataMap.get(MGMT_CLASSES);
    }

    
    /**
     * @param managementClassesIn the managementClasses to set
     */
    public void setManagementClasses(List<String> managementClassesIn) {
        modify(MGMT_CLASSES, managementClassesIn);
    }

    
    /**
     * @return the templateFiles
     */
    public Map<String, String> getTemplateFiles() {
        return (Map<String, String>)dataMap.get(TEMPLATE_FILES);
    }

    
    /**
     * @param templateFilesIn the templateFiles to set
     */
    public void setTemplateFiles(Map<String, String> templateFilesIn) {
        modify(TEMPLATE_FILES, templateFilesIn);
    }

    
    /**
     * @return the uid
     */
    public String getUid() {
        return (String)dataMap.get(UID);
    }

    /**
     * @return the uid
     */
    public String getId() {
        return getUid();
    }
    
    /**
     * @param uidIn the uid to set
     */
    public void setUid(String uidIn) {
        modify(UID, uidIn);
    }

    
    /**
     * @return the parent
     */
    public String getParent() {
        return (String)dataMap.get(PARENT);
    }

    
    /**
     * @param parentIn the parent to set
     */
    public void setParent(String parentIn) {
        modify(PARENT, parentIn);
    }
    
    /**
     * @return the owners
     */
    public List<String> getOwners() {
        return (List<String>)dataMap.get(OWNERS);
    }

    
    /**
     * @param ownersIn the owners to set
     */
    public void setOwners(List<String> ownersIn) {
        modify(OWNERS, ownersIn);
    }
    
    /**
     * @return the created
     */
    public Date getCreated() {
        Double time = (Double)dataMap.get(CTIME);
        return new Date(time.longValue());        
    }
    
    /**
     * @param createdIn the created to set
     */
    public void setCreated(Date createdIn) {
        modify(CTIME, createdIn.getTime());
    }
    
    /**
     * @return the modified
     */
    public Date getModified() {
        Double time = (Double)dataMap.get(MTIME);
        return new Date(time.longValue());
    }
    
    /**
     * @param modifiedIn the modified to set
     */
    public void setModified(Date modifiedIn) {
        modify(MTIME, modifiedIn.getTime());
    }

    /**
     * @return the depth
     */
    public int getDepth() {
        return (Integer)dataMap.get(DEPTH);
    }
    
    /**
     * @param depthIn the depth to set
     */
    public void setDepth(int depthIn) {
        modify(DEPTH, depthIn);
    }

    
    /**
     * @return the kernelOptions
     */
    public Map<String, String> getKernelOptions() {
        return (Map<String, String>)dataMap.get(KERNEL_OPTIONS);
    }

    
    /**
     * @param kernelOptionsIn the kernelOptions to set
     */
    public void setKernelOptions(Map<String, Object> kernelOptionsIn) {
        modify(SET_KERNEL_OPTIONS, kernelOptionsIn);
    }

    
    /**
     * @return the kernelMeta
     */
    public Map<String, String> getKsMeta() {
        return (Map<String, String>)dataMap.get(KS_META);
    }

    
    /**
     * @param kernelMetaIn the kernelMeta to set
     */
    public void setKsMeta(Map<String, String> kernelMetaIn) {
        modify(SET_KS_META, kernelMetaIn);
    }

    
    /**
     * @return the name
     */
    public String getName() {
        return (String)dataMap.get(NAME);
    }

    /**
     * @param nameIn sets the new name
     */
    public void setName(String nameIn) {
        invokeRename(nameIn);
        client.invokeTokenMethod("update", getHandle(), nameIn);
        dataMap.put(NAME, nameIn);
        reload();
    }
    
    
    /**
     * @return the kernelPostOptions
     */
    public Map<String, String> getKernelPostOptions() {
        return (Map<String, String>)dataMap.get(KERNEL_OPTIONS_POST);
    }

    
    /**
     * @param kernelPostOptionsIn the kernelPostOptions to set
     */
    public void setKernelPostOptions(Map<String, Object> kernelPostOptionsIn) {
        modify(SET_KERNEL_OPTIONS_POST, kernelPostOptionsIn);
    }
    
    protected void update() {
        client.invokeTokenMethod("update");            
    }
    
    protected boolean isBlank(String str) {
        return str == null || str.trim().length() == 0;
    }
}
