#!/usr/bin/python
#
# Red Hat Network registration tool
# Adapted from wrapper.py
# Copyright (c) 1999-2006 Red Hat, Inc.  Distributed under GPL.
#
# Authors:
#       Adrian Likins <alikins@redhat.com>
#       Preston Brown <pbrown@redhat.com>
#       James Bowes <jbowes@redhat.com> 
#       Daniel Benamy <dbenamy@redhat.com>

import sys
import os

from rhpl.translate import _

if "--development" in sys.argv:
    sys.path.append("../")
    from up2date_client import config
    cfg = config.initUp2dateConfig()
    cfg['development'] = True
    cfg['debug'] = 2
    sys.argv.remove("--development")

sys.path.append("/usr/share/rhn/")

from up2date_client import up2dateLog
up2dateLog.initLog().set_app_name('rhn_register')
from up2date_client import up2dateAuth
from up2date_client import rhncli, rhnreg
from up2date_client import tui
from up2date_client import up2dateErrors

class RhnRegister(rhncli.RhnCli):
    """Runs rhn_register. Can run it in gui or tui mode depending on 
    availablility of gui, DISPLAY environment variable, and --nox switch.
    
    """
    def __init__(self):
        super(RhnRegister, self).__init__()
        self.optparser.add_option("--nox", action="store_true", default=False,
            help=_("Do not attempt to use X"))

    def _get_ui(self):
        try:
            if os.access("/usr/share/rhn/up2date_client/gui.py", os.R_OK) and \
               os.environ["DISPLAY"] != "" and \
               not self.options.nox:
                from up2date_client import gui
                self.hasGui = True # Used by base class. Yech.
                return gui
        except:
            pass
        
        return tui

    @staticmethod
    def __restartRhnRegister():
        args = sys.argv[:]
        return_code = os.spawnvp(os.P_WAIT, sys.argv[0], args)
        sys.exit(return_code)

    def main(self):
        """RhnCli (the base class) just sets stuff up and then calls this to run
        the rest of the program.
        
        """
        if rhnreg.checkEmergencyUpdates():
            print "New package updates Found, Restarting ....."
            RhnRegister.__restartRhnRegister()
        
        ui = self._get_ui()
        ui.main()

        # Check to see if the registration worked.
        try:
            if not up2dateAuth.getLoginInfo():
                if not self._testRhnLogin():
                    sys.exit(1)
        except up2dateErrors.RhnServerException:
            sys.exit(1)
        
        # Assuming registration worked, remember to save info (proxy setup,etc)
        # from rhncli
        self.saveConfig()
        sys.exit(0)


if __name__ == "__main__":
    app = RhnRegister()
    app.run()
