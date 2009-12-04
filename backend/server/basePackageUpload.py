#!/usr/bin/python
#
# Copyright (c) 2008 Red Hat, Inc.
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
# 
# Red Hat trademarks are not licensed under GPLv2. No permission is
# granted to use or replicate Red Hat trademarks that are incorporated
# in this software or its documentation. 
#
# $Id$

import os
import tempfile
from mod_python import apache

from common import CFG, log_debug, rhnFault, rhn_mpm, rhnLib, UserDictCase
from rhn.common.rhn_rpm import get_header_byte_range

from server import rhnSQL
from server.importlib import importLib, userAuth, mpmSource, backendOracle, \
    packageImport, errataCache
from server.rhnLib import get_package_path, \
    get_package_path_without_package_name
from server.rhnServer import server_packages

class BasePackageUpload:
    def __init__(self, req):
        self.header_prefix = "X-RHN-Upload"
        self.error_header_prefix = 'X-RHN-Upload-Error'
        self.prefix = 'rhn/repository'
        self.is_source = 0
        self.rel_package_path = None
        self.package_path = None
        self.required_fields = [
            "Package-Name",
            "Package-Version",
            "Package-Release",
            "Package-Arch",
            "File-MD5sum",
        ]
        self.field_data = UserDictCase()
        self.org_id = None

    def headerParserHandler(self, req):
        """ This whole function is ugly as hell. The Auth field in the header used to be required, but now
            it must have either the Auth field or the Auth-Session field.
        """
        # Initialize the logging
        log_debug(3, "Method", req.method)
        
        #Header string. This is what the Auth-Session field will look like in the header.
        session_header = "%s-%s" % (self.header_prefix, "Auth-Session")

        for f in self.required_fields:
            hf = "%s-%s" % (self.header_prefix, f)
            if not req.headers_in.has_key(hf):
                #If the current field is Auth and Auth-Session field isn't present, something is wrong.
                if f == "Auth" and not req.headers_in.has_key(session_header):
                    log_debug(4, "Required field %s missing" % f)
                    raise rhnFault(500, f)

                #The current field is Auth and the Auth-Session field is present, so everything is good.
                elif f == "Auth" and req.headers_in.has_key(session_header):
                    self.field_data["Auth-Session"] = req.headers_in[session_header]
                    continue

                #The current field being looked for isn't the Auth field and it's missing, so something is wrong. 
                else:
                    log_debug(4, "Required field %s missing" % f)
                    raise rhnFault(500, f)

            if not (f == "Auth" and not req.headers_in.has_key(hf)):
                self.field_data[f] = req.headers_in[hf]
            else:
                if req.headers_in.has_key(session_header):
                    self.field_data[f] = req.headers_in[hf]
                
        self.package_name = self.field_data["Package-Name"]
        self.package_version = self.field_data["Package-Version"]
        self.package_release = self.field_data["Package-Release"]
        self.package_arch = self.field_data["Package-Arch"]
        self.file_md5sum = self.field_data["File-MD5sum"] 
        #4/18/05 wregglej. if 1051 is in the header's keys, then it's a nosrc package.
        self.is_source = (self.package_arch == 'src' or self.package_arch == 'nosrc')
        return apache.OK

    def handler(self, req):
        log_debug(3, "Method", req.method)
        return apache.OK
    def cleanupHandler(self, req):
        return apache.OK

    def logHandler(self, req):
        return apache.OK
