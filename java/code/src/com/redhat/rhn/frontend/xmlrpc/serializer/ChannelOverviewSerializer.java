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
package com.redhat.rhn.frontend.xmlrpc.serializer;

import com.redhat.rhn.frontend.dto.ChannelOverview;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;

import java.io.IOException;
import java.io.Writer;

import redstone.xmlrpc.XmlRpcCustomSerializer;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 * 
 * ChannelOverviewSerializer
 * @version $Rev$
 *
 * @xmlrpc.doc
 * #struct("channel entitlement") 
 *   #prop("string", "label")
 *   #prop("string", "name")
 *   #prop("int", "used_slots")
 *   #prop("int", "free_slots")
 *   #prop("int", "total_slots")
 * #struct_end()
 */
public class ChannelOverviewSerializer implements XmlRpcCustomSerializer {

    /**
     * 
     * {@inheritDoc}
     */
    public Class getSupportedClass() {
        return ChannelOverview.class;
    }
    /**
     * 
     * {@inheritDoc}
     */
    public void serialize(Object value, Writer output, XmlRpcSerializer builtInSerializer)
        throws XmlRpcException, IOException {
        SerializerHelper helper = new SerializerHelper(builtInSerializer);
        
        ChannelOverview group = (ChannelOverview) value;
        
        helper.add("name", group.getName());
        helper.add("label", group.getLabel());
        
        helper.add("used_slots", group.getCurrentMembers());
        

        Long max = group.getMaxMembers();
        if (max == null) {
            helper.add("total_slots", new Integer(0));
            helper.add("free_slots",  new Integer(0)); 
        }
        else {
            helper.add("total_slots", max);
            helper.add("free_slots",  new Long(group.getMaxMembers().longValue() -
                    group.getCurrentMembers().longValue()));
        }

        helper.writeTo(output);
    }
}
