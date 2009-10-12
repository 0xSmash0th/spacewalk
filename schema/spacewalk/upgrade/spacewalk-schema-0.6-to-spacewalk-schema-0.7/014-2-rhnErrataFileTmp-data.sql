insert into rhnChecksum (id, checksum_type_id, checksum)
        (select e.id, md5.id, e.md5sum
           from rhnErrataFileTmp e,
                (select id from rhnChecksumType where label = 'md5') md5
        );

update rhnErrataFileTmp e
   set checksum_id = (select c.id from rhnChecksum c where c.checksum = e.md5sum);

commit;

