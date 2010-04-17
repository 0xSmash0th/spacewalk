'''
Author: Aron Parsons <aron@redhat.com> -or- <aronparsons@gmail.com>
License: GPLv3+
'''

import atexit, logging, os, re, readline
import sys, urllib2, xml, xmlrpclib

from cmd import Cmd
from datetime import datetime, timedelta
from getpass import getpass
from operator import itemgetter
from pwd import getpwuid
from tempfile import mkstemp
from textwrap import wrap

class SpacewalkShell(Cmd):
    MINIMUM_API_VERSION = 10.8

    HISTORY_LENGTH = 1024

    SEPARATOR = '\n--------------------\n'
  
    ENTITLEMENTS = {'provisioning_entitled'        : 'Provisioning',
                    'enterprise_entitled'          : 'Management',
                    'monitoring_entitled'          : 'Monitoring',
                    'virtualization_host'          : 'Virtualization',
                    'virtualization_host_platform' : 'Virtualization Platform'}

    EDITORS = ('vim', 'vi', 'nano', 'emacs')

    intro = '''
Welcome to SpacewalkShell, a command line interface to Spacewalk.

For a full set of commands, type 'help' on the prompt.
For help for a specific command try 'help <cmd>'.
'''
    cmdqueue = []
    completekey = 'tab'
    stdout = sys.stdout
    prompt = 'Spacewalk> '

    # do nothing on an empty line
    emptyline = lambda self: None

    def __init__(self, options):
        self.session = ''
        self.username = ''
        self.server = ''
        self.ssm = {}

        # make the options available everywhere
        self.options = options

        userinfo = getpwuid(os.getuid())
        self.cache_file = os.path.join(userinfo[5], '.spacecmd_cache')
        self.history_file = os.path.join(userinfo[5], '.spacecmd_history')

        try:
            # don't split on hyphens or colons during tab completion
            newdelims = readline.get_completer_delims()
            newdelims = re.sub(':|-|/', '', newdelims)
            readline.set_completer_delims(newdelims)
        
            if not options.nohistory:
                try:
                    if os.path.isfile(self.history_file):
                        readline.read_history_file(self.history_file)
    
                    readline.set_history_length(self.HISTORY_LENGTH)

                    # always write the history file on exit
                    atexit.register(readline.write_history_file,
                                    self.history_file)
                except:
                    logging.error('Could not read history file')
                    logging.debug(sys.exc_info())
        except:
            logging.debug(sys.exc_info())


    # load the history file
    def preloop(self):
        if not self.session:
            self.args = []
            self.do_login(self.args)


    # handle commands that exit the shell
    def precmd(self, line, nohistory=False):
        # set the command and arguments once so they can be used elsewhere
        try:
            parts = line.split()
            self.cmd = parts[0]
            self.args = parts[1:]

            # allow simple globbing
            self.args = [re.sub('\*', '.*', a) for a in self.args]
        except IndexError:
            self.cmd = ''
            self.args = []

        if nohistory:
            return line

        # perform bash-like command substitution 
        if self.cmd[0] == '!':
            # remove the '!*' line from the history
            self.remove_last_history_item()

            history_match = False
           
            if self.cmd[1] == '!':
                # repeat the last command
                line = readline.get_history_item(
                           readline.get_current_history_length())

                if line:
                    history_match = True
                else:
                    logging.warning(self.cmd + ': event not found')
                    return ''
            
            if not history_match:
                # is a specific history item being referenced? 
                try:
                    number = int(self.cmd[1:])
                    line = readline.get_history_item(number)
                    if line:
                        history_match = True
                    else:
                        raise Exception
                except:
                    logging.warning(self.cmd + ': event not found')
                    return ''

            # attempt to match the beginning of the string with a history item
            if not history_match:
                history_range = range(1, readline.get_current_history_length())
                history_range.reverse()
 
                for i in history_range:
                    item = readline.get_history_item(i)
                    if re.match(self.cmd[1:], item):
                        line = item
                        history_match = True
                        break
           
            # append the arguments to the substituted command 
            if history_match:
                line += ' %s' % ''.join(self.args)
                print line
                readline.add_history(line)
            else:
                logging.warning(self.cmd + ': event not found')
                return ''

        if self.cmd.lower() in ('quit', 'exit', 'eof'):
            print
            sys.exit(0)
        else:
            return line

####################

    def tab_completer(self, options, text):
        return [o for o in options if re.match(text, o)]


    def filter_results(self, list, patterns):
        compiled_regex = []
        for pattern in patterns:
            if pattern != '':
                compiled_regex.append(re.compile(pattern, re.IGNORECASE))

        matches = []
        for item in list:
            for pattern in compiled_regex:
                if pattern.match(item):
                    matches.append(item)

        return matches


    def editor(self, template):
        # create a temporary file
        (descriptor, file_name) = mkstemp(prefix='spacecmd.')

        if template and descriptor:
            try:
                file = os.fdopen(descriptor, 'w')
                file.write(template)
                file.close()
            except:
                logging.warning('Could not open the temporary file')
                pass

        success = False
        for editor_cmd in self.EDITORS:
            try:
                exit_code = os.spawnlp(os.P_WAIT, editor_cmd, 
                                       editor_cmd, file_name)

                if exit_code == 0:
                    success = True
                    break
                else:
                    logging.error('Editor exited with code %s' % str(exit_code))
            except:
                logging.error(sys.exc_info()[1])
                logging.debug(sys.exc_info())

        if not success:
            logging.error('No editors found')
            return ''

        if os.path.isfile(file_name) and exit_code == 0:
            try:
                # read the session (format = username:session)
                file = open(file_name, 'r')
                contents = file.read()
                file.close()
                
                return (contents, file_name)
            except:
                logging.error('Could not read %s' % file_name)
                logging.debug(sys.exc_info())
                return ''


    def remove_last_history_item(self):
        last = readline.get_current_history_length() - 1
        readline.remove_history_item(last)


    def prompt_user(self, prompt):
        input = raw_input(prompt)

        if input != '':
            self.remove_last_history_item()

        return input


    def user_confirm(self, prompt='Is this correct?'):
        answer = self.prompt_user(prompt + ' ')

        if re.match('y', answer, re.IGNORECASE):
            return True
        else:
            return False


    # parse time input from the userand return xmlrpclib.DateTime
    def parse_time_input(self, time):
        if time == '' or re.match('now', time, re.IGNORECASE):
            time = datetime.now() 
        else:
            # parse the time provided
            match = re.search('^\+?(\d+)(s|m|h|d)$', time, re.IGNORECASE)
    
            if not match or len(match.groups()) != 2:
                logging.error('Invalid time provided')
                return
                
            number = int(match.group(1))
            unit = match.group(2)
   
            if re.match('s', unit, re.IGNORECASE):
                delta = timedelta(seconds=number)
            elif re.match('m', unit, re.IGNORECASE):
                delta = timedelta(minutes=number)
            elif re.match('h', unit, re.IGNORECASE):
                delta = timedelta(hours=number)
            elif re.match('d', unit, re.IGNORECASE):
                delta = timedelta(days=number)
    
            time = now() + delta
 
        time = xmlrpclib.DateTime(time.timetuple())

        if time:
            return time
        else:
            logging.error('Invalid time provided')
            return


    # build a proper RPM name from the various parts
    def build_package_names(self, packages):
        single = False

        if not isinstance(packages, list):
            packages = [packages]
            single = True

        package_names = []
        for p in packages:
            package = p.get('name') + '-' \
                    + p.get('version') + '-' \
                    + p.get('release')

            if p.get('epoch') != ' ' and p.get('epoch') != '':
                package += ':%s' % p.get('epoch')

            if p.get('arch'):
                # system.listPackages uses AMD64 instead of x86_64
                arch = re.sub('AMD64', 'x86_64', p.get('arch'))

                package += '.%s' % arch
            elif p.get('arch_label'):
                package += '.%s' % p.get('arch_label')

            package_names.append(package)
           
        if single:
            return package_names[0]
        else:
            package_names.sort()
            return package_names


    # check for duplicate system names and return the system ID
    def get_system_id(self, name):
        systems = self.client.system.getId(self.session, name)

        if not len(systems):
            logging.warning('No systems found')
            return 0
        elif len(systems) == 1:
            return systems[0].get('id')
        else:
            logging.warning('Multiple systems found with the same name')

            for system in sorted(systems):
                logging.warning('%s = %s' % (name, str(system.get('id'))))

            return 0


    def expand_systems(self, args):
        systems = []
        for item in args:
            if re.match('group:', item):
                item = re.sub('group:', '', item)
                members = self.do_group_listsystems(item, True)

                if len(members):
                    systems.extend(members)
                else:
                    logging.warning('No systems in group %s' % item)
            elif re.match('search:', item):
                query = item.split(':', 1)[1]
                results = self.do_system_search(query, True)
        
                if len(results):
                    systems.extend(results)
            else:
                systems.append(item)

        return systems


    def print_errata_summary(self, errata):
        date_parts = errata.get('date').split()

        if len(date_parts) > 1:
            errata['date'] = date_parts[0]

        print '%s  %s  %s'  % (
              errata.get('advisory_name').ljust(14), 
              wrap(errata.get('advisory_synopsis'), 50)[0].ljust(50),
              errata.get('date').rjust(8))


    def print_errata_list(self, errata):
            rhsa = []
            rhea = []
            rhba = []

            for e in errata:
                type = e.get('advisory_type').lower()

                if 'security' in type:
                    rhsa.append(e)
                elif 'bug fix' in type:
                    rhba.append(e)
                elif 'enhancement' in type:
                    rhea.append(e)
                else:
                    logging.warning(e.get('%s is an unknown type') % (
                                    e.get('advisory_name')))
                    continue
               
            if not len(errata):
                print 'No relevant errata'
                return

            if len(rhsa):
                print 'Security Errata:'
                map(self.print_errata_summary, rhsa)
            
            if len(rhba):
                if len(rhsa):
                    print

                print 'Bug Fix Errata:'
                map(self.print_errata_summary, rhba)
 
            if len(rhea):
                if len(rhsa) or len(rhba):
                    print

                print 'Enhancement Errata:'
                map(self.print_errata_summary, rhea)


    def print_action_summary(self, action, systems=[]):
        print 'ID:         %s' % str(action.get('id'))
        print 'Type:       %s' % action.get('type')
        print 'Scheduler:  %s' % action.get('scheduler')
        print 'Start Time: %s' % re.sub('T' , ' ', action.get('earliest').value)
      
        if len(systems): 
            print
            print 'Systems:'
            for s in systems:
                print '  %s' % s.get('server_name')

