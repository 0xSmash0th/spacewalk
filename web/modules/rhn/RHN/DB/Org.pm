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

use lib '/var/www/lib';

package RHN::DB::Org;

use RHN::DB;
use RHN::DB::TableClass;
use RHN::Exception;
use RHN::Probe;

use RHN::DataSource::Channel;

use Data::Dumper;
use Date::Parse;
use Time::HiRes;
use Params::Validate qw/:all/;
Params::Validate::validation_options(strip_leading => "-");

my @org_fields = qw/ID NAME ORACLE_CUSTOMER_ID ORACLE_CUSTOMER_NUMBER CUSTOMER_TYPE CREATED MODIFIED/;

my @skip_cert_channels = qw/%%-beta%% %%-staging%% rhn-satellite%%/;

my $o = new RHN::DB::TableClass("web_customer", "O", "", @org_fields);

# loads the org for a satellite, dies if called in non-satellite context
sub satellite_org {
  my $class = shift;

  my $dbh = RHN::DB->connect;
  my $sth;

  $sth = $dbh->prepare("SELECT is_satellite() FROM DUAL");
  $sth->execute();
  my ($is_satellite)= $sth->fetchrow;
  $sth->finish;

  die 'not a satellite!' unless $is_satellite;

  $sth = $dbh->prepare("SELECT id FROM web_customer");
  $sth->execute;
  my ($org_id) = $sth->fetchrow;
  $sth->finish;

  return $class->lookup(-id => $org_id);
}

sub lookup {
  my $class = shift;
  my %params = validate(@_, { id => 0, customer_number => 0, customer_name => 0 });

  my $query;
  my $value;

  if ($params{customer_number}) {
    $query = $o->select_query("O.ORACLE_CUSTOMER_NUMBER = ?");
    $value = $params{customer_number};
  }
  elsif ($params{customer_id}) {
    $query = $o->select_query("O.ORACLE_CUSTOMER_ID = ?");
    $value = $params{customer_id};
  }
  elsif ($params{id}) {
    $query = $o->select_query("O.ID = ?");
    $value = $params{id};
  }
  else {
    Carp::croak "must use -id, -customer_number, or -customer_id in lookup";
  }

  my $dbh = RHN::DB->connect;
  my $sth;

  $sth = $dbh->prepare($query);
  $sth->execute($value);

  my @columns = $sth->fetchrow;
  $sth->finish;

  my $ret;
  if ($columns[0]) {
    $ret = $class->blank_org;

    $ret->{__id__} = $columns[0];
    $ret->$_(shift @columns) foreach $o->method_names;
    delete $ret->{":modified:"};
  }
#  else {
#    local $" = ", ";
#    die "Error loading org $value; no ID? (@columns)";
#  }
  else {
    return;
  }

  $query = <<EOQ;
  SELECT   label
    FROM   rhnOrgEntitlementType OET, rhnOrgEntitlements OE
   WHERE   OE.org_id = ?
     AND   OET.id = OE.entitlement_id
EOQ
  $sth = $dbh->prepare($query);
  $sth->execute($ret->id);

  my $has_enterprise;
  my @ents;
  while (my ($ent) = $sth->fetchrow) {
    push @ents, $ent;
    $has_enterprise = 1 if $ent eq 'sw_mgr_enterprise';
  }
  push @ents, 'sw_mgr_personal' unless $has_enterprise;
  $ret->entitlements(@ents);

  return $ret;
}


my @valid_org_entitlements = qw/sw_mgr_personal sw_mgr_enterprise rhn_provisioning rhn_nonlinux rhn_monitor rhn_solaris/;

sub has_entitlement {
  my $self = shift;
  my $entitlement = shift;

  throw "no entitlement given" unless $entitlement;
  throw "Invalid org entitlement type '$entitlement'." unless grep { $entitlement eq $_ } @valid_org_entitlements;

  return (grep { $_ eq $entitlement } $self->entitlements) ? 1 : 0;
}

sub entitlements {
  my $self = shift;

  if (@_) {
    $self->{__entitlements__} = [ @_ ];
  }

  return @{$self->{__entitlements__} || []};
}

sub blank_org {
  my $class = shift;

  my $self = bless { }, $class;

  return $self;
}

sub create_org {
  my $class = shift;

  my $org = $class->blank_org;
  $org->{__id__} = -1;

  return $org;
}

# build some accessors
foreach my $field ($o->method_names) {
  my $sub = q {
    sub [[field]] {
      my $self = shift;
      if (@_ and "[[field]]" ne "id") {
        $self->{":modified:"}->{[[field]]} = 1;
        $self->{__[[field]]__} = shift;
      }
      return $self->{__[[field]]__};
    }
  };

  $sub =~ s/\[\[field\]\]/$field/g;
  eval $sub;

  if ($@) {
    die $@;
  }
}

