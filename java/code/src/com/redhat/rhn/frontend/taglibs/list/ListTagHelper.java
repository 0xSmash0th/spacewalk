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
package com.redhat.rhn.frontend.taglibs.list;

import com.redhat.rhn.frontend.taglibs.list.decorators.PageSizeDecorator;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * Static helper class for the "new-style" list tag
 * 
 * @version $Rev $
 */
public class ListTagHelper {
    

    public static final String PARENT_URL = "parentUrl";
    public static final String PAGE_LIST = "pageList";
    public static final String PAGE_ACTION = "PAGE_ACTION";
    private ListTagHelper() {
        
    }
    
    
    /**
     * Stores the declaration information of an rhnSet
     * so as to be used by the list tag while 
     * rendering a set.
     * @param listName name of list
     * @param decl the set  declaration to bind
     * @param request current HttpServletRequest
     */
    public static void bindSetDeclTo(String listName, RhnSetDecl decl,
                                ServletRequest request) {
        bindSetDeclTo(listName, decl.getLabel(), request);
    }    

    
    /**
     * Stores the declaration information of an rhnSet
     * so as to be used by the list tag while 
     * rendering a set.
     * @param listName name of list
     * @param label the set  declaration to bind
     * @param request current HttpServletRequest
     */
    public static void bindSetDeclTo(String listName, String label,
                                ServletRequest request) {
        String uniqueName = TagHelper.generateUniqueName(listName);
        String selectedName = makeSetDeclAttributeName(uniqueName);
        request.setAttribute(selectedName, label);
    }    
    
    /**
     * Returns a set declaration associated to this list
     * if it was previously bound.
     * @param listName the name of the list to who holds the set.
     *                  Note: this must be a Unique Name .. 
     *                  See bindSetDeclTo method for more info.
     * @param request the servlet request object 
     * @return returns the set declaration label associated to the list.
     */
    public static String lookupSetDeclFor(String listName,
                                                ServletRequest request) {
        String selectedName = makeSetDeclAttributeName(listName);
        return (String) request.getAttribute(selectedName);
    }
    
    private static String makeSetDeclAttributeName(String listName) {
        return "list_" + listName + "_rhn_set";
    }
    
    /**
     * Stores how many objects are selected for use by the list tag
     * @param listName name of list
     * @param amount amount of items selected
     * @param request current HttpServletRequest
     */
    public static void setSelectedAmount(String listName, int amount,
            HttpServletRequest request) {
        String uniqueName = TagHelper.generateUniqueName(listName);
        String selectedName = ListTagUtil.makeSelectedAmountName(uniqueName);
        request.setAttribute(selectedName, String.valueOf(amount));
    }
    
    /**
     * Gets the current page number for the named list
     * This is zero based
     * @param listName name of list
     * @param request active HttpServletRequest
     * @return page number
     */
    public static int getPageNumber(String listName, HttpServletRequest request) {
        String uniqueName = TagHelper.generateUniqueName(listName);
        String paramName = ListTagUtil.makePageNumberName(uniqueName);
        String page = request.getParameter(paramName);
        if (page == null) {
            return 0;
        }
        else {
            return Integer.parseInt(page);
        }
    }

    /**
     * Returns the values of all selected checkboxes
     * @param listName name of list
     * @param request active HttpServletRequest
     * @return string array if items found, else null
     */
    public static String[] getSelected(String listName, HttpServletRequest request) {
        String uniqueName = TagHelper.generateUniqueName(listName);
        String fieldParam = ListTagUtil.makeSelectedItemsName(uniqueName);
        return request.getParameterValues(fieldParam);
    }
    
    /**
     * Returns the values of all the row items in a given list
     * This is useful for example in diff'ing between the result set
     * and the selected items on a page. 
     * @param listName name of list
     * @param request active HttpServletRequest
     * @return string array if items found, else null
     */
    public static String[] getAll(String listName, HttpServletRequest request) {
        String uniqueName = TagHelper.generateUniqueName(listName);
        String fieldParam = ListTagUtil.makePageItemsName(uniqueName);
        return request.getParameterValues(fieldParam);
    }
    
    /**
     * Checks if any of the list actions were clicked like 
     * selectAll, unselectAll update set, pagination buttons (in which 
     * page_action will be returned).. etc
     * and returns the appropriate value
     * @param listName name of list
     * @param request active HttpServletRequest
     * @return List Action if any of the list actions were selected
     *         null if not.
     */
    
    public static String getListAction(String listName, HttpServletRequest request) {
        String uniqueName = TagHelper.generateUniqueName(listName);
        if (DataSetManipulator.getPaginationParam(request, uniqueName) != null ||
                        PageSizeDecorator.pageWidgetSelected(request, listName)) {
            return PAGE_ACTION;
        }
        
        String fieldParam = ListTagUtil.makeSelectActionName(uniqueName);
        return request.getParameter(fieldParam);
    }


    /**
     * returns the value that the list is being filtered upon.  (null if not being filtered)
     * @param request the request to look in
     * @param uniqueName the unique (hashed) name for the list
     * @return the filter value
     */
    public static String getFilterValue(ServletRequest request, String uniqueName) {
        
        String newValue = request.getParameter(
                ListTagUtil.makeFilterValueByLabel(uniqueName));
        String oldValue = request.getParameter(
                ListTagUtil.makeOldFilterValueByLabel(uniqueName));
        
        String clicked = 
            request.getParameter(ListTagUtil.makeFilterNameByLabel(uniqueName));
        
        if (clicked == null) {
            if (oldValue  != null && !oldValue.equals("null")) {
                return oldValue;  
            }
            else {
                return "";
            }
        }
        else {
            return newValue;
        }
        
    }    
}