####################

    def help_ssm_clear(self):
        print 'Usage: ssm_clear'
    
    def do_ssm_clear(self, args):
        self.ssm.clear()

####################

    def help_help(self):
        print 'Usage: help COMMAND'

####################

    def help_clear(self):
        print 'Usage: clear'
    
    def do_clear(self, args):
        os.system('clear')

####################

    def help_history(self):
        print 'Usage: history'

    def do_history(self, args):
        for i in range(1, readline.get_current_history_length()):
            print '%s  %s' % (str(i).rjust(4), readline.get_history_item(i))

####################

    def help_whoami(self):
        print 'Usage: whoami'

    def do_whoami(self, args):
        if len(self.username):
            print self.username
        else:
            logging.warning("You're not logged in")

####################

    def help_whoamitalkingto(self):
        print 'Usage: whoamitalkingto'

    def do_whoamitalkingto(self, args):
        if len(self.server):
            print self.server
        else:
            logging.warning('Yourself')

####################

    def help_ssm(self):
        print 'The System Set Manager (SSM) is a group of systems that you '
        print 'can perform tasks on as a group.'
        print
        print 'Example:'
        print '> ssm_add group:rhel5-x86_64'
        print '> ssm_add someotherhost.example.com'
        print '> system_details ssm'

    def help_ssm_add(self):
        print 'Usage: ssm_add SYSTEM|group:GROUP|search:QUERY ...'

    def complete_ssm_add(self, text, line, begidx, endidx):
        if re.match('group:', text):
            # prepend 'group' to each item for tab completion
            groups = ['group:%s' % g for g in self.do_group_list('', True)]

            return self.tab_completer(groups, text)
        else:
            return self.tab_completer(self.do_system_list('', True), text) 

    def do_ssm_add(self, args):
        if not len(self.args):
            self.help_ssm_add()
            return

        all_systems = {}
        for s in self.client.system.listSystems(self.session):
            all_systems[s.get('name')] = s.get('id')

        systems = self.expand_systems(self.args)
        matches = self.filter_results(all_systems.keys(), systems)

        if not len(matches):
            logging.warning('No matches found')
            return

        for match in matches:
            if match in self.ssm.keys():
                logging.warning(match + ' is already in the list')
                continue
            else:
                logging.info('Added %s' % match)
                self.ssm[match] = all_systems.get(match)

        if len(self.ssm):
            print 'Systems Selected: %s' % str(len(self.ssm))

####################

    def help_ssm_rm(self):
        print 'Usage: ssm_rm SYSTEM|group:GROUP|search:QUERY ...'
    
    def complete_ssm_rm(self, text, line, begidx, endidx):
        if re.match('group:', text):
            # prepend 'group' to each item for tab completion
            groups = ['group:%s' % g for g in self.do_group_list('', True)]

            return self.tab_completer(groups, text)
        else:
            return self.tab_completer(self.do_ssm_list('', True), text)

    def do_ssm_rm(self, args):
        if not len(self.args):
            self.help_ssm_rm()
            return

        systems = self.expand_systems(self.args)
        matches = self.filter_results(self.ssm.keys(), systems)
        
        if not len(matches):
            logging.warning('No matches found')
            return

        for match in matches:
            logging.info('Removed %s' % match)
            del self.ssm[match]
            
        print 'Systems Selected: %s' % str(len(self.ssm))

####################
 
    def help_ssm_list(self):
        print 'Usage: ssm_list'
    
    def do_ssm_list(self, args, doreturn=False):
        systems = sorted(self.ssm.keys())

        if doreturn:
            return systems
        else:
            print '\n'.join(systems)

            if len(systems):
                print 'Systems Selected: %s' % str(len(systems))

####################

    def help_login(self):
        print 'Usage: login [USERNAME] [SERVER]'

    def do_login(self, args):
        self.session = ''
        
        if self.options.nossl:
            proto = 'http'
        else:
            proto = 'https'

        if len(self.args) == 2 and self.args[1]:
            server = self.args[1]
        elif self.options.server:
            server = self.options.server
        else:
            logging.warning('No server specified')
            return

        serverurl = '%s://%s/rpc/api' % (proto, server)

        # connect to the server
        logging.debug('Connecting to %s' % (server))
        self.client = xmlrpclib.Server(serverurl)

        try:
            api_version = self.client.api.getVersion()
        except:
            logging.error('API version check failed')
            logging.error(sys.exc_info()[1])
            logging.debug(sys.exc_info())
            self.client = None
            return

        # ensure the server is recent enough
        if api_version < self.MINIMUM_API_VERSION:
            logging.error('API (%s) is too old (>= %s required)'
                          % (api_version, self.MINIMUM_API_VERSION))

            self.client = None
            return

        # retrieve a cached session
        if not self.options.nocache:
            if os.path.isfile(self.cache_file):
                try:
                    # read the session (format = username:session)
                    sessionfile = open(self.cache_file, 'r')
                    parts = sessionfile.read().split(':')
                    sessionfile.close()
   
                    username = parts[0]
                    self.session = parts[1]
                except:
                    logging.error('Could not read %s' % self.cache_file)
                    logging.debug(sys.exc_info())

                try:
                    logging.info('Using cached credentials from %s' %
                                 self.cache_file)

                    self.client.user.listUsers(self.session)
                except:
                    logging.info('Cached credentials are invalid')
                    self.session = ''

                    try:
                        os.remove(self.cache_file)
                    except:
                        logging.debug(sys.exc_info())
                        pass
        
        # attempt to login if we don't have a valid session yet    
        if not self.session:
            if self.options.username:
                username = self.options.username
                self.options.username = None
            elif len(self.args) and self.args[0]:
                username = self.args[0]
            else:
                username = self.prompt_user('Username: ')

                # don't store the username in the command history
                self.remove_last_history_item()

            if self.options.password:
                password = self.options.password
                self.options.password = None
            else:
                password = getpass('Password: ')

            try:
                self.session = self.client.auth.login(username, 
                                                      password)
            except:
                logging.warning('Invalid credentials')
                logging.debug(sys.exc_info())
                return

            # write the session to a cache
            if not self.options.nocache:
                try:
                    logging.debug('Writing session cache to %s' % 
                                  self.cache_file) 
                    sessionfile = open(self.cache_file, 'w')
                    sessionfile.write('%s:%s' % (username, self.session))
                    sessionfile.close()
                except:
                    logging.error('Could not write cache file')
                    logging.debug(sys.exc_info())
 
        # disable caching of subsequent logins
        self.options.nocache = True

        # keep track of who we are and who we're connected to
        self.username = username
        self.server = server

        logging.info('Connected to %s as %s' % (serverurl, username))