sub commit {
  my $self = shift;
  my $mode = 'update';

  if ($self->id == -1) {
    my $dbh = RHN::DB->connect;

    my $sth = $dbh->prepare("SELECT rhn_org_id_seq.nextval FROM DUAL");
    $sth->execute;
    my ($id) = $sth->fetchrow;
    die "No new org id from seq rhn_org_id_seq (possible error: " . $sth->errstr . ")" unless $id;
    $sth->finish;

    $self->{":modified:"}->{id} = 1;
    $self->{__id__} = $id;
    $mode = 'insert';
  }

  die "$self->commit called on org without valid id" unless $self->id and $self->id > 0;

  my @modified = keys %{$self->{":modified:"}};
  my %modified = map { $_ => 1 } @modified;

  return unless @modified;

  my $dbh = RHN::DB->connect;

  my $query;
  if ($mode eq 'update') {
    $query = $o->update_query($o->methods_to_columns(@modified));
    $query .= "O.ID = ?";
  }
  else {
    $query = $o->insert_query($o->methods_to_columns(@modified));
  }

  #warn "ins/upd query: $query";

  my $sth = $dbh->prepare($query);
  $sth->execute((map { $self->$_() } grep { $modified{$_} } $o->method_names), ($mode eq 'update') ? ($self->id) : ());

  $dbh->commit;
  $self->oai_customer_sync();

  delete $self->{":modified:"};
}

sub random_org_admin {
  my $self_or_class = shift;
  my $oid;

  if (ref $self_or_class) {
    $oid = $self_or_class->id;
  }
  else {
    $oid = shift;
  }

  my $dbh = RHN::DB->connect;
  my $query = <<EOSQL;
SELECT UGM.user_id
  FROM rhnUserGroupMembers UGM,
       rhnUserGroup UG
 WHERE UG.id = UGM.user_group_id
   AND UG.org_id = ?
   AND UG.group_type = (SELECT id FROM rhnUserGroupType WHERE label = 'org_admin')
EOSQL

  my $sth = $dbh->prepare($query);
  $sth->execute($oid);

  my ($id) = $sth->fetchrow;
  $sth->finish;

  return $id;
}

sub users_in_org {
  my $class = shift;
  my $org_id = shift;
  my @columns = @_;

  my $dbh = RHN::DB->connect;
  my $query = sprintf <<EOSQL, join(", ", map { "U.$_" } @columns);
SELECT %s
FROM rhnUser U
WHERE U.org_id = ?
EOSQL

  my $sth = $dbh->prepare($query);
  $sth->execute($org_id);

  my @ret;

  while (my @row = $sth->fetchrow) {
    push @ret, [ @row ];
  }

  return @ret;
}

# calling convention:
# RHN::DB::Org->servers_in_org(org_id, [upper, lower], columns to display)
# TODO: make servergroups_in_org, users_in_org, usergroups_in_org act like servers_in_org

sub servers_in_org {
  my $class = shift;
  my $org_id = shift;
  my $bounds = shift;
  my @columns = @_;

  if (not ref $bounds or ref $bounds ne 'ARRAY') {
    unshift @columns, $bounds;
    $bounds = [ 1, 1_000_000 ];
  }

  @columns = ('S.ID') unless @columns;

  my $dbh = RHN::DB->connect;
  my $query = sprintf <<EOSQL, join(", ", map { "S.$_" } @columns);
SELECT %s
FROM rhnServer S
WHERE S.org_id = ?
ORDER BY UPPER(S.name), S.id
EOSQL

  my $sth = $dbh->prepare($query);
  $sth->execute($org_id);

  my @ret;

  my $i = 1;
  while (my @row = $sth->fetchrow) {
    if ($i >= $bounds->[0] and $i <= $bounds->[1]) {
      push @ret, [ @row ];
    } else {
      last;
    }
    $i++;
  }
  $sth->finish;

  return @ret;
}

sub usergroups_in_org {
  my $class = shift;
  my $org_id = shift;
  my @columns = @_;

  my $dbh = RHN::DB->connect;
  my $query = sprintf <<EOSQL, join(", ", map { "UG.$_" } @columns);
SELECT %s
FROM rhnUserGroup UG
WHERE UG.org_id = ?
EOSQL

#  warn "q: $query";

  my $sth = $dbh->prepare($query);
  $sth->execute($org_id);

  my @ret;

  while (my @row = $sth->fetchrow) {
    push @ret, [ @row ];
  }

  return @ret;
}

sub servergroups_in_org {
  my $class = shift;
  my $org_id = shift;
  my @columns = @_ ? @_ : ("ID");

  my $dbh = RHN::DB->connect;
  my $query = sprintf <<EOSQL, join(", ", map { "SG.$_" } @columns);
SELECT %s
FROM rhnServerGroup SG
WHERE SG.org_id = ?
  AND SG.group_type is NULL
EOSQL

  my $sth = $dbh->prepare($query);
  $sth->execute($org_id);

  my @ret;

  while (my @row = $sth->fetchrow) {
    push @ret, [ @row ];
  }

  return @ret;
}


