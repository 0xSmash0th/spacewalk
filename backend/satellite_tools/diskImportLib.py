#!/usr/bin/python
#
# Common dumper stuff
#
# Copyright (c) 2008--2009 Red Hat, Inc.
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
#

import os

from xml.sax import SAXParseException

from common import Traceback, CFG
from server.rhnSQL.const import ORACLE, POSTGRESQL

import xmlSource
import xmlDiskSource
from server.importlib.channelImport import ChannelImport, ChannelFamilyImport
from server.importlib.packageImport import PackageImport
from server.importlib import archImport
from server.importlib import blacklistImport
from server.importlib import productNamesImport

class Backend:
    __backend = None

    def get_backend(self):
        if self.__backend:
            return self.__backend


        if CFG.DB_BACKEND == ORACLE:
            from server.importlib.backendOracle import OracleBackend
            Backend.__backend = OracleBackend()
        elif CFG.DB_BACKEND == POSTGRESQL:
            from server.importlib.backendOracle import PostgresqlBackend
            Backend.__backend = PostgresqlBackend()
        Backend.__backend.init()
        return Backend.__backend
        
# get_backend() returns a shared instance of an Oracle backend
def get_backend():
    return Backend().get_backend()

# Retrieves an attribute for a channel dumped in XML
def getChannelAttribute(mountPoint, channel, attribute, handler):
    dumper = xmlDiskSource.ChannelDiskSource(mountPoint=mountPoint)
    dumper.setChannel(channel)
    f = dumper.load()

    # save the previous container
    oldContainer = handler.get_container(xmlSource.ChannelContainer.container_name)
    # And replace it with the default one - only saves stuff in the batch
    newContainer = xmlSource.ChannelContainer()
    handler.set_container(newContainer)

    # Process the information
    handler.process(f)

    channel = newContainer.batch[0]

    # Cleanup
    handler.reset()

    # Restore the old container
    handler.set_container(oldContainer)

    return channel.get(attribute)


# Lists the packages linked to a specific channel
def listChannelPackages(mountPoint, channel, handler, sources=0, all=0):
    if sources:
        return getChannelAttribute(mountPoint, channel, 'source-packages', 
            handler)
    if all:
        # All packages requested
        ret = getChannelAttribute(mountPoint, channel, 'all-packages', handler)
        if ret:
            return ret
    return getChannelAttribute(mountPoint, channel, 'packages', handler)


# Lists the errata linked to a specific channel
def listChannelErrata(mountPoint, channel, handler):
    return getChannelAttribute(mountPoint, channel, 'errata', handler)


# Retrieves an attribute for a channel dumped in XML
def getKickstartTree(mountPoint, ks_label, handler):
    ds = xmlDiskSource.KickstartDataDiskSource(mountPoint=mountPoint)
    ds.setID(ks_label)
    f = ds.load()

    # save the previous container
    oldContainer = handler.get_container(xmlSource.KickstartableTreesContainer.container_name)
    # And replace it with the default one - only saves stuff in the batch
    newContainer = xmlSource.KickstartableTreesContainer()
    handler.set_container(newContainer)

    # Process the information
    handler.process(f)

    if not newContainer.batch:
        return None

    kstree = newContainer.batch[0]

    # Cleanup
    handler.reset()

    # Restore the old container
    handler.set_container(oldContainer)

    return kstree


# Returns a batch of package dumps for the requested package ids
def loadPackages(mountPoint, packageIds, handler, sources=0):
    if sources:
        container_class = xmlSource.SourcePackageContainer
        source_class = xmlDiskSource.SourcePackageDiskSource
    else:
        container_class = xmlSource.PackageContainer
        source_class = xmlDiskSource.PackageDiskSource
    # Save the previous container
    oldContainer = handler.get_container(container_class.container_name)
    # Create a new container object
    container = container_class()
    handler.set_container(container)

    package_source = source_class(mountPoint)

    for oid in packageIds:
        package_source.setID(oid)
        stream = package_source.load()

        try:
            handler.process(stream)
        except SAXParseException:
            text = "XML parser exception while processing %s" % oid
            Traceback(extra=text, mail=0)

        handler.reset()
        # At the end of this step, the package should have been appended to
        # the batch
    
    # Restore the old container
    handler.set_container(oldContainer)
    return container.batch

# Returns a batch of errata
# Also fixes the errata: loads short package info to switch from package ids
# to nvrea
def loadErrata(mountPoint, errataIds, handler):
    # Grab the current container
    oldContainer = handler.get_container(xmlSource.ErrataContainer.container_name)
    # Create a new container object
    container = xmlSource.ErrataContainer()
    handler.set_container(container)

    errata_source = xmlDiskSource.ErrataDiskSource(mountPoint)

    for oid in errataIds:
        errata_source.setID(oid)
        stream = errata_source.load()

        try:
            handler.process(stream)
        except SAXParseException:
            text = "XML parser exception while processing %s" % oid
            Traceback(extra=text, mail=0)

        handler.reset()
        
        # now the erratum is in the batch

    # Restore the old container

    handler.set_container(oldContainer)

    packages = {}

    errata = container.batch
    # Prepare to fix errata
    for erratum in errata:
        # Fetch the package ids
        for oid in erratum['packages']:
            packages[oid] = None

    # Replace the short package container
    oldContainer = handler.get_container(xmlSource.IncompletePackageContainer.container_name)
    container = xmlSource.IncompletePackageContainer()
    handler.set_container(container)

    # Parse the short package file
    incomplete_package_source = xmlDiskSource.ShortPackageDiskSource(mountPoint)
    for oid in packages.keys():
        if not incomplete_package_source.has_key(oid):
            print "Skipping package id %s" % oid
            continue

        incomplete_package_source.setID(oid)
        print "Parsing short package info for id %s" % oid
        stream = incomplete_package_source.load()

        handler.process(stream)
        handler.reset()
        packages[oid] = container.batch[0]
        # Clean up the batch
        container.batch = []

    # Now fix the errata
    for erratum in errata:
        # Fetch the package ids
        erratum['packages'] = map(lambda x, h=packages: h[x],
            filter(lambda x, h=packages: h[x] is not None, 
            erratum['packages']))

    # Restore the short package container
    handler.set_container(oldContainer)
        
    # And return the errata
    return errata