####################

    def help_logout(self):
        print 'Usage: logout'
        
    def do_logout(self, args):
        if self.session:
            self.client.auth.logout(self.session)
            self.session = ''
            self.username = ''
            self.server = ''
           
            if os.path.isfile(self.cache_file): 
                try:
                    os.remove(self.cache_file)
                except:
                    logging.debug(sys.exc_info())
        else:
            logging.warning("You're not logged in")

####################

    def help_get_apiversion(self):
        print 'Usage: get_apiversion'


    def do_get_apiversion(self, args):
        print self.client.api.getVersion()

####################

    def help_get_serverversion(self):
        print 'Usage: get_serverversion'

    def do_get_serverversion(self, args):
        print self.client.api.systemVersion()

####################

    def help_get_certificateexpiration(self):
        print 'Usage: get_certificateexpiration'

    def do_get_certificateexpiration(self, args):
        print self.client.satellite.getCertificateExpirationDate(self.session).value

####################

    def help_get_entitlements(self):
        print 'Usage: get_entitlements'

    def do_get_entitlements(self, args):
        entitlements = self.client.satellite.listEntitlements(self.session)

        print 'System:'
        for e in entitlements.get('system'):
            print '%s: %s/%s' % (
                  e.get('label'), 
                  str(e.get('used_slots')),  
                  str(e.get('total_slots')))

        print       
        print 'Channel:'
        for e in entitlements.get('channel'):
            print '%s: %s/%s' % (
                  e.get('label'), 
                  str(e.get('used_slots')),  
                  str(e.get('total_slots')))

####################

    def help_package_details(self):
        print 'Usage: package_details PACKAGE ...'        

    def do_package_details(self, args):
        if not len(self.args):
            self.help_package_details()
            return

        add_separator = False

        for package in self.args:
            if add_separator:
                print self.SEPARATOR

            add_separator = True

            try:
                id = int(package)
            except:
                id = self.client.packages.search.name(self.session,
                                                      package)[0].get('id')

            details = self.client.packages.getDetails(self.session, id)

            channels = \
                self.client.packages.listProvidingChannels(self.session, id)

            print 'Name:    %s' % details.get('name')
            print 'Version: %s' % details.get('version')
            print 'Release: %s' % details.get('release') 
            print 'Epoch:   %s' % details.get('epoch')
            print 'Arch:    %s' % details.get('arch_label')

            print
            print 'Description: '
            print '\n'.join(wrap(details.get('description')))

            print
            print 'File:    %s' % details.get('file')
            print 'Size:    %s' % details.get('size')
            print 'MD5:     %s' % details.get('md5sum')

            print
            print 'Available From:'
            print '\n'.join(sorted([c.get('label') for c in channels]))

####################

    def help_package_search(self):
        print 'Usage: package_search PACKAGE|QUERY'
        print 'Example: package_search kernel-2.6.18-92'
        print
        print 'Advanced Search:'
        print 'Available Fields: name, epoch, version, release, arch, ' + \
              'description, summary'
        print 'Example: name:kernel AND version:2.6.18 AND -description:devel' 

    def do_package_search(self, args):
        if not len(self.args):
            self.help_package_search()
            return

        fields = ('name', 'epoch', 'version', 'release', 
                  'arch', 'description', 'summary')

        advanced = False
        for f in fields:
            if re.match(f + ':', args):
                advanced = True
                break

        if advanced:
            packages = self.client.packages.search.advanced(self.session, args)
        else:
            packages = self.client.packages.search.name(self.session, args)
       
        if len(packages): 
            print '\n'.join(self.build_package_names(packages))
        else:
            logging.warning('No packages found')

####################

    def help_kickstart_list(self):
        print 'Usage: kickstart_list'
    
    def do_kickstart_list(self, args, doreturn=False):
        kickstarts = self.client.kickstart.listKickstarts(self.session)
        kickstarts = [k.get('name') for k in kickstarts]

        if doreturn:
            return kickstarts
        else:
            print '\n'.join(sorted(kickstarts))

####################

    def help_kickstart_details(self):
        print 'Usage: kickstart_details PROFILE'

    def complete_kickstart_details(self, text, line, begidx, endidx):
        return self.tab_completer(self.do_kickstart_list('', True), text)
 
    def do_kickstart_details(self, args):
        if len(self.args) != 1:
            self.help_kickstart_details()
            return

        label = self.args[0]
        kickstart = None

        profiles = self.client.kickstart.listKickstarts(self.session)
        for p in profiles:
            if p.get('label') == label:
                kickstart = p
                break

        if not kickstart:
            logging.warning('No Kickstart profile found')
            return

        act_keys = \
            self.client.kickstart.profile.keys.getActivationKeys(self.session, 
                                                                 label)

        variables = self.client.kickstart.profile.getVariables(self.session, 
                                                               label)

        tree = \
            self.client.kickstart.tree.getDetails(self.session,
                                                  kickstart.get('tree_label'))

        base_channel = \
            self.client.channel.software.getDetails(self.session,
                                                    tree.get('channel_id'))

        child_channels = \
            self.client.kickstart.profile.getChildChannels(self.session,
                                                           label)

        custom_options = \
            self.client.kickstart.profile.getCustomOptions(self.session,
                                                           label)

        advanced_options = \
            self.client.kickstart.profile.getAdvancedOptions(self.session, 
                                                             label)

        config_manage = \
            self.client.kickstart.profile.system.checkConfigManagement(\
                self.session, label)

        remote_commands = \
            self.client.kickstart.profile.system.checkRemoteCommands(\
                self.session, label)

        partitions = \
            self.client.kickstart.profile.system.getPartitioningScheme(\
                self.session, label)

        crypto_keys = \
            self.client.kickstart.profile.system.listKeys(self.session,
                                                          label)

        file_preservations = \
            self.client.kickstart.profile.system.listFilePreservations(\
                self.session, label)

        software = self.client.kickstart.profile.software.getSoftwareList(\
                self.session, label)

        scripts = self.client.kickstart.profile.listScripts(self.session,
                                                            label)

        print 'Name:        %s' % kickstart.get('name')
        print 'Label:       %s' % kickstart.get('label')
        print 'Tree:        %s' % kickstart.get('tree_label')
        print 'Active:      %s' % str(kickstart.get('active'))
        print 'Advanced:    %s' % str(kickstart.get('advanced_mode'))
        print 'Org Default: %s' % str(kickstart.get('org_default'))

        print 
        print 'Config Management: %s' % str(config_manage)
        print 'Remote Commands:   %s' % str(remote_commands)

        print
        print 'Software Channels:'
        print '  %s' % base_channel.get('label')

        for channel in sorted(child_channels):
            print '    |-- %s' % channel

        if len(advanced_options):
            print
            print 'Advanced Options:'
            for o in sorted(advanced_options, key=itemgetter('name')):
                if o.get('arguments'):
                    print '  %s  %s' % (o.get('name'), o.get('arguments'))

        if len(custom_options):
            print
            print 'Custom Options:'
            for o in sorted(custom_options, key=itemgetter('arguments')):
                print '  %s' % re.sub('\n', '', o.get('arguments'))

        if len(partitions):
            print
            print 'Partitioning:'
            for line in partitions:
                print '  %s' % line

        print 
        print 'Software:'
        for s in software:
            print '  %s' % s

        if len(act_keys):
            print
            print 'Activation Keys:'
            for k in sorted(act_keys, key=itemgetter('key')):
                print '  %s' % k.get('key')

        if len(crypto_keys):
            print
            print 'Crypto Keys:'
            for k in sorted(crypto_keys, key=itemgetter('description')):
                print '  %s' % k.get('description')

        if len(file_preservations):
            print
            print 'File Preservations:'
            for fp in sorted(file_preservations, key=itemgetter('name')):
                print '  %s' % fp.get('name')
                for file in sorted(fp.get('file_names')):
                    print '    |-- %s' % file

        if len(variables):
            print
            print 'Variables:'
            for k in sorted(variables.keys()):
                print '  %s=%s' %(k, str(variables[k]))

        if len(scripts):
            print
            print 'Scripts:'

            add_separator = False

            for s in scripts:
                if add_separator:
                    print self.SEPARATOR

                add_separator = True

                print '  Type:        %s' % s.get('script_type')
                print '  Chroot:      %s' % str(s.get('chroot'))
            
                if s.get('interpreter'):
                    print '  Interpreter: %s' % s.get('interpreter')

                print
                print s.get('contents')
 
