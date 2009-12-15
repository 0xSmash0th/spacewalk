# Copyright 2006 Red Hat, Inc.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
#
# Authors:
#     Daniel Benamy <dbenamy@redhat.com>

import sys
sys.path.append("/usr/share/rhn/up2date_client/")
sys.path.append("/usr/share/rhn")
import rhnreg
import rhnregGui
from rhn_register_firstboot_gui_window import RhnRegisterFirstbootGuiWindow

import gtk
from gtk import glade
import gettext
_ = gettext.gettext

gettext.textdomain("rhn-client-tools")
gtk.glade.bindtextdomain("rhn-client-tools")


class RhnChooseOrgWindow(RhnRegisterFirstbootGuiWindow, rhnregGui.ChooseOrgPage):
    runPriority=108.2
    moduleName = _("Choose Organization")
    windowTitle = moduleName
    shortMessage = _("Choose an organization to use with this username")
    needsparent = 1
    needsnetwork = 1
    noSidebar = True
    
    def __init__(self):
        RhnRegisterFirstbootGuiWindow.__init__(self)
        rhnregGui.ChooseOrgPage.__init__(self)
        if rhnreg.registered():
            self.skipme = True
    
    def _getVbox(self):
        return self.chooseOrgPageVbox()
    
    def updatePage(self):
        self.chooseOrgPagePrepare()
    
    def apply(self, *args):
        """Returns None to stay on the same page. Anything else will cause 
        firstboot to advance but True is generally used. This is different from 
        the gnome druid in rhn_register.
        
        """
        self.chooseOrgPageApply()
        if rhnregGui.activateSubscriptionShouldBeShown():
            self.parent.setPage("rhn_activate_gui")
        else:
            self.parent.setPage("rhn_create_profile_gui")
        return True


childWindow = RhnChooseOrgWindow
