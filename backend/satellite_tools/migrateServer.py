#!/usr/bin/python
"""
Multi-Org: Script to migrate server from one org to another

Script that connects to a given satellite db and migrates the
server and its history from source org to the destination org.

Copyright (c) 2008 Red Hat, Inc.  Distributed under GPL.
Author: Pradeep Kilambi <pkilambi@redhat.com>

# $Id: migrateServer.py 
"""

import os
import sys
import string
import getpass
import xmlrpclib

_topdir = '/usr/share/rhn'
if _topdir not in sys.path:
    sys.path.append(_topdir)

from optparse import OptionParser, Option


client = None

options_table = [
    Option("-v", "--verbose",       action="count", 
        help="Increase verbosity"),
    Option("-u", "--username",       action="store", 
        help="Satellite/Org Admin username"),
    Option("-p", "--password",       action="store", 
        help="Satellite/Org Admin password"),
    Option("--satellite",       action="store", 
        help="Satellite server to run migrate on"),
    Option("--list-systems",               action="store", 
        help=" Available Systems by org. Usage: \
                         --list-systems=<org_id>"),
    Option("--serverId",               action="append", 
        help="Server to migrate"),
    Option("--from-org-id",          action="store",
        help="Source Org ID"),
    Option("--to-org-id",          action="store",
        help="Destination Org ID"),
    Option("--csv",                action="store",
        help="CSV File to process"),
]

_csv_fields = [ 'server-id', 'from-org-id', 'to-org-id' ]


def main():
    global options_table, client
    parser = OptionParser(option_list=options_table)

    (options, args) = parser.parse_args()

    if options.satellite:
        SATELLITE_HOST = options.satellite
    else:
        SATELLITE_HOST = os.uname()[1]
    
    SATELLITE_URL = "http://%s/rpc/api" % SATELLITE_HOST

    client = xmlrpclib.Server(SATELLITE_URL, verbose=0)

    # Check data
    if options.list_systems:
        #lookup_server(options.list_systems)
        return

    if options.csv:
        migrate_data = read_csv_file(options.csv)
    else:
        migrate_data = []

    if not options.csv:
        if not options.serverId:
            print "Missing --serverId"
            return 1

        if not options.to_org_id:
            print "Missing Destination org id"
            return
        else:
            to_org_id = options.to_org_id or None

        if not options.from_org_id:
            print "Missing Source org id"
            return
        else:
            from_org_id = options.from_org_id or None


        migrate_data = [[options.serverId, from_org_id, to_org_id]]
    
    username, password = getUsernamePassword(options.username, \
                            options.password)
    
    sessionKey = login(username, password)
    
    if not migrate_data:
        sys.stderr.write("Nothing to migrate. Exiting.. \n")
        sys.exit(1)

    for server_id, from_org_id, to_org_id in migrate_data:
        server_id = map(lambda a:int(a), server_id)
        try:
            migrate_system(sessionKey, int(from_org_id), int(to_org_id),\
                           server_id)
        except:
            raise
    logout(sessionKey)

def login(username, password):
    """
     Authenticate Session call
    """ 
    sessionkey = client.auth.login(username, password)
    return sessionkey

def logout(session_key):
    """
     End Authentication call
    """
    client.auth.logout(session_key)


def migrate_system(key, oldOrgId, newOrgId, server_ids):
    """
    Call to migrate given system to new org
    """ 
    try:
        client.org.migrateSystems(key, oldOrgId, newOrgId, server_ids)
    except xmlrpclib.Fault, e:
        sys.stderr.write("Error: %s\n" % e.faultString)
        sys.exit(-1)
        
    return

def getUsernamePassword(cmdlineUsername, cmdlinePassword):
    """
     Returns a username and password (either by returning the ones passed as
     args, or the user's input
    """
    if cmdlineUsername and cmdlinePassword:
        return cmdlineUsername, cmdlinePassword

    username = cmdlineUsername
    password = cmdlinePassword

    # Read the username, if not already specified
    tty = open("/dev/tty", "r+")
    while not username:
        tty.write("Red Hat Network username: ")
        try:
            username = tty.readline()
        except KeyboardInterrupt:
                tty.write("\n")
                sys.exit(0)
        if username is None:
            # EOF
            tty.write("\n")
            sys.exit(0)
        username = string.strip(username)
        if username:
            break

    # Now read the password
    while not password:
        try:
            password = getpass.getpass("Red Hat Network password: ")
        except KeyboardInterrupt:
            tty.write("\n")
            sys.exit(0)
        tty.close()
    return username, password

def lookup_server(key, from_org_id):
    # Get the org id
    # TODO: replace with an api call
    rows = client.org.listServerByOrg(key, from_org_id)
    if not rows:
        sys.stderr.write("No Systems registered for Org-ID %s \n" % org_id)
        sys.exit(1)
    print "                                    "
    print "Available Systems for Org-ID: %s " % org_id
    print "------------------------------------"
    print " Server-ID      Server-Name         "
    print "------------------------------------"
    for row in rows:
        print " %s   %s " % (row['id'], row['name'])
    print "--------------------------------------------"
    
    return rows

def read_csv_file(csv_file):
    """
     Parse the fields in the given csv
    """
    import csv
    csv_data = []
    f_csv = open(csv_file)
    reader = csv.reader(f_csv)
    for data in reader:
        if len(data) != len(_csv_fields):
            sys.stderr.write("Invalid Data.Skipping line .. \n" \
                             % data)
            continue
        csv_data.append(data)
    return csv_data

if __name__ == '__main__':
    sys.exit(main() or 0)
