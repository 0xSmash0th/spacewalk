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

use strict;
package RHN::Entitlements;
use Carp qw/croak confess/;

use RHN::DB::Entitlements;

our @ISA = qw/RHN::DB::Entitlements/;

my %updates_features = map { $_ => 1 }
  qw/ftr_package_updates ftr_errata_updates ftr_hardware_refresh
     ftr_package_refresh ftr_package_remove ftr_auto_errata_updates/;

my %management_features = map { $_ => 1 }
  qw/ftr_system_grouping ftr_package_verify ftr_profile_compare
     ftr_proxy_capable ftr_sat_capable ftr_reboot
     ftr_satellite_applet ftr_osa_bus/;

my %provisioning_features = map { $_ => 1 }
  qw/ftr_kickstart ftr_config ftr_custom_info ftr_delta_action
     ftr_snapshotting ftr_agent_smith ftr_remote_command/;

my %monitoring_features = map { $_ => 1 }
  qw/ftr_schedule_probe ftr_probes/;

my %nonlinux_features = map { $_ => 1 }
  qw/ftr_nonlinux_support/;

# all features are the unique features in other FOO_features arrays
my %feature_universe = (%updates_features, %management_features,
			%provisioning_features, %nonlinux_features,
			%monitoring_features);

my %entitlement_feature_map =
  ( none => { },
    sw_mgr_entitled => { map { $_ => 1 } (keys %updates_features) },
    enterprise_entitled => { map { $_ => 1 } (keys %updates_features,
					      keys %management_features) },
    provisioning_entitled => { map { $_ => 1 } (keys %updates_features,
						keys %management_features,
						keys %provisioning_features,
# When we add monitoring entitlements back for systems, remove this line:
					        keys %monitoring_features) },
    monitoring_entitled => { map { $_ => 1 } (keys %monitoring_features) },
    nonlinux_entitled => { map { $_ => 1 } (keys %updates_features,
					    keys %management_features,
					    keys %provisioning_features,
					    keys %nonlinux_features) },
  );

my %excluded_features = (nonlinux_entitled => { map { $_ => 1 }
						qw/ftr_errata_updates ftr_proxy_capable ftr_sat_capable ftr_reboot
						   ftr_kickstart ftr_delta_action/ });

my %grant_map =
  ( none => [  ],
    sw_mgr_entitled => [ 'updates' ],
    enterprise_entitled => [ 'updates', 'management', 'monitoring' ],
    provisioning_entitled => [ 'updates', 'management', 'provisioning', 'monitoring' ],
    monitoring_entitled => [ 'monitoring' ],
    nonlinux_entitled => [ 'updates', 'management', 'provisioning', 'nonlinux' ]
  );
my %valid_services = map { $_ => 1 } qw/updates management provisioning monitoring nonlinux/;

sub entitlement_grants_service { # Need to refactor Tokens to remove this entirely
  my $class = shift;
  my $entitlement = shift;
  my $desired_service = shift;

  return 0 unless defined $desired_service and defined $entitlement;

  croak "Invalid entitlement type '$entitlement'" unless exists $grant_map{$entitlement};
  croak "Invalid service level '$desired_service'" unless exists $valid_services{$desired_service};

  return 1 if grep { $_ eq $desired_service } @{$grant_map{$entitlement}};

  return 0;
}

# given an org and a default entitlement, return the
# selectbox-suitable choice array
sub org_entitlement_choices {
  my $class = shift;
  my $org = shift;
  my $current_ent = shift;

  my @ret;
  if ($org->has_entitlement('sw_mgr_enterprise')) {
    push @ret, [ 'None', 'none' ];
    push @ret, [ $org->basic_slot_name, 'sw_mgr_entitled' ]
      if not PXT::Config->get("satellite");
    push @ret, [ $org->enterprise_slot_name, 'enterprise_entitled' ];
    push @ret, [ $org->provisioning_slot_name, 'provisioning_entitled' ]
      if $org->has_entitlement('rhn_provisioning');
    push @ret, [ $org->virtualization_slot_name, 'virtualization_host' ]
      if $org->has_entitlement('virtualization_host');
    push @ret, [ $org->virtualization_platform_slot_name, 'virtualization_host_platform' ]
      if $org->has_entitlement('virtualization_host_platform');
    push @ret, [ $org->nonlinux_slot_name, 'monitoring_entitled' ]
      if $org->has_entitlement('rhn_monitor');
    push @ret, [ $org->nonlinux_slot_name, 'nonlinux_entitled' ]
      if $org->has_entitlement('rhn_nonlinux');
  }
  else {
    @ret =
      ([ 'None', 'none' ],
       [ $org->basic_slot_name, 'sw_mgr_entitled' ]);
  }

  for my $ret (@ret) {
    $ret->[2] = 0;
    $ret->[2] = 1 if $ret->[1] eq $current_ent;
  }

  return @ret;
}

my %transitions =
  (none => [ qw/sw_mgr_entitled enterprise_entitled provisioning_entitled nonlinux_entitled/ ],
   sw_mgr_entitled => [ qw/enterprise_entitled provisioning_entitled/ ],
   enterprise_entitled => [ qw/provisioning_entitled/ ],
   provisioning_entitled => [ ]);

sub allowed_entitlement_transition {
  my $class = shift;
  my $from = shift;
  my $to = shift;

  return 1 if $from eq $to;

  return scalar grep { $to eq $_ } @{$transitions{$from}};
}

# What type of feature is this?  Management, provisioning, monitoring, etc.
sub feature_type {
  my $class = shift;
  my $feature = shift;

  if (exists $updates_features{$feature}) {
    return 'updates';
  }
  elsif (exists $management_features{$feature}) {
    return 'management';
  }
  elsif (exists $provisioning_features{$feature}) {
    return 'provisioning';
  }
  elsif (exists $monitoring_features{$feature}) {
    return 'monitoring';
  }
  elsif (exists $nonlinux_features{$feature}) {
    return 'nonlinux';
  }
  else {
    die "Invalid feature '$feature'";
  }

}

1;
