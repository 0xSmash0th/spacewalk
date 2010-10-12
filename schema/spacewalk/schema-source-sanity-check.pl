#!/usr/bin/perl

use strict;
use warnings FATAL => 'all';

use File::Find ();
use Getopt::Long ();
use Digest::SHA1 ();

my %files;
my $show_ignored = 0;
Getopt::Long::GetOptions('I' => \$show_ignored) or exit 9;

for my $dir (qw( common oracle postgres )) {
	File::Find::find(sub {
		my $name = $File::Find::name;
		if ($name eq $dir) {
			return;
		}
		if (not -f $_) {
			return;
		}
		if (substr($name, 0, length($dir) + 1) ne "$dir/") {
			die "In dir [$dir] we got [$name]\n";
		}
		my $rname = substr($name, length($dir) + 1);
		$files{$dir}{$rname} = $_;
		}, $dir);
}

my $error = 0;
for my $c (sort keys %{ $files{common} }) {
	for my $o (qw( oracle postgres )) {
		next unless $o =~ /\.(sql|pks|pkb)$/;
		if (exists $files{$o}{$c}) {
			print "Common file [$c] is also in $o\n";
			$error = 1;
		}
	}
}

for my $c (sort keys %{ $files{oracle} }) {
	next unless $c =~ /\.(sql|pks|pkb)$/;
	if (not exists $files{postgres}{$c}) {
		print "Oracle file [$c] is not in postgres (ignoring for now)\n" if $show_ignored;
		# $error = 1;
	}
}

for my $c (sort keys %{ $files{postgres} }) {
	next unless $c =~ /\.(sql|pks|pkb)$/;
	local *FILE;
	open FILE, "postgres/$c" or do {
		print "Error reading postgres/$c: $!\n";
		$error = 1;
		next;
	};
	my $first_line = <FILE>;
	close FILE;
	if (not $first_line =~ /^-- oracle equivalent source (?:(none)|sha1 ([0-9a-f]{40}))$/) {
		print "File postgres/$c does not specify SHA1 of Oracle source nor none\n" if $show_ignored;
		# $error = 1;
		next;
	}
	my $oracle_sha1 = $2;
	if (defined $1 and $1 eq 'none') {
		# the PostgreSQL source says there is no Oracle equivalent
		if (exists $files{oracle}{$c}) {
			print "PostgreSQL file [$c] claims it has no Oracle equivalent, but it exists\n";
			$error = 1;
		}
		next;
	}
	if (not exists $files{oracle}{$c}) {
		print "Postgres file [$c] is not in oracle\n";
		$error = 1;
		next;
	}
	open FILE, "oracle/$c" or do {
		print "Error reading oracle/$c to verify SHA1: $!\n";
		$error = 1;
		next;
	};
	my $sha1 = new Digest::SHA1;
	$sha1->addfile(\*FILE);
	my $sha1_hex = $sha1->hexdigest();
	close FILE;
	if ($oracle_sha1 ne $sha1_hex) {
		print "File postgres/$c says SHA1 of Oracle source should be [$oracle_sha1] but it is [$sha1_hex]\n";
		$error = 1;
	}
}

exit $error;