####################

    def help_kickstart_raw(self):
        print 'Usage: kickstart_raw LABEL'
    
    def complete_kickstart_raw(self, text, line, begidx, endidx):
        return self.tab_completer(self.do_kickstart_list('', True), text)

    def do_kickstart_raw(self, args, doreturn=False):
        url = 'http://%s/ks/cfg/label/%s' %(self.server, self.args[0])

        try:
            logging.debug('Retreiving %s' % url)
            response = urllib2.urlopen(url) 
            kickstart = response.read()
        except urllib2.HTTPError:
            logging.error(sys.exc_info()[1])
            logging.error('Could not retreive the Kickstart file')
            return

        # the value returned here is uninterpreted by Cobbler
        # which makes it useless
        #kickstart = \
        #    self.client.kickstart.profile.downloadKickstart(self.session,
        #                                                    self.args[0],
        #                                                    self.server)

        print kickstart

####################

    def help_kickstart_listsnippets(self):
        print 'Usage: kickstart_listsnippets'
    
    def do_kickstart_listsnippets(self, args, doreturn=False):
        snippets = self.client.kickstart.snippet.listCustom(self.session)
        snippets = [s.get('name') for s in snippets]

        if doreturn:
            return snippets
        else:
            print '\n'.join(sorted(snippets))

####################
 
    def help_kickstart_snippetdetails(self):
        print 'Usage: kickstart_snippetdetails SNIPPET ...'

    def complete_kickstart_snippetdetails(self, text, line, begidx, endidx):
        return self.tab_completer(self.do_kickstart_listsnippets('', True),
                                  text)
             
    def do_kickstart_snippetdetails(self, args):
        if not len(self.args):
            self.help_kickstart_snippetdetails()
            return

        add_separator = False

        snippets = self.client.kickstart.snippet.listCustom(self.session)
        
        for name in self.args:
            for s in snippets:
                if s.get('name') == name:
                    snippet = s
                    break

            if not snippet:
                logging.warning(name + ' is not a valid snippet')
                continue                

            if add_separator:
                print self.SEPARATOR
            
            add_separator = True

            print 'Name:   %s' % snippet.get('name')
            print 'Macro:  %s' % snippet.get('fragment')
            print 'File:   %s' % snippet.get('file')

            print
            print snippet.get('contents')

####################

    def help_errata_details(self):
        print 'Usage: errata_details NAME ...'

    def complete_errata_details(self, text, line, begidx, endidx):
        return self.tab_completer(self.do_errata_list('', True), text)
 
    def do_errata_details(self, args):
        if not len(self.args):
            self.help_errata_details()
            return

        name = self.args[0]

        add_separator = False

        for errata in self.args:
            try:
                details = self.client.errata.getDetails(self.session, name)

                packages = self.client.errata.listPackages(self.session, name)

                channels = \
                    self.client.errata.applicableToChannels(self.session, name)
            except:
                logging.warning(name + ' is not a valid errata')
                logging.debug(sys.exc_info())
                continue
     
            if add_separator:
                print self.SEPARATOR
            
            add_separator = True

            print 'Name:       %s' % name
            print
            print 'Product:    %s' % details.get('product')
            print 'Type:       %s' % details.get('type')
            print 'Issue Date: %s' % details.get('issue_date')
            print
            print 'Topic: '
            print '\n'.join(wrap(details.get('topic')))
            print
            print 'Description: '
            print '\n'.join(wrap(details.get('description')))

            if details.get('notes'):
                print
                print 'Notes:'
                print '\n'.join(wrap(details.get('notes')))

            print 
            print 'Solution:'
            print '\n'.join(wrap(details.get('solution')))
            print
            print 'References:'
            print '\n'.join(wrap(details.get('references')))
            print
            print 'Affected Channels:'
            print '\n'.join(sorted([c.get('label') for c in channels]))
            print
            print 'Affected Packages:'
            print '\n'.join(sorted(self.build_package_names(packages)))
     

####################

    def help_errata_findcve(self):
        print 'Usage: errata_findcve CVE ...'
    
    def do_errata_findcve(self, args, doreturn=False):
        if not len(self.args):
            self.help_errata_findcve
            return

        add_separator = False           

        for query in self.args:
            errata = self.client.errata.findByCve(self.session, query)
           
            if add_separator: print self.SEPARATOR
            add_separator = True 
 
            print 'Query: %s' % query
            print
            print 'Results:'
            
            if len(errata):
                map(self.print_errata_summary, errata)
            else:
                print 'None'

####################

    def help_system_list(self):
        print 'Usage: system_list'
    
    def do_system_list(self, args, doreturn=False):
        systems = self.client.system.listSystems(self.session)
        systems = [s.get('name') for s in systems]

        if doreturn:
            return systems
        else:
            print '\n'.join(sorted(systems))

####################

    def help_system_search(self):
        print 'Usage: system_search QUERY'
        print
        print 'Available Fields: id, name, ip, hostname, ' + \
              'device, vendor, driver'
        print 'Example: system_search vendor:vmware' 
    
    def do_system_search(self, args, doreturn=False):
        if (len(self.args)) != 1:
            self.help_system_search()
            return

        if re.search(':', args):
            try:
                (field, value) = args.split(':')
            except ValueError:
                logging.error('Invalid query')
                return []
        else:
            field = 'name'
            value = args

        if not value:
            logging.warning('Invalid query')
            return []

        results = []
        if field == 'name':
            results = self.client.system.search.nameAndDescription(self.session,
                                                                   value)  
            key = 'name' 
        elif field == 'id':
            results = self.client.system.listSystems(self.session)
            key = 'id' 
        elif field == 'ip':
            results = self.client.system.search.ip(self.session, value)
            key = 'ip'
        elif field == 'hostname':
            results = self.client.system.search.hostname(self.session, value)
            key = 'hostname'
        elif field == 'device':
            results = self.client.system.search.deviceDescription(self.session,
                                                                  value)
            key = 'hw_description'
        elif field == 'vendor':
            results = self.client.system.search.deviceVendorId(self.session,
                                                               value)
            key = 'hw_vendor_id'
        elif field == 'driver':
            results = self.client.system.search.deviceDriver(self.session,
                                                             value)
            key = 'hw_driver'
        else:
            logging.warning('Invalid search field')
            return []

        # only get real matches, not the fuzzy ones we get back
        systems = []
        for s in results:
            if re.search(value, str(s.get(key)), re.IGNORECASE):
                systems.append(s.get('name'))

        if doreturn:
            return systems
        else:
            if (len(systems)):
                print '\n'.join(sorted(systems))
            else:
                logging.warning('No systems found')