sub users_in_org_overview {
  my $class = shift;
  my %params = @_;

  my ($org_id, $lower, $upper, $total_ref, $mode, $mode_params, $all_ids) =
    map { $params{"-" . $_} } qw/org_id lower upper total_rows mode mode_params all_ids/;

  $lower ||= 1;
  $upper ||= 100000;

  my $dbh = RHN::DB->connect;
  my $query;

  if ($mode eq 'ugroup') {
    $query = <<EOQ;
SELECT    OV.USER_ID, USER_LOGIN, USER_FIRST_NAME, USER_LAST_NAME, USER_MODIFIED, SERVER_COUNT, SERVER_GROUP_COUNT, ROLE_NAMES
  FROM    rhnUsersInOrgOverview OV, rhnUserGroupMembers UGM
 WHERE    ORG_ID = ? AND OV.USER_ID = UGM.user_id AND UGM.user_group_id = ?
ORDER BY  UPPER(USER_LAST_NAME), UPPER(USER_FIRST_NAME), UPPER(USER_ID)
EOQ
  }
  elsif ($mode eq 'sgroup') {
    $query = <<EOQ;
SELECT    OV.USER_ID, USER_LOGIN, USER_FIRST_NAME, USER_LAST_NAME, USER_MODIFIED, SERVER_COUNT, SERVER_GROUP_COUNT, ROLE_NAMES
  FROM    rhnUsersInOrgOverview OV, rhnUserManagedServerGroups UMSG
 WHERE    OV.ORG_ID = ? AND OV.USER_ID = UMSG.user_id AND UMSG.server_group_id = ?
ORDER BY  UPPER(USER_LAST_NAME), UPPER(USER_FIRST_NAME), OV.USER_ID
EOQ
  }
  elsif ($mode eq 'server') {
    $query = <<EOQ;
SELECT    DISTINCT OV.USER_ID, USER_LOGIN, USER_FIRST_NAME, USER_LAST_NAME, USER_MODIFIED, SERVER_COUNT, SERVER_GROUP_COUNT, ROLE_NAMES
  FROM    rhnUsersInOrgOverview OV, rhnUserServerPerms SP
 WHERE    OV.ORG_ID = ? AND SP.USER_ID = OV.USER_ID AND SP.SERVER_ID = ?
ORDER BY  UPPER(USER_LAST_NAME), UPPER(USER_FIRST_NAME), USER_ID
EOQ
  }
  elsif ($mode eq 'set' or $mode eq 'search_set') {
    $query = <<EOQ;
SELECT    UO.USER_ID, USER_LOGIN, USER_FIRST_NAME, USER_LAST_NAME, USER_MODIFIED, SERVER_COUNT, SERVER_GROUP_COUNT, ROLE_NAMES
  FROM    rhnUsersInOrgOverview UO, rhnSet RS
 WHERE    ORG_ID = ? AND UO.USER_ID = RS.element AND RS.label = ? AND RS.user_id = ?
ORDER BY  UPPER(USER_LAST_NAME), UPPER(USER_FIRST_NAME), UO.USER_ID
EOQ
  }
  else {
    $query = <<EOQ;
SELECT    USER_ID, USER_LOGIN, USER_FIRST_NAME, USER_LAST_NAME, USER_MODIFIED, SERVER_COUNT, SERVER_GROUP_COUNT, ROLE_NAMES
  FROM    rhnUsersInOrgOverview
 WHERE    ORG_ID = ?
ORDER BY  UPPER(USER_LAST_NAME), UPPER(USER_FIRST_NAME), USER_ID
EOQ
  }

#  warn "query: $query";
  my $sth = $dbh->prepare($query);

  $sth->execute($org_id, @{ref $mode_params ? $mode_params : [ $mode_params ]});

  $$total_ref = 0;

  my @result;
  my $i = 1;
  while (my @data = $sth->fetchrow) {
    $$total_ref = $i;
    push @$all_ids, $data[0] if $all_ids;

    if ($i >= $lower and $i <= $upper) {
      push @result, [ @data ];
    }
    $i++;
  }
  $sth->finish;
  return @result;
}

sub user_count {
  my $self = shift;

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare('SELECT COUNT(id) FROM web_contact WHERE org_id = ?');
  $sth->execute($self->id);
  my ($count) = $sth->fetchrow;
  $sth->finish;

  return $count;
}

sub user_group_id {
  my $self = shift;
  my $label = shift;

  die "No label" unless $label;

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare('SELECT UG.id FROM rhnUserGroupType UGT, rhnUserGroup UG WHERE UG.org_id = ? AND UG.group_type = UGT.id AND UGT.label = ?');
  $sth->execute($self->id, $label);
  my ($id) = $sth->fetchrow;
  $sth->finish;

  return $id;
}

