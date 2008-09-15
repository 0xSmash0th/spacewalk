#!/usr/bin/perl
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
#
# $Id$

use strict;
use warnings;

use Getopt::Long;
use English;
use RHN::SatInstall;

use File::Spec;
use File::Copy;
use IPC::Open3;
use Symbol qw(gensym);

my $ORACLE_HOME = qx{dbhome '*'};
$ENV{PATH} .= ":$ORACLE_HOME/bin";

my $usage = "usage: $0 --dsn=<dsn> --schema-deploy-file=<filename>"
  . " [ --log=<logfile> ] [ --clear-db ] [ --nofork ] [ --help ]\n";

my $dsn = '';
my $schema_deploy_file = '';
my $log_file = '/var/log/rhn/populate_db.log';
my $clear_db = 0;
my $nofork = 0;
my $help = '';

GetOptions("dsn=s" => \$dsn, "schema-deploy-file=s" => \$schema_deploy_file,
	   "log=s" => \$log_file, "help" => \$help, "clear-db" => \$clear_db,
	   nofork => \$nofork);

if ($help or not ($dsn and $schema_deploy_file)) {
  die $usage;
}

my $lockfile = '/var/lock/subsys/rhn-satellite-db-population';
if (-e $lockfile) {
  warn "lock file $lockfile present...database population already in progress\n";
  exit 100;
}

system('/bin/touch', $lockfile);

# Move the old log file out of the way - prefork to avoid race
# condition
if (-e $log_file) {
  my $backup_file = get_next_backup_filename($log_file);
  my $success = File::Copy::move($log_file, $backup_file);

  unless ($success) {
    system('/bin/rm', $lockfile);

    die "Error moving log file '$log_file' to '$backup_file': $OS_ERROR";
  }
}

my $pid;

unless ($nofork) {
  $pid = fork();
}

# The parent process will exit so the child can do the work without
# blocking the web UI.
if ($pid) {
  exit 0;
}

if ($clear_db) {
  RHN::SatInstall->clear_db();
}

local *LOGFILE;
open(LOGFILE, ">", $log_file) or die "Error writing log file '$log_file': $OS_ERROR";
$pid = open3(gensym, ">&LOGFILE", ">&LOGFILE", 'sqlplus', $dsn, "\@$schema_deploy_file");
waitpid($pid, 0);

system('/bin/rm', $lockfile);

exit 0;

sub get_next_backup_filename {
  my ($vol, $dir, $filename) = File::Spec->splitpath($log_file);
  my $index = 0;
  my $backup_file;

  do {
    $index++;
    $backup_file = File::Spec->catfile($dir, $filename . ".$index");
  } while (-e $backup_file);

  return $backup_file;
}