####################

    def help_schedule_script(self):
        print 'Usage: schedule_script SSM|SYSTEM ...'
        print
        print 'Start Time Examples:'
        print 'now  -> right now!'
        print '15m  -> 15 minutes from now'
        print '1d   -> 1 day from now'

    def complete_schedule_script(self, text, line, begidx, endidx):
        return self.tab_completer(self.do_system_list('', True), text)
 
    def do_schedule_script(self, args):
        if not len(self.args):
            self.help_schedule_script()
            return

        # use the systems listed in the SSM
        if self.args[0].lower() == 'ssm':
            systems = self.ssm
        else:
            systems = self.args

        if not len(systems):
            logging.warning('No systems selected')
            return

        user    = self.prompt_user('User [root]: ')
        group   = self.prompt_user('Group [root]: ')
        timeout = self.prompt_user('Timeout (in seconds) [600]: ')
        time    = self.prompt_user('Start Time [now]: ')
        script_file  = self.prompt_user('Script File [create]: ')

        # defaults
        if not user:        user        = 'root'
        if not group:       group       = 'root'
        if not timeout:     timeout     = 600

        # convert the time input to xmlrpclib.DateTime
        time = self.parse_time_input(time)

        if script_file:
            keep_script_file = True
            
            script_file = os.path.abspath(script_file)

            try:
                file = open(script_file, 'r')
                script = file.read()
                file.close()
            except:
                logging.error('Could not read %s' % script_file)
                logging.error(sys.exc_info()[1])
                logging.debug(sys.exc_info())
                return
        else:
            keep_script_file = False

            # have the user put the script into that file
            (script, script_file) = self.editor('#!/bin/bash\n')

        if not script:
            logging.error('No script provided')
            return

        # display a summary
        print
        print 'User:       %s' % user
        print 'Group:      %s' % group
        print 'Timeout:    %s' % str(timeout) + ' seconds'
        print 'Start Time: %s' % re.sub('T', ' ', time.value)
        print
        print script
        print

        # have the user confirm
        if not self.user_confirm():
            return

        scheduled = 0
        for system in systems:
            system_id = self.get_system_id(system)
            if not system_id: return

            # the current API forces us to schedule each system individually
            id = self.client.system.scheduleScriptRun(self.session,
                                                      system_id,
                                                      user,
                                                      group,
                                                      timeout,
                                                      script,
                                                      time)

            logging.info('Schedule ID: %s' % str(id))
            scheduled += 1

        print 'Scheduled: %s systems' % str(scheduled)

        if not keep_script_file:
            try:
                os.remove(script_file)
            except:
                logging.error('Could not remove %s' % script_file)
                logging.error(sys.exc_info()[1])
                logging.debug(sys.exc_info())

####################

    def help_system_hardware(self):
        print 'Usage: system_hardware SSM|SYSTEM ...'

    def complete_system_hardware(self, text, line, begidx, endidx):
        return self.tab_completer(self.do_system_list('', True), text)
 
    def do_system_hardware(self, args):
        if not len(self.args):
            self.help_system_details()
            return

        add_separator = False

        # use the systems listed in the SSM
        if self.args[0].lower() == 'ssm':
            systems = self.ssm
        else:
            systems = self.args

        for system in sorted(systems):
            system_id = self.get_system_id(system)
            if not system_id: return

            cpu = self.client.system.getCpu(self.session, system_id)
            memory = self.client.system.getMemory(self.session, system_id)
            devices = self.client.system.getDevices(self.session, system_id)
            network = self.client.system.getNetworkDevices(self.session, 
                                                           system_id)
          
            # Solaris systems don't have these value s
            for v in ('cache', 'vendor', 'family', 'stepping'):
                if not cpu.get(v):
                    cpu[v] = ''
 
            try:
                dmi = self.client.system.getDmi(self.session, system_id)
            except xml.parsers.expat.ExpatError:
                dmi = None
            
            if add_separator:
                print self.SEPARATOR

            add_separator = True

            if len(systems) > 1:
                print 'System: %s' % system
                print

            if len(network):
                print 'Network:'

                count = 0
                for device in network:
                    if count: print
                    count += 1

                    print '  Interface:   %s' % device.get('interface')
                    print '  MAC Address: %s' % (
                                 device.get('hardware_address').upper())
                    print '  IP Address:  %s' % device.get('ip')
                    print '  Netmask:     %s' % device.get('netmask')
                    print '  Broadcast:   %s' % device.get('broadcast')
                    print '  Module:      %s' % device.get('module')
                print

            print 'CPU:'
            print '  Count:    %s' % str(cpu.get('count'))
            print '  Arch:     %s' % cpu.get('arch')
            print '  MHz:      %s' % cpu.get('mhz')
            print '  Cache:    %s' % cpu.get('cache')
            print '  Vendor:   %s' % cpu.get('vendor')
            print '  Model:    %s' % re.sub('\s+', ' ', cpu.get('model'))
            print '  Family:   %s' % cpu.get('family')
            print '  Stepping: %s' % cpu.get('stepping')

            print
            print 'Memory:'
            print '  RAM:  %s' % str(memory.get('ram'))
            print '  Swap: %s' % str(memory.get('swap'))

            if dmi:
                print
                print 'DMI:'
                print '  Vendor:       %s' % dmi.get('vendor')
                print '  System:       %s' % dmi.get('system')
                print '  Product:      %s' % dmi.get('product')
                print '  Board:        %s' % dmi.get('board')

                print
                print '  Asset:'
                for asset in dmi.get('asset').split(') ('):
                    print '    %s' % re.sub('\)|\(', '', asset)

                print
                print '  BIOS Release: %s' % dmi.get('bios_release')
                print '  BIOS Vendor:  %s' % dmi.get('bios_vendor')
                print '  BIOS Version: %s' % dmi.get('bios_version')

            if len(devices):
                print
                print 'Devices:'

                count = 0
                for device in devices:
                    if count: print
                    count += 1
    
                    print '  Description: %s' % (
                             wrap(device.get('description'), 60)[0])
                    print '  Driver:      %s' % device.get('driver')
                    print '  Class:       %s' % device.get('device_class')
                    print '  Bus:         %s' % device.get('bus')

####################

    def help_system_availableupgrades(self):
        print 'Usage: system_availableupgrades SSM|SYSTEM ...'

    def complete_system_availableupgrades(self, text, line, begidx, endidx):
        return self.tab_completer(self.do_system_list('', True), text)
 
    def do_system_availableupgrades(self, args):
        if not len(self.args):
            self.help_system_availableupgrades()
            return

        add_separator = False

        # use the systems listed in the SSM
        if self.args[0].lower() == 'ssm':
            systems = self.ssm
        else:
            systems = self.args

        for system in sorted(systems):
            system_id = self.get_system_id(system)
            if not system_id: return

            packages = \
                self.client.system.listLatestUpgradablePackages(self.session,
                                                                system_id)

            if add_separator:
                print self.SEPARATOR

            add_separator = True

            if len(systems) > 1:
                print 'System: %s' % system
                print

            for package in sorted(packages, key=itemgetter('name')):
                old = {'name'    : package.get('name'),
                       'version' : package.get('from_version'),
                       'release' : package.get('from_release'),
                       'epoch'   : package.get('from_epoch')}

                new = {'name'    : package.get('name'),
                       'version' : package.get('to_version'),
                       'release' : package.get('to_release'),
                       'epoch'   : package.get('to_epoch')}

                print 'Old: %s' % self.build_package_names(old)
                print 'New: %s' % self.build_package_names(new)
                print

####################

    def help_system_packages(self):
        print 'Usage: system_packages SSM|SYSTEM ...'

    def complete_system_packages(self, text, line, begidx, endidx):
        return self.tab_completer(self.do_system_list('', True), text)
 
    def do_system_packages(self, args):
        if not len(self.args):
            self.help_system_packages()
            return

        add_separator = False

        # use the systems listed in the SSM
        if self.args[0].lower() == 'ssm':
            systems = self.ssm
        else:
            systems = self.args

        for system in sorted(systems):
            system_id = self.get_system_id(system)
            if not system_id: return

            packages = self.client.system.listPackages(self.session,
                                                       system_id)

            if add_separator:
                print self.SEPARATOR

            add_separator = True

            if len(systems) > 1:
                print 'System: %s' % system
                print

            print '\n'.join(self.build_package_names(packages))