sub user_applicant_group {
  my $self = shift;

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare('SELECT UG.id FROM rhnUserGroupType UGT, rhnUserGroup UG WHERE UG.org_id = ? AND UG.group_type = UGT.id AND UGT.label = ?');
  $sth->execute($self->id, 'org_applicant');
  my ($id) = $sth->fetchrow;
  $sth->finish;

  return $id;
}

sub create_new_org {
  my $class = shift;
  my %params = @_;

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare(<<EOQ);
BEGIN
  CREATE_NEW_ORG(name_in => :org_name, password_in => :org_password,
                 oracle_customer_number_in => :oracle_customer_number,
                 oracle_customer_id_in => :oracle_customer_id,
                 customer_type_in => :customer_type,
                 org_id_out => :org_id, org_admin_group_out => :org_admin_group,
                 org_app_group_out => :org_app_group,
                 creation_location => 'rhn');
END;
EOQ

  $sth->bind_param(":org_name" => $params{"-org_name"});
  $sth->bind_param(":org_password" => $params{"-org_password"});

  $sth->bind_param(":${_}" => $params{"-$_"})
    foreach qw/oracle_customer_number oracle_customer_id customer_type/;

  my ($org_id, $org_admin_group, $org_app_group);
  $sth->bind_param_inout(':org_id' => \$org_id, 4096);
  $sth->bind_param_inout(':org_admin_group' => \$org_admin_group, 4096);
  $sth->bind_param_inout(':org_app_group' => \$org_app_group, 4096);
  $sth->execute;

  if ($params{-commit}) {
    $dbh->commit;
  }

  return ($org_id, $org_admin_group, $org_app_group);
}

sub user_applet_overview {
  my $class = shift;
  my $user_id = shift;

  my $dbh = RHN::DB->connect;

  my $sth = $dbh->prepare("SELECT COUNT(server_id) FROM rhnUserServerPerms WHERE user_id = ?");
  $sth->execute($user_id);
  my ($count) = $sth->fetchrow;
  $sth->finish;

  $sth = $dbh->prepare(<<EOSQL);
SELECT security_count, bug_count, enhancement_count
  FROM rhnUserAppletOverview
 WHERE user_id = ?
EOSQL
  $sth->execute($user_id);

  my ($sec, $bug, $enh) = $sth->fetchrow;
  $sth->finish;

  return ($count, $sec, $bug, $enh);
}

sub entitlement_counts {
  my $self = shift;
  my $entitlement = shift;

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare(<<EOQ);
SELECT current_members, max_members
  FROM rhnServerGroup SG
 WHERE SG.group_type = (SELECT id FROM rhnServerGroupType WHERE label = ?)
   AND SG.org_id = ?
EOQ

  $sth->execute($entitlement, $self->id);
  my @ret = $sth->fetchrow;
  $sth->finish;

  return @ret;
}

sub entitlement_data {
  my $self = shift;

  my $dbh = RHN::DB->connect;

  my $ent_data = { };

  foreach my $level (qw/sw_mgr_entitled enterprise_entitled provisioning_entitled
			monitoring_entitled/) {

    my ($used, $max) = $self->entitlement_counts($level);

    $used ||= 0;
    $max ||= 0;

    $ent_data->{$level}->{used} = $used;
    $ent_data->{$level}->{max} = $max;
    $ent_data->{$level}->{available} = $max - $used;
  }

  $ent_data->{sw_mgr_entitled} ||= 1;

  return $ent_data;
}

# How many systems in the org?
sub server_count {
  my $self = shift;

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare(<<EOS);
SELECT COUNT(S.id)
  FROM rhnServer S
 WHERE S.org_id = ?
EOS
  $sth->execute($self->id);
  my ($server_count) = $sth->fetchrow;
  $sth->finish;

  return ($server_count || 0);
}

#How many unused basic or enterprise slots does the org have?
sub unused_entitlements {
  my $self = shift;

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare(<<EOS);
SELECT SUM(max_members - current_members)
  FROM rhnServerGroup
 WHERE org_id = :org_id
   AND (   group_type = (SELECT id FROM rhnServerGroupType WHERE label = 'sw_mgr_entitled')
        OR group_type = (SELECT id FROM rhnServerGroupType WHERE label = 'enterprise_entitled'))
EOS
  $sth->execute_h(org_id => $self->id);
  my ($tot) = $sth->fetchrow;
  $sth->finish;

  return $tot;
}

sub owns_servers {
  my $self = shift;
  my @server_ids = @_;

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare("SELECT org_id FROM rhnServer WHERE id = ?");

  foreach my $server (@server_ids) {
    $sth->execute($server);
    my ($org_id) = $sth->fetchrow;
    $sth->finish;

    return 0 if not defined $org_id;
    return 0 if $org_id != $self->id;
  }

  return 1;
}

