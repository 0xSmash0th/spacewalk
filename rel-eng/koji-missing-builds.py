#!/usr/bin/python

try:
    import koji
except:
    import brew as koji
import os
import string
import sys
from optparse import OptionParser

usage = "usage: %prog [options] <tag to check>"
parser = OptionParser(usage, version='%prog 0.0.1')
parser.add_option("-g","--git", action="store_true", default=False,
  help="print list of builds not managed in git")
(opts, args) = parser.parse_args()

if len(args) < 1:
    print "ERROR: need to specify tag to check\n"
    parser.print_help()
    sys.exit(1)
if len(args) > 1:
    print "ERROR: Only one tag at a time.\n"
    parser.print_help()
    sys.exit(1)

distmap = {'6E':'.el6',
           '5E':'.el5',
           '4E':'.el4',
           'f10':'.fc10',
           'f11':'.fc11'}

tag = args[0]

disttag = distmap[tag.split('-')[1]]


mysession = koji.ClientSession("http://koji.rhndev.redhat.com/kojihub")

rpmlist = mysession.getLatestRPMS(tag)
nvrs = []
kojinames = []
pkglist = []
gitnames = []
notingit = []
for rpm in rpmlist[1]:
    rpmname = rpm['nvr'].replace(disttag,'')
    nvrs.append(rpmname)
    kojinames.append([rpm['name'], rpmname])

pkgfileList = os.listdir( '%s/packages/' % str(os.path.abspath(__file__)).strip('koji-missing-builds.py'))
pkgfileList.remove('.README')

for pkg in pkgfileList:
    fd = open('%s/packages/%s' % (str(os.path.abspath(__file__)).strip('koji-missing-builds.py'), pkg))
    pkginfo = fd.read()
    fd.close()
    pkginfo = pkginfo.split()
    pkglist.append("%s-%s" % (pkg, pkginfo[0]))
    gitnames.append(pkg)

pkglist.sort()
nvrs.sort()
if opts.git:
    print "Builds not in git:"
for pkg in kojinames:
    if not pkg[0] in gitnames:
        if opts.git:
            print "     %s" % pkg[1]
        notingit.append(pkg[1])

print "Extra builds in koji:"
for pkg in nvrs:
    if not pkg in pkglist and not pkg in notingit:
        print "     %s" % pkg

print "Builds missing in koji:"
for pkg in pkglist:
    if not pkg in nvrs:
        print "     %s" % pkg