####################

    def help_system_details(self):
        print 'Usage: system_details SSM|SYSTEM ...'

    def complete_system_details(self, text, line, begidx, endidx):
        return self.tab_completer(self.do_system_list('', True), text)
 
    def do_system_details(self, args):
        if not len(self.args):
            self.help_system_details()
            return

        add_separator = False

        # use the systems listed in the SSM
        if self.args[0].lower() == 'ssm':
            systems = self.ssm
        else:
            systems = self.args

        for system in sorted(systems):
            system_id = self.get_system_id(system)
            if not system_id: return

            last_checkin = \
                self.client.system.getName(self.session,
                                           system_id).get('last_checkin')

            details = self.client.system.getDetails(self.session, system_id)

            registered = self.client.system.getRegistrationDate(self.session,
                                                                system_id)
            
            entitlements = self.client.system.getEntitlements(self.session,
                                                              system_id)

            base_channel = \
                self.client.system.getSubscribedBaseChannel(self.session,
                                                            system_id)

            child_channels = \
                self.client.system.listSubscribedChildChannels(self.session,
                                                               system_id)

            groups = self.client.system.listGroups(self.session,
                                                   system_id)

            kernel = self.client.system.getRunningKernel(self.session,
                                                         system_id)
           
            network = self.client.system.getNetwork(self.session, system_id)

            keys = self.client.system.listActivationKeys(self.session,
                                                         system_id)

            ranked_config_channels = []
            if 'provisioning_entitled' in entitlements:
                config_channels = \
                    self.client.system.config.listChannels(self.session,
                                                           system_id)

                for channel in config_channels:
                    ranked_config_channels.append(channel.get('label'))
       
            if add_separator:
                print self.SEPARATOR

            add_separator = True

            print 'Name:          %s' % system
            print 'System ID:     %s' % str(system_id)
            print 'Locked:        %s' % str(details.get('lock_status'))
            print 'Registered:    %s' % re.sub('T', ' ', registered.value)
            print 'Last Checkin:  %s' % re.sub('T', ' ', last_checkin.value)
            print 'OSA Status:    %s' % details.get('osa_status')

            print
            print 'Hostname:      %s' % network.get('hostname')
            print 'IP Address:    %s' % network.get('ip')
            print 'Kernel:        %s' % kernel

            if len(keys):
                print
                print 'Activation Keys:'
                for key in keys:
                    print '  %s' % key

            print
            print 'Software Channels:'
            print '  %s' % base_channel.get('label')

            for channel in child_channels:
                print '    |-- %s' % channel.get('label')

            if len(ranked_config_channels):
                print
                print 'Configuration Channels:'
                for channel in ranked_config_channels:
                    print '  %s' % channel

            print
            print 'Entitlements:'
            for entitlement in sorted(entitlements):
                print '  %s' % self.ENTITLEMENTS[entitlement]

            if len(groups):
                print
                print 'System Groups:'
                for group in groups:
                    if group.get('subscribed') == 1:
                        print '  %s' % group.get('system_group_name')

####################

    def help_system_errata(self):
        print 'Usage: system_errata SSM|SYSTEM ...'
    
    def complete_system_errata(self, text, line, begidx, endidx):
        return self.tab_completer(self.do_system_list('', True), text)
 
    def do_system_errata(self, args):
        if not len(self.args):
            self.do_help_system_errata()
            return

        add_separator = False

        # use the systems listed in the SSM
        if self.args[0].lower() == 'ssm':
            systems = self.ssm
        else:
            systems = self.args

        for system in sorted(systems):
            system_id = self.get_system_id(system)
            if not system_id: return

            if len(systems) > 1:
                print 'System: %s' % system
                print

            errata = self.client.system.getRelevantErrata(self.session,
                                                          system_id)

            self.print_errata_list(errata)

            if add_separator:
                print self.SEPARATOR

            add_separator = True

####################

    def help_group_list(self):
        print 'Usage: group_list'

    def do_group_list(self, args, doreturn=False):
        groups = self.client.systemgroup.listAllGroups(self.session)
        groups = [g.get('name') for g in groups]

        if doreturn:
            return groups
        else:
            print '\n'.join(sorted(groups))

####################

    def help_group_listsystems(self):
        print 'Usage: group_listsystems GROUP'

    def complete_group_listsystems(self, text, line, begidx, endidx):
        return self.tab_completer(self.do_group_list('', True), text)
 
    def do_group_listsystems(self, args, doreturn=False):
        if not len(self.args):
            self.help_group_listsystems()
            return

        group = args

        try:
            systems = self.client.systemgroup.listSystems(self.session,
                                                          group)
            
            systems = [s.get('profile_name') for s in systems]
        except:
            logging.warning(group + ' is not a valid group')
            logging.debug(sys.exc_info())
            return []
 
        if doreturn:
            return systems
        else:
            print '\n'.join(sorted(systems))
     
####################
 
    def help_group_details(self):
        print 'Usage: group_details GROUP ...'

    def complete_group_details(self, text, line, begidx, endidx):
        return self.tab_completer(self.do_group_list('', True), text)
 
    def do_group_details(self, args):
        if not len(self.args):
            self.help_group_details()
            return

        add_separator = False

        for group in self.args:
            try:
                details = self.client.systemgroup.getDetails(self.session, 
                                                             group)
            
                systems = self.client.systemgroup.listSystems(self.session,
                                                              group)
            
                systems = [s.get('profile_name') for s in systems]
            except:
                logging.warning(key + ' is not a valid group')
                logging.debug(sys.exc_info())
                return
     
            if add_separator:
                print self.SEPARATOR
            
            add_separator = True

            print 'Name               %s' % details.get('name')
            print 'Description:       %s' % details.get('description')
            print 'Number of Systems: %s' % str(details.get('system_count'))

            print
            print 'Members:'
            print '  \n'.join(sorted(systems))

####################

    def help_schedule_cancel(self):
        print 'Usage: schedule_cancel ID|* ...'

    def complete_schedule_cancel(self, text, line, begidx, endidx):
        return self.tab_completer(self.do_schedule_listpending('', True), 
                                  text)
 
    def do_schedule_cancel(self, args):
        if not len(self.args):
            self.help_schedule_cancel()
            return

        # cancel all actions
        if '.*' in self.args:
            prompt = 'Do you really want to cancel all pending actions?'

            if self.user_confirm(prompt):
                strings = self.do_schedule_listpending('', True)
            else:
                return
        else:
            strings = self.args

        # convert strings to integers
        actions = []
        for a in strings:
            try:
                actions.append(int(a))
            except ValueError:
                logging.warning(str(a) + ' is not a valid ID')
                continue

        self.client.schedule.cancelActions(self.session, actions)

        for a in actions:
            logging.info('Canceled action %s' % str(a))

        print 'Canceled %s actions' % str(len(actions))

####################

    def help_schedule_summary(self):
        print 'Usage: schedule_summary ID'

    def do_schedule_summary(self, args):
        if not len(self.args):
            self.help_schedule_summary()
            return

        try:
            id = int(self.args[0])
        except:
            logging.warning(str(a) + ' is not a valid ID')
            return

        completed = self.client.schedule.listCompletedSystems(self.session, id)
        failed = self.client.schedule.listFailedSystems(self.session, id)
        pending = self.client.schedule.listInProgressSystems(self.session, id)

        # schedule.getAction() API call would make this easier
        all_actions = self.client.schedule.listAllActions(self.session)
        action = None
        for a in all_actions:
            if a.get('id') == id:
                action = a
                del all_actions
                break
 
        self.print_action_summary(action)

        if len(completed):
            print
            print 'Completed Systems:'
            for s in completed:
                print '  %s' % s.get('server_name')

        if len(failed):
            print
            print 'Failed Systems:'
            for s in failed:
                print '  %s' % s.get('server_name')

        if len(pending):
            print
            print 'Pending Systems:'
            for s in pending:
                print '  %s' % s.get('server_name')

        print
        print 'Completed: %s' % str(len(completed))
        print 'Failed:    %s' % str(len(failed))
        print 'Pending:   %s' % str(len(pending))