# Removes a directory tree
# XXX misa: should use os.path.walk instead
def recursiveDelete(dirname):
    dirs = [dirname]
    while dirs:
        if not os.path.isdir(dirs[-1]):
            del dirs[-1]
            continue
        ld = os.listdir(dirs[-1])
        if not ld:
            # Empty directory
            os.rmdir(dirs[-1])
            del dirs[-1]
            continue

        top = dirs[-1]
        # Delete files first
        for f in ld:
            path = "%s/%s" % (top, f)
            if os.path.isdir(path):
                # Directory; have to recurse into it
                dirs.append(path)
                continue
            # Remove the file/link/socket/etc
            os.unlink(path)


# Functions for dumping packages
def rpmsPath(obj_id, mountPoint, sources=0):
    # returns the package path (for exporter/importer only)
    # not to be confused with where the package lands on the satellite itself.
    if not sources:
        template = "%s/rpms/%s/%s.rpm"
    else:
        template = "%s/srpms/%s/%s.rpm"
    return os.path.normpath(template % (
        mountPoint, xmlDiskSource.hashPackageId(obj_id, mod=100, padding=2), obj_id))


class BlacklistObsoletesContainer(xmlSource.BlacklistObsoletesContainer):
    def endContainerCallback(self):
        if not self.batch:
            return
        importer = blacklistImport.BlacklistObsoletesImport(
            self.batch, get_backend())
        importer.run()
        self.batch = []

class ProductNamesContainer(xmlSource.ProductNamesContainer):
    def endContainerCallback(self):
        if not self.batch:
            return
        importer = productNamesImport.ProductNamesImport(
            self.batch, get_backend())
        importer.run()
        self.batch = []

class ChannelArchContainer(xmlSource.ChannelArchContainer):
    def endContainerCallback(self):
        importer = archImport.ChannelArchImport(self.batch, get_backend())
        importer.run()
        self.batch = []

class PackageArchContainer(xmlSource.PackageArchContainer):
    def endContainerCallback(self):
        importer = archImport.PackageArchImport(self.batch, get_backend())
        importer.run()
        self.batch = []

class ServerArchContainer(xmlSource.ServerArchContainer):
    def endContainerCallback(self):
        importer = archImport.ServerArchImport(self.batch, get_backend())
        importer.run()
        self.batch = []

class CPUArchContainer(xmlSource.CPUArchContainer):
    def endContainerCallback(self):
        importer = archImport.CPUArchImport(self.batch, get_backend())
        importer.run()
        self.batch = []

class ServerPackageArchCompatContainer(xmlSource.ServerPackageArchCompatContainer):
    def endContainerCallback(self):
        importer = archImport.ServerPackageArchCompatImport(self.batch,
            get_backend())
        importer.run()
        self.batch = []

class ServerChannelArchCompatContainer(xmlSource.ServerChannelArchCompatContainer):
    def endContainerCallback(self):
        importer = archImport.ServerChannelArchCompatImport(self.batch,
            get_backend())
        importer.run()
        self.batch = []

class ChannelPackageArchCompatContainer(xmlSource.ChannelPackageArchCompatContainer):
    def endContainerCallback(self):
        importer = archImport.ChannelPackageArchCompatImport(self.batch,
            get_backend())
        importer.run()
        self.batch = []

class ServerGroupServerArchCompatContainer(xmlSource.ServerGroupServerArchCompatContainer):
    def endContainerCallback(self):
        importer = archImport.ServerGroupServerArchCompatImport(self.batch,
            get_backend())
        importer.run()
        self.batch = []

class ChannelFamilyContainer(xmlSource.ChannelFamilyContainer):
    def endContainerCallback(self):
        importer = ChannelFamilyImport(self.batch, get_backend())
        importer.run()
        self.batch = []

class ChannelContainer(xmlSource.ChannelContainer):
    def endContainerCallback(self):
        importer = ChannelImport(self.batch, get_backend())
        importer.run()
        self.batch = []


class PackageContainer(xmlSource.PackageContainer):
    def endContainerCallback(self):
        importer = PackageImport(self.batch, get_backend())
        importer.setIgnoreUploaded(1)
        importer.run()
        self.batch = []

# Handy function that returns a new handler object (so we can parse XML
# streams)
def getHandler():
    handler = xmlSource.getHandler()
    handler.set_container(ChannelArchContainer())
    handler.set_container(PackageArchContainer())
    handler.set_container(ServerArchContainer())
    handler.set_container(ServerPackageArchCompatContainer())
    handler.set_container(ServerChannelArchCompatContainer())
    handler.set_container(ChannelPackageArchCompatContainer())
    handler.set_container(CPUArchContainer())
    handler.set_container(ChannelFamilyContainer())
    handler.set_container(ChannelContainer())
    handler.set_container(PackageContainer())
    handler.set_container(BlacklistObsoletesContainer())
    handler.set_container(ProductNamesContainer())
    return handler

