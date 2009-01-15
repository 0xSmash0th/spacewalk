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
#Mechanism to persistently cache sync info (mostly post-parsed(XML)
#    package objects). 
#

# system imports:
import os
import math
import string

# rhn imports:
from common import CFG, rhnCache

# NOTE: this is a python 2.2-ism
__all__ = []

class BaseCache:
    _compressed = 1
    def __init__(self):
        # Kind of kludgy - this may have weird side-effects if called from
        # within the server code
        rhnCache.CACHEDIR = CFG.SYNC_CACHE_DIR
    
    def cache_get(self, object_id, timestamp=None):
        # Get the key
        key = self._get_key(object_id)
        return rhnCache.get(key, modified=timestamp, raw=0,
            compressed=self._compressed)

    def cache_set(self, object_id, value, timestamp=None):
        # Get the key
        key = self._get_key(object_id)
        return rhnCache.set(key, value, modified=timestamp, raw=0,
            compressed=self._compressed)

    def cache_has_key(self, object_id, timestamp=None):
        # Get the key
        key = self._get_key(object_id)
        return rhnCache.has_key(key, modified=timestamp)

    def _get_key(self, object_id):
        raise NotImplementedError()

    def _hash_object_id(self, object_id, factor):
        """Given an object id (assumed to be <label>-<number>), returns the
        last few digits for the number. For instance, (812345, 1000) should
        return 345"""
        object_id = str(object_id)
        # Compute the number of digits we need from the hash
        format = int(math.ceil(math.log10(factor)))
        # Format the template
        format = '%0' + str(format) + 'd'
        # Grab the digits after -
        num_id = string.split(object_id, '-')[-1]
        num_id = int(num_id) % factor
        return format % num_id

class ChannelCache(BaseCache):
    def _get_key(self, object_id):
        return os.path.join("satsync", "channels", str(object_id))

class BasePackageCache(BaseCache):
    _subdir = "__unknown__"
    def _get_key(self, object_id):
        hash_val = self._hash_object_id(object_id, 100)
        return os.path.join("satsync", self._subdir, hash_val, str(object_id))

class ShortPackageCache(BasePackageCache):
    _subdir = "short-packages"
    _compressed = 0

class PackageCache(BasePackageCache):
    _subdir = "packages"

class SourcePackageCache(BasePackageCache):
    _subdir = "source-packages"

class ErratumCache(BaseCache):
    _subdir = "errata"
    def _get_key(self, object_id):
        hash_val = self._hash_object_id(object_id, 10)
        return os.path.join("satsync", self._subdir, hash_val, str(object_id))

class KickstartableTreesCache(BaseCache):
    _subdir = "kickstartable-trees"
    def _get_key(self, object_id):
        return os.path.normpath(os.path.join("satsync", self._subdir, 
            object_id))

if __name__ == '__main__':
    from common import initCFG
    initCFG("server.satellite")
    c = PackageCache()
    pid = 'package-12345'
    c.cache_set(pid, {'a' : 1, 'b' : 2})
    print c.cache_get(pid)