####################

    def help_schedule_rawoutput(self):
        print 'Usage: schedule_rawoutput ID'

    def do_schedule_rawoutput(self, args):
        if not len(self.args):
            self.help_schedule_output()
            return
        elif len(self.args) > 1:
            systems = self.args[1:]
        else:
            systems = []

        try:
            id = int(self.args[0])
        except:
            logging.warning(str(a) + ' is not a valid ID')
            return
        
        # schedule.getAction() API call would make this easier
        all_actions = self.client.schedule.listAllActions(self.session)
        action = None
        for a in all_actions:
            if a.get('id') == id:
                action = a
                del all_actions
                break

        results = self.client.system.getScriptResults(self.session, id)

        add_separator = False

        for r in results:
            if add_separator:
                print self.SEPARATOR

            add_separator = True

            print 'System:      %s' % 'UNKNOWN'
            print 'Start Time:  %s' % re.sub('T', ' ', r.get('startDate').value)
            print 'Stop Time:   %s' % re.sub('T', ' ', r.get('stopDate').value)
            print 'Return Code: %s' % str(r.get('returnCode'))

            print
            print r.get('output')

####################

    def help_schedule_listpending(self):
        print 'Usage: schedule_listpending [LIMIT]'
    
    def do_schedule_listpending(self, args, doreturn=False):
        actions = self.client.schedule.listInProgressActions(self.session)

        if not len(actions): return

        if doreturn:
            return [str(a.get('id')) for a in actions]
        else:
            try:
                limit = int(self.args[0])
            except:
                limit = len(actions)
    
            add_separator = False
    
            for i in range(0, limit):
                if add_separator:
                    print self.SEPARATOR
                
                add_separator = True
    
                systems = self.client.schedule.listInProgressSystems(\
                              self.session, actions[i].get('id'))
    
                self.print_action_summary(actions[i], systems)
            
####################

    def help_schedule_listcompleted(self):
        print 'Usage: schedule_listcompleted [LIMIT]'
    
    def do_schedule_listcompleted(self, args, doreturn=False):
        actions = self.client.schedule.listCompletedActions(self.session)

        if not len(actions): return

        if doreturn:
            return [str(a.get('id')) for a in actions]
        else:
            try:
                limit = int(self.args[0])
            except:
                limit = len(actions)
    
            add_separator = False
    
            for i in range(0, limit):
                if add_separator:
                    print self.SEPARATOR
                
                add_separator = True
    
                systems = self.client.schedule.listCompletedSystems(\
                              self.session, actions[i].get('id'))
    
                self.print_action_summary(actions[i], systems)
            
####################

    def help_schedule_listfailed(self):
        print 'Usage: schedule_listfailed [LIMIT]'
    
    def do_schedule_listfailed(self, args, doreturn=False):
        actions = self.client.schedule.listFailedActions(self.session)

        if not len(actions): return

        if doreturn:
            return [str(a.get('id')) for a in actions]
        else:
            try:
                limit = int(self.args[0])
            except:
                limit = len(actions)
    
            add_separator = False
    
            for i in range(0, limit):
                if add_separator:
                    print self.SEPARATOR
                
                add_separator = True
    
                systems = self.client.schedule.listFailedSystems(\
                              self.session, actions[i].get('id'))
    
                self.print_action_summary(actions[i], systems)
            
####################

    def help_schedule_listarchived(self):
        print 'Usage: schedule_listarchived [LIMIT]'
    
    def do_schedule_listarchived(self, args, doreturn=False):
        actions = self.client.schedule.listArchivedActions(self.session)

        if not len(actions): return

        if doreturn:
            return [str(a.get('id')) for a in actions]
        else:
            try:
                limit = int(self.args[0])
            except:
                limit = len(actions)
    
            add_separator = False
    
            for i in range(0, limit):
                if add_separator:
                    print self.SEPARATOR
                
                add_separator = True
    
                self.print_action_summary(actions[i])
            
####################

    def help_cryptokey_list(self):
        print 'Usage: cryptokey_list'

    def do_cryptokey_list(self, args, doreturn=False):
        keys = self.client.kickstart.keys.listAllKeys(self.session)
        keys = [k.get('description') for k in keys]

        if doreturn:
            return keys
        else:
            print '\n'.join(sorted(keys))

####################
 
    def help_cryptokey_details(self):
        print 'Usage: cryptokey_details KEY ...'

    def complete_cryptokey_details(self, text, line, begidx, endidx):
        return self.tab_completer(self.do_cryptokey_list('', True), text)
 
    def do_cryptokey_details(self, args):
        if not len(self.args):
            self.help_cryptokey_details()
            return

        add_separator = False

        for key in self.args:
            try:
                details = self.client.kickstart.keys.getDetails(self.session, 
                                                                key)
            except:
                logging.warning(key + ' is not a valid crypto key')
                logging.debug(sys.exc_info())
                return
        
            if add_separator:
                print self.SEPARATOR
            
            add_separator = True

            print 'Description: %s' % details.get('description')
            print 'Type:        %s' % details.get('type')

            print
            print details.get('content')

####################

    def help_activationkey_list(self):
        print 'Usage: activationkey_list'

    def do_activationkey_list(self, args, doreturn=False):
        keys = self.client.activationkey.listActivationKeys(self.session)
        keys = [k.get('key') for k in keys]

        if doreturn:
            return keys
        else:
            print '\n'.join(sorted(keys))

####################

    def help_activationkey_listsystems(self):
        print 'Usage: activationkey_listsystems KEY'

    def complete_activationkey_listsystems(self, text, line, begidx, endidx):
        return self.tab_completer(self.do_activationkey_list('', True), text)

    def do_activationkey_listsystems(self, args):
        if not len(self.args):
            self.help_activationkey_listsystems()
            return

        try:
            systems = \
                self.client.activationkey.listActivatedSystems(self.session,
                                                           self.args[0])
        except:
            logging.warning(self.args[0] + ' is not a valid activation key')
            logging.debug(sys.exc_info())
            return
        
        systems = sorted([s.get('hostname') for s in systems])

        print '\n'.join(systems)

####################
 
    def help_activationkey_details(self):
        print 'Usage: activationkey_details KEY ...'

    def complete_activationkey_details(self, text, line, begidx, endidx):
        return self.tab_completer(self.do_activationkey_list('', True), text)
 
    def do_activationkey_details(self, args):
        if not len(self.args):
            self.help_activationkey_details()
            return

        add_separator = False

        for key in self.args:
            try:
                details = self.client.activationkey.getDetails(self.session, 
                                                               key)

                config_channels = \
                    self.client.activationkey.listConfigChannels(self.session, 
                                                                 key) 
            except:
                logging.warning(key + ' is not a valid activation key')
                logging.debug(sys.exc_info())
                return
     
            groups = []
            for group in details.get('server_group_ids'):
                group_details = self.client.systemgroup.getDetails(self.session,
                                                                   group)
                groups.append(group_details.get('name'))
        
            if add_separator:
                print self.SEPARATOR
            
            add_separator = True

            print 'Key:               %s' % details.get('key')
            print 'Description:       %s' % details.get('description')
            print 'Universal Default: %s' % str(details.get('universal_default'))

            print
            print 'Software Channels:'
            print '  %s' % details.get('base_channel_label')

            for channel in details.get('child_channel_labels'):
                print '   |-- %s' % channel

            print
            print 'Configuration Channels:'
            for channel in config_channels:
                print '  %s' % channel.get('label')

            print
            print 'Entitlements:'
            for entitlement in sorted(details.get('entitlements')):
                print '  %s' % self.ENTITLEMENTS[entitlement]

            print
            print 'System Groups:'
            for group in groups:
                print '  %s' % group

            print
            print 'Packages:'
            for package in details.get('packages'):
                name = package.get('name')

                if package.get('arch'):
                    name = name + '.%s' % package.get('arch')

                print '  %s' % name

####################

    def help_configchannel_list(self):
        print 'Usage: configchannel_list'

    def do_configchannel_list(self, args, doreturn=False):
        channels = self.client.configchannel.listGlobals(self.session)
        channels = [c.get('label') for c in channels]

        if doreturn:
            return channels
        else:
            print '\n'.join(sorted(channels))

