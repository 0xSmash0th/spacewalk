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

package RHN::DB::ProbeParam;

use Params::Validate qw/:all/;
Params::Validate::validation_options(strip_leading => '-');

use strict;
use Carp;
use Data::Dumper;
use RHN::DataSource::Simple;

our $VERSION = (split(/s+/, q$Id$, 4))[2];

#########
sub new {
#########
  my $class = shift;
  my %args  = @_;
  my $self  = {};
  bless($self, $class);

  foreach my $arg (keys %args) {
    $self->$arg($args{$arg});
  }

  return $self;
}


############
sub create {
############
  my $class = shift;
  my $self = bless { }, $class;
  return $self;
}

#############################
sub list_probe_param_values {
#############################
  my $class = shift;
  my $probe_id = shift;
  my $command_id = shift;

  my $ds = new RHN::DataSource::Simple(-querybase => "probe_queries",
				       -mode => "params_for_probe");

  return $ds->execute_query(-probe_id => $probe_id, -command_id => $command_id);
}


1;

__END__
=head1 NAME

RHN::DB::ProbeParam - Monitoring probe parameters

=head1 SYNOPSIS

  use RHN::DB::ProbeParam;

  # Get a list of probe parameters
  my $params = RHN::DB::ProbeParam->list_probe_param_values($pid, $cid);

=head1 DESCRIPTION

RHN::DB::ProbeParam provides access to monitoring probe parameters
(the RHN_PROBE_PARAMS table);

=head1 CLASS METHODS

=over 8

=item new()

Creates a new RHN::DB::ProbeParam object

=item I<list_probe_param_values>($probe_id, $command_id)

Lists probe param values given a probe ID and a command ID.

=back

=head1 SEE ALSO

L<RHN::DB::Probe>, L<RHN::DB::Command>

=head1 COPYRIGHT

Copyright (c) 2004-2005, Red Hat, Inc.  All rights reserved

=cut