sub owns_server_groups {
  my $self = shift;
  my @sg_ids = @_;

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare("SELECT org_id FROM rhnServerGroup WHERE id = ?");

  foreach my $sgid (@sg_ids) {
    $sth->execute($sgid);
    my ($org_id) = $sth->fetchrow;
    $sth->finish;

    return 0 if $org_id != $self->id;
  }

  return 1;
}

sub update_errata_cache {
  my $self = shift;
  my $threshold = shift || 20;

  my $dbh = RHN::DB->connect;

  my $sth;
  $sth = $dbh->prepare("SELECT server_count FROM rhnOrgErrataCacheQueue WHERE org_id = ? AND processed = 0");
  $sth->execute($self->id);
  my ($server_count) = $sth->fetchrow;
  $sth->finish;

  # has the org been flagged as needing an EC update?  if not, bail

  return unless defined $server_count;

  # is the org small enough to work realtime?  if so, let's do it now,
  # otherwise an external process will do it (or already has).
  # typically this external process is the Errata Cache script in
  # rhn/sql/scripts

  if ($server_count < $threshold) {
    PXT::Debug->log(2, "Small org, using direct EC update");

    $sth = $dbh->prepare("SELECT id FROM rhnServer WHERE org_id = ?");
    $sth->execute($self->id);

    while (my ($sid) = $sth->fetchrow) {
      RHN::DB::Server->update_cache_for_server($dbh, $sid);
    }

    $sth = $dbh->prepare("DELETE FROM rhnOrgErrataCacheQueue WHERE org_id = ?");
    $sth->execute($self->id);
    $dbh->commit;
  }

}

sub has_channel_permission {
  my $self = shift;
  my $channel_id = shift;

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare(<<EOQ);
SELECT  AC.channel_name
  FROM  rhnAvailableChannels AC
 WHERE  AC.org_id = ?
   AND  AC.channel_id = ?
EOQ

  $sth->execute($self->id, $channel_id);

  my @rows = $sth->fetchrow;
  $sth->finish;

  return 1 if (@rows);
  return 0;
}

sub support_user_overview {
  my $class = shift;
  my %params = @_;

  my $org_id;

  if (exists $params{-org_id}) {
    $org_id = $params{-org_id};
  }
  elsif (exists $params{-custnum}) {
    my $dbh = RHN::DB->connect;
    my $sth = $dbh->prepare('SELECT id FROM web_customer WHERE oracle_customer_number = ?');

    $sth->execute($params{-custnum});
    ($org_id) = $sth->fetchrow;
    $sth->finish;
  }

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare('SELECT id, login FROM web_contact WHERE org_id = ?');

  $sth->execute($org_id);

  my @ret;
  while (my @row = $sth->fetchrow) {
    push @ret, [ @row ];
  }

  return @ret;
}

sub orders_for_org {
  my $class = shift;
  my $oid = shift;

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare(<<EOS);
SELECT CUSTOMER_NUMBER, WEB_ORDER_NUMBER, LINE_ID, PRODUCT_ITEM_CODE, PRODUCT_NAME, QUANTITY
  FROM user_orders
 WHERE customer_number = (SELECT oracle_customer_id FROM web_customer cu WHERE cu.id = ?)
EOS

  $sth->execute($oid);

  my @ret;
  while (my @row = $sth->fetchrow) {
    push @ret, [ @row ];
  }

  return @ret;
}

sub products_for_org {
  my $class = shift;
  my $oid = shift;

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare(<<EOS);
SELECT REGISTRATION_USER_ID, REG_NUMBER, SERVICE_TAG, PRODUCT_DESCRIPTION, PRODUCT_ITEM_CODE,
       PRODUCT_START_DATE, PRODUCT_END_DATE, PRODUCT_ACTIVE_FLAG, PRODUCT_QUANTITY,
       SERVICE_DESCRIPTION, SERVICE_ITEM_CODE, SERVICE_START_DATE, SERVICE_END_DATE,
       SERVICE_ACTIVE_FLAG
  FROM user_products
 WHERE org_id = ?
EOS

  $sth->execute($oid);

  my @ret;
  while (my @row = $sth->fetchrow) {
    push @ret, [ @row ];
  }

  return @ret;
}

sub pending_users {
  my $class = shift;
  my $org_id = shift;

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare(<<EOS);
SELECT wc.id, wc.login, wupi.first_names, wupi.last_name, wupi.title, wupi.email
  FROM web_user_personal_info wupi,
       web_contact wc,
       rhnUserGroupMembers UGM
 WHERE UGM.user_group_id = (SELECT id
                              FROM rhnUserGroup
                             WHERE org_id = ?
                               AND group_type = (SELECT id FROM rhnUserGroupType WHERE label = 'org_applicant'))
   AND UGM.user_id = wc.id
   AND wupi.web_user_id = wc.id
EOS

  $sth->execute($org_id);

  my @ret = $sth->fullfetch;

  return @ret;
}

sub org_admins {
  my $self = shift;

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare(<<EOS);
SELECT ugm.user_id
  FROM rhnUserGroupMembers ugm,
       web_contact wc
 WHERE ugm.user_group_id = (SELECT id
                              FROM rhnUserGroup
                             WHERE org_id = ?
                               AND group_type = (SELECT id FROM rhnUserGroupType WHERE label = 'org_admin'))
   AND wc.id = ugm.user_id
ORDER BY wc.id
EOS
  $sth->execute($self->id);

  my @ret;
  while (my ($id) = $sth->fetchrow) {
    push @ret, $id;
  }

  return @ret;
}

# find a user who is responsible (org admin; current policy is org
# admin with lowest id)
sub find_responsible_user {
  my $self = shift;

  my @ret = $self->org_admins;
  if (@ret) {
    return RHN::User->lookup(-id => $ret[0]);
  }
  else {
    return undef;
  }
}

sub entitled_satellite_families {
  my $self = shift;

  my @families;

  foreach my $fam (qw/rhn-satellite/) {
    push @families, $fam if $self->has_channel_family_entitlement($fam);
  }

  return @families;
}

sub has_channel_family_entitlement {
  my $self = shift;
  my $label = shift;

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare(<<EOS);
SELECT 1
  FROM rhnChannelFamilyPermissions CFP
 WHERE CFP.channel_family_id = (SELECT id FROM rhnChannelFamily WHERE label = ?)
   AND (CFP.org_id IS NULL OR CFP.org_id = ?)
EOS
  $sth->execute($label, $self->id);
  my ($ret) = $sth->fetchrow;

  $sth->finish;

  return $ret;
}

sub entitlement_history {
  my $self = shift;

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare(<<EOS);
SELECT wce.customer_id,
       wce.active_flag,
       TO_CHAR(wce.start_date_active, 'YYYY-MM-DD'),
       TO_CHAR(wce.end_date_active, 'YYYY-MM-DD'),
       wce.product_name,
       wce.service_name,
       wce.product_item_code,
       wce.service_item_code,
       wce.group_label,
       wce.quantity,
       wce.customer_product_id,
       wce.paid
  FROM web_customer_entitlements wce
 WHERE wce.customer_id = ?
EOS

  $sth->execute($self->id);

  my @ret = $sth->fullfetch;

  return @ret;
}

sub rename_customer {
  my $self = shift;
  my $name = shift;

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare("UPDATE web_customer SET name = ? WHERE id = ?");
  $sth->execute($name, $self->id);

  $sth = $dbh->prepare("UPDATE web_user_personal_info SET company = ? WHERE web_user_id IN (SELECT id FROM web_contact WHERE org_id = ?)");
  $sth->execute($name, $self->id);

  $self->name($name);

  $self->oai_customer_sync();
}

sub oai_customer_sync {
  my $self = shift;

  if (PXT::Config->get('enable_oai_sync')) {
    warn "OAI customer sync";
    my $dbh = RHN::DB->connect;

    $dbh->call_procedure("XXRH_OAI_WRAPPER.sync_customer", $self->id);
    $dbh->commit;
  }
  else {
    warn "No OAI customer sync";
  }
}

#get the org's channelfamil(y|ies)
sub get_channel_family {
  my $self = shift;
  my $org_id = shift || $self->id || return;

  my $dbh = RHN::DB->connect;
  my $query = <<EOQ;
  SELECT CF.id
    FROM rhnChannelFamily CF
   WHERE CF.org_id = ?
ORDER BY CF.id
EOQ

  my $sth = $dbh->prepare($query);
  $sth->execute($org_id);

  my @channel_family_ids;

  while (my ($data) = $sth->fetchrow) {
    push @channel_family_ids, $data;
  }

  return @channel_family_ids;

}

# all roles that are available to the org
sub available_roles {
  my $self = shift;

  my $dbh = RHN::DB->connect;
  my $query =<<EOQ;
SELECT  DISTINCT UGT.label
  FROM  rhnUserGroupType UGT, rhnUserGroup UG
 WHERE  UGT.id = UG.group_type
   AND  UG.org_id = ?
EOQ

  my $sth = $dbh->prepare($query);
  $sth->execute($self->id);

  my @roles;

  while (my ($role) = $sth->fetchrow) {
    push @roles, $role;
  }

  return @roles;
}

sub has_role {
  my $self = shift;
  my $role = shift;

  return unless $role;

  my %roles = map { ($_, 1) } $self->available_roles;

  return (exists $roles{$role} ? 1 : 0);
}


sub server_group_count {
  my $self = shift;

  my $dbh = RHN::DB->connect;
  my $query =<<EOQ;
SELECT COUNT(id)
  FROM rhnServerGroup
 WHERE org_id = ?
   AND group_type IS NULL
EOQ

  my $sth = $dbh->prepare($query);
  $sth->execute($self->id);

  my ($count) = $sth->fetchrow;

  $sth->finish;

  return $count;

}

# this should become the cannonical answer to "is this person a paying customer of RHN?"
sub is_paying_customer {
  my $self = shift;

  my $dbh = RHN::DB->connect;
  return $dbh->call_function("rhn_bel.is_org_paid", $self->id);
}

# generalized slot name function; also caches for better performance
sub slot_name {
  my $self = shift;
  my $ent = shift;

  if ($ent eq 'sw_mgr_entitled' and PXT::Config->get('satellite')) {
    return 'VERY BROKEN UPDATE ENTITLEMENT';
  }

  if (exists $self->{cached_slot_names}) {
    return $self->{cached_slot_names}->{$ent};
  }

  my %entitlement_name =
    (none => 'None',
     sw_mgr_entitled => 'Update', 
     enterprise_entitled => 'Management',
     provisioning_entitled => 'Provisioning',
     virtualization_host => 'Virtualization',
     virtualization_host_platform => 'Virtualization Platform',
     monitoring_entitled => 'Monitoring',
     nonlinux_entitled => 'Non-Linux');

  $self->{cached_slot_names} = \%entitlement_name;

  return $self->{cached_slot_names}->{$ent};
}

sub basic_slot_name {
  my $self = shift;

  return $self->slot_name('sw_mgr_entitled');
}

sub enterprise_slot_name {
  my $self = shift;

  return $self->slot_name('enterprise_entitled');
}

sub provisioning_slot_name {
  my $self = shift;

  return $self->slot_name('provisioning_entitled');
}

sub monitoring_slot_name {
  my $self = shift;

  return $self->slot_name('monitoring_entitled');
}

sub nonlinux_slot_name {
  my $self = shift;

  return $self->slot_name('nonlinux_entitled');
}

sub virtualization_slot_name {
  my $self = shift;

  return $self->slot_name('virtualization_host');
}

sub virtualization_platform_slot_name {
  my $self = shift;

  return $self->slot_name('virtualization_host_platform');
}


sub org_channel_setting {
  my $self = shift;
  my $cid = shift;
  my $label = shift;

  throw "No channel id" unless $cid;
  throw "No label id" unless $label;

  my $dbh = RHN::DB->connect;
  my $query =<<EOQ;
SELECT 1
  FROM rhnOrgChannelSettings OCS, rhnOrgChannelSettingsType OCST
 WHERE OCS.org_id = :org_id
   AND OCS.channel_id = :cid
   AND OCST.label = :label
   AND OCST.id = OCS.setting_id
EOQ

  my $sth = $dbh->prepare($query);

  $sth->execute_h(org_id => $self->id, cid => $cid, label => $label);
  my ($setting) = $sth->fetchrow;

  $sth->finish;

  return ($setting) ? 1 : 0;
}

sub remove_org_channel_setting {
  my $self = shift;
  my $cid = shift;
  my $label = shift;

  throw "No channel id" unless $cid;
  throw "No label" unless $label;

  my $dbh = RHN::DB->connect;
  my $query =<<EOQ;
DELETE
  FROM rhnOrgChannelSettings OCS
 WHERE OCS.org_id = :org_id
   AND OCS.channel_id = :cid
   AND OCS.setting_id = (SELECT id FROM rhnOrgChannelSettingsType WHERE label = :label)
EOQ

  my $sth = $dbh->prepare($query);

  $sth->execute_h(org_id => $self->id, cid => $cid, label => $label);

  $dbh->commit;
}

sub add_org_channel_setting {
  my $self = shift;
  my $cid = shift;
  my $label = shift;

#delete it first...

  $self->remove_org_channel_setting($cid, $label);

  my $dbh = RHN::DB->connect;
  my $query =<<EOQ;
INSERT
  INTO rhnOrgChannelSettings
       (org_id, channel_id, setting_id)
VALUES (:org_id, :cid, (SELECT id FROM rhnOrgChannelSettingsType WHERE label = :label))
EOQ

  my $sth = $dbh->prepare($query);
  $sth->execute_h(org_id => $self->id, cid => $cid, label => $label);

  $dbh->commit;

  return;
}

sub users_in_org_with_channel_role {
  my $self = shift;
  my %attr = validate(@_, { cid => 1, role => 1 });

  my $dbh = RHN::DB->connect;
  my $query =<<EOQ;
SELECT DISTINCT CP.user_id
  FROM rhnChannelPermission CP, rhnChannelPermissionRole CPR, web_contact WC
 WHERE CP.channel_id = :cid
   AND CP.user_id = WC.id
   AND WC.org_id = :org_id
   AND CP.role_id = CPR.id
   AND CPR.label = :role_label
EOQ

  my $sth = $dbh->prepare($query);
  $sth->execute_h(org_id => $self->id, cid => $attr{cid}, role_label => $attr{role});

  my @uids;

  while (my ($uid) = $sth->fetchrow) {
    push @uids, $uid;
  }

  return @uids;
}


sub remove_channel_permissions {
  my $self = shift;
  my %attr = validate(@_, { uids => 1, role => 1, cid => 1, transaction => 0});

  my $dbh = $attr{transaction} || RHN::DB->connect;

  my $query =<<EOQ;
DELETE
  FROM rhnChannelPermission CP
 WHERE CP.user_id = :user_id
   AND CP.channel_id = :cid
   AND CP.role_id = (SELECT id FROM rhnChannelPermissionRole WHERE label = :role_label)
EOQ

  my $sth = $dbh->prepare($query);

  foreach my $uid (@{$attr{uids}}) {
    $sth->execute_h(cid => $attr{cid}, user_id => $uid, role_label => $attr{role});
  }
  $sth->finish;

  unless ($attr{transaction}) {
    $dbh->commit;
  }

  return $dbh;
}


sub reset_channel_permissions {
  my $self = shift;
  my %attr = validate(@_, { uids => 1, role => 1, cid => 1 });

  die "uids param is not an arrayref" unless (ref $attr{uids} eq 'ARRAY');

  my $dbh = RHN::DB->connect;

  $attr{transaction} = $dbh;
  $dbh = $self->remove_channel_permissions(%attr);

  my $query =<<EOQ;
INSERT
  INTO rhnChannelPermission
       (channel_id, user_id, role_id)
VALUES (:cid, :user_id, (SELECT id FROM rhnChannelPermissionRole WHERE label = :role_label))
EOQ

  my $sth = $dbh->prepare($query);

  foreach my $uid (@{$attr{uids}}) {
    $sth->execute_h(cid => $attr{cid}, user_id => $uid, role_label => $attr{role});
  }

  $sth->finish;
  $dbh->commit;
}

sub channel_entitlements {
  my $self = shift;

  my $ds = new RHN::DataSource::Channel(-mode => 'channel_entitlements');
  my $channels = $ds->execute_query(-org_id => $self->id);

  foreach my $row (@{$channels}) {
    $row->{AVAILABLE_MEMBERS} = defined $row->{MAX_MEMBERS} ? ($row->{MAX_MEMBERS} - $row->{CURRENT_MEMBERS}) : undef;
  }

  return $channels;
}

sub quota_data {
  my $self = shift;

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare(<<EOQ);
SELECT OQ.total, OQ.bonus, OQ.used
  FROM rhnOrgQuota OQ
 WHERE OQ.org_id = :org_id
EOQ

  $sth->execute_h(org_id => $self->id);
  my $data = $sth->fetchrow_hashref;

  $sth->finish;

  return unless (ref $data eq 'HASH');

  $data->{LIMIT} = $data->{TOTAL} + $data->{BONUS};
  $data->{AVAILABLE} = $data->{LIMIT} - $data->{USED};

  return $data;
}

sub validate_cert {
  my $self = shift;

  my $dbh = RHN::DB->connect;
  my $sth = $dbh->prepare(<<EOQ);
SELECT MAX(SC.expires) as expires
  FROM rhnSatelliteCert SC
 WHERE SC.label = 'rhn-satellite-cert'
   AND version = (SELECT MAX(version) from rhnSatelliteCert
                   WHERE label = 'rhn-satellite-cert')
 GROUP BY SC.label
EOQ

  $sth->execute_h();
  my $data = $sth->fetchrow_hashref;

  $sth->finish;

  my $expDate = Date::Parse::str2time($data->{EXPIRES});
  my $currDate = time();

  if ($currDate < $expDate) {
      return 1;
  }
  else {
      return 0;
  }
}


sub available_cert_channels {
  my $self = shift;

  my $dbh = RHN::DB->connect;
  my $stmt =  <<EOQ;
SELECT  
        cf.id                           id,
        cf.name                         name,
        cf.label                        label
FROM    rhnChannelFamily                cf
WHERE   1=1
    and cf.org_id is null
EOQ

  foreach my $skip_clause (@skip_cert_channels) {
    $stmt .= " and cf.label not like '$skip_clause'";
  }
  $stmt .= " order by cf.label";

  my $sth = $dbh->prepare($stmt);

  $sth->execute_h();

  my @result;

  while ( my (@data) = $sth->fetchrow ) {
      push ( @result, \@data );
  }

  $sth->finish;

  return @result;
  
}


# Get options defined for this Org's list of monitoring Scouts
sub get_scout_options {
    my $self = shift;
    my $scouts = RHN::Probe->list_scouts($self->id);
    my @scout_options = map { { value => $_->{SAT_CLUSTER_ID}, label => $_->{DESCRIPTION} } } @{$scouts};
    return @scout_options;
}

1;