####################

    def help_configchannel_listsystems(self):
        print 'Usage: configchannel_listsystems CHANNEL'

    def complete_configchannel_listsystems(self, text, line, begidx, endidx):
        return self.tab_completer(self.do_configchannel_list('', True), text)

    def do_configchannel_listsystems(self, args):
        print 'configchannel.listSubscribedSystems is not implemented'
        return

        if not len(self.args):
            self.help_configchannel_listsystems()
            return

        systems = \
            self.client.configchannel.listSubscribedSystems(self.session,
                                                            self.args[0])
        
        systems = sorted([s.get('name') for s in systems])

        print '\n'.join(systems)

####################

    def help_configchannel_listfiles(self):
        print 'Usage: configchannel_listfiles CHANNEL ...'

    def complete_configchannel_listfiles(self, text, line, begidx, endidx):
        return self.tab_completer(self.do_configchannel_list('', True), text)
 
    def do_configchannel_listfiles(self, args, doreturn=False):
        if not len(args):
            self.help_configchannel_listfiles()
            return []

        for channel in args.split():
            files = self.client.configchannel.listFiles(self.session,
                                                        channel)
            files = [f.get('path') for f in files]

            if doreturn:
                return files
            else:
                print '\n'.join(sorted(files))

####################
 
    def help_configchannel_filedetails(self):
        print 'Usage: configchannel_filedetails CHANNEL FILE ...'

    def complete_configchannel_filedetails(self, text, line, begidx, endidx):
        parts = line.split(' ')

        if len(parts) == 2:
            return self.tab_completer(self.do_configchannel_list('', True), 
                                      text)
        elif len(parts) > 2:
            return self.tab_completer(\
                self.do_configchannel_listfiles(parts[1], True), text)
        else:
            return []
             
    def do_configchannel_filedetails(self, args):
        if len(self.args) < 2:
            self.help_configchannel_filedetails()
            return

        add_separator = False

        channel = self.args[0]
        filenames = self.args[1:]

        # the server return a null exception if an invalid file is passed
        valid_files = self.do_configchannel_listfiles(channel, True)
        for f in filenames:
            if not f in valid_files:
                filenames.remove(f)
                logging.warning(f + ' is not in this configuration channel')
                continue

        files = self.client.configchannel.lookupFileInfo(self.session, 
                                                         channel,
                                                         filenames)

        for file in files:
            if add_separator:
                print self.SEPARATOR
            
            add_separator = True

            print 'File:     %s' % file.get('path')
            print 'Type:     %s' % file.get('type')
            print 'Revision: %s' % str(file.get('revision'))
            print 'Created:  %s' % re.sub('T', ' ', file.get('creation').value)
            print 'Modified: %s' % re.sub('T', ' ', file.get('modified').value)

            print
            print 'Owner:    %s' % file.get('owner')
            print 'Group:    %s' % file.get('group')
            print 'Mode:     %s' % file.get('permissions_mode')

            if file.get('type') == 'file':
                print 'MD5:      %s' % file.get('md5')
                print 'Binary:   %s' % str(file.get('binary'))

                if not file.get('binary'):
                    print
                    print file.get('contents')

####################
 
    def help_configchannel_details(self):
        print 'Usage: configchannel_details CHANNEL ...'

    def complete_configchannel_details(self, text, line, begidx, endidx):
        return self.tab_completer(self.do_configchannel_list('', True), text)
 
    def do_configchannel_details(self, args):
        if not len(self.args):
            self.help_configchannel_details()
            return

        add_separator = False

        for channel in self.args:
            details = self.client.configchannel.getDetails(self.session, 
                                                           channel)
      
            files = self.client.configchannel.listFiles(self.session,
                                                        channel)
 
            if add_separator:
                print self.SEPARATOR
            
            add_separator = True

            print 'Label:       %s' % details.get('label')
            print 'Name:        %s' % details.get('name')
            print 'Description: %s' % details.get('description')

            print
            print 'Files:'
            for file in files:
                print '  %s' % file.get('path')

####################

    def help_softwarechannel_list(self):
        print 'Usage: softwarechannel_list'

    def do_softwarechannel_list(self, args, doreturn=False):
        channels = self.client.channel.listAllChannels(self.session)
        channels = [c.get('label') for c in channels]

        if doreturn:
            return channels
        else:
            print '\n'.join(sorted(channels))
      
####################

    def help_softwarechannel_listsystems(self):
        print 'Usage: softwarechannel_listsystems CHANNEL'

    def complete_softwarechannel_listsystems(self, text, line, begidx, endidx):
        return self.tab_completer(self.do_softwarechannel_list('', True), text)

    def do_softwarechannel_listsystems(self, args):
        if not len(self.args):
            self.help_softwarechannel_listsystems()
            return

        systems = \
            self.client.channel.software.listSubscribedSystems(self.session,
                                                               self.args[0])
        
        systems = sorted([s.get('name') for s in systems])

        print '\n'.join(systems)

####################

    def help_softwarechannel_packages(self):
        print 'Usage: softwarechannel_packages CHANNEL [PACKAGE ...]'

    def complete_softwarechannel_packages(self, text, line, begidx, endidx):
        # only tab complete the channel name
        if len(line.split(' ')) == 2:
            return self.tab_completer(self.do_softwarechannel_list('', True), text)
        else:
            return []

    def do_softwarechannel_packages(self, args, doreturn=False):
        if not len(self.args):
            self.help_softwarechannel_packages()
            return

        packages = self.client.channel.software.listLatestPackages(self.session,
                                                                   self.args[0])

        packages = self.build_package_names(packages)

        if doreturn:
            return packages
        else:
            if len(self.args) > 1:
                packages = self.filter_results(packages, self.args[1:])

            print '\n'.join(sorted(packages)) 
            
####################
 
    def help_softwarechannel_details(self):
        print 'Usage: softwarechannel_details CHANNEL ...'

    def complete_softwarechannel_details(self, text, line, begidx, endidx):
        return self.tab_completer(self.do_softwarechannel_list('', True), text)
 
    def do_softwarechannel_details(self, args):
        if not len(self.args):
            self.help_softwarechannel_details()
            return

        add_separator = False

        for channel in self.args:
            details = self.client.channel.software.getDetails(self.session, 
                                                              channel)
      
            systems = \
                self.client.channel.software.listSubscribedSystems(self.session,                                                                   channel)
 
            trees = self.client.kickstart.tree.list(self.session, channel)

            if add_separator:
                print self.SEPARATOR

            add_separator = True

            print 'Label:              %s' % details.get('label')
            print 'Name:               %s' % details.get('name')
            print 'Architecture:       %s' % details.get('arch_name')
            print 'Parent:             %s' % details.get('parent_channel_label')
            print 'Systems Subscribed: %s' % str(len(systems))
            print
            print 'Summary:'
            print '\n'.join(wrap(details.get('summary')))
            print
            print 'Description:'
            print '\n'.join(wrap(details.get('description')))
            print 
            print 'GPG Key:            %s' % details.get('gpg_key_id')
            print 'GPG Fingerprint:    %s' % details.get('gpg_key_fp')
            print 'GPG URL:            %s' % details.get('gpg_key_url')

            if len(trees):
                print
                print 'Kickstart Trees:'
                for tree in trees:
                    print '  %s' % tree.get('label')

####################

    def help_softwarechannel_errata(self):
        print 'Usage: softwarechannel_errata CHANNEL ...'
    
    def complete_softwarechannel_errata(self, text, line, begidx, endidx):
        return self.tab_completer(self.do_softwarechannel_list('', True), text)
 
    def do_softwarechannel_errata(self, args):
        if not len(self.args):
            self.do_help_softwarechannel_errata()
            return

        channels = self.args

        add_separator = False

        for channel in sorted(channels):
            if len(channels) > 1:
                print 'Channel: %s' % channel
                print

            errata = self.client.channel.software.listErrata(self.session,
                                                             channel)

            self.print_errata_list(errata)

            if add_separator:
                print self.SEPARATOR

            add_separator = True

# vim:ts=4:expandtab:
